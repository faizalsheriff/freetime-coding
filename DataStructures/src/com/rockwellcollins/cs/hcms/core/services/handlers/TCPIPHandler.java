package com.rockwellcollins.cs.hcms.core.services.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.CoreThreadException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;

public class TCPIPHandler extends Handler {

	public enum Status {
		Disconnected, Connecting, Connected,
	}

	public static final String SETTING_DST_PORT = "dst port";
	public static final String SETTING_DST_IP = "dst ip";
	public static final String SETTING_CONNECTION_RETRY = "connection retry";
	public static final String PROPERTY_SEND_BUFFER = "send buffer";
	public static final String PROPERTY_RECEIVE_BUFFER = "receive buffer";
	public static final String PROPERTY_CONNECTION_STATUS = "connection status";
	public static final long WORKER_TIMEOUT = 1000;
	public static final int CONNECTION_TIMEOUT = 1000;

	private static final long serialVersionUID = 1L;

	private transient Socket socket;
	private transient Thread worker;
	private boolean running;
	private transient BufferedReader fin;
	private transient BufferedWriter fout;
	private final Object lock = new Object();
	private transient final CountdownTimer timer = new CountdownTimer();
	private String dstIp;
	private int dstPort;
	private int connRetry;
	private Status connStatus;

	@Override
	protected void onPropertyChanged(final Object source,
			final PropertyChangedArgs args) {

		final String name = args.getPropertyName();
		final String value = args.getPropertyValue();

		if (PROPERTY_SEND_BUFFER.equals(name)) {
			write(value);
		}
	}

	@Override
	protected void onPropertyChangedTimeout(Object source,
			PropertyChangeTimeoutArgs args) {

		synchronized (lock) {
			if ((socket == null || !socket.isConnected() || socket.isClosed())
					&& timer.hasExpired()) {
				connect();
				timer.startTimer(connRetry);
			}
		}
	}

	@Override
	protected void onStarted(Object source, ServiceStartArgs args)
			throws ServiceStartException {
		super.onStarted(source, args);
		connect();
	}

	@Override
	protected void onStopped(Object source, ServiceStopArgs args)
			throws ServiceStopException {
		close();
		super.onStopped(source, args);
	}

	protected void setStatus(final Status status) {
		if (this.connStatus != status) {
			setProperty(PROPERTY_CONNECTION_STATUS, status.toString());
			this.connStatus = status;
		}
	}

	public Status getStatus() {
		return connStatus;
	}

	protected boolean connect() {
		boolean success = true;

		synchronized (lock) {

			if (dstIp == null) {
				success = false;
			}

			if (success) {
				success = connect(new InetSocketAddress(dstIp, dstPort));
			}
		}

		return success;
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		timer.startTimer(0);

		connRetry = getSetting(SETTING_CONNECTION_RETRY, 1000);
		dstIp = getSetting(SETTING_DST_IP, "127.0.0.1");
		dstPort = getSetting(SETTING_DST_PORT, 50011);
	}

	protected boolean connect(final SocketAddress address) {

		boolean success = false;

		synchronized (lock) {

			success = close();

			setStatus(Status.Connecting);

			if (success) {
				try {
					socket = new Socket();
					socket.connect(address, CONNECTION_TIMEOUT);
				} catch (ConnectException e) {
					success = false;
				} catch (IOException e) {
					UnitManager.Logging.logSevere(getName()
							+ " could not create Socket", e);
					success = false;
				}
			}

			if (success) {
				try {
					fin = new BufferedReader(new InputStreamReader(socket
							.getInputStream()));
				} catch (IOException e) {
					UnitManager.Logging.logSevere(getName()
							+ " could not create input stream", e);
					success = false;
				}
			}

			if (success) {
				try {
					fout = new BufferedWriter(new OutputStreamWriter(socket
							.getOutputStream()));
				} catch (IOException e) {
					UnitManager.Logging.logSevere(getName()
							+ " could not create output stream", e);
					success = false;
				}
			}

			if (success && !startWorker()) {
				close();
				success = false;
			}

			if (success) {
				setStatus(Status.Connected);
			}
		}

		return success;
	}

	protected boolean write(final String string) {
		boolean success = true;
		synchronized (lock) {
			if (fout != null) {
				try {
					fout.append(string);
					fout.flush();
				} catch (IOException e) {
					UnitManager.Logging.logSevere(getName()
							+ " could not send message", e);
					success = false;
				}
			}
		}
		return success;
	}

	protected boolean startWorker() {
		boolean success = true;
		try {
			worker = UnitManager.Threading.createThread(this, new Runnable() {
				public void run() {
					TCPIPHandler.this.run();
				}
			}, "worker thread");
		} catch (CoreThreadException e) {
			UnitManager.Logging.logSevere(getName()
					+ " errored starting working thread", e);
			success = false;
		}

		if (success) {
			running = true;
			worker.setName(getName() + " Session (worker thread).");
			worker.start();
		}

		return success;
	}

	protected void receivedMessage(final String message) {
		if (message != null && message.length() > 0) {
			setProperty(PROPERTY_RECEIVE_BUFFER, message);
		}
	}

	private final void run() {

		while (running && socket.isConnected() && !socket.isClosed()) {
			try {
				receivedMessage(fin.readLine());
			} catch (SocketException e) {
				running = false;
			} catch (IOException e) {
				UnitManager.Logging.logSevere(getName()
						+ " error reading socket", e);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean close() {

		boolean success = true;

		synchronized (lock) {

			if (socket != null && !socket.isClosed()) {
				running = false;
				try {
					socket.close();
				} catch (IOException e) {
					success = false;
				}
			}

			if (worker != null && worker.isAlive()) {
				try {
					worker.join(WORKER_TIMEOUT);
				} catch (InterruptedException e) {
					worker.interrupt();
				}
			}

			if (success && getStatus() == Status.Connected) {
				setStatus(Status.Disconnected);
			}
		}

		return success;
	}
}
