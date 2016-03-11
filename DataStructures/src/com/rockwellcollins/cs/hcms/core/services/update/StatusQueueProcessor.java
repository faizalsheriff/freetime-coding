package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;

/**
 * The Class StatusQueueProcessor queues up all the SA Messages that
 * are sent by all the LRUs and processes them one by one.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see SAProcessor
 * @see UpdateService
 * 
 */
public class StatusQueueProcessor extends QueueProcessor<SAMessage> {

	/**
	 * Instantiates a new status queue processor.
	 * 
	 * @param name the name
	 */
	public StatusQueueProcessor(final String name) {
		super(name);
	}
}
