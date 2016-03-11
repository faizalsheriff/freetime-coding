package com.rockwellcollins.cs.hcms.core.services.messaging.filters;

import org.json.JSONException;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;
import com.rockwellcollins.cs.hcms.core.services.messaging.MessageFilter;
import com.rockwellcollins.cs.hcms.core.services.messaging.wrappers.CoreVersionMessage;

public class CoreVersionMessageFilter implements MessageFilter {
	
	private final int version;

	public CoreVersionMessageFilter(final int version) {
		this.version = version;
	}

	public void receivingMessage(final Message message) {
	}

	public void sendingMessage(final Message message) {
		try {
			message.getJsonObject().put(CoreVersionMessage.JSON_CORE_VERSION,
					version);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}
}
