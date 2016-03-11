package com.rockwellcollins.cs.hcms.core.services.update.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateService;

/**
 * The Class SAMessage describes the Status Announcement Message.  The 
 * message is represented in the form of a JSON string.
 * 
 * A SA message includes the following information:
 * 
 * - Status Announcement message version
 * - IP address of the LRU that sends the message
 * - LRU type
 * - LRU instance
 * - Total steps for the installation
 * - Current step number
 * - Current step name
 * - Current substep name
 * - Current substep percentage
 * - Is status is available or not
 * - Is currently running or not
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateServiceMessage
 * @see com.rockwellcollins.cs.hcms.core.services.update.UpdateService
 * 
 */
public class SAMessage extends UpdateServiceMessage {
	
	private UpdateService updateService;
	
	private String ipAddress;
	
	private String lruType;
	
	private String lruInstance;
	
	private int totalSteps;
	
	private int stepNumber;
	
	private String stepName;
	
	private String subStepName;
	
	private String subStepPercentage;
	
	private boolean isStatusAvailable;
	
	private boolean isRunning;
	
	private static final String JSON_IP = "ip address";

	private static final String JSON_LRU_TYPE = "lru type";

	private static final String JSON_LRU_INSTANCE = "lru instance";

	private static final String JSON_TOTAL_STEPS = "total steps";

	private static final String JSON_STEP_NUMBER = "step number";

	private static final String JSON_STEP_NAME = "step name";

	private static final String JSON_SUB_STEP_NAME = "sub step name";

	private static final String JSON_SUB_STEP_PERCENTAGE = "sub step percentage";

	private static final String JSON_STATUS_AVAILABLE = "status available";

	private static final String JSON_RUNNING = "running";

	/** The Constant VERSION. */
	public static final String VERSION = "1.0";

	/**
	 * Instantiates a new SA message.
	 */
	public SAMessage() {
		super();
		setType(UpdateServiceMessageTypes.STATUS_ANNOUNCEMENT);
		setMessageVersion(VERSION);
	}

	/**
	 * Instantiates a new SA message.
	 * 
	 * @param jsonObject the json object
	 */
	public SAMessage(final JSONObject jsonObject) {
		super(jsonObject);
		initialize();
	}
	
	/**
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Initializes the newly created SA Message.
	 */
	private void initialize() {
		ipAddress = getJsonObject().optString(JSON_IP);
		
		lruType = getJsonObject().optString(JSON_LRU_TYPE);
		
		lruInstance = getJsonObject().optString(JSON_LRU_INSTANCE);
		
		totalSteps = getJsonObject().optInt(JSON_TOTAL_STEPS);
		
		stepNumber = getJsonObject().optInt(JSON_STEP_NUMBER);
		
		stepName = getJsonObject().optString(JSON_STEP_NAME);
		
		subStepName = getJsonObject().optString(JSON_SUB_STEP_NAME);
		
		subStepPercentage = getJsonObject().optString(JSON_SUB_STEP_PERCENTAGE);
		
		isStatusAvailable = getJsonObject().optBoolean(JSON_STATUS_AVAILABLE);
		
		isRunning = getJsonObject().optBoolean(JSON_RUNNING);
	}

