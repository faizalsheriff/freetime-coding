package com.rockwellcollins.cs.hcms.core.services.statemanager.rules;

import com.rockwellcollins.cs.hcms.core.CoreException;

/**
 * The Class RulesEngineException.
 */
public class RulesEngineException extends CoreException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new rules engine exception.
	 */
	public RulesEngineException() {
		super();
	}

	/**
	 * Instantiates a new rules engine exception.
	 * 
	 * @param error the error
	 * @param exception the exception
	 */
	public RulesEngineException(final String error, final Throwable exception) {
		super(error, exception);
	}

	/**
	 * Instantiates a new rules engine exception.
	 * 
	 * @param error the error
	 */
	public RulesEngineException(final String error) {
		super(error);
	}

	/**
	 * Instantiates a new rules engine exception.
	 * 
	 * @param exception the exception
	 */
	public RulesEngineException(final Throwable exception) {
		super(exception);
	}
}
