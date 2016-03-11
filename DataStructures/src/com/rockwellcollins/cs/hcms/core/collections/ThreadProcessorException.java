package com.rockwellcollins.cs.hcms.core.collections;

import com.rockwellcollins.cs.hcms.core.CoreException;

/**
 * Thread Processor exception
 * @author getownse
 * @see ThreadProcessor
 */
public class ThreadProcessorException extends CoreException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create new exception
	 */
	public ThreadProcessorException() {
		super();
	}

	/**
	 * Create new exception with error description and inner exception
	 * @param error error description
	 * @param exception inner exception
	 */
	public ThreadProcessorException(final String error,
			final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create new exception with error description
	 * @param error error description
	 */
	public ThreadProcessorException(final String error) {
		super(error);
	}

	/**
	 * Create new exception with inner exception
	 * @param exception inner exception
	 */
	public ThreadProcessorException(final Throwable exception) {
		super(exception);
	}
}
