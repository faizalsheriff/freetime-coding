package com.rockwellcollins.cs.hcms.core.services.diagnostic.commands;

import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticService;

public abstract class CommandTemplate implements Command {

	private static final long serialVersionUID = 1L;

	DiagnosticService service;

	public DiagnosticService getService() {
		return service;
	}

	public void onCommandLoad(final DiagnosticService source) {
		this.service = source;
	}
}
