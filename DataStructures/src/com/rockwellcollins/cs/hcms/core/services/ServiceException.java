package com.rockwellcollins.cs.hcms.core.services;

import com.rockwellcollins.cs.hcms.core.CoreException;

public class ServiceException extends CoreException {

	private static final long serialVersionUID = 1L;

	public ServiceException() {
		super();
	}

	public ServiceException(final String error) {
		super(error);
	}

	public ServiceException(final String error, final Throwable exception) {
		super(error, exception);
	}

	public ServiceException(final Throwable exception) {
		super(exception);
	}
}
