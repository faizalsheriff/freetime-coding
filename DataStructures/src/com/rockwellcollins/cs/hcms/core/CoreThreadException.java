package com.rockwellcollins.cs.hcms.core;

/**
 * Core thread exception
 * @author getownse
 *
 */
public class CoreThreadException extends CoreException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create new exception
	 */
	public CoreThreadException() {
		super();
	}

	/**
	 * Create new exception with descriptive error
	 * @param error descriptive error
	 */
	public CoreThreadException(final String error) {
		super(error);
	}

	/**
	 * Create new exception with descriptive error and inner exception
	 * @param error descriptive error
	 * @param exception inner exception
	 */
	public CoreThreadException(final String error, final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create a new exception with inner exception
	 * @param exception inner exception
	 */
	public CoreThreadException(final Throwable exception) {
		super(exception);
	}
}
