package com.rockwellcollins.cs.hcms.core;

public interface ComponentListener {
	void componentSetup(final Component component, final ComponentSetupArgs args);

	void componentVerify(final Component component,
			final ComponentVerifyArgs args);
}
