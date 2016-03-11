package com.rockwellcollins.cs.hcms.core.services.statemanager;

import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.ControlMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StatusMessage;

public interface StateManagerListener {
	public void stateManagerStatusChanged(StateManager sm, StateManagerStatus status);
	public void stateManagerModeChanged(StateManager sm, StateManagerMode mode);
	public void stateManagerProcessMessage(StateManager sm, StateManagerMessage message);
	public void stateManagerProcessMessageComplete(StateManager sm, StateManagerMessage message);
	public void stateManagerProcessControlMessage(StateManager sm, ControlMessage message);
	public void stateManagerProcessStatusMessage(StateManager sm, StatusMessage message);
	public void stateManagerProcessStatusMessageComplete(StateManager sm, StatusMessage message);
	public void stateManagerSendMessage(StateManager sm, StateManagerMessage message);
}
