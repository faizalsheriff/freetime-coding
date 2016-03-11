package com.rockwellcollins.cs.hcms.core.profiling;

import com.rockwellcollins.cs.hcms.core.Component;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;

public class ProfileTest extends Component {

	private static final long serialVersionUID = 1L;

	private static final String PROFILE_TEST_POLL_TIME = "profile test poll time";

	long pollTime = 1000;
	long timestamp = 0;

	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {
		super.onInitialize(source, args);
		setSetupPriority(10);
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {
		super.onSetup(source, args);

		pollTime = getSetting(PROFILE_TEST_POLL_TIME, pollTime);
	}

	public void setPollTime(final long pollTime) {
		this.pollTime = pollTime;
	}

	public long getPollTime() {
		return pollTime;
	}

	public final ProfileResult run() {
		final ProfileResult result = new ProfileResult();
		onRun(result);
		return result;
	}

	protected void onRun(final ProfileResult result) {

	}
}
