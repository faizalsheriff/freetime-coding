package com.rockwellcollins.cs.hcms.core.services;

public class ServiceStartException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public ServiceStartException() {
		super();
	}

	public ServiceStartException(final String error) {
		super(error);
	}

	public ServiceStartException(final String error, final Throwable exception) {
		super(error, exception);
	}

	public ServiceStartException(final Throwable exception) {
		super(exception);
	}
}
