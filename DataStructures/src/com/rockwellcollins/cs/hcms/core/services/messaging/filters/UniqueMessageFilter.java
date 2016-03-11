package com.rockwellcollins.cs.hcms.core.services.messaging.filters;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;
import com.rockwellcollins.cs.hcms.core.services.messaging.MessageFilter;
import com.rockwellcollins.cs.hcms.core.services.messaging.wrappers.UniqueMessage;

public class UniqueMessageFilter implements MessageFilter {

	public void receivingMessage(final Message message) {

		final UniqueMessage msg = new UniqueMessage(message.getJsonObject());

		msg.setSender(msg.getIp().equals(
				UnitManager.ObjectModel.getUnit().getIpAddress()));
	}

	public void sendingMessage(final Message message) {
	}
}
