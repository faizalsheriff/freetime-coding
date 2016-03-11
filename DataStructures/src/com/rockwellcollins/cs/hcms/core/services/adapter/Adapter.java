package com.rockwellcollins.cs.hcms.core.services.adapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessor;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessorException;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessorListener;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.services.MessagingService;
import com.rockwellcollins.cs.hcms.core.services.MessagingServiceReceiveMessageArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.AdapterMessage;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.AdapterMessageType;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.GetAdaptersAckMessage;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.GetAdaptersMessage;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.MethodCallAckMessage;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.MethodCallDirection;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.MethodCallMessage;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.Parameter;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.SetModeAckMessage;
import com.rockwellcollins.cs.hcms.core.services.adapter.messages.SetModeMessage;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;

/**
 * An Adapter is component bride from a handler to the device driver. An adapter
 * can be simulated by an external tool called the Test and Simulation Tool
 * (otherwise known as the TST).
 * 
 * @author tmcrane
 * 
 */
public abstract class Adapter extends MessagingService {

	private static final long serialVersionUID = 1L;

	private static long messageId = 0;

	private static final int TST_TIMEOUT_DEFAULT = 5000;

	private static final int DEFAULT_ADAPTER_PORT = 60010;

	// private static final int adapterPortStart = 50000;
	// private static int adapterPortCounter = 0;

	transient private Handler handler;

	transient protected InetSocketAddress tstSocketAddress = null;

	transient private AdapterMode mode = AdapterMode.NORMAL;

	transient private HashMap<Long, MethodCallMessage> blockingMessages;

	transient private ThreadProcessor tstReceiverThread;

	// private int adapterPort;
	private CountdownTimer timer;

	private int TST_TIMEOUT = TST_TIMEOUT_DEFAULT;

	private static final String CHARSET = Consts.CHARACTER_SET;

	protected boolean isSendToHandler() {
		return true;
	}

	protected boolean isSendToDevice() {
		return true;
	}

	protected boolean isTSTConnected() {
		return mode != AdapterMode.NORMAL;
	}

	protected void sendHTODCall(final String function, final Object[] params) {

	}

