package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.ComponentContainer;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileHandler;

public class SystemStateChangeTest extends ProfileTest {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private Handler handler;
	private transient ProfileResult currResult = new ProfileResult();
	private boolean newEvent = false;
	private double startTime = 0;
	private int currVal = 0;
	private boolean testEnabled = false;
	private String TestPropertyRequestName = "ProfileTestReq";
	private static final String SETTING_TEST_PROPERTY_REQUEST_NAME = "test property request name";

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);

		sm = UnitManager.ObjectModel.getStateManager();
		ComponentContainer cc = UnitManager.ObjectModel.getComponents();
		Handler[] handlers = cc.getHandlers();
		for (int i = 0; i < handlers.length; i++ ) {
			if (handlers[i] instanceof ProfileHandler) {
				handler = handlers[i];
				break;
			}
		}
		
		TestPropertyRequestName = getSetting(SETTING_TEST_PROPERTY_REQUEST_NAME, TestPropertyRequestName);
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);
		
		/** On Run with Result Goes Here **/

		if (newEvent) {
			result.setResult(currResult);
			newEvent = false;
			startTime = 0;
			currResult.clear();
		}
		
		// send new state change request only if enabled to ensure only one state update occurs
		if (testEnabled == true) {
			try {
				currVal++;
				handler.setProperty(TestPropertyRequestName, Integer.valueOf(currVal).toString());
				startTime = UnitManager.Timing.getTimeAlive();
			}
			catch (Exception e) {
				UnitManager.Logging.logSevere("SystemStateChangeTest exception setting property" + e);
			}
		}
	}

	public void setResponseTime() {
		if (startTime != 0 && testEnabled == true) {
			double currTime = UnitManager.Timing.getTimeAlive();
			currResult.setTime(currTime - startTime);
			newEvent = true;
		}
	}
	
	public void enableTest(boolean enable) {
		testEnabled = enable;
	}
}
