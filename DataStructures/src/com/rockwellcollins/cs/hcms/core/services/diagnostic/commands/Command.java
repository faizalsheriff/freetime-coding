package com.rockwellcollins.cs.hcms.core.services.diagnostic.commands;

import java.io.Serializable;

import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticService;
import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticSession;

public interface Command extends Serializable {
	String[] getCommandNames();

	String getCommandHelp();

	String getCommandDescription();

	String executeCommand(DiagnosticSession session, String command, String args);

	void onCommandLoad(DiagnosticService source);
}
