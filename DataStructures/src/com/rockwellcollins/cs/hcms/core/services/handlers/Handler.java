package com.rockwellcollins.cs.hcms.core.services.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.rockwellcollins.cs.hcms.core.ComponentDestroyArgs;
import com.rockwellcollins.cs.hcms.core.ComponentDestroyException;
import com.rockwellcollins.cs.hcms.core.ComponentImportXmlElementArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.ComponentXmlParserException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.TwoWayMap;
import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.ServiceRunArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceRunException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceState;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerListenMode;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateChangeRequestMessage;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;

public class Handler extends Service {

	private static final long serialVersionUID = 1L;

	public static final String SETTING_HANDLER_QUEUE_SIZE = "handler queue size";
	public static final String SETTING_HANDLER_QUEUE_TIMEOUT = "handler queue timeout";
	public static final String SETTING_HANDLER_CHECK_LISTEN_STATES = "handler check listen states";

	private TwoWayMap<String, String> propertyToStateMap;
	private ArrayList<String> listenStates;
	private int pollTimeout = 0; // turn off background timer by default (must enable via LCP)
	private int queueCapacity = 1000;
	private StateManagerListenMode mode = StateManagerListenMode.CHOICE;;
	private boolean checkListenStates;
	private boolean discardInitialStates = true;
	private boolean queueFullLogged = false;
	
	private transient CountdownTimer pollTimer;
	private transient StateManager stateManager;
	private transient StateDatabase stateDatabase;
	private transient HandlerQueue handlerQueue;
	private transient PropertyMapChangedArgs propertyMapChangedArgs;
	private transient PropertyChangeTimeoutArgs propertyChangeTimeoutArgs;
	private transient ArrayList<HandlerListener> listeners;

	private final StateMap convertPropertyMapToStateMap(
			final PropertyMap propertyMap) {

		final StateMap stateMap = new StateMap();

		for (final Entry<String, String> property : propertyMap.entrySet()) {

			final String state = getStateName(property.getKey());

			if (state == null) {
				UnitManager.Logging
						.logSevere("Property Change request from Handler '"
								+ toString()
								+ "' contained an unknown property '"
								+ property.getKey() + "'.  Skipping.");
			} else {

				stateMap.put(state, property.getValue());

			}
		}

		return stateMap;
	}

	private final PropertyMap convertStateMapToPropertyMap(
			final StateMap stateMap) {

		final PropertyMap propertyMap = new PropertyMap();

		if (stateMap == null) {
			UnitManager.Logging
					.logWarning("Handler '"
							+ getName()
							+ "' tried to convert a state map to a property map when the state map is null.  Skipping.");
		} else {

			for (final Entry<String, String> state : stateMap.entrySet()) {

				String stateName = state.getKey();
				String stateValue = state.getValue();

				if (stateName == null) {
					UnitManager.Logging
							.logWarning("Handler '"
									+ getName()
									+ "' tried to convert a state name that was null.  Skipping.");
					continue;
				}

				if (stateValue == null) {
					UnitManager.Logging
							.logWarning("Handler '"
									+ getName()
									+ "' tried to convert state '"
									+ stateName
									+ "' to a property that has a value that is null.  Skipping.");
					continue;
				}

				final String propertyName = getPropertyName(stateName);

				propertyMap.put(propertyName, stateValue);
			}
		}

		return propertyMap;
	}

	public final StateManagerListenMode getListenMode() {
		return mode;
	}

	public final void setListenMode(StateManagerListenMode mode) {
		this.mode = mode;
	}

	public final ArrayList<String> getListenStates() {
		return listenStates;
	}

