package com.rockwellcollins.cs.hcms.core.services.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.CoreThreadException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.QueueListener;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessorException;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.MessagingService;
import com.rockwellcollins.cs.hcms.core.services.MessagingServiceReceiveMessageArgs;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceSocketException;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.update.messages.CAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.EAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.LAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.MAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.NRMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.UpdateServiceMessage;

/**
 * The Class UpdateService acts the starting point for any kind of software installation on the LRU.
 * It is responsible for LRU's LSP and LCP installation. The loading process can be started by
 * sending a Load Announcement to the Update Service. It can also do a Self Load if it loses the
 * voting among all the LRUs for the prevailing software or LCP.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see CAMessage
 * @see LAMessage
 * @see MAMessage
 * @see SAMessage
 * @see ElectionProcessor
 * @see LAProcessor
 * @see MAProcessor
 * @see SAProcessor
 * @see CIIParser
 * @see ConformityChecker
 * @see ConfigurationAnnouncer
 * @see Installer
 * @see UpdateServiceQueueProcessor
 * @see StatusQueueProcessor
 * @see UpdateHandler
 * @see UpdateServiceException
 * 
 */
public class UpdateService extends MessagingService implements QueueListener<CAMessage> {

	private static final long serialVersionUID = 1L;

	private UpdateHandler updateHandler;

	private FTPAdapter ftpAdapter;

	private StateManager stateManager;

	private transient HardwareInfo hardwareDetails;

	private transient LogParser logParser;

	private HashMap<String, ParentCI> parentCIs;

	private transient UpdateServiceQueueProcessor queueProcessor;

	private int queueProcessorSize;

	private transient Installer installer;

	private transient Thread installerThread;

	private boolean isInitialized;

	private boolean isReady;

	private boolean isInstalling;

	private boolean selfLoadFlag;

	private transient CIIParser ciiParser;

	private transient CAMessage ca;

	private transient ConfigurationAnnouncer configurationAnnouncer;

	private transient Thread configurationAnnouncerThread;

	private transient StatusAnnouncer statusAnnouncer;

	private transient Thread statusAnnouncerThread;

	private transient LAProcessor laProcessor;

	private transient MAProcessor maProcessor;

	private transient SAProcessor saProcessor;

	private transient StatusQueueProcessor statusQueueProcessor;

	private int statusQueueProcessorSize;

	private transient ConformityChecker conformityChecker;

	private String lruInstance;

	private int caMessageInterval;

	private int queueTimeout;

	private String pathToInstallLog;

	private String pathToCii;

	private String pathToLoadable;

	private String pathToLa;

	private List<String> primaryPackagesList;

	private HashMap<String, String> lcpToLRUTypes;

	private HashMap<String, String> lruToLCPTypes;

	private List<String> secondaryLruList;

	private List<String> backupLruList;

	private List<String> knownLCPTypes;

	private transient ElectionProcessor electionProcessor;

	private HashMap<String, HashMap<String, List<String>>> votes;

	private HashMap<String, CAMessage> voters;

	private HashMap<String, List<String>> hwPartNumbers;

	private HashMap<String, ParentCI> toBeInstalled;
	
	private boolean isToBeInstalledGood;

	private transient CountdownTimer votingTimer;

	private int initialVotingTime;

	private int votingTime;

	private int statusTimeout;

	private int statusQueueTimeout;

	private String localFtpPathForCii;

	private String localFtpPathForLoadable;

	private int ftpMessageInterval;

	private long laWaitTime;

	private int finalStatusDuration;

	private int finalStatusFrequency;

	private int saMessageInterval;

	private int saSendLimit;

	private ArrayList<String> wrongCA;

	private ArrayList<String> loadedSecondaryLrus;

	private Object waitForReboot;

	private boolean isRebootReceived;

	private int rebootCommandFrequency;

	private int rebootCommandSendLimit;

	private int rebootCommandTimeout;

	private int sleepBeforeReboot;

	private static final int MIN_PACKET_SIZE = 65536;

	/** The Constant MCD. */
	public static final String MCD = "mcd";

	/** The Constant LCP_TYPE. */
	public static final String LCP_TYPE = "lcp";

	/** The Constant NO_LCP. */
	public static final String NO_LCP = "nolcp";

	/** The Constant INSTALL_LOG_FILE. */
	public static final String INSTALL_LOG_FILE = "install.log";

	/** The Constant INSTALL_LCP_FILE. */
	public static final String INSTALL_LCP_FILE = "installlcp.txt";

	/** The Constant INSTALL_LCP_LOG_FILE. */
	public static final String INSTALL_LCP_LOG_FILE = "installlcp.log";

	/** The Constant INSTALL_ABORT_LOG_FILE. */
	public static final String INSTALL_ABORT_LOG_FILE = "installabort.log";

	/** The Constant INSTALL_LIST_FILE. */
	public static final String INSTALL_LIST_FILE = "installlist.txt";

	/** The Constant SECONDARY_INSTALL_FILE. */
	public static final String SECONDARY_INSTALL_FILE = "installsecondary.txt";

	/** The Constant BACKUP_INSTALL_FILE. */
	public static final String BACKUP_INSTALL_FILE = "installbackup.txt";

	/** The Constant DOWNLOAD_LIST_FILE. */
	public static final String DOWNLOAD_LIST_FILE = "downloadlist.txt";

	/** The Constant TO_BE_INSTALLED_FILE. */
	public static final String TO_BE_INSTALLED_FILE = "tobeinstalled.txt";

	/** The Constant DOWNLOAD_ABORT_LOG_FILE. */
	public static final String DOWNLOAD_ABORT_LOG_FILE = "downloadabort.txt";

	/** The Constant LA_FILE. */
	public static final String LA_FILE = "la.txt";

	/** The Constant CII_EXTN. */
	public static final String CII_EXTN = ".cii";

	/**
	 * Checks if is ready.
	 * 
	 * @return true, if is ready
	 */
	public boolean isReady() {
		return isReady;
	}

	/**
	 * Checks if is installing.
	 * 
	 * @return true, if is installing
	 */
	public boolean isInstalling() {
		return isInstalling;
	}

	/**
	 * Sets the installing.
	 * 
	 * @param isInstalling
	 *            the new installing
	 */
	public void setInstalling(final boolean isInstalling) {
		this.isInstalling = isInstalling;
		updateHandler.setProperty(UpdateHandler.Properties.IS_INSTALLING.getPropertyName(), String.valueOf(isInstalling));
	}

	/**
	 * Sets the self load flag.
	 * 
	 * @param selfLoadFlag
	 *            the new self load flag
	 */
	public void setSelfLoadFlag(final boolean selfLoadFlag) {
		this.selfLoadFlag = selfLoadFlag;
	}

	/**
	 * Gets the queue processor.
	 * 
	 * @return the queue processor
	 */
	public final UpdateServiceQueueProcessor getQueueProcessor() {
		if (queueProcessor == null) {
			queueProcessor = new UpdateServiceQueueProcessor("Update Service Queue Proccessor");
		}
		return queueProcessor;
	}

	/**
	 * Gets the status queue processor.
	 * 
	 * @return the status queue processor
	 */
	private final StatusQueueProcessor getStatusQueueProcessor() {
		if (statusQueueProcessor == null) {
			statusQueueProcessor = new StatusQueueProcessor("Status Queue Proccessor");
		}
		return statusQueueProcessor;
	}

	/**
	 * Gets the installer.
	 * 
	 * @return the installer
	 */
	public Installer getInstaller() {
		return installer;
	}

	/**
	 * Gets the installerThread.
	 * 
	 * @return the installer thread
	 */
	public Thread getInstallerThread() {
		return installerThread;
	}

	/**
	 * Gets the sa processor.
	 * 
	 * @return the sa processor
	 */
	public SAProcessor getSaProcessor() {
		return saProcessor;
	}

	/**
	 * Gets the state manager.
	 * 
	 * @return the state manager
	 */
	public StateManager getStateManager() {
		return stateManager;
	}

	/**
	 * Gets the lru instance.
	 * 
	 * @return the lru instance
	 */
	public String getLruInstance() {
		return lruInstance;
	}

	/**
	 * Gets the ftp message interval.
	 * 
	 * @return the ftp message interval
	 */
	public int getFtpMessageInterval() {
		return ftpMessageInterval;
	}

	/**
	 * Gets the la wait time.
	 * 
	 * @return the la wait time
	 */
	public long getLaWaitTime() {
		return laWaitTime;
	}

	/**
	 * Gets the final status duration.
	 * 
	 * @return the final status duration
	 */
	public int getFinalStatusDuration() {
		return finalStatusDuration;
	}

	/**
	 * Gets the final status frequency.
	 * 
	 * @return the final status frequency
	 */
	public int getFinalStatusFrequency() {
		return finalStatusFrequency;
	}

	/**
	 * Gets the queue timeout.
	 * 
	 * @return the queue timeout
	 */
	public int getQueueTimeout() {
		return queueTimeout;
	}

	/**
	 * Gets the local ftp path for cii.
	 * 
	 * @return the local ftp path for cii
	 */
	public String getLocalFtpPathForCii() {
		return localFtpPathForCii;
	}

	/**
	 * Gets the local ftp path for loadable.
	 * 
	 * @return the local ftp path for loadable
	 */
	public String getLocalFtpPathForLoadable() {
		return localFtpPathForLoadable;
	}

	/**
	 * Gets the path to install log.
	 * 
	 * @return the path to install log
	 */
	public String getPathToInstallLog() {
		return pathToInstallLog;
	}

	/**
	 * Gets the conformity checker.
	 * 
	 * @return the conformity checker
	 */
	public ConformityChecker getConformityChecker() {
		return conformityChecker;
	}

