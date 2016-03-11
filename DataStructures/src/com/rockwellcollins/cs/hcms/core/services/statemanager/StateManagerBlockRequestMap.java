package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.net.InetAddress;

import com.rockwellcollins.cs.hcms.core.collections.TwoWayMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class StateManagerBlockRequestMap extends
		TwoWayMap<InetAddress, StateManagerMessage> {

	private static final long serialVersionUID = 1L;

}
