package com.rockwellcollins.cs.hcms.core.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;

public class MulticastService extends Service {

	private static final long serialVersionUID = 1L;

	public static final String SETTING_MULTICAST_SERVICE_BIND_NETWORK_INTERFACE = "multicast service bind network interface";
	public static final String SETTING_MULTICAST_SERVICE_BIND_INTERFACE = "multicast service bind interface";
	public static final String SETTING_MULTICAST_SERVICE_RECEIVE_TIMEOUT = "multicast service receive timeout";
	public static final String SETTING_MULTICAST_SERVICE_PACKET_SIZE = "multicast service packet size";
	public static final String SETTING_MULTICAST_SERVICE_PORT = "multicast service port";

	private int port = 0;
	private int packetSize = 65507;
	private int receiveTimeout = 0;
	private boolean bindInterface = false;

	/** Set to bind for specific Network Interface */
	private boolean bindNetworkInterface = true;

	transient private MulticastSocket socket;
	transient private ArrayList<MulticastServiceListener> listeners;

	private InetAddress interfaceAddress;
	private transient NetworkInterface networkInterface;

	public final int getPacketSize() {
		return this.packetSize;
	}

	public final int getPort() {
		return this.port;
	}

	public final int getReceiveTimeout() {
		return receiveTimeout;
	}

	public final void joinMulticastGroup(final InetAddress group)
			throws MulticastServiceSocketException {

		if (socket == null) {
			throw new MulticastServiceSocketException("Multicast Service '"
					+ getName()
					+ "' Socket is null.  Could not join multicast group '"
					+ group.getHostName() + "'");
		}

		try {

			// CR RC-TC00013024: The IP Address is checked whether it is a
			// multicast IP Address so
			// that the multicast join is not performed on a broad cast address
			if (group.isMulticastAddress()) {
				socket.joinGroup(group);
			} else {
				if (isDebug()) {
					UnitManager.Logging.logDebug(group
							+ " is not a multicast address");
				}
			}

		} catch (final Exception e) {
			throw new MulticastServiceSocketException("Multicast Service '"
					+ getName()
					+ "' exception occured while joining multicast group '"
					+ group.getHostAddress() + "'", e);
		}
	}

	public final void leaveMulticastGroup(final InetAddress group)
			throws MulticastServiceSocketException {

		if (socket == null) {
			throw new MulticastServiceSocketException("Multicast Service '"
					+ getName()
					+ "' scoket is null.  Could not leave multicast group '"
					+ group);
		}

		try {
			socket.leaveGroup(group);
		} catch (IOException e) {
			throw new MulticastServiceSocketException("Multicast Service '"
					+ getName()
					+ "' exception occured while leaving multicast group '"
					+ group.getHostAddress() + "'", e);
		}
	}

	public final void send(final byte[] data, final InetAddress ipAddress,
			final int port) throws ServiceIOException {

		try {

			if (getServiceState() == ServiceState.RUNNING) {

				if (socket == null) {
					setServiceState(ServiceState.ERROR);
					throw new ServiceIOException(
							"Socket in System Messaging Server '"
									+ toString()
									+ "' is NULL.  Is the Service Running?  Setting State to Error.");
				}

				if (data.length > packetSize) {
					UnitManager.Logging.logSevere("Multicast Service '"
							+ toString() + "' data size being sent '"
							+ data.length
							+ "' is larger than the max packet size '"
							+ packetSize + "'.  Sending truncated data.");
				}

				final DatagramPacket packet = new DatagramPacket(data,
						data.length, ipAddress, port);

				socket.send(packet);
			}

		} catch (final Exception e) {
			throw new ServiceIOException("Multicast Service '" + getName()
					+ "' error sending DatagramPacket.", e);
		}
	}

	public final void setPacketSize(final int packetSize) {
		this.packetSize = packetSize;
	}

	public final void setPort(final int port) {
		this.port = port;
	}

	public final void setReceiveTimeout(final int receiveTimeout) {
		try {

			this.receiveTimeout = receiveTimeout;

			if (socket != null) {
				socket.setSoTimeout(receiveTimeout);
			}

		} catch (final Exception e) {
			UnitManager.Logging
					.logSevere(
							"Multicast Service '"
									+ toString()
									+ "' could not set Receive Timeout to "
									+ receiveTimeout
									+ " while"
									+ " running.  The timeout will be set the next time the service is started.",
							e);
		}
	}

	/**
	 * Create Socket for Multicast messages
	 * 
	 * @return - Socket object
	 * @throws - IOException
	 */
	protected MulticastSocket createSocket() throws IOException {

		MulticastSocket s;

		if (port == 0) {
			s = new MulticastSocket();
			port = s.getLocalPort();
		} else {
			s = new MulticastSocket(port);
		}

		if (interfaceAddress != null) {
			s.setInterface(interfaceAddress);
		} else if (bindInterface) {
			s.setInterface(UnitManager.ObjectModel.getUnit().getInetAddress());
		}

		if (networkInterface != null) {
			s.setNetworkInterface(networkInterface);
		} else if (bindNetworkInterface) {
			s.setNetworkInterface(UnitManager.Network
					.getNetworkInterface(UnitManager.ObjectModel.getUnit()
							.getInetAddress()));
		}

		return s;
	}

