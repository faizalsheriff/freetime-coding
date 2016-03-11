package com.rockwellcollins.cs.hcms.core.services.diagnostic.commands;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticSession;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;

public class StateManagerCommand extends CommandTemplate {

	private static final long serialVersionUID = 1L;

	public String executeCommand(final DiagnosticSession session,
			final String command, final String args) {

		final StringBuilder sb = new StringBuilder();
		final String[] argSplit = args.split(" ");

		final StateManager sm = UnitManager.ObjectModel.getStateManager();

		if (sm == null) {

			sb.append("Could not find State Manager.");

		} else if (argSplit.length == 1) {

			if ("reject".equals(argSplit[0])) {
				sb.append("Rejecting State Manager '" + sm.getName() + "'");
				UnitManager.ObjectModel.getStateManager().masterReject();
			} else {
				sb.append(getCommandHelp());
			}

		} else {

			sb.append(getCommandHelp());

		}

		return sb.toString();
	}

	public String getCommandDescription() {
		return "State Manager Commands";
	}

	public String getCommandHelp() {
		return "sm [reject]";
	}

	public String[] getCommandNames() {
		return new String[] { "sm" };
	}
}
