package com.rockwellcollins.cs.hcms.core.services.update.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;
import com.rockwellcollins.cs.hcms.core.services.messaging.wrappers.CoreVersionMessage;

/**
 * The Class UpdateServiceMessage acts as the super class for all the message
 * types used within Update Service. It adds the message type and the message
 * version for every message type.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see CAMessage
 * @see LAMessage
 * @see SAMessage
 * @see UpdateServiceMessageTypes
 * @see CoreVersionMessage
 * 
 */
public class UpdateServiceMessage extends CoreVersionMessage {

	/** The Constant JSON_TYPE. */
	public static final String JSON_TYPE = "type";

	/** The Constant JSON_VERSION. */
	public static final String JSON_VERSION = "message version";

	/**
	 * Instantiates a new update service message.
	 */
	public UpdateServiceMessage() {
		super();
	}

	/**
	 * Instantiates a new update service message.
	 * 
	 * @param jsonObject the json object
	 */
	public UpdateServiceMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public UpdateServiceMessageTypes getType() {
		return getType(this);
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	public void setType(final UpdateServiceMessageTypes type) {
		try {
			getJsonObject().put(JSON_TYPE, type.toString());
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	/**
	 * Gets the type.
	 * 
	 * @param message the message
	 * 
	 * @return the type
	 */
	public static UpdateServiceMessageTypes getType(final Message message) {
		return UpdateServiceMessageTypes.valueOf(message.getJsonObject().optString(JSON_TYPE));
	}

	/**
	 * Gets the message version.
	 * 
	 * @return the message version
	 */
	public String getMessageVersion() {
		return getJsonObject().optString(JSON_VERSION);
	}

	/**
	 * Sets the message version.
	 * 
	 * @param version the new message version
	 */
	public void setMessageVersion(final String version) {
		try {
			getJsonObject().put(JSON_VERSION, version);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	/**
	 * Gets the message version.
	 * 
	 * @param jobj the JSON object
	 * 
	 * @return the message version
	 */
	public static String getMessageVersion(final JSONObject jobj) {
		return jobj.optString(JSON_VERSION);
	}
}
