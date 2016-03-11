package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerListenerImpl;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class BlockCRCTest extends ProfileTest {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private transient ProfileResult currResult = new ProfileResult();
	private boolean newEvent = false;
	private double startTime = 0;
	private double currTime = 0;
	private double totalSamples = 0;
	private double totalTime = 0;
	private double maxTime = 0;
	private double minTime = 0;
	private double dirtyBlocks = 0;
	private double dirtyStates = 0;

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);

		sm = UnitManager.ObjectModel.getStateManager();

		sm.register(new StateManagerListenerImpl() {
			@Override
			public void stateManagerProcessMessage(StateManager sm,
					StateManagerMessage message) {
				BlockCRCTest.this.processStateManagerProcessMessage(message);
			}

			public void stateManagerProcessMessageComplete(StateManager sm,
					StateManagerMessage message) {
				BlockCRCTest.this
						.processStateManagerProcessMessageComplete(message);
			}
		});
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here **/

		// System.out.println(sm.getDatabaseSignature());
		if (newEvent) {
			currResult.setMin(minTime);
			currResult.setMax(maxTime);
			currResult.setTime(totalTime / totalSamples);
			currResult.setValue(dirtyBlocks / totalSamples);
			currResult.setGrade(dirtyStates / totalSamples);
			
			result.setResult(currResult);
			newEvent = false;
			startTime = 0;
			totalSamples = 0;
			minTime = -1;
			maxTime = 0;
			currTime = 0;
			totalTime = 0;
			dirtyBlocks = 0;
			dirtyStates = 0;
			currResult.clear();
		}
	}

	public void processStateManagerProcessMessage(StateManagerMessage message) {
		if ((message.getType() == StateManagerMessage.TYPE_BLOCK_CRC
				&& sm.getStatus() != StateManagerStatus.MASTER) ||
			(message.getType() == StateManagerMessage.TYPE_BLOCK_CRC_REQUEST
				&& sm.getStatus() == StateManagerStatus.MASTER)) {
			
			dirtyStates += sm.getStateDatabase().dirtyCount();
			dirtyBlocks += sm.getStateDatabase().dirtyBlockCount();
			startTime = UnitManager.Timing.getTimeAlive();
		}
	}

	public void processStateManagerProcessMessageComplete(
			StateManagerMessage message) {
		try {
			if ((message.getType() == StateManagerMessage.TYPE_BLOCK_CRC
					&& sm.getStatus() != StateManagerStatus.MASTER)
			|| (message.getType() == StateManagerMessage.TYPE_BLOCK_CRC_REQUEST
					&& sm.getStatus() == StateManagerStatus.MASTER)) {
				if (startTime != 0) {
					totalSamples++;
					currTime = UnitManager.Timing.getTimeAlive() - startTime;
					totalTime += currTime;
					
					//currResult.setNotes("BLOCK CRC");
					
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
			UnitManager.Logging.logSevere("BlockCRCTest exception: " + e);
		}
	}
}
