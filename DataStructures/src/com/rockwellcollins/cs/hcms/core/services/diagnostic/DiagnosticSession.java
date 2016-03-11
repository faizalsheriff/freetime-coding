package com.rockwellcollins.cs.hcms.core.services.diagnostic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.rockwellcollins.cs.hcms.core.UnitManager;

public class DiagnosticSession extends Thread {

	private static final String WELCOME = "Connected to Diagnostic Service.";

	private static final String GOODBYE = "Goodbye.";

	private DiagnosticService service;

	private Socket socket;

	private BufferedReader in;

	private PrintWriter out;

	private Map<String, String> properties;

	public DiagnosticSession(final DiagnosticService service,
			final Socket socket) throws IOException {

		setName("Diagnostic Session for Socket:"
				+ socket.getRemoteSocketAddress());

		this.service = service;
		this.socket = socket;

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		return properties;
	}

	public DiagnosticService getService() {
		return service;
	}

	private String executeCommandLine(final String line) {

		final int index = line.indexOf(" ");

		String result;
		String command;
		String args;

		if (index >= 0) {
			command = line.substring(0, index);
			args = line.substring(index + 1);
		} else {
			command = line;
			args = "";
		}

		result = getService().executeCommand(this, command, args);

		return result;
	}

	public void writePrompt() {
		print("> ");
	}

	public void println(final String str) {
		out.println(str);
		out.flush();
	}

	public void print(final String str) {
		out.print(str);
		out.flush();
	}

	@Override
	public void run() {

		String line;
		String result;
		String lastLine = null;

		getService().addSession(this);

		try {
			println(WELCOME);
			writePrompt();

			while (!(line = in.readLine()).equalsIgnoreCase("quit")) {

				try {

					if (lastLine != null && "".equals(line)) {
						line = lastLine;
						println("Repeating: " + line);
					}

					result = executeCommandLine(line);
					println(result);
				} catch (final Exception e) {
					println("Exception: " + e.getMessage());
				} finally {
					writePrompt();
				}

				lastLine = line;
			}
		} catch (final IOException e) {
			UnitManager.Logging.logSevere(e);
		} finally {

			try {
				println(GOODBYE);
			} catch (final Exception e) {
				UnitManager.Logging.logSevere(e);
			}

			try {
				in.close();
				out.close();
				socket.close();
			} catch (final IOException e) {
				UnitManager.Logging.logSevere(e);
			} finally {
				getService().removeSession(this);
			}

		}
	}
}
