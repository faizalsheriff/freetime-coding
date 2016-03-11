package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;

public class GetAdaptersAckMessage extends AdapterMessage {

	private static final String JSON_NEXT_ID = "next id";

	public GetAdaptersAckMessage() {
		super();
		setType(AdapterMessageType.GET_ADAPTERS_ACK);
	}

	public GetAdaptersAckMessage(final JSONObject jsonObject) {
		super(jsonObject);
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
