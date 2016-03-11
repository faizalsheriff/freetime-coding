package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;

/**
 * An Adapter Message is sent between an adapter and the Test and Simulation
 * Tool. The purpose of the message is to be able to control/simulate the
 * Adapter remotely.
 * 
 * @author getownse
 * 
 */
public abstract class AdapterMessage extends Message {

	/**
	 * The JSON key for Message Type
	 */
	public static final String JSON_TYPE = "type";

	/**
	 * The JSON key for Message ID
	 */
	public static final String JSON_ID = "id";

	/**
	 * The JSON key for Message Port
	 */
	public static final String JSON_PORT = "port";

	/**
	 * The JSON key for Adapter Name
	 */
	public static final String JSON_ADAPTER_NAME = "adapter name";

	/**
	 * The JSON key for Adapter IP
	 */
	public static final String JSON_ADAPTER_IP = "adapter ip";

	/**
	 * Instantiate an Adapter Message
	 */
	public AdapterMessage() {
		super();
	}

	/**
	 * Wrap an Adapter Message with given JSON object
	 * 
	 * @param jsonObject
	 *            JSON object to wrap current AdapterMessage
	 */
	public AdapterMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

	/**
	 * Get the Message ID
	 * 
	 * @return Message ID
	 */
	public long getId() {
		return getJsonObject().optLong(JSON_ID);
	}

	/**
	 * Set the Message ID
	 * 
	 * @param id
	 */
	public void setId(final long id) {
		try {
			getJsonObject().put(JSON_ID, id);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	/**
	 * Set the Message Type
	 * 
	 * @param type
	 *            Message Type
	 */
	public void setType(final AdapterMessageType type) {
		try {
			getJsonObject().put(JSON_TYPE, type.toString());
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	/**
	 * Get the Message Port
	 * 
	 * @return Message Port
	 */
	public int getPort() {
		return getJsonObject().optInt(JSON_PORT);
	}

	/**
	 * Set the Message Port
	 * 
	 * @param port
	 */
	public void setPort(final int port) {
		try {
			getJsonObject().put(JSON_PORT, port);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	/**
	 * Get the Adapter Name
	 * 
	 * @return Adapter Name
	 */
	public String getAdapterName() {
		return getJsonObject().optString(JSON_ADAPTER_NAME);
	}

	/**
	 * Get the Adapter Name for given Message
	 * 
	 * @param message
	 * @return Adapter Name
	 */
	static public String getAdapterName(final Message message) {
		return message.getJsonObject().optString(JSON_ADAPTER_NAME);
	}

	/**
	 * Set the Adapter Name
	 * 
	 * @param adapterName
	 */
	public void setAdapterName(final String adapterName) {
		try {
			getJsonObject().put(JSON_ADAPTER_NAME, adapterName);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	/**
	 * Get the Adapter IP
	 * 
	 * @return Adatper IP
	 */
	public String getAdapterIp() {
		return getJsonObject().optString(JSON_ADAPTER_IP);
	}

	/**
	 * Get the Adapter IP
	 * 
	 * @param message
	 * @return Adapter IP
	 */
	static public String getAdapterIp(final Message message) {
		return message.getJsonObject().optString(JSON_ADAPTER_IP);
	}

	/**
	 * Set the Adapter IP
	 * 
	 * @param adapterIp
	 */
	public void setAdapterIp(final String adapterIp) {
		try {
			getJsonObject().put(JSON_ADAPTER_IP, adapterIp);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	/**
	 * Return the Adapter Message type.
	 * 
	 * @return Adapter Message Type
	 */
	public AdapterMessageType getType() {
		return getType(this);
	}

	/**
	 * Get the Adapter Message Type.
	 * 
	 * @param message
	 * @return Adapter Message Type
	 */
	static public AdapterMessageType getType(final Message message) {
		return AdapterMessageType.valueOf(message.getJsonObject().optString(
				JSON_TYPE));
	}
}
