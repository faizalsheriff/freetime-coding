package com.rockwellcollins.cs.hcms.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.rockwellcollins.cs.hcms.core.profiling.ProfileHandler;
import com.rockwellcollins.cs.hcms.core.services.logging.LoggingService;
import com.rockwellcollins.cs.hcms.core.services.logging.LoggingType;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateHandler;


/**
 * UnitManager is a static class that is responsible for creation of components
 * from the LCP (Loadable Configuration Package) and contains many commonly used
 * methods such as Logging, Serialization and Runtime creation of objects.
 * 
 * The UnitManager is also the starting point of the primary running thread.
 * 
 * @author getownse
 * 
 */
public class UnitManager {
	private static String LRULCPFilePath = "";
	private static String SYSLCPFilePath = "";
	private static String DEF_LOG_FILE_EXT = "log";
	public static final class Network {

		public final static ArrayList<InetAddress> getFaceIpAddresses() {

			final ArrayList<InetAddress> ipAddresses = new ArrayList<InetAddress>();
			try {

				Enumeration<NetworkInterface> en;

				en = NetworkInterface.getNetworkInterfaces();

				if (en == null) {
					return ipAddresses;
				}

				while (en.hasMoreElements()) {

					final NetworkInterface face = en.nextElement();

					if (face != null) {

						final Enumeration<InetAddress> ea = face
								.getInetAddresses();

						if (ea != null) {

							while (ea.hasMoreElements()) {
								final InetAddress addr = ea.nextElement();

								if (addr != null) {
									ipAddresses.add(addr);
								}
							}
						}
					}
				}
			} catch (final SocketException e) {
				UnitManager.Logging
						.logWarning("Network in Utils could not get network interfaces.");
			}

			return ipAddresses;
		}

