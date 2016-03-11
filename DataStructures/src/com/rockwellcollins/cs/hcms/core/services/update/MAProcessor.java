package com.rockwellcollins.cs.hcms.core.services.update;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.CoreThreadException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.handlers.HandlerPropertyNotFoundException;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerMode;
import com.rockwellcollins.cs.hcms.core.services.update.messages.LAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.MAActionTypes;
import com.rockwellcollins.cs.hcms.core.services.update.messages.MAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;
import com.rockwellcollins.cs.hcms.core.utils.Directory;

/**
 * The Class MAProcessor listens for any state change sent by UI
 * to start the load process. It involves the following stages:
 * - REQUEST - To start the load process
 * - RESPONSE - To get the details of removable media available
 * with "venue" folder in it
 * - VALIDATE - To validate the contents of the media for hardware
 * and LCP compatibility
 * - LOAD - To confirm and start the actual load process
 * - ERROR - To handle any error in the above flow
 * - CANCEL - To cancel the load process
 * 
 * Any LRU that responds first with "venue" folder available in it
 * will be shown to the user. All other reponses will be rejected.
 * 
 * The "venue" folder should be present directly under the root folder
 * of the removable media.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * @see MediaAnnouncer
 * @see MAActionTypes
 * @see LAMessage
 * 
 */
public class MAProcessor {
	
	private UpdateService updateService;
	
	private UpdateHandler updateHandler;
	
	private StateManager stateManager;
	
	private HardwareInfo hardwareDetails;
	
	private MediaAnnouncer mediaAnnouncer;
	
	private Thread mediaAnnouncerThread;
	
	private LoadAnnouncer loadAnnouncer;
	
	private Thread loadAnnouncerThread;
	
	private MAActionTypes currentAction;
	
	private String loadOriginalTime;
	
	private String loadError;
	
	private boolean isResponseForRequestReceived;
	
	private boolean isResponseForLoadReceived;
	
	private HashMap<String, HashMap<String, ParentCI>> ciiFiles;
	
	private String mediaIp;
	
	private String mediaLruType;
	
	private CountdownTimer waitToLoad;
	
	private LAMessage la;
	
	private boolean isLASender;
	
	private boolean isWaiting;
	
	private boolean isMediaLoadCompleted;
	
	private boolean isSWCentreLoad = false;
	
	private HashMap<String, SAMessage> statusTracker;
	
	private List<String> duplicateCIIFiles;
	
	private HashMap<String, ParentCI> parentCIs;
	
	private static final String VENUE_FOLDER = "venue";

	/**
	 * Gets the status tracker.
	 * 
	 * @return the status tracker
	 */
	private HashMap<String, SAMessage> getStatusTracker() {
		if(statusTracker == null) {
			statusTracker = new HashMap<String, SAMessage>();
		}
		return statusTracker;
	}

	/**
	 * Gets the media announcer.
	 * 
	 * @return the media announcer
	 */
	private MediaAnnouncer getMediaAnnouncer() {
		if(mediaAnnouncer == null) {
			mediaAnnouncer = new MediaAnnouncer();
		}
		return mediaAnnouncer;
	}
	
	/**
	 * Gets the load announcer.
	 * 
	 * @return the load announcer
	 */
	private LoadAnnouncer getLoadAnnouncer() {
		if(loadAnnouncer == null) {
			loadAnnouncer = new LoadAnnouncer();
		}
		return loadAnnouncer;
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
	 * Sets the state manager.
	 * 
	 * @param stateManager
	 */
	public void setStateManager(final StateManager stateManager) {
		this.stateManager = stateManager;
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
	 * Sets the update handler.
	 * 
	 * @param updateHandler the new update handler
	 */
	public void setUpdateHandler(final UpdateHandler updateHandler) {
		this.updateHandler = updateHandler;
	}
	
	/**
	 * Sets the parent C is.
	 * 
	 * @param parentCIs the parent C is
	 */
	public void setParentCIs(final HashMap<String, ParentCI> parentCIs) {
		this.parentCIs = parentCIs;
	}
	
	/**
	 * Gets the wait to load.
	 * 
	 * @return the wait to load
	 */
	private CountdownTimer getWaitToLoad() {
		if(waitToLoad == null) {
			waitToLoad = new CountdownTimer();
			waitToLoad.startTimer(0);
		}
		return waitToLoad;
	}

	/**
	 * Processes media load request from the UI.
	 * 
	 * @param action the action
	 * @param value the value
	 */
	public void processMediaLoad(final MAActionTypes action, final boolean value) {
		if(!updateService.isReady()) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Update Service is not yet ready");
			}
			return;
		}
		switch(action) {
			case REQUEST:
				isSWCentreLoad = false;
				processUIRequest(value);
				break;
			case SOFTWARECENTREREQUEST:
				isSWCentreLoad = true;				
				processUIRequest(value);		
				break;
			case VALIDATE:
				processUIValidate(value);
				break;
			case CONFIRM_FILES:
				processUIConfirmFiles(value);
				break;
			case LOAD:
				processUILoad(value);
				break;
			case CANCEL:
				processUICancel(value);
				break;
			default:
				UnitManager.Logging.logSevere("Update Service - MAProcessor: Invalid request from UI");
				break;
		}
	}
	
