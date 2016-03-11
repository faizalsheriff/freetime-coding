package com.rockwellcollins.cs.hcms.core;

/**
 * Component level base exception
 * @author getownse
 *
 */
public class ComponentException extends CoreException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new component exception
	 */
	public ComponentException() {
		super();
	}

	/**
	 * Create a new component exception with a descriptive error
	 * @param error descriptive error
	 */
	public ComponentException(final String error) {
		super(error);
	}

	/**
	 * Create a new component exception with a descriptive error and an inner exception
	 * @param error descriptive error
	 * @param exception inner exception
	 */
	public ComponentException(final String error, final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create a new component exception with inner exception
	 * @param exception inner exception
	 */
	public ComponentException(final Throwable exception) {
		super(exception);
	}
}
