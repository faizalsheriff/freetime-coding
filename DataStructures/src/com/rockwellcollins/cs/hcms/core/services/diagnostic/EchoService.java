package com.rockwellcollins.cs.hcms.core.services.diagnostic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.ComponentVerifyArgs;
import com.rockwellcollins.cs.hcms.core.ComponentVerifyException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.MulticastService;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceReceivedDatagramPacketArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerListener;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerMode;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.ControlMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StatusMessage;

public class EchoService extends MulticastService {

	public static final byte CMD_UNKNOWN = 0x00;
	public static final byte CMD_REGISTER = 0x01;
	public static final byte CMD_UNREGISTER = 0x02;
	public static final byte CMD_MESSAGE_PROCESS = 0x03;
	public static final byte CMD_MESSAGE_SEND = 0x04;

	public static final short VERSION = 1;

	private static final long serialVersionUID = 1L;

	transient public ArrayList<EchoSession> sessions;
	transient private StateManager sm;
	transient private StateManagerListener sml;

	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		/* sessions registered for message echo */
		sessions = new ArrayList<EchoSession>();

		sml = new StateManagerListener() {

			public void stateManagerModeChanged(final StateManager sm,
					final StateManagerMode mode) {
			}

			public void stateManagerProcessControlMessage(
					final StateManager sm, final ControlMessage message) {
				dispatchMessages(CMD_MESSAGE_PROCESS, message);
			}

			public void stateManagerProcessMessage(final StateManager sm,
					final StateManagerMessage message) {
				dispatchMessages(CMD_MESSAGE_PROCESS, message);
			}

			public void stateManagerProcessMessageComplete(final StateManager sm,
					final StateManagerMessage message) {
				
			}

			public final void stateManagerProcessStatusMessage(
					final StateManager sm, final StatusMessage message) {
				dispatchMessages(CMD_MESSAGE_PROCESS, message);
			}

			public final void stateManagerProcessStatusMessageComplete(
					final StateManager sm, final StatusMessage message) {
				
			}

			public final void stateManagerSendMessage(final StateManager sm,
					final StateManagerMessage message) {
				dispatchMessages(CMD_MESSAGE_SEND, message);
			}

			public final void stateManagerStatusChanged(final StateManager sm,
					final StateManagerStatus status) {
			}
		};
	}

	private final void dispatchMessages(final byte cmd,
			final StateManagerMessage message) {
		synchronized (sessions) {
			Iterator<EchoSession> itr = sessions.iterator();
			EchoSession s;
			while (itr.hasNext()) {
				s = itr.next();
				if (s.expire < UnitManager.Timing.getTimeAlive()) {
					itr.remove();
				} else {
					try {
						ByteArrayOutputStream bout = new ByteArrayOutputStream(
								255);
						DataOutputStream out = new DataOutputStream(bout);
						s.id = s.id + 1;
						out.writeShort(VERSION);
						out.writeByte(cmd);
						out.writeLong(s.id);
						out.write(message.toBytes());
						send(bout.toByteArray(), s.ip, s.port);
					} catch (final Exception e) {
						UnitManager.Logging.logSevere("Echo Service '"
								+ getName() + "' could not echo message.", e);
					}
				}
			}
		}
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		/* cache state Manager */
		sm = UnitManager.ObjectModel.getStateManager();
	}

	@Override
	protected void onVerify(Object source, ComponentVerifyArgs args)
			throws ComponentVerifyException {

		super.onVerify(source, args);

		/* verify state manager is not null */
		if (sm == null) {
			throw new ComponentVerifyException("Echo Service '" + getName()
					+ "' State Manager is null");
		}
	}

	@Override
	protected void onStarted(Object source, ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);

		/* hook state manager for messages */
		sm.register(sml);
	}

	@Override
	protected void onStopped(Object source, ServiceStopArgs args)
			throws ServiceStopException {

		super.onStopped(source, args);

		/* unhook listener */
		sm.unregister(sml);
	}

	@Override
	protected final void onReceivedDatagramPacket(final Object source,
			final MulticastServiceReceivedDatagramPacketArgs args) {

		DatagramPacket packet = args.getDatagramPacket();

		final ByteArrayInputStream bin = new ByteArrayInputStream(packet
				.getData());
		final DataInputStream in = new DataInputStream(bin);

		try {

			byte cmd = in.readByte();
			int port;
			InetAddress ip = packet.getAddress();
			long timeout = 0L;
			boolean found = false;
			EchoSession s;

			synchronized (sessions) {

				int len = sessions.size();

				switch (cmd) {
				case 0x01: // Register
					port = in.readInt();
					timeout = in.readLong();
					for (int i = 0; i < len; i++) {
						s = sessions.get(i);
						if (s.ip.equals(ip) && s.port == port) {
							s.expire = UnitManager.Timing.getTimeAlive() + timeout;
							found = true;
							break;
						}
					}
					if (!found) {
						sessions.add(new EchoSession(ip, port, UnitManager.Timing.getTimeAlive()
								+ timeout));
					}
					break;
				case 0x02: // Unregister
					port = in.readInt();
					synchronized (sessions) {
						Iterator<EchoSession> itr = sessions.iterator();
						while (itr.hasNext()) {
							s = itr.next();
							if (s.ip == ip && s.port == port) {
								itr.remove();
							}
						}
					}
					break;
				default:
					if (UnitManager.Logging.isCore()) {
						UnitManager.Logging.logCore("Echo Service '"
								+ getName() + "' received invalid command '"
								+ cmd + "'");
					}
				}
			}

		} catch (final IOException e) {
			UnitManager.Logging.logSevere("Echo Service '" + getName()
					+ "' could not read Datagram Packet", e);
		}
	}
}
