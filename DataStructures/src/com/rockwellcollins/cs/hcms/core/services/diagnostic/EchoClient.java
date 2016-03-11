package com.rockwellcollins.cs.hcms.core.services.diagnostic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.MulticastService;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceReceivedDatagramPacketArgs;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceTimeoutArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class EchoClient extends MulticastService {

	private static final long serialVersionUID = 1L;

	private static final String SETTING_ECHO_CLIENT_RENEW = "echo client renew";
	public static final String SETTING_ECHO_CLIENT_TIMEOUT = "echo client timeout";

	transient private ArrayList<EchoClientListener> listeners;
	transient private InetAddress ip;
	transient private int port;
	transient private CountdownTimer renewTimer;
	transient private boolean connected;
	transient private Object lock;

	private int timeout = 10000;
	private int renew = 4000;

	@Override
	protected void onSetup(final Object source, final ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		timeout = getSetting(SETTING_ECHO_CLIENT_TIMEOUT, timeout);
		renew = getSetting(SETTING_ECHO_CLIENT_RENEW, renew);

		if (getReceiveTimeout() == 0) {
			setReceiveTimeout(renew);
		}
	}

	public final void connect(final InetAddress ip, final int port) {
		this.ip = ip;
		this.port = port;
		if (connected) {
			disconnect();
		}
		renew();
	}

	public final void renew() {

		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		final DataOutputStream out = new DataOutputStream(bout);

		try {
			synchronized (lock) {
				out.writeByte(EchoService.CMD_REGISTER);
				out.writeInt(getPort());
				out.writeLong(timeout);
				send(bout.toByteArray(), ip, port);
				connected = true;
			}
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Echo Client '" + getName()
					+ "' error sending connection.", e);
		}
	}

	public final void disconnect() {
		if (ip != null && port > 0) {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final DataOutputStream out = new DataOutputStream(bout);
			try {
				synchronized (lock) {
					out.writeByte(EchoService.CMD_UNREGISTER);
					out.writeInt(getPort());
					out.writeLong(timeout);
					send(bout.toByteArray(), ip, port);
					connected = false;
				}
			} catch (final Exception e) {
				UnitManager.Logging.logSevere("Echo Client '" + getName()
						+ "' error sending connection.", e);
			}
		}
	}

	public final boolean isConnected() {
		return connected;
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		listeners = new ArrayList<EchoClientListener>();
		renewTimer = new CountdownTimer();
		lock = new Object();
	}

	@Override
	protected void onStarted(Object source, ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);

		renewTimer.startTimer(renew);
	}

	@Override
	protected void onReceivedTimeout(final MulticastService multicastService,
			final MulticastServiceTimeoutArgs targs) {

		super.onReceivedTimeout(multicastService, targs);

		processTimers();
	}

	private final void processTimers() {
		if (renewTimer.hasExpired() && ip != null && port > 0 && connected) {
			renew();
			renewTimer.startTimer(renew);
		}
	}

	@Override
	protected void onReceivedDatagramPacket(final Object source,
			final MulticastServiceReceivedDatagramPacketArgs args) {

		super.onReceivedDatagramPacket(source, args);

		DatagramPacket packet = args.getDatagramPacket();

		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(packet
					.getData());
			DataInputStream in = new DataInputStream(bin);
			short version = in.readShort();

			if (version == EchoService.VERSION) {
				byte cmd = in.readByte();
				long id = in.readLong();
				StateManagerMessage message = StateManagerMessage.create(bin);

				switch (cmd) {
				case EchoService.CMD_MESSAGE_PROCESS:
					synchronized (listeners) {
						int len = listeners.size();
						for (int i = 0; i < len; i++) {
							listeners.get(i).echoClientProcessMessage(id,
									message);
						}
					}
					break;

				case EchoService.CMD_MESSAGE_SEND:
					synchronized (listeners) {
						int len = listeners.size();
						for (int i = 0; i < len; i++) {
							listeners.get(i).echoClientSendMessage(id, message);
						}
					}
					break;
				}
			}
		} catch (final IOException e) {
			UnitManager.Logging.logSevere(
					"Echo Client could not assemble echo message", e);
		}

		processTimers();
	}

	public final boolean register(final EchoClientListener listener) {
		synchronized (listeners) {
			return listeners.add(listener);
		}
	}

	public final boolean unregister(final EchoClientListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setRenew(int renew) {
		this.renew = renew;
	}

	public int getRenew() {
		return renew;
	}

}
