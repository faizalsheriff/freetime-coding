package com.rockwellcollins.cs.hcms.core.services.diagnostic.commands;

import java.util.Map;

import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticSession;

public class ThreadCommand extends CommandTemplate {

	private static final long serialVersionUID = 1L;

	public String executeCommand(final DiagnosticSession session,
			final String command, final String args) {

		final StringBuilder sb = new StringBuilder();

		sb.append("command: " + command + "\n");
		sb.append("args: " + args + "\n");

		final Map<Thread, StackTraceElement[]> threadInfo = Thread
				.getAllStackTraces();

		sb.append(String.format("[%1$5s] [%2$3s] [%3$15s] %4$s", "ID", "PRI",
				"STATE", "NAME\n"));

		for (final Thread thread : threadInfo.keySet()) {

			sb.append(String.format("[%1$5s] [%2$3s] [%3$15s] %4$s", thread
					.getId(), thread.getPriority(), thread.getState(), thread
					.getName())
					+ "\n");

			if (args.contains("all")) {
				for (final StackTraceElement ste : threadInfo.get(thread)) {
					sb.append("\t" + ste.toString() + "\n");
				}
			}
		}

		return sb.toString();
	}

	public String[] getCommandNames() {
		return new String[] { "th" };
	}

	public String getCommandDescription() {
		return "Displays Thread Information";
	}

	public String getCommandHelp() {
		return "More help to come for threads.";
	}
}
