package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.TreeMap;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyMap;
import com.rockwellcollins.cs.hcms.core.services.update.messages.MAActionTypes;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;
import com.rockwellcollins.cs.hcms.core.services.ServiceState;

/**
 * The Class UpdateHandler exposes necessary methods for Update Service. It
 * includes methods for making state changes and abstract methods that needs to
 * be implemented in a LRU specific way.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * 
 */
public abstract class UpdateHandler extends Handler {

	private static final long serialVersionUID = 1L;
	
	protected UpdateService updateService;
	
	protected HashMap<String, PropertyMap> secondaryLruPropertyMapMap;
	
	/** PPC state set retry limit */
	protected int ppcStateRetryLimit = 4;
	
	/** PPC state set retry timeout */
	protected int ppcStateRetryTimeout = 10000;
	
	/** The Constant HDS_INFO_FILE. */
	public static final String HDS_INFO_FILE = "HDSInfo.xml";
			
	/**
	 * The Enum Properties.
	 */
	public static enum Properties {

		/** THE SOFTWARE_PART_NUMBER. */
		DISABLE_SOFTWARE_LOAD("DisableSoftwareLoad"),
		
		/** THE IS_INSTALLING. */
		IS_INSTALLING("IsInstalling"),
		
		SELF_LOAD("SelfLoad"),
		
		LOAD_STATUS_IP("LoadStatusIP"),

		/** THE DISABLE_BUTTONS. */
		DISABLE_BUTTONS("DisableButtons"),

		/** THE TITLE. */
		TITLE("Title"),

		/** THE TEXT_1. */
		TEXT_1("Text1"),

		/** THE TEXT_2. */
		TEXT_2("Text2"),

		/** THE TEXT_3. */
		TEXT_3("Text3"),

		/** THE TEXT_4. */
		TEXT_4("Text4"),

		/** THE TEXT_5. */
		TEXT_5("Text5"),

		/** THE CONFIRM_TEXT_1. */
		CONFIRM_TEXT_1("ConfirmText1"),

		/** THE CONFIRM_TEXT_2. */
		CONFIRM_TEXT_2("ConfirmText2"),

		/** THE CONFIRM_TEXT_3. */
		CONFIRM_TEXT_3("ConfirmText3"),

		/** THE CONFIRM_TEXT_4. */
		CONFIRM_TEXT_4("ConfirmText4"),
		
		/** THE CII_FILES_TO_LOAD. */
		CII_FILES_TO_LOAD("CIIFilesToLoad"),

		/** THE NAVIGATE_TO_LOAD_SCREEN. */
		NAVIGATE_TO_LOAD_SCREEN("NavigateToLoadScreen"),

		/** THE NAVIGATE_TO_MAINTENANCE_SCREEN. */
		NAVIGATE_TO_MAINTENANCE_SCREEN("NavigateToMaintenanceScreen"),

		/** THE NAVIGATE_TO_CONFIRM_SCREEN. */
		NAVIGATE_TO_CONFIRM_SCREEN("NavigateToConfirmScreen"),
		
		NAVIGATE_TO_CONFIRM_FILES_SCREEN("NavigateToConfirmFilesScreen"),
		
		/** THE IS_DISABLE_COMPATIBILITY. */
		IS_DISABLE_COMPATIBILITY("IsDisableCompatibility"),

		/** THE DISABLE_SOFTWARE_LOAD_STATUS. */
		DISABLE_SOFTWARE_LOAD_STATUS("DisableSoftwareLoadStatus"),

		/** THE SYSTEM_STATUS. */
		SYSTEM_LOAD_STATUS("SystemLoadStatus"),

		/** THE LRUS_LOAD_STATUS_LIST. */
		LRUS_LOAD_STATUS_LIST("LRUsLoadStatusList"),
		
		/** THE LOAD_STATUS_TEXT. */
		LOAD_STATUS_TEXT("LoadStatusText"),
		
		/** THE OVERALL_STATUS. */
		OVERALL_STATUS("OverAllStatus"),
		
		/** THE OVERALL_STATUS_TEXT. */
		OVERALL_STATUS_TEXT("OverAllStatusText"),
		
		/** THE SUBSTEP_STATUS. */
		SUBSTEP_STATUS("SubStepStatus"),
		