	/**
	 * Gets the ma processor.
	 * 
	 * @return the ma processor
	 */
	public MAProcessor getMaProcessor() {
		return maProcessor;
	}

	/**
	 * Gets the cii parser.
	 * 
	 * @return the cii parser
	 */
	public CIIParser getCiiParser() {
		return ciiParser;
	}

	/**
	 * Gets the la processor.
	 * 
	 * @return the la processor
	 */
	public LAProcessor getLaProcessor() {
		return laProcessor;
	}

	/**
	 * Gets the primary packages list.
	 * 
	 * @return the primary packages list
	 */
	public List<String> getPrimaryPackagesList() {
		return primaryPackagesList;
	}

	/**
	 * Gets the lcp to LRU types.
	 * 
	 * @return the lcp to LRU types
	 */
	public HashMap<String, String> getLcpToLRUTypes() {
		return lcpToLRUTypes;
	}

	/**
	 * Gets the lru to LCP types.
	 * 
	 * @return the lru to LCP types
	 */
	public HashMap<String, String> getLruToLCPTypes() {
		return lruToLCPTypes;
	}

	/**
	 * Gets the secondary lru list.
	 * 
	 * @return the secondary lru list
	 */
	public List<String> getSecondaryLruList() {
		return secondaryLruList;
	}

	/**
	 * Gets the backup lru list.
	 * 
	 * @return the backup lru list
	 */
	public List<String> getBackupLruList() {
		return backupLruList;
	}

	/**
	 * Gets the known LCP types.
	 * 
	 * @return the known LCP types
	 */
	public List<String> getKnownLCPTypes() {
		return knownLCPTypes;
	}

	/**
	 * Gets the path to cii.
	 * 
	 * @return the path to cii
	 */
	public String getPathToCii() {
		return pathToCii;
	}

	/**
	 * Gets the path to loadable.
	 * 
	 * @return the path to loadable
	 */
	public String getPathToLoadable() {
		return pathToLoadable;
	}

	/**
	 * Gets the path to LA
	 * 
	 * @return the path to LA
	 */
	public String getPathToLa() {
		return pathToLa;
	}

	/**
	 * Sets the path to la.
	 * 
	 * @param pathToLa
	 *            the new path to la
	 */
	public void setPathToLa(final String pathToLa) {
		this.pathToLa = pathToLa;
	}

	/**
	 * Gets the hw part numbers.
	 * 
	 * @return the hw part numbers
	 */
	public HashMap<String, List<String>> getHwPartNumbers() {
		return hwPartNumbers;
	}

	/**
	 * Gets the multicast ip.
	 * 
	 * @return the multicast ip
	 */
	public String getMulticastIp() {
		return getMulticastGroup().getHostAddress();
	}

	/**
	 * Gets the multicast port.
	 * 
	 * @return the multicast port
	 */
	public int getMulticastPort() {
		return getPort();
	}

	/**
	 * Gets the wait for reboot object.
	 * 
	 * @return the wait for reboot
	 */
	public Object getWaitForReboot() {
		return waitForReboot;
	}

	/**
	 * Is reboot received.
	 * 
	 * @return is reboot received
	 */
	public boolean isRebootReceived() {
		return isRebootReceived;
	}

	/**
	 * Sets is reboot received.
	 * 
	 * @param isRebootReceived
	 *            the new rebootReceived
	 */
	public void setRebootReceived(final boolean isRebootReceived) {
		this.isRebootReceived = isRebootReceived;
	}

	/**
	 * Gets reboot command frequency.
	 * 
	 * @return the reboot command frequency
	 */
	public int getRebootCommandFrequency() {
		return rebootCommandFrequency;
	}

	/**
	 * Gets reboot command send limit.
	 * 
	 * @return the reboot command send limit
	 */
	public int getRebootCommandSendLimit() {
		return rebootCommandSendLimit;
	}

	/**
	 * Gets reboot command timeout.
	 * 
	 * @return the reboot command timeout
	 */
	public int getRebootCommandTimeout() {
		return rebootCommandTimeout;
	}

	/**
	 * Gets sleep before reboot.
	 * 
	 * @return the sleep before reboot
	 */
	public int getSleepBeforeReboot() {
		return sleepBeforeReboot;
	}

	/**
	 * Gets the update handler.
	 * 
	 * @return the update handler
	 */
	public UpdateHandler getUpdateHandler() {
		return updateHandler;
	}

	public String getLCPType() {
		return updateHandler.getLCPType();
	}

	/**
	 * Sets the update handler reference.
	 */
	private void setUpdateHandlerReference() {
		final Handler handler = getComponents().getHandler("UpdateHandler");

		if (handler != null) {
			if (handler instanceof UpdateHandler) {
				updateHandler = (UpdateHandler) handler;
			} else {
				UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: " + handler.getName() + " not an instance of UpdateHandler"));
			}
		} else {
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: Update Handler is null"));
		}
	}

	/**
	 * Sets the adapter references.
	 */
	private void setAdapterReferences() {
		ftpAdapter = updateHandler.getFTPAdapter();

		if (ftpAdapter == null) {
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: FTP Adapter is null."));
		}
	}

	/**
	 * Sets the state manager reference.
	 */
	private void setStateManagerReference() {
		stateManager = UnitManager.ObjectModel.getStateManager();

		if (stateManager == null) {
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: State Manager is null."));
		}
	}

	/**
	 * Sets the hardware details.
	 */
	private void setHardwareDetails() {
		hardwareDetails = updateHandler.getHardwareDetails();
		if (hardwareDetails == null) {
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: HardwareInfo is null"));
		}
	}

	/**
	 * Sets the primary packages list.
	 */
	private void setPrimaryPackagesList() {
		primaryPackagesList = updateHandler.getPrimaryPackagesList();
		if (primaryPackagesList == null) {
			primaryPackagesList = new ArrayList<String>();
		}
	}

	/**
	 * Initializes update service.
	 */
	private void initializeUpdateService() {
		if (isInfo()) {
			logInfo("Update Service: Initializing Update Service");
		}
		lruInstance = UnitManager.ObjectModel.getUnit().getInstanceName();
		setUpdateHandlerReference();
		setAdapterReferences();
		setStateManagerReference();
		setHardwareDetails();
		setPrimaryPackagesList();

		// All the possible LCP types except the regular LCP
		// are hard coded here
		// All the possible primary packages for all LRU types
		// are also hard coded here
		lcpToLRUTypes = new HashMap<String, String>();
		lcpToLRUTypes.put("hlcp", "htse");

		lruToLCPTypes = new HashMap<String, String>();
		lruToLCPTypes.put("htse", "hlcp");
		lruToLCPTypes.put("bmp", NO_LCP);
		lruToLCPTypes.put("mcp", NO_LCP);
		lruToLCPTypes.put("mmcdp", NO_LCP);
		lruToLCPTypes.put("esp", NO_LCP);
		lruToLCPTypes.put("map", NO_LCP);
		lruToLCPTypes.put("mmcntp", NO_LCP);
		lruToLCPTypes.put("mmcfgp", NO_LCP);
		lruToLCPTypes.put("mmdbp", NO_LCP);
		lruToLCPTypes.put("avb", NO_LCP);
		lruToLCPTypes.put("aob", NO_LCP);

		// Remove any empty strings present under
		// Backup LRU and Secondary LRU lists

		if (backupLruList.size() > 0) {
			final Iterator<String> backupItr = backupLruList.iterator();
			while (backupItr.hasNext()) {
				final String lruType = backupItr.next();
				if (lruType != null && lruType.length() == 0) {
					if (isInfo()) {
						logInfo("Update Service: Removing empty string from backup LRU list");
					}
					backupItr.remove();
				}
			}
		}

		if (secondaryLruList.size() > 0) {
			final Iterator<String> secondaryItr = secondaryLruList.iterator();
			while (secondaryItr.hasNext()) {
				final String lruType = secondaryItr.next();
				if (lruType != null && lruType.length() == 0) {
					if (isInfo()) {
						logInfo("Update Service: Removing empty string from secondary LRU list");
					}
					secondaryItr.remove();
				}
			}
		}

		knownLCPTypes = new ArrayList<String>();
		for (final String lruType : backupLruList) {
			String knownLCPType = lruToLCPTypes.get(lruType);
			if (knownLCPType == null) {
				knownLCPType = LCP_TYPE;
			}
			knownLCPTypes.add(knownLCPType);
		}

		waitForReboot = new Object();
		isRebootReceived = false;

		logParser = new LogParser();
		logParser.setUpdateService(this);
		logParser.setUpdateHandler(updateHandler);
		logParser.setHardwareDetails(hardwareDetails);

		wrongCA = new ArrayList<String>();

		loadedSecondaryLrus = new ArrayList<String>();

		conformityChecker = new ConformityChecker();
		conformityChecker.setUpdateService(this);
		conformityChecker.setHardwareDetails(hardwareDetails);
		conformityChecker.setPrimaryPackagesList(primaryPackagesList);
		conformityChecker.setSecondaryLruList(secondaryLruList);
		conformityChecker.setBackupLruList(backupLruList);

		laProcessor = new LAProcessor();
		laProcessor.setUpdateService(this);
		laProcessor.setPrimaryPackagesList(primaryPackagesList);
		laProcessor.setBackupLruList(backupLruList);
		laProcessor.setSecondaryLruList(secondaryLruList);
		laProcessor.setConformityChecker(conformityChecker);
		laProcessor.setHardwareDetails(hardwareDetails);

		electionProcessor = new ElectionProcessor();
		electionProcessor.setUpdateService(this);
		electionProcessor.setHardwareDetails(hardwareDetails);
		electionProcessor.setConformityChecker(conformityChecker);
		electionProcessor.setPrimaryPackagesList(primaryPackagesList);
		electionProcessor.setBackupLruList(backupLruList);
		electionProcessor.setSecondaryLruList(secondaryLruList);
		electionProcessor.setStateManager(stateManager);
		votes = new HashMap<String, HashMap<String, List<String>>>();
		voters = new HashMap<String, CAMessage>();
		hwPartNumbers = new HashMap<String, List<String>>();
		votingTimer = new CountdownTimer();

		maProcessor = new MAProcessor();
		maProcessor.setUpdateService(this);
		maProcessor.setUpdateHandler(updateHandler);
		maProcessor.setStateManager(stateManager);
		maProcessor.setHardwareDetails(hardwareDetails);
		maProcessor.setMediaLoadCompleted(true);

		saProcessor = new SAProcessor();
		saProcessor.setUpdateService(this);
		saProcessor.setUpdateHandler(updateHandler);
		saProcessor.setStatusQueueProcessor(getStatusQueueProcessor());
		saProcessor.setStatusTimeout(statusTimeout);
		saProcessor.setStatusQueueTimeout(statusQueueTimeout);
		saProcessor.setStateManager(stateManager);

		if (isInfo()) {
			logInfo("Update Service: Update Service initialization complete");
		}
	}

