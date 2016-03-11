package com.rockwellcollins.cs.hcms.core.services.messaging;

import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.RelayStopWatch;

public class Message {

	private RelayStopWatch profileWatch;

	private JSONObject jsonObject;

	public Message() {
	}

	public Message(final JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public void startProfile() {
		if (profileWatch == null) {
			profileWatch = new RelayStopWatch();
		}
		profileWatch.reset();
	}

	public void addProfile(final String event) {
		if (profileWatch != null) {
			profileWatch.mark(event);
		}
	}

	public void stopProfile() {
		if (profileWatch != null) {
			profileWatch.stop();
			UnitManager.Logging.logStdOut("PROFILE: '"
					+ profileWatch.toString() + "' for message '" + toString()
					+ "'");
		}
	}

	public JSONObject getJsonObject() {
		if (jsonObject == null) {
			jsonObject = new JSONObject();
		}
		return jsonObject;
	}

	public RelayStopWatch getProfileWatch() {
		return profileWatch;
	}

	@Override
	public String toString() {
		return getJsonObject().toString();
	}
}
