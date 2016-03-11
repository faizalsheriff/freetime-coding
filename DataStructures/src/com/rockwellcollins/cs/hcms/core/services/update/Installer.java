package com.rockwellcollins.cs.hcms.core.services.update;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Key;
import java.security.KeyException;
import java.security.Provider;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.handlers.HandlerPropertyNotFoundException;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerMode;
import com.rockwellcollins.cs.hcms.core.services.update.messages.CAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;

/**
 * The Class Installer installs the necessary software for the LRU. The 
 * installer process runs as a separate thread and is responsible for 
 * installing secondary LRU, backup LRU, LCP and the actual LRU software.
 * The following process is followed for anything that needs to be installed:
 * 
 * - Download the CII and Loadable file via FTP using FTPAdapter or via NFS copy
 * - Check the digital signature of the CII file
 * - Run the MD5 checksum for the loadable file
 * - Call the native API present in Update Handler to do the actual installation
 * - Send the status of the installation periodically
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * @see UpdateHandler
 * @see FTPAdapter
 * @see InstallListInfo
 * @see SAMessage
 * 
 */
public class Installer implements Runnable {
	
	private UpdateService updateService;

	private UpdateHandler updateHandler;
	
	private StateManager stateManager;

	private HardwareInfo hardwareDetails;

	private LogParser logParser;

	private FTPAdapter ftpAdapter;

	private InstallListInfo installList;

	private boolean isRunning;

	private boolean isPointOfNoReturn;
	
	private String pathToLoadable;
	
	private String pathToCII;

	private boolean isRestart;

	private SAMessage sa;

	private int totalSteps;

	private int stepNumber;

	private String stepName;

	private List<String> filesDownloaded;

	private boolean isDownloadSuccess;
	
	private String nfsMountPath;
	
	private boolean isSecondaryInstalled;
	
	private boolean isBackupInstalled;
	
	private boolean isPrimaryPackage = false;

	private boolean mcdPatchOnly = false;	// mcd UpdatePatch only install for self load condition
	
	private boolean isUserTriggeredLoad = false;

	private static final String INSTALLER = "Update Service - Installer: ";
	
	/** The Constant PPC_STATUS_START. */
	private static final String PPC_STATUS_START = "START";
	
	/** The Constant PPC_FORCE_LOAD_STATUS. */
	private static final String PPC_FORCE_LOAD_STATUS = "ForceLoadStatus";

	/** The Constant LA_FILE. */
	public static final String LA_FILE = "la.txt";
	
	/** The Constant REBOOT_STEP. */
	public static final String REBOOT_STEP = "Waiting for synchronized reboot";
	
	/**
	 * Sets the update handler.
	 * 
	 * @param updateHandler the new update handler
	 */
	public void setUpdateHandler(final UpdateHandler updateHandler) {
		this.updateHandler = updateHandler;
	}

	/**
	 * Sets the state manager.
	 * 
	 * @param stateManager the new state manager
	 */
	public void setStateManager(StateManager stateManager) {
		this.stateManager = stateManager;
	}

	/**
	 * Sets the ftp adapter.
	 * 
	 * @param ftpAdapter the new ftp adapter
	 */
	public void setFtpAdapter(final FTPAdapter ftpAdapter) {
		this.ftpAdapter = ftpAdapter;
	}

	/**
	 * Sets the hardware details.
	 * 
	 * @param hardwareDetails the new hardware details
	 */
	public void setHardwareDetails(final HardwareInfo hardwareDetails) {
		this.hardwareDetails = hardwareDetails;
	}

	/**
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(final UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Sets the log parser.
	 * 
	 * @param logParser the new log parser
	 */
	public void setLogParser(final LogParser logParser) {
		this.logParser = logParser;
	}

	/**
	 * Sets the install list.
	 * 
	 * @param installList the new install list
	 */
	public void setInstallList(final InstallListInfo installList) {
		this.installList = installList;
	}
	
	/**
	 * Checks if is point of no return.
	 * 
	 * @return true, if is point of no return
	 */
	public boolean isPointOfNoReturn() {
		return isPointOfNoReturn;
	}

	/**
	 * Sets the point of no return.
	 * 
	 * @param isPointOfNoReturn the new point of no return
	 */
	private void setPointOfNoReturn(final boolean isPointOfNoReturn) {
		this.isPointOfNoReturn = isPointOfNoReturn;
	}

	/**
	 * Gets the path to CII.
	 * 
	 * @return the path to CII
	 */
	public String getPathToCII() {
		return pathToCII;
	}

	/**
	 * Sets the path to CII.
	 * 
	 * @param pathToCII the new path to CII
	 */
	public void setPathToCII(final String pathToCII) {
		this.pathToCII = pathToCII;
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
	 * Sets the path to loadable.
	 * 
	 * @param pathToLoadable the new path to loadable
	 */
	public void setPathToLoadable(final String pathToLoadable) {
		this.pathToLoadable = pathToLoadable;
	}

	/**
	 * Checks if restart is required for installation.
	 * 
	 * @return true, if restart is required
	 */
	public boolean isRestart() {
		return isRestart;
	}

	/**
	 * Sets whether restart is required or not.
	 * 
	 * @param isRestart the new restart
	 */
	public void setRestart(final boolean isRestart) {
		this.isRestart = isRestart;
	}

	/**
	 * Gets the nfs mount path.
	 * 
	 * @return the nfs mount path
	 */
	public String getNfsMountPath() {
		return nfsMountPath;
	}

	/**
	 * Sets the nfs mount path.
	 * 
	 * @param nfsMountPath the new nfs mount path
	 */
	public void setNfsMountPath(final String nfsMountPath) {
		this.nfsMountPath = nfsMountPath;
	}

	/**
	 * Checks if is user triggered load.
	 * 
	 * @return true, if is user triggered load
	 */
	public boolean isUserTriggeredLoad() {
		return isUserTriggeredLoad;
	}

	/**
	 * Sets the user triggered load.
	 * 
	 * @param isUserTriggeredLoad the new user triggered load
	 */
	public void setUserTriggeredLoad(boolean isUserTriggeredLoad) {
		this.isUserTriggeredLoad = isUserTriggeredLoad;
	}

	/**
	 * Stops the installer thread.
	 */
	public void stopInstaller() {
		isRunning = false;
	}

	/**
	 * The actual thread that runs until the installation process is over.
	 */
	public void run() {
		isRunning = true;
		kickOffInstaller();
	}

	/**
	 * Does the actual installation.
	 */
	private void kickOffInstaller() {
		// Possible steps for installation
		// Step 0: Initializing to install
		// Sub Step Name: Stopping UI
		// Sub Step Value: 100% Completed
		// Step 1: Downloading files to install
		// Repeat the following for all Parent CIs to install
		// Sub Step Name: Downloading the CII file: <CII file name>
		// Sub Step Value: % completed
		// Sub Step Name: Digital signature check on CII file
		// Sub Step Value: 100% completed
		// Sub Step Name: Downloading Parent 811 file: <Parent 811 file name>...
		// Sub Step Value: % completed
		// Sub Step Name: MD5 Check on Parent 811 file
		// Sub Step Value: 100% completed
		// Step 2: Generate the install list for secondary LRU
		// Sub Step Name: Generate the install list for secondary LRU
		// Sub Step Value: 100% completed
		// Step 3: Generate the install list for backup LRU
		// Sub Step Name: Generate the install list for backup LRU
		// Sub Step Value: 100% completed
		// Step 4: Installing <ParentCI lruType>
		// Sub Step Name: Generating install list
		// Sub Step Value: 100% completed
		// Sub Step Name: Calling Native Installer
		// Sub Step Value: 100% completed
		// Step 5: Installing LCP
		// Sub Step Name: Burning the files
		// Sub Step Value: 100% completed
		// Step 6: Rebooting

		isSecondaryInstalled = false;
		isBackupInstalled = false;
		mcdPatchOnly = false;	

		final HashMap<String, ParentCI> parentCIsToInstall = installList.getParentCIsToInstall();

		if (parentCIsToInstall == null || parentCIsToInstall.size() <= 0) {
			UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "An attempt has been made to install without anything in install list"));
			isRunning = false;
			return;
		}