	/**
	 * Starts the update service only when the update handler indicates that the LRU is ready to
	 * accept any NAND writes or when the start timeout gets expired.
	 */
	private void startUpdateService() {
		if (updateHandler.isReady()) {
			if (isInfo()) {
				logInfo("Update Service: Getting ready");
			}

			// CR ID RC-TC00013024
			// Send the Multicast Group Join request once again after state manager synchronization
			try {
				if (isInfo()) {
					logInfo("Update Service: Joining Multicast group once again after getting ready");
				}
				joinMulticastGroup(getMulticastGroup());
			} catch (final MulticastServiceSocketException e) {
				if (isInfo()) {
					UnitManager.Logging.logSevere("Update Service: Exception occured while joining multicast group in Update Service Force Join '" + getName() + "'", e);
				}
			}

			isInitialized = updateHandler.initializeForLoad();
			if (!isInitialized) {
				if (isInfo()) {
					logInfo("Update Service: Failed to initialize for load during first time");
				}
				int retryCount = 0;
				while (retryCount <= 2) {
					retryCount++;
					if (isInfo()) {
						logInfo("Update Service: Attempting retry: " + retryCount);
					}
					isInitialized = updateHandler.initializeForLoad();
					if (isInitialized) {
						break;
					}
				}
			}
			if (!isInitialized) {
				UnitManager.Logging.logSevere("Update Service: Failed to initialize for load");
			} else {
				if (isInfo()) {
					logInfo("Update Service: Initialized for load successfully");
				}
			}

			// Mount the partitions to read-write to perform any deletion
			if (!updateHandler.mountToReadWrite()) {
				UnitManager.Logging.logWarning("Update Service: Failed to mount partitions to read-write.");
			}

			laProcessor.readOriginalTime(pathToLa, LA_FILE);
			deleteCompatibilityMatrix();
			checkPreviousInstallation();
			createCA();

			// Verify the list of ParentCIs with 'to be installed' file
			// Do not start sending the CA Message if the verification fails
			// This will make sure no other LRU can self load from this LRU
			isToBeInstalledGood = verifyToBeInstalled();

			// Mount back the partitions to read-only
			if (!updateHandler.mountToReadOnly()) {
				UnitManager.Logging.logWarning("Update Service: Failed to mount partitions to read-only.");
			}

			conformityChecker.setParentCIs(parentCIs);
			laProcessor.setParentCIs(parentCIs);
			electionProcessor.setParentCIs(parentCIs);
			maProcessor.setParentCIs(parentCIs);

			if (secondaryLruList != null && secondaryLruList.size() > 0) {
				for (final String secondaryLru : secondaryLruList) {
					if (loadedSecondaryLrus != null && loadedSecondaryLrus.contains(secondaryLru)) {
						if (isInfo()) {
							logInfo("Update Service: Already loaded " + secondaryLru);
						}
						continue;
					}
					if (isInfo()) {
						logInfo("Update Service: Looping through secondary LRU: " + secondaryLru);
					}
					final ParentCI parentCI = parentCIs.get(secondaryLru);
					if (parentCI != null) {
						final String partNumber = parentCI.getCpn();
						final String buildNumber = parentCI.getBuildNumber();
						final boolean forceLoad = false;
						String downloadPath = null;
						if (pathToLoadable.endsWith(Consts.IOs.FILE_SEPARATOR)) {
							downloadPath = pathToLoadable + parentCI.getParent811FileName();
						} else {
							downloadPath = pathToLoadable + Consts.IOs.FILE_SEPARATOR + parentCI.getParent811FileName();
						}
						if (updateHandler != null) {
							if (isInfo()) {
								logInfo("Update Service: Setting states for: " + secondaryLru);
								logInfo("Update Service: Part Number: " + partNumber);
								logInfo("Update Service: Build Number: " + buildNumber);
								logInfo("Update Service: Download Path: " + downloadPath);
								logInfo("Update Service: Force Load: " + forceLoad);
							}

							updateHandler.loadSecondaryLru(null, secondaryLru, downloadPath, partNumber, buildNumber, forceLoad);
						} else {
							UnitManager.Logging.logSevere("Update Service: Not able to set states for: " + secondaryLru);
						}
					} else {
						if (isInfo()) {
							logInfo("Update Service: CII not available for: " + secondaryLru);
						}
					}
				}
			}

			try {
				if (updateHandler != null) {
					updateHandler.runConformity(parentCIs);
				}
			} catch (Exception e) {
				UnitManager.Logging.logSevere("Update Service: Exception while running conformity", e);
			}

			startConfigurationAnnouncer();
			votingTimer.startTimer(initialVotingTime);
			isReady = true;
			getQueueProcessor().setTimeout(0);
			updateHandler.setProperty(UpdateHandler.Properties.DISABLE_SOFTWARE_LOAD.getPropertyName(), "false");

			if (isInfo()) {
				logInfo("Update Service: Update Service is ready");
			}
		} else {
			if (isInfo()) {
				logInfo("Update Service: Not yet ready");
			}
			isReady = false;
		}
	}

	/**
	 * Delete compatibility matrix.
	 */
	private void deleteCompatibilityMatrix() {
		updateHandler.deleteCompatibilityMatrix();
	}

