package com.rockwellcollins.cs.hcms.core.services;

public class ServiceRunException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public ServiceRunException() {
		super();
	}

	public ServiceRunException(final String error) {
		super(error);
	}

	public ServiceRunException(final String error, final Throwable exception) {
		super(error, exception);
	}

	public ServiceRunException(final Throwable exception) {
		super(exception);
	}
}
