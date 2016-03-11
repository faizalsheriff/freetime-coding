package com.rockwellcollins.cs.hcms.core.services.diagnostic.commands;

import java.util.Map;

import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticSession;

public class ListCommand extends CommandTemplate {

	private static final long serialVersionUID = 1L;

	public String executeCommand(final DiagnosticSession session,
			final String command, final String args) {

		final StringBuilder sb = new StringBuilder();

		if ("commands".equals(args)) {
			sb
					.append(String.format("%1$-15s %2$s\n", "Command",
							"Description"));
			for (final Map.Entry<String, Command> entry : session.getService()
					.getCommands().entrySet()) {

				sb.append(String.format("%1$-15s %2$s\n", entry.getKey(), entry
						.getValue().getCommandDescription()));
			}
		} else {
			sb.append(getCommandHelp());
		}
		return sb.toString();
	}

	public String getCommandDescription() {
		return "Lists information about certain collections";
	}

	public String getCommandHelp() {
		return "list [arguments]\n" + "  commands : list all commands.";
	}

	public String[] getCommandNames() {
		return new String[] { "list" };
	}

}
