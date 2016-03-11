package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.net.SocketAddress;

import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class StateManagerQueueItem {

	private StateManagerMessage smmsg;

	private SocketAddress sourceSocketAddress;

	public StateManagerMessage getStateManagerMessage() {
		return smmsg;
	}

	public SocketAddress getSourceSocketAddress() {
		return sourceSocketAddress;
	}

	public void setSourceSocketAddress(final SocketAddress sourceSocketAddress) {
		this.sourceSocketAddress = sourceSocketAddress;
	}

	public void setStateManagerMessage(final StateManagerMessage smmsg) {
		this.smmsg = smmsg;
	}
}
