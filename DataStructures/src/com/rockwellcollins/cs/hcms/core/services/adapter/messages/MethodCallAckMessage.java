package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONObject;

public class MethodCallAckMessage extends AdapterMessage {

	public MethodCallAckMessage() {
		super();
		setType(AdapterMessageType.METHOD_CALL_ACK);
	}

	public MethodCallAckMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

}