	/**
	 * Checks for previous installation every time during the startup and sends a Status
	 * Announcement message with the last installation status.
	 */
	private void checkPreviousInstallation() {
		try {
			if (isInfo()) {
				logInfo("Update Service: Checking for previous installation");
			}
			if (loadedSecondaryLrus != null && loadedSecondaryLrus.size() > 0) {
				if (isInfo()) {
					logInfo("Update Service: Clearing the old data for loaded secondary LRUs");
				}
				loadedSecondaryLrus.clear();
			}

			setInstalling(false);
			boolean isLogAvailable = false;
			final SAMessage sa = new SAMessage();
			sa.setUpdateService(this);
			logParser.setDeleted(false);

			// Check for partial download
			// If this log exists then the unit is power cycled during download step

			final String status = logParser.checkDownloadAbort();

			// Check for LCP header file
			// If this header file exists then the unit is power cycled during LCP burning
			// And the unit failed to recover upon power cycle

			final ArrayList<String> lcpFiles = logParser.parseLCPHeader();

			if ((status != null && status.length() > 0) || (lcpFiles != null && lcpFiles.size() > 0)) {
				if (isInfo()) {
					logInfo("Update Service: Either partial download has happened OR LCP burning has failed");
				}
				isLogAvailable = true;
				sa.setIpAddress(hardwareDetails.getIpAddress());
				sa.setLruType(hardwareDetails.getDeviceType());
				sa.setLruInstance(lruInstance);
				sa.setTotalSteps(0);
				sa.setStepNumber(0);
				if (status != null && status.length() > 0) {
					sa.setStepName(status);
				} else {
					sa.setStepName("LCP installation aborted during burning files");
				}
				sa.setSubStepName("Status: FAIL");
				sa.setSubStepPercentage("100%");
				sa.setStatusAvailable(true);
				sa.setRunning(true);
			} else {
				// Check for abort log
				// If this log exists then the installation failed due to some known reason

				final ArrayList<InstallStatus> abortInstallStatusList = logParser.parseAbortLog(true);
				if (abortInstallStatusList != null && abortInstallStatusList.size() > 0) {
					// Form the install status string as the following example:
					// LCP=fail, PSW=fail, PPC=fail, HDAV=fail
					final StringBuilder sb = new StringBuilder();
					int totalSteps = 0;
					String abortReason = "Installation complete";
					for (int i = 0; i < abortInstallStatusList.size(); i++) {
						isLogAvailable = true;
						final InstallStatus installStatus = abortInstallStatusList.get(i);
						final String lruType = installStatus.getLruType();
						totalSteps = installStatus.getTotalSteps();
						if (!installStatus.getAbortReason().equals(LogParser.NOT_AVAILABLE)) {
							abortReason = installStatus.getAbortReason();
						}
						final String overallInstall = installStatus.getOverallInstall();

						sb.append(lruType.toUpperCase());
						sb.append("=");
						sb.append(overallInstall);
						if (i != abortInstallStatusList.size() - 1) {
							sb.append(", ");
						}
					}
					if (isInfo()) {
						logInfo("Update Service: Install status string: " + sb.toString());
					}

					sa.setIpAddress(hardwareDetails.getIpAddress());
					sa.setLruType(hardwareDetails.getDeviceType());
					sa.setLruInstance(lruInstance);
					sa.setTotalSteps(totalSteps);
					sa.setStepNumber(totalSteps);
					sa.setStepName(abortReason);
					sa.setSubStepName("Status: " + sb.toString());
					sa.setSubStepPercentage("100%");
					sa.setStatusAvailable(true);
					sa.setRunning(true);
				} else {
					// Check for all other log files
					// If any of the log file exists, then the installation is completed
					// Status could still be either PASS or FAIL

					final ArrayList<InstallStatus> installStatusList = logParser.parseAllInstallLogs(true);
					if (installStatusList != null && installStatusList.size() > 0) {
						isLogAvailable = true;
						// Form the install status string as the following example:
						// LCP=pass, PSW=pass, PPC (secondary)=pass, HDAV (backup)=pass
						final StringBuilder sb = new StringBuilder();
						int totalSteps = 0;
						for (int i = 0; i < installStatusList.size(); i++) {
							final InstallStatus installStatus = installStatusList.get(i);
							final String updateTime = installStatus.getUpdateTime();
							final String lruType = installStatus.getLruType();
							final String partNumber = installStatus.getPartNumber();
							final String buildNumber = installStatus.getBuildNumber();
							final boolean isForceLoad = installStatus.isForceLoad();
							final String LA = installStatus.getLA();
							String loadableFile = null;
							String downloadPath = null;
							if (installStatus.getLoadableFile() != null && installStatus.getLoadableFile().lastIndexOf(Consts.IOs.FILE_SEPARATOR) > 0) {
								loadableFile = installStatus.getLoadableFile().substring(installStatus.getLoadableFile().lastIndexOf(Consts.IOs.FILE_SEPARATOR) + 1);
							}

							if (loadableFile != null) {
								if (pathToLoadable.endsWith(Consts.IOs.FILE_SEPARATOR)) {
									downloadPath = pathToLoadable + loadableFile;
								} else {
									downloadPath = pathToLoadable + Consts.IOs.FILE_SEPARATOR + loadableFile;
								}
							}

							if (totalSteps == 0) {
								totalSteps = installStatus.getTotalSteps();
							} else {
								if (isInfo()) {
									logInfo("Update Service: Total steps is already initialized: " + totalSteps);
								}
							}

							final String overallInstall = installStatus.getOverallInstall();

							sb.append(lruType.toUpperCase());
							if (installStatus.isSecondaryLru()) {
								sb.append(" (secondary)");

								// Load the secondary LRU
								if (!lruType.equals(LogParser.NOT_AVAILABLE) && updateTime != null && partNumber != null && buildNumber != null && !updateTime.equals(LogParser.NOT_AVAILABLE) && !partNumber.equals(LogParser.NOT_AVAILABLE) && !buildNumber.equals(LogParser.NOT_AVAILABLE) && downloadPath != null && !LA.equals(LogParser.NOT_AVAILABLE)) {
									if (isInfo()) {
										final StringBuilder logString = new StringBuilder();
										logString.append("Setting states for secondary LRU: ");
										logString.append(lruType);
										logString.append(" : Part Number : ");
										logString.append(partNumber);
										logString.append(" : Build Number : ");
										logString.append(buildNumber);
										logString.append(" : ForceLoad : ");
										logString.append(isForceLoad);
										logInfo("Update Service: " + logString.toString());
									}
									if (!installStatus.getStatesAlreadySet().equals("true")) {
										if (isInfo()) {
											logInfo("Update Service: Setting states for secondary LRU: " + lruType);
										}
										updateHandler.loadSecondaryLru(updateTime, lruType, downloadPath, partNumber, buildNumber, isForceLoad, LA);
										logParser.updateStatesAlreadySet(SECONDARY_INSTALL_FILE, "true");
										if (loadedSecondaryLrus != null) {
											if (isInfo()) {
												logInfo("Update Service: Adding " + lruType + " to loaded secondary LRUs");
											}
											loadedSecondaryLrus.add(lruType);
										}
									} else {
										if (isInfo()) {
											logInfo("Update Service: No need to set states for secondary LRU: " + lruType);
										}
									}
								} else {
									final StringBuilder logString = new StringBuilder();
									logString.append("Failed to set state for secondary LRU: ");
									logString.append(lruType);
									logString.append(" : Part Number : ");
									logString.append(partNumber);
									logString.append(" : Build Number : ");
									logString.append(buildNumber);
									logString.append(" : FroceLoad : ");
									logString.append(isForceLoad);
									UnitManager.Logging.logSevere("Update Service: " + logString.toString());
								}
							} else if (installStatus.isBackupLru()) {
								sb.append(" (backup)");
							}
							sb.append("=");
							sb.append(overallInstall);
							if (i != installStatusList.size() - 1) {
								sb.append(", ");
							}
						}

						if (isInfo()) {
							logInfo("Update Service: Install status string: " + sb.toString());
						}

						sa.setIpAddress(hardwareDetails.getIpAddress());
						sa.setLruType(hardwareDetails.getDeviceType());
						sa.setLruInstance(lruInstance);
						sa.setTotalSteps(totalSteps);
						sa.setStepNumber(totalSteps);
						sa.setStepName("Install complete");
						sa.setSubStepName("Status: " + sb.toString());
						sa.setSubStepPercentage("100%");
						sa.setStatusAvailable(true);
						sa.setRunning(true);
					} else {
						isLogAvailable = false;
						if (isInfo()) {
							logInfo("Update Service: No installation log files are available");
						}
					}
				}
			}

			// Flush the changes if any files has been deleted
			if (logParser.isDeleted()) {
				if (isInfo()) {
					logInfo("Update Service: Files were deleted during the startup");
				}
				updateHandler.flushFiles(true);
				logParser.setDeleted(false);
			} else {
				if (isInfo()) {
					logInfo("Update Service: No files were deleted during the startup");
				}
			}

			// If any type of log files are available, then start the Status Announcer
			if (isLogAvailable) {
				if (isInfo()) {
					sa.printCurrentValues();
				}
				if (statusAnnouncer == null) {
					statusAnnouncer = new StatusAnnouncer();
				}
				statusAnnouncer.setUpdateService(this);
				statusAnnouncer.setSa(sa);
				statusAnnouncer.setSaMessageInterval(saMessageInterval);
				statusAnnouncer.setSendLimit(saSendLimit);
				statusAnnouncer.setRunning(true);
				try {
					statusAnnouncerThread = UnitManager.Threading.createThread(this, statusAnnouncer, "Update Service - Status Announcer");
					statusAnnouncerThread.start();
				} catch (CoreThreadException cte) {
					UnitManager.Logging.logSevere("Update Service: Error while creating Status Announcer thread", cte);
				}
			} else {
				if (isInfo()) {
					logInfo("Update Service: No need to send any final status. No installation log files are available");
				}
			}

			// Check for 'to be installed' file
			// If this file exists, then the last user driven load was not successful
			// So do not send CA messages out - meaning no one can self load from this LRU
			toBeInstalled = logParser.parseToBeInstalledFile();
		} catch (Exception e) {
			UnitManager.Logging.logSevere("Update Service: Error while checking for previous installation log files", e);
		}
	}

	/**
	 * Send status.
	 * 
	 * @param sa
	 *            the sa
	 * 
	 */
	public void sendStatus(final SAMessage sa) {
		try {
			if (isInfo()) {
				logInfo("Update Service: Sending SA Message");
			}
			send(sa);
		} catch (final ServiceIOException e) {
			UnitManager.Logging.logSevere("Update Service: Error while sending SA Message");
		}
	}

	/**
	 * Check for backup or secondary LRU installation alone where there is no need to restart the
	 * LRU.
	 */
	public void otherLruInstalled() {
		checkPreviousInstallation();
		createCA();
		conformityChecker.setParentCIs(parentCIs);
		laProcessor.setParentCIs(parentCIs);
		electionProcessor.setParentCIs(parentCIs);
		maProcessor.setParentCIs(parentCIs);
		try {
			if (updateHandler != null) {
				updateHandler.runConformity(parentCIs);
			}
		} catch (Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception while running conformity", e);
		}
		startServices();
	}

	/**
	 * Abort installation.
	 */
	public void abortInstallation() {
		updateHandler.onAbortInstallation();
		setInstalling(false);
		startServices();
		checkPreviousInstallation();
	}

	/**
	 * Start services.
	 */
	public void startServices() {
		votingTimer.startTimer(initialVotingTime);
		startConfigurationAnnouncer();
	}

	/**
	 * Stop services.
	 * 
	 * @param isRestart
	 *            the is restart
	 * 
	 */
	public void stopServices(final boolean isRestart) {
		stopConfigurationAnnouncer();
		if (isRestart) {
			updateHandler.stopGui();
			if (isInfo()) {
				logInfo("Update Service: Stop UIHandler");
			}

		} else {
			if (isInfo()) {
				logInfo("Update Service: No need to stop the UI, if any available");
			}
		}
	}

	/**
	 * Creates the CA.
	 */
	private void createCA() {
		if (isInfo()) {
			logInfo("Update Service: Creating CA Message");
		}

		ca = new CAMessage();
		ca.setUpdateService(this);
		ca.setLAMessageVersion(LAMessage.VERSION);
		ca.setSAMessageVersion(SAMessage.VERSION);
		ca.setHardwareDetails(hardwareDetails);

		if (!hardwareDetails.getDeviceType().equalsIgnoreCase(MCD)) {
			ca.setPathToCII(pathToCii);
			ca.setPathToLoadable(pathToLoadable);
		} else {
			// mcd special case
			// path is in the form <driveid>:\
			// need to discard <driveid>: for ftp
			String trimmedpathToCii = pathToCii.substring(pathToCii.indexOf(':') + 1);
			ca.setPathToCII(trimmedpathToCii);

			String trimmedpathToLoadable = pathToLoadable.substring(pathToLoadable.indexOf(':') + 1);
			ca.setPathToLoadable(trimmedpathToLoadable);
		}

		ca.setFtpIpAddress(updateHandler.getFtpIpAddress());
		ca.setFtpPortNumber(updateHandler.getFtpPortNumber());
		ca.setFtpUsername(updateHandler.getFtpUsername());
		ca.setFtpPassword(updateHandler.getFtpPassword());

		parseCIIFiles(pathToCii);
		ca.setParentCIs(parentCIs);
		if (parentCIs == null || parentCIs.size() == 0) {
			UnitManager.Logging.logSevere("Update Service: No ParentCIs available");
		}

		if (getLCPType().equals(LCP_TYPE)) {
			ca.setRegularLCPType(true);
			ca.setLcpType(LCP_TYPE);
		} else {
			ca.setRegularLCPType(false);
			ca.setLcpType(getLCPType());
		}

		ca.printCurrentValues();
	}

