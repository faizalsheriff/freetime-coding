package com.rockwellcollins.cs.hcms.core.services.messaging.wrappers;

import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.services.messaging.Message;

public class CoreVersionMessage extends Message {

	public static final String JSON_CORE_VERSION = "_c_version";

	public CoreVersionMessage() {
		super();
	}

	public CoreVersionMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

	public double getCoreVersion() {
		return getJsonObject().optDouble(JSON_CORE_VERSION);
	}
}
