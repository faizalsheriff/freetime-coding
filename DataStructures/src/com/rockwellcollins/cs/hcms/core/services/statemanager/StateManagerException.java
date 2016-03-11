package com.rockwellcollins.cs.hcms.core.services.statemanager;

import com.rockwellcollins.cs.hcms.core.CoreException;

public class StateManagerException extends CoreException {

	private static final long serialVersionUID = 1L;

	public StateManagerException() {
		super();
	}

	public StateManagerException(final String error, final Throwable exception) {
		super(error, exception);
	}

	public StateManagerException(final String error) {
		super(error);
	}

	public StateManagerException(final Throwable exception) {
		super(exception);
	}
}
