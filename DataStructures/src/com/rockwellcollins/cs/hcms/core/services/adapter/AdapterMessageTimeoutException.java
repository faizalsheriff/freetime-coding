package com.rockwellcollins.cs.hcms.core.services.adapter;

/**
 * Unique adapter message timeout exception for notifying child adapters that an
 * attempted method call message has timed out while waiting for a simulated
 * response.
 * 
 * @author tmcrane
 * 
 */
public class AdapterMessageTimeoutException extends Exception {

	private static final long serialVersionUID = -8714633135774013470L;

	/**
	 * @param message
	 */
	public AdapterMessageTimeoutException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AdapterMessageTimeoutException(final String message,
			final Throwable cause) {
		super(message, cause);
	}
}
