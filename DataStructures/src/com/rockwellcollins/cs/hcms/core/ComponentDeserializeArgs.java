package com.rockwellcollins.cs.hcms.core;

import java.io.ObjectInputStream;

/**
 * Argument that is passed when a Component calls it's onDeserialized method
 * 
 * @author getownse
 * @see Component
 */
public class ComponentDeserializeArgs {

	private final ObjectInputStream objIn;

	/**
	 * Creates a ComponentDeserializeArgs with given Object Input Stream
	 * @param objIn object stream with serialized information
	 */
	public ComponentDeserializeArgs(final ObjectInputStream objIn) {
		if (objIn == null) {
			UnitManager.Logging
					.logSevere("ObjectInputStream in ComponentDeserializeArg is NULL");
		}
		this.objIn = objIn;
	}

	/**
	 * @return ObjectInputStream that the deserializer is uses
	 */
	public final ObjectInputStream getObjectInputStream() {
		return this.objIn;
	}
}
