package com.rockwellcollins.cs.hcms.core.services.messaging;

public interface MessageFilter {
	void sendingMessage(Message message);

	void receivingMessage(Message message);
}
