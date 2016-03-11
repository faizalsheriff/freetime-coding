package com.rockwellcollins.cs.hcms.core.services.statemanager;

import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.ControlMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StatusMessage;

public class StateManagerListenerImpl implements StateManagerListener {

	public void stateManagerModeChanged(StateManager sm, StateManagerMode mode) {

	}

	public void stateManagerProcessControlMessage(StateManager sm,
			ControlMessage message) {

	}

	public void stateManagerProcessMessage(StateManager sm,
			StateManagerMessage message) {

	}

	public void stateManagerProcessStatusMessage(StateManager sm,
			StatusMessage message) {

	}

	public void stateManagerProcessStatusMessageComplete(StateManager sm,
			StatusMessage message) {

	}

	public void stateManagerStatusChanged(StateManager sm,
			StateManagerStatus status) {

	}

	public void stateManagerSendMessage(StateManager sm,
			StateManagerMessage message) {

	}

	public void stateManagerProcessMessageComplete(StateManager sm,
			StateManagerMessage message) {

	}

}
