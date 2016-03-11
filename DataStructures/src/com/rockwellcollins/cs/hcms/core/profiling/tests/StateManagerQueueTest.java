package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.QueueListener;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class StateManagerQueueTest extends ProfileTest implements
		QueueListener<StateManagerMessage> {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private transient ProfileResult currResult = new ProfileResult();
	private int currTotalQueue = 0;
	private int currTotalCount = 0;
	private int currQueue = 0;
	private int currCount = 0;
	private int currSamples = 0;
	private double currMin = -1;
	private double currMax = 0;
	private double currTime = 0;
	private boolean newEvent = false;

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);

		sm = UnitManager.ObjectModel.getStateManager();
		sm.getQueueProcessor().addListener(this);

		UnitManager.ObjectModel.getComponents();
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here **/

		if (newEvent == true) {
			
			if( currTime != 0) {
				double samplePeriod = UnitManager.Timing.getTimeAlive() - currTime;
				currResult.setTime(currSamples / (samplePeriod / 1000)); // avg number of items this period
			}
			
			currResult.setMin(currMin);
			currResult.setMax(currMax);
			currResult.setValue((double)currTotalCount / (double)currSamples); // running avg. msg counter
			currResult.setGrade((double)currTotalQueue / (double)currSamples); // running avg. queue depth
			
			result.setResult(currResult);
			currTotalQueue = 0;
			currTotalCount = 0;
			currQueue = 0;
			currCount = 0;
			currMin = -1;
			currMax = 0;
			currSamples = 0;
			currResult.clear();
			currTime = UnitManager.Timing.getTimeAlive();
			newEvent = false;
		}
	}

	// for each item received
	public void queueProcessorItemReceived(
			QueueProcessor<StateManagerMessage> processor,
			StateManagerMessage message) {
		currSamples++;

		currCount = sm.messageCounter.total(); 
		currTotalCount += currCount;

		currQueue = sm.getQueueProcessor().getQueueSize(); 
		currTotalQueue += currQueue;

		if (currMax < currQueue) {
			currMax = currQueue;
		}
		
		if (currMin > currQueue || currMin == -1) {
			currMin = currQueue;
		}
		newEvent = true;

		/*
		int index = message.getType();
		switch (index) {

		case StateManagerMessage.TYPE_STATE_CHANGE_REQUEST:
			sm.messageCounter.get(index);
			break;

		case StateManagerMessage.TYPE_BLOCK_REQUEST:
			break;

		case StateManagerMessage.TYPE_STATE_CHANGE_RESPONSE:
			break;

		case StateManagerMessage.TYPE_BLOCK_CRC:
			break;

		case StateManagerMessage.TYPE_BLOCK_CRC_REQUEST:
			break;

		case StateManagerMessage.TYPE_SYNCHRONIZE:
			break;

		case StateManagerMessage.TYPE_DATABASE_CRC:
			break;

		case StateManagerMessage.TYPE_DATABASE_CRC_REQUEST:
			break;

		default:
			break;

		}
		*/
	}

	public void queueProcessorTimeout(
			QueueProcessor<StateManagerMessage> processor) {
	}
}
