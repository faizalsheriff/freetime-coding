package com.rockwellcollins.cs.hcms.core.profiling.tests;

import java.net.InetAddress;
import java.util.ArrayList;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerListenerImpl;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateChangeRequestMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StatusMessage;

public class StateManagerStatusCommTest extends ProfileTest {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private transient ProfileResult currResult = new ProfileResult();
	private boolean newEvent = false;
	private ArrayList<InetAddress> srcMap = new ArrayList<InetAddress>();
	private int srcCount = 0;
	private double totalTime = 0;
	private double minTime = 0;
	private double maxTime = 0;
	private double startTime = 0;
	private double currTime = 0;
	private double totalSamples = 0;
	private String myName;

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);
		
		myName = UnitManager.ObjectModel.getUnit().getInetAddress().toString();

		sm = UnitManager.ObjectModel.getStateManager();

		if (!sm.register(new StateManagerListenerImpl() {
			@Override
			
			public void stateManagerProcessStatusMessage(StateManager sm,
				StatusMessage message) {
					StateManagerStatusCommTest.this
					.stateManagerProcessStatusMessage(message);
				}
			
			public void stateManagerProcessStatusMessageComplete(StateManager sm,
				StatusMessage message) {
					StateManagerStatusCommTest.this
					.stateManagerProcessStatusMessageComplete(message);
				}
		})) 
		{
			System.out.println("StateManagerStatusCommTest register listener error");
		}
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here **/
		
		if (newEvent) {
			//UnitManager.Logging.logWarning(myName + " curr unit count: " + srcMap.size());
			if (totalSamples > 0)
				currResult.setTime(totalTime / totalSamples); // avg time per status message
			
			currResult.setMax(maxTime);
			currResult.setMin(minTime);			
			
			result.setResult(currResult);
			newEvent = false;
			currResult.clear();
			srcMap.clear();
			srcCount = 0;
			totalSamples = 0;
			totalTime = 0;
			minTime = -1;
			maxTime = 0;
			startTime = 0;
			currTime = 0;
			currResult.clear();
		}
	}

	public void stateManagerProcessStatusMessage(
			StatusMessage message) {
		if (message.getType() == StateManagerMessage.TYPE_STATUS) {
			startTime = UnitManager.Timing.getTimeAlive();
			InetAddress source = message.getSourceInetAddress();
			if (!srcMap.contains(source)) {
				srcMap.add(source);
				currResult.setValue(srcCount++);
				newEvent = true;
			}
			if (((StatusMessage)message).getStatus() == StateManagerStatus.MASTER ) {
				currResult.setNotes(source.getHostAddress());
			}
		}
	}
	
	public void stateManagerProcessStatusMessageComplete(
			StatusMessage message) {
		if (startTime != 0) {
			totalSamples++;
			currTime = UnitManager.Timing.getTimeAlive() - startTime;
			totalTime += currTime;

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