	/**
	 * Parses the CII files.
	 * 
	 * @param pathToCII
	 *            the path to CII
	 */
	private void parseCIIFiles(final String pathToCII) {
		String filePath = null;
		ciiParser = new CIIParser();
		parentCIs = new HashMap<String, ParentCI>();

		final List<String> ciiFiles = updateHandler.listFiles(pathToCII);
		if (ciiFiles != null) {
			for (final String fileName : ciiFiles) {
				if (fileName.endsWith(CII_EXTN)) {
					if (isInfo()) {
						logInfo("Update Service: Parsing CII File: " + fileName);
					}

					if (pathToCII.endsWith(Consts.IOs.FILE_SEPARATOR)) {
						filePath = pathToCII + fileName;
					} else {
						filePath = pathToCII + Consts.IOs.FILE_SEPARATOR + fileName;
					}

					if (isInfo()) {
						logInfo("Update Service: CII File Path: " + filePath);
					}

					final File ciiFile = new File(filePath);

					ParentCI parentCI = null;
					try {
						parentCI = ciiParser.parseCII(ciiFile);
					} catch (final ParserConfigurationException pce) {
						UnitManager.Logging.logSevere("Update Service: Error parsing the CII File: " + filePath, pce);
					} catch (final SAXException saxe) {
						UnitManager.Logging.logSevere("Update Service: Error parsing the CII File: " + filePath, saxe);
					} catch (final IOException ioe) {
						UnitManager.Logging.logSevere("Update Service: Error parsing the CII File: " + filePath, ioe);
					} catch (final UpdateServiceException use) {
						UnitManager.Logging.logSevere("Update Service: Error parsing the CII File: " + filePath, use);
					}

					if (parentCI == null) {
						if (isInfo()) {
							logInfo("Update Service: Parent CI is null after parsing: " + filePath);
							logInfo("Update Service: Deleting the corrupted CII file: " + filePath);
						}
						if (ciiFile.exists()) {
							if (ciiFile.delete()) {
								if (isInfo()) {
									logInfo("Update Service: Successfully deleted: " + filePath);
								}
							} else {
								UnitManager.Logging.logSevere("Update Service: Failed to delete the corrupted CII file: " + filePath);
							}
							updateHandler.flushFiles(filePath, true);
						}
						continue;
					}

					if (isInfo()) {
						logInfo("Update Service: ParentCI: " + parentCI.getCpn() + " Type: " + parentCI.getLruType());
					}

					// Check if this is a duplicate CII file for the same LRU type
					if (parentCIs.containsKey(parentCI.getLruType())) {
						UnitManager.Logging.logSevere("Update Service: Duplicate Parent CIs available for: " + parentCI.getLruType());

						UnitManager.Logging.logSevere("Update Service: Found: " + parentCI.getCiiFileName() + " and duplicate: " + parentCIs.get(parentCI.getLruType()).getCiiFileName());

						UnitManager.Logging.logSevere("Update Service: Deleting: " + parentCI.getCiiFileName() + " and " + parentCI.getParent811FileName());

						deleteParentCI(parentCI);
					} else {
						parentCIs.put(parentCI.getLruType(), parentCI);
					}
				}
			}
		} else {
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: No CII Files available to create CA Message"));
		}

		// Check is there any invalid Parent CIs
		if (parentCIs != null && parentCIs.size() > 0) {
			final Iterator<Entry<String, ParentCI>> parentCIsItr = parentCIs.entrySet().iterator();
			while (parentCIsItr.hasNext()) {
				final Entry<String, ParentCI> parentCIEntry = parentCIsItr.next();
				final ParentCI parentCI = parentCIEntry.getValue();
				final boolean isValid = isValidParentCI(parentCI);
				if (!isValid) {
					UnitManager.Logging.logSevere("Update Service: Deleting invalid Parent CI: " + parentCI.getCiiFileName() + " and " + parentCI.getParent811FileName());
					deleteParentCI(parentCI);
					parentCIsItr.remove();
				}
			}
		}
	}

	/**
	 * Checks if is valid parent CI.
	 * 
	 * @param parentCI
	 *            the parent CI
	 * 
	 * @return true, if is valid parent CI
	 */
	private boolean isValidParentCI(final ParentCI parentCI) {
		final String lruType = parentCI.getLruType();
		if (lruType.equals(hardwareDetails.getDeviceType()) || lruType.equals(getLCPType())) {
			if (isInfo()) {
				logInfo("Update Service: Valid parent CI: " + parentCI.getLruType() + " : " + parentCI.getCpn());
			}
			return true;
		} else if (primaryPackagesList.contains(lruType)) {
			if (isInfo()) {
				logInfo("Update Service: Valid primary package parent CI: " + parentCI.getLruType() + " : " + parentCI.getCpn());
			}
			return true;
		} else if (secondaryLruList.contains(lruType)) {
			if (isInfo()) {
				logInfo("Update Service: Valid secondary LRU parent CI: " + parentCI.getLruType() + " : " + parentCI.getCpn());
			}
			return true;
		} else if (backupLruList.contains(lruType)) {
			String backupLCPType = getLruToLCPTypes().get(lruType);
			if (backupLCPType == null) {
				backupLCPType = LCP_TYPE;
			}
			if (backupLCPType.equals(NO_LCP) || parentCIs.get(backupLCPType) != null) {
				if (isInfo()) {
					logInfo("Update Service: Valid backup LRU parent CI: " + parentCI.getLruType() + " : " + parentCI.getCpn());
				}
				return true;
			} else {
				if (isInfo()) {
					logInfo("Update Service: Invalid backup LRU parent CI: " + parentCI.getLruType() + " : " + parentCI.getCpn());
					logInfo("Update Service: No Parent CI available for LCP type: " + backupLCPType);
				}
				return false;
			}
		} else {
			boolean isLCPUserFound = false;
			String lcpUser = null;
			for (final String backupLru : backupLruList) {
				String backupLCPType = getLruToLCPTypes().get(backupLru);
				if (backupLCPType == null) {
					backupLCPType = LCP_TYPE;
				}
				if (lruType.equals(backupLCPType)) {
					isLCPUserFound = true;
					lcpUser = backupLru;
					break;
				}
			}
			if (isLCPUserFound) {
				if (parentCIs.get(lcpUser) != null) {
					if (isInfo()) {
						logInfo("Update Service: Valid LCP type: " + lruType + " for backup LRU: " + lcpUser);
					}
					return true;
				} else {
					if (isInfo()) {
						logInfo("Update Service: Invalid LCP type: " + lruType + " : " + parentCI.getCpn() + " as no backup copy is available for LRU: " + lcpUser);
					}
					return false;
				}
			} else {
				if (isInfo()) {
					logInfo("Update Service: Invalid LCP type: " + lruType + " : " + parentCI.getCpn() + " as no backup copy is using this");
				}
				return false;
			}
		}
	}

	/**
	 * Delete parent CI.
	 * 
	 * @param parentCI
	 *            the parent CI
	 */
	private void deleteParentCI(final ParentCI parentCI) {
		String ciiFilePath = null;
		String parent811FilePath = null;

		if (pathToCii.endsWith(Consts.IOs.FILE_SEPARATOR)) {
			ciiFilePath = pathToCii + parentCI.getCiiFileName();
		} else {
			ciiFilePath = pathToCii + Consts.IOs.FILE_SEPARATOR + parentCI.getCiiFileName();
		}

		if (pathToLoadable.endsWith(Consts.IOs.FILE_SEPARATOR)) {
			parent811FilePath = pathToLoadable + parentCI.getParent811FileName();
		} else {
			parent811FilePath = pathToLoadable + Consts.IOs.FILE_SEPARATOR + parentCI.getParent811FileName();
		}

		File file = new File(ciiFilePath);
		if (file.exists()) {
			if (file.delete()) {
				if (isInfo()) {
					logInfo("Deleted the CII file: " + ciiFilePath);
				}
			} else {
				UnitManager.Logging.logSevere("Failed to delete the CII file: " + ciiFilePath);
			}
		}

		file = new File(parent811FilePath);
		if (file.exists()) {
			if (file.delete()) {
				if (isInfo()) {
					logInfo("Deleted the loadable file: " + parent811FilePath);
				}
			} else {
				UnitManager.Logging.logSevere("Failed to delete the loadable file: " + parent811FilePath);
			}
		}
		updateHandler.flushFiles(true);
	}

