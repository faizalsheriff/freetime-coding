package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentContainer;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.handlers.HandlerListener;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangeTimeoutArgs;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangedArgs;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyMapChangedArgs;
import com.rockwellcollins.cs.hcms.core.services.statemanager.RulesEngineListener;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.rules.RulesEngine;

public class RuleExecutionTest extends ProfileTest {
	private static final long serialVersionUID = 1L;
	private StateManager sm;
	private Handler testHandler;
	private ComponentContainer cc;
	private transient ProfileResult currResult = new ProfileResult();
	private boolean newEvent = false;
	private double avgRuleTime = 0;
	private double minRuleTime = 0;
	private double maxRuleTime = 0;
	private double lastRuleTime = 0;
	private double startTime = 0;
	private double totalTime = 0; 
	private double numRuleTotal = 0;
	private double ourRuleTotal = 0;
	private Integer testVal = 0;
	private Integer triggerVal = 0;
	
	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		// TODO Auto-generated method stub
		super.onSetup(source, args);
		startTime = UnitManager.Timing.getTimeAlive();
		
		sm = UnitManager.ObjectModel.getStateManager();
		
		cc = UnitManager.ObjectModel.getComponents();
		testHandler = cc.getHandler("TESTHANDLER");
		if(testHandler != null) {
			testHandler.register(new HandlerListener() {
				public void handlerPropertyChanged(Handler handler,
						PropertyChangedArgs args) {
					RuleExecutionTest.this.processStateChange(handler, args);
					};
	
				public void handlerPropertyMapChanged(Handler handler,
						PropertyMapChangedArgs args) {};
	
				public void handlerPropertyChangedTimeout(Handler handler,
						PropertyChangeTimeoutArgs args) {};
			});
		}
		
		
		sm.register(new RulesEngineListener() {
			public void rulesEngineChanged(RulesEngine rulesEngine) {
				RuleExecutionTest.this.processRulesEngineChanged(rulesEngine);
			}
		});
		
		UnitManager.ObjectModel.getComponents();
//		System.out.println("RuleExecutionTest start");

		startTime = UnitManager.Timing.getTimeAlive();
	}
	
	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here **/

		if (newEvent) {
			result.setResult(currResult);
			newEvent = false;
		}
		testVal++;
		triggerVal++;
		testHandler.setProperty("TriggerState", testVal.toString());

	}
	
	// called any time the rules change or when a rule is executed?
	public void processRulesEngineChanged(RulesEngine rulesEngine) {
		numRuleTotal++;
		lastRuleTime = sm.getLastRuleExecTime();
		avgRuleTime = (avgRuleTime + lastRuleTime) / numRuleTotal;
		currResult.setGrade(avgRuleTime);  // avg time per rule execution
		
		totalTime = (UnitManager.Timing.getTimeAlive() - startTime) * 1000;
		currResult.setValue(numRuleTotal / totalTime);  // avg num of rules per second
		newEvent = true;
		
	}

	// listen for resulting 'test state' update resulting from 
	// rule execution after 'trigger state' update
	public void processStateChange(Handler handler,
			PropertyChangedArgs args) {
		
		if (args.getPropertyName().equalsIgnoreCase("TestState")) {
			//if (args.getPropertyValue().equals(testVal.toString())) {
				ourRuleTotal++;
				currResult.setValue(ourRuleTotal);  // total rule executions
				
				lastRuleTime = sm.getLastRuleExecTime();
				//avgRuleTime = (avgRuleTime + lastRuleTime) / ourRuleTotal;
				currResult.setTime(lastRuleTime);  // time per rule execution
				
				if (lastRuleTime > maxRuleTime) {
					maxRuleTime = lastRuleTime;
					currResult.setMax(maxRuleTime);
				}
				
				if (lastRuleTime < minRuleTime || minRuleTime == 0) {
					minRuleTime = lastRuleTime;
					currResult.setMin(minRuleTime);
				}
				
				newEvent = true;
			//}
		}
		else {  // all other state change events we are listening for?
			
		}
	}
	
}
