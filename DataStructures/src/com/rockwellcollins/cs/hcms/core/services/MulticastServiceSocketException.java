package com.rockwellcollins.cs.hcms.core.services;

import com.rockwellcollins.cs.hcms.core.CoreException;

public class MulticastServiceSocketException extends CoreException {

	private static final long serialVersionUID = 1L;

	public MulticastServiceSocketException() {
		super();
	}

	public MulticastServiceSocketException(final String error) {
		super(error);
	}

	public MulticastServiceSocketException(final String error,
			final Throwable exception) {
		super(error, exception);
	}

	public MulticastServiceSocketException(final Throwable exception) {
		super(exception);
	}
}
