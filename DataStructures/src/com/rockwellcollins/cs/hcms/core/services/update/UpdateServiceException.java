package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.CoreException;

/**
 * The Class UpdateServiceException is a customized exception class used for
 * Update Service related exceptions.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * 
 */
public class UpdateServiceException extends CoreException {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new update service exception.
	 */
	public UpdateServiceException() {
		super();
	}

	/**
	 * Instantiates a new update service exception.
	 * 
	 * @param error the error
	 */
	public UpdateServiceException(final String error) {
		super(error);
	}

	/**
	 * Instantiates a new update service exception.
	 * 
	 * @param error the error
	 * @param exception the exception
	 */
	public UpdateServiceException(final String error, final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Instantiates a new update service exception.
	 * 
	 * @param exception the exception
	 */
	public UpdateServiceException(final Throwable exception) {
		super(exception);
	}
}
