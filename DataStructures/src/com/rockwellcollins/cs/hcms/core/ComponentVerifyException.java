package com.rockwellcollins.cs.hcms.core;

/**
 * Exception that is thrown from component onSetup event
 * @author getownse
 *
 */
public class ComponentVerifyException extends CoreException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new exception
	 */
	public ComponentVerifyException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a new exception with descriptive error
	 * @param error descriptive error
	 */
	public ComponentVerifyException(final String error) {
		super(error);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a new exception with descriptive error and inner exception
	 * @param error descriptive error
	 * @param exception inner exception
	 */
	public ComponentVerifyException(final String error,
			final Throwable exception) {
		super(error, exception);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a new exception with inner exception
	 * @param exception inner exception
	 */
	public ComponentVerifyException(final Throwable exception) {
		super(exception);
		// TODO Auto-generated constructor stub
	}

}
