package com.rockwellcollins.cs.hcms.core;

/**
 * Exception that may occur when deserializing a component
 * @author getownse
 *
 */
public class ComponentDeserializeException extends ComponentException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a Component Deserialize Exception
	 */
	public ComponentDeserializeException() {
		super();
	}

	/**
	 * Create a Component Deserialize Exception with given error
	 * @param error error string
	 */
	public ComponentDeserializeException(final String error) {
		super(error);
	}

	/**
	 * Create a Component Deserialize Exceptoin with given error and inner exception
	 * @param error error test
	 * @param exception inner exception
	 */
	public ComponentDeserializeException(final String error,
			final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create a Component Deserialize Exception with given inner exception
	 * @param exception inner exception
	 */
	public ComponentDeserializeException(final Throwable exception) {
		super(exception);
	}
}
