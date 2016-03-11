package com.rockwellcollins.cs.hcms.core.services;

public class ServiceIOException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public ServiceIOException() {
		super();
	}

	public ServiceIOException(final String error) {
		super(error);
	}

	public ServiceIOException(final String error, final Throwable exception) {
		super(error, exception);
	}

	public ServiceIOException(final Throwable exception) {
		super(exception);
	}
}
