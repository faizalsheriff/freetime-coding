package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONObject;

public class GetAdaptersMessage extends AdapterMessage {

	public GetAdaptersMessage() {
		super();
		setType(AdapterMessageType.GET_ADAPTERS);
	}

	public GetAdaptersMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}
}
