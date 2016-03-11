package com.rockwellcollins.cs.hcms.core;

/**
 * Exception for onDestroy component event
 * @author getownse
 *
 */
public class ComponentDestroyException extends ComponentException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new exception
	 */
	public ComponentDestroyException() {
		super();
	}

	/**
	 * Create a new exception with descriptive error
	 * @param error error string
	 */
	public ComponentDestroyException(final String error) {
		super(error);
	}

	/**
	 * Create a new exception with descriptive error and an inner exception
	 * @param error descriptive error
	 * @param exception inner exception
	 */
	public ComponentDestroyException(final String error,
			final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create a new exception with inner exception
	 * @param exception inner exception
	 */
	public ComponentDestroyException(final Throwable exception) {
		super(exception);
	}
}