	/**
	 * Processes request from UI to start the loading process.
	 */
	private void processUIRequest(final boolean value) {
		if(!value) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Nothing to do - Received Start Media Load : false");
			}
			return;
		}
		
		if(isWaiting) {
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Loading is going on");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "Waiting for target");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "LRUs to copy files");
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
			updateHandler.setProperty(propertyMap);
			return;
		}
		
		if(updateService.isInstalling() || !isMediaLoadCompleted) {
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Loading is going on");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "Cannot start another");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "load process");
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
			updateHandler.setProperty(propertyMap);
			return;
		}
		
		if(getMediaAnnouncer().isRunning()) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: MediaAnnouncer is already running. Cannot process media load request.");
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Already a load");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "is going on");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
			updateHandler.setProperty(propertyMap);
			return;
		}
		
		isMediaLoadCompleted = false;
		final PropertyMap propertyMap = new PropertyMap();
		propertyMap.put(UpdateHandler.Properties.CII_FILES_TO_LOAD.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "30000");
		propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
		propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Searching files");
		propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "Please wait");
		propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
		updateHandler.setProperty(propertyMap);

		final MAMessage ma = createRequest();
		getMediaAnnouncer().setMa(ma);
		getMediaAnnouncer().setMaMessageInterval(2000);
		getMediaAnnouncer().setSendLimit(10);
		getMediaAnnouncer().setRunning(true);
		getMediaAnnouncer().setIpAddress(null);
		getMediaAnnouncer().setUpdateService(updateService);
		if(isSWCentreLoad==true){
			currentAction = MAActionTypes.SOFTWARECENTREREQUEST;
		}
		else {
			currentAction = MAActionTypes.REQUEST;
		}
		isResponseForRequestReceived = false;
		ciiFiles = null;
		mediaIp = null;
		mediaLruType = null;
		
		updateService.getQueueProcessor().setTimeout(updateService.getQueueTimeout());
		try {
			mediaAnnouncerThread = UnitManager.Threading.createThread(this, getMediaAnnouncer(), "Update Service - Media Announcer - Request");
			mediaAnnouncerThread.start();
		} catch(CoreThreadException cte) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error while creating MediaAnnouncer thread", cte);
		}
	}
	
	/**
	 * Processes the request from UI to validate the compatibility of the
	 * media files with the installed software/hardware.
	 */
	private void processUIValidate(final boolean value) {
		if(value) {
			if(isMediaLoadCompleted) {
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_LOAD_SCREEN.getPropertyName(), "false");
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Load got");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "cancelled already");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				updateHandler.setProperty(propertyMap);
				return;
			}
			
			if(currentAction.equals(MAActionTypes.RESPONSE)) {
				currentAction = MAActionTypes.VALIDATE;
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_LOAD_SCREEN.getPropertyName(), "false");
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "30000");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Validating files");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "Please wait");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				updateHandler.setProperty(propertyMap);
				
				boolean isSuccess = validateFiles();
				if(isSuccess) {
					checkCompatibility();
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Current action is: " + currentAction + " - Cannot validate files now");
				}
			}
		} else {
			if(!isMediaLoadCompleted) {
				processUICancel(true);
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: UI Validate: false");
					updateService.logInfo("Update Service - MAProcessor: Media load is already completed");
				}
			}
		}
	}
	
	/**
	 * Processes the request from UI for confirming the list
	 * of files to load.
	 * 
	 * @param value
	 */
	private void processUIConfirmFiles(final boolean value) {
		if(value) {
			if(isMediaLoadCompleted) {
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Load got");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "cancelled already");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				updateHandler.setProperty(propertyMap);
				return;
			}
			checkCompatibility();
		} else {
			if(!isMediaLoadCompleted) {
				processUICancel(true);
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: UI Confirm files: false");
					updateService.logInfo("Update Service - MAProcessor: Media load is already completed");
				}
			}
		}
	}
	
	/**
	 * Processes the request from UI for confirming the load.
	 * 
	 * @param value the value
	 */
	private void processUILoad(final boolean value) {
		if(value) {
			if(isMediaLoadCompleted) {
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Load got");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "cancelled already");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				updateHandler.setProperty(propertyMap);
				return;
			}
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "30000");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Initializing to load");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "Please wait");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
			updateHandler.setProperty(propertyMap);

			final MAMessage ma = createLoad();
			getMediaAnnouncer().setMa(ma);
			getMediaAnnouncer().setMaMessageInterval(2000);
			getMediaAnnouncer().setSendLimit(10);
			getMediaAnnouncer().setRunning(true);
			getMediaAnnouncer().setIpAddress(mediaIp);
			getMediaAnnouncer().setUpdateService(updateService);
			currentAction = MAActionTypes.LOAD;
			isResponseForLoadReceived = false;
			
			updateService.getQueueProcessor().setTimeout(updateService.getQueueTimeout());
			try {
				mediaAnnouncerThread = UnitManager.Threading.createThread(this, getMediaAnnouncer(), "Update Service - Media Announcer - Load");
				mediaAnnouncerThread.start();
			} catch(CoreThreadException cte) {
				UnitManager.Logging.logSevere("Update Service - MAProcessor: Error while creating MediaAnnouncer thread", cte);
			}
		} else {
			if(!isMediaLoadCompleted) {
				processUICancel(true);
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: UI Load: false");
					updateService.logInfo("Update Service - MAProcessor: Media load is already completed");
				}
			}
		}
	}
	
	/**
	 * Processes the request from UI for canceling the load.
	 */
	private void processUICancel(final boolean value) {
		if(!value) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Received Cancel Media Load : false");
			}
			return;
		}
		
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service - MAProcessor: Cancelling media load");
		}
		mediaLoadComplete(value);
	}
	
	/**
	 * Creates the request.
	 * 
	 * @return the MA message
	 */
	private MAMessage createRequest() {
		final MAMessage ma = new MAMessage();
		ma.setUpdateService(updateService);
		if(isSWCentreLoad==true){
			if(updateService.isInfo()) {
				updateService.logInfo("createRequest is available in isSWCentreLoad");
			}
			ma.setAction(MAActionTypes.SOFTWARECENTREREQUEST);
		}
		else {
			ma.setAction(MAActionTypes.REQUEST);
		}
		ma.setLruType(hardwareDetails.getDeviceType());
		ma.setLruInstance(updateService.getLruInstance());
		ma.setSourceIp(hardwareDetails.getIpAddress());
		ma.setDestinationIp(updateService.getMulticastIp());
		final long time = UnitManager.Timing.getTimeAlive();
		ma.setOriginalTime(String.valueOf(time));
		ma.setCurrentTime(String.valueOf(time));
		ma.printCurrentValues();
		return ma;
	}
	
	/**
	 * Creates the response.
	 * 
	 * @param destinationIp the destination ip
	 * @param ciiFiles the cii files
	 * @param error the error
	 * 
	 * @return the MA message
	 */
	private MAMessage createResponse(final String destinationIp, final HashMap<String, HashMap<String, ParentCI>> ciiFiles, final String error) {
		final MAMessage ma = new MAMessage();
		ma.setUpdateService(updateService);
		ma.setAction(MAActionTypes.RESPONSE);
		ma.setLruType(hardwareDetails.getDeviceType());
		ma.setLruInstance(updateService.getLruInstance());
		ma.setSourceIp(hardwareDetails.getIpAddress());
		ma.setDestinationIp(destinationIp);
		if(error == null) {
			ma.setCiiFiles(ciiFiles);
		} else {
			ma.setError(error);
		}
		final long time = UnitManager.Timing.getTimeAlive();
		ma.setOriginalTime(String.valueOf(time));
		ma.setCurrentTime(String.valueOf(time));
		ma.printCurrentValues();
		return ma;
	}
	
	/**
	 * Creates the load.
	 * 
	 * @return the MA message
	 */
	private MAMessage createLoad() {
		final MAMessage ma = new MAMessage();
		ma.setUpdateService(updateService);
		ma.setAction(MAActionTypes.LOAD);
		ma.setLruType(hardwareDetails.getDeviceType());
		ma.setLruInstance(updateService.getLruInstance());
		ma.setSourceIp(hardwareDetails.getIpAddress());
		ma.setDestinationIp(mediaIp);
		ma.setCiiFiles(ciiFiles);
		boolean isForceLoad = false;
		try {
			isForceLoad = Boolean.parseBoolean(updateHandler.getProperty(UpdateHandler.Properties.MEDIA_FORCE_LOAD.getPropertyName()));
		} catch (HandlerPropertyNotFoundException hpnfe) {
			UnitManager.Logging.logSevere(hpnfe);
		}
		ma.setForceLoad(isForceLoad);
		ma.setDisableCompatibility(true);
		final long time = UnitManager.Timing.getTimeAlive();
		ma.setOriginalTime(String.valueOf(time));
		ma.setCurrentTime(String.valueOf(time));
		ma.printCurrentValues();
		return ma;
	}
	
	/**
	 * Creates the reboot.
	 * 
	 * @return the MA message
	 */
	private MAMessage createReboot() {
		final MAMessage ma = new MAMessage();
		ma.setUpdateService(updateService);
		ma.setUpdateService(updateService);
		ma.setAction(MAActionTypes.REBOOT);
		ma.setLruType(hardwareDetails.getDeviceType());
		ma.setLruInstance(updateService.getLruInstance());
		ma.setSourceIp(hardwareDetails.getIpAddress());
		ma.setDestinationIp(updateService.getMulticastIp());
		final long time = UnitManager.Timing.getTimeAlive();
		ma.setOriginalTime(String.valueOf(time));
		ma.setCurrentTime(String.valueOf(time));
		ma.printCurrentValues();
		return ma;
	}
	
	/**
	 * Creates the error.
	 * 
	 * @param destinationIp the destination ip
	 * @param error the error
	 * 
	 * @return the MA message
	 */
	private MAMessage createError(final String destinationIp, final String error) {
		final MAMessage ma = new MAMessage();
		ma.setUpdateService(updateService);
		ma.setAction(MAActionTypes.ERROR);
		ma.setLruType(hardwareDetails.getDeviceType());
		ma.setLruInstance(updateService.getLruInstance());
		ma.setSourceIp(hardwareDetails.getIpAddress());
		ma.setDestinationIp(destinationIp);
		ma.setError(error);
		final long time = UnitManager.Timing.getTimeAlive();
		ma.setOriginalTime(String.valueOf(time));
		ma.setCurrentTime(String.valueOf(time));
		ma.printCurrentValues();
		return ma;
	}
	
	/**
	 * Processes the MA message received from other LRUs.
	 * 
	 * @param ma the ma
	 */
	public void processMAMessage(final MAMessage ma) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service - MAProcessor: Received a MA Message: ");
			ma.printCurrentValues();
		}
		final MAActionTypes action = ma.getAction();
		switch(action) {
			case REQUEST:
				processRequest(ma);
				break;
			case SOFTWARECENTREREQUEST:
				if(updateService.isInfo()) {
					updateService.logInfo("SOFTWARECENTREREQUEST received in processMAMessage");
				}
				processSoftwareCentreRequest(ma);
				break;
			case RESPONSE:
				processResponse(ma);
				break;
			case LOAD:
				processLoad(ma);
				break;
			case REBOOT:
				processReboot(ma);
				break;
			case ERROR:
				processError(ma);
				break;
			default:
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Invalid MA message type: " + action);
				}
				break;
		}
	}
	
	public HashMap<String, HashMap<String, ParentCI>> getParentCIs() {
		try {
			final List<String> mediaPathList = updateHandler.getRemovableLoadMedia();
			if(mediaPathList != null) {
				boolean isVenueFolder = false;
				String validMediaPath = null;
				for(final String mediaPath : mediaPathList) {
					if(Directory.isDirectoryGood(mediaPath)) {
						if (mediaPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {
							//Check if the media directory is good, if not continue to next media available
							validMediaPath = mediaPath + VENUE_FOLDER;
						} else {
							validMediaPath = mediaPath + Consts.IOs.FILE_SEPARATOR + VENUE_FOLDER;
						}
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("processRequest:: The directory is invalid" + mediaPath);
						}	
						continue;
					}

					final File venueFolder = new File(validMediaPath);
					if(venueFolder.exists()) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - getParentCIs: Removable media found with " + VENUE_FOLDER + ": " + validMediaPath);
						}
						isVenueFolder = true;
						break;
					}
				}
				if(isVenueFolder) {
					if(duplicateCIIFiles == null) {
						duplicateCIIFiles = new ArrayList<String>();
					}
					duplicateCIIFiles.clear();
					final HashMap<String, ParentCI> parentCIs = parseCIIFiles(validMediaPath);
					
					final HashMap<String, HashMap<String, ParentCI>> localCiiFiles = new HashMap<String, HashMap<String,ParentCI>>();
					if(duplicateCIIFiles.size() == 0) {
						localCiiFiles.put(validMediaPath, parentCIs);
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - getParentCIs: Done getting Parent CIs");
						}
						return localCiiFiles;
					} else {
						final JSONObject jError = new JSONObject();
						try {
							jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
							jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
							jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Duplicate CII present");

							final String ipAddress = hardwareDetails.getIpAddress();
							jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), ipAddress + "-" + updateService.getLruInstance());
							final StringBuilder error = new StringBuilder();
							for(int i = 0;i < duplicateCIIFiles.size();i++) {
								error.append(duplicateCIIFiles.get(i).toUpperCase());
								if(i < duplicateCIIFiles.size() - 1) {
									error.append(",");
								}
							}
							jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), error.toString());
							jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
						} catch (final JSONException je) {
							UnitManager.Logging.logSevere("Update Service - getParentCIs: Not able to create error message", je);
						}
					}
				} else {
					UnitManager.Logging.logWarning("Update Service - getParentCIs: No removable media found with " + VENUE_FOLDER);
				}
			} else {
				UnitManager.Logging.logWarning("Update Service - getParentCIs: No removable media found");
			}
		} catch (final Exception e) {
			UnitManager.Logging.logSevere(e);
		} 
		
		return null;
	}
	
	/**
	 * Processes the MA request sent by the LRU where the user
	 * initiated the load process.
	 * 
	 * @param maRequest the ma request
	 */
	private void processRequest(final MAMessage maRequest) {
		try {
			final List<String> mediaPathList = updateHandler.getRemovableLoadMedia();
			if(mediaPathList != null) {
				boolean isVenueFolder = false;
				String validMediaPath = null;
				for(final String mediaPath : mediaPathList) {
					if(Directory.isDirectoryGood(mediaPath)){
						if (mediaPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {
							//Check if the media directory is good, if not continue to next media available
							validMediaPath = mediaPath + VENUE_FOLDER;
						} else {
							validMediaPath = mediaPath + Consts.IOs.FILE_SEPARATOR + VENUE_FOLDER;
						}
					}else{
						if(updateService.isInfo()) {
							updateService.logInfo("processRequest:: The directory is invalid" + mediaPath);
						}	
						continue;
					}

					final File venueFolder = new File(validMediaPath);
					if(venueFolder.exists()) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - MAProcessor: Removable media found with " + VENUE_FOLDER + ": " + validMediaPath);
						}
						isVenueFolder = true;
						break;
					}
				}
				if(isVenueFolder) {
					if(duplicateCIIFiles == null) {
						duplicateCIIFiles = new ArrayList<String>();
					}
					duplicateCIIFiles.clear();
					final HashMap<String, ParentCI> parentCIs = parseCIIFiles(validMediaPath);
					MAMessage ma = null;
					final HashMap<String, HashMap<String, ParentCI>> ciiFiles = new HashMap<String, HashMap<String,ParentCI>>();
					if(duplicateCIIFiles.size() == 0) {
						ciiFiles.put(validMediaPath, parentCIs);
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - MAProcessor: Sending response");
						}
						ma = createResponse(maRequest.getSourceIp(), ciiFiles, null);
					} else {
						final JSONObject jError = new JSONObject();
						try {
							jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
							jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
							jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Duplicate CII present");

							final String ipAddress = hardwareDetails.getIpAddress();
							jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), ipAddress + "-" + updateService.getLruInstance());
							final StringBuilder error = new StringBuilder();
							for(int i = 0;i < duplicateCIIFiles.size();i++) {
								error.append(duplicateCIIFiles.get(i).toUpperCase());
								if(i < duplicateCIIFiles.size() - 1) {
									error.append(",");
								}
							}
							jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), error.toString());
							jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
						} catch (final JSONException je) {
							UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to create error message", je);
						}
						ma = createResponse(maRequest.getSourceIp(), ciiFiles, jError.toString());
					}
					updateService.send(ma, InetAddress.getByName(maRequest.getSourceIp()), updateService.getPort());
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: No removable media found with " + VENUE_FOLDER);
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: No removable media found");
				}
			}
		} catch (final ServiceIOException sioe) {
			UnitManager.Logging.logSevere(sioe);
		} catch (final UnknownHostException uhe) {
			UnitManager.Logging.logSevere(uhe);
		}
	}
	/**
	 * Processes the MA request sent by the LRU where the user
	 * initiated the load process.
	 * 
	 * @param maRequest the ma request
	 */
	private void processSoftwareCentreRequest(final MAMessage maRequest) {
		if(updateService.isInfo()) {
				updateService.logInfo("!!!! inside processSoftwareCentreRequest !!!!!");
		}
		try {
			final List<String> mediaPathList = updateHandler.getSoftwareCentreLocation();
			if(mediaPathList != null) {
				boolean isValidFolder = false;
				String validMediaPath = null;
				for(final String mediaPath : mediaPathList) {
					if(Directory.isDirectoryGood(mediaPath)){
						if (mediaPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {
							//Check if the media directory is good, if not continue to next media available
							validMediaPath = mediaPath ;
						} else {
							validMediaPath = mediaPath + Consts.IOs.FILE_SEPARATOR ;
						}
						isValidFolder = true;
					}else{
						if(updateService.isInfo()) {
							updateService.logInfo("processRequest:: The directory is invalid" + mediaPath);
						}						
					}
					break;
//					final File venueFolder = new File(validMediaPath);
//					if(venueFolder.exists()) {
//						if(updateService.isInfo()) {
//							updateService.logInfo("Update Service - MAProcessor:  media found with " +  validMediaPath);
//						}
//						isVenueFolder = true;
//						break;
//					}
				}
				if(isValidFolder) {
					if(duplicateCIIFiles == null) {
						duplicateCIIFiles = new ArrayList<String>();
					}
					duplicateCIIFiles.clear();
					final HashMap<String, ParentCI> parentCIs = parseCIIFiles(validMediaPath);
					MAMessage ma = null;
					final HashMap<String, HashMap<String, ParentCI>> ciiFiles = new HashMap<String, HashMap<String,ParentCI>>();
					if(duplicateCIIFiles.size() == 0) {
						ciiFiles.put(validMediaPath, parentCIs);
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - MAProcessor: Sending response");
						}
						if(updateService.isInfo()) {
							updateService.logInfo("!!!!! Creating response !!!!!");
						}
						ma = createResponse(maRequest.getSourceIp(), ciiFiles, null);
						
					} else {
						final JSONObject jError = new JSONObject();
						try {
							jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
							jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
							jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Duplicate CII present");

							final String ipAddress = hardwareDetails.getIpAddress();
							jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), ipAddress + "-" + updateService.getLruInstance());
							final StringBuilder error = new StringBuilder();
							for(int i = 0;i < duplicateCIIFiles.size();i++) {
								error.append(duplicateCIIFiles.get(i).toUpperCase());
								if(i < duplicateCIIFiles.size() - 1) {
									error.append(",");
								}
							}
							jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), error.toString());
							jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
						} catch (final JSONException je) {
							UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to create error message", je);
						}
						ma = createResponse(maRequest.getSourceIp(), ciiFiles, jError.toString());
					}
					updateService.send(ma, InetAddress.getByName(maRequest.getSourceIp()), updateService.getPort());
				} else {					
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: software centre folder not found " + validMediaPath);
					}
				}
			} else {
				if(updateService.isInfo()) {					
					updateService.logInfo("Update Service - MAProcessor: software centre property not found");
				}
			}
		} catch (final ServiceIOException sioe) {
			UnitManager.Logging.logSevere(sioe);
		} catch (final UnknownHostException uhe) {
			UnitManager.Logging.logSevere(uhe);
		}
	}
	/**
	 * Processes the MA response sent by all the LRUs for the available
	 * removable media with "venue" folder in it.
	 * 
	 * @param maResponse the ma response
	 */
	private void processResponse(final MAMessage maResponse) {
		if(isResponseForRequestReceived) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Already got the response");
			}
			return;
		}
		isResponseForRequestReceived = true;
		currentAction = MAActionTypes.RESPONSE;
		getMediaAnnouncer().stop();
		updateService.getQueueProcessor().setTimeout(0);
		
		// Choose the first removable media attached to this LRU
		// The user will not be let to choose between multiple removable media plugged to the same LRU
		// But the MAmessage is capable of storing
			// All the LRUs that has at least one removable media with venue folder plugged to it
				// Can be obtained after receiving all the responses
				// Currently it listens only for the first response
			// All the removable media with venue folder plugged to the same LRU
				// Can be obtained from the below HashMap ciiFiles
		// In future if needed
			// The user can be prompted to choose the LRU first
			// Choose the removable media within the chosen LRU
		mediaIp = maResponse.getSourceIp();
		mediaLruType = maResponse.getLruType();
		ciiFiles = maResponse.getCiiFiles();
		final String error = maResponse.getError();
		if(error != null && error.length() > 0) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error at: " + mediaIp + ": " + error);
			mediaLoadComplete(false);
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.START_MEDIA_LOAD.getPropertyName(), "false");
			propertyMap.put(UpdateHandler.Properties.START_SWCENTRE_LOAD.getPropertyName(), "false");
			JSONObject jError = null;
			try {
				jError = new JSONObject(error);
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), jError.optString(UpdateHandler.Properties.TITLE.getPropertyName()));
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_1.getPropertyName()));
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_2.getPropertyName()));
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_3.getPropertyName()));
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_4.getPropertyName()));
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_5.getPropertyName()));
			} catch (final JSONException je) {
				UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to read error message", je);
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Error in response");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "IP: " + mediaIp);
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "Unit: " + maResponse.getLruInstance());
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
			}
			updateHandler.setProperty(propertyMap);
		} else {
			if(ciiFiles != null && ciiFiles.size() > 0) {
				final Iterator<String> mediaPaths = ciiFiles.keySet().iterator();
				final String mediaPath = mediaPaths.next();
				final HashMap<String, ParentCI> parentCIs = ciiFiles.get(mediaPath);
				if(parentCIs == null || parentCIs.size() <= 0) {
					UnitManager.Logging.logSevere("Update Service - MAProcessor: No contents under venue folder at: " + mediaPath);
					mediaLoadComplete(false);
					final PropertyMap propertyMap = new PropertyMap();
					propertyMap.put(UpdateHandler.Properties.START_MEDIA_LOAD.getPropertyName(), "false");
					propertyMap.put(UpdateHandler.Properties.START_SWCENTRE_LOAD.getPropertyName(), "false");
					propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
					propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
					propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
					propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "No contents under");
					propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "venue folder at");
					propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "IP: " + mediaIp);
					propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "Unit: " + maResponse.getLruInstance());
					updateHandler.setProperty(propertyMap);
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: CII files present under venue folder at: " + mediaIp + ": " + mediaPath);
					}
					setCIIFilesToLoad(parentCIs, maResponse);
				}
			} else {
				UnitManager.Logging.logSevere("Update Service - MAProcessor: Received response do not have any media information");
				mediaLoadComplete(false);
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.START_MEDIA_LOAD.getPropertyName(), "false");
				propertyMap.put(UpdateHandler.Properties.START_SWCENTRE_LOAD.getPropertyName(), "false");
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Do not have any media");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "information in the");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "response received from");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), mediaIp + "-" + maResponse.getLruInstance());
				updateHandler.setProperty(propertyMap);
			}
		}
	}
	
	/**
	 * Sets the CII files to load.
	 * 
	 * @param parentCIs the parent CIs
	 * @param maResponse the ma response
	 */
	private void setCIIFilesToLoad(final HashMap<String, ParentCI> parentCIs, final MAMessage maResponse) {
		String ciiFilesToLoad = updateHandler.convertCIIFilesToStateValue(parentCIs);
		if(ciiFilesToLoad == null || ciiFilesToLoad.length() <= 0) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: The contents under venue folder are not good");
			mediaLoadComplete(false);
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.START_MEDIA_LOAD.getPropertyName(), "false");
			propertyMap.put(UpdateHandler.Properties.START_SWCENTRE_LOAD.getPropertyName(), "false");
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "The contents under");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "venue folder ");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "are not good");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "IP: " + mediaIp);
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "Unit: " + maResponse.getLruInstance());
			updateHandler.setProperty(propertyMap);
		} else {
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.START_MEDIA_LOAD.getPropertyName(), "false");
			propertyMap.put(UpdateHandler.Properties.START_SWCENTRE_LOAD.getPropertyName(), "false");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_LOAD_SCREEN.getPropertyName(), "true");
			propertyMap.put(UpdateHandler.Properties.CII_FILES_TO_LOAD.getPropertyName(), ciiFilesToLoad);
			updateHandler.setProperty(propertyMap);
		}
	}
	
	/**
	 * Validates the list of files to load. If MCD
	 * is present in the list and if the media is 
	 * inserted into PSW, then MCD cannot be loaded.
	 */
	private boolean validateFiles() {
		final Iterator<String> mediaPaths = ciiFiles.keySet().iterator();
		final String mediaPath = mediaPaths.next();
		final HashMap<String, ParentCI> newParentCIs = ciiFiles.get(mediaPath);
		
		// MCD is a special case
		// If MCD CII is present in the media to load
		// And the LRU that holds the removable media is not MCD
		// Then loading cannot be done
		// MCD needs the removable media itself to do the load
		// MCD will not copy the contents to its local hard drive
		
		if(newParentCIs.containsKey(UpdateService.MCD) && !mediaLruType.equals(UpdateService.MCD)) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Media contains " + UpdateService.MCD + " inserted at: " + mediaLruType);
			}

			newParentCIs.remove(UpdateService.MCD);
			if(newParentCIs.size() > 0) {
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_1.getPropertyName(), "MCD cannot be ");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_2.getPropertyName(), "loaded from: " + mediaLruType.toUpperCase());
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_3.getPropertyName(), "Continue loading");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_4.getPropertyName(), "the other files?");
				propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_CONFIRM_FILES_SCREEN.getPropertyName(), "true");
				updateHandler.setProperty(propertyMap);
			} else {
				mediaLoadComplete(false);
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "MCD cannot be");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "loaded from: " + mediaLruType.toUpperCase() + ".");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				updateHandler.setProperty(propertyMap);
			}
			return false;
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: CII files present under venue folder at: " + mediaIp + ": " + mediaPath);
			}
			return true;
		}
	}
	
	/**
	 * Checks compatibility.
	 */
	private void checkCompatibility() {
		PropertyMap propertyMap = new PropertyMap();
		propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "30000");
		propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
		propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Checking compatibility");
		propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "Please wait");
		propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
		updateHandler.setProperty(propertyMap);
		
		final Iterator<String> mediaPaths = ciiFiles.keySet().iterator();
		final String mediaPath = mediaPaths.next();
		final HashMap<String, ParentCI> newParentCIs = ciiFiles.get(mediaPath);
		
		boolean isLru = false;
		boolean isLcp = newParentCIs.containsKey(updateService.getLCPType());
		
		if((isLcp && newParentCIs.size() > 1) || (!isLcp && newParentCIs.size() > 0)) {
			isLru = true;
		}
		
		if (isLcp && !isLru) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Only LCP needs to be loaded");
			}
			final ParentCI lcpParentCI = newParentCIs.get(updateService.getLCPType());
			boolean isLcpCompatible = checkLcpCompatibility(lcpParentCI);
			if(!isLcpCompatible) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: LCP incompatibility warning");
				}
				propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_1.getPropertyName(), "Software Load Warning");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_2.getPropertyName(), "LCP incompatibility");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_3.getPropertyName(), "Confirm to load");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_CONFIRM_SCREEN.getPropertyName(), "true");
				updateHandler.setProperty(propertyMap);
				return;
			}
		} else if (isLru) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: LRU Software needs to be loaded");
			}
			boolean isHardwareCompatible = checkHardwareCompatibility(newParentCIs);
			if(!isHardwareCompatible) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Hardware incompatibility warning");
				}
				propertyMap = new PropertyMap();
				propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_1.getPropertyName(), "Software Load Warning");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_2.getPropertyName(), "Hardware Incompatibility");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_3.getPropertyName(), "Confirm to load");
				propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_4.getPropertyName(), "");
				propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_CONFIRM_SCREEN.getPropertyName(), "true");
				updateHandler.setProperty(propertyMap);
				return;
			}
		}

		if(updateService.isInfo()) {
			updateService.logInfo("Update Service - MAProcessor: No warning");
		}
		propertyMap = new PropertyMap();
		propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
		propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_1.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_2.getPropertyName(), "Confirm to load");
		propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_3.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.CONFIRM_TEXT_4.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_CONFIRM_SCREEN.getPropertyName(), "true");
		updateHandler.setProperty(propertyMap);
	}
	
	/**
	 * Checks hardware compatibility.
	 * 
	 * @param newParentCIs the new parent C is
	 * 
	 * @return true, if successful
	 */
	private boolean checkHardwareCompatibility(final HashMap<String, ParentCI> newParentCIs) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Checking hardware compatibility");
		}
		
		for(final Entry<String, ParentCI> newParentCIEntry : newParentCIs.entrySet()) {
			final String lruType = newParentCIEntry.getKey();
			final ParentCI newParentCI = newParentCIEntry.getValue();
			
			if(lruType.equals(updateService.getLCPType())) {
				continue;
			}
			
			if(newParentCI == null) {
				UnitManager.Logging.logSevere("Update Service: New Parent CI is null for: " + lruType);
				UnitManager.Logging.logSevere("Update Service: Returns as hardware incompatible");
				return false;
			}
			
			final List<String> hardwarePartNumbers = newParentCI.getHardwarePartNumbers();
			if(hardwarePartNumbers != null && hardwarePartNumbers.size() > 0) {
				final HashMap<String, List<String>> hwPartNumbers = updateService.getHwPartNumbers();
				if(hwPartNumbers == null || hwPartNumbers.size() < 0) {
					UnitManager.Logging.logSevere("Update Service: Installed hardware part numbers are not available for any LRU");
					UnitManager.Logging.logSevere("Update Service: Returns as hardware incompatible");
					return false;
				}
				
				final List<String> localHwPartNumbers = hwPartNumbers.get(lruType);
				
				if(localHwPartNumbers == null || localHwPartNumbers.size() < 0) {
					UnitManager.Logging.logSevere("Update Service: Installed hardware part numbers are not available for: " + lruType);
					UnitManager.Logging.logSevere("Update Service: Returns as hardware incompatible");
					return false;
				}

				for(final String localHwPartNumber : localHwPartNumbers) {
					if(!hardwarePartNumbers.contains(localHwPartNumber)) {
						UnitManager.Logging.logSevere("Update Service: Installed Hardware: " + localHwPartNumber + " is not present in the hardware part number list");
						return false;
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: New software is compatible for all hardware: " + lruType);
				}
			}
		}
		return true;
	}
	
	/**
	 * Checks lcp compatibility.
	 * 
	 * @param lcpParentCI the lcp parent CI
	 * 
	 * @return true, if successful
	 */
	private boolean checkLcpCompatibility(ParentCI lcpParentCI) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Checking LCP compatibility");
		}
		
		if(lcpParentCI == null) {
			UnitManager.Logging.logSevere("Update Service: The new LCP Parent CI is not available");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		
		final String deviceType = hardwareDetails.getDeviceType();
		if(parentCIs == null || parentCIs.size() <= 0) {
			UnitManager.Logging.logSevere("Update Service: There are no installed Parent CIs available");
			UnitManager.Logging.logSevere("Update Service: Cannot check the LCP compatibility");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		final ParentCI parentCI = parentCIs.get(deviceType);
		if(parentCI == null) {
			UnitManager.Logging.logSevere("Update Service: There is no installed Parent CI available for: " + deviceType);
			UnitManager.Logging.logSevere("Update Service: Cannot check the LCP compatibility");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		final String expected = lcpParentCI.getReleaseNumber();
		final String actual = parentCI.getReleaseNumber();
		
		if(expected == null || actual == null) {
			UnitManager.Logging.logSevere("Update Service: Release number(s) are null");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		
		int eLastIndexOfDot = expected.lastIndexOf(".");
		int aLastIndexOfDot = actual.lastIndexOf(".");
		
		if(eLastIndexOfDot <= 0 || aLastIndexOfDot <= 0) {
			UnitManager.Logging.logSevere("Update Service: Release numbers are not in expected format");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		
		String expectedSub = expected.substring(0, eLastIndexOfDot);
		String actualSub = actual.substring(0, aLastIndexOfDot);
		int exceptedNumber = 0;
		try {
			exceptedNumber = Integer.parseInt(expectedSub.substring(0, 1));
		} catch(NumberFormatException nfe) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Cannot convert the release number: " + expected + " to an int");
			}
		}
		// Backward compatibility with Release 3.0 and above
		if(!expectedSub.equals(actualSub) && exceptedNumber <= 2) {
			UnitManager.Logging.logSevere("Update Service: The new LCP is incompatible with the installed software: " + deviceType);
			UnitManager.Logging.logSevere("Update Service: Expected: " + expectedSub + " : Actual: " + actualSub);
			return false;
		}
		return true;
	}
	
	/**
	 * Processes the request to start the LA Message sent by 
	 * the LRU where the user browses the UI and confirmed the
	 * load.
	 * 
	 * @param maLoad the ma load
	 */
	private void processLoad(final MAMessage maLoad) {
		try {
			boolean isNewLoad = false;
			if(maLoad.getOriginalTime() == null || maLoad.getCurrentTime() == null) {
				UnitManager.Logging.logSevere("Update Service - MAProcessor: Cannot process MA Load as original and current time are null");
				final JSONObject jError = new JSONObject();
				try {
					jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
					jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
					jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Cannot process MA Load");
					jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "as original and/or");
					jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "current time is null");
					jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
				} catch (final JSONException je) {
					UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to create error message", je);
				}
				loadError = jError.toString();
				final MAMessage ma = createError(maLoad.getSourceIp(), loadError);
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Sending error message");
				}
				updateService.send(ma, InetAddress.getByName(maLoad.getSourceIp()), updateService.getPort());
				return;
			} else if(maLoad.getOriginalTime().equals(maLoad.getCurrentTime())) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Received a new MA - Load message");
				}
				isNewLoad = true;
			} else if(loadOriginalTime != null && loadOriginalTime.equals(maLoad.getOriginalTime())) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Already processed the MA - Load message");
				}
				isNewLoad = false;
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Missed the MA - Load message earlier and received it now");
				}
				isNewLoad = true;
			}
			
			if(isNewLoad) {
				loadError = null;
				loadOriginalTime = maLoad.getOriginalTime();
				final HashMap<String, HashMap<String, ParentCI>> ciiFiles = maLoad.getCiiFiles();
				if(ciiFiles == null || ciiFiles.size() == 0) {
					UnitManager.Logging.logSevere("Update Service - MAProcessor: Loading cannot be started as no CII file is available");
					final JSONObject jError = new JSONObject();
					try {
						jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
						jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
						jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Loading cannot be");
						jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "started as no CII");
						jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "file is available");
						jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
					} catch (final JSONException je) {
						UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to create error message", je);
					}
					loadError = jError.toString();
					final MAMessage ma = createError(maLoad.getSourceIp(), loadError);
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Sending error message");
					}
					updateService.send(ma, InetAddress.getByName(maLoad.getSourceIp()), updateService.getPort());
					return;
				} else if(ciiFiles.size() > 1) {
					UnitManager.Logging.logSevere("Update Service - MAProcessor: Loading cannot be started as multiple media are available");
					final JSONObject jError = new JSONObject();
					try {
						jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
						jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
						jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Loading cannot be");
						jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "started as multiple");
						jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "media are available");
						jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
					} catch (final JSONException je) {
						UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to create error message", je);
					}
					loadError = jError.toString();
					final MAMessage ma = createError(maLoad.getSourceIp(), loadError);
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Sending error message");
					}
					updateService.send(ma, InetAddress.getByName(maLoad.getSourceIp()), updateService.getPort());
					return;
				} else {
					final Iterator<Entry<String, HashMap<String, ParentCI>>> mediaPathsEntry = ciiFiles.entrySet().iterator();
					final Entry<String, HashMap<String, ParentCI>> mediaPathEntry = mediaPathsEntry.next();
					final String mediaPath = mediaPathEntry.getKey();
					final HashMap<String, ParentCI> parentCIs = mediaPathEntry.getValue();
					if(parentCIs == null || parentCIs.size() <= 0) {
						UnitManager.Logging.logSevere("Update Service - MAProcessor: Loading cannot be started as no CII files available at: " + mediaPath);
						final JSONObject jError = new JSONObject();
						try {
							jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
							jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
							jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Loading cannot be");
							jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "started as no CII");
							jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "files available at");
							jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), mediaPath);
						} catch (final JSONException je) {
							UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to create error message", je);
						}
						loadError = jError.toString();
						final MAMessage ma = createError(maLoad.getSourceIp(), loadError);
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - MAProcessor: Sending error message");
						}
						updateService.send(ma, InetAddress.getByName(maLoad.getSourceIp()), updateService.getPort());
						return;
					}
					
					for(final Entry<String, ParentCI> parentCIsEntry : parentCIs.entrySet()) {
						final ParentCI parentCI = parentCIsEntry.getValue();
						String loadableFilePath = null;
						if(mediaPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {
							loadableFilePath = mediaPath + parentCI.getParent811FileName();
						}
						else {
							loadableFilePath = mediaPath + Consts.IOs.FILE_SEPARATOR + parentCI.getParent811FileName();
						}
						final File loadableFile = new File(loadableFilePath);
						if(!loadableFile.exists()) {
							UnitManager.Logging.logSevere("Update Service - MAProcessor: Loading cannot be started as enough loadable files are not available in the media path: " + mediaPath);
							final JSONObject jError = new JSONObject();
							try {
								jError.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
								jError.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
								jError.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Loading cannot be started");
								jError.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "as enough loadable files");
								jError.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "are not available at");
								jError.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), mediaPath);
							} catch (final JSONException je) {
								UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to create error message", je);
							}
							loadError = jError.toString();
							final MAMessage ma = createError(maLoad.getSourceIp(), loadError);
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service - MAProcessor: Sending error message");
							}
							updateService.send(ma, InetAddress.getByName(maLoad.getSourceIp()), updateService.getPort());
							return;
						}
					}
					
					la = createLA(mediaPath, parentCIs, maLoad.isDisableCompatibility(), maLoad.isForceLoad());
					updateService.getQueueProcessor().setTimeout(updateService.getQueueTimeout());
					// Set the state manager to local mode before starting the installation
					// This avoids any other LRU going to the LOCAL mode if the current LRU
					// is taking more time to install
					if(stateManager != null) {
						if(stateManager.getMode() != StateManagerMode.LOCAL) {
							stateManager.setMode(StateManagerMode.LOCAL);
						}
					}
					startLoadAnnouncer(la);
					loadError = null;
				}
			} else {
				if(loadError != null) {
					final MAMessage ma = createError(maLoad.getSourceIp(), loadError);
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Sending error message");
					}
					updateService.send(ma, InetAddress.getByName(maLoad.getSourceIp()), updateService.getPort());
				}
			}
		} catch (final ServiceIOException sioe) {
			UnitManager.Logging.logSevere(sioe);
		} catch (final UnknownHostException uhe) {
			UnitManager.Logging.logSevere(uhe);
		}
	}
	
	/**
	 * Parses the CII files.
	 * 
	 * @param mediaPath the media path
	 * 
	 * @return the hash map< string, parent C i>
	 */
	private HashMap<String, ParentCI> parseCIIFiles(final String mediaPath) {
		final HashMap<String, ParentCI> parentCIs = new HashMap<String, ParentCI>();
		String ciiFilePath = null;
		try {
			final List<String> ciiFiles = updateHandler.listFiles(mediaPath);

			if (ciiFiles != null) {
				for (final String fileName : ciiFiles) {
					if (fileName.endsWith(".cii")) {
						if (mediaPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {
							ciiFilePath = mediaPath + fileName;
						} else {
							ciiFilePath = mediaPath + Consts.IOs.FILE_SEPARATOR + fileName;
						}

						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - MAProcessor: CII File Path: " + ciiFilePath);
						}

						final File ciiFile = new File(ciiFilePath);

						final ParentCI parentCI = updateService.getCiiParser().parseCII(ciiFile);
						if(parentCIs.containsKey(parentCI.getLruType())) {
							duplicateCIIFiles.add(parentCI.getLruType());
						} else {
							parentCIs.put(parentCI.getLruType(), parentCI);
						}
					}
				}
			}
		}
		catch (final ParserConfigurationException pce) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error parsing the CII File: " + ciiFilePath, pce);
		}
		catch (final SAXException saxe) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error parsing the CII File: " + ciiFilePath, saxe);
		}
		catch(final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error parsing the CII File: " + ciiFilePath, ioe);
		}
		catch(final UpdateServiceException use) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error parsing the CII File: " + ciiFilePath, use);
		}
		return parentCIs;
	}
	
	/**
	 * Creates the LA.
	 * 
	 * @param mediaPath the media path
	 * @param parentCIs the parent C is
	 * @param isDisableCompatibility the is disable compatibility
	 * 
	 * @return the LA message
	 */
	private LAMessage createLA(final String mediaPath, final HashMap<String, ParentCI> parentCIs, boolean isDisableCompatibility, boolean forceLoadFlag) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service - MAProcessor: Creating LA Message");
		}
		
		final LAMessage la = new LAMessage();
		
		la.setUpdateService(updateService);
		la.setParentCIs(parentCIs);
		la.setForceLoadFlag(forceLoadFlag);
		la.setSelfLoadDisableFlag(false);
		la.setLruIpAddress("");
		final long time = UnitManager.Timing.getTimeAlive();
		la.setOriginalTime(String.valueOf(time));
		la.setCurrentTime(String.valueOf(time));
		la.setPathToCII(mediaPath);
		la.setPathToLoadable(mediaPath);
		la.setFtpIpAddress(updateHandler.getFtpIpAddress());
		la.setFtpPortNumber(updateHandler.getFtpPortNumber());
		la.setFtpUsername(updateHandler.getFtpUsername());
		la.setFtpPassword(updateHandler.getFtpPassword());
		if(!isSWCentreLoad){
			la.setNfs(true);
		}
		la.setDisableCompatibility(isDisableCompatibility);
		la.setRebootWait(true);
		la.printCurrentValues();
		return la;
	}
	
	/**
	 * Start load announcer.
	 * 
	 * @param la the la
	 */
	private void startLoadAnnouncer(final LAMessage la) {
		isLASender = true;
		getStatusTracker().clear();
		getWaitToLoad().startTimer(updateService.getLaWaitTime());

		getLoadAnnouncer().setLa(la);
		getLoadAnnouncer().setLaMessageInterval(300);
		getLoadAnnouncer().setSendLimit(20);
		getLoadAnnouncer().setRunning(true);
		getLoadAnnouncer().setUpdateService(updateService);
		try {
			loadAnnouncerThread = UnitManager.Threading.createThread(this, getLoadAnnouncer(), "Update Service - Load Announcer");
			loadAnnouncerThread.start();
		} catch(CoreThreadException cte) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error while creating MediaAnnouncer thread", cte);
		}
	}
	
	/**
	 * Processes the MA Reboot message sent by the host LRU.
	 * 
	 * @param maReboot
	 */
	private void processReboot(final MAMessage maReboot) {
		updateService.setRebootReceived(true);
		synchronized(updateService.getWaitForReboot()) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Received REBOOT command and notifying");
			}
			updateService.getWaitForReboot().notifyAll();
		}
	}

	/**
	 * Processes the MA Error message sent by other LRUs.
	 * 
	 * @param maError the ma error
	 */
	private void processError(final MAMessage maError) {
		if(isResponseForLoadReceived) {
			updateService.logInfo("Update Service - MAProcessor: Already got the response for LOAD");
			return;
		}
		isResponseForLoadReceived = true;
		getMediaAnnouncer().stop();
		UnitManager.Logging.logSevere("Update Service - MAProcessor: ERROR while attempting to LOAD from: " + maError.getSourceIp() + " : " + maError.getError());
		mediaLoadComplete(false);
		final PropertyMap propertyMap = new PropertyMap();
		JSONObject jError = null;
		try {
			jError = new JSONObject(maError.getError());
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Not able to read error message", je);
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Error in response");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "to start the load");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), maError.getSourceIp() + "-" + maError.getLruInstance());
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
		}
		if (jError != null) {
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), jError.optString(UpdateHandler.Properties.TITLE.getPropertyName()));
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_1.getPropertyName()));
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_2.getPropertyName()));
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_3.getPropertyName()));
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_4.getPropertyName()));
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), jError.optString(UpdateHandler.Properties.TEXT_5.getPropertyName()));
			updateHandler.setProperty(propertyMap);
		}
	}
	
	/**
	 * Process timeout.
	 */
	public void processTimeout() {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service - MAProcessor: Timeout: Is LA Sender: " + isLASender);
		}
		if(mediaAnnouncerThread != null && !mediaAnnouncerThread.isAlive()) {
			if(currentAction == MAActionTypes.REQUEST) {
				processNoResponseForRequest();
			} else if(currentAction == MAActionTypes.LOAD) {
				processNoResponseForLoad();
			}
		}
		
		if(isLASender) {
			processLoadTimeout();
		}
	}
	
	/**
	 * Processes no response for requesting the details of available
	 * removable media.
	 */
	private void processNoResponseForRequest() {
		if(isResponseForRequestReceived) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Already got the response for REQUEST");
			}
			return;
		}
		isResponseForRequestReceived = true;
		updateService.getQueueProcessor().setTimeout(0);
		UnitManager.Logging.logSevere("Update Service - MAProcessor: No LRU has a removable media with " + VENUE_FOLDER + " available to start the load");
		mediaLoadComplete(false);
		final PropertyMap propertyMap = new PropertyMap();
		propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
		propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
		propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "No removable media");
		propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "available with");
		propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "venue folder");
		propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
		updateHandler.setProperty(propertyMap);
	}
	
	/**
	 * Processes no response for requesting to start the load
	 * announcement.
	 */
	private void processNoResponseForLoad() {
		if(isResponseForLoadReceived) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: Already got the response for LOAD");
			}
			return;
		}
		isResponseForLoadReceived = true;
		updateService.getQueueProcessor().setTimeout(0);
		UnitManager.Logging.logSevere("Update Service - MAProcessor: LOAD timeout - Failed to start the LOAD from: " + mediaIp);
		isWaiting = false;
		mediaLoadComplete(false);
		final PropertyMap propertyMap = new PropertyMap();
		propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
		propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load Error");
		propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Failed to start");
		propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "the load from");
		propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), mediaIp);
		propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
		updateHandler.setProperty(propertyMap);
	}
	
	/**
	 * Process LA message.
	 * 
	 * @param la the la
	 * 
	 * @return true, if successful
	 */
	public boolean processLAMessage(final LAMessage la) {
		if(currentAction == MAActionTypes.LOAD) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service - MAProcessor: LA started from: " + mediaIp);
			}
			isResponseForLoadReceived = true;
			getMediaAnnouncer().stop();
			
			if(!isLASender) {
				updateService.getQueueProcessor().setTimeout(0);
			} else {
				isWaiting = true;
			}
			mediaLoadComplete(false);
			final PropertyMap propertyMap = new PropertyMap();
			propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "Software Load");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "Loading started");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
			updateHandler.setProperty(propertyMap);
		}
		
		return true;
	}
	
	/**
	 * Process SA message.
	 * 
	 * @param sa the sa
	 */
	public void processSAMessage(final SAMessage sa) {
		if(isLASender) {
			if(loadAnnouncerThread != null && loadAnnouncerThread.isAlive()) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Adding status: " + sa.getIpAddress() + " : " + sa.getStepNumber());
				}
				getStatusTracker().put(sa.getIpAddress(), sa);
			} else {
				if(getStatusTracker().containsKey(sa.getIpAddress())) {
					getStatusTracker().put(sa.getIpAddress(), sa);
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Rejecting status as it was received after LoadAnnouncer timeout: " + sa.getIpAddress());
					}
				}
			}
		}
	}
	
	/**
	 * Processes the load timeout when other LRUs have either
	 * finished copying the files from the media or got timed 
	 * out.
	 */
	private void processLoadTimeout() {
		if(isLASender) {
			if(loadAnnouncerThread != null && !loadAnnouncerThread.isAlive()) {
				boolean copyProgress = checkCopyProgress();
				if(copyProgress) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Wait time over - Ready to reboot: Copy complete: " + copyProgress);
					}
					startReboot();
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Waiting for others to copy the files - Cannot reboot yet");
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Still sending LA Message");
				}
			}
		}
	}
	
	/**
	 * Check copy progress.
	 * 
	 * @return true, if successful
	 */
	private boolean checkCopyProgress() {
		// Check local unit status first
		// If local unit is still copying, then return false
		
		SAMessage saLocal = getStatusTracker().get(hardwareDetails.getIpAddress());
		if(saLocal != null) {
			if(saLocal.getStepNumber() == 0) {
				if(!saLocal.getSubStepName().startsWith(LAProcessor.LA_PROCESS_COMPLETE)) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Local: Still LA processing is going on at: " + saLocal.getIpAddress() + ": " + saLocal.getStepNumber());
					}
					return false;
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Local: LA processing completed at: " + saLocal.getIpAddress() + ": " + saLocal.getStepNumber());
					}
				}
			} else if(saLocal.getStepNumber() == saLocal.getTotalSteps()) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Local: Copy completed at: " + saLocal.getIpAddress() + ": " + saLocal.getStepNumber());
				}
			} else if(saLocal.getStepNumber() == 1) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Local: Still copying is going on at: " + saLocal.getIpAddress() + ": " + saLocal.getStepNumber());
				}
				return false;
			} else if(saLocal.getStepNumber() > 1) {
				if(saLocal.getSubStepName().startsWith(Installer.REBOOT_STEP)) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Local: Copy completed at: " + saLocal.getIpAddress() + ": " + saLocal.getStepNumber());
					}
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Local: Still copying is going on at: " + saLocal.getIpAddress() + ": " + saLocal.getStepNumber());
					}
					return false;
				}
			}
		}
		
		// Check everyone's status
		// If any unit is still copying, then return false
		
		for(final Entry<String, SAMessage> statusEntry : getStatusTracker().entrySet()) {
			final SAMessage sa = statusEntry.getValue();
			
			if(sa.getStepNumber() == 0) {
				if(!sa.getSubStepName().startsWith(LAProcessor.LA_PROCESS_COMPLETE)) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: Still LA processing is going on at: " + sa.getIpAddress() + ": " + sa.getStepNumber());
					}
					return false;
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - MAProcessor: LA processing completed at: " + sa.getIpAddress() + ": " + sa.getStepNumber());
					}
				}
			} else if((sa.getStepNumber() == sa.getTotalSteps()) || sa.getStepNumber() > 1) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Copy completed at: " + sa.getIpAddress() + ": " + sa.getStepNumber());
				}
			} else if(sa.getStepNumber() == 1) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service - MAProcessor: Still copying is going on at: " + sa.getIpAddress() + ": " + sa.getStepNumber());
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Start load.
	 */
	private void startReboot() {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service - MAProcessor: Starting to send REBOOT command");
		}		
		if(isSWCentreLoad){
			if(updateService.isInfo()) {
				updateService.logInfo("!!!!!Inside isSWCentreLoad!!!!!!!!");
			}
			deleteSoftwareCentreFiles();
		}
		mediaLoadComplete(false);
		isLASender = false;
		isWaiting = false;
		updateService.getQueueProcessor().setTimeout(0);
		final MAMessage ma = createReboot();
		getMediaAnnouncer().setMa(ma);
		getMediaAnnouncer().setMaMessageInterval(updateService.getRebootCommandFrequency());
		getMediaAnnouncer().setSendLimit(updateService.getRebootCommandSendLimit());
		getMediaAnnouncer().setRunning(true);
		getMediaAnnouncer().setIpAddress(null);
		getMediaAnnouncer().setUpdateService(updateService);
		currentAction = MAActionTypes.REBOOT;
		if(updateService.isInfo()) {
			ma.printCurrentValues();
		}
		try {
			mediaAnnouncerThread = UnitManager.Threading.createThread(this, getMediaAnnouncer(), "Update Service - Media Announcer - Reboot");
			mediaAnnouncerThread.start();
		} catch(CoreThreadException cte) {
			UnitManager.Logging.logSevere("Update Service - MAProcessor: Error while creating MediaAnnouncer thread", cte);
		}
	}
	
	/**
	 * Cleans up once the media load is completed.
	 */
	private void mediaLoadComplete(final boolean isResetText) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service - MAProcessor: Media Load completed");
		}
		getMediaAnnouncer().stop();
		currentAction = MAActionTypes.NONE;
		getStatusTracker().clear();
		if(ciiFiles != null) {
			ciiFiles.clear();
		}
		mediaIp = "";
		mediaLruType = "";
		if(!isWaiting) {
			updateService.getQueueProcessor().setTimeout(0);
		}
		
		isMediaLoadCompleted = true;
		final PropertyMap propertyMap = new PropertyMap();
		propertyMap.put(UpdateHandler.Properties.DISABLE_BUTTONS.getPropertyName(), "0");
		propertyMap.put(UpdateHandler.Properties.START_MEDIA_LOAD.getPropertyName(), "false");
		propertyMap.put(UpdateHandler.Properties.START_SWCENTRE_LOAD.getPropertyName(), "false");		
		propertyMap.put(UpdateHandler.Properties.CII_FILES_TO_LOAD.getPropertyName(), "");
		propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_LOAD_SCREEN.getPropertyName(), "false");
		propertyMap.put(UpdateHandler.Properties.VALIDATE_MEDIA_LOAD.getPropertyName(), "false");
		propertyMap.put(UpdateHandler.Properties.CONFIRM_FILES_TO_LOAD.getPropertyName(), "false");
		propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_CONFIRM_SCREEN.getPropertyName(), "false");
		propertyMap.put(UpdateHandler.Properties.NAVIGATE_TO_CONFIRM_FILES_SCREEN.getPropertyName(), "false");
		propertyMap.put(UpdateHandler.Properties.CONFIRM_MEDIA_LOAD.getPropertyName(), "false");
		propertyMap.put(UpdateHandler.Properties.CANCEL_MEDIA_LOAD.getPropertyName(), "false");
		if(isResetText) {
			propertyMap.put(UpdateHandler.Properties.TITLE.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_1.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_2.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_3.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_4.getPropertyName(), "");
			propertyMap.put(UpdateHandler.Properties.TEXT_5.getPropertyName(), "");
		}
		Iterator<Entry<String, String>> itr = propertyMap.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, String> entry = itr.next();
			final String propertyName = entry.getKey();
			final String propertyValue = entry.getValue();
			if(updateHandler.hasProperty(propertyName)) {
				try {
					final String oldPropertyValue = updateHandler.getProperty(propertyName);
					if(oldPropertyValue != null && propertyValue.equals(oldPropertyValue)) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - MAProcessor: Old and current values are same");
							updateService.logInfo("Update Service - MAProcessor: Property: " + propertyName + " Value: " + propertyValue);
						}
						itr.remove();
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - MAProcessor: Old and current values are not same");
							updateService.logInfo("Update Service - MAProcessor: Property: " + propertyName + " Old Value: " + oldPropertyValue);
							updateService.logInfo("Update Service - MAProcessor: Property: " + propertyName + " New Value: " + propertyValue);
						}
					}
				} catch (HandlerPropertyNotFoundException hpnfe) {
					UnitManager.Logging.logSevere("Update Service - MAProcessor: Property not found: " + propertyName, hpnfe);
					itr.remove();
				}
			} else {
				UnitManager.Logging.logSevere("Update Service - MAProcessor: Property not found: " + propertyName);
				itr.remove();
			}
		}
		if(propertyMap.size() > 0) {
			updateHandler.setProperty(propertyMap);
		}
	}
	/**
	 * Deletes the files from software center. 
	 */
	private void deleteSoftwareCentreFiles() {
		if(updateService.isInfo()) {
				updateService.logInfo("!!!!!!Inside deleteSoftwareCentreFiles !!!!!");
		}
		boolean status = false;
		final List<String> mediaPathList = updateHandler.getSoftwareCentreLocation();
		if(mediaPathList != null){
			for(int i = 0 ; i < mediaPathList.size() ; i++) {
				String mediaPath = updateHandler.getSoftwareCentreLocation().get(i);
				if (!mediaPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {					
					mediaPath = mediaPath + Consts.IOs.FILE_SEPARATOR ;
				}
				status = updateHandler.delete(mediaPath);
				if(updateService.isInfo()) {
					updateService.logInfo("software center file deletion " );
				}
			}
		}
		if(!status){
			UnitManager.Logging.logSevere("software center file deletion failed" );
			
		}
	}	

	/**
	 * Sets the media load completed.
	 * 
	 * @param isMediaLoadCompleted the new media load completed
	 */
	public void setMediaLoadCompleted(boolean isMediaLoadCompleted) {
		this.isMediaLoadCompleted = isMediaLoadCompleted;
	}
}
