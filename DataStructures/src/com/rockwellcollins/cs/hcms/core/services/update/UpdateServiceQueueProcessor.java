package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.services.update.messages.CAMessage;

/**
 * The Class UpdateServiceQueueProcessor queues up all the CA Messages that
 * are sent by all the LRUs and processes them one by one.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see CAMessage
 * @see UpdateService
 * 
 */
public class UpdateServiceQueueProcessor extends QueueProcessor<CAMessage> {

	/**
	 * Instantiates a new update service queue processor.
	 * 
	 * @param name the name
	 */
	public UpdateServiceQueueProcessor(final String name) {
		super(name);
	}
	
	@Override
	public void setTimeout(int timeout) {
		super.setTimeout(timeout);
		if(timeout != 0 && getThread() != null) {
			getThread().interrupt();
		}
	}
}
