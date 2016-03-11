package com.rockwellcollins.cs.hcms.core;

/**
 * Base exception for the Core Framework
 * @author getownse
 *
 */
public class CoreException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new core exception 
	 */
	public CoreException() {
		super();
	}

	/**
	 * Create a new core exception with descriptive error
	 * @param error descriptive error
	 */
	public CoreException(final String error) {
		super(error);
	}

	/**
	 * Create a new core exception with descriptive error and inner exception
	 * @param error descriptive error
	 * @param exception inner exception
	 */
	public CoreException(final String error, final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create a new core exception with inner exception
	 * @param exception inner exception
	 */
	public CoreException(final Throwable exception) {
		super(exception);
	}

}