		/** THE SUBSTEP_STATUS_TEXT. */
		SUBSTEP_STATUS_TEXT("SubStepStatusText"),
		
		/** THE MEDIA_FORCE_LOAD. */
		MEDIA_FORCE_LOAD("MediaForceLoad"),
		
		/** THE START_MEDIA_LOAD. */
		START_MEDIA_LOAD("StartMediaLoad"),
		
		/** THE START_SWCENTRE_LOAD. */
		START_SWCENTRE_LOAD("StartSWCentreLoad"),

		/** THE VALIDATE_MEDIA_LOAD. */
		VALIDATE_MEDIA_LOAD("ValidateMediaLoad"),

		/** THE CONFIRM_FILES_TO_LOAD. */
		CONFIRM_FILES_TO_LOAD("ConfirmFilesToLoad"),

		/** THE CONFIRM_MEDIA_LOAD. */
		CONFIRM_MEDIA_LOAD("ConfirmMediaLoad"),

		/** THE CANCEL_MEDIA_LOAD. */
		CANCEL_MEDIA_LOAD("CancelMediaLoad");

		/** The property name. */
		private String propertyName;

		/**
		 * Instantiates a new properties.
		 * 
		 * @param propertyName the property name
		 */
		private Properties(final String propertyName) {
			this.propertyName = propertyName;
		}

		/**
		 * Gets the property name.
		 * 
		 * @return the property name
		 */
		public String getPropertyName() {
			return propertyName;
		}
	}
	
	/**
	 * Gets the hardware details.
	 * 
	 * @return the hardware details
	 */
	public abstract HardwareInfo getHardwareDetails();

	/**
	 * Gets the FTP adapter.
	 * 
	 * @return the FTP adapter
	 */
	public abstract FTPAdapter getFTPAdapter();

	/**
	 * Gets the ftp ip address.
	 * 
	 * @return the ftp ip address
	 */
	public abstract String getFtpIpAddress();

	/**
	 * Gets the ftp port number.
	 * 
	 * @return the ftp port number
	 */
	public abstract int getFtpPortNumber();

	/**
	 * Gets the ftp username.
	 * 
	 * @return the ftp username
	 */
	public abstract String getFtpUsername();

	/**
	 * Gets the ftp password.
	 * 
	 * @return the ftp password
	 */
	public abstract String getFtpPassword();

	/**
	 * List files.
	 * 
	 * @param directoryPath the directory path
	 * 
	 * @return the list< string>
	 */
	public abstract List<String> listFiles(String directoryPath);

	/**
	 * Launch installer.
	 */
	public abstract void launchInstaller();

	/**
	 * Gets the md5 sum.
	 * 
	 * @param filePath the file path
	 * 
	 * @return the md5 sum
	 */
	public abstract String getMd5Sum(String filePath);

	/**
	 * Gets the installer steps.
	 * 
	 * @param installList the install list
	 * 
	 * @return the installer steps
	 */
	public abstract int getInstallerSteps(String installList);

	/**
	 * Install lcp.
	 * 
	 * @param pathToLoadable the path to loadable
	 * 
	 * @return true, if successful
	 */
	public abstract boolean installLcp(String pathToLoadable);

	/**
	 * Restart java.
	 * 
	 * @return true, if successful
	 */
	public abstract boolean restartJava();

	/**
	 * Removes the old CII files.
	 * 
	 * @param newCIIFile the new CII file
	 * @param charactersMatch the characters match
	 * 
	 * @return true, if successful
	 */
	public abstract boolean removeOldCIIFiles(String newCIIFile, int charactersMatch);

	/**
	 * Removes the old loadable files.
	 * 
	 * @param newLoadableFile the new loadable file
	 * @param charactersMatch the characters match
	 * 
	 * @return true, if successful
	 */
	public abstract boolean removeOldLoadableFiles(String newLoadableFile, int charactersMatch);

	/**
	 * Checks if is copy load files.
	 * 
	 * @return true, if is copy load files
	 */
	protected abstract boolean isCopyLoadFiles();

	/**
	 * Nfs mount path.
	 * 
	 * @return the string
	 */
	protected abstract String nfsMountPath();