	@Override
	protected void onSetup(final Object source, final ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		port = getSetting(SETTING_MULTICAST_SERVICE_PORT, port);
		packetSize = getSetting(SETTING_MULTICAST_SERVICE_PACKET_SIZE,
				packetSize);
		receiveTimeout = getSetting(SETTING_MULTICAST_SERVICE_RECEIVE_TIMEOUT,
				receiveTimeout);
		bindInterface = getSetting(SETTING_MULTICAST_SERVICE_BIND_INTERFACE,
				bindInterface);

		// Removed to always set to the default value
		// bindNetworkInterface = getSetting(
		// SETTING_MULTICAST_SERVICE_BIND_NETWORK_INTERFACE,
		// bindNetworkInterface);
	}

	protected void onReceivedDatagramPacket(final Object source,
			final MulticastServiceReceivedDatagramPacketArgs args) {

		synchronized (listeners) {
			int len = listeners.size();
			for (int i = 0; i < len; i++) {
				listeners.get(i).multicastServiceReceivedDatagramPacket(this,
						args);
			}
		}
	}

	protected void onReceivedTimeout(final MulticastService multicastService,
			final MulticastServiceTimeoutArgs args) {
		synchronized (listeners) {
			int len = listeners.size();
			for (int i = 0; i < len; i++) {
				listeners.get(i).multicastServiceReceivedTimeout(this, args);
			}
		}
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		listeners = new ArrayList<MulticastServiceListener>();
	}

	@Override
	protected void onRun(final Object source, final ServiceRunArgs args)
			throws ServiceRunException {

		final byte[] buffer = new byte[packetSize];
		final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		while (getServiceState() == ServiceState.RUNNING) {

			try {

				if (socket == null) {
					setServiceState(ServiceState.ERROR);
					throw new ServiceRunException(
							"Socket in System Messaging Server '"
									+ toString()
									+ "' is NULL.  Is the Service Running?  Setting State to Error.");
				}

				socket.receive(packet);

				if (getServiceState() == ServiceState.RUNNING) {
					final MulticastServiceReceivedDatagramPacketArgs margs = new MulticastServiceReceivedDatagramPacketArgs(
							packet);
					onReceivedDatagramPacket(this, margs);
				}

			} catch (final SocketTimeoutException e) {

				final MulticastServiceTimeoutArgs targs = new MulticastServiceTimeoutArgs();
				onReceivedTimeout(this, targs);

			} catch (final SocketException e) {
				if (getServiceState() == ServiceState.RUNNING) {
					setServiceState(ServiceState.ERROR);
					throw new ServiceRunException(
							"Exception in Multicast Socket in Service '"
									+ toString()
									+ "'.  Setting ServiceState to Error");
				} else {
					if (UnitManager.Logging.isCore()) {
						UnitManager.Logging.logCore("Service '" + toString()
								+ "' Socket Debug: " + e);
					}
				}
			} catch (final IOException e) {
				throw new ServiceRunException(
						"Exception while receiving DatagramPacket in Service '"
								+ toString() + "'.  Haulting Service.", e);
			} catch (final Exception e) {
				UnitManager.Logging.logSevere(
						"Could not receive packet in Multicast Service '"
								+ toString() + '"', e);
			}
			packet.setLength(buffer.length);
		}
	}

	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		try {

			socket = createSocket();
			socket.setReceiveBufferSize(packetSize);
			socket.setSendBufferSize(packetSize);

			if (socket == null) {
				throw new ServiceStartException(
						"Could not create socket for Service '" + toString()
								+ "'.  Socket is (NULL)");
			}

			if (receiveTimeout != 0) {
				socket.setSoTimeout(receiveTimeout);
			}

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Multicast Service '" + toString()
						+ "' Opened Socket '" + socket.getNetworkInterface()
						+ ":" + socket.getLocalPort());
			}
		} catch (final Exception e) {
			throw new ServiceStartException("Multicast Service '" + getName()
					+ "' could not create socket." + toString() + "'", e);
		}

		super.onStarted(source, args);
	}

	@Override
	protected final void onStopping(final Object source,
			final ServiceStopArgs args) throws ServiceStopException {

		super.onStopping(source, args);

		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (final Exception e) {
			throw new ServiceStopException("Multicast Serivce '" + getName()
					+ "' exception closing socket.", e);
		}
	}

	@Override
	protected final void onStarting(final Object source,
			final ServiceStartArgs args) throws ServiceStartException {
		super.onStarting(source, args);
	}

	public final InetAddress getInterfaceAddress() {
		return interfaceAddress;
	}

	public final void setInterfaceAddress(final InetAddress interfaceAddress) {
		this.interfaceAddress = interfaceAddress;
	}

	public final void setNetworkInterface(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}

	public final NetworkInterface getNetworkInterface() {
		return networkInterface;
	}

	public final boolean register(final MulticastServiceListener listener) {
		synchronized (listeners) {
			return listeners.add(listener);
		}
	}

	public final boolean unregister(final MulticastServiceListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}
}
