package com.rockwellcollins.cs.hcms.core.collections;

import com.rockwellcollins.cs.hcms.core.CoreException;

/**
 * Queue Processor exception
 * @author getownse
 * @see QueueProcessor
 */
public class QueueProcessorException extends CoreException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create an exception
	 */
	public QueueProcessorException() {
		super();
	}

	/**
	 * Create an exception with error description and inner exception
	 * @param error error description
	 * @param exception inner exception
	 */
	public QueueProcessorException(final String error, final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Create an exception with error description
	 * @param error error description
	 */
	public QueueProcessorException(final String error) {
		super(error);
	}

	/**
	 * Create an exception with inner exception
	 * @param exception inner exception
	 */
	public QueueProcessorException(final Throwable exception) {
		super(exception);
	}
}