	/**
	 * Mount via nfs.
	 * 
	 * @param ipAddress the ip address
	 * @param remotePath the remote path
	 * @param localPath the local path
	 * 
	 * @return true, if successful
	 */
	protected abstract boolean mountViaNfs(String ipAddress, String remotePath, String localPath);

	/**
	 * Unmount.
	 * 
	 * @param localPath the local path
	 * 
	 * @return true, if successful
	 */
	protected abstract boolean unmount(String localPath);

	/**
	 * Copy.
	 * 
	 * @param sourceDir the source dir
	 * @param localDir the local dir
	 * @param fileName the file name
	 * 
	 * @return true, if successful
	 */
	protected abstract boolean copy(String sourceDir, String localDir, String fileName);
	
	/**
	 * Delete compatibility matrix.
	 */
	protected abstract void deleteCompatibilityMatrix();
	
	/**
	 * Convert CII files to state value.
	 * 
	 * @param parentCIs the parent C is
	 * 
	 * @return the string
	 */
	protected abstract String convertCIIFilesToStateValue(final HashMap<String, ParentCI> parentCIs);
	
	/**
	 * Convert status to state value.
	 * 
	 * @param statusDetails the status details
	 * 
	 * @return the string
	 */
	protected abstract String convertStatusToStateValue(final TreeMap<String, SAMessage> statusDetails);
	
	/**
	 * Run conformity.
	 * 
	 * @param parentCIs the parent C is
	 */
	protected abstract void runConformity(HashMap<String, ParentCI> parentCIs);
	
	/**
	 * Initialize for load.
	 * 
	 * @return true, if successful
	 */
	protected boolean initializeForLoad() {
		return true;
	}

	/**
	 * Initialize to FTP.
	 * 
	 * @return true, if successful
	 */
	protected boolean initializeToFTP() {
		return true;
	}
	
	/**
	 * Notification of download completion.
	 * 
	 */
	protected void onDownloadComplete() {
		
	}
	
	/**
	 * Mount the partitions to read write
	 * 
	 * @return true
	 */
	protected boolean mountToReadWrite() {
		return true;
	}
	
	/**
	 * Mount the partitions to read only
	 * 
	 * @return true
	 */
	protected boolean mountToReadOnly() {
		return true;
	}
	
	/**
	 * Stop GUI.
	 */
	protected void stopGui() {
		
	}
	
	/**
	 * Update progress bar.
	 * 
	 * @param repaint the repaint
	 * @param firstBar the first bar
	 * @param secondBar the second bar
	 * 
	 * @return true, if successful
	 */
	protected boolean updateProgressBar(final int repaint, final int firstBar, final int secondBar) {
		return true;
	}

	public void flushFiles() {
		flushFiles(true);
	}
	/**
	 * Flush files.
	 */
	protected void flushFiles(final boolean isUpdateChecksum) {

	}

	/**
	 * Flush files.
	 * 
	 * @param filePath the file path
	 */
	protected void flushFiles(final String filePath, final boolean isUpdateChecksum) {

	}

	/**
	 * On abort installation.
	 */
	protected void onAbortInstallation() {

	}

	/**
	 * Checks if is ready.
	 * 
	 * @return true, if is ready
	 */
	protected boolean isReady() {
		return (this.getServiceState() == ServiceState.RUNNING);
	}
	
	/**
	 * This method was added just to provide backward
	 * compatibility with Release 3.0.0 LCP as the  
	 * setting Path To LA was introduced only in Release 
	 * 4.0.0. MCD needs to override the default value for
	 * Release 3.0.0 LCP.
	 * 
	 * @param pathToLa the new path to la
	 */
	protected void setPathToLa(final String pathToLa) {
		updateService.setPathToLa(pathToLa);
	}
	
	/**
	 * This method was added just to provide backward
	 * compatibility with Release 3.0.0 LCP as the  
	 * setting Path To LA was introduced only in Release 
	 * 4.0.0. MCD needs to override the default value for
	 * Release 3.0.0 LCP.
	 * 
	 * @return the path to la
	 */
	protected String getPathToLa() {
		return updateService.getPathToLa();
	}
	