	/**
	 * Gets the ip address.
	 * 
	 * @return the ip address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the ip address.
	 * 
	 * @param ipAddress the new ip address
	 */
	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
		try {
			getJsonObject().put(JSON_IP, ipAddress);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the lru type.
	 * 
	 * @return the lru type
	 */
	public String getLruType() {
		return lruType;
	}

	/**
	 * Sets the lru type.
	 * 
	 * @param lruType the new lru type
	 */
	public void setLruType(final String lruType) {
		this.lruType = lruType;
		try {
			getJsonObject().put(JSON_LRU_TYPE, lruType);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
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
	 * Sets the lru instance.
	 * 
	 * @param lruInstance the new lru instance
	 */
	public void setLruInstance(final String lruInstance) {
		this.lruInstance = lruInstance;
		try {
			getJsonObject().put(JSON_LRU_INSTANCE, lruInstance);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the total steps.
	 * 
	 * @return the total steps
	 */
	public int getTotalSteps() {
		return totalSteps;
	}

	/**
	 * Sets the total steps.
	 * 
	 * @param totalNumberOfSteps the new total steps
	 */
	public void setTotalSteps(final int totalSteps) {
		this.totalSteps = totalSteps;
		try {
			getJsonObject().put(JSON_TOTAL_STEPS, totalSteps);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the step number.
	 * 
	 * @return the step number
	 */
	public int getStepNumber() {
		return stepNumber;
	}

	/**
	 * Sets the step number.
	 * 
	 * @param stepNumber the new step number
	 */
	public void setStepNumber(final int stepNumber) {
		this.stepNumber = stepNumber;
		try {
			getJsonObject().put(JSON_STEP_NUMBER, stepNumber);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the step name.
	 * 
	 * @return the step name
	 */
	public String getStepName() {
		return stepName;
	}

	/**
	 * Sets the step name.
	 * 
	 * @param stepName the new step name
	 */
	public void setStepName(final String stepName) {
		this.stepName = stepName;
		try {
			getJsonObject().put(JSON_STEP_NAME, stepName);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the sub step name.
	 * 
	 * @return the sub step name
	 */
	public String getSubStepName() {
		return subStepName;
	}

	/**
	 * Sets the sub step name.
	 * 
	 * @param subStepName the new sub step name
	 */
	public void setSubStepName(final String subStepName) {
		this.subStepName = subStepName;
		try {
			getJsonObject().put(JSON_SUB_STEP_NAME, subStepName);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Gets the sub step percentage.
	 * 
	 * @return the sub step percentage
	 */
	public String getSubStepPercentage() {
		return subStepPercentage;
	}

	/**
	 * Sets the sub step percentage.
	 * 
	 * @param subStepPercentage the new sub step percentage
	 */
	public void setSubStepPercentage(final String subStepPercentage) {
		this.subStepPercentage = subStepPercentage;
		try {
			getJsonObject().put(JSON_SUB_STEP_PERCENTAGE, subStepPercentage);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Checks if is status available.
	 * 
	 * @return true, if is status available
	 */
	public boolean isStatusAvailable() {
		return isStatusAvailable;
	}

	/**
	 * Sets the status available.
	 * 
	 * @param isStatusAvailable the new status available
	 */
	public void setStatusAvailable(final boolean isStatusAvailable) {
		this.isStatusAvailable = isStatusAvailable;
		try {
			getJsonObject().put(JSON_STATUS_AVAILABLE, isStatusAvailable);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Checks if is running.
	 * 
	 * @return true, if is running
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Sets the running.
	 * 
	 * @param isRunning the new running
	 */
	public void setRunning(final boolean isRunning) {
		this.isRunning = isRunning;
		try {
			getJsonObject().put(JSON_RUNNING, isRunning);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}

	/**
	 * Prints the current values.
	 */
	public void printCurrentValues() {
		if (updateService.isInfo()) {
			updateService.logInfo("*******************************************");
			updateService.logInfo("Printing current values of SAMessage");
			updateService.logInfo("SA Message Version:         " + getMessageVersion());
			updateService.logInfo("ipAddress:                  " + getIpAddress());
			updateService.logInfo("lruType:                    " + getLruType());
			updateService.logInfo("lruInstance:                " + getLruInstance());
			updateService.logInfo("totalSteps:                 " + getTotalSteps());
			updateService.logInfo("StepNumber:                 " + getStepNumber());
			updateService.logInfo("StepName:                   " + getStepName());
			updateService.logInfo("SubStepName:                " + getSubStepName());
			updateService.logInfo("SubStepPercentage:          " + getSubStepPercentage());
			updateService.logInfo("isStatusAvailable:          " + isStatusAvailable());
			updateService.logInfo("isRunning:                  " + isRunning());
			updateService.logInfo("*******************************************");
		}
	}
}
