package com.rockwellcollins.cs.hcms.core.services.diagnostic;

import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public interface EchoClientListener {
	void echoClientProcessMessage(final long id, final StateManagerMessage message);
	void echoClientSendMessage(final long id, final StateManagerMessage message);
}