	/**
	 * Sets the self load flag.
	 * 
	 * @param selfLoadFlag the new self load flag
	 */
	protected void setSelfLoadFlag(final boolean selfLoadFlag) {
		updateService.setSelfLoadFlag(selfLoadFlag);
	}

	/**
	 * Process media load.
	 * 
	 * @param action the action
	 * @param value the value
	 */
	protected void processMediaLoad(final MAActionTypes action, final boolean value) {
		if(updateService != null && updateService.getMaProcessor() != null) {
			updateService.getMaProcessor().processMediaLoad(action, value);
		} else {
			if(isInfo()) {
				logInfo("Update Handler: Either updateService or maProcessor is null");
			}
		}
	}

	/**
	 * Gets the removable load media.
	 * 
	 * @return the removable load media
	 */
	protected List<String> getRemovableLoadMedia() {
		return null;
	}
	
	/**
	 * Gets software centre location.
	 * 
	 * @return the removable load media
	 */
	protected List<String> getSoftwareCentreLocation() {
		return null;
	}
	
	/**
	 * Gets software centre location.
	 * 
	 * @return the removable load media
	 */
	protected boolean delete(String destinationPath) {
		return false;
	}
	
	/**
	 * Disables the watchdog, if any, during downloading.
	 */
	protected void disableWatchDog() {
		
	}

	/**
	 * Gets the IP address for which the load status has 
	 * to be updated.
	 * 
	 * @return the load status ip
	 */
	protected String getLoadStatusIp() {
		return null;
	}
	
	/**
	 * Sets the software load status.
	 * 
	 * @param ipAddress the new software load status
	 */
	protected void setSoftwareLoadStatus(final String ipAddress) {
		if(updateService != null && updateService.getSaProcessor() != null) {
			updateService.getSaProcessor().setSoftwareLoadStatus(ipAddress);
		} else {
			if(isInfo()) {
				logInfo("Update Handler: Either updateService or saProcessor is null");
			}
		}
	}
	
	/**
	 * Gets the primary packages list.
	 * 
	 * @return the primary packages list
	 */
	protected List<String> getPrimaryPackagesList() {
		return null;
	}
	
	protected String getLCPType() {
		return UpdateService.LCP_TYPE;
	}
	
	protected List<String> getPPCInstanceNames() {
		return null;
	}
	