	public String getProperty(final String propertyName)
			throws HandlerPropertyNotFoundException {

		if (this.stateManager == null) {

			throw new HandlerPropertyNotFoundException(
					"Handler '"
							+ getName()
							+ "' is not attached to a State Manager and can not get property '"
							+ propertyName + "'");
		}

		String stateName = getStateName(propertyName);

		if (stateName == null) {

			throw new HandlerPropertyNotFoundException("Handler '" + toString()
					+ "' could not find a State matching the property '"
					+ propertyName + "'");

		}

		String value = stateDatabase.getValue(stateName);

		if (value == null) {
			throw new HandlerPropertyNotFoundException("Handler '" + toString()
					+ "' could not find the state name '" + stateName
					+ "' in the Database.");
		}

		return value;
	}
	
	public int getPropertyIndex(final String propertyName)
		throws HandlerPropertyNotFoundException {

		String stateName = getStateName(propertyName);
	
		if (stateName == null) {
			
			throw new HandlerPropertyNotFoundException("Handler '" + toString()
					+ "' could not find a State matching the property '"
					+ propertyName + "'");
		}
	
		int index = stateDatabase.getIndex(stateName);
	
		if (index == -1) {
			throw new HandlerPropertyNotFoundException("Handler '" + toString()
					+ "' could not find the state name '" + stateName
					+ "' in the Database.");
		}
	
		return index;
	}
	
	public int getPropertyCrc(final int index, final boolean useCache) {
		int crc = 0;
		
		if (index > -1) {
			crc = stateDatabase.getValueCrc(index, useCache);
		} else {
			UnitManager.Logging.logSevere("Handler '" + getName()
															+ "' index out of bounds.");
		}
		return crc;
	}

	public String getPropertyDefault(final String propertyName)
			throws HandlerPropertyNotFoundException {

		if (this.stateManager == null) {
		
			throw new HandlerPropertyNotFoundException(
					"Handler '"
							+ getName()
							+ "' is not attached to a State Manager and can not get property '"
							+ propertyName + "'");
		}
		
		String stateName = getStateName(propertyName);
		
		if (stateName == null) {
		
			throw new HandlerPropertyNotFoundException("Handler '" + toString()
					+ "' could not find a State matching the property '"
					+ propertyName + "'");
		
		}
		
		String value = stateDatabase.getDefaultValue(stateName);
		
		if (value == null) {
			throw new HandlerPropertyNotFoundException("Handler '" + toString()
					+ "' could not find the state name '" + stateName
					+ "' in the Database.");
		}
		
		return value;
	}

	public String getPropertyName(final String statename) {
		return propertyToStateMap.getKey(statename);
	}

	protected final StateManager getStateManager() {
		return stateManager;
	}

	protected final String getStateName(final String propertyName) {
		return propertyToStateMap.getValue(propertyName);
	}

	public final boolean hasProperty(final String propertyName) {
		return propertyToStateMap.getValue(propertyName) != null;
	}

	private final void importPropertyElements(final NodeList eProperties)
			throws ComponentXmlParserException {

		listenStates.clear();

		if (eProperties != null && eProperties.getLength() > 0) {

			for (int i = 0; i < eProperties.getLength(); i++) {

				if (eProperties.item(i) instanceof Element) {

					final Element eProperty = (Element) eProperties.item(i);

					final String name = eProperty.getAttribute("name");
					final String state = eProperty.getAttribute("state");
					String listen = eProperty.getAttribute("listen");

					if (name == null || name.length() == 0) {
						throw new ComponentXmlParserException(
								"Property element must have a name attribute.");
					}

					if (state == null || state.length() == 0) {
						throw new ComponentXmlParserException(
								"Property element must have a state attribute.");
					}

					if (listen == null || listen.length() == 0) {
						listen = "true";
					}

					propertyToStateMap.put(name, state);

					if (Boolean.parseBoolean(listen)) {
						getListenStates().add(state);
					}
				}
			}
		}
	}

	protected void onHandlerEvent(final Object source,
			final HandlerEventArgs args) {
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		pollTimeout = getSetting(SETTING_HANDLER_QUEUE_TIMEOUT, pollTimeout);
		checkListenStates = getSetting(SETTING_HANDLER_CHECK_LISTEN_STATES, false);

		handlerQueue = new HandlerQueue(queueCapacity);
	}
	
