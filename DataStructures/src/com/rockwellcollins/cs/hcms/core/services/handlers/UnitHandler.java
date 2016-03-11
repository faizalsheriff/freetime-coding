package com.rockwellcollins.cs.hcms.core.services.handlers;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;

public class UnitHandler extends Handler {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_POLL_TIME = 5000;

	private static final String PROPERTY_LOG_DIRECTORY = "unit log directory";

	private static final String PROPERTY_IP_ADDRESS = "unit ip address";

	private static final String SETTING_POLL_TIME = "unit handler poll time";

	private int pollTime;

	private transient CountdownTimer pollTimer;

	protected final int getPollTime() {
		return pollTime;
	}

	protected final void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}

	protected final CountdownTimer getPollTimer() {
		if (pollTimer == null) {
			pollTimer = new CountdownTimer();
			pollTimer.startTimer(getPollTime());
		}
		return pollTimer;
	}

	@Override
	protected void onStarted(Object source, ServiceStartArgs args)
			throws ServiceStartException {
		super.onStarted(source, args);

		updateProperties();
	}

	@Override
	protected void onPropertyChangedTimeout(Object source,
			PropertyChangeTimeoutArgs args) {
		super.onPropertyChangedTimeout(source, args);

		if (getPollTimer().hasExpired()) {
			updateProperties();
			getPollTimer().startTimer(getPollTime());
		}
	}

	protected void updateProperties() {

		updateProperty(PROPERTY_LOG_DIRECTORY, UnitManager.ObjectModel
				.getUnit().getLogDirectory().getAbsolutePath());

		updateProperty(PROPERTY_IP_ADDRESS, UnitManager.ObjectModel.getUnit()
				.getIpAddress());
	}

	protected void updateProperty(String property, String value) {
		try {
			if (hasProperty(property) && !getProperty(property).equals(value)) {
				setProperty(property, value);
			}
		} catch (HandlerPropertyNotFoundException e) {
			// Ignore
		}
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		pollTime = getSetting(SETTING_POLL_TIME, DEFAULT_POLL_TIME);
	}
}
