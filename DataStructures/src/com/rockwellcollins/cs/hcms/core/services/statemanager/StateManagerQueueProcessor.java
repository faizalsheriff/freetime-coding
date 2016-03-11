package com.rockwellcollins.cs.hcms.core.services.statemanager;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class StateManagerQueueProcessor extends
		QueueProcessor<StateManagerMessage> {

	public StateManagerQueueProcessor(final String name) {
		super(name);
	}

	public boolean isProcessing(final StateManagerMessage message) {

		boolean result = false;

		try {

			synchronized (this) {

				for (final StateManagerMessage queueMessage : getQueue()) {

					if (queueMessage.getId() == message.getId()
							&& queueMessage.getSourceInetAddress().equals(
									message.getSourceInetAddress())) {
						result = true;
						break;
					}
				}
			}

		} catch (final Exception e) {
			UnitManager.Logging.logSevere(e);
		}

		return result;
	}
}
