package com.rockwellcollins.cs.hcms.core.services.adapter.messages;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;;

public class Parameter {
	public static final String JSON_PARAMTER_TYPE = "parameter type";

	public static final String JSON_PAREMETER_VALUE = "parameter value";

	private ParameterType type;

	private String name;

	private Object value;

	public Parameter(final String name, final ParameterType type) {
		this.name = name;
		this.type = type;
	}

	public Parameter(final JSONObject jsonObject) throws Exception {
		for (final String type : JSONObject.getNames(jsonObject)) {
			this.type = ParameterType.valueOf(type);
			setValue(jsonObject);
		}
	}

	public String getName() {
		return this.name;
	}

	public ParameterType getType() {
		return this.type;
	}

	public void setValue(final JSONObject value) throws Exception {
		switch (type) {
		case BYTE:
			setByteValue((byte) value.getInt(this.type.toString()));
			break;
		case BYTE_ARRAY:

			if (value.get(type.toString()).getClass().equals(JSONArray.class)) {
				final JSONArray array = value.getJSONArray(type.toString());
				final byte[] bytes = new byte[array.length()];
				for (int i = 0; i < array.length(); i++) {
					bytes[i] = (byte) array.optInt(i);
				}
				setByteArrayValue(bytes);
			} else {
				setByteArrayValue((byte[]) value.get(type.toString()));
			}
			break;
		case DOUBLE:
			setDoubleValue(value.getDouble(type.toString()));
			break;
		case DOUBLE_ARRAY:
			if (value.getClass().equals(JSONArray.class)) {
				final JSONArray array = value.getJSONArray(type.toString());
				final double[] newArray = new double[array.length()];
				for (int i = 0; i < array.length(); i++) {
					newArray[i] = (byte) array.optInt(i);
				}
				setDoubleArrayValue(newArray);
			} else {
				setDoubleArrayValue((double[]) value.get(type.toString()));
			}
			break;
		case INT:
			setIntegerValue(value.getInt(type.toString()));
			break;
		case INT_ARRAY:
			if (value.getClass().equals(JSONArray.class)) {
				final JSONArray array = value.getJSONArray(type.toString());
				final int[] newArray = new int[array.length()];
				for (int i = 0; i < array.length(); i++) {
					newArray[i] = (byte) array.optInt(i);
				}
				setIntegerArrayValue(newArray);
			} else {
				setIntegerArrayValue((int[]) value.get(type.toString()));
			}
			break;
		case LONG:
			setLongValue(value.getLong(type.toString()));
			break;
		case LONG_ARRAY:
			if (value.getClass().equals(JSONArray.class)) {
				final JSONArray array = value.getJSONArray(type.toString());
				final long[] newArray = new long[array.length()];
				for (int i = 0; i < array.length(); i++) {
					newArray[i] = (byte) array.optInt(i);
				}
				setLongArrayValue(newArray);
			} else {
				setLongArrayValue((long[]) value.get(type.toString()));
			}
			break;
		case STRING:
			setStringValue(value.getString(type.toString()));
			break;
		case STRING_ARRAY:
			if (value.getClass().equals(JSONArray.class)) {
				final JSONArray array = value.getJSONArray(type.toString());
				final String[] newArray = new String[array.length()];
				for (int i = 0; i < array.length(); i++) {
					newArray[i] = array.optString(i);
				}
				setStringArrayValue(newArray);
			} else {
				setStringArrayValue((String[]) value.get(type.toString()));
			}
			break;
		}
	}

	public Object getValue() {
		return this.value;
	}

	public int getIntegerValue() {
		return (Integer) this.value;
	}

	public void setIntegerValue(final int value) throws Exception {
		if (!type.equals(ParameterType.INT)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the integer value '" + value
					+ "' for the type " + getType());
		}
		this.value = value;
	}

	public int[] getIntegerArrayValue() {
		return (int[]) this.value;
	}

	public void setIntegerArrayValue(final int[] value) throws Exception {
		if (!type.equals(ParameterType.INT_ARRAY)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the integer array value '"
					+ Arrays.toString(value) + "' for the type " + getType());
		}
		this.value = value;
	}

	public byte getByteValue() {
		return (Byte) this.value;
	}

	public void setByteValue(final byte value) throws Exception {
		if (!type.equals(ParameterType.BYTE)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the byte value '" + value
					+ "' for the type " + getType());
		}
		this.value = value;
	}

	public byte[] getByteArrayValue() {
		return (byte[]) this.value;
	}

	public void setByteArrayValue(final byte[] bytes) throws Exception {
		if (!type.equals(ParameterType.BYTE_ARRAY)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the byte array value '" + value
					+ "' for the type " + getType());
		}
		this.value = bytes;
	}

	public double getDoubleValue() {
		return (Double) this.value;
	}

	public void setDoubleValue(final double value) throws Exception {
		if (!type.equals(ParameterType.DOUBLE)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the double value '" + value
					+ "' for the type " + getType());
		}
		this.value = value;
	}

	public double[] getDoubleArrayValue() {
		return (double[]) this.value;
	}

	public void setDoubleArrayValue(final double[] value) throws Exception {
		if (!type.equals(ParameterType.DOUBLE_ARRAY)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the double array value '"
					+ Arrays.toString(value) + "' for the type " + getType());
		}
		this.value = value;
	}

	public long getLongValue() {
		return (Long) this.value;
	}

	public void setLongValue(final long value) throws Exception {
		if (!type.equals(ParameterType.LONG)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the long value '" + value
					+ "' for the type " + getType());
		}
		this.value = value;
	}

	public long[] getLongArrayValue() {
		return (long[]) this.value;
	}

	public void setLongArrayValue(final long[] value) throws Exception {
		if (!type.equals(ParameterType.LONG_ARRAY)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the long array value '" 
					+ Arrays.toString(value)
					+ "' for the type " + getType());
		}
		this.value = value;
	}

	public String getStringValue() {
		return (String) this.value;
	}

	public void setStringValue(final String value) throws Exception {
		if (!type.equals(ParameterType.STRING)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the String value '" + value
					+ "' for the type " + getType());
		}
		this.value = value;
	}

	public void setStringArrayValue(final String[] value) throws Exception {
		if (!type.equals(ParameterType.STRING_ARRAY)) {
			throw new Exception("Invalid value type for parameter '" + name
					+ "':  Attempted to assign the String array value '"
					+ Arrays.toString(value) + "' for the type " + getType());
		}
		this.value = value;
	}
}
