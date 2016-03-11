package com.rockwellcollins.cs.hcms.core.services;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;
import com.rockwellcollins.cs.hcms.core.services.messaging.MessageFilter;
import com.rockwellcollins.cs.hcms.core.services.messaging.filters.CoreVersionMessageFilter;
import com.rockwellcollins.cs.hcms.core.utils.StringHelper;

public abstract class MessagingService extends MulticastService {

	private static final long serialVersionUID = 1L;

	public static final String SETTING_MESSAGING_SERVICE_CHARACTER_SET = "messaging service character set";
	public static final String SETTING_MESSAGING_SERVICE_MULTICAST_GROUP = "messaging service multicast group";
	private static final String DEFAULT_MULTICAST_GROUP = "228.5.6.7";

	private static final int CORE_VERSION = 1;

	private InetAddress multicastGroup = null;

	transient private List<MessageFilter> messageFilters;

	public InetAddress getMulticastGroup() {
		try {
			if (multicastGroup == null) {

				UnitManager.Logging
						.logWarning("Messaging Service '"
								+ getName()
								+ "' does not have a multicast group specified.  Defaulting to "
								+ DEFAULT_MULTICAST_GROUP);

				multicastGroup = InetAddress.getByName(DEFAULT_MULTICAST_GROUP);
			}
		} catch (final Exception e) {
			UnitManager.Logging
					.logSevere(
							"Exception occured while defaulting multicast group to "
									+ DEFAULT_MULTICAST_GROUP
									+ " in SystemMessagingService '"
									+ toString() + "'", e);
		}
		return multicastGroup;
	}

	public void send(final Message message, final InetAddress ipAddress,
			final int port) throws ServiceIOException {

		try {
			filterSendMessage(message);

			final byte[] b = message.getJsonObject().toString().getBytes(
					Consts.CHARACTER_SET);

			send(b, ipAddress, port);

		} catch (final UnsupportedEncodingException e) {

			throw new ServiceIOException("Messaging Service '" + getName()
					+ "' could not Encode message '"
					+ message.getJsonObject().toString() + "' using Encoding '"
					+ Consts.CHARACTER_SET + "'", e);
		}
	}

	public void send(final Message message) throws ServiceIOException {
		send(message, getMulticastGroup(), getPort());
	}

	public void setMulticastGroup(final InetAddress ipAddress) {
		this.multicastGroup = ipAddress;
	}

	private void filterReceiveMessage(final Message message) {
		synchronized (getMessageFilters()) {
			for (final MessageFilter filter : getMessageFilters()) {
				filter.receivingMessage(message);
			}
		}
	}

	private void filterSendMessage(final Message message) {
		synchronized (getMessageFilters()) {
			for (final MessageFilter filter : getMessageFilters()) {
				filter.sendingMessage(message);
			}
		}
	}

	protected List<MessageFilter> getMessageFilters() {
		if (messageFilters == null) {
			messageFilters = new ArrayList<MessageFilter>();
		}
		return messageFilters;
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		try {
			this.multicastGroup = InetAddress.getByName(getSetting(
					SETTING_MESSAGING_SERVICE_MULTICAST_GROUP,
					DEFAULT_MULTICAST_GROUP));
		} catch (final UnknownHostException e) {
			UnitManager.Logging.logSevere(
					"Exception creating mulitcast Group for System Messaging Server '"
							+ toString() + "'", e);
		}
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		getMessageFilters().add(new CoreVersionMessageFilter(CORE_VERSION));
	}

	@Override
	protected void onReceivedDatagramPacket(final Object source,
			final MulticastServiceReceivedDatagramPacketArgs args) {

		super.onReceivedDatagramPacket(source, args);

		if (args.getDatagramPacket() == null) {

			UnitManager.Logging
					.logSevere("MulticastService received a (NULL) DatagramPacket.");
		} else {

			String jstr = "";

			try {
				final DatagramPacket packet = args.getDatagramPacket();

				jstr = new String(packet.getData(), packet.getOffset(), packet
						.getLength(), Consts.CHARACTER_SET);

				final Message message = new Message(new JSONObject(jstr));

				filterReceiveMessage(message);

				final MessagingServiceReceiveMessageArgs margs = new MessagingServiceReceiveMessageArgs(
						message, args.getDatagramPacket());

				onReceivedMessage(this, margs);

			} catch (final JSONException e) {

				UnitManager.Logging
						.logSevere("In Messaging Service '"
								+ toString()
								+ "' Incoming Data Could not be formed into a JSON Message.  Data '"
								+ jstr + "'");

			} catch (final UnsupportedEncodingException e) {

				UnitManager.Logging.logSevere("In Message Service '"
						+ getName()
						+ "' incoming data could not use character set '"
						+ Consts.CHARACTER_SET + "'", e);

			} catch (final Exception e) {

				StringBuilder sb = new StringBuilder();

				sb.append("Unknown exception occurred in MessagingService '"
						+ getName() + "' in onReceivedDatagramPacket.");

				InetAddress sAddr = args.getDatagramPacket().getAddress();
				int sPort = args.getDatagramPacket().getPort();
				byte[] sData = args.getDatagramPacket().getData();
				String sDataText;
				try {
					sDataText = new String(sData, Consts.CHARACTER_SET);
				} catch (final Exception e1) {
					sDataText = "Unknown";
				}

				sb.append("Source IP=[");
				sb.append(sAddr.getHostAddress());
				sb.append("], Source Port=[");
				sb.append(sPort);
				sb.append("], Data = [");
				sb.append(StringHelper.join(sData, ","));
				sb.append("], Data Text = [");
				sb.append(sDataText);
				sb.append("]");

				UnitManager.Logging.logSevere(sb.toString(), e);
			}
		}
	}

	abstract protected void onReceivedMessage(final Object source,
			final MessagingServiceReceiveMessageArgs args);

	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);

		try {
			joinMulticastGroup(getMulticastGroup());
		} catch (final MulticastServiceSocketException e) {
			UnitManager.Logging.logSevere(
					"Exception occured while joining multicast group in SystemMessageService '"
							+ toString() + "'", e);
		}
	}
}
