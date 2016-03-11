package com.rockwellcollins.cs.hcms.core.services.update.messages;

/**
 * The Enum UpdateServiceMessageTypes defines the possible message types
 * for Update Service.
 * 
 * @author Raja Sonnia Pattabiraman
 * 
 */
public enum UpdateServiceMessageTypes {
	
	/** The CONFIGURATION_ANNOUNCEMENT. */
	CONFIGURATION_ANNOUNCEMENT, 
	
	/** The LOAD_ANNOUNCEMENT. */
	LOAD_ANNOUNCEMENT, 
	
	/** The STATUS_ANNOUNCEMENT. */
	STATUS_ANNOUNCEMENT,
	
	/** The MEDIA_ANNOUNCEMENT. */
	MEDIA_ANNOUNCEMENT,
	
	/** The ELECTION_ANNOUNCEMENT. */
	ELECTION_ANNOUNCEMENT,
	
	/** The NAND_RECOVERY. */
	NAND_RECOVERY;
}
