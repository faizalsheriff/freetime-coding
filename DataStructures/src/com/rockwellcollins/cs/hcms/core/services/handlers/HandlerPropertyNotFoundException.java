package com.rockwellcollins.cs.hcms.core.services.handlers;

import com.rockwellcollins.cs.hcms.core.CoreException;

public class HandlerPropertyNotFoundException extends CoreException {

	private static final long serialVersionUID = 1L;

	public HandlerPropertyNotFoundException() {
		super();
	}

	public HandlerPropertyNotFoundException(final String error,
			final Throwable exception) {
		super(error, exception);
	}

	public HandlerPropertyNotFoundException(final String error) {
		super(error);
	}

	public HandlerPropertyNotFoundException(final Throwable exception) {
		super(exception);
	}

}
