package com.rockwellcollins.cs.hcms.core.services.diagnostic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.ServiceRunArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceRunException;
import com.rockwellcollins.cs.hcms.core.services.ServiceState;
import com.rockwellcollins.cs.hcms.core.services.diagnostic.commands.Command;

public class DiagnosticService extends Service {

	transient private Vector<DiagnosticSession> sessions;

	private Map<String, Command> commands;

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_PORT = 1234;

	private int port = DEFAULT_PORT;
	
	private boolean logBindError = true;

	transient private ServerSocket serverSocket;

	public void addSession(final DiagnosticSession session) {
		synchronized (getSessions()) {
			getSessions().add(session);
		}
	}

	public void removeSession(final DiagnosticSession session) {
		synchronized (getSessions()) {
			getSessions().remove(session);
		}
	}

	private Vector<DiagnosticSession> getSessions() {
		if (sessions == null) {
			sessions = new Vector<DiagnosticSession>();
		}
		return sessions;
	}

	public Map<String, Command> getCommands() {
		if (commands == null) {
			commands = new HashMap<String, Command>();
		}
		return commands;
	}

	public String executeCommand(final DiagnosticSession session,
			final String command, final String args) {

		final Command cmd = getCommands().get(command);
		String result;

		if (cmd == null) {
			result = "Command '" + command + "' not found.";
		} else {
			synchronized (cmd) {
				result = cmd.executeCommand(session, command, args);
			}
		}
		return result;
	}

	private void loadCommandClasses(final String[] commandClasses) {
		if (commandClasses != null) {
			for (final String commandClass : commandClasses) {
				try {
					final Command command = (Command) UnitManager.Runtime
							.newInstance(commandClass);
					loadCommand(command);
				} catch (final Exception e) {
					UnitManager.Logging.logSevere(
							"Could not load Diagnostic Command Class '"
									+ commandClass + "'", e);
				}
			}
		}
	}

	private void loadCommand(final Command command) {

		for (final String name : command.getCommandNames()) {
			getCommands().put(name, command);
		}

		command.onCommandLoad(this);
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		setPort(getSetting("diagnostic service port", DEFAULT_PORT));

		loadCommandClasses(getSettingList("diagnostic service commands"));
	}

	@Override
	protected void onRun(final Object source, final ServiceRunArgs args)
			throws ServiceRunException {

		Socket socket;

		logBindError = true;
		
		while (getServiceState() == ServiceState.RUNNING) {

			try {
				socket = getServerSocket().accept();

				if (socket != null) {
					final DiagnosticSession session = new DiagnosticSession(
							this, socket);
					session.start();
					logBindError = true;
				}

			} catch (final IOException e) {
				if (logBindError) {
					UnitManager.Logging.logSevere(e);
					logBindError = false;
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException ie) {
						UnitManager.Logging.logSevere(ie);
					}
				}
			}
		}

	}

	protected ServerSocket getServerSocket() throws IOException {
		if (serverSocket == null) {
			serverSocket = new ServerSocket(getPort());
		}
		return serverSocket;
	}

	protected void setServerSocket(final ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	protected int getPort() {
		return port;
	}

	protected void setPort(final int port) {
		this.port = port;
	}
}
