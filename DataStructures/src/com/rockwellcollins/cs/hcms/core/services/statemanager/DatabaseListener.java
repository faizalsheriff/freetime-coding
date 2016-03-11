package com.rockwellcollins.cs.hcms.core.services.statemanager;

import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;

public interface DatabaseListener {
	public void stateChanged(StateDatabase db, final int stateIndex);
	public void databaseChanged(StateDatabase db);
}
