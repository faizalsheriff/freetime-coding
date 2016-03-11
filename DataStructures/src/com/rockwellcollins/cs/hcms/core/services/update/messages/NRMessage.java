package com.rockwellcollins.cs.hcms.core.services.update.messages;

import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.services.update.UpdateService;

/**
 * The Class NRMessage describes the NAND Recovery Message.  The 
 * message is represented in the form of a JSON string.
 * 
 * A NR message includes the following information:
 * 
 * - IP Address
 * - Corruption detected or not
 * - Corrupted partition names
 * - Recovery successful or not
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateServiceMessage
 * @see com.rockwellcollins.cs.hcms.core.services.update.UpdateService
 * 
 */
public class NRMessage extends UpdateServiceMessage {
	
	private UpdateService updateService;
	
	private String ipAddress;
	
	private boolean isCorruptionDetected;
	
	private String corruptedPartition;
	
	private boolean isRecoverySuccess;
	
	private static final String JSON_IP = "ip address";
	
	private static final String JSON_NAND_CORRUPTION_DETECTED = "nand corruption detected";

	private static final String JSON_NAND_PARTITION_CORRUPTED = "nand partition corrupted";

	private static final String JSON_NAND_RECOVERY_SUCCEESS = "nand recovery success";
	
	/** The Constant VERSION. */
	public static final String VERSION = "1.0";

	/**
	 * Instantiates a new NR message.
	 */
	public NRMessage() {
		super();
		setType(UpdateServiceMessageTypes.NAND_RECOVERY);
		setMessageVersion(VERSION);
	}
	
	/**
	 * Instantiates a new NR message.
	 * 
	 * @param jsonObject the json object
	 */
	public NRMessage(final JSONObject jsonObject) {
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
	 * Initializes the newly created NR Message.
	 */
	private void initialize() {
		ipAddress = getJsonObject().optString(JSON_IP);
		
		isCorruptionDetected = getJsonObject().optBoolean(JSON_NAND_CORRUPTION_DETECTED);
		
		corruptedPartition = getJsonObject().optString(JSON_NAND_PARTITION_CORRUPTED);
		
		isRecoverySuccess = getJsonObject().optBoolean(JSON_NAND_RECOVERY_SUCCEESS);
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
	 * Checks if is corruption detected.
	 * 
	 * @return true, if is corruption detected
	 */
	public boolean isCorruptionDetected() {
		return isCorruptionDetected;
	}
	
	/**
	 * Gets the corrupted partition.
	 * 
	 * @return the corrupted partition
	 */
	public String getCorruptedPartition() {
		return corruptedPartition;
	}
	
	/**
	 * Checks if is recovery success.
	 * 
	 * @return true, if is recovery success
	 */
	public boolean isRecoverySuccess() {
		return isRecoverySuccess;
	}
	
	/**
	 * Prints the current values.
	 */
	public void printCurrentValues() {
		if (updateService.isInfo()) {
			updateService.logInfo("*******************************************");
			updateService.logInfo("Printing current values of NRMessage");
			updateService.logInfo("NR Message Version:         " + getMessageVersion());
			updateService.logInfo("ipAddress:                  " + getIpAddress());
			updateService.logInfo("isCorruptionDetected:       " + isCorruptionDetected());
			updateService.logInfo("getCorruptedPartition:      " + getCorruptedPartition());
			updateService.logInfo("isRecoverySuccess:          " + isRecoverySuccess());
			updateService.logInfo("*******************************************");
		}
	}
}
