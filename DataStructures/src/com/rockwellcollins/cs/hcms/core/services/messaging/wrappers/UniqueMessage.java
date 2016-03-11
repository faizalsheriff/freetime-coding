package com.rockwellcollins.cs.hcms.core.services.messaging.wrappers;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;

public class UniqueMessage extends CoreVersionMessage {

	public static final String JSON_ID = "_c_id";

	public static final String JSON_IP = "_c_ip";

	public static final String JSON_SENDER = "_c_sender";

	private static long idGenerator;

	public static long getNextId() {
		return ++idGenerator;
	}

	public UniqueMessage() {
		super();
		createUniqueMessage(this);
	}

	public UniqueMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

	public static Message createUniqueMessage(final Message message) {

		try {
			message.getJsonObject().put(JSON_ID, getNextId());
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}

		try {
			message.getJsonObject().put(JSON_IP,
					UnitManager.ObjectModel.getUnit().getIpAddress());
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}

		try {
			message.getJsonObject().put(JSON_SENDER, false);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}

		return message;
	}

	public void setSender(final boolean sender) {
		try {
			getJsonObject().put(JSON_SENDER, sender);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	public String getIp() {
		return getJsonObject().optString(JSON_IP, "");
	}

	public boolean isSender() {
		return getJsonObject().optBoolean(JSON_SENDER);
	}

	public long getId() {
		return getJsonObject().optLong(JSON_ID);
	}
}
