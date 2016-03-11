package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerListenerImpl;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateChangeRequestMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateChangeResponseMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.DatabaseListener;

public class StateChangeTest extends ProfileTest {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private transient ProfileResult currResult = new ProfileResult();
	private boolean newEvent = false;
	private double totalTime = 0;
	private double minTime = 0;
	private double maxTime = 0;
	private double startTime = 0;
	private double currTime = 0;
	private double totalSamples = 0;
	private double totalItems = 0;
	private long totalStates = 0;
	private long pollTime = 5000;

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);

		pollTime = super.getPollTime();
		
		sm = UnitManager.ObjectModel.getStateManager();
		sm.register(new StateManagerListenerImpl() {
			@Override
			public void stateManagerProcessMessage(StateManager sm,
					StateManagerMessage message) {
				StateChangeTest.this.processStateManagerProcessMessage(message);
			}

			public void stateManagerProcessMessageComplete(StateManager sm,
					StateManagerMessage message) {
				StateChangeTest.this
						.processStateManagerProcessMessageComplete(message);
			}
		});
		
		sm.register(new DatabaseListener() {
			public void stateChanged(StateDatabase db, final int stateIndex) {
				StateChangeTest.this.processStateChanged(stateIndex);
			}
			
			public void databaseChanged(StateDatabase db) {
				
			}
		});
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here **/

		if (newEvent) {
			if (totalItems > 0)
				currResult.setGrade(totalTime / totalItems); // avg time per state
			
			if (totalSamples > 0)
				currResult.setValue(totalTime / totalSamples); // avg time per state map
			
			currResult.setTime((double)totalStates / ((double)pollTime / 1000));
			
			currResult.setMax(maxTime);
			currResult.setMin(minTime);			
			
			result.setResult(currResult);
			newEvent = false;
			totalSamples = 0;
			totalTime = 0;
			totalItems = 0;
			minTime = -1;
			maxTime = 0;
			startTime = 0;
			currTime = 0;
			currResult.clear();
			totalStates = 0;
		}
	}

	public void processStateManagerProcessMessage(StateManagerMessage message) {
		if ((message.getType() == StateManagerMessage.TYPE_STATE_CHANGE_REQUEST
				&& sm.getStatus() == StateManagerStatus.MASTER) 
		|| (message.getType() == StateManagerMessage.TYPE_STATE_CHANGE_RESPONSE
				&& sm.getStatus() != StateManagerStatus.MASTER)) {
			startTime = UnitManager.Timing.getTimeAlive();
		}
	}
	
	public void processStateChanged(final int stateIndex) {
		totalStates++;
	}

	public void processStateManagerProcessMessageComplete(
			StateManagerMessage message) {

		if (message == null) {
			UnitManager.Logging.logSevere("In StateChangeTest message is null");
			return;
		}

		try {
			if (message.getType() == StateManagerMessage.TYPE_STATE_CHANGE_REQUEST
				&& sm.getStatus() == StateManagerStatus.MASTER) {
				if (startTime != 0) {
					totalSamples++;
					currTime = UnitManager.Timing.getTimeAlive() - startTime;
					totalTime += currTime;
	
					if (message instanceof StateChangeRequestMessage) {
						StateMap stateMap = ((StateChangeRequestMessage) message)
									.getStateMap();
			
						if (stateMap != null && stateMap.entrySet() != null) {
							totalItems += stateMap.entrySet().size();
						}
						if (currTime > maxTime) {
							maxTime = currTime;
						}
						if (currTime < minTime || minTime == -1) {
							minTime = currTime;
						}
						
						startTime = 0;
						newEvent = true;
					}
				}
			}
			else if (message.getType() == StateManagerMessage.TYPE_STATE_CHANGE_RESPONSE
					&& sm.getStatus() != StateManagerStatus.MASTER) {
				if (startTime != 0) {
					totalSamples++;
					currTime = UnitManager.Timing.getTimeAlive() - startTime;
					totalTime += currTime;
	
					if (message instanceof StateChangeResponseMessage) {
						StateMap stateMap = ((StateChangeResponseMessage) message)
						.getStateMap();
	
						if (stateMap != null && stateMap.entrySet() != null) {
							totalItems += stateMap.entrySet().size();
						}
						if (currTime > maxTime) {
							maxTime = currTime;
						}
						if (currTime < minTime || minTime == -1) {
							minTime = currTime;
						}
						startTime = 0;
						newEvent = true;
					}
				}
			}
		} catch (Exception e) {
			UnitManager.Logging.logSevere("StateChangeTest exception: " + e);
		}
	}
}
