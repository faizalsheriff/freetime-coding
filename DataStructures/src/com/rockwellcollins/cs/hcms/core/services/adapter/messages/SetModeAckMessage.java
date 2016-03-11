package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.adapter.AdapterMode;

public class SetModeAckMessage extends AdapterMessage {

	private static final String JSON_NEXT_ID = "next id";

	public SetModeAckMessage() {
		super();
		setType(AdapterMessageType.SET_MODE_ACK);
	}

	public SetModeAckMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

	public AdapterMode getMode() throws JSONException {
		return AdapterMode.valueOf(getJsonObject().getString(
				SetModeMessage.JSON_MODE));
	}

	public void setMode(final AdapterMode mode) {
		try {
			getJsonObject().put(SetModeMessage.JSON_MODE, mode);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	public long getNextId() {
		return getJsonObject().optLong(JSON_NEXT_ID);
	}

	public void setNextId(final long nextId) {
		try {
			getJsonObject().put(JSON_NEXT_ID, nextId);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}
}
