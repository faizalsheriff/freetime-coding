package com.rockwellcollins.cs.hcms.core.services.update.messages;

/**
 * The Enum MAActionTypes defines the possible actions 
 * while loading from a removable media.
 */
public enum MAActionTypes {
	
	/** The NONE. */
	NONE,
	
	/** The REQUEST. */
	REQUEST,
	
	/** The Software Center request. */
	SOFTWARECENTREREQUEST,
	
	/** The RESPONSE. */
	RESPONSE,
	
	/** The VALIDATE. */
	VALIDATE,
	
	/** The CONFIRM_FILES. */
	CONFIRM_FILES,
	
	/** The LOAD. */
	LOAD,
	
	/** The REBOOT. */
	REBOOT,
	
	/** The ERROR. */
	ERROR,
	
	/** The CANCEL. */
	CANCEL;
}