	protected void sendDTOHCall(final String function, final Object[] params) {

	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(final Handler handler) {
		this.handler = handler;
	}

	private void setMode(final AdapterMode mode, final InetAddress ipAddress,
			final int port) {
		if (this.mode.equals(mode)) {
			return;
		}
		final AdapterMode oldMode = getMode();
		this.mode = mode;
		// If changing the mode to NORMAL or TEST from a non NORMAL or TEST
		// mode, then free up all of the blocked threads and remove them
		// from the collection
		if ((getMode().equals(AdapterMode.NORMAL) || getMode().equals(
				AdapterMode.TEST))
				&& !oldMode.equals(getMode())) {
			if (getMode().equals(AdapterMode.NORMAL)) {
				setTstSocketAddress(null);
			} else {
				setTstSocketAddress(new InetSocketAddress(ipAddress, port));
			}
			synchronized (this.blockingMessages) {
				for (final MethodCallMessage msg : this.blockingMessages
						.values()) {
					synchronized (msg) {
						msg.notifyAll();
					}
				}
			}
		} else if (getMode().equals(AdapterMode.DEVICE_ONLY_SIMULATION)
				&& oldMode.equals(AdapterMode.SIMULATION)) {
			synchronized (this.blockingMessages) {
				for (final MethodCallMessage msg : this.blockingMessages
						.values()) {
					// Notify all handler to device messages that are
					// blocking because of SIMULATION mode
					if (msg.getDirection().equals(
							MethodCallDirection.HANDLER_TO_DEVICE)) {
						synchronized (msg) {
							msg.notify();
						}
					}
				}
			}
		} else {
			setTstSocketAddress(new InetSocketAddress(ipAddress, port));
		}
	}

	protected AdapterMode getMode() {
		return this.mode;
	}

	protected boolean isWaitForTST(final MethodCallDirection dir) {
		switch (dir) {
		case DEVICE_TO_HANDLER:
			return isHandlerSimulation();
		case HANDLER_TO_DEVICE:
			return isDeviceSimulation();
		}
		return false;
	}

	private boolean isDeviceSimulation() {
		return this.mode.equals(AdapterMode.SIMULATION)
				|| this.mode.equals(AdapterMode.DEVICE_ONLY_SIMULATION);
	}

	private boolean isHandlerSimulation() {
		return this.mode.equals(AdapterMode.SIMULATION);
	}

	// protected boolean isSimulation() {
	// return this.mode.equals(AdapterMode.SIMULATION);
	// }
	//
	// protected boolean isDeviceOnlySimulation() {
	// return this.mode.equals(AdapterMode.DEVICE_ONLY_SIMULATION);
	// }

	protected InetSocketAddress getTstSocketAddress() {
		return this.tstSocketAddress;
	}

	private void setTstSocketAddress(final InetSocketAddress socketAddress) {
		this.tstSocketAddress = socketAddress;
	}

	protected int getTstTimeout() {
		return this.TST_TIMEOUT;
	}

	private boolean proceedWithMessageProcessing(final Message message)
			throws UnknownHostException {
		// Always process GET_ADAPTERS messages
		if (AdapterMessage.getType(message).equals(
				AdapterMessageType.GET_ADAPTERS)) {
			return true;
		}
		// Process the message if the adapter name and lru ip match the values
		// inside the message
		if (AdapterMessage.getAdapterIp(message).equals(
				UnitManager.ObjectModel.getUnit().getIpAddress())
				&& AdapterMessage.getAdapterName(message).equals(getName())) {
			// If a TST is connected, only respond to messages from the
			// connected TST's ip address
			return !isTSTConnected()
					|| isTSTConnected()
					&& UnitManager.ObjectModel.getUnit().getIpAddress().equals(
							tstSocketAddress.getAddress().toString().replace(
									"/", ""));
		}

		// Anything else, return false
		return false;
	}

	private synchronized void processPacket(final DatagramPacket packet) {
		String jsonStr = null;
		try {
			jsonStr = new String(packet.getData(), packet.getOffset(), packet
					.getLength(), "UTF-16LE");

			final Message message = new Message(new JSONObject(jsonStr));

			if (!proceedWithMessageProcessing(message)) {
				return;
			} else {
				timer.restart();
			}

			if (isDebug()) {
				logDebug("Adapter '" + getName()
						+ "' processing message type '"
						+ AdapterMessage.getType(message) + "'");
				UnitManager.Logging.logDebug("Message: " + jsonStr);
			}

			switch (AdapterMessage.getType(message)) {
			case GET_ADAPTERS:
				processGetAdapters(new GetAdaptersMessage(message
						.getJsonObject()), packet);
				break;
			case GET_ADAPTERS_ACK:
				// Should never receive this message from TST
				break;
			case METHOD_CALL:
				if (!isTSTConnected()) {
					return;
				}
				processMethodCall(new MethodCallMessage(message.getJsonObject()));
				break;
			case METHOD_CALL_ACK:
				if (!isTSTConnected()) {
					return;
				}
				processMethodCallAck(new MethodCallAckMessage(message
						.getJsonObject()));
				break;
			case SET_MODE:
				processSetMode(new SetModeMessage(message.getJsonObject()),
						packet);
				break;
			case SET_MODE_ACK:
				break;
			default:
				UnitManager.Logging.logWarning("Unknown message type '"
						+ AdapterMessage.getType(message)
						+ "' received by adapter '" + getName() + "' ");
				break;
			}

		} catch (final JSONException e) {

			UnitManager.Logging
					.logSevere("In Messaging Service '"
							+ toString()
							+ "' Incoming Data Could not be formed into a JSON Message.  Data '"
							+ jsonStr + "'");
		} catch (final UnsupportedEncodingException e) {

			UnitManager.Logging.logSevere("In Message Service '" + getName()
					+ "' incoming data could not use character set '" + CHARSET
					+ "'", e);
		} catch (final Exception e) {

			UnitManager.Logging.logSevere(e);
		}
	}

	private long getNextId() {
		return messageId + 1;
	}

	private long getMessageId() {
		return ++messageId;
	}

	private void processGetAdapters(final GetAdaptersMessage message,
			final DatagramPacket packet) throws UnknownHostException {
		final GetAdaptersAckMessage messageAck = new GetAdaptersAckMessage();
		messageAck.setAdapterName(getName());
		messageAck.setId(message.getId());
		messageAck.setAdapterIp(UnitManager.ObjectModel.getUnit()
				.getIpAddress());
		try {
			send(messageAck, packet.getAddress(), message.getPort());
		} catch (final UnsupportedEncodingException e) {
			UnitManager.Logging
					.logSevere(
							"Unsupported encoding exception occurred when attempting to send GetAdaptersAck message",
							e);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere(
					"Unable to send GetAdaptersAckMessage message '"
							+ messageAck.toString() + "'", e);
		}
	}

	private void processMethodCall(final MethodCallMessage message)
			throws Exception {
		if (this.getMode().equals(AdapterMode.NORMAL)
				|| this.getMode().equals(AdapterMode.TEST)) {
			return;
		}
		final MethodCallAckMessage messageAck = new MethodCallAckMessage();
		messageAck.setAdapterName(getName());
		messageAck.setId(message.getId());
		messageAck.setAdapterIp(UnitManager.ObjectModel.getUnit()
				.getIpAddress());
		try {
			send(messageAck.toString().getBytes(CHARSET), tstSocketAddress
					.getAddress(), tstSocketAddress.getPort());
		} catch (final UnsupportedEncodingException e) {
			UnitManager.Logging
					.logSevere(
							"Unsupported encoding exception occurred when attempting to send MethodCallAck message",
							e);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere(
					"Unable to send MethodCallAckMessage message '"
							+ messageAck.toString() + "'", e);
		}

		if (this.blockingMessages.containsKey(message.getId())
				&& this.blockingMessages.get(message.getId()).getMethodName()
						.equals(message.getMethodName())
				&& this.blockingMessages.get(message.getId()).getDirection()
						.equals(message.getDirection())) {
			synchronized (this.blockingMessages.get(message.getId())) {
				// This message is the result of our blocking method call
				this.blockingMessages.get(message.getId()).setParameters(
						message.getParameters());
				this.blockingMessages.get(message.getId()).setReturnValue(
						message.getReturnValue());
				this.blockingMessages.get(message.getId()).setSimulated(true);
				this.blockingMessages.get(message.getId()).notify();
			}
		} else {
			if (this.getMode().equals(AdapterMode.SIMULATION)
					|| this.getMode()
							.equals(AdapterMode.DEVICE_ONLY_SIMULATION)
					&& message.getDirection().equals(
							MethodCallDirection.DEVICE_TO_HANDLER)) {
				// Method call was injected and needs to be executed via
				// reflection
				try {
					final String returnValue = callMethodThroughReflection(
							message.getMethodName(), message.getParameters());
					if (isDebug()) {
						logDebug("Return value: "
								+ returnValue);
					}
				} catch (final Exception e) {
					UnitManager.Logging
							.logSevere("Unable to perform method call via reflection from message '"
									+ message.toString() + "'");
				}
			}
		}
	}

	private void processMethodCallAck(final MethodCallAckMessage message) {
		/** TODO Method call message was received by TST, so we can move on now */
	}

	private void processSetMode(final SetModeMessage message,
			final DatagramPacket packet) throws JSONException,
			ServiceIOException, UnknownHostException {
		final SetModeAckMessage setModeAck = new SetModeAckMessage();
		setModeAck.setAdapterName(getName());
		setModeAck.setId(message.getId());
		setModeAck.setNextId(getNextId());
		setModeAck.setAdapterIp(UnitManager.ObjectModel.getUnit()
				.getIpAddress());
		setModeAck.setMode(message.getMode());
		try {
			send(setModeAck, packet.getAddress(), message.getPort());
		} catch (final UnsupportedEncodingException e) {
			UnitManager.Logging
					.logSevere(
							"Unsupported encoding exception occurred when attempting to send SetModeAck message",
							e);
		}
		try {
			TST_TIMEOUT = message.getTstTimeout() > 0 ? message.getTstTimeout()
					: TST_TIMEOUT_DEFAULT;
			timer.startTimer(TST_TIMEOUT);
			setMode(message.getMode(), packet.getAddress(), message.getPort());
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere("Unable to set adapter mode", e);
		}
	}

	protected final Parameter handlerToDeviceCall(final String methodName)
			throws Exception {
		return handlerToDeviceCall(methodName, new Parameter[] {}, null);
	}

	protected final Parameter handlerToDeviceCall(final String methodName,
			final Parameter[] parameters) throws Exception {
		return handlerToDeviceCall(methodName, parameters, null);
	}

	protected final Parameter handlerToDeviceCall(final String methodName,
			final Parameter returnValue) throws Exception {
		return handlerToDeviceCall(methodName, new Parameter[] {}, returnValue);
	}

	protected final Parameter handlerToDeviceCall(final String methodName,
			final Parameter[] parameters, final Parameter returnValue)
			throws Exception {
		return methodCall(methodName, parameters, returnValue,
				MethodCallDirection.HANDLER_TO_DEVICE);
	}

	protected final Parameter deviceToHandlerCall(final String methodName)
			throws Exception {
		return deviceToHandlerCall(methodName, new Parameter[] {});
	}

	protected final Parameter deviceToHandlerCall(final String methodName,
			final Parameter[] parameters) throws Exception {
		return deviceToHandlerCall(methodName, parameters, null);
	}

	protected final Parameter deviceToHandlerCall(final String methodName,
			final Parameter returnValue) throws Exception {
		return deviceToHandlerCall(methodName, new Parameter[] {}, returnValue);
	}

	protected final Parameter deviceToHandlerCall(final String methodName,
			final Parameter[] parameters, final Parameter returnValue)
			throws Exception {
		return methodCall(methodName, parameters, returnValue,
				MethodCallDirection.DEVICE_TO_HANDLER);
	}

	private Parameter methodCall(final String methodName,
			final Parameter[] parameters, final Parameter returnValue,
			final MethodCallDirection direction) throws Exception {
		final MethodCallMessage methodCall = new MethodCallMessage();
		methodCall.setAdapterName(getName());
		methodCall.setDirection(direction);
		methodCall.setId(getMessageId());
		methodCall.setMethodName(methodName);
		methodCall.setParameters(parameters);
		// methodCall.setPort(adapterPort);
		methodCall.setAdapterIp(UnitManager.ObjectModel.getUnit()
				.getIpAddress());
		if (returnValue != null) {
			methodCall.setReturnValue(returnValue);
		}

		final Parameter o = send(methodCall, getTstSocketAddress());
		final Parameter[] returnedParameters = methodCall.getParameters();
		int length = 0;
		for (int i = 0; i < parameters.length; i++) {
			if (returnedParameters.length > i) {
				switch (parameters[i].getType()) {
				case BYTE:
					parameters[i].setByteValue(returnedParameters[i]
							.getByteValue());
					break;
				case DOUBLE:
					parameters[i].setDoubleValue(returnedParameters[i]
							.getDoubleValue());
					break;
				case INT:
					parameters[i].setIntegerValue(returnedParameters[i]
							.getIntegerValue());
					break;
				case LONG:
					parameters[i].setLongValue(returnedParameters[i]
							.getLongValue());
					break;
				case BYTE_ARRAY:
				case DOUBLE_ARRAY:
				case INT_ARRAY:
				case LONG_ARRAY:
					length = Array.getLength(parameters[i].getValue());
					for (int j = 0; j < length; j++) {
						Array.set(parameters[i].getValue(), j, Array.get(
								returnedParameters[i].getValue(), j));
					}
					break;
				}
			}
		}
		return o;
	}

	private String callMethodThroughReflection(final String methodName,
			final Object[] parameters) throws Exception {
		for (final Method m : this.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				// && m.getParameterTypes().length == parameters.size()) {
				final Object[] args = new Object[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					if (m.getParameterTypes()[i].isArray()) {
						if (isDebug()) {
							logDebug("Parameter is an array");
						}
						final org.json.JSONArray arr = (JSONArray) parameters[i];
						final Object arg = Array.newInstance(m
								.getParameterTypes()[i].getComponentType(), arr
								.length());
						for (int j = 0; j < arr.length(); j++) {
							// Array.set(arg, j, m.getParameterTypes()[i]
							// .getComponentType().getConstructor(
							// String.class).newInstance(
							// arr.get(j)));
							Array.set(arg, j, arr.get(j));
						}
						args[i] = arg;
					} else {
						// args[i] = m.getParameterTypes()[i].getConstructor(
						// String.class).newInstance(parameters.get(i));
						args[i] = parameters[i];
						// getParameter(m.getParameterTypes()[i],
						// parameters.get(i));
					}
				}
				return m.invoke(this, args).toString();
			}
		}
		throw new Exception("Unknown method call '" + methodName + "'");
	}

