package com.rockwellcollins.cs.hcms.core;

import java.io.ObjectOutputStream;

/**
 * Arguments pass to component onSerialize event
 * @author getownse
 *
 */
public class ComponentSerializeArgs {

	private final ObjectOutputStream objOut;

	/**
	 * Creates a new argument with serialized output stream
	 * @param objOut stream that is being serialized
	 */
	public ComponentSerializeArgs(final ObjectOutputStream objOut) {

		if (objOut == null) {
			UnitManager.Logging
					.logSevere("ObjectOutputStream in ComponentSerializeArg is NULL");
		}

		this.objOut = objOut;
	}

	/**
	 * Returns the output stream that is being serialized
	 * @return output stream that is being serialized
	 */
	public final ObjectOutputStream getObjectOutputStream() {
		return this.objOut;
	}
}