		public final static NetworkInterface getNetworkInterface(
				final InetAddress ipAddress) {
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface
						.getNetworkInterfaces();
				if (interfaces != null) {
					while (interfaces.hasMoreElements()) {
						NetworkInterface nif = interfaces.nextElement();
						Enumeration<InetAddress> ips = nif.getInetAddresses();
						if (ips != null) {
							while (ips.hasMoreElements()) {
								InetAddress ip = ips.nextElement();
								if (ip.equals(ipAddress)) {
									return nif;
								}
							}
						}
					}
				}
			} catch (final Exception e) {
				UnitManager.Logging
						.logWarning("Network in Utils could not get network interfaces.");
			}
			return null;
		}
	}

	/**
	 * All System and Unit Logging should go through the UnitManager Logging.
	 * 
	 * There are multiple types of logging.
	 * 
	 * Debug: Developer logs for testing. Debug logs can output in high volumes
	 * (for example: in for loops)
	 * 
	 * Info: Event milestones. Info should not be in any logical loops.
	 * 
	 * Profile: Profiling information. Profiles should be removed from code for
	 * official releases.
	 * 
	 * Warning: An unexpected or expected issue occurred that should be
	 * reviewed; however, the system will continue to run properly.
	 * 
	 * Severe: An unexpected issue occurred that should be reviewed; the system
	 * may not continue to run properly.
	 * 
	 * @author getownse
	 * 
	 */
	public static final class Logging {

		private static boolean debug;

		private static boolean info;

		private static boolean profile;

		private static boolean core;

		private static boolean stdout;

		private static long mcdTimeDiff = 0;
		
		private static final DateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");

		/**
		 * Queries if the UnitManager is currently processing debug messages
		 * 
		 * @return true if processing debug messages
		 */
		public static final boolean isDebug() {
			return debug;
		}

		/**
		 * Queries if the UnitManager is currently processing core messages
		 * 
		 * @return true if processing core messages
		 */
		public static final boolean isCore() {
			return core;
		}

		/**
		 * Queries if the UnitManager is currently processing info messages
		 * 
		 * @return true if processing info messages
		 */
		public static final boolean isInfo() {
			return info;
		}

		/**
		 * Queries if the UnitManager is currently processing profile messages
		 * 
		 * @return true if processing profile messages
		 */
		public static final boolean isProfile() {
			return profile;
		}

		/**
		 * Toggle if the UnitManager should process core messages
		 * 
		 * @param value
		 *            true if the UnitManager should process core messages
		 */
		public static final void setCore(final boolean value) {
			core = value;
		}

		/**
		 * Toggle if the UnitManager should process profile messages
		 * 
		 * @param value
		 *            true if the UnitManager should process profile messages
		 */
		public static final void setProfile(final boolean value) {
			profile = value;
		}

		/**
		 * Toggle if the UnitManager should process debug messages
		 * 
		 * @param value
		 *            true if the UnitManager should process debug messages
		 */
		public static final void setDebug(final boolean value) {
			debug = value;
		}

		/**
		 * Toggle if the UnitManager should process info messages
		 * 
		 * @param value
		 *            true if the UnitManager should process info messages
		 */
		public static final void setInfo(final boolean value) {
			info = value;
		}

		/**
		 * Logs a message directly to standard out.
		 * 
		 * @param logMessage
		 *            message to send to standard out.
		 */
		public static final void logStdOut(final String logMessage) {
			if (stdout) {
				System.out
						.println("[" + getFormattedTime() + "] " + logMessage);
			}
		}

		public static final void logStdErr(final String logMessage) {
			System.err.println("[" + getFormattedTime() + "] " + logMessage);
		}

		/**
		 * Sends a message to the profile log
		 * 
		 * @param logmessage
		 *            profile message
		 */
		public static final void logProfile(final String logmessage) {

			if (profile) {
				//logStdOut("PROFILE: " + logmessage);
				System.out
					.println("[" + getFormattedTime() + "] " + "PROFILE: " + logmessage);
			}
		}

		/**
		 * Sends a message to the core log
		 * 
		 * @param logmessage
		 *            core message
		 */
		public static final void logCore(final String logmessage) {
			if (core) {
				logStdOut("CORE: " + logmessage);
				logService(LoggingType.CORE, logmessage);
			}
		}

		/**
		 * Sends a message to the debug log
		 * 
		 * @param logmessage
		 *            debug message
		 */
		public static final void logDebug(final String logmessage) {

			if (debug) {
				logStdOut("DEBUG: " + logmessage);
				logService(LoggingType.DEBUG, logmessage);
			}
		}

		/**
		 * Sends a message to the info log
		 * 
		 * @param logmessage
		 *            info message
		 */
		public static final void logInfo(final String logmessage) {
			if (info) {
				logStdOut("INFO: " + logmessage);
				logService(LoggingType.INFO, logmessage);
			}
		}

		/**
		 * Sends a message to the severe log
		 * 
		 * @param logmessage
		 *            severe message
		 */
		public static final void logSevere(final String logmessage) {
			logSevere(logmessage, null);
		}

		/**
		 * Sends a message to the severe log with the exception that was thrown
		 * 
		 * @param logmessage
		 *            severe message
		 * @param exception
		 *            causing exception
		 */
		public static final void logSevere(final String logmessage,
				final Throwable exception) {
			logStdErr("SEVERE: " + logmessage);

			if (exception != null) {
				logStdErr(getStackTrace(exception));
				logService(LoggingType.SEVERE, logmessage,
						getStackTrace(exception));
			} else {
				logService(LoggingType.SEVERE, logmessage,
						"No Exception Thrown");
			}

		}

		/**
		 * Sends a message to the severe log with the exception that was thrown
		 * 
		 * @param exception
		 *            causing exception
		 */
		public static final void logSevere(final Throwable exception) {
			logSevere("Empty Exception", exception);
		}

		/**
		 * Sends a message to the warning log
		 * 
		 * @param logmessage
		 *            warning message
		 */
		public static final void logWarning(final String logmessage) {
			logStdErr("WARNING: " + logmessage);
			logService(LoggingType.WARNING, logmessage);
		}

		public static final void logEvent(final String logmessage) {
			logStdErr("EVENT: " + logmessage);
			logService(LoggingType.EVENT, logmessage);
		}

		private static final void logService(final LoggingType type,
				final String message) {
			logService(type, message, null);
		}

		public static final void setLogging(final boolean logging) {
			final LoggingService loggingService = ObjectModel
					.getLoggingService();

			if (loggingService != null) {
				loggingService.setLogging(logging);
			}
		}

		public static final boolean isLogging() {
			final LoggingService loggingService = ObjectModel
					.getLoggingService();
			boolean result = false;

			if (loggingService != null) {
				result = loggingService.isLogging();
			}

			return result;
		}

		private static final void logService(final LoggingType type,
				final String message, final String exception) {

			final LoggingService loggingService = ObjectModel
					.getLoggingService();

			if (loggingService != null) {

				String source = "Unknown";

				if (ObjectModel.getUnit() != null) {
					source = ObjectModel.getUnit().getIpAddress();
				}

				final Message logMessage = LoggingService.createMessage(
						getFormattedTime(), type, source, message, exception);

				loggingService.send(logMessage);
			}
		}

		public final static String getFormattedTime() {
			return dateFormat.format(new Date(System.currentTimeMillis()+getTimeDifference()));
		}

		public final static String getStackTrace(final Throwable e) {
			if (e == null) {
				return "";
			}
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		}

		public static void setStdOut(boolean enable) {
			stdout = enable;
		}

		public static boolean isStdOut() {
			return stdout;
		}
		
		public static long getTimeDifference() {
			return mcdTimeDiff;
		}
		
		public static void setTimeDifference(long timeDifference) {
			mcdTimeDiff = timeDifference;
		}
	}

	/**
	 * The UnitManager has the collection of all Core Components running on the
	 * Unit. The collects are contained in a ComponentContainer. The ObjectModel
	 * contains this container and functions to access the unit components
	 * quickly.
	 * 
	 * @author getownse
	 * @see ComponentContainer
	 * 
	 */
	public static final class ObjectModel {

		static final ComponentContainer components = new ComponentContainer();

		static Unit unit;

		static LoggingService loggingService;

		static StateManager stateManager;
		
		static UpdateHandler updateHandler;

		public final static void add(final Component component) {
			components.add(component);
		}

		public final static void remove(final Component component) {
			components.remove(component);
		}

		/**
		 * Returns the UntiManager's component container
		 * 
		 * @return component container
		 */
		public final static ComponentContainer getComponents() {
			return components;
		}

		public final static LoggingService getLoggingService() {
			return loggingService;
		}

		/**
		 * Get the state manager. A UnitManager can only have one state manager.
		 * 
		 * @return state manager
		 */
		public static final StateManager getStateManager() {
			return stateManager;
		}

		/**
		 * Get the Update Handler.
		 * 
		 * @return Update Handler
		 */
		public static final UpdateHandler getUpdateHandler() {
			return updateHandler;
		}

		/**
		 * Get the unit class. A UnitManager can only have one Unit.
		 * 
		 * @return unit
		 */
		public static final Unit getUnit() {
			return unit;
		}
	}

	/**
	 * Runtime methods for dynamic runtime instantiation of classes.
	 * 
	 * @author getownse
	 * 
	 */
	public static class Runtime {

		private static final ClassLoader classLoader = ClassLoader
				.getSystemClassLoader();

		/**
		 * Return a class descriptor from a class name
		 * 
		 * @param className
		 *            name of the class with fully qualified path
		 * @return class descriptor
		 * @throws ClassNotFoundException
		 */
		public static Class<?> loadClass(final String className)
				throws ClassNotFoundException {
			return classLoader.loadClass(className);
		}

		/**
		 * Creates a new instance of a class from a class name
		 * 
		 * @param className
		 *            name of the class with fully qualified path
		 * @return a new instance of the given class
		 * @throws ClassNotFoundException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 */
		public static Object newInstance(final String className)
				throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {

			final Class<?> clazz = loadClass(className);
			Object insObject = null;

			if (clazz != null) {
				insObject = clazz.newInstance();
			}

			return insObject;
		}
	}

	/**
	 * The Serializer static class contains methods for Serializing and
	 * Deserializing.
	 * 
	 * @author getownse
	 */
	public static final class IO {

		/**
		 * Creates a directory in the path specified if the Directory does not
		 * exist. Returns the directory or null if the directory is impossible
		 * to get.
		 * 
		 * @param directoryName
		 *            name of the directory
		 * @return File descriptor of the directory, null returned on error
		 */
		public static final File mkdirs(final String dir) {

			File d = new File(dir);
			UnitIOArgs args = new UnitIOArgs(d);
			Unit u = ObjectModel.unit;

			if (u == null || u.notifyBeginIOWrite(null, args)) {
				if (!d.exists() && !d.mkdirs()) {
					d = null;
				}
				if (u != null) {
					u.notifyEndIOWrite(null, args);
				}
			} else {
				d = null;
			}

			return d;
		}

		/**
		 * Deletes file by given name.
		 * 
		 * @param name
		 *            Name of the file to delete.
		 * @return true if the delete was successful.
		 */
		public static final boolean rm(final File file) {
			boolean result = false;
			UnitIOArgs args = new UnitIOArgs(file);
			Unit u = ObjectModel.unit;

			if (u == null || u.notifyBeginIOWrite(null, args)) {
				result = file.delete();
				if (u != null) {
					u.notifyEndIOWrite(null, args);
				}
			}

			return result;
		}

		public static final Object deserialize(final File file) {

			Object obj = null;
			FileInputStream fileInputStream = null;
			ObjectInputStream inputStream = null;
			Unit u = ObjectModel.unit;
			UnitIOArgs args = new UnitIOArgs(file);

			if (u == null || u.notifyBeginIORead(null, args)) {

				try {
					fileInputStream = new FileInputStream(file);
					inputStream = new ObjectInputStream(fileInputStream);

					obj = inputStream.readObject();
				} catch (final FileNotFoundException e) {
					Logging.logSevere("FileNotFoundException - UnitManager Could not deserialize '"
							+ file + "'", e);
					obj = null;
				} catch (final IOException e) {
					Logging.logSevere("IOException - UnitManager Could not deserialize '"
							+ file + "'", e);
					obj = null;
				} catch (final ClassNotFoundException e) {
					Logging.logSevere("ClassNotFoundException - UnitManager Could not deserialize '"
							+ file + "'", e);
					obj = null;
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (final IOException e) {
							Logging.logSevere(
									"Could not close ObjectInputStream while deserializing '"
											+ file + "'", e);
						}
					}

					if (fileInputStream != null) {
						try {
							fileInputStream.close();
						} catch (final IOException e) {
							Logging.logSevere(
									"Could not close FileInputStream while deserializing '"
											+ file + "'", e);
						}
					}
				}

				if (u != null) {
					u.notifyEndIORead(null, args);
				}
			}

			return obj;
		}

		public static final boolean serialize(final Object obj, final File file) {

			FileOutputStream fout = null;
			ObjectOutputStream out = null;

			boolean result = false;
			Unit u = ObjectModel.unit;
			UnitIOArgs args = new UnitIOArgs(file);

			if (u == null || u.notifyBeginIOWrite(null, args)) {
				try {
					fout = new FileOutputStream(file);
					out = new ObjectOutputStream(fout);

					out.writeObject(obj);
					result = true;

				} catch (final FileNotFoundException e) {
					Logging.logSevere("Could not serialize '" + file + "'", e);
					result = false;
				} catch (final IOException e) {
					Logging.logSevere("Could not serialize '" + file + "'", e);
					result = false;
				} finally {
					if (out != null) {
						try {
							out.flush();
							out.close();
						} catch (final IOException e) {
							Logging.logSevere(
									"Could not flush ObjectOutputStream while serializing '"
											+ file + "'", e);
						}
					}

					if (fout != null) {
						try {
							fout.flush();
							fout.close();
						} catch (final IOException e) {
							Logging.logSevere(
									"Could not flush FileOutputStream while serializing '"
											+ file + "'", e);
						}
					}
				}
				if (u != null) {
					u.notifyEndIOWrite(null, args);
				}
			}
			
			if (ObjectModel.updateHandler != null) {
				ObjectModel.updateHandler.flushFiles();
			}

			return result;
		}
	}

	/**
	 * Common methods to creating and managing threads. All threads created by
	 * the core framework and derived classes should use these methods.
	 * 
	 * @author getownse
	 * 
	 */
	public static final class Threading {

		private static final ThreadGroup threadGroup = new ThreadGroup(
				"ThreadManager Thread Group");

		/**
		 * Create a new thread.
		 * 
		 * @param source
		 *            Object creating the thread
		 * @param runnable
		 *            Location of the threads entry point
		 * @param name
		 *            name of the thread
		 * @return a new thread
		 * @throws CoreThreadException
		 */
		public static final Thread createThread(final Object source,
				final Runnable runnable, final String name)
				throws CoreThreadException {
			
			final String sourceName;
			final Thread thread;
			
			if (source == null) {
				sourceName = "unknown";

				thread = new Thread(threadGroup, runnable, "Thread '"
						+ name + "' created by " + sourceName);
				
				UnitManager.Logging.logStdOut("Source '"
					+ "' is NULL.");
				
			} else {
				sourceName = source.toString();

				thread = new Thread(threadGroup, runnable, "Thread '"
						+ name + "' created by " + sourceName);
				
				if (source.equals(runnable)) {
					UnitManager.Logging.logStdOut("Source '"
						+ source
						+ "' should not implement Runnable.  Please use an anonymous class.");
				}
			}
			return thread;
		}

		/**
		 * The thread group that all core framework threads are created in
		 * 
		 * @return core framework thread group
		 */
		public static final ThreadGroup getThreadGroup() {
			return threadGroup;
		}
	}

	/**
	 * Methods for gathering time information such as up time.
	 * 
	 * @author getownse
	 * 
	 */
	public static final class Timing {
		
		private static final long NANO2MILLI_DIVISOR = 1000000;
		private static final long START_TIME = System.nanoTime();

		/**
		 * Uptime in mS since virtual machine started. 
		 * System.nanoTime() is monotonic (non adjustable, incremental clock). Java Sun states that the value
		 * returned represents nanoseconds since some fixed but arbitrary time (perhaps in the future, so values
		 * may be negative).
		 * 
		 * @author kknight
		 * 
		 */
		public static final long getTimeAlive() {
				return (System.nanoTime() - START_TIME)/NANO2MILLI_DIVISOR; 
			}
	}

	/**
	 * The UnitManager thread entry point and command line parameter handling.
	 * 
	 * @author getownse
	 * 
	 */

	public static final void main(final String[] args) throws Exception {

		String sLoader = System.getProperty("UnitManagerLoader");
		UnitManagerLoader loader = null;
		
		/**
		 * Running from the PERC shell causes an instantiation of the 
		 * security manager. The security manager runs the java.policy
		 * file found in $PERC_HOME/lib/security or if that is not found 
		 * reverts to hard-coded permissions. 
		 * Typically socket and file permissions are set to
		 * java.security.AllPermissions.
		 * 
		 * The security manager causes upto ten times the 
		 * amount of garbage collection because it buffers incoming 
		 * Ethernet packets to allow sufficient time to examine them.
	     * 
		 */
		System.setSecurityManager(null);

		if (sLoader != null && sLoader.length() > 0) {
			loader = (UnitManagerLoader) Runtime.newInstance(sLoader);
		}

		if (loader == null) {
			loader = new LcpLoader();
		}

		execute(loader, parseArguments(args));
		// cleanUpObsoleteFiles();
	}

	public static final Map<String, String> parseArguments(final String[] args) {
		HashMap<String, String> p = new HashMap<String, String>();

		String name = "";
		String value = "";
		int index;

		for (final String arg : args) {
			if ((index = arg.indexOf("=")) > -1 && index < arg.length()) {
				name = arg.substring(0, index);
				value = arg.substring(index + 1);
			} else {
				name = arg;
				value = "";
			}
			p.put(name, value);
		}

		return p;
	}

	public static final void execute(final UnitManagerLoader loader,
			final Map<String, String> properties) throws CoreException {
		int i;
		final String PATH_KEY = "custom";
		final String SYSTEM_KEY = "system";
		String PATH_MARK = "\\";
		String fullNamePath;
		loader.initialize(properties);
		fullNamePath = loader.getLoadedLCPName();
		if(fullNamePath!=null){
			i = fullNamePath.lastIndexOf(PATH_MARK);
			if(i<0){
				PATH_MARK = "/";
				i = fullNamePath.lastIndexOf(PATH_MARK);
			}
			i += 1;
			LRULCPFilePath = fullNamePath.substring(0, i);
			i = fullNamePath.lastIndexOf(PATH_KEY+"\\");
			if(i<0)
				i = fullNamePath.lastIndexOf(PATH_KEY+"/");
			i +=PATH_KEY.length()+1;
			SYSLCPFilePath = fullNamePath.substring(0, i)+ SYSTEM_KEY + PATH_MARK;
		}
		loader.load();
		loader.setup();
		loader.verify();
		loader.complete();
	}

	public static final void execute(final UnitManagerLoader loader)
			throws CoreException {
		execute(loader, new HashMap<String, String>());
	}
	public static final String getLRULCPFilePath(){
		return LRULCPFilePath;
	}
	public static final String getSYSLCPFilePath(){
		return SYSLCPFilePath;
	}

	/*
	private static void cleanUpObsoleteFiles() {
		String fileExtension = DEF_LOG_FILE_EXT;
		if(UnitManager.ObjectModel.getComponents().get("LoggingService")!=null) {
			if(Logging.isDebug()) {
				Logging.logDebug("UnitManager->CleanUpObsoleteFiles(): LoggingService Component found...");
			}
			fileExtension = UnitManager.ObjectModel.getComponents().get("LoggingService").getSetting(LoggingService.SETTING_LOG_FILE_EXTENSION, fileExtension);
		} else {
			Logging.logWarning("UnitManager->CleanUpObsoleteFiles(): LoggingService Component not found...");
		}
		File varlog = UnitManager.ObjectModel.getUnit().getLogDirectory();
		if(Logging.isDebug()) {
			Logging.logDebug("UnitManager->cleanUpObsoleteFiles().log Directory: '"+varlog+"'");
		}
		String[] children = varlog.list();
		for (int i=0; i<children.length; i++) {
			Logging.logDebug("UnitManager->cleanUpObsoleteFiles(): file: '"+children[i]+"'");
           	if( (children[i].contains(fileExtension)) && (!children[i].contains(UnitManager.ObjectModel.getUnit().getInstanceName())) ) {
           		Logging.logWarning("UnitManager->cleanUpObsoleteFiles(): Obsolete log file found.....Removing.."+children[i]);
           		new File(varlog,children[i]).delete();
           	}
        }
	}
	*/
}