	private void send(final AdapterMessage message, final InetAddress address,
			final int port) throws UnsupportedEncodingException,
			ServiceIOException {
		// return send(message, address, port);
		send(message.toString().getBytes(CHARSET), address, port);
	}

	private Parameter send(final MethodCallMessage msg,
			final InetSocketAddress socketAddress) throws Exception {
		send(msg.toString().getBytes(CHARSET), socketAddress.getAddress(),
				socketAddress.getPort());
		try {
			if (isWaitForTST(msg.getDirection())) {
				// if (!getMode().equals(AdapterMode.TEST)) {
				this.blockingMessages.put(msg.getId(), msg);
				synchronized (msg) {
					msg.wait(TST_TIMEOUT);
				}
				// If the message was set to simulated while waiting, then a
				// response was received from the TST. Otherwise, the
				// message has timed out and an exception is to be thrown.
				if (msg.isSimulated()) {
					if (isDebug()) {
						logDebug("Received return value for message '"
										+ msg.toString() + "'");
					}
					// this.blockingMessages.get(msg.getId()).setParameters(
					// msg.getParameters());
					return this.blockingMessages.remove(msg.getId())
							.getReturnValue();
				} else {
					this.blockingMessages.remove(msg.getId());
					this.setMode(AdapterMode.NORMAL, null, -1);
					UnitManager.Logging
							.logDebug("Timed out waiting for response to message '"
									+ msg.toString() + "'");
					throw new AdapterMessageTimeoutException(
							"Timed out waiting for response to message '"
									+ msg.toString() + "' in adapter '"
									+ getName() + "'");
				}
			} else {
				return msg.getReturnValue();
			}
		} catch (final JSONException e) {
			UnitManager.Logging.logSevere(
					"Error processing JSON message from TST", e);
		} catch (final InterruptedException e) {
			UnitManager.Logging
					.logWarning("Interrupted waiting for response to message '"
							+ msg.toString() + "' in adapter '" + getName()
							+ "'");
		}
		return null;
	}