	@Override
	protected void onImportXmlElement(final Object source,
			final ComponentImportXmlElementArgs args)
			throws ComponentXmlParserException {

		super.onImportXmlElement(source, args);

		final NodeList propElements = args.getElement().getElementsByTagNameNS(
				"*", "property");

		importPropertyElements(propElements);
	}

	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		setSetupPriority(20);

		propertyMapChangedArgs = new PropertyMapChangedArgs();
		propertyChangeTimeoutArgs = new PropertyChangeTimeoutArgs();
		listenStates = new ArrayList<String>();
		propertyToStateMap = new TwoWayMap<String, String>();
		listeners = new ArrayList<HandlerListener>();
		checkListenStates = false;
		pollTimer = new CountdownTimer();
	}

	protected void onPropertyChanged(final Object source,
			final PropertyChangedArgs args) {
		
		synchronized (listeners) {
			int len = listeners.size();
			for (int i = 0; i < len; i++) {
				listeners.get(i).handlerPropertyChanged(this, args);
			}
		}
	}

	protected void onPropertyChangedTimeout(final Object source,
			final PropertyChangeTimeoutArgs args) {
		
		synchronized (listeners) {
			int len = listeners.size();
			for (int i = 0; i < len; i++) {
				listeners.get(i).handlerPropertyChangedTimeout(this, args);
			}
		}
	}

	protected void onPropertyMapChanged(final Object source,
			final PropertyMapChangedArgs args) {

		synchronized (listeners) {
			int len = listeners.size();
			for (int i = 0; i < len; i++) {
				listeners.get(i).handlerPropertyMapChanged(this, args);
			}
		}

		for (final Entry<String, String> property : args.getPropertyMap()
				.entrySet()) {

			final PropertyChangedArgs pArgs = new PropertyChangedArgs(property
					.getKey(), property.getValue());

			onPropertyChanged(source, pArgs);
		}
	}

	@Override
	protected final void onRun(final Object source, final ServiceRunArgs args)
			throws ServiceRunException {

		while (getServiceState() == ServiceState.RUNNING) {
			try {
				HandlerQueueItem queueItem;

				if (handlerQueue == null) {
					UnitManager.Logging.logSevere("Handler '" + getName()
							+ "' queue is null.  Stopping Handler.");
					break;
				}

				if (pollTimeout == 0) {
					queueItem = handlerQueue.take();
				} else {
					queueItem = handlerQueue.poll(pollTimeout,
							TimeUnit.MILLISECONDS);
				}

				if (queueItem == null) {
					try {
						processTimeout();
					} catch (final Exception e) {
						UnitManager.Logging
								.logSevere(
										"Handler '"
												+ getName()
												+ "' onPropertyChangedTimeout threw an exception that was not handled",
										e);
					}
				} else {
					// ensure background timer processing isn't starved
					// during a continuous stream on incoming messages
					if (pollTimeout != 0) {
						if (pollTimer.hasExpired()) {
							processTimeout();
						}
					}
					
					switch (queueItem.getType()) {
					case EVENT:
						try {
							processEvent(queueItem.getEvent(), queueItem
									.getTag());
						} catch (final Exception e) {
							UnitManager.Logging
									.logSevere(
											"Handler '"
													+ getName()
													+ "' onHandlerEvent threw an exception that was not handled",
											e);
						}
						break;
					case STATE_CHANGE:
						try {
							processStateChange(queueItem.getStateMap());
						} catch (final Exception e) {
							UnitManager.Logging
									.logSevere(
											"Handler '"
													+ getName()
													+ "' onPropertyMapChanged threw an exception that was not handled",
											e);
						}
						break;
					}
				}

			} catch (final InterruptedException e) {
				if (UnitManager.Logging.isDebug()) {
					UnitManager.Logging
							.logDebug("Handler Working Thread was Interrupted.  Setting Service to Error Mode");
				}
				setServiceState(ServiceState.ERROR);
			} catch (final Exception e) {
				UnitManager.Logging.logSevere("Handler '" + toString()
						+ "' threw an exception that was not caught.", e);
			}
		}
	}

	public void discardInitialStates(boolean discard){
		this.discardInitialStates = discard;
	}
	
	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
	throws ServiceStartException {
		super.onStarted(source, args);
		if (pollTimeout != 0) {
			pollTimer.startTimer(pollTimeout);
		}
		if (!discardInitialStates) {
			queueInitialStates();
		}
	}

	@Override
	protected void onStopped(Object source, ServiceStopArgs args)
			throws ServiceStopException {

		super.onStopped(source, args);

		if (handlerQueue != null) {
			handlerQueue.clear();
		}
	}

	protected final void processEvent(final HandlerEvent event, final Object tag) {
		final HandlerEventArgs args = new HandlerEventArgs(event, tag);
		onHandlerEvent(this, args);
	}

	private final void processStateChange(final StateMap stateMap) {

		final PropertyMap propertyMap = convertStateMapToPropertyMap(stateMap);

		if (propertyMap.size() == 0) {
			return;
		}

		propertyMapChangedArgs.setPropertyMap(propertyMap);

		onPropertyMapChanged(this, propertyMapChangedArgs);
	}

	private final void processTimeout() {
		onPropertyChangedTimeout(this, propertyChangeTimeoutArgs);
		pollTimer.restart();
	}

	public final void queueHandlerEvent(final HandlerEvent event,
			final Object tag) {
		try {
			handlerQueue.put(new HandlerQueueItem(event, tag));
		} catch (final NullPointerException e) {
			UnitManager.Logging
					.logSevere(
							"Handler '"
									+ getName()
									+ "' queue is null.  Ensure onSetup is calling super.onSetup.",
							e);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Could not queue Handler Event '"
					+ event + "' in Handler '" + getName() + "'.", e);
		}
	}
	
	
	public final void queueInitialStates() {
		// queue the current state values for all states for which the handler listens
		StateMap stateMap = new StateMap();
		String state, value;
		int len = listenStates.size();
		for (int i=0; i<len; i++) {
			state = listenStates.get(i);
			value = stateDatabase.getValue(state);
			if (stateMap.containsKey(state)) {
				UnitManager.Logging.logWarning("Handler: " + getName() 
						+ " initial queue of duplicate state: " + state); 
			}
			stateMap.put(state, value);
		}
		if (stateMap.size() > 0) {
			queueStatesChanged(stateMap);
			
			if (UnitManager.Logging.isCore())
				UnitManager.Logging.logCore("Handler: " + getName() 
						+ " initial queue of: " + len + " states");
		}
	}
	
	public final void flushQueue() {
		handlerQueue.clear();
		
	}
	
	public final int peekQueue() {
		return handlerQueue.size();
	}
	
	public final void queueStatesChanged(final StateMap stateMap) {
		try {
			handlerQueue.add(new HandlerQueueItem(stateMap));
			
			if (queueFullLogged == true) {
				if (handlerQueue.size() < (queueCapacity/2)) {
					queueFullLogged = false;
				}
			}
		} catch (final NullPointerException e) {
			UnitManager.Logging.logSevere(
					"Handler '" + getName()
					+ "' queue is null.  Ensure onSetup is calling super.onSetup.",
					e);
		} catch (final IllegalStateException e) {
			if (!queueFullLogged) {
				UnitManager.Logging.logSevere(
					"Exception in Handler '" + toString()
					+ "' Handler Queue is full.",
					e);
				queueFullLogged = true;
			}
		} catch (final Exception e) {
			UnitManager.Logging.logSevere(
					"Exception in Handler '" + toString()
					+ "' when calling HandlerTemplate::queueStatesChanged, called by State Manager.",
					e);
		}
	}

	protected final void setPollTimeout(final int ms) {
		pollTimeout = ms;
	}
	
	public boolean setPropertyDefault(final ArrayList<String> properties) {
		boolean success = true;

		if (stateManager == null) {

			UnitManager.Logging.logWarning("Handler '"	+ toString()
				+ "' is not attached to a State Manager and cannot set property defaults.");

			success = false;

		} else {
			
			final PropertyMap propertyMap = new PropertyMap();
			final Iterator<String> itr = properties.iterator();
			String propName;
			String defaultValue;

			while (itr.hasNext()) {
				propName = itr.next();
				try {
					defaultValue = getPropertyDefault(propName);
					propertyMap.put(propName, defaultValue);
				} catch (HandlerPropertyNotFoundException hpnfe) {
					UnitManager.Logging
						.logWarning("setPropertyDefault from Handler '"
						+ toString() + "' contained bad property name: " 
						+ propName + " Ignoring.");
				}
			}

			final StateMap stateMap = convertPropertyMapToStateMap(propertyMap);

			if (stateMap.size() == 0) {

				UnitManager.Logging
						.logWarning("setPropertyDefault request from Handler '"
								+ toString() + "' is Empty.  Ignoring Request.");

				success = false;

			} else {
				if (checkListenStates) {
					for (Entry<String, String> set : stateMap.entrySet()) {
						if (listenStates.contains(set.getKey())) {
							UnitManager.Logging.logWarning("Handler: '" + getName() 
									+ "' setting state for which it listens: "
									+ set.getKey());							
						}
					}
				}
				this.stateManager.requestStateChange(getName(), stateMap);
			}
		}
		return success;
	}

	public boolean setProperty(final PropertyMap propertyMap) {

		boolean success = true;

		if (stateManager == null) {

			UnitManager.Logging
					.logWarning("Handler '"
							+ toString()
							+ "' is not attached to a State Manager and can not set property map.");

			success = false;

		} else {

			final StateMap stateMap = convertPropertyMapToStateMap(propertyMap);

			if (stateMap.size() == 0) {

				UnitManager.Logging
						.logWarning("State Change request from Handler '"
								+ toString() + "' is Empty.  Ignoring Request.");

				success = false;

			} else {

				if (checkListenStates) {
					for (Entry<String, String> set : stateMap.entrySet()) {
						if (listenStates.contains(set.getKey())) {
							UnitManager.Logging.logWarning("Handler: '" + getName() 
									+ "' setting state for which it listens: "
									+ set.getKey());							
						}
					}
				}
				this.stateManager.requestStateChange(getName(), stateMap);
			}
		}

		return success;
	}

	public boolean setProperty(final String name, final String value) {

		final PropertyMap propertyMap = new PropertyMap();

		propertyMap.put(name, value);

		return setProperty(propertyMap);
	}

	public boolean setPropertyDefault(final String name) {

		final ArrayList<String> propertyList = new ArrayList<String>();

		propertyList.add(name);

		return setPropertyDefault(propertyList);
	}


	public final void setStateManager(final StateManager stateManager) {

		if (stateManager == null) {

			this.stateManager = null;
			this.stateDatabase = null;

		} else if (this.stateManager == null) {

			this.stateManager = stateManager;
			this.stateDatabase = stateManager.getStateDatabase();

		} else {

			UnitManager.Logging.logSevere("Handler '" + toString()
					+ "' already attached to State Manager '"
					+ this.stateManager.toString()
					+ "'.  Ignonring new Attach request.");
		}
	}

	public final boolean register(final HandlerListener listener) {
		synchronized (listeners) {
			return listeners.add(listener);
		}
	}

	public final boolean unregister(final HandlerListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	@Override
	protected void onDestroy(Object source, ComponentDestroyArgs args)
			throws ComponentDestroyException {

		super.onDestroy(source, args);

		if (stateManager != null) {
			stateManager.detachHandler(this);
		}
	}

	@Override
	protected final void onStarting(Object source, ServiceStartArgs args)
			throws ServiceStartException {
		super.onStarting(source, args);
	}
}
