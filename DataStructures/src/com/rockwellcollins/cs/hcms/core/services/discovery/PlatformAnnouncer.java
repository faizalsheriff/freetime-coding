package com.rockwellcollins.cs.hcms.core.services.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramPacket;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.MulticastService;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceListener;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceReceivedDatagramPacketArgs;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceTimeoutArgs;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceSocketException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;

import com.rockwellcollins.cs.hcms.core.services.discovery.PlatformAnnouncementMessage;

/**
 * This class is responsible for providing the Platform Announcement Service
 * that sends out the announcement message. It should run any any Venue LRU
 * except any of the HTSE, which will be listening for the message.
 * 
 * @author gdneshei
 * 
 */
public class PlatformAnnouncer extends MulticastService implements
		MulticastServiceListener {
	private static final long serialVersionUID = 1L;

	public static final String SETTING_PLATFORM_ANNOUNCER_MULTICAST_GROUP = "platform announcer service multicast group";

	private InetAddress group;
	private transient PlatformAnnouncementMessage announcement;
	private long unique_wait = 0;
	private long lastTimeSent = 0;
	private long lastTimeReceived = 0;
	private long msgTimeout = 10000; /* default to 10 seconds */

	/**
	 * Read settings and set member variables
	 */
	@Override
	protected void onSetup(final Object source, final ComponentSetupArgs args)
			throws ComponentSetupException {

		long instance = 0;
		String ipAddress = "";
		String name = "PlatformAnnouncer";

		super.onSetup(source, args);

		ipAddress = getSetting(SETTING_PLATFORM_ANNOUNCER_MULTICAST_GROUP,
				ipAddress);
		msgTimeout = getReceiveTimeout();

		try {
			group = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			UnitManager.Logging.logSevere("Service " + getName()
					+ " unable to get multicast group address.", e);
		}

		name = UnitManager.ObjectModel.getUnit().getInstanceName();
		instance = UnitManager.ObjectModel.getUnit().getUnitNumber();
		unique_wait = instance * 10;

		/* create an announcement message */
		announcement = new PlatformAnnouncementMessage();
		announcement.setSourceName(name);
		announcement.update();

		if (isInfo()) {
			logInfo("Service " + getName() + " instance set to "
					+ Long.toString(instance) + ".");
			logInfo("Service " + getName() + " name set to " + name + ".");
		}

	}

	/**
	 * The service has started so register to listen for events. Join the
	 * multicast group to listen for announcements from other LRUs.
	 * 
	 */
	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);

		/*
		 * Delay for a very small random amount of time so as not to be in
		 * perfect sync with other LRUs that are sending the same message. Lower
		 * instance, less delay.
		 */
		try {
			Thread.sleep(unique_wait);
			lastTimeReceived = UnitManager.Timing.getTimeAlive();
			lastTimeSent = lastTimeReceived;
		} catch (InterruptedException e) {
			UnitManager.Logging.logSevere("Service " + getName()
					+ " started not properly delayed.", e);
		}

		/*
		 * Join the platform announcer multicast group to listen for platform
		 * announcement messages. The timeout on the listen will be used to
		 * trigger when to send the next message.
		 */
		try {
			joinMulticastGroup(group);
		} catch (MulticastServiceSocketException e) {
			UnitManager.Logging.logSevere("Service " + getName()
					+ " unable to join multicast group.", e);
		}

		/* Register to listen for socket events. */
		register(this);

	}

	/**
	 * Received a multicast platform announcement packet.
	 */
	public void multicastServiceReceivedDatagramPacket(
			final MulticastService multicastService,
			final MulticastServiceReceivedDatagramPacketArgs args) {

		/*
		 * If received a platform announcement message before timing out, do
		 * nothing since another LRU already sent the message. Delay for a very
		 * small random amount of time so as not to be in perfect sync with
		 * other LRUs that are sending the same message. Otherwise, if received
		 * another type of message, send a platform announcement message if time
		 * since last message received > timeout.
		 */
		long timeReceived = UnitManager.Timing.getTimeAlive();

		try {
			DatagramPacket packet = args.getDatagramPacket();
			byte data[] = packet.getData();

			PlatformAnnouncementMessage msg = new PlatformAnnouncementMessage();
			msg.setData(data);

			/*
			 * got an anouncement message from somewhere else so sleep and just
			 * keep going
			 */
			if (msg.getMessageType() == PlatformAnnouncementMessage.MESSAGE_TYPE_PLATFORM_ID) {
				lastTimeReceived = timeReceived;
				Thread.sleep(unique_wait);
			}
			/*
			 * that message was not an announcement message and the last time
			 * one was received was longer than the timeout send one now
			 */
			else {
				if (timeReceived - lastTimeReceived >= msgTimeout) {
					sendAnnouncement();
					lastTimeReceived = timeReceived;
				}
			}
		} catch (InterruptedException e) {
			UnitManager.Logging.logSevere("Service " + getName()
					+ " packet received not properly delayed.", e);
		}

	}

	/**
	 * The service timed out waiting for a platform announcement packet so send
	 * one.
	 */
	public void multicastServiceReceivedTimeout(
			final MulticastService multicastService,
			final MulticastServiceTimeoutArgs args) {

		/*
		 * No message was received by another LRU, so this LRU timed out. Send a
		 * platform announcement message since no other LRU sent it yet.
		 */
		sendAnnouncement();
	}

	/**
	 * Send the announcement only if the timeout period has elapsed.
	 * 
	 */
	private void sendAnnouncement() {
		try {
			long timeSent = UnitManager.Timing.getTimeAlive();

			/* send message and save time stamp when sent */
			if (timeSent - lastTimeSent >= msgTimeout) {
				send(announcement.toByteArray(), group, getPort());
				lastTimeSent = timeSent;
			}
		} catch (ServiceIOException e) {
			UnitManager.Logging.logSevere("Service " + getName()
					+ " unable to send multicast message.", e);
		}
	}
}