	// private void createAdapterSocket(InetAddress ipAddress) throws Exception
	// {
	// // Try the configured port/default socket
	// try {
	// this.adapterSocket = new DatagramSocket(adapterPort, ipAddress);
	// return;
	// } catch (Exception e) {
	// UnitManager.Logging.logWarning("Unable to user port " + adapterPort
	// + " for adapter/TST socket address.");
	// }
	// // Try the next 10 sockets and then fail
	// for (int i = 0; i < 10; i++) {
	// try {
	// this.adapterSocket = new DatagramSocket(++adapterPort,
	// ipAddress);
	// return;
	// } catch (Exception e) {
	// UnitManager.Logging.logWarning("Unable to user port "
	// + adapterPort + " for adapter/TST socket address.");
	// }
	// }
	// throw new Exception("Unable to create adapter/TST communication socket");
	//
	// }

	/**
	 * @param object
	 * @return array of given object
	 * @deprecated
	 */
	public Object convertObjectToArray(final Object object) {
		Object array;
		if (object.getClass().isArray()) {
			array = Array.newInstance(object.getClass().getComponentType(),
					Array.getLength(object));
		} else {
			array = object;
		}
		return array;
	}

	private void threadProcessorAction(final ThreadProcessor processor)
			throws InterruptedException {
		if (isTSTConnected() && timer != null && timer.hasExpired()) {
			final String tstSocketConnection = this.tstSocketAddress.toString();
			setMode(AdapterMode.NORMAL, null, -1);
			if (isDebug()) {
				logDebug("Adapter '" + getName()
						+ "' has timed out it TST connection at '"
						+ tstSocketConnection + "' due to inactivity");
			}
		}
		synchronized (this) {
			this.wait(1000);
		}
	}

	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		setPort(DEFAULT_ADAPTER_PORT);

		setSetupPriority(10);

		this.blockingMessages = new HashMap<Long, MethodCallMessage>();
	}

	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {
		super.onStarted(source, args);
		try {
			// createAdapterSocket(java.net.InetAddress.getLocalHost());
			this.tstReceiverThread = new ThreadProcessor("Adapter '"
					+ getName() + "' TST receiver thread",
					new ThreadProcessorListener() {
						public void threadProcessorAction(
								ThreadProcessor processor)
								throws InterruptedException {
							Adapter.this
									.threadProcessorAction(tstReceiverThread);
						}
					});
			this.tstReceiverThread.start();
			this.timer = new CountdownTimer();
			timer.startTimer(TST_TIMEOUT);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	@Override
	protected void onStopped(final Object source, final ServiceStopArgs args)
			throws ServiceStopException {
		super.onStopped(source, args);
		try {
			this.tstReceiverThread.stop();
		} catch (final ThreadProcessorException e) {
			UnitManager.Logging
					.logSevere("Exception occurred attempting to stop TST receiver thread in adapter '"
							+ getName() + "'");
		}
	}

	@Override
	protected void onReceivedMessage(final Object source,
			final MessagingServiceReceiveMessageArgs args) {
		processPacket(args.getDatagramPacket());
	}
	
}