	/**
	 * Verifies whether all the CII files in the 'to be installed file' is available or not.
	 */
	private boolean verifyToBeInstalled() {
		// If 'to be installed' file is missing, then return true by default
		if (toBeInstalled == null || toBeInstalled.size() <= 0) {
			return true;
		}

		// Loop through the 'to be installed' file to make sure everything listed
		// In 'to be installed' file is really installed
		// If anything listed in 'to be installed' file is not available, then return false
		for (Entry<String, ParentCI> toBeInstalledEntry : toBeInstalled.entrySet()) {
			final String lruType = toBeInstalledEntry.getKey();
			final ParentCI toBeParentCI = toBeInstalledEntry.getValue();
			
			if(toBeParentCI != null) {
				if(!isValidParentCI(toBeParentCI)) {
					// This ParentCI is not configured for this LRU
					continue;
				}

				if (parentCIs == null || !parentCIs.containsKey(lruType)) {
					return false;
				}
				
				final ParentCI currentParentCI = parentCIs.get(lruType);
				if (!currentParentCI.getCpn().equalsIgnoreCase(toBeParentCI.getCpn()) || !currentParentCI.getBuildNumber().equalsIgnoreCase(toBeParentCI.getBuildNumber())) {
					return false;
				}
			}
		}

		// If everything listed in 'to be installed' file is available
		// Then delete the 'to be installed' file
		String toBeInstalledFilePath = null;
		if (pathToInstallLog.endsWith(Consts.IOs.FILE_SEPARATOR)) {
			toBeInstalledFilePath = pathToInstallLog + TO_BE_INSTALLED_FILE;
		} else {
			toBeInstalledFilePath = pathToInstallLog + Consts.IOs.FILE_SEPARATOR + TO_BE_INSTALLED_FILE;
		}

		final File toBeInstalledFile = new File(toBeInstalledFilePath);
		if (toBeInstalledFile.exists()) {
			if (toBeInstalledFile.delete()) {
				if (isInfo()) {
					logInfo("Update Service: Deleted the file: " + toBeInstalledFilePath + " successfully");
				}
			} else {
				UnitManager.Logging.logSevere("Update Service: Failed to delete the file: " + toBeInstalledFilePath);
			}
		}
		
		return true;
	}

	/**
	 * Start configuration announcer.
	 */
	private void startConfigurationAnnouncer() {
		if (!isToBeInstalledGood) {
			ca.setParentCIs(new HashMap<String, ParentCI>());
			UnitManager.Logging.logWarning("UpdateService: Not all the files in the " + TO_BE_INSTALLED_FILE + " is available");
			UnitManager.Logging.logWarning("UpdateService: Sending CA message with no information on CII files");
		} else {
			if (isInfo()) {
				logInfo("Update Service: Sending CA message with all the CII files available");
			}
		}

		if (configurationAnnouncer == null) {
			configurationAnnouncer = new ConfigurationAnnouncer();
		}
		configurationAnnouncer.setUpdateService(this);
		configurationAnnouncer.setCa(ca);
		configurationAnnouncer.setCaMessageInterval(caMessageInterval);
		configurationAnnouncer.setRunning(true);
		try {
			configurationAnnouncerThread = UnitManager.Threading.createThread(this, configurationAnnouncer, "Update Service - Configuration Announcer");
			configurationAnnouncerThread.start();
		} catch (CoreThreadException cte) {
			UnitManager.Logging.logSevere("Update Service: Error while creating Configuration Announcer thread", cte);
		}
	}

	/**
	 * Stops the configuration announcer.
	 */
	private void stopConfigurationAnnouncer() {
		if (configurationAnnouncer != null) {
			configurationAnnouncer.setRunning(false);
		}
	}

	/**
	 * Stops the status announcer.
	 */
	private void stopStatusAnnouncer() {
		if (statusAnnouncer != null) {
			statusAnnouncer.setRunning(false);
			if (statusAnnouncerThread != null && statusAnnouncerThread.isAlive()) {
				try {
					statusAnnouncerThread.join(saSendLimit);
				} catch (InterruptedException ie) {
					UnitManager.Logging.logSevere("Update Service: Error while stopping the Status Announcer thread", ie);
				}
			}
		}
	}

