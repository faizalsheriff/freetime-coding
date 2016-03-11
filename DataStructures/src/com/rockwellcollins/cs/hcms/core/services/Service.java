package com.rockwellcollins.cs.hcms.core.services;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rockwellcollins.cs.hcms.core.Component;
import com.rockwellcollins.cs.hcms.core.ComponentImportXmlElementArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.ComponentXmlParserException;
import com.rockwellcollins.cs.hcms.core.CoreThreadException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.UnitManager.ObjectModel;
import com.rockwellcollins.cs.hcms.core.services.logging.LoggingService;
import com.rockwellcollins.cs.hcms.core.services.logging.LoggingType;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;

public class Service extends Component {

	private static final long serialVersionUID = 1L;

	public static final String SETTING_SERVICE_THREAD_PRIORITY = "service thread priority";

	private static final String THREAD_NAME = "Service Thread";
	private static final int THREAD_STOP_WAIT = 3000;

	private static final String ATTRIBUTE_AUTOSTART = "autostart";

	private ServiceStartupType startupType = ServiceStartupType.FALSE;
	private int threadPriority = 5;

	private transient Thread serviceThread;
	private transient ServiceState serviceState;
	private transient Object lock;
	private transient ArrayList<ServiceListener> listeners;
	private transient Object startMutex;
	private transient boolean didStarted;
	private transient boolean didStopped;

	private int verbosity = 1;
	
	public void setVerbosity(int level) {
		if (level > 0) {
			verbosity = level;
			UnitManager.Logging.logWarning("Verbosity level set to: " 
					+ Integer.valueOf(verbosity).toString() + " for: " 
					+ this.getName());
		}
	}
	
	public int getVerbosity() {
		return verbosity;
	}
	
	private boolean isStdOut = false;
	
	public void setStdOut(boolean flag) {
		UnitManager.Logging.logWarning("StdOut turned: " 
				+ Boolean.valueOf(flag).toString() + " for: " 
				+ this.getName());
		isStdOut = flag;
	}
	
	protected boolean isStdOut() {
		return (isStdOut || UnitManager.Logging.isStdOut());
	}

	private boolean isInfo = false;
	
	public void setInfo(boolean flag) {
		UnitManager.Logging.logWarning("Info turned: " 
				+ Boolean.valueOf(flag).toString() + " for: " 
				+ this.getName());
		isInfo = flag;
	}
	
	protected boolean isInfo() {
		return (isInfo || UnitManager.Logging.isInfo());
	}

	protected boolean isInfo(int level) {
		return ((isInfo || UnitManager.Logging.isInfo()) 
				&& (verbosity >= level));
	}
	
	protected void logInfo(int level, final String message) {
		if (verbosity >= level) {
			logInfo(message);
		}
	}
	
