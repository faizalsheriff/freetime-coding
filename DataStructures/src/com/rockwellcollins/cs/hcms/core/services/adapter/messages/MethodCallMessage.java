package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;

public class MethodCallMessage extends AdapterMessage {

	public static final String JSON_METHOD_NAME = "method name";

	public static final String JSON_METHOD_PARAMETERS = "method parameters";

	public static final String JSON_METHOD_DIRECTION = "method direction";

	public static final String JSON_RETURN_PARAMETER = "return parameter";

	public static final String JSON_SIMULATED = "simulated";

	public MethodCallMessage() {
		super();
		setType(AdapterMessageType.METHOD_CALL);
	}

	public MethodCallMessage(final JSONObject jsonObject) {
		super(jsonObject);
	}

	public String getMethodName() {
		return getJsonObject().optString(JSON_METHOD_NAME);
	}

	public void setMethodName(final String methodName) {
		try {
			getJsonObject().put(JSON_METHOD_NAME, methodName);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	public MethodCallDirection getDirection() {
		return MethodCallDirection.valueOf(getJsonObject().optString(
				JSON_METHOD_DIRECTION));
	}

	public void setDirection(final MethodCallDirection direction) {
		try {
			getJsonObject().put(JSON_METHOD_DIRECTION, direction.toString());
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	public boolean isSimulated() {
		return getJsonObject().optBoolean(JSON_SIMULATED, false);
	}

	public void setSimulated(final boolean simulated) {
		try {
			getJsonObject().put(JSON_SIMULATED, simulated);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	public Parameter getReturnValue() throws Exception {
		return new Parameter(getJsonObject().optJSONObject(
				JSON_RETURN_PARAMETER));
	}

	public void setReturnValue(final Parameter returnValue) {
		try {
			final JSONObject object = new JSONObject().put(returnValue
					.getType().toString(), returnValue.getValue());
			getJsonObject().put(JSON_RETURN_PARAMETER, object);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	public Parameter[] getParameters() throws Exception {

		final JSONArray jobj = getJsonObject().optJSONArray(
				JSON_METHOD_PARAMETERS);
		final Parameter[] parameters = new Parameter[jobj.length()];

		for (int index = 0; index < jobj.length(); index++) {
			try {
				parameters[index] = new Parameter(jobj.getJSONObject(index));
			} catch (final JSONException e) {
				UnitManager.Logging.logSevere(e);
			}
		}
		return parameters;
	}

	public void setParameters(final Parameter[] parameters) {
		try {
			final JSONArray jsonArray = new JSONArray();
			for (final Parameter element : parameters) {
				jsonArray.put(new JSONObject().put(
						element.getType().toString(), element.getValue()));
			}
			getJsonObject().put(JSON_METHOD_PARAMETERS, jsonArray);
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(e);
		}
	}
}
