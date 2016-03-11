package com.rockwellcollins.cs.hcms.core;

public interface UnitEventCallback {
	void eventExpired(final Unit unit, final UnitEvent event);
}
