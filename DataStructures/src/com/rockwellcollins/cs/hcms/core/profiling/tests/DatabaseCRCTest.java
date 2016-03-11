package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerListenerImpl;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class DatabaseCRCTest extends ProfileTest {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private transient ProfileResult currResult = new ProfileResult();
	private boolean newEvent = false;
	private double startTime = 0;
	private double currTime = 0;
	private double totalSamples = 0;
	private double totalTime = 0;
	private double dirtyCount = 0;
	private double dirtyBlocks = 0;
	private double maxTime = 0;
	private double minTime = 0;

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);

		sm = UnitManager.ObjectModel.getStateManager();

		if (!sm.register(new StateManagerListenerImpl() {
			@Override
			public void stateManagerProcessMessage(StateManager sm,
					StateManagerMessage message) {
				DatabaseCRCTest.this.processStateManagerProcessMessage(message);
			}

			public void stateManagerProcessMessageComplete(StateManager sm,
					StateManagerMessage message) {
				DatabaseCRCTest.this
						.processStateManagerProcessMessageComplete(message);
			}
		})) {
			System.out.println("DatabaseCRCTest register listener error");
		}
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here **/

		if (newEvent) {
			currResult.setMax(maxTime);
			currResult.setMin(minTime);
			currResult.setTime(totalTime / totalSamples);
			currResult.setGrade(dirtyCount / totalSamples);
			currResult.setValue(dirtyBlocks / totalSamples);
			
			result.setResult(currResult);
			newEvent = false;
			startTime = 0;
			totalSamples = 0;
			minTime = -1;
			maxTime = 0;
			currTime = 0;
			totalTime = 0;
			dirtyCount = 0;
			dirtyBlocks = 0;
			currResult.clear();
		}
	}

	public void processStateManagerProcessMessage(
			StateManagerMessage message) {
		if ((message.getType() == StateManagerMessage.TYPE_DATABASE_CRC 
				&& sm.getStatus() != StateManagerStatus.MASTER) ||
			(message.getType() == StateManagerMessage.TYPE_DATABASE_CRC_REQUEST 
				&& sm.getStatus() == StateManagerStatus.MASTER)) {
						
			// retrieve dirty count before message is processed
			dirtyCount += sm.getStateDatabase().dirtyCount();			
			dirtyBlocks += sm.getStateDatabase().dirtyBlockCount();			
			startTime = UnitManager.Timing.getTimeAlive();}
	}

	public void processStateManagerProcessMessageComplete(
			StateManagerMessage message) {
		try {
			if ((message.getType() == StateManagerMessage.TYPE_DATABASE_CRC 
					&& sm.getStatus() != StateManagerStatus.MASTER) ||
				(message.getType() == StateManagerMessage.TYPE_DATABASE_CRC_REQUEST 
					&& sm.getStatus() == StateManagerStatus.MASTER)) {
				
				if (startTime != 0) {
					totalSamples++;
					currTime = UnitManager.Timing.getTimeAlive() - startTime;
					totalTime += currTime;
					
					if (maxTime < currTime) {
						maxTime = currTime;
					}
					if (minTime > currTime || minTime == -1) {
						minTime = currTime;
					}
					startTime = 0;
					newEvent = true;
				}
			}
		} catch (Exception e) {
			UnitManager.Logging.logSevere("DatabaseCRCTest exception: " + e);
		}
	}
}
