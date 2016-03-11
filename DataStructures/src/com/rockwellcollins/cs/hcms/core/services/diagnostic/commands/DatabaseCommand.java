package com.rockwellcollins.cs.hcms.core.services.diagnostic.commands;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticSession;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;

public class DatabaseCommand extends CommandTemplate {

	private static final long serialVersionUID = 1L;

	public String executeCommand(final DiagnosticSession session,
			final String command, final String args) {

		final StringBuilder sb = new StringBuilder();
		final String[] argSplit = args.split(" ");

		final StateManager sm = UnitManager.ObjectModel.getStateManager();

		if (sm == null) {

			sb.append("Could not find State Manager.");

		} else {
			final StateDatabase db = sm.getStateDatabase();

			if (argSplit.length == 2) {
				if ("get".equals(argSplit[0])) {
	
					int index = db.getIndex(argSplit[1]);
	
					if (index != -1) {
						sb.append("Name: " + db.getName(index) + "\n");
						sb.append("Type: " + db.getType(index) + "\n");
						sb.append("Value: " + db.getValue(index) + "\n");
						sb
								.append("Description: " + db.getDescription(index)
										+ "\n");
						sb.append("Value Crc: " + db.getValueCrc(index, true)
								+ "\n");
						sb.append("Structure Crc: " + db.getStateCrc(index, true)
								+ "\n");
						sb
								.append("Last Updated: " + db.getUpdateTime(index)
										+ "\n");
						sb.append("Rule Name: " + db.getRule(index));
					} else {
						sb.append("State not found.");
					}
	
				} else if ("list".equals(argSplit[0])) {
	
					sb.append(String.format("%1$-30s %2$-10s %3$-30s %4$-20s\n\n",
							"NAME", "TYPE", "VALUE", "RULE"));
	
					int size = db.getSize();
	
					for (int i = 0; i < size; i++) {
	
						sb.append(String.format(
								"%1$-30s %2$-10s %3$-30s %4$-20s\n", db.getName(i),
								db.getType(i), db.getValue(i), db.getRule(i)));
					}
	
				} else {
					sb.append(getCommandHelp());
				}
	
			} else if (argSplit.length > 2) {
	
				if ("set".equals(argSplit[0])) {
	
					final String state = argSplit[1];
					final StringBuilder valueSb = new StringBuilder();
	
					valueSb.append(argSplit[2]);
	
					for (int i = 3; i < argSplit.length; i++) {
						valueSb.append(" " + argSplit[i]);
					}
	
					final String value = valueSb.toString();
	
					final StateMap stateMap = new StateMap();
					stateMap.put(state, value);
	
					sb.append("Setting '" + state + "' to '" + value + "'");
	
					sm.requestStateChange("DatabaseCommand", stateMap);
	
				} else if ("force".equals(argSplit[0])) {
	
					final String state = argSplit[1];
					final StringBuilder valueSb = new StringBuilder();
	
					valueSb.append(argSplit[2]);
	
					for (int i = 3; i < argSplit.length; i++) {
						valueSb.append(" " + argSplit[i]);
					}
	
					final String value = valueSb.toString();
	
					db.setValue(db.getIndex(state), value, true);
	
					sb.append("Forced'" + state + "' to '" + value
							+ "' and updating time.");
				} else {
	
					sb.append(getCommandHelp());
	
				}
	
			} else {
	
				sb.append(getCommandHelp());
	
			}
		}

		return sb.toString();
	}

	public String getCommandDescription() {
		return "Database commands";
	}

	public String getCommandHelp() {
		return "db get [stateName]\ndb list [regExp]\ndb set [name] [value]";
	}

	public String[] getCommandNames() {
		return new String[] { "db" };
	}

}
