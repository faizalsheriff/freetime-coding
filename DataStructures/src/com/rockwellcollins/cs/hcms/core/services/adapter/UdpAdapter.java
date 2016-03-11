package com.rockwellcollins.cs.hcms.core.services.adapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessor;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessorException;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessorListener;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;

public class UdpAdapter extends Adapter {

	private static final long serialVersionUID = 1L;

	public int DEFAULT_PORT = 50000;

	public static final int DEFAULT_BUFFER_SIZE = 1024;

	public transient ThreadProcessor processor;

	public transient byte[] buffer;

	private transient ArrayList<UdpAdapterListener> listeners;

	private transient DatagramSocket socket;

	public int socketPort;

	public int socketBufferSize;

	@Override
	protected void onSetup(final Object source, final ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		this.socketPort = getSetting("adapter socket receiving port",
				DEFAULT_PORT);
		this.socketBufferSize = getSetting(
				"adapter socket receiving buffer size", DEFAULT_BUFFER_SIZE);
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		this.listeners = new ArrayList<UdpAdapterListener>();

		this.processor = new ThreadProcessor("Udp Adapter Thread Processor",
				new ThreadProcessorListener() {
					public void threadProcessorAction(ThreadProcessor processor)
							throws InterruptedException {
						processAction(processor);
					}
				});
	}

	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);
	startProcessor();
	}
	protected void startProcessor() throws ServiceStartException {
		try {

			socket = new DatagramSocket(getSocketPort());

			try {

				processor.start();

			} catch (final ThreadProcessorException e) {

				throw new ServiceStartException("Udp Adapter '" + toString()
						+ "' could not start Thread Processor", e);
			}

		} catch (final IOException e) {

			throw new ServiceStartException("Could not create Adapter '"
					+ toString() + "' Socket.  IO Exception.", e);
		}
	}

	@Override
	protected void onStopped(final Object source, final ServiceStopArgs args)
			throws ServiceStopException {

		super.onStopped(source, args);

		DatagramSocket socket = getAdapterSocket();

		if (socket != null) {
			socket.disconnect();
			socket.close();
			socket = null;
		}

		try {
			processor.stop();
		} catch (final ThreadProcessorException e) {

			throw new ServiceStopException("Udp Adapter '" + toString()
					+ "' Queue Process could not stop.", e);
		}
	}

	public boolean send(final DatagramPacket packet) {
		final DatagramSocket socket = getAdapterSocket();

		if (socket == null) {
			return false;
		}

		try {
			socket.send(packet);
		} catch (final IOException e) {
			UnitManager.Logging.logSevere("In Adapter '" + toString()
					+ "' Could not send Datagram Packet", e);
			return false;
		}

		return true;
	}

	public boolean send(final InetAddress address, final int port,
			final byte[] data, final int offset, final int length) {

		final DatagramPacket packet = new DatagramPacket(data, offset, length);

		packet.setAddress(address);
		packet.setPort(port);

		return send(packet);
	}

	public boolean send(final String address, final int port,
			final byte[] data, final int offset, final int length) {

		final DatagramPacket packet = new DatagramPacket(data, offset, length);

		try {

			packet.setAddress(InetAddress.getByName(address));
			packet.setPort(port);

		} catch (final UnknownHostException e) {

			UnitManager.Logging.logSevere("In Adapter '" + toString()
					+ "' Could not find IP Address '" + address + "'.", e);
			return false;

		}

		return send(packet);
	}

	public boolean send(final InetAddress address, final int port,
			final byte[] data) {
		return send(address, port, data, 0, data.length);
	}

	public boolean send(final String address, final int port, final byte[] data) {
		return send(address, port, data, 0, data.length);
	}

	public int getSocketPort() {
		return socketPort;
	}

	private DatagramSocket getAdapterSocket() {
		return socket;
	}

	public byte[] getBuffer() {
		if (buffer == null) {
			buffer = new byte[getSocketBufferSize()];
		}
		return buffer;
	}

	private void processAction(final ThreadProcessor processor)
			throws InterruptedException {

		final DatagramSocket socket = getAdapterSocket();

		if (socket != null) {

			final byte[] buffer = getBuffer();

			final DatagramPacket packet = new DatagramPacket(buffer,
					buffer.length);

			try {

				socket.receive(packet);
				notifyListenerReceivedPacket(packet);

			} catch (final IOException e) {

				UnitManager.Logging.logSevere(
						"Socket Exception in Udp Adapter '" + toString()
								+ "'.  Retrying.", e);
			}

		} else {

			Thread.sleep(10);
		}
	}

	public int getSocketBufferSize() {
		return socketBufferSize;
	}

	public void addListener(final UdpAdapterListener listener) {

		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	private void notifyListenerReceivedPacket(final DatagramPacket packet) {

		synchronized (listeners) {
			for (final UdpAdapterListener listener : listeners) {
				listener.udpAdapterReceivedPacket(this, packet);
			}
		}
	}
}