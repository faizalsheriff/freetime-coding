package com.rockwellcollins.cs.hcms.core.services.diagnostic.commands;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.diagnostic.DiagnosticSession;
import com.rockwellcollins.cs.hcms.core.ComponentContainer;
import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.adapter.Adapter;

import java.util.*;

public class LogCommand extends CommandTemplate {

	private static final long serialVersionUID = 1L;

	private Map<String, String> comps = new HashMap<String, String>();

	
	public String executeCommand(final DiagnosticSession session,
			final String command, final String args) {

		final String[] splitArgs = args.split(" ");
		final StringBuilder sb = new StringBuilder();

		if (args.length() == 0 || splitArgs.length == 0
				|| splitArgs.length == 1 && "show".equals(splitArgs[0])) {

			sb.append(String.format("%1$-15s %2$s\n", "Log Type", "Status"));
			sb.append(String.format("%1$-15s %2$s\n", "Severe", "Always On"));
			sb.append(String.format("%1$-15s %2$s\n", "Warning", "Always On"));
			sb.append(String.format("%1$-15s %2$s\n", "UM Debug",
					UnitManager.Logging.isDebug()));
			sb.append(String.format("%1$-15s %2$s\n", "UM Info", 
					UnitManager.Logging.isInfo()));
			sb.append(String.format("%1$-15s %2$s\n", "UM Core", 
					UnitManager.Logging.isCore()));
			sb.append(String.format("%1$-15s %2$s\n", "UM Profile",
					UnitManager.Logging.isProfile()));
			sb.append(String.format("%1$-15s %2$s\n", "UM StdOut",
					UnitManager.Logging.isStdOut()));
			
			for (String entry: comps.keySet()) {
				sb.append(String.format("%1$-15s %2$s\n", entry, comps.get(entry)));
			}

		} else if (splitArgs.length == 2) {

			final boolean newSetting = "on".equals(splitArgs[1])
					|| "true".equals(splitArgs[1]);

			if ("info".equals(splitArgs[0])) {
				if (newSetting) {
					UnitManager.Logging.setInfo(newSetting);
					sb.append("Logging Info is now ON");
					if (UnitManager.Logging.isInfo()) {
						UnitManager.Logging
								.logInfo("Logging Info is now ON");
					}
				} else {
					sb.append("Logging Info is now OFF");
					if (UnitManager.Logging.isInfo()) {
						UnitManager.Logging
								.logInfo("Logging Info is now OFF");
						UnitManager.Logging.setInfo(newSetting);
					}
				}
			} else if ("debug".equals(splitArgs[0])) {
				if (newSetting) {
					UnitManager.Logging.setDebug(newSetting);
					sb.append("Logging Debug is now ON");
					if (UnitManager.Logging.isDebug()) {
						UnitManager.Logging
								.logDebug("Logging Debug is now ON");
					}
				} else {
					sb.append("Logging Debug is now OFF");
					if (UnitManager.Logging.isDebug()) {
						UnitManager.Logging
								.logDebug("Logging Debug is now OFF");
						UnitManager.Logging.setDebug(newSetting);
					}
				}
			} else if ("profile".equals(splitArgs[0])) {
				if (newSetting) {
					UnitManager.Logging.setProfile(newSetting);
					sb.append("Logging Profile is now ON");
					if (UnitManager.Logging.isProfile()) {
						UnitManager.Logging
								.logProfile("Logging Profile is now ON");
					}
				} else {
					sb.append("Logging Profile is now OFF");
					if (UnitManager.Logging.isProfile()) {
						UnitManager.Logging
								.logProfile("Logging Profile is now OFF");
						UnitManager.Logging.setProfile(newSetting);
					}
				}
			} else if ("core".equals(splitArgs[0])) {
				if (newSetting) {
					UnitManager.Logging.setCore(newSetting);
					sb.append("Logging Core is now ON");
					if (UnitManager.Logging.isCore()) {
						UnitManager.Logging
								.logCore("Logging Core is now ON");
					}
				} else {
					sb.append("Logging Core is now OFF");
					if (UnitManager.Logging.isCore()) {
						UnitManager.Logging
								.logCore("Logging Core is now OFF");
						UnitManager.Logging.setCore(newSetting);
					}
				}
			} else if ("stdout".equals(splitArgs[0])) {
				if (newSetting) {
					UnitManager.Logging.setStdOut(newSetting);
					sb.append("Logging to StdOut is now ON");
					if (UnitManager.Logging.isStdOut()) {
						UnitManager.Logging
							.logStdOut("Logging to StdOut is now ON");
					}
				} else {
					sb.append("Logging to StdOut is now OFF");
					if (UnitManager.Logging.isStdOut()) {
						UnitManager.Logging
							.logStdOut("Logging to StdOut is now OFF");
						UnitManager.Logging.setStdOut(newSetting);
					}
				}
			} else {
				sb.append(getCommandHelp());
			}

			// 'componentName' 'info/debug/stdout' 'on/off'
			// 'componentName' 'verbosity' 'level'
		} else if (splitArgs.length == 3) { 

			ComponentContainer cc = UnitManager.ObjectModel.getComponents();
			// first check list of handlers
			Handler[] handlers = cc.getHandlers();
			Handler handler = null;
			for (int i = 0; i < handlers.length; i++ ) {
				if (handlers[i].getName().equals(splitArgs[0])) {
					handler = handlers[i];
					break;
				}
			}
			if (handler != null) {
				UnitManager.Logging.logWarning("DiagService found handler: " 
						+ splitArgs[0]);
				
				sb.append(splitArgs[0]);
				
				if ("debug".equalsIgnoreCase(splitArgs[1])) {
					Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
					|| "true".equals(splitArgs[2]);
					
					sb.append(" debug ");
					comps.put(sb.toString(), newSetting.toString());
					sb.append(newSetting);
					handler.setDebug(newSetting);
				} else if ("info".equalsIgnoreCase(splitArgs[1])) {
					Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
					|| "true".equals(splitArgs[2]);
					
					sb.append(" info ");
					comps.put(sb.toString(), newSetting.toString());
					sb.append(newSetting);
					handler.setInfo(newSetting);					
				} else if ("stdout".equalsIgnoreCase(splitArgs[1])) {
					Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
					|| "true".equals(splitArgs[2]);
					
					sb.append(" stdout ");
					comps.put(sb.toString(), newSetting.toString());
					sb.append(newSetting);
					handler.setStdOut(newSetting);		
				} else if ("verbosity".equalsIgnoreCase(splitArgs[1])) {
					Integer newLevel;
					try {
						newLevel = new Integer(splitArgs[2]);
					} catch (NumberFormatException nfe) {
						newLevel = new Integer(1);
					}

					sb.append(" verbosity ");
					comps.put(sb.toString(), newLevel.toString());
					sb.append(newLevel.toString());
					handler.setVerbosity(newLevel);		
				}
			} else { // then check services
				Service[] services = cc.getServices();
				Service aservice = null;
				for (int i = 0; i < services.length; i++ ) {
					if (services[i].getName().equals(splitArgs[0])) {
						aservice = services[i];
						break;
					}
				}
				if (aservice != null) {
					UnitManager.Logging.logWarning("DiagService found service: " 
							+ splitArgs[0]);
					
					sb.append(splitArgs[0]);
					
					if ("debug".equalsIgnoreCase(splitArgs[1])) {
						Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
						|| "true".equals(splitArgs[2]);

						sb.append(" debug ");
						comps.put(sb.toString(), newSetting.toString());
						sb.append(newSetting);
						aservice.setDebug(newSetting);
					} else if ("info".equalsIgnoreCase(splitArgs[1])) {
						Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
						|| "true".equals(splitArgs[2]);

						sb.append(" info ");
						comps.put(sb.toString(), newSetting.toString());
						sb.append(newSetting);
						aservice.setInfo(newSetting);					
					} else if ("stdout".equalsIgnoreCase(splitArgs[1])) {
						Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
						|| "true".equals(splitArgs[2]);

						sb.append(" stdout ");
						comps.put(sb.toString(), newSetting.toString());
						sb.append(newSetting);
						aservice.setStdOut(newSetting);					
					} else if ("verbosity".equalsIgnoreCase(splitArgs[1])) {
						Integer newLevel;
						try {
							newLevel = new Integer(splitArgs[2]);
						} catch (NumberFormatException nfe) {
							newLevel = new Integer(1);
						}

						sb.append(" verbosity ");
						comps.put(sb.toString(), newLevel.toString());
						sb.append(newLevel.toString());
						aservice.setVerbosity(newLevel);		
					}
				}
				else { // lastly check adapters
					Adapter[] adapters = cc.getAdapters();
					Adapter adapter = null;
					for (int i = 0; i < adapters.length; i++ ) {
						if (adapters[i].getName().equals(splitArgs[0])) {
							adapter = adapters[i];
							break;
						}
					}
					if (adapter != null) {
						UnitManager.Logging.logWarning("DiagService found Adapter: " 
								+ splitArgs[0]);
						
						sb.append(splitArgs[0]);
						
						if ("debug".equalsIgnoreCase(splitArgs[1])) {
							Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
							|| "true".equals(splitArgs[2]);

							sb.append(" debug ");
							comps.put(sb.toString(), newSetting.toString());
							sb.append(newSetting);
							adapter.setDebug(newSetting);
						} else if ("info".equalsIgnoreCase(splitArgs[1])) {
							Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
							|| "true".equals(splitArgs[2]);

							sb.append(" info ");
							comps.put(sb.toString(), newSetting.toString());
							sb.append(newSetting);
							adapter.setInfo(newSetting);					
						} else if ("stdout".equalsIgnoreCase(splitArgs[1])) {
							Boolean newSetting = "on".equalsIgnoreCase(splitArgs[2])
							|| "true".equals(splitArgs[2]);

							sb.append(" stdout ");
							comps.put(sb.toString(), newSetting.toString());
							sb.append(newSetting);
							adapter.setStdOut(newSetting);					
						} else if ("verbosity".equalsIgnoreCase(splitArgs[1])) {
							Integer newLevel;
							try {
								newLevel = new Integer(splitArgs[2]);
							} catch (NumberFormatException nfe) {
								newLevel = new Integer(1);
							}

							sb.append(" verbosity ");
							comps.put(sb.toString(), newLevel.toString());
							sb.append(newLevel.toString());
							adapter.setVerbosity(newLevel);		
						}
					} else {
						sb.append("Component: " + splitArgs[0] + " not found");
					}
				}
			}
		} else {
			sb.append(getCommandHelp());
		}

		return sb.toString();
	}

	public String getCommandDescription() {
		return "Allows dynamic adjustments to Frameworks log output";
	}

	public String getCommandHelp() {
		return "log [show|[debug|info|core|profile|stdout] [on|off]]\n" +
			"log ['component'] [debug|info|core|profile|stdout] [on|off]\n";
	}

	public String[] getCommandNames() {
		return new String[] { "log" };
	}

}
