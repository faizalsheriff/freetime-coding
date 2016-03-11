package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.adapter.AdapterMode;

public class SetModeMessage extends AdapterMessage {

	public static final String JSON_MODE = "mode";

	public static final String JSON_TST_TIMEOUT = "tst timeout";

	public SetModeMessage() {
		super();
		setType(AdapterMessageType.SET_MODE);
	}

	public SetModeMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

	public AdapterMode getMode() throws JSONException {
		return AdapterMode.valueOf(getJsonObject().getString(JSON_MODE));
	}

	public void setMode(final AdapterMode mode) {
		try {
			getJsonObject().put(JSON_MODE, mode);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	public int getTstTimeout() {
		return getJsonObject().optInt(JSON_TST_TIMEOUT, 0);
	}

	public void setTstTimeout(final int tstTimeout) {
		try {
			getJsonObject().put(JSON_TST_TIMEOUT, tstTimeout);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	@Override
	public int getPort() {
		return getJsonObject().optInt(JSON_PORT);
	}

	@Override
	public void setPort(final int port) {
		try {
			getJsonObject().put(JSON_PORT, port);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}
}
