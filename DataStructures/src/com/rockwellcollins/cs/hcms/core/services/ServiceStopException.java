package com.rockwellcollins.cs.hcms.core.services;

public class ServiceStopException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public ServiceStopException() {
		super();
	}

	public ServiceStopException(final String error) {
		super(error);
	}

	public ServiceStopException(final String error, final Throwable exception) {
		super(error, exception);
	}

	public ServiceStopException(final Throwable exception) {
		super(exception);
	}
}