	/**
	 * CA install - Starting installation through Self Load.
	 * 
	 * @param ca
	 *            the ca
	 * @param installList
	 *            the install list
	 */
	public void caInstall(final CAMessage ca, final InstallListInfo installList) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Update Service: Attempting self load from: ");
		sb.append(ca.getFtpIpAddress());
		sb.append(" to install: \n");
		for (final Entry<String, ParentCI> parentCIEntry : installList.getParentCIsToInstall().entrySet()) {
			final ParentCI parentCI = parentCIEntry.getValue();
			sb.append(" LruType : " + parentCI.getLruType() + " CPN : " + parentCI.getCpn() + " BuildNumber : " + parentCI.getBuildNumber() + "\n");
		}
		UnitManager.Logging.logWarning(sb.toString());
		installList.setFtpIPAddress(ca.getFtpIpAddress());
		installList.setFtpPortNumber(ca.getFtpPortNumber());
		installList.setFtpUserName(ca.getFtpUsername());
		installList.setFtpPassword(ca.getFtpPassword());
		installList.setPathToCII(ca.getPathToCII());
		installList.setPathToLoadable(ca.getPathToLoadable());
		installList.setNfs(false);
		installList.setForceLoad(false);
		loadTheSystem(installList, false);
	}

	/**
	 * LA install - Starting installation through Removable Media or TST.
	 * 
	 * @param la
	 *            the la
	 * @param installList
	 *            the install list
	 */
	public void laInstall(final LAMessage la, final InstallListInfo installList) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Update Service: User initiated load from: ");
		sb.append(la.getFtpIpAddress());
		sb.append(" ForceLoad : " + la.isForceLoadFlag());
		sb.append(" to install: \n");
		for (final Entry<String, ParentCI> parentCIEntry : installList.getParentCIsToInstall().entrySet()) {
			final ParentCI parentCI = parentCIEntry.getValue();
			sb.append(" LruType : " + parentCI.getLruType() + " CPN : " + parentCI.getCpn() + " BuildNumber : " + parentCI.getBuildNumber() + "\n");
		}
		UnitManager.Logging.logWarning(sb.toString());
		installList.setFtpIPAddress(la.getFtpIpAddress());
		installList.setFtpPortNumber(la.getFtpPortNumber());
		installList.setFtpUserName(la.getFtpUsername());
		installList.setFtpPassword(la.getFtpPassword());
		installList.setPathToCII(la.getPathToCII());
		installList.setPathToLoadable(la.getPathToLoadable());
		installList.setNfs(la.isNfs());
		installList.setForceLoad(la.isForceLoadFlag());
		installList.setRebootWait(la.isRebootWait());
		loadTheSystem(installList, true);
	}

	/**
	 * Start the installation.
	 * 
	 * @param installList
	 *            the install list
	 */
	private void loadTheSystem(final InstallListInfo installList, final boolean isUserTriggeredLoad) {
		if (installList == null) {
			UnitManager.Logging.logSevere("Update Service: An attempt has been made to install without anything in install list. This should never happen");
		} else {
			stopStatusAnnouncer();
			setInstalling(true);
			installList.printCurrentValues();

			if (installer == null) {
				installer = new Installer();
			}

			installer.setUpdateService(this);
			installer.setUpdateHandler(updateHandler);
			installer.setStateManager(stateManager);
			installer.setHardwareDetails(hardwareDetails);
			installer.setLogParser(logParser);
			installer.setFtpAdapter(ftpAdapter);
			installer.setUserTriggeredLoad(isUserTriggeredLoad);
			installer.setInstallList(installList);

			try {
				installerThread = UnitManager.Threading.createThread(this, installer, "Update Service - Installer");
				installerThread.start();
			} catch (CoreThreadException cte) {
				UnitManager.Logging.logSevere("Update Service: Error while creating Installer thread", cte);
			}
		}
	}

	/**
	 * Queue up the CA Message whenever it is received from all LRUs.
	 * 
	 * @param ca
	 *            the ca
	 * 
	 */
	private void processCAMessage(final CAMessage ca) {
		if (isReady) {
			if (isInitialized) {
				if (getQueueProcessor().getQueueSize() < queueProcessorSize) {
					getQueueProcessor().addQueue(ca);
				}
			} else {
				if (isInfo()) {
					logInfo("Update Service: Update Service is not yet initialized");
					logInfo("Update Service: Required partitions might not have been mounted");
					logInfo("Update Service: Attempting to initialize for load again");
				}
				isInitialized = updateHandler.initializeForLoad();
				if (!isInitialized) {
					if (isInfo()) {
						logInfo("Update Service: Failed to initialize for load again");
					}
				}
			}
		} else {
			if (isInfo()) {
				logInfo("Update Service: Update Service is not yet ready");
			}
		}
	}

	/**
	 * Process LA message whenever it is received by using the LAProcessor.
	 * 
	 * @param la
	 *            the la
	 * 
	 */
	private void processLAMessage(final LAMessage la) {
		stopStatusAnnouncer();

		synchronized (this) {
			if (!isReady) {
				final SAMessage sa = new SAMessage();
				sa.setUpdateService(this);
				sa.setIpAddress(hardwareDetails.getIpAddress());
				sa.setLruType(hardwareDetails.getDeviceType());
				sa.setLruInstance(lruInstance);
				sa.setTotalSteps(0);
				sa.setStepNumber(0);
				sa.setStepName("Update Service is not yet ready");
				sa.setSubStepName("Still in starting stage");
				sa.setSubStepPercentage("100%");
				sa.setStatusAvailable(true);
				sa.setRunning(true);
				sendStatus(sa);
				return;
			}

			if (!isInitialized) {
				SAMessage sa = new SAMessage();
				sa.setUpdateService(this);
				sa.setIpAddress(hardwareDetails.getIpAddress());
				sa.setLruType(hardwareDetails.getDeviceType());
				sa.setLruInstance(lruInstance);
				sa.setTotalSteps(0);
				sa.setStepNumber(0);
				sa.setStepName("Update Service is not yet initialized");
				sa.setSubStepName("Required partitions might not have been mounted");
				sa.setSubStepPercentage("100%");
				sa.setStatusAvailable(true);
				sa.setRunning(true);
				sendStatus(sa);

				if (isInfo()) {
					logInfo("Update Service: Update Service is not yet initialized");
					logInfo("Update Service: Required partitions might not have been mounted");
					logInfo("Update Service: Attempting to initialize for load again");
				}

				sa = new SAMessage();
				sa.setUpdateService(this);
				sa.setIpAddress(hardwareDetails.getIpAddress());
				sa.setLruType(hardwareDetails.getDeviceType());
				sa.setLruInstance(lruInstance);
				sa.setTotalSteps(0);
				sa.setStepNumber(0);
				sa.setStepName("Update Service is not yet initialized");
				sa.setSubStepName("Attempting to initialize for load again");
				sa.setSubStepPercentage("100%");
				sa.setStatusAvailable(true);
				sa.setRunning(true);
				sendStatus(sa);

				isInitialized = updateHandler.initializeForLoad();
				if (!isInitialized) {
					if (isInfo()) {
						logInfo("Update Service: Failed to initialize for load");
					}
					sa = new SAMessage();
					sa.setUpdateService(this);
					sa.setIpAddress(hardwareDetails.getIpAddress());
					sa.setLruType(hardwareDetails.getDeviceType());
					sa.setLruInstance(lruInstance);
					sa.setTotalSteps(0);
					sa.setStepNumber(0);
					sa.setStepName("Cannot load");
					sa.setSubStepName(LAProcessor.LA_PROCESS_COMPLETE + "Failed to initialize for load");
					sa.setSubStepPercentage("100%");
					sa.setStatusAvailable(true);
					sa.setRunning(true);
					sendStatus(sa);
					return;
				} else {
					if (isInfo()) {
						logInfo("Update Service: Initialized successfully for load");
					}
					sa = new SAMessage();
					sa.setUpdateService(this);
					sa.setIpAddress(hardwareDetails.getIpAddress());
					sa.setLruType(hardwareDetails.getDeviceType());
					sa.setLruInstance(lruInstance);
					sa.setTotalSteps(0);
					sa.setStepNumber(0);
					sa.setStepName("Update Service is not yet initialized");
					sa.setSubStepName("Initializing");
					sa.setSubStepPercentage("100%");
					sa.setStatusAvailable(true);
					sa.setRunning(true);
					sendStatus(sa);
				}
			}

			final boolean isReadyToLoad = maProcessor.processLAMessage(la);
			if (isReadyToLoad) {
				laProcessor.processLAMessage(la);
			} else {
				if (isInfo()) {
					logInfo("Update Service: Cannot process LA right now as media load is going on");
				}
			}
		}
	}

	/**
	 * Process SA message whenever it is received by using the SAProcessor.
	 * 
	 * @param sa
	 *            the sa
	 * 
	 */
	private void processSAMessage(final SAMessage sa) {
		if (sa.getIpAddress().equals(hardwareDetails.getIpAddress())) {
			if (sa.getTotalSteps() != sa.getStepNumber()) {
				if (isInfo()) {
					logInfo("Update Service: Stopping the status announcer as a new status is received");
				}
				stopStatusAnnouncer();
			} else {
				if (isInfo()) {
					logInfo("Update Service: No need to stop the status announcer as it is a final status");
				}
			}
		}
		if (maProcessor != null) {
			maProcessor.processSAMessage(sa);
		}
		if (getStatusQueueProcessor().getQueueSize() < statusQueueProcessorSize) {
			getStatusQueueProcessor().addQueue(sa);
		}
	}

	/**
	 * Process MA message whenever it is received by using the MAProcessor.
	 * 
	 * @param ma
	 *            the ma
	 * 
	 */
	private void processMAMessage(final MAMessage ma) {
		if (isReady) {
			maProcessor.processMAMessage(ma);
		} else {
			final SAMessage sa = new SAMessage();
			sa.setUpdateService(this);
			sa.setIpAddress(hardwareDetails.getIpAddress());
			sa.setLruType(hardwareDetails.getDeviceType());
			sa.setLruInstance(getLruInstance());
			sa.setTotalSteps(0);
			sa.setStepNumber(0);
			sa.setStepName("Update Service is not yet ready");
			sa.setSubStepName("Required partitions might not have been mounted");
			sa.setSubStepPercentage("100%");
			sa.setStatusAvailable(true);
			sa.setRunning(true);
			sendStatus(sa);
		}
	}

	/**
	 * Whenever a EA message is received it will be logged at Info level.
	 * 
	 * @param ea
	 *            the ea
	 * 
	 */
	private void processEAMessage(final EAMessage ea) {
		ea.printCurrentValues();
	}

	/**
	 * Whenever a NR message is received it will be logged.
	 * 
	 * @param nr
	 *            the nr
	 * 
	 */
	private void processNRMessage(final NRMessage nr) {
		if (isInfo()) {
			logInfo("Update Service: Received NR Message");
			nr.printCurrentValues();
		}
		if (nr.isCorruptionDetected()) {
			UnitManager.Logging.logSevere("Update Service: NAND corruption detected at: " + nr.getIpAddress());
			UnitManager.Logging.logSevere("Update Service: Corrupted partition: " + nr.getCorruptedPartition());
			UnitManager.Logging.logSevere("Update Service: Recovery status: " + nr.isRecoverySuccess());
		}
	}

	/**
	 * Processes all the possible message types received for Update Service.
	 */
	@Override
	protected void onReceivedMessage(final Object source, final MessagingServiceReceiveMessageArgs args) {
		String messageVersion = null;
		switch (UpdateServiceMessage.getType(args.getMessage())) {
		case CONFIGURATION_ANNOUNCEMENT:
			messageVersion = UpdateServiceMessage.getMessageVersion(args.getMessage().getJsonObject());
			if (!messageVersion.equals(CAMessage.VERSION)) {
				if (isInfo()) {
					logInfo("Update Service: CA version mismatch. Expected: " + CAMessage.VERSION + " Received: " + messageVersion + ". Continuing to process the message.");
				}
			}
			final CAMessage ca = new CAMessage(args.getMessage().getJsonObject());
			ca.setUpdateService(this);
			processCAMessage(ca);
			break;
		case LOAD_ANNOUNCEMENT:
			messageVersion = UpdateServiceMessage.getMessageVersion(args.getMessage().getJsonObject());
			if (!messageVersion.equals(LAMessage.VERSION)) {
				if (isInfo()) {
					logInfo("Update Service: LA version mismatch. Expected: " + LAMessage.VERSION + " Received: " + messageVersion + ". Continuing to process the message.");
				}
			}
			final LAMessage la = new LAMessage(args.getMessage().getJsonObject());
			la.setUpdateService(this);
			processLAMessage(la);
			break;
		case STATUS_ANNOUNCEMENT:
			messageVersion = UpdateServiceMessage.getMessageVersion(args.getMessage().getJsonObject());
			if (!messageVersion.equals(SAMessage.VERSION)) {
				if (isInfo()) {
					logInfo("Update Service: SA version mismatch. Expected: " + SAMessage.VERSION + " Received: " + messageVersion + ". Continuing to process the message.");
				}
			}
			final SAMessage sa = new SAMessage(args.getMessage().getJsonObject());
			sa.setUpdateService(this);
			processSAMessage(sa);
			break;
		case MEDIA_ANNOUNCEMENT:
			messageVersion = UpdateServiceMessage.getMessageVersion(args.getMessage().getJsonObject());
			if (!messageVersion.equals(MAMessage.VERSION)) {
				if (isInfo()) {
					logInfo("Update Service: MA version mismatch. Expected: " + MAMessage.VERSION + " Received: " + messageVersion + ". Continuing to process the message.");
				}
			}
			final MAMessage ma = new MAMessage(args.getMessage().getJsonObject());
			ma.setUpdateService(this);
			processMAMessage(ma);
			break;
		case ELECTION_ANNOUNCEMENT:
			messageVersion = UpdateServiceMessage.getMessageVersion(args.getMessage().getJsonObject());
			if (!messageVersion.equals(EAMessage.VERSION)) {
				if (isInfo()) {
					logInfo("Update Service: EA version mismatch. Expected: " + EAMessage.VERSION + " Received: " + messageVersion + ". Continuing to process the message.");
				}
			}
			final EAMessage ea = new EAMessage(args.getMessage().getJsonObject());
			ea.setUpdateService(this);
			processEAMessage(ea);
			break;
		case NAND_RECOVERY:
			messageVersion = UpdateServiceMessage.getMessageVersion(args.getMessage().getJsonObject());
			if (!messageVersion.equals(NRMessage.VERSION)) {
				if (isInfo()) {
					logInfo("Update Service: NR version mismatch. Expected: " + NRMessage.VERSION + " Received: " + messageVersion + ". Continuing to process the message.");
				}
			}
			final NRMessage nr = new NRMessage(args.getMessage().getJsonObject());
			nr.setUpdateService(this);
			processNRMessage(nr);
			break;
		default:
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: Received Unknown Message"));
		}
	}

	/**
	 * This method gets called whenever a CA Message is queued up. It collects all the CA Messages
	 * and retrieves the votes for various LRUs. It also checks whether the voting timeout has been
	 * expired whenever a CA Message is received.
	 */
	public void queueProcessorItemReceived(final QueueProcessor<CAMessage> processor, final CAMessage item) {
		if (!isReady) {
			startUpdateService();
			return;
		}

		if (isInstalling) {
			if (isInfo()) {
				logInfo("Update Service: Election process stopped as installation is going on");
			}
			clearVotes();
			votingTimer.restart();
			return;
		}

		if (item == null) {
			UnitManager.Logging.logSevere("Update Service: CA Message received is null");
			processVotingTimeout();
			return;
		}

		final String ipAddress = item.getHardwareDetails().getIpAddress();
		if (ipAddress.length() > 0 && voters.containsKey(ipAddress)) {
			if (isInfo()) {
				logInfo("Update Service: Duplicate vote from: " + ipAddress);
			}
			processVotingTimeout();
			return;
		}

		final String deviceType = item.getHardwareDetails().getDeviceType();
		final String hwPartNumber = item.getHardwareDetails().getHardwarePartNumber();
		final HashMap<String, ParentCI> parentCIs = item.getParentCIs();

		if (deviceType.length() <= 0 || hwPartNumber.length() <= 0 || parentCIs == null) {
			if (!wrongCA.contains(ipAddress)) {
				UnitManager.Logging.logSevere("Update Service: CA Message is not good - Parameters are missing: " + ipAddress);
				wrongCA.add(ipAddress);
			}
			processVotingTimeout();
			return;
		}
		if (wrongCA.contains(ipAddress)) {
			wrongCA.remove(ipAddress);
		}

		// Check for duplicate vote
		if (!voters.containsKey(ipAddress)) {
			// Add to the list of voters
			if (isInfo()) {
				logInfo("Update Service: Adding vote from: " + ipAddress);
			}

			// Collect the hardware part numbers
			synchronized (hwPartNumbers) {
				List<String> partNumbers = hwPartNumbers.get(deviceType);
				if (partNumbers == null) {
					partNumbers = new ArrayList<String>();
					hwPartNumbers.put(deviceType, partNumbers);
				}
				if (!partNumbers.contains(hwPartNumber)) {
					partNumbers.add(hwPartNumber);
				}
			}

			// Reject votes from those LRUs which do not have any LCP installed
			boolean isLCPFound = false;
			String lcpType = item.getLcpType();
			float caVersion = 0;
			try {
				caVersion = Float.parseFloat(item.getMessageVersion());
			} catch (NumberFormatException nfe) {
				UnitManager.Logging.logSevere("Update Service: Rejecting the vote as the CA message is of unknown version: " + caVersion, nfe);
				caVersion = 0;
			}

			// Older CA messages do not include the LCP type info
			if (caVersion != 0 && caVersion <= 2.0) {
				// All CA message below version 2.0 always uses
				// Regular LCP - so set those details here
				item.setRegularLCPType(true);
				item.setLcpType(LCP_TYPE);
				if (parentCIs.get(LCP_TYPE) != null) {
					lcpType = LCP_TYPE;
					isLCPFound = true;
				} else {
					isLCPFound = false;
				}
			} else {
				if (lcpType == null) {
					isLCPFound = false;
				} else {
					if (parentCIs.get(lcpType) != null) {
						isLCPFound = true;
					} else {
						isLCPFound = false;
					}
				}
			}

			if (!isLCPFound) {
				if (isInfo()) {
					logInfo("Update Service: Rejecting the vote as there is no LCP: " + ipAddress);
				}
			} else {
				// Collect the votes
				voters.put(ipAddress, item);
				for (final Entry<String, ParentCI> parentCIEntry : parentCIs.entrySet()) {
					final String lruType = parentCIEntry.getKey();
					final ParentCI parentCI = parentCIEntry.getValue();

					// Ignore the vote for LCP right now
					// The LCP will be chosen after choosing the software
					if (parentCI.getLruType().equals(lcpType) || parentCI.getLruType().equals(LCP_TYPE) || lcpToLRUTypes.containsKey(parentCI.getLruType())) {
						continue;
					}

					HashMap<String, List<String>> lruVotes = votes.get(lruType);
					if (lruVotes == null) {
						lruVotes = new HashMap<String, List<String>>();
						votes.put(lruType, lruVotes);
					}

					List<String> ipAddresses = lruVotes.get(parentCI.getCpn());
					if (ipAddresses == null) {
						ipAddresses = new ArrayList<String>();
						lruVotes.put(parentCI.getCpn(), ipAddresses);
					}
					if (!ipAddresses.contains(ipAddress)) {
						ipAddresses.add(ipAddress);
					}
				}
			}
		} else {
			if (isInfo()) {
				logInfo("Update Service: Duplicate vote: " + ipAddress);
			}
		}

		processVotingTimeout();
	}

	/**
	 * The queue timeout will be set to a non zero value only: - During the startup of update
	 * service - Will be set back to 0, once the Update Service is ready - During a media load is
	 * going on - Will be set back to 0, when there is no need to wait for a response
	 * 
	 * @param processor
	 */
	public void queueProcessorTimeout(final QueueProcessor<CAMessage> processor) {
		if (isInfo()) {
			logInfo("Update Service: Queue timeout");
		}
		processTimers();
	}

	/**
	 * Processes timers to start the Update Service and for any media load that is going on.
	 */
	private void processTimers() {
		if (!isReady) {
			startUpdateService();
			return;
		}

		maProcessor.processTimeout();
	}

	/**
	 * Starts the election if the voting timeout has expired. The election happens only if self load
	 * is enabled. If any installation is going on the election process will not happen.
	 */
	private void processVotingTimeout() {
		if (votingTimer.hasExpired()) {
			if (isInstalling) {
				if (isInfo()) {
					logInfo("Update Service: Election process stopped as installation is going on");
				}
				clearVotes();
			} else {
				if (selfLoadFlag) {
					synchronized (this) {
						electionProcessor.setVoters(voters);
						electionProcessor.setVotes(votes);
						electionProcessor.processVotes();
						clearVotes();
					}
				} else {
					if (isInfo()) {
						logInfo("Update Service: Election process not required as self load is disabled");
					}
				}
			}
			votingTimer.startTimer(votingTime);
		}
	}

	/**
	 * Clear the collection objects that stores the votes of various LRUs, once the voting is
	 * completed.
	 */
	private void clearVotes() {
		votes.clear();
		voters.clear();
	}

	/**
	 * Get all the settings from the LCP and initialize the variables.
	 */
	@Override
	protected void onSetup(Object source, ComponentSetupArgs args) throws ComponentSetupException {
		super.onSetup(source, args);

		// Check whether the setting 'multicast service packet size' is having 
		// the min required value
		// This is to support backward compatibility of Release 4 or greater software 
		// with Release 3 LCP
		// The Release 3 LCP (build 250) do not have a large enough value for this setting
		// This causes UDP messages to be truncated and not able to start the load
		// This check makes sure it overrides the LCP setting if the value is less than min required
		// value
		if (getPacketSize() < MIN_PACKET_SIZE) {
			UnitManager.Logging.logWarning("Update Service: Overriding setting 'multicast service packet size': " + getPacketSize() + " is less than min value required: " + MIN_PACKET_SIZE);
			setPacketSize(MIN_PACKET_SIZE);
		}

		pathToCii = getSetting("Path To CII File", "/install");

		pathToLoadable = getSetting("Path To Loadable", "/bulk");

		caMessageInterval = getSetting("CA Message Interval", 5000);

		initialVotingTime = getSetting("Initial Voting Time Out", 60000);

		votingTime = getSetting("Voting Time Out", 10000);

		localFtpPathForCii = getSetting("Local FTP Path For CII File", "/install");

		localFtpPathForLoadable = getSetting("Local FTP Path For Loadable File", "/bulk");

		ftpMessageInterval = getSetting("FTP Status Interval", 10000);

		pathToInstallLog = getSetting("Path to install log file", "/install");

		selfLoadFlag = getSetting("Self Load", true);

		secondaryLruList = Arrays.asList(getSettingList("Secondary LRU List", new String[0]));

		backupLruList = Arrays.asList(getSettingList("Backup LRU List", new String[0]));

		queueTimeout = getSetting("Update Service Queue Timeout", 500);

		laWaitTime = getSetting("LA Wait Time", 300000);

		statusTimeout = getSetting("Status Timeout", 600000);

		statusQueueTimeout = getSetting("Status Queue Timeout", 10000);

		queueProcessorSize = getSetting("Queue Size", 100);

		statusQueueProcessorSize = getSetting("Status Queue Size", 100);

		finalStatusDuration = getSetting("Final Status Duration", 3000);

		finalStatusFrequency = getSetting("Final Status Frequency", 500);

		saMessageInterval = getSetting("Final Status Interval", 2000);

		saSendLimit = getSetting("Final Status Send Limit", 450);

		pathToLa = getSetting("Path To LA", "/install");

		rebootCommandFrequency = getSetting("Reboot Command Frequency", 1000);

		rebootCommandSendLimit = getSetting("Reboot Command Send Limit", 60);

		rebootCommandTimeout = getSetting("Reboot Command Timeout", 900000);

		sleepBeforeReboot = getSetting("Sleep Before Reboot", 60000);
	}

	/**
	 * Initialize all the variables and instantiate all the required classes. Start the queue
	 * processor threads.
	 */
	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args) throws ServiceStartException {
		super.onStarted(source, args);

		initializeUpdateService();
		getQueueProcessor().setTimeout(queueTimeout);
		getQueueProcessor().addListener(this);
		getStatusQueueProcessor().setTimeout(0);
		getStatusQueueProcessor().addListener(saProcessor);
		try {
			getQueueProcessor().start();
			getStatusQueueProcessor().start();
		} catch (final QueueProcessorException qpe) {
			UnitManager.Logging.logSevere("Update Service: Error starting UpdateServiceQueueProcessor", qpe);
		}
	}

	/**
	 * Stop the threads for Configuration Announcer, Queue Processor and Status Queue Processor.
	 * 
	 * @param source
	 * @param args
	 */
	@Override
	protected void onStopped(Object source, ServiceStopArgs args) throws ServiceStopException {
		if (configurationAnnouncer != null) {
			configurationAnnouncer.stop();
		}

		try {
			getQueueProcessor().stop();
			getStatusQueueProcessor().stop();
		} catch (final QueueProcessorException qpe) {
			UnitManager.Logging.logSevere("Update Service: Error stopping UpdateServiceQueueProcessor", qpe);
		}

		super.onStopped(source, args);
	}

	@Override
	public boolean isInfo() {
		return super.isInfo();
	}

	@Override
	public void logInfo(final String message) {
		super.logInfo(message);
	}
}
