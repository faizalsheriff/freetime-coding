package com.rockwellcollins.cs.hcms.core.services.statemanager;

import com.rockwellcollins.cs.hcms.core.services.statemanager.rules.RulesEngine;

public interface RulesEngineListener {
	public void rulesEngineChanged(RulesEngine rulesEngine);
}