	protected void logInfo(final String message) {

		final LoggingService loggingService = ObjectModel
				.getLoggingService();

		if (loggingService != null && isInfo() == true) {

			String source = "Unknown";

			if (ObjectModel.getUnit() != null) {
				source = ObjectModel.getUnit().getIpAddress();
			}

			final Message logMessage = LoggingService.createMessage(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()), 
					LoggingType.INFO, source, message, null);

			loggingService.send(logMessage);
			
			if (UnitManager.Logging.isStdOut() || isStdOut) {
				UnitManager.Logging.logStdOut(message);
			}
		}
	}

	private boolean isDebug = false;
	
	public void setDebug(boolean flag) {
		UnitManager.Logging.logWarning("Debug turned: " 
				+ Boolean.valueOf(flag).toString() + " for: " 
				+ this.getName());
		isDebug = flag;
	}
	
	protected boolean isDebug() {
		return (isDebug || UnitManager.Logging.isDebug());
	}

	protected boolean isDebug(int level) {
		return ((isDebug || UnitManager.Logging.isDebug()) 
				&& (verbosity >= level));
	}
	
	protected void logDebug(int level, final String message) {
		if (verbosity >= level) {
			logDebug(message);
		}
	}	

	protected void logDebug(final String message) {
		
		final LoggingService loggingService = ObjectModel
				.getLoggingService();

		if (loggingService != null & isDebug() == true) {

			String source = "Unknown";

			if (ObjectModel.getUnit() != null) {
				source = ObjectModel.getUnit().getIpAddress();
			}

			final Message logMessage = LoggingService.createMessage(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()), 
					LoggingType.DEBUG, source, message, null);

			loggingService.send(logMessage);
			
			if (UnitManager.Logging.isStdOut() || isStdOut) {
				UnitManager.Logging.logStdOut(message);
			}
		}
	}	


	public final boolean register(final ServiceListener listener) {
		synchronized (listeners) {
			return listeners.add(listener);
		}
	}

	public final boolean unregister(final ServiceListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	public final ServiceState getServiceState() {
		return serviceState;
	}

	public final Thread getServiceThread() {
		return serviceThread;
	}

	public final int getThreadPriority() {
		return threadPriority;
	}

	public final ServiceStartupType getStartupType() {
		return startupType;
	}

	public final void setStartupType(final ServiceStartupType startupType) {
		this.startupType = startupType;
	}

	private final void run() {

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Service '" + toString() + "' "
					+ THREAD_NAME + " Starting.");
		}

		try {

			didStarted = false;

			ServiceStartArgs sargs = new ServiceStartArgs();

			onStarted(this, sargs);

			if (!didStarted) {
				UnitManager.Logging
						.logWarning("Service '"
								+ getName()
								+ "' class '"
								+ getClass()
								+ "' did not call onStarted.super.  Logic may not execute as expected.");
			}

			this.serviceState = ServiceState.RUNNING;

			synchronized (startMutex) {
				startMutex.notifyAll();
			}

			ServiceRunArgs rargs = new ServiceRunArgs();
			onRun(this, rargs);

			if (this.serviceState == ServiceState.RUNNING) {
				UnitManager.Logging.logWarning("Service '" + getName()
						+ "' has Stopped before being instructed.");
			}

			if (this.serviceState == ServiceState.ERROR) {
				if (UnitManager.Logging.isInfo()) {
					UnitManager.Logging.logInfo("Service '" + getName()
							+ "' has been flagged with an Error");
				}
			}
		} catch (final ServiceRunException e) {

			UnitManager.Logging
					.logSevere("Service '"
							+ getName()
							+ "' "
							+ THREAD_NAME
							+ " exited due to a ServiceRunException in the onRun method.");

			serviceState = ServiceState.ERROR;

		} catch (final ServiceStartException e) {

			UnitManager.Logging
					.logSevere(
							"Service '"
									+ getName()
									+ "' "
									+ THREAD_NAME
									+ " exited due to a ServiceStartException in the onRunMethod",
							e);

			serviceState = ServiceState.ERROR;

		} finally {

			serviceState = ServiceState.STOPPED;

			final ServiceStopArgs stargs = new ServiceStopArgs();

			try {

				didStopped = false;

				onStopped(this, stargs);

			} catch (final ServiceStopException e) {

				UnitManager.Logging.logSevere("Service " + getName()
						+ " could not properly execute onStop method.", e);

				this.serviceState = ServiceState.ERROR;

			} finally {

				if (!didStopped) {
					UnitManager.Logging
							.logWarning("Service '"
									+ getName()
									+ "' class '"
									+ getClass()
									+ "' did not call onStopped.super.  Logic may not function as expected.");
				}
			}
		}
	}

	public final void setThreadPriority(final int threadPriority) {
		synchronized (lock) {
			if (serviceThread != null) {
				serviceThread.setPriority(threadPriority);
			}
			this.threadPriority = threadPriority;
		}
	}

	public final boolean startAndWait(final long timeout)
			throws ServiceStartException {
		synchronized (lock) {
			synchronized (startMutex) {
				if (serviceState == ServiceState.STOPPED) {
					try {
						start();
						startMutex.wait(timeout);
					} catch (final InterruptedException e) {
						throw new ServiceStartException("Service '" + getName()
								+ "' interruped while startAndWait.", e);
					}
				}
			}
		}
		return ServiceState.RUNNING == serviceState;
	}

	public final void start() throws ServiceStartException {
		synchronized (lock) {
			switch (serviceState) {
			case STOPPED:
				gracefulStart();
				break;
			case ERROR:
				try {
					interruptStop();
					gracefulStart();
				} catch (ServiceStopException e) {
					UnitManager.Logging.logSevere("Service '" + getName()
							+ "' could not be started from ERROR State.", e);

				}
				break;
			default:
				throw new ServiceStartException("Service '" + getName()
						+ "' could not Start because it is in the state '"
						+ serviceState + "'");

			}
		}
	}

	private final void gracefulStart() throws ServiceStartException {
		try {
			if (serviceThread != null && serviceThread.isAlive()) {
				throw new ServiceStartException(
						"Service '"
								+ getName()
								+ "' could not Start because the working thread is currently running.");
			}

			serviceThread = UnitManager.Threading.createThread(this,
					new Runnable() {
						public void run() {
							Service.this.run();
						}
					}, "Service '" + getName() + "' " + THREAD_NAME);
			serviceThread.setPriority(getThreadPriority());

		} catch (final CoreThreadException e) {
			UnitManager.Logging.logSevere("Service '" + getName()
					+ "' could not start Service " + THREAD_NAME, e);

			serviceState = ServiceState.STARTING;
		}

		ServiceStartArgs args = new ServiceStartArgs();
		onStarting(this, args);

		serviceThread.start();

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Service '" + getName() + "' started.");
		}
	}

	public final void stop() throws ServiceStopException {

		synchronized (lock) {
			switch (getServiceState()) {
			case STOPPED:
			case STOPPING:
				UnitManager.Logging.logWarning("Service '" + toString()
						+ "' was already stopped.");
				break;
			default:
				this.serviceState = ServiceState.STOPPING;
				gracefulStop();
				interruptStop();
				break;
			}
		}
	}

	private final void interruptStop() throws ServiceStopException {
		if (serviceThread != null && serviceThread.isAlive()) {
			try {

				if (UnitManager.Logging.isCore()) {
					UnitManager.Logging.logStdOut("Service '" + getName()
							+ "' is being interrupted to stop.");
				}

				serviceThread.interrupt();
				serviceThread.join(THREAD_STOP_WAIT);

				if (this.getServiceState() != ServiceState.STOPPED) {
					throw new ServiceStopException("Service '" + getName()
							+ "' could not " + THREAD_NAME
							+ ".  Thread did not complete properly.");
				}

			} catch (final InterruptedException e) {
				serviceState = ServiceState.ERROR;
				throw new ServiceStopException(
						"Service '"
								+ getName()
								+ "' could not Stop "
								+ THREAD_NAME
								+ " with Interrupt Follwed by Join.  Thread is not responding",
						e);
			}
		}
	}

	private final void gracefulStop() {
		if (serviceThread != null && serviceThread.isAlive()) {
			try {

				ServiceStopArgs args = new ServiceStopArgs();
				onStopping(this, args);

				serviceThread.join(THREAD_STOP_WAIT);

				if (this.getServiceState() != ServiceState.STOPPED) {
					throw new ServiceStopException("Could not Stop Service '"
							+ toString() + "' " + THREAD_NAME
							+ ".  Thread did not complete properly.");
				}

			} catch (final ServiceStopException e) {

				if (UnitManager.Logging.isInfo()) {
					UnitManager.Logging.logInfo("Could not Stop Service '"
							+ toString() + " " + THREAD_NAME
							+ "' with Join, Trying to Interrupt");
				}

			} catch (final InterruptedException e) {

				if (UnitManager.Logging.isInfo()) {
					UnitManager.Logging.logInfo("Could not Stop Service '"
							+ toString() + "' " + THREAD_NAME
							+ " with Join, Trying to Interrupt");
				}

			}
		}
	}

	@Override
	protected void onSetup(final Object source, final ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		threadPriority = getSetting(SETTING_SERVICE_THREAD_PRIORITY,
				threadPriority);
	}

	@Override
	protected void onImportXmlElement(final Object source,
			final ComponentImportXmlElementArgs args)
			throws ComponentXmlParserException {

		super.onImportXmlElement(source, args);

		try {
			final String aAutostart = args.getElement().getAttribute(
					ATTRIBUTE_AUTOSTART);

			if (aAutostart != null && aAutostart.length() > 0) {
				setStartupType(ServiceStartupType.valueOf(aAutostart
						.toUpperCase()));
			}
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Service '" + getName()
					+ "' invalid autostart attribute.  Setting to false.", e);
			setStartupType(ServiceStartupType.FALSE);
		}
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		listeners = new ArrayList<ServiceListener>();
		serviceState = ServiceState.STOPPED;
		lock = new Object();
		startMutex = new Object();
	}

	protected void onRun(final Object source, final ServiceRunArgs args)
			throws ServiceRunException {

	}

	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		didStarted = true;

		try {
			synchronized (listeners) {
				int len = listeners.size();
				for (int i = 0; i < len; i++) {
					listeners.get(i).serviceStarted(this, args);
				}
			}
		} catch (final Exception e) {
			throw new ServiceStartException("Service '" + getName()
					+ "' exception in onStarted listeners", e);
		}

	}

	protected void onStopping(final Object source, final ServiceStopArgs args)
			throws ServiceStopException {

	}

	protected void onStarting(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

	}

	protected void onStopped(final Object source, final ServiceStopArgs args)
			throws ServiceStopException {

		didStopped = true;

		try {
			synchronized (listeners) {
				int len = listeners.size();
				for (int i = 0; i < len; i++) {
					listeners.get(i).serviceStopped(this, args);
				}
			}
		} catch (final Exception e) {
			throw new ServiceStopException("Service '" + getName()
					+ "' exception in onStopped listeners", e);
		}
	}

	protected final void setServiceState(final ServiceState state) {
		synchronized (lock) {
			this.serviceState = state;
		}
	}
}