	/**
	 * Load secondary lru.
	 * 
	 * @param lruType the lru type
	 * @param downloadPath the download path
	 * @param partNumber the part number
	 * @param buildNumber the build number
	 * @param forceLoad the force load
	 */
	public void loadSecondaryLru(final String updateTime, final String lruType, final String downloadPath, final String partNumber, final String buildNumber, final boolean forceLoad) {
		String propertyName = null;
		final PropertyMap propertyMap = new PropertyMap();

		if(updateTime != null) {
			propertyName = lruType.toUpperCase() + "UpdateTime";
			if(hasProperty(propertyName)) {
				propertyMap.put(propertyName, updateTime);
			}
		}

		propertyName = lruType.toUpperCase() + "ForceLoadFlag";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, String.valueOf(forceLoad));
		}
		
		propertyName = lruType.toUpperCase() + "DownloadPath";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, downloadPath);
		}

		propertyName = lruType.toUpperCase() + "PartNumber";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, partNumber);
		}
		
		propertyName = lruType.toUpperCase() + "BuildNumber";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, buildNumber);
		}
		
		if(propertyMap.size() > 0) {
			if(isInfo()) {
				logInfo("UpdateHandler: Setting the following states (without LA): ");
				for(Entry<String, String> entry : propertyMap.entrySet()) {
					logInfo("UpdateHandler: " + entry.getKey() + " : " + entry.getValue());
				}
			}
			setProperty(propertyMap);
			
			// retry to set the above state if they are not set
			boolean allPPCStateSet = false;
			try {
				for(int i=1; i<ppcStateRetryLimit ; i++){
					Thread.sleep(ppcStateRetryTimeout);
					allPPCStateSet = true;
					for (Entry<String, String> entry : propertyMap.entrySet()) {
						if (!getProperty(entry.getKey()).equals(entry.getValue())) {
							setProperty(entry.getKey(), entry.getValue());
							allPPCStateSet = false;
						}				
					}
					if(allPPCStateSet)
						break;
				}
			} catch(Exception e) {
				UnitManager.Logging.logSevere("UpdateHandler: Exception occured in loadSecondaryLru", e);
			}
			if (secondaryLruPropertyMapMap == null) {
				secondaryLruPropertyMapMap = new HashMap<String, PropertyMap>() ;
			}
			secondaryLruPropertyMapMap.put(lruType, propertyMap);
		} else {
			if(isInfo()) {
				logInfo("UpdateHandler: No states to set (without LA) for loading secondary LRU: " + lruType);
			}
		}
	}

	/**
	 * Load secondary lru.
	 * 
	 * @param lruType the lru type
	 * @param downloadPath the download path
	 * @param partNumber the part number
	 * @param buildNumber the build number
	 * @param forceLoad the force load
	 * @param LA la time
	 */
	public void loadSecondaryLru(final String updateTime, final String lruType, final String downloadPath, final String partNumber, final String buildNumber, final boolean forceLoad, final String LA) {
		String propertyName = null;
		final PropertyMap propertyMap = new PropertyMap();

		if(updateTime != null) {
			propertyName = lruType.toUpperCase() + "UpdateTime";
			if(hasProperty(propertyName)) {
				propertyMap.put(propertyName, updateTime);
			}
		}

		propertyName = lruType.toUpperCase() + "ForceLoadFlag";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, String.valueOf(forceLoad));
		}
		
		propertyName = lruType.toUpperCase() + "DownloadPath";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, downloadPath);
		}

		propertyName = lruType.toUpperCase() + "PartNumber";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, partNumber);
		}
		
		propertyName = lruType.toUpperCase() + "BuildNumber";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, buildNumber);
		}
		
		propertyName = lruType.toUpperCase() + "LA";
		if(hasProperty(propertyName)) {
			propertyMap.put(propertyName, LA);
		}
		
		if(propertyMap.size() > 0) {
			if(isInfo()) {
				logInfo("UpdateHandler: Setting the following states: ");
				for(Entry<String, String> entry : propertyMap.entrySet()) {
					logInfo("UpdateHandler: " + entry.getKey() + " : " + entry.getValue());
				}
			}
			setProperty(propertyMap);
			boolean allPPCStateSet = false;
			try {
				for(int i=1; i<ppcStateRetryLimit ; i++){
					Thread.sleep(ppcStateRetryTimeout);
					allPPCStateSet = true;
					for (Entry<String, String> entry : propertyMap.entrySet()) {
						if (!getProperty(entry.getKey()).equals(entry.getValue())) {
							setProperty(entry.getKey(), entry.getValue());
							allPPCStateSet = false;
						}				
					}
					if(allPPCStateSet)
						break;
				}
			} catch(Exception e) {
				UnitManager.Logging.logSevere("UpdateHandler: Exception occured in loadSecondaryLru", e);
			}
			if (secondaryLruPropertyMapMap == null) {
				secondaryLruPropertyMapMap = new HashMap<String, PropertyMap>() ;
			}
			secondaryLruPropertyMapMap.put(lruType, propertyMap);
		} else {
			if(isInfo()) {
				logInfo("UpdateHandler: No states to set for loading secondary LRU: " + lruType);
			}
		}
	}

	/**
	 * Obtain the reference of Update Service during onSetup
	 * 
	 * @param source
	 * @param args
	 */
	@Override
	protected void onSetup(Object source, ComponentSetupArgs args) throws ComponentSetupException {
		super.onSetup(source, args);
		
		updateService = getComponents().getFirstByClass(UpdateService.class);

		if (updateService == null) {
			UnitManager.Logging.logSevere(new Exception("Update Handler: Update Service is null"));
		}
		
		secondaryLruPropertyMapMap = new HashMap<String, PropertyMap>() ;
	}
	
	/**
	 * Gets the HDSInfo.
	 *
	 * @return the HDSInfo
	 */
	protected HashMap<String, ParentCI> getHDSInfo() {
		return null;
	}
	
	/**
	 * Parses the HDSInfo file.
	 *
	 * @param pathToHDSInfoFile the path to HDSInfo file
	 */
	protected void parseHDSInfoFile(final String pathToHDSInfoFile) {
		return;
	}
	
}
