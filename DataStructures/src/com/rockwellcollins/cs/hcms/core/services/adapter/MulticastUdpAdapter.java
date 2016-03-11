package com.rockwellcollins.cs.hcms.core.services.adapter;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

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

public class MulticastUdpAdapter extends UdpAdapter {

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MULTICASTIP = "228.5.6.7";
	private static final int DEFAULT_MULTICASTPORT = 50060;
	private transient ArrayList<MulticastUdpAdapterListener> listeners;

	private transient MulticastSocket socket;
	private String multicastIp;

	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.MessagingService#onSetup(java.lang.Object, com.rockwellcollins.cs.hcms.core.ComponentSetupArgs)
	 */
	@Override
	protected void onSetup(final Object source, final ComponentSetupArgs args)
			throws ComponentSetupException {
		DEFAULT_PORT = DEFAULT_MULTICASTPORT;
		super.onSetup(source, args);
		this.multicastIp = getSetting("MulticastIP",DEFAULT_MULTICASTIP);

		this.listeners = new ArrayList<MulticastUdpAdapterListener>();

		
	}

	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.adapter.Adapter#onStarted(java.lang.Object, com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs)
	 */
	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);
		startProcessor();
	}
	/**
	 * Send.
	 * Creating a new multicastSocket and joining the group
	 * @throws ServiceStartException
	 */
	@Override
	protected void startProcessor() throws ServiceStartException {
		try {
			 socket = new MulticastSocket(getSocketPort());
			 InetAddress group = InetAddress.getByName(getMulticastIp());
			 socket.joinGroup(group);
				this.processor = new ThreadProcessor(
						"MulticastUdp Adapter Thread Processor",
						new ThreadProcessorListener() {
							public void threadProcessorAction(ThreadProcessor processor)
									throws InterruptedException {
								processAction(processor);
							}
						});
		
			try {
				  processor.start();
			} catch (final ThreadProcessorException e) {

				throw new ServiceStartException("MulticastUdp Adapter '" + toString()
						+ "' could not start Thread Processor", e);
			}

		} catch (final IOException e) {

			throw new ServiceStartException("Could not create Adapter '"
					+ toString() + "' Socket.  IO Exception.", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.adapter.Adapter#onStopped(java.lang.Object, com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs)
	 */
	@Override
	protected void onStopped(final Object source, final ServiceStopArgs args)
			throws ServiceStopException {

		super.onStopped(source, args);

		if (this.socket != null) {
			this.socket.disconnect();
			this.socket.close();
			this.socket = null;
		}

		try {
			processor.stop();
		} catch (final ThreadProcessorException e) {

			throw new ServiceStopException("MulticastUdp Adapter '" + toString()
					+ "' Queue Process could not stop.", e);
		}
	}

	/**
	 * Gets the multicast ip.
	 *
	 * @return the multicast ip
	 */
	public String getMulticastIp() {
		return multicastIp;
	}
	
	/**
	 * Process action.
	 * 
	 *
	 * @param processor the processor
	 * @throws InterruptedException the interrupted exception
	 */
	private void processAction(final ThreadProcessor processor)
			throws InterruptedException {

			final byte[] buffer = getBuffer();

			try {
				
		   	
			final DatagramPacket packet = new DatagramPacket(buffer,buffer.length,InetAddress.getByName(getMulticastIp()),getSocketPort());
                      socket.receive(packet);
				notifyListenerReceivedPacket(packet);

			} catch (final IOException e) {

			UnitManager.Logging.logSevere("Socket Exception in MulticastUdp Adapter '"
					+ toString() + "'.  Retrying.", e);
			}

		}

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	public void addListener(final MulticastUdpAdapterListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				//adding the listeners
				listeners.add(listener);
			}
		}
	}

	/**
	 * Notify listener received packet.
	 *
	 * @param packet the packet
	 */
	private void notifyListenerReceivedPacket(final DatagramPacket packet) {
		synchronized (listeners) {
			for (final MulticastUdpAdapterListener listener : listeners) {
		           	listener.udpAdapterReceivedPacket(this, packet);
			}
		}
	}
}