		// Calculate the total steps required for this installation
		calculateTotalSteps(parentCIsToInstall);
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Total number of steps for installation: " + totalSteps);
		}
		
		// Initialize the SAMessage object to send status messages
		initializeSA();

		// Check if any of the primary packages (apart from LSP/LCP) needs to be installed
		final List<String> primaryPackagesList = updateService.getPrimaryPackagesList();
		for(final String primaryPackage : primaryPackagesList) {
			if(parentCIsToInstall.containsKey(primaryPackage)) {
				isPrimaryPackage = true;
				break;
			}
		}

		// Except MCD other LRUs are permitted to transfer files over network
		// MCD's LSP cannot be transferred over the network
		// But LCP will still be transferred over the network for MCD
		if(updateHandler.isCopyLoadFiles()) {
			// PSWs and HDAVs permitted to transfer files over network
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Must copy the load files, even if the files are available remotely");
			}
		} else {
			// MCD or other device
			// MCD will access remotely unless a local copy of LRU files is present
			
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "LSP load files must be present locally");
			}
			if(parentCIsToInstall.containsKey(hardwareDetails.getDeviceType())) {
				if(installList.getFtpIPAddress().equals(hardwareDetails.getIpAddress())) {
					// install ftp == local ip so files to be installed are local
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Files are available locally");
					}
				} else {
					boolean localInstallOk = false;
					
					// install ftp != local ip so files are on another lru
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Attempting to access local media for CII files");
					}
					
					// Get local CII files from local media if present by querying MAProcessor
					final HashMap<String, HashMap<String, ParentCI>> localCIIs  = updateService.getMaProcessor().getParentCIs();
					
					// Check for same files in local drive as those in LA message
					if (localCIIs != null && !localCIIs.isEmpty()) {
						final Iterator<Entry<String, HashMap<String, ParentCI>>> mediaPathsEntry = localCIIs.entrySet().iterator();
						final Entry<String, HashMap<String, ParentCI>> mediaPathEntry = mediaPathsEntry.next();
						final String mediaPath = mediaPathEntry.getKey();
						final HashMap<String, ParentCI> localCIs = mediaPathEntry.getValue();
						
						if(localCIs != null) {
							if(updateService.isInfo()) {
								updateService.logInfo(INSTALLER + "Found local media CII files at: " + mediaPath);
							}
							
							// Generate an install list from local media files if present
							// this will be: ParentCIs, ChildCIs and childCIs not to install
							// generate using same forceLoad flag setting as original installList
							final InstallListInfo localInstallList = updateService.getConformityChecker().getInstallList(localCIs, installList.isForceLoad());

							if(updateService.isInfo()) {
								updateService.logInfo(INSTALLER + "Conformity Checker get Install List complete");
							}

							// Compare local installList to that in original LA message 
							if (!localInstallList.getParentCIsToInstall().isEmpty()) {
								if(updateService.isInfo()) {
									updateService.logInfo(INSTALLER + "Begin Compare of local install list");
								}
								
								// Fetch and compare the ParentCI which matches our LRU type
								if (localInstallList.getParentCIsToInstall().containsKey(hardwareDetails.getDeviceType())) {
									ParentCI ourCI = localInstallList.getParentCIsToInstall().get(hardwareDetails.getDeviceType());
									if (parentCIsToInstall.get(hardwareDetails.getDeviceType()).compares(ourCI)) {
										
										if(updateService.isInfo()) {
											updateService.logInfo(INSTALLER + "Local CII file is same as the remote CII file");
										}
										// Reinitialize the download parameters to the local media
										installList.setPathToLoadable(mediaPath);
										installList.setFtpIPAddress(hardwareDetails.getIpAddress());
										installList.setFtpPassword(updateHandler.getFtpPassword());
										installList.setFtpPortNumber(updateHandler.getFtpPortNumber());
										installList.setFtpUserName(updateHandler.getFtpUsername());
										
										localInstallOk = true;
									}
								}
							}
						}
					}
					
					if (! localInstallOk) {
						final String deviceType = hardwareDetails.getDeviceType();
						
						if(! deviceType.equalsIgnoreCase(UpdateService.MCD)) {
							returnWithoutInstalling("Not MCD - Cannot load local files");
							return;
						}
						
						// MCD and remote files so allow parent only copy and install
						mcdPatchOnly = true;	// mcd UpdatePatch only install for self load condition
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Other load files may be available to load over network");
				}
			}
		}

		// Check whether restart is required for this installation
		if (parentCIsToInstall.containsKey(updateService.getLCPType()) || parentCIsToInstall.containsKey(hardwareDetails.getDeviceType()) || isPrimaryPackage) {
			updateService.setRebootReceived(false);
			setRestart(true);
			// Set the state manager to local mode before starting the installation
			// This avoids any other LRU going to the LOCAL mode if the current LRU
			// is taking more time to install
			if(stateManager != null) {
				if(stateManager.getMode() != StateManagerMode.LOCAL) {
					stateManager.setMode(StateManagerMode.LOCAL);
				}
			}
		} else {
			setRestart(false);
		}

		// Disable any watch dog that may reset the LRU before the download completes
		if(isRestart) {
			updateHandler.disableWatchDog();
		}

		// Initialize the FTPAdapter
		setFtpInfo();

		if (!isRunning) {
			abortInstallation("Installation aborted by Update Service");
			return;
		}

		setPathToCII(updateService.getLocalFtpPathForCii());
		setPathToLoadable(updateService.getLocalFtpPathForLoadable());
		
		// This makes sure all the required partitions are mounted correctly
		updateHandler.initializeToFTP();
		
		// Create a file with a list of the files to be downloaded 
		// For recovery in case of incomplete/interrupted download
		generateDownloadListFile(parentCIsToInstall, UpdateService.DOWNLOAD_LIST_FILE);
		
		// Generate the "to be installed" file if the load is triggered by the user
		// Delete the old "to be installed" file first and generate a new one
		if(isUserTriggeredLoad()) {
			delete(updateService.getPathToInstallLog(), UpdateService.TO_BE_INSTALLED_FILE);
			generateToBeInstalledFile(parentCIsToInstall, UpdateService.TO_BE_INSTALLED_FILE);
		}

		// Delete all the old log files
		delete(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_ABORT_LOG_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_ABORT_LOG_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.SECONDARY_INSTALL_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.BACKUP_INSTALL_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_LIST_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_LOG_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_LCP_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_LCP_LOG_FILE);
		
		// Delete serialized database file
		delete(stateManager.getStateDatabase().getSerDatabaseFile()); 
		
		updateHandler.flushFiles(updateService.getPathToInstallLog(), true);
		
		// Initialize to install
		initializeToInstall();

		if (!isRunning) {
			abortInstallation("Installation aborted by Update Service");
			return;
		}

		// Download step
		stepNumber = 1;
		filesDownloaded = new ArrayList<String>();
		
		downloadFiles(parentCIsToInstall, installList.isNfs());

		if (!isRunning) {
			abortInstallation("Installation aborted by Update Service");
			return;
		}

		if(isRestart) {
			updateHandler.disableWatchDog();
		}

		if (!isDownloadSuccess) {
			final List<CAMessage> prevailingVotes = installList.getPrevailingVotes();
			// In case of self load make an attempt to download from other LRUs
			// The loop starts with index 1 as index 0 has been tried already
			if (prevailingVotes != null && prevailingVotes.size() > 1) {
				for (int i = 1; i < prevailingVotes.size(); i++) {
					final CAMessage ca = prevailingVotes.get(i);
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Attempting to download from: " + ca.getFtpIpAddress());
					}
					ftpAdapter.setRemoteIP(ca.getFtpIpAddress());
					ftpAdapter.setRemotePort(String.valueOf(ca.getFtpPortNumber()));
					ftpAdapter.setUserName(ca.getFtpUsername());
					ftpAdapter.setPassword(ca.getFtpPassword());
					downloadFiles(parentCIsToInstall, installList.isNfs());

					if (!isRunning) {
						abortInstallation("Installation aborted by Update Service");
						return;
					}
					if (isDownloadSuccess) {
						break;
					}
				}
				if (!isDownloadSuccess) {
					abortInstallation("Installation aborted as downloading failed");
					UnitManager.Logging.logSevere(INSTALLER + "End of installer thread as the download failed");
					return;
				}
			} else {
				abortInstallation("Installation aborted as downloading failed");
				UnitManager.Logging.logSevere(INSTALLER + "End of installer thread as the download failed");
				return;
			}
		}

		// Unmount the remote path if the files are downloaded via NFS
		if(installList.isNfs()) {
			updateHandler.unmount(getNfsMountPath());
		}

		// Beyond this installer thread cannot be stopped
		setPointOfNoReturn(true);
		
		// Install Secondary LRUs
		final List<String> secondaryLruList = updateService.getSecondaryLruList();
		final boolean isSecondaryLruSuccess = installSecondaryLru(secondaryLruList, parentCIsToInstall);
		if (!isSecondaryLruSuccess) {
			return;
		}

		// Install Backup LRUs
		final List<String> backupLruList = updateService.getBackupLruList();
		final boolean isBackupLruSuccess = installBackupLru(backupLruList, parentCIsToInstall);
		if (!isBackupLruSuccess) {
			return;
		}

		if (!parentCIsToInstall.containsKey(updateService.getLCPType()) && !parentCIsToInstall.containsKey(hardwareDetails.getDeviceType()) && !isPrimaryPackage) {
			if(isSecondaryInstalled) {
				logParser.parseOtherInstallLog(UpdateService.SECONDARY_INSTALL_FILE, true);
			}
			if(isBackupInstalled) {
				logParser.parseOtherInstallLog(UpdateService.BACKUP_INSTALL_FILE, true);
			}
			// Delete 'download list' file now that all files are successfully fetched and all log/header files are fully generated
			// After this point if the unit is power cycled, the unit will still be recoverable without this download list file
			delete(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_LIST_FILE);
			
			// Delete the 'to be installed' file only if the load is triggered by the user
			if(isUserTriggeredLoad()) {
				// Delete the 'to be installed' file as all required download is complete
				delete(updateService.getPathToInstallLog(), UpdateService.TO_BE_INSTALLED_FILE);
			}
			
			updateHandler.flushFiles(true);
			updateService.otherLruInstalled();
			updateHandler.onDownloadComplete();
			isRunning = false;
			return;
		}

		// Install current LRU software
		if (parentCIsToInstall.containsKey(hardwareDetails.getDeviceType()) || isPrimaryPackage) {
			boolean isLruSuccess = installLruSoftware(parentCIsToInstall);
			if (!isLruSuccess) {
				return;
			} else if (!parentCIsToInstall.containsKey(updateService.getLCPType())) {
				if(isSecondaryInstalled) {
					logParser.parseOtherInstallLog(UpdateService.SECONDARY_INSTALL_FILE, true);
				}
				if(isBackupInstalled) {
					logParser.parseOtherInstallLog(UpdateService.BACKUP_INSTALL_FILE, true);
				}
				// Delete 'download list' file now that all files are successfully fetched and all log/header files are fully generated
				// After this point if the unit is power cycled, the unit will still be recoverable without this download list file
				delete(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_LIST_FILE);

				// Delete the 'to be installed' file only if the load is triggered by the user
				if(isUserTriggeredLoad()) {
					// Delete the 'to be installed' file as all required download is complete
					delete(updateService.getPathToInstallLog(), UpdateService.TO_BE_INSTALLED_FILE);
				}
				
				updateHandler.flushFiles(true);
				launchInstaller();
			}
		}

		// Install LCP
		if (parentCIsToInstall.containsKey(updateService.getLCPType())) {
			final ParentCI newParentCI = parentCIsToInstall.get(updateService.getLCPType());
			installLcp(newParentCI);

			if(isSecondaryInstalled) {
				logParser.parseOtherInstallLog(UpdateService.SECONDARY_INSTALL_FILE, true);
			}
			if(isBackupInstalled) {
				logParser.parseOtherInstallLog(UpdateService.BACKUP_INSTALL_FILE, true);
			}
			logParser.parseInstallLog(updateService.getLCPType(), true);
			updateHandler.flushFiles(true);
			if (!parentCIsToInstall.containsKey(hardwareDetails.getDeviceType()) && !isPrimaryPackage) {
				stepNumber = stepNumber + 1;
				stepName = "Installation complete";
				restartJava();
			} else {
				launchInstaller();
			}
		}
	}

	
	/**
	 * Calculates the total steps for installation.
	 * 
	 * @param parentCIsToInstall the parent CIs to install
	 */
	private void calculateTotalSteps(final HashMap<String, ParentCI> parentCIsToInstall) {
		// Calculate Total steps to install
		// One step is counted to download all the files
		// Two steps are counted to burn the LCP
		// For LSP, the total steps will be determined through the LRU specific Update Handler
		
		boolean isInstallLcp = false;
		boolean isInstallLru = false;
		boolean isInstallSecondary = false;
		boolean isInstallBackup = false;
		final StringBuilder installListString = new StringBuilder();
		final List<String> secondaryLruList = updateService.getSecondaryLruList();
		final List<String> backupLruList = updateService.getBackupLruList();
		
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Calculating the total steps to install");
		}

		totalSteps = 0;
		
		for (final Entry<String, ParentCI> parentCIEntry : parentCIsToInstall.entrySet()) {
			final String lruType = parentCIEntry.getKey();
			final ParentCI newParentCI = parentCIEntry.getValue();
			
			if(lruType.equalsIgnoreCase(updateService.getLCPType())) {
				isInstallLcp = true;
			} else if(lruType.equals(hardwareDetails.getDeviceType()) || updateService.getPrimaryPackagesList().contains(lruType)) {
				isInstallLru = true;
				if(!newParentCI.isPrimitive()) {
					final List<ChildCI> childCIsToInstall = installList.getChildCIsToInstall().get(lruType);
					if (childCIsToInstall != null) {
						int i = 0;
						for (final ChildCI childCI : childCIsToInstall) {
							i++;
							installListString.append(childCI.getChildCIType());
							installListString.append("=yes");
							installListString.append(",");
						}
					}

					final List<ChildCI> childCIsNotToInstall = installList.getChildCIsNotToInstall().get(lruType);
					if (childCIsNotToInstall != null) {
						for (final ChildCI childCI : childCIsNotToInstall) {
							installListString.append(childCI.getChildCIType());
							installListString.append("=no");
							installListString.append(",");
						}
					}
				} else {
					installListString.append(newParentCI.getLruType().toUpperCase());
					installListString.append("=yes");
					installListString.append(",");
				}
			} else if(secondaryLruList.contains(lruType)) {
				isInstallSecondary = true;
			} else if(backupLruList.contains(lruType)) {
				isInstallBackup = true;
			}
		}
		
		// If no backup LRU type is not available to install
		// Check for any backup LCP type that may have to be installed
		// The backup LCP type should be considered as a separate step
		// Only if it is different from the local LRU's LCP type
		if(!isInstallBackup) {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Checking for any backup LCP type is available to install");
			}
			for (final String backupLru : backupLruList) {
				String backupLCPType = updateService.getLruToLCPTypes().get(backupLru);
				if(backupLCPType == null) {
					backupLCPType = UpdateService.LCP_TYPE;
				}
				if(!backupLCPType.equals(updateService.getLCPType())) {
					if (parentCIsToInstall.containsKey(backupLCPType)) {
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + "Need to install LCP type: " + backupLCPType + " for backup LRU: " + backupLru);
						}
						isInstallBackup = true;
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + "No need to install LCP type: " + backupLCPType + " for backup LRU: " + backupLru);
						}
					}
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "LCP type: " + backupLCPType + " is same as local LCP type - Backup LRU: " + backupLru);
					}
				}
			}
		}
		
		int installerSteps = 0;
		if(installListString.length() > 0) {
			if(installListString.toString().endsWith(",")) {
				installListString.deleteCharAt(installListString.length() - 1);
			}
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Installlist String: " + installListString.toString());
			}
			installerSteps = updateHandler.getInstallerSteps(installListString.toString());
			if (installerSteps == -1) {
				UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Aborting Installation. Failed to obtain installer steps for the install list: " + installListString.toString()));
				return;
			} else if (installerSteps == 0) {
				if (parentCIsToInstall.size() > 1) {
					UnitManager.Logging.logSevere(INSTALLER + "Installer steps for : " + installListString.toString() + " is 0");
				} else {
					UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Aborting Installation. Installer steps for : " + installListString.toString() + " is 0. There are no other Parent CIs to install."));
					return;
				}
			}
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Native Installer Steps: " + installerSteps);
			}
		}
		
		// One step to download all CII and Parent 811 files
		// This step includes downloading the files for all 
		// Secondary and Backup LRUs as well
		totalSteps = totalSteps + 1;
		
		// One step to generate header files for Secondary LRUs
		if(isInstallSecondary) {
			totalSteps = totalSteps + 1;
		}
		
		// One step to generate header files for Backup LRUs
		if(isInstallBackup) {
			totalSteps = totalSteps + 1;
		}
		
		// Install only LCP
		if(isInstallLcp && !isInstallLru) {
			// One step to install the actual Parent CI of LCP
			// One step to restart the Java environment
			totalSteps = totalSteps + 2;
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Total Steps: Only LCP: " + totalSteps);
			}
		}
		
		// Only LRU or Primary Package
		if(!isInstallLcp && isInstallLru) {
			// One step to generate the install list and launching native installer
			// Include steps of native installer
			totalSteps = totalSteps + 1;
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Total Steps: Only LRU: " + totalSteps);
			}
			totalSteps = totalSteps + installerSteps;
		}
		
		// LCP + (LRU or Primary Package)
		if(isInstallLcp && isInstallLru) {
			// One step to install the actual Parent CI of LCP
			// One step to generate the install list and launching native installer
			// Include steps of native installer
			totalSteps = totalSteps + 2;
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Total Steps: LCP + LRU: " + totalSteps);
			}
			totalSteps = totalSteps + installerSteps;
		}

		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Total Steps including native installer: " + totalSteps);
		}
	}

	/**
	 * Initializes for sending Status Announcement.
	 */
	private void initializeSA() {
		// Initialize the SA Message with message header
		sa = new SAMessage();
		sa.setUpdateService(updateService);
		sa.setIpAddress(hardwareDetails.getIpAddress());
		sa.setLruType(hardwareDetails.getDeviceType());
		sa.setLruInstance(updateService.getLruInstance());
		sa.setTotalSteps(totalSteps);
	}
	
	/**
	 * Return without installing.
	 * 
	 * @param subStepName the sub step name
	 */
	private void returnWithoutInstalling(final String subStepName) {
		UnitManager.Logging.logWarning(INSTALLER + subStepName);
		
		// Send the final status more than once
		stepNumber = totalSteps;
		stepName = "Nothing to install";
		sendFinalStatus(subStepName, "100%", true, false);
		updateService.setInstalling(false);
		isRunning = false;
	}

	/**
	 * Sets the ftp info.
	 */
	private void setFtpInfo() {
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "FTP IP Address: " + installList.getFtpIPAddress());
		}

		ftpAdapter.setRemoteIP(installList.getFtpIPAddress());

		ftpAdapter.setRemotePort(String.valueOf(installList.getFtpPortNumber()));

		ftpAdapter.setUserName(installList.getFtpUserName());

		ftpAdapter.setPassword(installList.getFtpPassword());
	}
	
	/**
	 * Initializes for installation process.
	 */
	private void initializeToInstall() {
		stepNumber = 0;

		stepName = "Initializing to install";
		final String subStepName = "Stopping services";

		sendSA(subStepName, "0%", true, true);

		updateService.stopServices(isRestart());

		sendSA(subStepName, "100%", true, true);

		if (isRestart()) {
			updateHandler.updateProgressBar(1, 0, 0);
		}
	}

	/**
	 * Gets the absolute file path with platform specific file separator.
	 * 
	 * @param directoryPath the directory path
	 * @param fileName the file name
	 * 
	 * @return the file path
	 */
	private String getFilePath(final String directoryPath, final String fileName) {
		String filePath = null;
		if (directoryPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {
			filePath = directoryPath + fileName;
		} else {
			filePath = directoryPath + Consts.IOs.FILE_SEPARATOR + fileName;
		}
		return filePath;
	}

	/**
	 * Deletes a given file.
	 * 
	 * @param file the file
	 * 
	 * @return true, if deletion is successful
	 */
	private boolean delete(final File file) {
		boolean returnValue = false;
		if (file.exists()) {
			 returnValue = file.delete();
			if (returnValue) {
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Deleted the file: " + file.getAbsolutePath());
				}
			} else {
				UnitManager.Logging.logSevere(INSTALLER + "Failed to delete the file: " + file.getAbsolutePath());
			}
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Deleting the file: " + file.getAbsolutePath() + " failed as the file do not exist");
			}
		}
		updateHandler.flushFiles(file.getAbsolutePath(), true);
		return returnValue;
	}

	/**
	 * Deletes a given file in the given directory.
	 * 
	 * @param directoryPath the directory path
	 * @param fileName the file name
	 */
	private boolean delete(final String directoryPath, final String fileName) {
		final String filePath = getFilePath(directoryPath, fileName);
		final File file = new File(filePath);
		return delete(file);
	}

	/**
	 * Sends the Status Announcement.
	 * 
	 * @param subStepName the sub step name
	 * @param subStepPercentage the sub step percentage
	 * @param isStatusAvailable the is status available
	 * @param isRunning the is running
	 */
	private void sendSA(final String subStepName, final String subStepPercentage, final boolean isStatusAvailable, final boolean isRunning) {
		sa.setStepNumber(stepNumber);
		sa.setStepName(stepName);
		sa.setSubStepName(subStepName);
		sa.setSubStepPercentage(subStepPercentage);
		sa.setStatusAvailable(isStatusAvailable);
		sa.setRunning(isRunning);
		updateService.sendStatus(sa);

		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "" + subStepName + ": " + subStepPercentage);
		}
		if (stepNumber > 0 && isStatusAvailable) {
			String percentageValue = null;
			if (subStepPercentage.indexOf('%') > 0) {
				percentageValue = subStepPercentage.substring(0, subStepPercentage.indexOf('%'));
			} else {
				percentageValue = subStepPercentage;
			}
			if (percentageValue != null) {
				if (percentageValue.length() > 0 && percentageValue.length() < 4) {
					updateProgressBar(Integer.parseInt(percentageValue));
				} else {
					updateProgressBar(0);
				}
			}
		}
	}

	/**
	 * Updates progress bar if a GUI is available.
	 * 
	 * @param subStepPercentage the sub step percentage
	 */
	private void updateProgressBar(final int subStepPercentage) {
		if (isRestart()) {
			final int systemWidePercentage = stepNumber * 100 / totalSteps;

			if(updateService.isInfo()) {
				final StringBuilder sb = new StringBuilder();
				sb.append("Updating the progress bar: ");
				sb.append("Step: ");
				sb.append(stepNumber);
				sb.append(" Total: ");
				sb.append(totalSteps);
				sb.append(" System%: ");
				sb.append(systemWidePercentage);
				sb.append(" SubStep%: ");
				sb.append(subStepPercentage);
				updateService.logInfo(INSTALLER + sb.toString());
			}
			boolean returnValue = updateHandler.updateProgressBar(0, systemWidePercentage, subStepPercentage);
			if (!returnValue) {
				UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Failed to update the progress bar"));
			}
		}
	}

	/**
	 * Downloads the CII and Loadable files and does a MD5 checksum.
	 * 
	 * @param parentCIsToInstall the parent CIs to install
	 */
	private void downloadFiles(final HashMap<String, ParentCI> parentCIsToInstall, final boolean isNfs) {
		// For every Parent CI to install
		// Download CII File
		// Run digital signature check on CII File
		// If signature check fails
		// Try downloading two more times
		// If signature check still fails, abort
		// Download Parent 811 File
		// Run MD5 checksum on Parent 811 File
		// If MD5 checksum fails
		// Try downloading two more times
		// If MD5 checksum still fails, abort
		
		int retry = 0;
		String subStepName = null;
		
		if(isNfs) {
			if(installList.getFtpIPAddress().equals(hardwareDetails.getIpAddress())) {
				// Do not do NFS mount if the media is available locally
				// Rather just do the copy directly
				setNfsMountPath(installList.getPathToCII());
				if(updateService.isInfo()) {
					UnitManager.Logging.logInfo(INSTALLER + "Media available locally: " + installList.getPathToCII());
					UnitManager.Logging.logInfo(INSTALLER + "No need to do NFS mount");
				}
				stepName = "Downloading files to install directly from: " + installList.getFtpIPAddress();
			} else {
				// Media is not available locally
				// Do the NFS mount
				// The max number of attempted nfs mounts = nbrOfSleeps + 1
				// nfs mount give up time = nbrOfSleeps x nfsMountRetryDelaymS
				int nfsMountAttempts = 0;
				final int nbrOfSleeps = 9;
				final long nfsMountRetryDelaymS = 10000;

				setNfsMountPath(updateHandler.nfsMountPath());
				if(updateService.isInfo()) {
					UnitManager.Logging.logInfo(INSTALLER + "Media available remotely - Must do NFS mount");
					UnitManager.Logging.logInfo(INSTALLER + "IP: " + installList.getFtpIPAddress());
					UnitManager.Logging.logInfo(INSTALLER + "Remote path: " + installList.getPathToCII());
					UnitManager.Logging.logInfo(INSTALLER + "Local path: " + getNfsMountPath());
				}
				while (!updateHandler.mountViaNfs(installList.getFtpIPAddress(), installList.getPathToCII(), getNfsMountPath())) {
					// nfs mount failed.
					if (++nfsMountAttempts > nbrOfSleeps)
					{
						// nfs mount failed after retries, return.
						abortInstallation("Cannot download as NFS mount failed: IP: " + installList.getFtpIPAddress() + ": Remote Path: " + installList.getPathToCII());
						return;
					}
					UnitManager.Logging.logSevere(INSTALLER + "NFS mount failed, sleep then retry");
					try {
						Thread.sleep(nfsMountRetryDelaymS);
					} catch (InterruptedException ie) {
						UnitManager.Logging.logSevere(INSTALLER + "Interrupted while sleeping between NFS mount retries" + ie);					
				    }
				}
				// nfs mount succeeded, continue.
				if(updateService.isInfo()) {
					UnitManager.Logging.logInfo(INSTALLER + "NFS mount successful");
				}
				
				stepName = "Downloading files to install via NFS from: " + installList.getFtpIPAddress();
			}
		} else {
			// TST or self load
			stepName = "Downloading files to install via FTP from: " + installList.getFtpIPAddress();
		}
		
		for (final Entry<String, ParentCI> parentCIEntry : parentCIsToInstall.entrySet()) {
			final ParentCI newParentCI = parentCIEntry.getValue();

			subStepName = "Downloading CII File: " + newParentCI.getCiiFileName();

			sendSA(subStepName, "0%", true, true);

			UnitManager.Logging.logWarning(INSTALLER + subStepName);
			
			boolean ciiFileDownloadCheck = false;
			retry = 0;
			while (retry < 2 && !ciiFileDownloadCheck) {
				if(retry == 0) {
					subStepName = "Downloading CII File: " + newParentCI.getCiiFileName();
				}
				else {
					subStepName = "Downloading CII File: " + newParentCI.getCiiFileName() + " Attempting retry: " + retry;					
				}

				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + subStepName);
				}
				sendSA(subStepName, "0%", true, true);
				
				retry++;
				ciiFileDownloadCheck = downloadCiiFile(newParentCI, subStepName, isNfs);
				if (ciiFileDownloadCheck)
					filesDownloaded.add(getFilePath(updateService.getLocalFtpPathForCii(), 
							newParentCI.getCiiFileName()));
	
				if (!isRunning) {
					// Final Step
					abortInstallation("Installation aborted by Update Service");
					return;
				}
				try {
					Thread.sleep(15000L);
				} catch (InterruptedException ie) {
					UnitManager.Logging.logSevere(ie);					
				}
			}

			if (ciiFileDownloadCheck) {
				subStepName = "Checking digital signature of CII File: " + newParentCI.getCiiFileName();
				sendSA(subStepName, "0%", true, true);

				boolean ciiSignatureCheck = false;
				try {
					ciiSignatureCheck = ciiFileSignatureCheck(newParentCI, subStepName);
				} catch (final UpdateServiceException use) {
					UnitManager.Logging.logSevere(use);
				}

				if (!isRunning) {
					// Final Step

					abortInstallation("Installation aborted by Update Service");
					return;
				}

				if (!ciiSignatureCheck) {
					UnitManager.Logging.logSevere(INSTALLER + "Digital signature of CII failed: " + newParentCI.getCiiFileName());
					retry = 0;
					while (retry < 2 && !ciiSignatureCheck) {
						retry++;
						subStepName = "Downloading CII File: " + newParentCI.getCiiFileName() 
							+ " again as digital signature failed. Attempting retry: " + retry;
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + subStepName);
						}

						sendSA(subStepName, "0%", true, true);

						ciiFileDownloadCheck = downloadCiiFile(newParentCI, subStepName, isNfs);
						if (ciiFileDownloadCheck) {
							subStepName = "Checking digital signature of CII File: " + newParentCI.getCiiFileName() 
								+ " retry: " + retry;
							sendSA(subStepName, "0%", true, true);

							try {
								ciiSignatureCheck = ciiFileSignatureCheck(newParentCI, subStepName);
							} catch (final UpdateServiceException use) {
								UnitManager.Logging.logSevere(use);
							}
						}
						if (!isRunning) {
							// Final Step

							abortInstallation("Installation aborted by Update Service");
							return;
						}
					}
				}

				if (ciiSignatureCheck) {
					subStepName = "Downloading Parent 811 File: " + newParentCI.getParent811FileName();
					sendSA(subStepName, "0%", true, true);

					UnitManager.Logging.logWarning(INSTALLER + subStepName);
					
					boolean parent811FileDownloadCheck = false;
					retry = 0;
					while (retry < 2 && !parent811FileDownloadCheck) {
						if(retry == 0) {
							subStepName = "Downloading parent 811 File: " + newParentCI.getParent811FileName();
						}
						else {
						subStepName = "Downloading parent 811 File: " + newParentCI.getParent811FileName() 
							+ " Attempting retry: " + retry;
						}
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + subStepName);
						}
						sendSA(subStepName, "0%", true, true);
						
						retry++;
						parent811FileDownloadCheck = downloadParent811File(newParentCI, subStepName, isNfs);
						
						if (parent811FileDownloadCheck) {
							filesDownloaded.add(getFilePath(updateService.getLocalFtpPathForLoadable(), 
									newParentCI.getParent811FileName()));
						}
						if (!isRunning) {
							// Final Step
							abortInstallation("Installation aborted by Update Service");
							return;
						}
						try {
							Thread.sleep(15000L);
						} catch (InterruptedException ie) {
							UnitManager.Logging.logSevere(ie);					
						}
					}

					if (parent811FileDownloadCheck) {
						subStepName = "Running checksum on Parent 811 File: " + newParentCI.getParent811FileName();
						sendSA(subStepName, "0%", true, true);

						boolean parent811Md5Check = parent811FileMd5Check(newParentCI, subStepName);

						if (!isRunning) {
							// Final Step

							abortInstallation("Installation aborted by Update Service");
							return;
						}

						if (!parent811Md5Check) {
							UnitManager.Logging.logSevere(INSTALLER + "Checksum of Parent 811 failed: " + newParentCI.getParent811FileName());
							retry = 0;
							while (retry < 2 && !parent811Md5Check) {
								retry++;
								subStepName = "Downloading Parent 811 File: " + newParentCI.getParent811FileName() + " again as checksum failed. Attempting retry: " + retry;

								if(updateService.isInfo()) {
									updateService.logInfo(INSTALLER + subStepName);
								}

								sendSA(subStepName, "0%", true, true);

								parent811FileDownloadCheck = downloadParent811File(newParentCI, subStepName, isNfs);
								if (parent811FileDownloadCheck) {
									subStepName = "Running checksum on Parent 811 File: " + newParentCI.getParent811FileName() + " retry: " + retry;
									sendSA(subStepName, "0%", true, true);

									parent811Md5Check = parent811FileMd5Check(newParentCI, subStepName);
								}
								if (!isRunning) {
									// Final Step

									abortInstallation("Installation aborted by Update Service");
									return;
								}
							}
						}
						if (parent811Md5Check) {
							UnitManager.Logging.logWarning(INSTALLER + "Downloaded CII and Parent 811 File successfully for: " + newParentCI.getLruType());
							isDownloadSuccess = true;
						} else {
							// Final Step

							isDownloadSuccess = false;
							deleteDownloadedFiles(filesDownloaded);
							UnitManager.Logging.logSevere(INSTALLER + "parent 811 file MD5 check failed");
							return;
						}
					} else {
						// Final Step

						isDownloadSuccess = false;
						deleteDownloadedFiles(filesDownloaded);
						UnitManager.Logging.logSevere(INSTALLER + "parent 811 file download failed");
						return;
					}
				} else {
					// Final Step

					isDownloadSuccess = false;
					deleteDownloadedFiles(filesDownloaded);
					UnitManager.Logging.logSevere(INSTALLER + "CII file digital signature failed");
					return;
				}
			} else {
				// Final Step

				isDownloadSuccess = false;
				deleteDownloadedFiles(filesDownloaded);
				UnitManager.Logging.logSevere(INSTALLER + "CII file download failed");
				return;
			}
		}
	}

	/**
	 * Deletes the downloaded files if download or MD5 checksum fails.
	 * Also deletes the download list file.
	 * 
	 * @param filesDownloaded the files downloaded
	 */
	private void deleteDownloadedFiles(final List<String> filesDownloaded) {
		if (filesDownloaded != null) {
			for (final String filePath : filesDownloaded) {
				final File file = new File(filePath);
				// Delete the downloaded file
				delete(file);
			}
		}
		// Delete the 'download list' file only
		// Do not delete the 'to be installed' file as the installation is aborted
		delete(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_LIST_FILE);
		updateHandler.flushFiles(true);
	}
	
	/**
	 * Aborts the installation process in the following procedure:
	 * - Generate the abort log
	 * - Delete all the newly downloaded files
	 * - Delete all the log/header files generated newly
	 * - Send out a Status Announcement
	 * 
	 * @param subStepName the sub step name
	 */
	private void abortInstallation(final String subStepName) {
		UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + subStepName));
		generateAbortLog(subStepName);
		deleteDownloadedFiles(filesDownloaded);
		if(installList.isNfs()) {
			updateHandler.unmount(getNfsMountPath());
		}
		// Delete all the possible log/header files that 
		// might have been created after downloading
		delete(updateService.getPathToInstallLog(), UpdateService.SECONDARY_INSTALL_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.BACKUP_INSTALL_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_LIST_FILE);
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_LCP_FILE);
		updateHandler.flushFiles(updateService.getPathToInstallLog(), true);
		isRunning = false;
		
		// Send the final status more than once
		stepNumber = totalSteps;
		stepName = "Installation Failed";
		sendFinalStatus(subStepName, "100%", true, false);

		updateService.abortInstallation();

		if (isRestart()) {
			restartJava();
		}
	}

	private void generateAbortLog(final String abortReason) {
		try {
			final String installLogFileName = UpdateService.INSTALL_ABORT_LOG_FILE;
			final String pathToInstallLog = updateService.getPathToInstallLog();
			final String installLogFilePath = getFilePath(pathToInstallLog, installLogFileName);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Generating install abort log file: " + installLogFilePath);
			}

			final File installLogFile = new File(installLogFilePath);
			final FileOutputStream fos = new FileOutputStream(installLogFile);
			final PrintStream ps = new PrintStream(fos);

			final StringBuilder sb = new StringBuilder();
			final HashMap<String, ParentCI> parentCIsToInstall = installList.getParentCIsToInstall();
			final Iterator<Entry<String, ParentCI>> lruTypesEntry = parentCIsToInstall.entrySet().iterator();
			while(lruTypesEntry.hasNext()) {
				final Entry<String, ParentCI> lruTypeEntry = lruTypesEntry.next();
				final ParentCI newParentCI = lruTypeEntry.getValue();
				// Line 1: LRU type
				sb.append("LRU = ");
				sb.append(newParentCI.getLruType());
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + installLogFilePath + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());

				// Line 2 Update Time
				final Calendar c = Calendar.getInstance();
				final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
				sdf.format(c.getTime());
				sb.append("UpdateTime = ");
				sb.append(sdf.format(c.getTime()));
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + installLogFilePath + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 3: Total Steps
				sb.append("TotalSteps = ");
				sb.append(totalSteps);
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + installLogFilePath + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 4: Update Patch - Path to the Parent 811 file
				sb.append("UpdatePatch = ");
				if (getPathToLoadable().endsWith(Consts.IOs.FILE_SEPARATOR)) {
					sb.append(getPathToLoadable());
				} else {
					sb.append(getPathToLoadable());
					sb.append(Consts.IOs.FILE_SEPARATOR);
				}
				sb.append(newParentCI.getParent811FileName());
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + installLogFilePath + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());

				// Line 5: Update CII - Path to the CII file
				sb.append("UpdateCII = ");
				if (getPathToCII().endsWith(Consts.IOs.FILE_SEPARATOR)) {
					sb.append(getPathToCII());
				} else {
					sb.append(getPathToCII());
					sb.append(Consts.IOs.FILE_SEPARATOR);
				}
				sb.append(newParentCI.getCiiFileName());
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + installLogFilePath + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 6: Abort Reason
				sb.append("AbortReason = ");
				sb.append(abortReason);
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + installLogFilePath + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 7: Overall Install
				sb.append("OverallInstall = fail");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + installLogFilePath + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
			}
			
			ps.flush();
			ps.close();
		} catch (final FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere(INSTALLER + "install.log file missing", fnfe);
		}
		updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
	}

	/**
	 * Downloads the CII file.
	 * 
	 * @param newParentCI the new parent CI
	 * @param subStepName the sub step name
	 * 
	 * @return true, if successful
	 */
	private boolean downloadCiiFile(final ParentCI newParentCI, final String subStepName, final boolean isNfs) {
		boolean returnValue = false;
		if(isNfs) {
			sendSA(subStepName, "0%", true, true);
			returnValue = updateHandler.copy(getNfsMountPath(), getPathToCII(), newParentCI.getCiiFileName());
			sendSA(subStepName, "100%", true, true);
			return returnValue;
		} else {
			ftpAdapter.setRemoteDir(installList.getPathToCII());
			ftpAdapter.setLocalDir(getPathToCII());
			ftpAdapter.setRemoteFile(newParentCI.getCiiFileName());
			ftpAdapter.setLocalFile(newParentCI.getCiiFileName());
			returnValue = startFtp("get", subStepName);
			return returnValue;
		}
	}

	/**
	 * Does the digital signature check on the CII file.
	 * 
	 * @param newParentCI the new parent CI
	 * @param subStepName the sub step name
	 * 
	 * @return true, if successful
	 */
	private boolean ciiFileSignatureCheck(final ParentCI newParentCI, String subStepName) throws UpdateServiceException {
		boolean returnValue = false;
		try {
			String filePath = null;
			if (getPathToCII().endsWith(Consts.IOs.FILE_SEPARATOR)) {
				filePath = getPathToCII() + newParentCI.getCiiFileName();
			} else {
				filePath = getPathToCII() + Consts.IOs.FILE_SEPARATOR + newParentCI.getCiiFileName();
			}
			
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Retrieving Signed XML File At: " + filePath);
			}

			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			final Document doc = dbf.newDocumentBuilder().parse(new FileInputStream(filePath));

			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Searching for <Signature> Element...");
			}

			final NodeList nodeList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
			if (nodeList.getLength() == 0) {
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "CII file digital signature info is missing. So, it's assumed that, this version of file is old.");
				}
				return true;
			}

			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Verifying Signature...");
			}

			final String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
			final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());
			final DOMValidateContext valContext = new DOMValidateContext(new KeyValueKeySelector(), nodeList.item(0));

			final XMLSignature signature = fac.unmarshalXMLSignature(valContext);
			returnValue = signature.validate(valContext);

			if (returnValue == false) {
				subStepName = "CII file digital signature failed";
				UnitManager.Logging.logSevere(INSTALLER + subStepName);
			} else {
				subStepName = "CII file digital signature pass";
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + subStepName);
				}
			}
			sendSA(subStepName, "100%", true, true);
		} catch (final javax.xml.crypto.MarshalException e) {
			UnitManager.Logging.logSevere(e);
		} catch (final SAXException e) {
			UnitManager.Logging.logSevere(e);
		} catch (final IOException e) {
			UnitManager.Logging.logSevere(e);
		} catch (final ParserConfigurationException e) {
			UnitManager.Logging.logSevere(e);
		} catch (final IllegalAccessException e) {
			UnitManager.Logging.logSevere(e);
		} catch (final InstantiationException e) {
			UnitManager.Logging.logSevere(e);
		} catch (final ClassNotFoundException e) {
			UnitManager.Logging.logSevere(e);
		} catch (final XMLSignatureException e) {
			UnitManager.Logging.logSevere(e);
		}
		return returnValue;
	}
	
	/**
	 * Downloads the parent 811 file.
	 * 
	 * @param newParentCI the new parent CI
	 * @param subStepName the sub step name
	 * 
	 * @return true, if successful
	 */
	private boolean downloadParent811File(final ParentCI newParentCI, final String subStepName, final boolean isNfs) {
		boolean returnValue = false;
		if(isNfs) {
			sendSA(subStepName, "0%", true, true);
			returnValue = updateHandler.copy(getNfsMountPath(), getPathToLoadable(), newParentCI.getParent811FileName());
			sendSA(subStepName, "100%", true, true);
			return returnValue;
		} else {
			ftpAdapter.setRemoteDir(installList.getPathToLoadable());
			ftpAdapter.setLocalDir(getPathToLoadable());
			ftpAdapter.setRemoteFile(newParentCI.getParent811FileName());
			ftpAdapter.setLocalFile(newParentCI.getParent811FileName());
			returnValue = startFtp("get", subStepName);
			return returnValue;
		}
	}

	/**
	 * Does the MD5 checksum on parent 811 file.
	 * 
	 * @param newParentCI the new parent CI
	 * @param subStepName the sub step name
	 * 
	 * @return true, if successful
	 */
	private boolean parent811FileMd5Check(final ParentCI newParentCI, String subStepName) {
		boolean returnValue = false;

		String filePath = null;
		if (getPathToLoadable().endsWith(Consts.IOs.FILE_SEPARATOR)) {
			filePath = getPathToLoadable() + newParentCI.getParent811FileName();
		} else {
			filePath = getPathToLoadable() + Consts.IOs.FILE_SEPARATOR + newParentCI.getParent811FileName();
		}

		final String md5Sum = updateHandler.getMd5Sum(filePath);

		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Parent 811 File checksum: " + newParentCI.getMd5Value() + " : length: " + newParentCI.getMd5Value().length());
		}

		if (md5Sum.equalsIgnoreCase(newParentCI.getMd5Value())) {
			subStepName = "Parent 811 File checksum matches";
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + subStepName);
			}
			returnValue = true;
		} else {
			subStepName = "Parent 811 File checksum do not match";
			UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + subStepName +
					" - expected: " + newParentCI.getMd5Value() +
					" got: " + md5Sum));
			returnValue = false;
		}

		sendSA(subStepName, "100%", true, true);

		return returnValue;
	}

	/**
	 * Starts the FTP process and obtains the status of FTP periodically.
	 * 
	 * @param command the command
	 * @param subStepName the sub step name
	 * 
	 * @return true, if successful
	 */
	private boolean startFtp(final String command, String subStepName) {
		final FTP ftp = new FTP();
		final Thread ftpGetThread = new Thread(ftp);
		ftpGetThread.setName("Update Service - FTP GET");
		ftp.setUpdateService(updateService);
		ftp.setFTPAdapter(ftpAdapter);
		ftp.setCommand(command);
		ftp.setResult("starting");
		ftpGetThread.start();

		String result = null;
		boolean isStatusAvailable = false;
		String subStepPercentage = null;
		boolean isRunning = false;

		result = ftp.getResult();

		while (result.equalsIgnoreCase("starting")) {
			result = ftp.getResult();

			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Download result: " + result);
			}

			if (result.equalsIgnoreCase("starting")) {
				try {
					Thread.sleep(updateService.getFtpMessageInterval());
				} catch (final InterruptedException ie) {
					UnitManager.Logging.logSevere(INSTALLER + "Thread interrupted while sleeping", ie);
				}

				subStepPercentage = ftpAdapter.getStatus();
				if (subStepPercentage != null) {
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Download Status: " + subStepPercentage);
					}
					isStatusAvailable = true;
				} else {
					UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Unable to get download status. Status is null"));
					isStatusAvailable = false;
				}
				sendSA(subStepName, subStepPercentage, isStatusAvailable, true);
			}
		}

		subStepPercentage = ftpAdapter.getStatus();
		sa.setSubStepPercentage(subStepPercentage);
		if (subStepPercentage != null) {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Download Status: " + subStepPercentage);
			}
			isStatusAvailable = true;
		} else {
			UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Unable to get download status. Status is null"));
			isStatusAvailable = false;
		}

		if (result.equalsIgnoreCase("success")) {
			isRunning = true;
			isStatusAvailable = true;
			subStepPercentage = "100%";
		} else {
			UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Download failed"));
			subStepName = "Download failed";
			isRunning = false;
		}
		updateHandler.flushFiles(ftpAdapter.getLocalDir(), false);
		sendSA(subStepName, subStepPercentage, isStatusAvailable, isRunning);
		return isRunning;
	}

	/**
	 * Installs the LCP in the following procedure:
	 * - Generate the installlcp.txt header file
	 * - Burn the actual LCP with the help of Update Handler
	 * - Generate the new installlcp.log file
	 * - Delete the installlcp.txt header file
	 * 
	 * @param newParentCI the new parent CI
	 * 
	 * @return true, if successful
	 */
	private boolean installLcp(final ParentCI newParentCI) {
		boolean isLcpSuccess = false;
		stepNumber = stepNumber + 1;
		stepName = "Installing LCP";
		
		// Generate the installlcp.txt header file
		String subStepName = "Generating the header file for: " + newParentCI.getLruType();
		sendSA(subStepName, "0%", true, true);
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + subStepName);
		}
		
		generateInstallLCPFile(newParentCI);
		
		sendSA(subStepName, "100%", true, true);
		
		// Delete 'download list' file now that all files are successfully fetched and all log/header files are fully generated
		// After this point if the unit is power cycled, the unit will still be recoverable without this download list file
		delete(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_LIST_FILE);
		updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
		
		// Burn the actual LCP with the help of Update Handler
		subStepName = "Burning the files";
		sendSA(subStepName, "0%", true, true);
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + subStepName);
		}

		final String filePath = getFilePath(updateService.getLocalFtpPathForLoadable(), newParentCI.getParent811FileName());
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Calling Update Handler to install LCP from: " + filePath);
		}

		final boolean isInstallLcp = updateHandler.installLcp(filePath);
		if (isInstallLcp) {
			isLcpSuccess = true;
		} else {
			isLcpSuccess = false;
		}

		// Generate the new installlcp.log file
		subStepName = "Generating log file";
		sendSA(subStepName, "100%", true, true);
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + subStepName);
		}
		generateInstallLCPLog(newParentCI, isLcpSuccess);
		
		// Delete the installlcp.txt header file
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Deleting the header file");
		}
		delete(updateService.getPathToInstallLog(), UpdateService.INSTALL_LCP_FILE);

		return isLcpSuccess;
	}
	
	/**
	 * Generate header file for installing LCP in case of power 
	 * failure scenarios. This header file will be checked for its 
	 * availability by the native API during the LRU startup.
	 * 
	 * @param newParentCI the new parent CI
	 */
	private void generateInstallLCPFile(final ParentCI newParentCI) {
		try {
			final String installLCPFilePath = getFilePath(updateService.getPathToInstallLog(), UpdateService.INSTALL_LCP_FILE);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Generating install LCP file: " + installLCPFilePath);
			}

			final File installLCPFile = new File(installLCPFilePath);

			final FileOutputStream fos = new FileOutputStream(installLCPFile);

			final PrintStream ps = new PrintStream(fos);

			final StringBuilder sb = new StringBuilder();
			
			// Line 1: Total Number of Steps including things done already
			// by java - Used to generate the current step number
			sb.append("TotalSteps = ");
			sb.append(totalSteps);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 2: Update Patch - Path to the Parent 811 TGZ
			sb.append("UpdatePatch = ");
			if (getPathToLoadable().endsWith(Consts.IOs.FILE_SEPARATOR)) {
				sb.append(getPathToLoadable());
			} else {
				sb.append(getPathToLoadable());
				sb.append(Consts.IOs.FILE_SEPARATOR);
			}
			sb.append(newParentCI.getParent811FileName());
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 3: MD5 checksum of Parent 811 TGZ
			sb.append("MD5SUM = ");
			sb.append(newParentCI.getMd5Value());
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());
			
			// Line 4: Update CII - Path to the CII
			sb.append("UpdateCII = ");
			if (getPathToCII().endsWith(Consts.IOs.FILE_SEPARATOR)) {
				sb.append(getPathToCII());
			} else {
				sb.append(getPathToCII());
				sb.append(Consts.IOs.FILE_SEPARATOR);
			}
			sb.append(newParentCI.getCiiFileName());
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			ps.flush();
			ps.close();
		} catch (final FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere(INSTALLER + "installlist.txt file missing", fnfe);
		}
		updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
	}
	
	/**
	 * Generates the log file for LCP installation.
	 * 
	 * @param newParentCI the new parent CI
	 * @param type the type
	 * @param isSuccess the is success
	 */
	private void generateInstallLCPLog(final ParentCI newParentCI, final boolean isSuccess) {
		try {
			final String installLogFileName = UpdateService.INSTALL_LCP_LOG_FILE;
			final String pathToInstallLog = updateService.getPathToInstallLog();
			final String installLogFilePath = getFilePath(pathToInstallLog, installLogFileName);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Generating install log file for LCP: " + installLogFilePath);
			}

			final File installLogFile = new File(installLogFilePath);
			final FileOutputStream fos = new FileOutputStream(installLogFile);
			final PrintStream ps = new PrintStream(fos);

			final StringBuilder sb = new StringBuilder();
			final Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
			sdf.format(c.getTime());
			
			// Line 1
			sb.append("UpdateTime = ");
			sb.append(sdf.format(c.getTime()));
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 2
			sb.append("TotalSteps = ");
			sb.append(totalSteps);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 3
			sb.append("UpdatePatch = ");
			sb.append(getFilePath(getPathToLoadable(), newParentCI.getParent811FileName()));
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 4
			sb.append("UpdateCII = ");
			sb.append(getFilePath(getPathToCII(), newParentCI.getCiiFileName()));
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 5
			sb.append("OverallInstall = ");
			if (isSuccess) {
				sb.append("pass");
			} else {
				sb.append("fail");
			}

			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			ps.flush();
			ps.close();
		} catch (final FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere(INSTALLER + "install.log file missing", fnfe);
		}
		updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
	}

	/**
	 * Installs the secondary LRU software in the following
	 * procedure:
	 * - Loop through all the secondary LRUs and generate
	 * the installsecondary.txt file
	 * 
	 * @param newParentCI the new parent CI
	 * 
	 * @return true, if successful
	 */
	private boolean installSecondaryLru(final List<String> secondaryLruList, final HashMap<String, ParentCI> parentCIsToInstall) {
		boolean isStepNumberIncremented = false;
		for (final String secondaryLru : secondaryLruList) {
			if (parentCIsToInstall.containsKey(secondaryLru)) {
				if(!isStepNumberIncremented) {
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Incrementing the Step Number by 1 for secondary LRUs");
					}
					stepNumber = stepNumber + 1;
					isStepNumberIncremented = true;
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Step Number already incremented for secondary LRUs");
					}
				}
				
				final ParentCI newParentCI = parentCIsToInstall.get(secondaryLru);
				boolean isSecondaryLruSuccess = installSecondaryLru(newParentCI);

				isSecondaryInstalled = true;
				if (!isSecondaryLruSuccess) {
					return false;
				}
				
				// Set the ForceLoadStatus property for all possible PPC types connected to this LRU
				final List<String> ppcInstanceNames = updateHandler.getPPCInstanceNames();
				if(ppcInstanceNames != null && !ppcInstanceNames.isEmpty()) {
					for(final String ppcInstance : ppcInstanceNames) {
						try {
							if(ppcInstance != null && ppcInstance.startsWith(secondaryLru)) {
								final String propertyName = ppcInstance + PPC_FORCE_LOAD_STATUS;
								if(updateHandler.hasProperty(propertyName)) {
									if(!updateHandler.getProperty(PPC_FORCE_LOAD_STATUS).equalsIgnoreCase(PPC_STATUS_START)) {
										updateHandler.setProperty(propertyName, PPC_STATUS_START);
									}
								}
							}
						} catch(HandlerPropertyNotFoundException hpnfe) {
							if(updateService.isInfo()) {
								updateService.logInfo(INSTALLER + "ForceLoadStatus property is not available for secondary LRU: " + secondaryLru);
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Installs the given secondary LRU in the following
	 * procedure:
	 * - Add an entry in installsecondary.txt file
	 * 
	 * @param newParentCI
	 * @return
	 */
	private boolean installSecondaryLru(final ParentCI newParentCI) {
		stepName = "Secondary LRU: " + newParentCI.getLruType();
		String subStepName = "Generating secondary LRU install list for: " + newParentCI.getLruType();
		sendSA(subStepName, "0%", true, true);
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Installing " + newParentCI.getLruType() + " software");
		}

		final boolean returnValue = generateOtherLruInstallFile(newParentCI, UpdateService.SECONDARY_INSTALL_FILE, updateService.getPathToInstallLog());

		if (returnValue) {
			sendSA(subStepName, "100%", true, true);
			subStepName = "Files downloaded";
			sendFinalStatus(subStepName, "100%", true, true);
			return true;
		} else {
			abortInstallation("Installation aborted as generating secondary install list failed for: " + newParentCI.getLruType());
			return false;
		}
	}

	/**
	 * Installs the backup LRU software in the following
	 * procedure:
	 * - Loop through all the backup LRUs and generate
	 * the installbackup.txt file
	 * 
	 * @param newParentCI the new parent CI
	 * 
	 * @return true, if successful
	 */
	private boolean installBackupLru(final List<String> backupLruList, final HashMap<String, ParentCI> parentCIsToInstall) {
		// More than one backup LRU type can have the same LCP type
		// Maintain a list of LCP types to make sure that the log for same 
		// LCP type is not generated twice
		final ArrayList<String> lcpTypes = new ArrayList<String>();
		boolean isStepNumberIncremented = false;
		for (final String backupLru : backupLruList) {
			String backupLCPType = updateService.getLruToLCPTypes().get(backupLru);
			if(backupLCPType == null) {
				backupLCPType = UpdateService.LCP_TYPE;
			}
			boolean isSameLCPType = false;
			if(backupLCPType.equals(updateService.getLCPType())) {
				isSameLCPType = true;
			} else {
				isSameLCPType = false;
			}
			if (parentCIsToInstall.containsKey(backupLru) || parentCIsToInstall.containsKey(backupLCPType)) {
				if(!parentCIsToInstall.containsKey(backupLru)) {
					if(isSameLCPType) {
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + "No need to backup LCP type: " + backupLCPType + "as it is same as local LCP type");
						}
						continue;
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + "LCP type: " + backupLCPType + " should be backed up");
						}
					}
				}
				
				if(!isStepNumberIncremented) {
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Incrementing the Step Number by 1 for backup LRUs");
					}
					stepNumber = stepNumber + 1;
					isStepNumberIncremented = true;
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Step Number already incremented for backup LRUs");
					}
				}
				
				boolean isBackupLruSuccess = true;
				if(parentCIsToInstall.containsKey(backupLru)) {
					final ParentCI newParentCI = parentCIsToInstall.get(backupLru);
					isBackupLruSuccess = installBackupLru(newParentCI);
					isBackupInstalled = true;
					if (!isBackupLruSuccess) {
						return false;
					}
				}

				if(!isSameLCPType && !backupLCPType.equals(UpdateService.NO_LCP) && parentCIsToInstall.containsKey(backupLCPType) && !lcpTypes.contains(backupLCPType)) {
					final ParentCI newLCPParentCI = parentCIsToInstall.get(backupLCPType);
					isBackupLruSuccess = installBackupLru(newLCPParentCI);
					lcpTypes.add(backupLCPType);
					isBackupInstalled = true;
					if (!isBackupLruSuccess) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Installs the given backup LRU in the following
	 * procedure:
	 * - Add an entry in installbackup.txt file
	 * 
	 * @param newParentCI
	 * @return
	 */
	private boolean installBackupLru(final ParentCI newParentCI) {
		stepName = "Back LRU: " + newParentCI.getLruType();
		String subStepName = "Generating backup install list for: " + newParentCI.getLruType();
		sendSA(subStepName, "0%", true, true);
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Installing " + newParentCI.getLruType() + " software");
		}

		final boolean returnValue = generateOtherLruInstallFile(newParentCI, UpdateService.BACKUP_INSTALL_FILE, updateService.getPathToInstallLog());

		if (returnValue) {
			sendSA(subStepName, "100%", true, true);
			subStepName = "Backup complete";
			sendFinalStatus(subStepName, "100%", true, true);
			return true;
		} else {
			abortInstallation("Installation aborted as generating backup install list failed for: " + newParentCI.getLruType());
			return false;
		}
	}

	/**
	 * Generates the log file for secondary and backup LRU 
	 * installation.
	 * 
	 * @param newParentCI the new parent CI
	 * @param installFileName the install file name
	 * @param pathToInstall the path to install
	 * 
	 * @return true, if successful
	 */
	private boolean generateOtherLruInstallFile(final ParentCI newParentCI, final String installFileName, final String pathToInstall) {
		final String installFilePath = getFilePath(pathToInstall, installFileName);
		try {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Generating other LRU install file: " + installFilePath);
			}

			final FileWriter installFile = new FileWriter(installFilePath, true);
			final BufferedWriter writer = new BufferedWriter(installFile);
			final StringBuilder sb = new StringBuilder();

			// Line 1: LRU type
			sb.append("LRU = ");
			sb.append(newParentCI.getLruType());
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 2 Update Time
			final Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
			sdf.format(c.getTime());
			sb.append("UpdateTime = ");
			sb.append(sdf.format(c.getTime()));
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 3 Total Steps
			sb.append("TotalSteps = ");
			sb.append(totalSteps);
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 4: Update Patch - Path to the Parent 811 file
			sb.append("UpdatePatch = ");
			if (getPathToLoadable().endsWith(Consts.IOs.FILE_SEPARATOR)) {
				sb.append(getPathToLoadable());
			} else {
				sb.append(getPathToLoadable());
				sb.append(Consts.IOs.FILE_SEPARATOR);
			}
			sb.append(newParentCI.getParent811FileName());
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 5: Update CII - Path to the CII file
			sb.append("UpdateCII = ");
			if (getPathToCII().endsWith(Consts.IOs.FILE_SEPARATOR)) {
				sb.append(getPathToCII());
			} else {
				sb.append(getPathToCII());
				sb.append(Consts.IOs.FILE_SEPARATOR);
			}
			sb.append(newParentCI.getCiiFileName());
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 6: Part Number
			sb.append("PartNumber = ");
			sb.append(newParentCI.getCpn());
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 7: Build Number
			sb.append("BuildNumber = ");
			sb.append(newParentCI.getBuildNumber());
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 8: Force Load
			sb.append("ForceLoad = ");
			sb.append(String.valueOf(installList.isForceLoad()));
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			// Line 9: Overall Install status
			sb.append("OverallInstall = pass");
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			
			// Line 10 : LA time
			sb.append("LA = ");
			sb.append(readOriginalTime(updateService.getPathToLa(), LA_FILE));
			sb.append("\n");
			if(updateService.isInfo()) {
				UnitManager.Logging.logWarning(INSTALLER + "Writing to " + installFileName + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());

			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Flushing and closing the file: " + installFilePath);
			}
			writer.flush();
			writer.close();
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere(INSTALLER + "Not able to write to " + installFilePath, ioe);
			updateHandler.flushFiles(pathToInstall, false);
			return false;
		}
		updateHandler.flushFiles(pathToInstall, false);
		return true;
	}

	
	/**
	 * Reads original time of the last LA message received during
	 * the startup.
	 * 
	 * @param laFileName the la file name
	 * @param pathToLA the path to LA
	 */
	public String readOriginalTime(final String pathToLA, final String laFileName) {
		
		String laFilePath = null;
		String laTime = null;
		try {
			if (pathToLA.endsWith(Consts.IOs.FILE_SEPARATOR)) {
				laFilePath = pathToLA + laFileName;
			} else {
				laFilePath = pathToLA + Consts.IOs.FILE_SEPARATOR + laFileName;
			}

			final File la = new File(laFilePath);

			if (la.exists()) {
				if(updateService.isInfo()) {
					updateService.logInfo("Installer: Reading originalTimeOfLA from: " + laFilePath);
				}

				final FileReader laFile = new FileReader(la);

				final BufferedReader reader = new BufferedReader(laFile);
				
				final String line = reader.readLine();
				if(line != null && line.length() > 0) {
					laTime = line.trim();
				} else {
					laTime = null;
				}

				reader.close();
			}
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Installer: Not able to read from: " + laFilePath, ioe);
		}
		
		return laTime;
	}
	
	/**
	 * Installs the current LRU software in the following
	 * procedure:
	 * - Generate new installlist.txt header file
	 * - Reboot
	 * - The native installer will pickup the installlist.txt
	 * and starts the actual installation
	 * 
	 * @param parentCIsToInstall the parent CIs to install
	 * 
	 * @return true, if successful
	 */
	private boolean installLruSoftware(final HashMap<String, ParentCI> parentCIsToInstall) {
		stepNumber = stepNumber + 1;
		stepName = "Installing LRU software";
		
		String subStepName = "Generating install list header file";
		sendSA(subStepName, "0%", true, true);
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Installing LRU software");
		}

		final boolean returnValue = generateInstallListFile(parentCIsToInstall);

		if (returnValue) {
			sendSA(subStepName, "100%", true, true);
			
			return true;
		} else {
			abortInstallation("Installation aborted as generating install list header file failed");
			return false;
		}
	}
	
	/**
	 * Generates a header file of the CII/Parent software files to be downloaded.
	 * This header file will be checked by Update Service during its startup
	 * to resolve any duplicate files found (likely due to an incomplete or
	 * interrupted download) so that the incorrect one may be deleted.
	 * 
	 * @param parentCIsToInstall  list of parent CIs
	 * @param downloadListFileName file name
	 * 
	 * @return true, if successful
	 */
	private boolean generateDownloadListFile(final HashMap<String, ParentCI> parentCIsToInstall, final String downloadListFileName) {
		final String pathToDownloadlist = updateService.getPathToInstallLog();
		
		try {
			String downloadListFilePath = getFilePath(pathToDownloadlist, downloadListFileName);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Generating download list file: " + downloadListFilePath);
			}

			final File downloadListFile = new File(downloadListFilePath);

			final FileOutputStream fos = new FileOutputStream(downloadListFile);

			final PrintStream ps = new PrintStream(fos);

			final StringBuilder sb = new StringBuilder();

			for (final Entry<String, ParentCI> parentCIEntry : parentCIsToInstall.entrySet()) {
				final ParentCI newParentCI = parentCIEntry.getValue();

				// Line 1: Download CII - Path to the CII file
				sb.append("DownloadCII = ");
				sb.append(getFilePath(getPathToCII(), newParentCI.getCiiFileName()));
				sb.append("\n");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + downloadListFileName + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 2: Download Patch - Path to the Parent 811 file
				sb.append("DownloadPatch = ");
				sb.append(getFilePath(getPathToLoadable(), newParentCI.getParent811FileName()));
				sb.append("\n");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + downloadListFileName + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
			}

			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Flushing and closing the download file: " + downloadListFile.getName());
			}
			ps.flush();
			ps.close();
			
		} catch  (final FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere(INSTALLER + downloadListFileName + " file missing", fnfe);
			updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
			return false;
		}
		
		updateHandler.flushFiles(pathToDownloadlist, false);
		return true;
	}	

	/**
	 * Generates a header file of the CII/Parent software files to be downloaded.
	 * This header file will be checked by Update Service during its startup
	 * to make sure whatever user wanted to load was successfully loaded or not.
	 * This file will be generated only for user driver load and not for self load.
	 * 
	 * @param parentCIsToInstall  list of parent CIs
	 * @param downloadListFileName file name
	 * 
	 * @return true, if successful
	 */
	private boolean generateToBeInstalledFile(final HashMap<String, ParentCI> parentCIsToInstall, final String downloadListFileName) {
		final String pathToDownloadlist = updateService.getPathToInstallLog();
		
		try {
			String downloadListFilePath = getFilePath(pathToDownloadlist, downloadListFileName);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Generating to be loaded file: " + downloadListFilePath);
			}

			final File downloadListFile = new File(downloadListFilePath);

			final FileOutputStream fos = new FileOutputStream(downloadListFile);

			final PrintStream ps = new PrintStream(fos);

			final StringBuilder sb = new StringBuilder();

			for (final Entry<String, ParentCI> parentCIEntry : parentCIsToInstall.entrySet()) {
				final ParentCI newParentCI = parentCIEntry.getValue();

				// Line 1: LRU - LRU type
				sb.append("LRU = ");
				sb.append(newParentCI.getLruType());
				sb.append("\n");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + downloadListFileName + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 2: CPN - Part Number
				sb.append("CPN = ");
				sb.append(newParentCI.getCpn());
				sb.append("\n");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + downloadListFileName + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 3: BN - Build Number
				sb.append("BN = ");
				sb.append(newParentCI.getBuildNumber());
				sb.append("\n");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + downloadListFileName + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 4: CII - CII file name
				sb.append("CII = ");
				sb.append(newParentCI.getCiiFileName());
				sb.append("\n");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + downloadListFileName + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
				
				// Line 5: Parent - Parent 811 file name
				sb.append("Parent = ");
				sb.append(newParentCI.getParent811FileName());
				sb.append("\n");
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Writing to " + downloadListFileName + ": " + sb.toString());
				}
				ps.println(sb.toString());
				sb.delete(0, sb.length());
			}

			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Flushing and closing the to be loaded file: " + downloadListFile.getName());
			}
			ps.flush();
			ps.close();
			
		} catch  (final FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere(INSTALLER + downloadListFileName + " file missing", fnfe);
			updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
			return false;
		}
		
		updateHandler.flushFiles(pathToDownloadlist, false);
		return true;
	}	

	/**
	 * Generates the header file current LRU software installation.
	 * This header file will be checked for its availability by the 
	 * native API during the LRU startup. If the file is available, 
	 * it starts the installation process.
	 * 
	 * @param parentCIsToInstall the list of parent CIs to install
	 * 
	 * @return true, if successful
	 */
	private boolean generateInstallListFile(final HashMap<String, ParentCI> parentCIsToInstall) {
		try {
			final String installlistFileName = UpdateService.INSTALL_LIST_FILE;
			final String pathToInstalllist = updateService.getPathToInstallLog();
			String installlistFilePath = getFilePath(pathToInstalllist, installlistFileName);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Generating install list file: " + installlistFilePath);
			}

			final File installListFile = new File(installlistFilePath);

			final FileOutputStream fos = new FileOutputStream(installListFile);

			final PrintStream ps = new PrintStream(fos);

			final StringBuilder sb = new StringBuilder();

			// Line 1: Total Number of Steps including things done already
			// by java - Used to generate the current step number
			sb.append("TotalSteps = ");
			sb.append(totalSteps);
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 2: LRU Instance - Used in the SA Message
			sb.append("LRUinstance = ");
			sb.append(updateService.getLruInstance());
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());

			// Line 3: MulticastInfo - Multicast IP/Port Number
			sb.append("MulticastInfo = ");
			sb.append(updateService.getMulticastIp());
			sb.append("/");
			sb.append(updateService.getMulticastPort());
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());
			
			ps.println();

			ParentCI newParentCI = parentCIsToInstall.get(hardwareDetails.getDeviceType());
			boolean isWriteSuccess = false;
			if(newParentCI != null) {
				isWriteSuccess = writeToInstallList(newParentCI, ps, sb, false);
				if(!isWriteSuccess) {
					ps.flush();
					ps.close();
					delete(pathToInstalllist, installlistFileName);
					return false;
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "No need to install: " + hardwareDetails.getDeviceType());
				}
			}
			if(isPrimaryPackage) {
				final List<String> primaryPackagesList = updateService.getPrimaryPackagesList();
				for(final String primaryPackage : primaryPackagesList) {
					newParentCI = parentCIsToInstall.get(primaryPackage);
					if(newParentCI != null) {
						isWriteSuccess = writeToInstallList(newParentCI, ps, sb, true);
						if(!isWriteSuccess) {
							ps.flush();
							ps.close();
							delete(pathToInstalllist, installlistFileName);
							return false;
						}
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "No need to install any primary packages");
				}
			}
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Flushing and closing the file: " + installListFile.getName());
			}
			ps.flush();
			ps.close();
		} catch (final FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere(INSTALLER + "installlist.txt file missing", fnfe);
			updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
			return false;
		}
		updateHandler.flushFiles(updateService.getPathToInstallLog(), false);
		return true;
	}

	/**
	 * Writes individual Parent CI details to the install list file.
	 * 
	 * @param newParentCI the new Parent CI
	 * @param ps the Print Stream
	 * @param sb the String Builder
	 * @param isAppendType the Is Append Type
	 * 
	 * @return true if success
	 */
	private boolean writeToInstallList(final ParentCI newParentCI, final PrintStream ps, final StringBuilder sb, final boolean isAppendType) {
		
		
		// Update Patch - Path to the Parent 811 TAR
		sb.append("UpdatePatch");
		if(isAppendType) {
			sb.append(newParentCI.getLruType().toUpperCase());
		}
		sb.append(" = ");
		if(updateHandler.isCopyLoadFiles() || mcdPatchOnly) {
			sb.append(getPathToLoadable());
		} else {
			sb.append(installList.getPathToLoadable());
		}
		if(!sb.toString().endsWith(Consts.IOs.FILE_SEPARATOR)) {
			sb.append(Consts.IOs.FILE_SEPARATOR);
		}
		sb.append(newParentCI.getParent811FileName());
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
		}
		ps.println(sb.toString());
		sb.delete(0, sb.length());

		// Update Patch - Path to the CII
		sb.append("UpdateCII");
		if(isAppendType) {
			sb.append(newParentCI.getLruType().toUpperCase());
		}
		sb.append(" = ");
		if(updateHandler.isCopyLoadFiles() || mcdPatchOnly) {
			sb.append(getPathToCII());
		} else {
			sb.append(installList.getPathToCII());
		}
		if(!sb.toString().endsWith(Consts.IOs.FILE_SEPARATOR)) {
			sb.append(Consts.IOs.FILE_SEPARATOR);
		}
		sb.append(newParentCI.getCiiFileName());
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
		}
		ps.println(sb.toString());
		sb.delete(0, sb.length());
		
		if(newParentCI.isPrimitive()) {
			sb.append(newParentCI.getLruType().toUpperCase());
			sb.append(" = yes   File = ");
			sb.append(newParentCI.getParent811FileName());
			sb.append("   md5sum = ");
			sb.append(newParentCI.getMd5Value());
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
			}
			ps.println(sb.toString());
			sb.delete(0, sb.length());
			ps.println();
		} else {
			final List<ChildCI> toInstall = installList.getChildCIsToInstall().get(newParentCI.getLruType());
			final List<ChildCI> notToInstall = installList.getChildCIsNotToInstall().get(newParentCI.getLruType());

			// ChildCIs to install
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Looping through ChildCIs to install");
			}

			if (toInstall != null && toInstall.size() > 0) {
				for (final ChildCI newChildCI : toInstall) {
					final String type = newChildCI.getChildCIType();
					final String fileName = newChildCI.getChild811FileName();
					final String md5Value = newChildCI.getMd5Value();

					if (type == null || type.length() == 0
							|| fileName == null || fileName.length() == 0) {
						UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Cannot write to file: type: " + type + " : fileName: " + fileName));
						return false;
					} else {
						sb.append(type);
						if ( ! mcdPatchOnly ) {			
							sb.append(" = yes");
						} else { // mcd patch only so only JavaApp,FactoryLCP
							if (type.equalsIgnoreCase("JavaApp") || type.equalsIgnoreCase("FactoryLCP")) {
								sb.append(" = yes");
							} else {
								sb.append(" = no");
							}
						}
						sb.append("   File = ");
						sb.append(fileName);
						sb.append("   md5sum = ");
						sb.append(md5Value);
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
						}
						ps.println(sb.toString());
						sb.delete(0, sb.length());
					}
				}
			} else {
				UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "No ChildCIs to install for: " + newParentCI.getLruType() + ". Atleast one ChildCI should be installed."));
				return false;
			}

			// ChildCIs not to install
			if (notToInstall != null && notToInstall.size() > 0) {
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Looping through ChildCIs not to install");
				}

				for (final ChildCI newChildCI : notToInstall) {
					final String type = newChildCI.getChildCIType();
					final String fileName = newChildCI.getChild811FileName();
					final String md5Value = newChildCI.getMd5Value();

					if (type == null || type.length() == 0 || fileName == null || fileName.length() == 0) {
						UnitManager.Logging.logSevere(new UpdateServiceException(INSTALLER + "Cannot write to file: type: " + type + " : fileName: " + fileName));
						return false;
					} else {
						sb.append(type);
						sb.append(" = no   File = ");
						sb.append(fileName);
						sb.append("   md5sum = ");
						sb.append(md5Value);
						if(updateService.isInfo()) {
							updateService.logInfo(INSTALLER + "Writing to file: " + sb.toString());
						}
						ps.println(sb.toString());
						sb.delete(0, sb.length());
					}
				}
			}
			
			ps.println();
		}

		return true;
	}

	/**
	 * Restarts the java environment.
	 * 
	 * @return true, if successful
	 */
	private boolean restartJava() {
		String subStepName = REBOOT_STEP;

		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + subStepName);
		}
		
		// Send the reboot wait status more than once
		sendFinalStatus(subStepName, "0%", true, true);

		waitForReboot();
		
		subStepName = "Rebooting";
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + subStepName);
		}
		
		// Send the final status more than once
		sendFinalStatus(subStepName, "100%", true, true);
		
		final boolean returnValue = updateHandler.restartJava();
		if (returnValue) {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Rebooted");
			}
		} else {
			UnitManager.Logging.logSevere(INSTALLER + "Rebooting failed");
		}
		return returnValue;
	}
	
	/**
	 * Launch installer.
	 */
	private void launchInstaller() {
		stepName = "Installing LRU software";
		String subStepName = REBOOT_STEP;
		
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + subStepName);
		}

		// Send the reboot wait status more than once
		sendFinalStatus(subStepName, "0%", true, true);
		waitForReboot();
		
		subStepName = "Rebooting";
		if(updateService.isInfo()) {
			updateService.logInfo(INSTALLER + subStepName);
		}
		
		// Send the final status more than once
		sendFinalStatus(subStepName, "100%", true, true);
		
		updateHandler.launchInstaller();
	}
	
	/**
	 * Wait for reboot command.
	 */
	private void waitForReboot() {
		if(installList.isSelfLoad() || !installList.isRebootWait()) {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "No need to wait for REBOOT command");
				updateService.logInfo(INSTALLER + "Self Load - " + installList.isSelfLoad());
				updateService.logInfo(INSTALLER + "Reboot Wait - " + installList.isRebootWait());
			}
			return;
		}
		
		if(updateService.isRebootReceived()) {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + "Already received reboot command");
			}
			String subStepName = "Reboot command has been received already: " + updateService.isRebootReceived();
			sendSA(subStepName, "0%", true, true);
		} else {
			synchronized(updateService.getWaitForReboot()) {
				try {
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Waiting for reboot command: Timeout: " + updateService.getRebootCommandTimeout());
					}
					updateService.getWaitForReboot().wait(updateService.getRebootCommandTimeout());
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + "Wait time over for reboot command");
						updateService.logInfo(INSTALLER + "Reboot command received: " + updateService.isRebootReceived());
					}
					String subStepName = "Reboot command received: " + updateService.isRebootReceived();
					sendSA(subStepName, "0%", true, true);
					if(updateService.isInfo()) {
						updateService.logInfo(INSTALLER + subStepName);
					}
				} catch (InterruptedException ie) {
					UnitManager.Logging.logSevere(INSTALLER + "Interrupted while waiting for reboot command", ie);
				}
			}
		}
		
		if(updateService.isRebootReceived()) {
			// Wait for some more time before doing the reboot
			// This is to make sure that all the LRUs receive the reboot command from the host LRU
			try {
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Received the reboot command - Waiting for extra time: " + updateService.getSleepBeforeReboot());
					updateService.logInfo(INSTALLER + "Sleeping right before reboot");
				}
				String subStepName = "Reboot in: " + updateService.getSleepBeforeReboot()/1000 + " seconds";
				sendSA(subStepName, "0%", true, true);
				Thread.sleep(updateService.getSleepBeforeReboot());
				if(updateService.isInfo()) {
					updateService.logInfo(INSTALLER + "Finished sleeping right before reboot");
				}
			} catch (InterruptedException ie) {
				UnitManager.Logging.logSevere(INSTALLER + "Interrupted while sleeping just before doing the reboot", ie);
			}
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo(INSTALLER + " Not sleeping just before reboot - isRebootReceived: " + updateService.isRebootReceived());
			}
		}
	}
	
	/**
	 * Sends the final status more than once to make 
	 * sure it reaches everyone.
	 * 
	 * @param subStepName
	 * @param subStepPercentage
	 * @param isStatusAvailable
	 * @param isRunning
	 */
	private void sendFinalStatus(final String subStepName, final String subStepPercentage, final boolean isStatusAvailable, final boolean isRunning) {
		int duration = updateService.getFinalStatusDuration();
		int frequency = updateService.getFinalStatusFrequency();
		int timeElapsed = 0;
		while(timeElapsed < duration) {
			sendSA(subStepName, subStepPercentage, isStatusAvailable, isRunning);
			try {
				Thread.sleep(frequency);
			} catch (InterruptedException ie) {
				UnitManager.Logging.logSevere(INSTALLER + "Error while sending the final status more than once", ie);
			}
			timeElapsed = timeElapsed + frequency;
		}
	}
	
	/**
	 * KeySelector which retrieves the public key out of the KeyValue element
	 * and returns it. NOTE: If the key algorithm doesn't match signature
	 * algorithm, then the public key will be ignored.
	 */
	private static class KeyValueKeySelector extends KeySelector {
		public KeySelectorResult select(final KeyInfo keyInfo, final KeySelector.Purpose purpose, final AlgorithmMethod method, final XMLCryptoContext context) throws KeySelectorException {
			if (keyInfo == null) {
				throw new KeySelectorException("Null KeyInfo object!");
			}
			final SignatureMethod sm = (SignatureMethod) method;
			final List<?> list = keyInfo.getContent();

			for (int i = 0; i < list.size(); i++) {
				final XMLStructure xmlStructure = (XMLStructure) list.get(i);
				if (xmlStructure instanceof KeyValue) {
					PublicKey pk = null;
					try {
						pk = ((KeyValue) xmlStructure).getPublicKey();
					} catch (final KeyException ke) {
						throw new KeySelectorException(ke);
					}
					// make sure algorithm is compatible with method
					if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
						return new SimpleKeySelectorResult(pk);
					}
				}
			}
			throw new KeySelectorException("No KeyValue element found!");
		}

		static boolean algEquals(final String algURI, final String algName) {
			if (algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
				return true;
			} else if (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	private static class SimpleKeySelectorResult implements KeySelectorResult {
		private PublicKey pk;

		SimpleKeySelectorResult(final PublicKey pk) {
			this.pk = pk;
		}

		public Key getKey() {
			return pk;
		}
	}
}
