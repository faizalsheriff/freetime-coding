package com.rockwellcollins.cs.hcms.core;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.rockwellcollins.cs.hcms.core.Consts.SystemProperties;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.ServiceRunArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceRunException;
import com.rockwellcollins.cs.hcms.core.services.ServiceState;

/**
 * The Unit's responsibility is to provide the framework with center hub of
 * configuration. To ensure that all components are using identical
 * configurations and synchronized methods, such as IP address and Random
 * numbers.
 * 
 * @author getownse
 * 
 */
public class Unit extends Service {

	private static final int MINWAIT = 10;

	private static final long serialVersionUID = 1L;
	
	public static final String SETTING_TRISTATE_ENABLED = "isTriStateEnabled";

	public static final String SETTING_UNIT_TEMP_DIRECTORY = "unit temp directory";
	public static final String SETTING_UNIT_INSTANCE_NAME = "unit instance name";
	public static final String SETTING_UNIT_STORE_DIRECTORY = "unit store directory";
	public static final String SETTING_UNIT_LOG_DIRECTORY = "unit log directory";
	public static final String SETTING_UNIT_CACHE_DIRECTORY = "unit cache directory";
	public static final String SETTING_UNIT_BULK_DIRECTORY = "unit bulk directory";
	public static final String SETTING_UNIT_WORKING_DIRECTORY = "unit working directory";
	public static final String SETTING_UNIT_SYSTEM_DATA_DIRECTORY = "unit system data directory";
	public static final String SETTING_UNIT_DATA_DIRECTORY = "unit data directory";
	public static final String SETTING_UNIT_IP = "unit ip";
	public static final String SETTING_UNIT_IP_FILTER = "unit ip filter";
	public static final String SETTING_UNIT_IP_PATTERN = "unit ip pattern";
	public static final String SETTING_UNIT_VLAN_IP_PATTERN = "unit vlan ip pattern";

	/** Venue IP Address range pattern */
	private String ipPattern = "^(1[0-9]?)\\.(2[0-4]?[0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	private String[] ipFilters = new String[] { "127.0.0.1", "127.0.1.1" };
	private long randomSeed = System.currentTimeMillis();
	private String instanceName = "";

	private File bulkDirectory;
	private File cacheDirectory;
	private File logDirectory;
	private File storeDirectory;
	private File tempDirectory;
	private File workingDirectory;
	private File systemDataDirectory;
	private File unitDataDirectory;

	private transient Pattern ipRegPattern;
	private InetAddress ipAddress;
	private transient ArrayList<UnitEvent> events;
	private transient ArrayList<InetAddress> ipAddresses;
	private transient long eventWaitTime = 0L;
	private transient String macAddress;

	private transient boolean isTriStateEnabled;


	public boolean isTriStateEnabled() {
		return isTriStateEnabled;
	}

	public void setTriStateEnabled(boolean isTriStateEnabled) {
		this.isTriStateEnabled = isTriStateEnabled;
	}

	public final long getRandomSeed() {
		return randomSeed;
	}

	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (final UnknownHostException e) {
			ipAddress = null;
		}
		macAddress = SystemProperties.MAC_ADDRESS;
		if (macAddress == null || macAddress.length() <= 0 && ipAddress != null) {
			macAddress = ipAddress.getHostAddress();
		}

		ipAddresses = UnitManager.Network.getFaceIpAddresses();
		events = new ArrayList<UnitEvent>();

