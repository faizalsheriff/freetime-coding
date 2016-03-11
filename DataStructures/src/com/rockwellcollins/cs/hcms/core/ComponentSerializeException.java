package com.rockwellcollins.cs.hcms.core;

/**
 * Exception thrown in component onSerizlize event
 * @author getownse
 *
 */
public class ComponentSerializeException extends ComponentException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new exception
	 */
	public ComponentSerializeException() {
		super();
	}

	/**
	 * Create a new exception with descriptive error
	 * @param error descriptive error
	 */
	public ComponentSerializeException(final String error) {
		super(error);
	}

	/**
	 * Create a new exception with descriptive error and inner exception
	 * @param error descriptive error
	 * @param exception inner exception
	 */
	public ComponentSerializeException(final String error,
			final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create a new exception with inner exception
	 * @param exception inner exception
	 */
	public ComponentSerializeException(final Throwable exception) {
		super(exception);
	}

}
