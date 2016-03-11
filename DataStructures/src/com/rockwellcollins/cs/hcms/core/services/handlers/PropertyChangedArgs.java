package com.rockwellcollins.cs.hcms.core.services.handlers;

public class PropertyChangedArgs {

	private String propertyName;

	private String propertyValue;

	public PropertyChangedArgs(final String propertyName,
			final String propertyValue) {
		setPropertyName(propertyName);
		setPropertyValue(propertyValue);
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	protected void setPropertyName(final String propertyName) {
		this.propertyName = propertyName;
	}

	protected void setPropertyValue(final String propertyValue) {
		this.propertyValue = propertyValue;
	}
}