		setSetupPriority(1);
	}

	public final String[] getIpFilters() {
		return ipFilters;
	}

	private final Pattern getIpRegPattern() {
		return ipRegPattern;
	}

	public final String getMacAddress() {
		return macAddress;
	}

	private final InetAddress queryIp() {

		for (final InetAddress ip : ipAddresses) {
			final String addr = ip.getHostAddress();
			if (getIpRegPattern().matcher(addr).matches()) {
				boolean accept = true;
				for (final String filter : getIpFilters()) {
					if (filter.equals(addr)) {
						accept = false;
						break;
					}
				}
				if (accept) {
					return ip;
				}
			}
		}

		return ipAddress;
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		instanceName = getSetting(SETTING_UNIT_INSTANCE_NAME, instanceName);

		if (instanceName == null || instanceName.length() == 0) {
			instanceName = getName();
		}

		// Removed the old UNIT_IP_PATTERN and added UNIT_VLAN_IP_PATTERN to
		// support different IP address schemes on different system
		ipPattern = getSetting(SETTING_UNIT_VLAN_IP_PATTERN, ipPattern);

		ipRegPattern = Pattern.compile(ipPattern);

		ipFilters = getSettingList(SETTING_UNIT_IP_FILTER, ipFilters);
		String sip = getSetting(SETTING_UNIT_IP, "");

		if (sip.equals("query ip") || sip.length() == 0) {

			ipAddress = queryIp();

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Unit '" + getName()
						+ "' IP Address is being Auto Configured to '"
						+ ipAddress + "'");
			}

		} else {

			try {
				ipAddress = InetAddress.getByName(sip);
			} catch (final UnknownHostException e) {
				ipAddress = queryIp();
				UnitManager.Logging.logWarning("Unit '" + getName()
						+ "' could not detect IP Address '" + sip
						+ "'.  Auto Detected '" + ipAddress + "'");
			}
		}

		bulkDirectory = UnitManager.IO.mkdirs(getSetting(
				SETTING_UNIT_BULK_DIRECTORY, "/bulk"));

		if (bulkDirectory == null) {
			bulkDirectory = new File("./"); // current directory
		}

		cacheDirectory = UnitManager.IO.mkdirs(getSetting(
				SETTING_UNIT_CACHE_DIRECTORY, "/bulk"));

		if (cacheDirectory == null) {
			cacheDirectory = new File("./");
		}

		logDirectory = UnitManager.IO.mkdirs(getSetting(
				SETTING_UNIT_LOG_DIRECTORY, "/var/log"));
		
		setTriStateEnabled(getSetting(SETTING_TRISTATE_ENABLED, false));
		if (logDirectory == null) {
			logDirectory = new File("./");
		}

		storeDirectory = UnitManager.IO.mkdirs(getSetting(
				SETTING_UNIT_STORE_DIRECTORY, "/bulk"));

		if (storeDirectory == null) {
			storeDirectory = new File("./");
		}

		tempDirectory = UnitManager.IO.mkdirs(getSetting(
				SETTING_UNIT_TEMP_DIRECTORY, "/tmp"));

		if (tempDirectory == null) {
			tempDirectory = new File("./");
		}

		workingDirectory = UnitManager.IO.mkdirs(getSetting(
				SETTING_UNIT_WORKING_DIRECTORY, "./"));

		if (workingDirectory == null) {
			workingDirectory = new File("./");
		}

		systemDataDirectory = UnitManager.IO
				.mkdirs(getSetting(SETTING_UNIT_SYSTEM_DATA_DIRECTORY,
						"/conf/custom/system/data"));

		if (systemDataDirectory == null) {
			systemDataDirectory = new File("./");
		}

		unitDataDirectory = UnitManager.IO.mkdirs(getSetting(
				SETTING_UNIT_DATA_DIRECTORY, "/conf/custom/unit/data"));

		if (unitDataDirectory == null) {
			unitDataDirectory = new File("./");
		}

		randomSeed = ipAddress.hashCode();
	}

	public final void setInetAddress(final InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public final InetAddress getInetAddress() {
		return ipAddress;
	}

	public final File getBulkDirectory() {
		return bulkDirectory;
	}

	/**
	 * Returns the 4th octet of the IPV4 Ip Address
	 * 
	 * @return 4th octet of the IPV4 Ip Address or -1 if there is an error
	 */
	public final int getUnitNumber() {
		if (ipAddress == null) {
			return -1;
		}
		return 0xFF & ipAddress.getAddress()[3];
	}

	/**
	 * Returns the 3rd octet of the IPV4 Ip Address
	 * 
	 * @return 3rd octet of the IPV4 Ip Address or -1 if there is an error
	 */
	public final int getUnitGroup() {
		if (ipAddress == null) {
			return -1;
		}
		return 0xFF & ipAddress.getAddress()[2];
	}

	/**
	 * Sets the bulk directory. The bulk directory is a public large capacity
	 * space for software uploads and file transfers.
	 * 
	 * @param bulkDirectory
	 *            the bulk directory
	 */
	public final void setBulkDirectory(final File bulkDirectory) {
		this.bulkDirectory = bulkDirectory;
	}

	public final File getCacheDirectory() {
		return cacheDirectory;
	}

	/**
	 * Sets the cache directory. The cache directory is not persistent through
	 * reboot and used as temporary storage.
	 * 
	 * @param cacheDirectory
	 *            the cache directory location
	 */
	public final void setCacheDirectory(final File cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}

	public final File getLogDirectory() {
		return logDirectory;
	}

	/**
	 * The log directory is used for system and local persistent log file
	 * storage.
	 * 
	 * @param logDirectory
	 *            log directory location
	 */
	public final void setLogDirectory(final File logDirectory) {
		this.logDirectory = logDirectory;
	}

	public final File getStoreDirectory() {
		return storeDirectory;
	}

	/**
	 * The store directory is used for persistent object serialization for load
	 * performance.
	 * 
	 * @param storeDirectory
	 *            store directory location
	 */
	public final void setStoreDirectory(final File storeDirectory) {
		this.storeDirectory = storeDirectory;
	}

	public final File getTempDirectory() {
		return tempDirectory;
	}

	/**
	 * The temp directory is used for
	 * 
	 * @param tempDirectory
	 */
	public final void setTempDirectory(final File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public final boolean notifyBeginIORead(final Object source,
			final UnitIOArgs args) {
		onBeginIORead(source, args);
		return !args.isCancel();
	}

	public final boolean notifyBeginIOWrite(final Object source,
			final UnitIOArgs args) {
		onBeginIOWrite(source, args);
		return !args.isCancel();
	}

	public final boolean notifyEndIORead(final Object source,
			final UnitIOArgs args) {
		onEndIORead(source, args);
		return !args.isCancel();
	}

	public final boolean notifyEndIOWrite(final Object source,
			final UnitIOArgs args) {
		onEndIOWrite(source, args);
		return !args.isCancel();
	}

	public final void removeEvent(final UnitEvent event) {
		synchronized (events) {
			events.remove(event);
		}
	}

	public final void removeEvent(final UnitEventCallback callback) {
		synchronized (events) {
			final Iterator<UnitEvent> itr = events.iterator();
			while (itr.hasNext()) {
				final UnitEvent event = itr.next();
				if (event.getCallback() == null
						|| event.getCallback().equals(callback)) {
					itr.remove();
				}
			}
		}
	}

	public final void addEvent(final long timeMs,
			final UnitEventCallback callback, final boolean repeat) {
		addEvent(timeMs, callback, repeat, null);
	}

	public final void addEvent(final long timeMs,
			final UnitEventCallback callback, final boolean repeat,
			final Object tag) {
		UnitEvent event = new UnitEvent();
		CountdownTimer timer = new CountdownTimer();
		event.setCallback(callback);
		event.setRepeat(repeat);
		event.setTimer(timer);
		event.setTag(tag);
		timer.startTimer(timeMs);
		addEvent(event);
	}

	@Override
	protected void onRun(Object source, ServiceRunArgs args)
			throws ServiceRunException {

		while (getServiceState() == ServiceState.RUNNING) {
			synchronized (events) {
				if (events.size() == 0) {
					try {
						events.wait();
					} catch (final InterruptedException e) {
					}
				} else {
					try {
						if (eventWaitTime > 0) {
							events.wait((eventWaitTime < MINWAIT) ? MINWAIT
									: eventWaitTime);
						}
					} catch (final InterruptedException e) {
					}
				}
				processEvents();
			}
		}
	}

	public final void addEvent(final UnitEvent event) {
		synchronized (events) {
			if (event.getCallback() != null) {
				events.add(event);
			}
			events.notify();
		}
	}

	private final void processEvents() {
		synchronized (events) {

			eventWaitTime = 0;

			final Iterator<UnitEvent> itr = events.iterator();

			while (itr.hasNext()) {

				final UnitEvent event = itr.next();

				CountdownTimer timer = event.getTimer();

				if (timer.hasExpired()) {

					try {
						event.getCallback().eventExpired(this, event);
					} catch (final Exception e) {
						UnitManager.Logging.logSevere("Unit '" + getName()
								+ "' failed to call an Event", e);
					}

					if (event.isRepeat()) {
						timer.restart();
					} else {
						itr.remove();
					}

					long remaining = timer.getRemainingTime();

					if (eventWaitTime == 0L) {
						eventWaitTime = remaining;
					} else if (eventWaitTime > remaining) {
						eventWaitTime = remaining;
					}
				}
			}
		}
	}

	public final String getIpAddress() {
		if (ipAddress == null) {
			return "Unknown Ip";
		}
		return getInetAddress().getHostAddress();
	}

	protected void onBeginIORead(Object source, UnitIOArgs args) {
	}

	protected void onEndIORead(final Object source, final UnitIOArgs args) {
	}

	protected void onBeginIOWrite(final Object source, final UnitIOArgs args) {
	}

	protected void onEndIOWrite(final Object source, final UnitIOArgs args) {
	}

	public final void setWorkingDirectory(final File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public final File getWorkingDirectory() {
		return workingDirectory;
	}

	public final void setSystemDataDirectory(final File systemDataDirectory) {
		this.systemDataDirectory = systemDataDirectory;
	}

	public final File getSystemDataDirectory() {
		return systemDataDirectory;
	}

	public final void setUnitDataDirectory(final File unitDataDirectory) {
		this.unitDataDirectory = unitDataDirectory;
	}

	public final File getUnitDataDirectory() {
		return unitDataDirectory;
	}

	public final void setInstanceName(final String instanceName) {
		this.instanceName = instanceName;
	}

	public final String getInstanceName() {
		return instanceName;
	}
}