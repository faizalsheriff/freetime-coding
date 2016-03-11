package com.rockwellcollins.cs.hcms.core.services.handlers;

public class PropertyMapChangedArgs {

	private PropertyMap propertyMap;

	public PropertyMapChangedArgs(final PropertyMap propertyMap) {
		setPropertyMap(propertyMap);
	}

	public PropertyMapChangedArgs() {
	}

	public PropertyMap getPropertyMap() {
		return propertyMap;
	}

	protected void setPropertyMap(final PropertyMap propertyMap) {
		this.propertyMap = propertyMap;
	}
}
