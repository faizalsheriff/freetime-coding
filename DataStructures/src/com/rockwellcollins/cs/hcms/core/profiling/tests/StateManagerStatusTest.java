package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerListenerImpl;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;

public class StateManagerStatusTest extends ProfileTest {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private double syncTime = 0;
	private double loadingTime = 0;
	private double onlineTime = 0;
	private double masterTime = 0;
	private double errorTime = 0;
	private double recoverTime = 0;
	private transient ProfileResult currResult = new ProfileResult();
	private boolean newEvent = false;
	private static final String STATUS_TEST_TYPE = "status test type";
	private String type = "";
	private boolean isSync;
	private boolean isMaster;
	private boolean isOnline;

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);

		sm = UnitManager.ObjectModel.getStateManager();

		sm.register(new StateManagerListenerImpl() {
			@Override
			public void stateManagerStatusChanged(StateManager sm,
					StateManagerStatus status) {
				StateManagerStatusTest.this
						.processStateManagerStatusChanged(status);
			}
		});
		
		isSync = getSetting(STATUS_TEST_TYPE, "").equals("SYNC");
		isMaster = getSetting(STATUS_TEST_TYPE, "").equals("MASTER");
		isOnline = getSetting(STATUS_TEST_TYPE, "").equals("ONLINE");
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here * */

		// System.out.println(sm.getDatabaseSignature());
		if (newEvent) {
//			System.out.println("StateManagerStatusTest new event");
			result.setResult(currResult);
			newEvent = false;
		}
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeSetting() {
		return STATUS_TEST_TYPE;
	}

	public void processStateManagerStatusChanged(StateManagerStatus status) {
		switch (status) {
		case ONLINE:
			if (isOnline) {
				onlineTime = UnitManager.Timing.getTimeAlive();
				currResult.setTime(onlineTime);
				currResult.setNotes("ONLINE TIME");
				newEvent = true;
			}
			break;
		case LOADING:
			if (onlineTime != 0 && isOnline) {
				loadingTime = UnitManager.Timing.getTimeAlive() - onlineTime;
				currResult.setValue(loadingTime);
				currResult.setNotes("LOADING TIME");
				newEvent = true;
			}
			break;
		case SYNCHRONIZED:
			if (isSync) {
				syncTime = UnitManager.Timing.getTimeAlive();
				currResult.setTime(syncTime);
				currResult.setNotes("SYNC TIME");
				newEvent = true;
			}
			break;
		case RECOVER:
			if (syncTime != 0 && isSync) {
				recoverTime = UnitManager.Timing.getTimeAlive() - syncTime;
				currResult.setValue(recoverTime);
				currResult.setNotes("RECOVER TIME");
				newEvent = true;
			}
			break;
		case MASTER:
			if (isMaster) {
				masterTime = UnitManager.Timing.getTimeAlive();
				currResult.setTime(masterTime);
				currResult.setNotes("MASTER TIME");
				newEvent = true;
			}			
			break;
		case ERROR:
			errorTime = UnitManager.Timing.getTimeAlive();
			break;
		}
	}

}
