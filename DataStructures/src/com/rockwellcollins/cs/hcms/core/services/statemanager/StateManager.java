package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.Component;
import com.rockwellcollins.cs.hcms.core.ComponentContainer;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.IntArray;
import com.rockwellcollins.cs.hcms.core.collections.QueueListener;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessorException;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;
import com.rockwellcollins.cs.hcms.core.profiling.StopWatch;
import com.rockwellcollins.cs.hcms.core.services.MulticastService;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceReceivedDatagramPacketArgs;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceSocketException;
import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartupType;
import com.rockwellcollins.cs.hcms.core.services.ServiceState;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.handlers.HandlerEvent;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.PersistentData;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.BlockCrcMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.BlockCrcRequestMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.BlockRequestMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.ControlMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.DatabaseCrcMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.DatabaseCrcRequestMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateChangeRequestMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateChangeResponseMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StatusMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.SynchronizeMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.rules.RulesEngine;
import com.rockwellcollins.cs.hcms.core.services.statemanager.rules.RulesEngineException;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateServiceException;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateHandler;
import com.rockwellcollins.cs.hcms.core.utils.StringHelper;

public class StateManager extends MulticastService implements
		QueueListener<StateManagerMessage> {

	// CR RC-TC00013024
	private static final String DEFAULT_BROADCAST_ADDRESS = "10.255.255.255";
	private static final long serialVersionUID = 1L;
	private static final byte TSTtoolGroup = (byte) 254;
	private static final byte numGroups = (byte) 10; // used to allocate lookup
	// array
	private static final byte BDUGroup = (byte) 9;
	private static final byte HTSEGroup = (byte) 8;
	private static final byte WAPGroup = (byte) 7;
	private static final byte TC6000Group = (byte) 6;
	private static final byte BDP6002Group = (byte) 5;
	private static final byte HDAV6000Group = (byte) 4;
	private static final byte MCDGroup = (byte) 3;
	private static final byte HDAV3000Group = (byte) 2;
	private static final byte PSWGroup = (byte) 1;
	private static final byte UnusedGroup = (byte) 0;

	private static final byte BDUPriority = (byte) 9;
	private static final byte HTSEPriority = (byte) 8;
	private static final byte HDAV6000Priority = (byte) 7;
	private static final byte MCDPriority = (byte) 6;
	private static final byte HDAV3000Priority = (byte) 5;
	private static final byte PSWPriority = (byte) 4;
	private static final byte TC6000Priority = (byte) 3;
	private static final byte BDP3002Priority = (byte) 2;
	private static final byte WAPPriority = (byte) 1;
	private static final byte UnusedPriority = (byte) 0;

	private static byte groupPriority[];

	public static final String SETTING_STATE_MANAGER_CRC_RESPONSE_DEPTH = "state manager crc response depth";
	public static final String SETTING_STATE_MANAGER_CRC_REQUEST_DEPTH = "state manager crc request depth";
	public static final String SETTING_STATE_MANAGER_SYSTEM_STATES = "state manager system states";
	public static final String SETTING_STATE_MANAGER_CRC_DEPTH = "state manager crc depth";
	public static final String SETTING_STATE_MANAGER_RETRY_DEPTH = "state manager retry depth";
	public static final String SETTING_STATE_MANAGER_MULTICAST_GROUP = "state manager multicast group";
	public static final String SETTING_STATE_MANAGER_MODE = "state manager mode";
	public static final String SETTING_STATE_MANAGER_DATABASE_GLOBAL_PERSISTENT_CLASS = "state manager database global persistent class";
	public static final String SETTING_STATE_MANAGER_DATABASE_GLOBAL_PERSISTENT_NAME = "state manager database global persistent name";
	public static final String SETTING_STATE_MANAGER_DATABASE_FILE = "state manager database file";
	public static final String SETTING_STATE_MANAGER_DATABASE_SERIAL_FILE = "state manager database serial file";
	public static final String SETTING_STATE_MANAGER_DATABASE_SIZE = "state manager database size";
	public static final String SETTING_STATE_MANAGER_ERROR_TIMEOUT = "state manager error timeout";
	public static final String SETTING_STATE_MANAGER_LOADING_TIMEOUT = "state manager loading timeout";
	public static final String SETTING_STATE_MANAGER_SYSTEM_TIMEOUT = "state manager system timeout";
	public static final String SETTING_STATE_MANAGER_PROCESS_RETRY_THRESHOLD = "state manager process retry threshold";
	public static final String SETTING_STATE_MANAGER_STATE_REQUEST_CACHE_SIZE = "state manager state request cache size";
	public static final String SETTING_STATE_MANAGER_PROCESS_REQUEST_SIZE = "state manager process request size";
	public static final String SETTING_STATE_MANAGER_STATE_CRC_MISMATCH_BLOCK_COUNT = "state manager state crc mismatch block count";
	public static final String SETTING_STATE_MANAGER_STATE_GSM_CORRUPTION = "state manager state gsm corruption";
	public static final String SETTING_STATE_MANAGER_STATE_TEST_MODE = "state manager state test mode";
	public static final String SETTING_STATE_MANAGER_STATE_LAST_RULE_EXECUTED = "state manager state last rule executed";
	public static final String SETTING_STATE_MANAGER_STATE_GSM_RACE_CONDITION_COUNT = "state manager state gsm race condition count";
	public static final String SETTING_STATE_MANAGER_STATE_ACTIVE_GSM = "state manager state active gsm";
	public static final String SETTING_STATE_MANAGER_SYNCHRONIZE_AGE_PADDING = "state manager synchronize age padding";
	public static final String SETTING_STATE_MANAGER_RULES_FILE = "state manager rules file";
	public static final String SETTING_STATE_MANAGER_ELECTION_RANDOM_TIME = "state manager election random time";
	public static final String SETTING_STATE_MANAGER_MASTER_STATUS_BROADCAST_TIME = "state manager master status broadcast time";
	public static final String SETTING_STATE_MANAGER_STATUS_BROADCAST_TIME = "state manager status broadcast time";
	public static final String SETTING_STATE_MANAGER_DATABASE_CRC_BROADCAST_TIME = "state manager database crc broadcast time";
	public static final String SETTING_STATE_MANAGER_ELECTION_TIME = "state manager election time";
	public static final String SETTING_STATE_MANAGER_QUEUE_TIMEOUT = "state manager queue timeout";
	public static final String SETTING_STATE_MANAGER_QUEUE_PRIORITY = "state manager queue priority";
	public static final String SETTING_STATE_MANAGER_DEFERRED_QUEUE_SIZE = "state manager deferred queue size";
	public static final String SETTING_STATE_MANAGER_PENDING_QUEUE_SIZE = "state manager pending queue size";
	public static final String SETTING_STATE_MANAGER_PENDING_RETRY_TIME = "state manager pending retry time";
	public static final String SETTING_STATE_MANAGER_PENDING_RETRY_COUNT = "state manager pending retry count";
	public static final String SETTING_STATE_MANAGER_ELECTION_PRIORITY = "state manager election priority";
	public static final String SETTING_STATE_MANAGER_INITIAL_ELECTION_TIME = "state manager initial election time";
	public static final String SETTING_STATE_MANAGER_SERVICE_START_DELAY_TIME = "state manager services startup delay";
	public static final String SETTING_STATE_MANAGER_LOAD_PERSISTENT_FAIL_TIMEOUT = "state manager load persistent fail timeout";

	private transient StateManagerQueueProcessor queueProcessor;
	private transient ArrayList<StateChangeRequestMessage> deferredQueue;
	private transient StateManagerRequestMessageCache stateChangeRequestCache;
	private transient StateManagerResponseMessageCache stateChangeResponseCache;
	private transient ArrayList<StateChangeRequestMessage> pendingQueue;
	private transient StateDatabase stateDatabase;
	private transient InetAddress masterStateManagerAddress = null;
	private transient HashMap<String, ArrayList<Handler>> listenChoice;
	private transient ArrayList<Handler> listenAll;
	private transient RulesEngine rulesEngine;
	private transient ArrayList<Handler> handlers;
	public transient StateManagerMessageCounter messageCounter;
	private transient Random random; // No longer used
	private transient StateManagerBlockRequestMap blockRequestMap;
	private transient StateManagerVote persistentVote;
	private transient StateManagerVote signatureVote;
	private transient StopWatch loadingStopWatch;
	private transient StopWatch errorStopWatch;
	private transient ArrayList<DatabaseListener> databaseListeners;
	private transient ArrayList<StateManagerListener> stateManagerListeners;
	private transient ArrayList<RulesEngineListener> rulesEngineListeners;
	private transient CountdownTimer electionTimer;
	private transient CountdownTimer candidateTimer;
	private transient CountdownTimer databaseBroadcastTimer;
	private transient CountdownTimer statusBroadcastTimer;
	private transient CountdownTimer systemTimeoutTimer;
	private transient CountdownTimer serviceDelayTimer;
	private transient CountdownTimer pendingQueueRetryTimer;
	private transient CountdownTimer deferredQueueRetryTimer;
	private transient boolean pendingQueueErrorPosting;
	private transient boolean deferredQueueErrorPosting;
	private transient boolean majorSigErrorPosting;

	private int stateRequestCacheSize = 1000; // CR-8998
	private int pendingQueueSize = 200;
	private int deferredQueueSize = 200;
	private int pendingRetryTime = 10000; // CR-8998
	private int deferredRetryTime = 5000; // CR-8998
	private int pendingRetryCount = 60;
	private InetAddress multicastGroup = null;
	private int electionPriority = 100;
	private int queueTimeout = 100;
	private int queuePriority = 6;
	private long electionTime = 2000;
	private long candidateTime = 6000;
	private int electionRandomTime = 1000; // No longer used.
	private long initialElectionTime = 8000;
	private long databaseBroadcastTime = 2000;
	private long masterStatusBroadcastTime = 500;
	private long statusBroadcastTime = 2000;
	private long serviceStartDelay = 0;
	private long synchronizeAgePadding = 1000;
	private String rulesFilename = "conf/default/system/data/rules/Rules.xml";
	// private String rulesFilename = "data\\rules\\Rules.xml"; // for desktop
	// PC testing
	private StateManagerMode mode = StateManagerMode.SYSTEM;
	private StateManagerMode defaultMode = StateManagerMode.SYSTEM;
	private String stateActiveGsm = "SYS/GSM/ActiveGsm";
	private String stateGsmRaceConditionCount = "SYS/GSM/GsmRaceConditionCount";
	private String stateLastRuleExecuted = "SYS/GSM/LastRuleExecuted";
	private String stateTestMode = "SYS/GSM/TestMode";
	private String stateGsmCorruption = "SYS/GSM/ActiveGsmCrcCorrupted";
	private String stateCrcMismatchBlockCount = "SYS/GSM/CrcMismatchBlockCount";
	private int processRequestSize = 750;
	private int processRetryThreshold = 400;
	private int systemTimeout = 6000;
	private StateManagerStatus status = StateManagerStatus.ONLINE;
	private transient StatusMessage masterStatusMessage;
	private boolean systemStates = true;
	private long loadingTimeout = 120000;
	private long errorTimeout = 20000;
	private String databaseXmlFilename = "conf/default/system/data/database/Database.xml";
	// private String databaseXmlFilename = "data\\database\\Database.xml"; //
	// for desktop PC testing
	private String databaseSerFilename = "database.ser";
	private String databaseGlobalPersistentName = "persistent.global.ser";
	private String databaseGlobalPersistentClass = "";
	private int local_databaseSignature = 0; // Crc of loaded Database.xml.
	// Should be same for all state
	// managers.
	private int master_databaseSignature = 0; // Crc of incoming GSM signature.
	// Should be same as local.
	private int databaseSize = 10000; // The default size to accommodate the
	// number of records in Database.xml
	private int retryDepth = 50;
	private int crcDepth = 0;
	private int crcIndex = 0;
	private int crcRequestDepth = 0;
	private int crcResponseDepth = 0;
	private boolean ignoreBlockCRCRequests = false;
	private boolean throttle = false;
	private double lastRuleExecTime = 0;
	private boolean serviceDelayFlag = false;
	private long lastMasterId = 0;
	private boolean send_blockrequest_all_once = true;
	private boolean noStateManagerLogged = false;
	private boolean retryNotReadyLogged = false;
	private int pendingMessageRetryCount = 0;
	private int stateChangeRequests = 0;
	private int incomingStateChangeRequests = 0;
	private int incomingStateChangeDuplicates = 0;
	private long stateRequestQueueTime = 0;
	private int stateRequestQueueCount = 0;
	private int stateRequestProcessTime = 1000;
	private int currRequestQueueTime = 0;
	private long lastRetryTime = 0;
	private long stateRequestResponseTime;
	private int stateRequestResponseCount;
	private int stateRequestResponseAvg;
	private boolean incomingRequestLogging = true;
	private int incomingRequestsDropped = 0;
	private int loadPersistentFailTimeout = 10;

	public final void setSendBlockRequestAllOnce( boolean newValue ) {
		send_blockrequest_all_once = newValue;
	}
	
	private final StateMap executeRule(final StateMap changes) {

		StateMap result;

		StateMap changesAfterRule = null;
		String lastRuleExecuted = null;

		synchronized (rulesEngine) {

			try {
				changesAfterRule = rulesEngine.executeRule(changes);
				lastRuleExecuted = rulesEngine.getLastRuleExecuted();
			} catch (final RulesEngineException e) {
				UnitManager.Logging.logSevere("Exception Executing Rules.", e);
			}
		}

		if (systemStates) {
			if (lastRuleExecuted != null
					&& "On".equalsIgnoreCase(stateDatabase
							.getValue(stateTestMode))
					&& !lastRuleExecuted.equals(stateDatabase
							.getValue(stateLastRuleExecuted))) {

				processStateChangeRequest(getName(), stateLastRuleExecuted,
						lastRuleExecuted);
			}
		}

		if (changesAfterRule == null) {
			result = changes;
		} else {
			result = changesAfterRule;
		}

		return result;
	}

	private final void forceStateChange(final String requestor,
			final StateMap stateMap) {

		final StateChangeRequestMessage request = new StateChangeRequestMessage();
		request.nextId();
		request.setRequestor(requestor);
		request.setStateMap(stateMap);

		processMessageStateChangeRequest(request);
	}

	public final synchronized StateManagerMode getMode() {
		return mode;
	}

	private final String getStateChangeIncrement(final String counterStateName,
			final int increment) {

		int count = 1;
		int stateIndex;

		try {

			stateIndex = stateDatabase.getIndex(counterStateName);
			if (stateIndex != -1) {
				count = Integer.parseInt(stateDatabase.getValue(stateIndex));
			}

		} catch (final NumberFormatException e) {

			UnitManager.Logging.logSevere("State Manager '" + getName()
					+ "' could not format '" + counterStateName
					+ "'.  Setting count to 1.", e);
		}

		return String.valueOf(count + increment);
	}

	public final StateDatabase getStateDatabase() {
		return stateDatabase;
	}

	public final synchronized StateManagerStatus getStatus() {
		return status;
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		try {

			// CR ID RC-TC00013024
			// Reads the State Manger Multicast Group From LCP and Default Value
			// is Broadcast address 10.255.255.255
			String mcg = getSetting(SETTING_STATE_MANAGER_MULTICAST_GROUP,
					DEFAULT_BROADCAST_ADDRESS);

			if (mcg.length() > 0) {
				multicastGroup = InetAddress.getByName(mcg);
			}

		} catch (final UnknownHostException e) {

			UnitManager.Logging.logSevere(
					"Exception creating mulitcast Group for System Messaging Server '"
							+ toString() + "'", e);

		}

		initialElectionTime = getSetting(
				SETTING_STATE_MANAGER_INITIAL_ELECTION_TIME,
				initialElectionTime);
		serviceStartDelay = getSetting(
				SETTING_STATE_MANAGER_SERVICE_START_DELAY_TIME,
				serviceStartDelay);
		systemStates = getSetting(SETTING_STATE_MANAGER_SYSTEM_STATES,
				systemStates);
		electionPriority = getSetting(SETTING_STATE_MANAGER_ELECTION_PRIORITY,
				electionPriority);
		pendingRetryCount = getSetting(
				SETTING_STATE_MANAGER_PENDING_RETRY_COUNT, pendingRetryCount);
		pendingRetryTime = getSetting(SETTING_STATE_MANAGER_PENDING_RETRY_TIME,
				pendingRetryTime);
		pendingRetryTime = 10000; // CR-8998 remove after LCP update
		/*
		 * pendingQueueSize = getSetting(
		 * SETTING_STATE_MANAGER_PENDING_QUEUE_SIZE, pendingQueueSize);
		 * deferredQueueSize = getSetting(
		 * SETTING_STATE_MANAGER_DEFERRED_QUEUE_SIZE, deferredQueueSize);
		 */
		queuePriority = getSetting(SETTING_STATE_MANAGER_QUEUE_PRIORITY,
				queuePriority);
		queueTimeout = getSetting(SETTING_STATE_MANAGER_QUEUE_TIMEOUT,
				queueTimeout);
		electionTime = getSetting(SETTING_STATE_MANAGER_ELECTION_TIME,
				electionTime);
		databaseBroadcastTime = getSetting(
				SETTING_STATE_MANAGER_DATABASE_CRC_BROADCAST_TIME,
				databaseBroadcastTime);
		statusBroadcastTime = getSetting(
				SETTING_STATE_MANAGER_STATUS_BROADCAST_TIME,
				statusBroadcastTime);
		masterStatusBroadcastTime = getSetting(
				SETTING_STATE_MANAGER_MASTER_STATUS_BROADCAST_TIME,
				masterStatusBroadcastTime);
		electionRandomTime = getSetting(
				SETTING_STATE_MANAGER_ELECTION_RANDOM_TIME, electionRandomTime);
		rulesFilename = getSetting(SETTING_STATE_MANAGER_RULES_FILE,
				rulesFilename);
		synchronizeAgePadding = getSetting(
				SETTING_STATE_MANAGER_SYNCHRONIZE_AGE_PADDING,
				synchronizeAgePadding);
		stateActiveGsm = getSetting(SETTING_STATE_MANAGER_STATE_ACTIVE_GSM,
				stateActiveGsm);
		stateGsmRaceConditionCount = getSetting(
				SETTING_STATE_MANAGER_STATE_GSM_RACE_CONDITION_COUNT,
				stateGsmRaceConditionCount);
		stateLastRuleExecuted = getSetting(
				SETTING_STATE_MANAGER_STATE_LAST_RULE_EXECUTED,
				stateLastRuleExecuted);
		stateTestMode = getSetting(SETTING_STATE_MANAGER_STATE_TEST_MODE,
				stateTestMode);
		stateGsmCorruption = getSetting(
				SETTING_STATE_MANAGER_STATE_GSM_CORRUPTION, stateGsmCorruption);
		stateCrcMismatchBlockCount = getSetting(
				SETTING_STATE_MANAGER_STATE_CRC_MISMATCH_BLOCK_COUNT,
				stateCrcMismatchBlockCount);
		processRequestSize = getSetting(
				SETTING_STATE_MANAGER_PROCESS_REQUEST_SIZE, processRequestSize);
		stateRequestCacheSize = getSetting(
				SETTING_STATE_MANAGER_STATE_REQUEST_CACHE_SIZE,
				stateRequestCacheSize);
		processRetryThreshold = getSetting(
				SETTING_STATE_MANAGER_PROCESS_RETRY_THRESHOLD,
				processRetryThreshold);
		systemTimeout = getSetting(SETTING_STATE_MANAGER_SYSTEM_TIMEOUT,
				systemTimeout);
		loadingTimeout = getSetting(SETTING_STATE_MANAGER_LOADING_TIMEOUT,
				loadingTimeout);
		errorTimeout = getSetting(SETTING_STATE_MANAGER_ERROR_TIMEOUT,
				errorTimeout);
		databaseXmlFilename = getSetting(SETTING_STATE_MANAGER_DATABASE_FILE,
				databaseXmlFilename);
		databaseSerFilename = getSetting(
				SETTING_STATE_MANAGER_DATABASE_SERIAL_FILE, databaseSerFilename);
		databaseSize = getSetting(SETTING_STATE_MANAGER_DATABASE_SIZE,
				databaseSize);
		databaseGlobalPersistentName = getSetting(
				SETTING_STATE_MANAGER_DATABASE_GLOBAL_PERSISTENT_NAME,
				databaseGlobalPersistentName);
		databaseGlobalPersistentClass = getSetting(
				SETTING_STATE_MANAGER_DATABASE_GLOBAL_PERSISTENT_CLASS,
				databaseGlobalPersistentClass);
		/*
		 * retryDepth = getSetting( SETTING_STATE_MANAGER_RETRY_DEPTH,
		 * retryDepth);
		 */
		crcDepth = getSetting(SETTING_STATE_MANAGER_CRC_DEPTH, crcDepth);
		/*
		 * crcRequestDepth = getSetting(
		 * SETTING_STATE_MANAGER_CRC_REQUEST_DEPTH, crcRequestDepth);
		 */
		crcResponseDepth = getSetting(SETTING_STATE_MANAGER_CRC_RESPONSE_DEPTH,
				crcResponseDepth);
		loadPersistentFailTimeout = getSetting(
				SETTING_STATE_MANAGER_LOAD_PERSISTENT_FAIL_TIMEOUT,
				loadPersistentFailTimeout);

		if (crcResponseDepth > 0)
			throttle = true;

		stateRequestProcessTime = pendingRetryTime; // CR-8998

		long starttime = 0, stoptime = 0;

		stateDatabase.setSerDatabaseFile(new File(UnitManager.ObjectModel
				.getUnit().getStoreDirectory(), databaseSerFilename));

		boolean serLoadOk = false;

		if (!databaseXmlFilename.contains("default")) {
			try {
				UnitManager.Logging.logWarning("Start Database.ser load!");
				starttime = UnitManager.Timing.getTimeAlive();
				serLoadOk = loadDatabaseSer(stateDatabase.getSerDatabaseFile());
				stoptime = UnitManager.Timing.getTimeAlive();
				if (serLoadOk) {
					UnitManager.Logging.logEvent("State Database loaded from "
							+ stateDatabase.getSerDatabaseFile()
									.getAbsolutePath() + " in "
							+ (stoptime - starttime) + " msecs");
				}

			} catch (StateManagerException e) {
				UnitManager.Logging.logSevere("Exception in State Manager '"
						+ getName() + "' loading Database Ser File '"
						+ databaseSerFilename + "'", e);
			}
		}

		// load serialized database failed so load from XML
		if (!serLoadOk) {
			try {
				UnitManager.Logging.logWarning("Start Database.xml load!: "
						+ databaseXmlFilename);
				starttime = UnitManager.Timing.getTimeAlive();
				loadDatabase(new File(databaseXmlFilename));
				stoptime = UnitManager.Timing.getTimeAlive();
				UnitManager.Logging
						.logEvent("State Database loaded from Database.xml in "
								+ (stoptime - starttime) + " msecs");

			} catch (StateManagerException e) {
				UnitManager.Logging.logSevere("Exception in State Manager '"
						+ getName() + "' loading Database XML File '"
						+ databaseXmlFilename + "'", e);
			}
		}
		if (databaseXmlFilename.contains("default")) {
			delete(stateDatabase.getSerDatabaseFile());
		}
		/**
		 * Force crc calculation of each state. Includes name, type, value,
		 * rule, persistent. blockCrc[] not calculated. Used for LCP check.
		 */
		UnitManager.Logging.logWarning("Start Signature Calculation!");
		starttime = UnitManager.Timing.getTimeAlive();
		local_databaseSignature = stateDatabase.getDefaultDatabaseCrc(false);
		stoptime = UnitManager.Timing.getTimeAlive();
		UnitManager.Logging.logWarning("Database signature is: "
				+ local_databaseSignature);
		UnitManager.Logging.logWarning("Time taken to calculate Signature is "
				+ (stoptime - starttime) + " msecs");

		try {
			stateDatabase.setPersistentDataFile(new File(
					UnitManager.ObjectModel.getUnit().getStoreDirectory(),
					databaseGlobalPersistentName));

			if (databaseGlobalPersistentClass != null
					&& databaseGlobalPersistentClass.length() > 0) {
				stateDatabase
						.setPersistentData((PersistentData) UnitManager.Runtime
								.newInstance(databaseGlobalPersistentClass));
			}
		} catch (final ClassNotFoundException e) {
			UnitManager.Logging.logSevere("State Manager '" + getName()
					+ "' could not find Persistent Database Class '"
					+ databaseGlobalPersistentClass + "'", e);
		} catch (final InstantiationException e) {
			UnitManager.Logging.logSevere("State Manager '" + getName()
					+ "' could not create Persistent Database Class '"
					+ databaseGlobalPersistentClass + "'", e);
		} catch (final IllegalAccessException e) {
			UnitManager.Logging.logSevere("State Manager '" + getName()
					+ "' could not create Persistent Database Class '"
					+ databaseGlobalPersistentClass + "'", e);
		} finally {
			if (!stateDatabase.loadPersistentData()) {
				// if load had issues and likely resorted to default LCP values
				// then defer attempts to become Master so as not to possibly
				// overwrite system persistent data
				initialElectionTime = (initialElectionTime * loadPersistentFailTimeout);
				UnitManager.Logging
						.logWarning(UnitManager.ObjectModel.getUnit()
								.getInstanceName()
								+ " had problem loading persitent data so delaying initial Master election");
			}
		}

		try {
			UnitManager.Logging.logWarning("Start Rules load!");
			// rulesEngine.setSerRulesFile(new File("rules.ser"));
			starttime = UnitManager.Timing.getTimeAlive();
			loadRules(new File(rulesFilename));
			stoptime = UnitManager.Timing.getTimeAlive();
			UnitManager.Logging.logEvent("Rules loaded in "
					+ (stoptime - starttime) + " msecs");
		} catch (final StateManagerException e) {
			UnitManager.Logging.logSevere("Exception in State Manager '"
					+ toString() + "' loading Rules File '" + rulesFilename
					+ "'", e);
		}

		try {
			String smode = getSetting(SETTING_STATE_MANAGER_MODE, "");

			if (smode.length() > 0) {
				setMode(StateManagerMode.valueOf(smode));
			}
		} catch (final Exception e) {
			UnitManager.Logging.logSevere(
					"Could not determine State Manager Mode.  Using '" + mode
							+ "'", e);
			setMode(mode);
		}

		defaultMode = mode;

		/**
		 * Force crc calculation of each state. Includes value only. blockCrc[]
		 * calculated. Used for synchronizing.
		 */
		UnitManager.Logging.logInfo("Start DatabaseCrc calculation!");
		starttime = UnitManager.Timing.getTimeAlive();
		stateDatabase.getDatabaseCrcBlock(false);
		stoptime = UnitManager.Timing.getTimeAlive();
		UnitManager.Logging.logInfo("Time taken to calculate DatabaseCRC is "
				+ (stoptime - starttime) + " msecs");

		/*
		 * Attach Handlers
		 */
		starttime = UnitManager.Timing.getTimeAlive();
		for (Component component : getComponents()) {
			if (component instanceof Handler) {
				attachHandler((Handler) component);
			}
		}
		stoptime = UnitManager.Timing.getTimeAlive();
		UnitManager.Logging.logInfo("Time taken to attach handlers is "
				+ (stoptime - starttime) + " msecs");
	}

	private void delete(final File file) {
		if (file.exists()) {
			final boolean returnValue = file.delete();
			if (returnValue) {
				if (isInfo()) {
					logInfo("Deleted the file: " + file.getAbsolutePath());
				}
			} else {
				UnitManager.Logging.logSevere("Failed to delete the file: "
						+ file.getAbsolutePath());
			}
		} else {
			if (isInfo()) {
				logInfo("Deleting the file: " + file.getAbsolutePath()
						+ " failed as the file do not exists");
			}
		}
		// UpdateHandler updateHandler =
		// UnitManager.ObjectModel.getComponents().getFirstByClass(UpdateHandler.class);
		// updateHandler.flushFiles(file.getAbsolutePath(), true);
	}

	private final boolean isAlreadyDeferred(
			final StateChangeRequestMessage message) {
		boolean result = false;
		int len = deferredQueue.size();
		StateChangeRequestMessage deferredMessage;
		for (int i = 0; i < len; i++) {
			deferredMessage = deferredQueue.get(i);
			if (message.getStateMap().union(deferredMessage.getStateMap())
					.size() > 0) {
				result = true;
				deferredMessage.setConflict(true);
				// break;
			}
		}
		return result;
	}

	private final boolean handlePendingConflict(
			final StateChangeRequestMessage message) {

		boolean result = false;

		synchronized (pendingQueue) {

			int len = pendingQueue.size();
			StateChangeRequestMessage pendingMessage;

			for (int i = 0; i < len; i++) {
				pendingMessage = pendingQueue.get(i);

				if (message.getStateMap().union(pendingMessage.getStateMap())
						.size() > 0) {
					result = true;
					pendingMessage.setConflict(true);
					processDeferredStateChangeRequest(message);
					break;
				}
			}

			if (result == false) {
				processPendingStateChangeRequest(message);
			}

			return result;
		}
	}

	private final boolean isStillPendingConflict(
			final StateChangeRequestMessage message) {
		boolean result = false;
		synchronized (pendingQueue) {
			int len = pendingQueue.size();
			StateChangeRequestMessage pendingMessage;
			for (int i = 0; i < len; i++) {
				pendingMessage = pendingQueue.get(i);
				// if (pendingMessage.isConflict()) {
				if (message.getStateMap().union(pendingMessage.getStateMap())
						.size() > 0) {
					result = true;
					break;
				}
				// }
			}
		}
		return result;
	}

	private final boolean isRetryReady() {
		boolean ready = masterStateManagerAddress != null
				&& masterStatusMessage != null;
		// && (status == StateManagerStatus.SYNCHRONIZED || status ==
		// StateManagerStatus.MASTER)
		// && masterStatusMessage.getProcessQueueSize() < processRetryThreshold;
		// && queueProcessor.getQueueSize() < processRetryThreshold;

		if (UnitManager.Logging.isCore()) {
			if (!ready) {
				if (!retryNotReadyLogged) {
					retryNotReadyLogged = true;

					if (masterStatusMessage == null
							|| masterStateManagerAddress == null) {
						UnitManager.Logging
								.logCore("Retry Not Ready: No Master found");
					}

					/***
					 * if (currRequestQueueTime >=
					 * (UnitManager.Timing.getTimeAlive()-lastRetryTime)) {
					 * UnitManager.Logging.logCore(
					 * "Retry Not Ready: MASTER queue time too high: " +
					 * currRequestQueueTime); }
					 ***/
				}
			} else {
				retryNotReadyLogged = false;
			}
		}
		return ready;
	}

	public final void loadDatabase(final File databaseFile)
			throws StateManagerException {
		if (databaseFile != null && databaseFile.exists() && databaseSize > 0) {
			if (!stateDatabase.loadXml(databaseFile, databaseSize)) {
				send_blockrequest_all_once = true;
				throw new StateManagerException(
						"Error while loading Database XML '" + databaseFile
								+ "'");
			} else {
				send_blockrequest_all_once = false;
				fireDatabaseChanged();

				stateDatabase.saveDatabase();
			}
		} else {
			send_blockrequest_all_once = true;
			throw new StateManagerException(
					"Could not find Database XML File '" + databaseFile + "'");
		}
	}

	public final boolean loadDatabaseSer(final File databaseFile)
			throws StateManagerException {
		boolean result = false;

		if (databaseFile != null && databaseFile.exists() && databaseSize > 0) {
			result = stateDatabase.loadSerializedDatabase(databaseFile,
					databaseSize);
			if (!result) {
				send_blockrequest_all_once = true;
				throw new StateManagerException(
						"Error while loading Database Ser'" + databaseFile
								+ "'");
			} else {
				send_blockrequest_all_once = false;
				fireDatabaseChanged();
			}
		} else {
			send_blockrequest_all_once = true;
			// throw new
			// StateManagerException("Could not find Database Ser File '"
			// + databaseFile + "'");
			UnitManager.Logging
					.logWarning("Database serialized files doesn't exist");
		}
		return result;
	}

	public final void loadRules(final File rulesFile)
			throws StateManagerException {
		if (rulesFile.exists()) {
			try {
				rulesEngine.loadRules(rulesFile);
			} catch (RulesEngineException e) {
				throw new StateManagerException(
						"Rule's engine failed to load.", e);
			}
			fireRulesEngineChanged();
		} else {
			throw new StateManagerException("In Rule's Engine '" + toString()
					+ "', Could not find Rule's File '" + rulesFilename
					+ "'.  Using Emptry Rule's Collection.");
		}
	}

	protected final void masterElect() {

		masterStateManagerAddress = UnitManager.ObjectModel.getUnit()
				.getInetAddress();
		
		lastMasterId = 0;

		if (getStatus() != StateManagerStatus.MASTER
				&& getStatus() != StateManagerStatus.CANDIDATE) {
			// if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logInfo("State Manager '"
					+ UnitManager.ObjectModel.getUnit().toString()
					+ "' elected itself as CANDIDATE State Manager");
			// }

			setStatus(StateManagerStatus.CANDIDATE);

			candidateTimer.startTimer(candidateTime);
		} else if (getStatus() == StateManagerStatus.CANDIDATE) {
			// if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logInfo("State Manager '"
					+ UnitManager.ObjectModel.getUnit().toString()
					+ "' elected itself as MASTER State Manager");
			// }

			setStatus(StateManagerStatus.MASTER);
			master_databaseSignature = local_databaseSignature;

			databaseBroadcastTimer.startTimer(0);
		}
	}

	public final void masterReject() {

		StateManagerStatus stat = getStatus();

		// if (UnitManager.Logging.isCore()) {
		if (stat == StateManagerStatus.CANDIDATE) {
			UnitManager.Logging.logInfo("Rejected self '"
					+ UnitManager.ObjectModel.getUnit().toString()
					+ "' as CANDIDATE State Manager");
		} else if (stat == StateManagerStatus.MASTER) {
			UnitManager.Logging.logInfo("Rejected self '"
					+ UnitManager.ObjectModel.getUnit().toString()
					+ "' as MASTER State Manager");
		}
		// }

		electionTimer.startTimer(electionTime);

		setStatus(StateManagerStatus.SYNCHRONIZED);

		masterStateManagerAddress = null;
		lastMasterId = 0;
	}

	public final void notifyHandlerEvent(final HandlerEvent event,
			final Object tag, final Handler handler) {

		if (event == null) {
			UnitManager.Logging.logWarning("State Manager '" + getName()
					+ "' could not dispatch a null HandlerEvent with Tag '"
					+ tag + "' to Handler '" + handler + "'.  Skipping");
			return;
		}

		if (handler == null) {
			UnitManager.Logging.logWarning("State Manager '" + getName()
					+ "' could not dispatch Event '" + event
					+ "' to a null handler");
			return;
		}

		handler.queueHandlerEvent(event, tag);
	}

	public final void notifyHandlersEvent(final HandlerEvent event,
			final Object tag) {

		if (event == null) {
			UnitManager.Logging.logWarning("State Manager '" + getName()
					+ "' could not dispatch null event to Handlers.  Skipping");
			return;
		}

		synchronized (handlers) {
			int len = handlers.size();
			for (int i = 0; i < len; i++) {
				notifyHandlerEvent(event, tag, handlers.get(i));
			}
		}
	}

	private final void notifyHandlersStateChange(final StateMap stateMap) {

		if (stateMap != null) {

			synchronized (listenAll) {
				int len = listenAll.size();
				for (int i = 0; i < len; i++) {
					if (listenAll.get(i).getServiceState() == ServiceState.RUNNING) {
						listenAll.get(i).queueStatesChanged(stateMap);
					}
				}
			}

			synchronized (listenChoice) {

				HashMap<Handler, StateMap> delivery = new HashMap<Handler, StateMap>();

				for (final Entry<String, String> stateEntry : stateMap
						.entrySet()) {

					final ArrayList<Handler> lset = listenChoice.get(stateEntry
							.getKey());

					if (lset != null) {

						int len = lset.size();
						Handler handler;

						for (int i = 0; i < len; i++) {
							handler = lset.get(i);
							StateMap handlerStateMap = delivery.get(handler);

							if (handlerStateMap == null) {
								handlerStateMap = new StateMap();
								delivery.put(handler, handlerStateMap);
							}

							handlerStateMap.put(stateEntry.getKey(), stateEntry
									.getValue());
						}
					}
				}

				if (delivery != null && !delivery.isEmpty()) {
					for (final Entry<Handler, StateMap> entry : delivery
							.entrySet()) {
						if (entry.getKey().getServiceState() == ServiceState.RUNNING) {
							entry.getKey().queueStatesChanged(entry.getValue());
						}
					}
				}
			}
		}
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		setSetupPriority(3);

		lastRetryTime = UnitManager.Timing.getTimeAlive();

		random = new Random();

		groupPriority = new byte[numGroups];

		groupPriority[UnusedGroup] = UnusedPriority;
		groupPriority[PSWGroup] = PSWPriority;
		groupPriority[HDAV3000Group] = HDAV3000Priority;
		groupPriority[WAPGroup] = WAPPriority;
		groupPriority[BDP6002Group] = BDP3002Priority;
		groupPriority[TC6000Group] = TC6000Priority;
		groupPriority[MCDGroup] = MCDPriority;
		groupPriority[HDAV6000Group] = HDAV6000Priority;
		groupPriority[HTSEGroup] = HTSEPriority;
		groupPriority[BDUGroup] = BDUPriority;

		pendingQueueErrorPosting = true;
		deferredQueueErrorPosting = true;
		majorSigErrorPosting = true;

		stateDatabase = new StateDatabase();
		rulesEngine = new RulesEngine(stateDatabase);

		systemTimeoutTimer = new CountdownTimer();
		statusBroadcastTimer = new CountdownTimer();
		pendingQueueRetryTimer = new CountdownTimer(); // CR-8998
		deferredQueueRetryTimer = new CountdownTimer(); // CR-8998
		loadingStopWatch = new StopWatch();
		errorStopWatch = new StopWatch();

		persistentVote = new StateManagerVote();
		signatureVote = new StateManagerVote();
		messageCounter = new StateManagerMessageCounter(
				StateManagerMessage.NUM_TYPES);

		handlers = new ArrayList<Handler>();
		// stateChangeRequestCache = new StateManagerRequestMessageCache(
		// stateRequestCacheSize);
		pendingQueue = new ArrayList<StateChangeRequestMessage>();
		electionTimer = new CountdownTimer();
		candidateTimer = new CountdownTimer();
		serviceDelayTimer = new CountdownTimer();
		deferredQueue = new ArrayList<StateChangeRequestMessage>();
		databaseBroadcastTimer = new CountdownTimer();
		queueProcessor = new StateManagerQueueProcessor(
				"State Manager Queue Proccessor");

		listenAll = new ArrayList<Handler>();
		listenChoice = new HashMap<String, ArrayList<Handler>>();

		blockRequestMap = new StateManagerBlockRequestMap();

		databaseListeners = new ArrayList<DatabaseListener>();
		stateManagerListeners = new ArrayList<StateManagerListener>();
		rulesEngineListeners = new ArrayList<RulesEngineListener>();

		queueProcessor.addListener(this);
	}

	public final void fireDatabaseChanged() {
		synchronized (databaseListeners) {
			int len = databaseListeners.size();
			for (int i = 0; i < len; i++) {
				databaseListeners.get(i).databaseChanged(stateDatabase);
			}
		}
	}

	public final void fireRulesEngineChanged() {
		synchronized (rulesEngineListeners) {
			int len = rulesEngineListeners.size();
			for (int i = 0; i < len; i++) {
				rulesEngineListeners.get(i).rulesEngineChanged(rulesEngine);
			}
		}
	}

	public final boolean register(final DatabaseListener databaseListener) {
		synchronized (databaseListeners) {
			return databaseListeners.add(databaseListener);
		}
	}

	public final boolean register(final RulesEngineListener rulesEngineListener) {
		synchronized (rulesEngineListeners) {
			return rulesEngineListeners.add(rulesEngineListener);
		}
	}

	public final boolean register(
			final StateManagerListener stateManagerListener) {
		synchronized (stateManagerListeners) {
			return stateManagerListeners.add(stateManagerListener);
		}
	}

	public final boolean unregister(final DatabaseListener databaseListener) {
		synchronized (databaseListeners) {
			return databaseListeners.remove(databaseListener);
		}
	}

	public final boolean unregister(
			final RulesEngineListener rulesEngineListener) {
		synchronized (rulesEngineListeners) {
			return rulesEngineListeners.remove(rulesEngineListener);
		}
	}

	public final boolean unregister(
			final StateManagerListener stateManagerListener) {
		synchronized (stateManagerListeners) {
			return stateManagerListeners.remove(stateManagerListener);
		}
	}

	@Override
	protected void onReceivedDatagramPacket(final Object source,
			final MulticastServiceReceivedDatagramPacketArgs args) {

		super.onReceivedDatagramPacket(source, args);

		DatagramPacket packet = args.getDatagramPacket();

		try {
			final StateManagerMessage message = StateManagerMessage
					.create(new ByteArrayInputStream(packet.getData(), 0,
							packet.getLength()));
			/**
			 * message returns null is returned if the received message protocol
			 * version differs from VERSION specified in class
			 * StateManagerMessage
			 */
			if (message != null) {

				if (!message.isSender()) {
					systemTimeoutTimer.startTimer(systemTimeout);
					processMode();
				}

				if (getMode() == StateManagerMode.SYSTEM
						|| getMode() == StateManagerMode.PASSIVE
						|| message.isSender()) {

					if (UnitManager.Logging.isCore()) {
						if (message.getType() != StateManagerMessage.TYPE_STATUS) {
							UnitManager.Logging.logCore("Receive: " + message);
						}
					}

					StateManagerStatus stat = getStatus();

					switch (message.getType()) {

					case StateManagerMessage.TYPE_CONTROL:

						processMessageControl((ControlMessage) message);

						break;

					case StateManagerMessage.TYPE_STATUS:

						processMessageStatus((StatusMessage) message);

						break;

					case StateManagerMessage.TYPE_SYNCHRONIZE:

						if (!message.isSender()
								&& stat != StateManagerStatus.SYNCHRONIZED
								&& local_databaseSignature != 0
								&& local_databaseSignature == master_databaseSignature) {

							queueMessage(message);
						}

						break;

					case StateManagerMessage.TYPE_BLOCK_REQUEST:

						if (stat == StateManagerStatus.MASTER) {
							synchronized (blockRequestMap) {
								if (!blockRequestMap.containsKey(message
										.getSourceInetAddress())) {

									blockRequestMap.put(message
											.getSourceInetAddress(), message);
									UnitManager.Logging
											.logInfo("Block Request from "
													+ message
															.getSourceInetAddress()
													+ " accepted");
									queueMessage(message);
								} else {
									UnitManager.Logging
											.logInfo("Block Request from "
													+ message
															.getSourceInetAddress()
													+ " ignored");
								}
							}
						}
						break;

					case StateManagerMessage.TYPE_STATE_CHANGE_REQUEST:

						final StateChangeRequestMessage request = (StateChangeRequestMessage) message;

						if (stat == StateManagerStatus.MASTER) {
							// if this is a retry first see if it has already
							// been processed
							// don't check if request is from self since state
							// update was handled
							// on request and the response is ignored
							if (request.getRetry() > 0 && !message.isSender()) {
								if (UnitManager.Logging.isCore()) {
									UnitManager.Logging
											.logCore("RX Retry State Change Request: "
													+ request);
								}
								synchronized (stateChangeResponseCache) {
									if (stateChangeResponseCache
											.contains(request)) {

										final StateChangeResponseMessage response = (StateChangeResponseMessage) stateChangeResponseCache
												.get(request);

										if (UnitManager.Logging.isCore()) {
											UnitManager.Logging
													.logCore("Found cached state change response for request retry: "
															+ response);
										}
										sendStateChangeResponse(request
												.getSourceInetAddress(),
												response);

										break;
									} else {

										if (UnitManager.Logging.isCore()) {
											UnitManager.Logging
													.logCore("Duplicate state change request not processed yet: "
															+ request);
										}
									}
								}
							}

							synchronized (stateChangeRequestCache) {
								if (stateChangeRequestCache.contains(request)) {

									final StateChangeRequestMessage cached = (StateChangeRequestMessage) stateChangeRequestCache
											.get(request);

									incomingStateChangeDuplicates++;

									if (UnitManager.Logging.isCore()) {
										UnitManager.Logging
												.logCore("RX Duplicate State Change Request from: "
														+ request
																.getSourceInetAddress()
														+ " ID: "
														+ request.getId()
														+ " retry: "
														+ request.getRetry()
														+ " cached retry: "
														+ cached.getRetry());
									}

									sendStateChangeResponse(request
											.getSourceInetAddress(), request
											.getStateMap(), request, true);

								} else {
									if (messageCounter
											.get(StateManagerMessage.TYPE_STATE_CHANGE_REQUEST) < processRequestSize) {

										stateChangeRequestCache.put(request);
										request.setQueueTime(UnitManager.Timing
												.getTimeAlive());
										incomingStateChangeRequests++;
										queueMessage(request);

										if (incomingRequestLogging == false
												&& messageCounter
														.get(StateManagerMessage.TYPE_STATE_CHANGE_REQUEST) < (processRequestSize / 2)) {
											incomingRequestLogging = true;
											UnitManager.Logging
													.logWarning("Resume logging after "
															+ incomingRequestsDropped
															+ " state change requests dropped because incoming queue was full ");
											incomingRequestsDropped = 0;
										}
									} else {
										incomingRequestsDropped++;
										if (incomingRequestLogging) {
											UnitManager.Logging
													.logWarning("State Change Request dropped because incoming queue is full ");
											incomingRequestLogging = false;
										}
									}
								}
							}
						}
						break;

					case StateManagerMessage.TYPE_STATE_CHANGE_RESPONSE:
						// Processed by LSM
						// final StateChangeResponseMessage response =
						// (StateChangeResponseMessage) message;

						if (stat != StateManagerStatus.MASTER) {
							if (local_databaseSignature != 0
									&& local_databaseSignature == master_databaseSignature) {

								queueMessage(message);
							}
						}
						break;

					case StateManagerMessage.TYPE_BLOCK_CRC_REQUEST:
					case StateManagerMessage.TYPE_DATABASE_CRC_REQUEST:
						// Processed by GSM
						if (stat == StateManagerStatus.MASTER) {
							queueMessage(message);
						}
						break;

					case StateManagerMessage.TYPE_BLOCK_CRC:
						// Processed by LSM
						if (stat != StateManagerStatus.MASTER
								&& messageCounter
										.get(StateManagerMessage.TYPE_BLOCK_CRC) <= 0
								&& messageCounter
										.get(StateManagerMessage.TYPE_DATABASE_CRC) <= 0) {

							if (stat == StateManagerStatus.ONLINE) {
								setStatus(StateManagerStatus.LOADING);
							}

							queueMessage(message);
						}

						break;

					case StateManagerMessage.TYPE_DATABASE_CRC:
						// Processed by LSM
						if (stat != StateManagerStatus.MASTER
								&& messageCounter
										.get(StateManagerMessage.TYPE_BLOCK_CRC) <= 0
								&& messageCounter
										.get(StateManagerMessage.TYPE_SYNCHRONIZE) <= 0
								&& messageCounter
										.get(StateManagerMessage.TYPE_DATABASE_CRC) <= 0) {

							if (stat == StateManagerStatus.ONLINE) {
								setStatus(StateManagerStatus.LOADING);
							}

							queueMessage(message);
						}

						break;

					default:

						UnitManager.Logging.logSevere("State Manager '"
								+ getName()
								+ " received unknown message type: " + message);
						break;

					}

				} else {

					if (UnitManager.Logging.isCore()) {
						UnitManager.Logging.logCore("Ignored: " + message);
					}
				}

				processStatus();

			} else {
				UnitManager.Logging
						.logSevere("State Manager: Received message is wrong protocol version!");
			}

		} catch (final IOException e) {

			StringBuilder sb = new StringBuilder();

			sb.append("Unknown exception occurred in State Manager '"
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

	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);
		random = new Random(UnitManager.ObjectModel.getUnit().getRandomSeed()
				+ UnitManager.Timing.getTimeAlive());

		stateRequestCacheSize = processRequestSize * 2;
		stateChangeRequestCache = new StateManagerRequestMessageCache(
				stateRequestCacheSize);

		stateChangeResponseCache = new StateManagerResponseMessageCache(
				stateRequestCacheSize);

		setStatus(StateManagerStatus.ONLINE);

		statusBroadcastTimer.startTimer(0);
		databaseBroadcastTimer.startTimer(0);
		pendingQueueRetryTimer.startTimer(1000);
		deferredQueueRetryTimer.startTimer(deferredRetryTime);
		electionTimer.startTimer(initialElectionTime);
		serviceDelayTimer.startTimer(serviceStartDelay);
		systemTimeoutTimer.startTimer(systemTimeout);

		try {
			joinMulticastGroup(multicastGroup);
		} catch (final MulticastServiceSocketException e) {
			UnitManager.Logging.logSevere(
					"Exception occured while joining multicast group in State Manager '"
							+ getName() + "'", e);
		}

		try {
			queueProcessor.setTimeout(queueTimeout);
			queueProcessor.setPriority(queuePriority);
			queueProcessor.start();
		} catch (final QueueProcessorException e) {
			throw new ServiceStartException("State Manager '" + toString()
					+ "' Queue Processor did not start.", e);
		}
	}

	@Override
	protected void onStopped(Object source, ServiceStopArgs args)
			throws ServiceStopException {
		try {
			queueProcessor.stop();
		} catch (QueueProcessorException e) {
			throw new ServiceStopException("State Manager '" + getName()
					+ "' could not stop Queue Processor", e);
		}

		super.onStopped(source, args);
	}

	private final void processDeferredStateChangeRequest(
			final StateChangeRequestMessage message) {

		synchronized (deferredQueue) {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging
						.logCore("Add to Deferred Queue: " + message);
			}

			if (deferredQueue.size() + 1 < deferredQueueSize) {

				deferredQueueErrorPosting = true;
				// is this message a conflict with other items in deferred queue
				isAlreadyDeferred(message);
				deferredQueue.add(message);
			} else if (deferredQueueErrorPosting) {

				deferredQueueErrorPosting = false;
				UnitManager.Logging
						.logSevere("Deferred Queue is Full!  Ignoring incoming message:' "
								+ message.getStateMap());
			}
		}
	}

	private final void processMessage(final StateManagerMessage message) {

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				try {
					stateManagerListeners.get(i).stateManagerProcessMessage(
							this, message);
				} catch (Exception e) {
					UnitManager.Logging
							.logSevere(
									"State Manager '"
											+ UnitManager.ObjectModel.getUnit()
													.getInstanceName()
											+ "' has an Unknown Exception while calling a hook in stateManagerProcessMessage.",
									e);
				}
			}
		}

		/******************************
		 * if (status != StateManagerStatus.MASTER) { if
		 * (message.getSourceInetAddress().equals(masterStateManagerAddress)) {
		 * if (message.getId() < lastMasterId) {
		 * UnitManager.Logging.logSevere("LSM '" +
		 * UnitManager.ObjectModel.getUnit().getInstanceName() +
		 * "' received out of order message ID from Master: " +
		 * masterStateManagerAddress.getHostAddress() + " curr: " +
		 * message.getId() + ", last: " + Integer.valueOf((int)lastMasterId)); }
		 * lastMasterId = message.getId(); } }
		 ***********************************/

		switch (message.getType()) {

		case StateManagerMessage.TYPE_STATE_CHANGE_REQUEST:
			processMessageStateChangeRequest((StateChangeRequestMessage) message);
			break;

		case StateManagerMessage.TYPE_BLOCK_REQUEST:
			processMessageBlockRequest((BlockRequestMessage) message);
			break;

		case StateManagerMessage.TYPE_STATE_CHANGE_RESPONSE:
			processMessageStateChangeResponse((StateChangeResponseMessage) message);
			break;

		case StateManagerMessage.TYPE_BLOCK_CRC:
			processMessageBlockCrc((BlockCrcMessage) message);
			break;

		case StateManagerMessage.TYPE_BLOCK_CRC_REQUEST:
			processMessageBlockCrcRequest((BlockCrcRequestMessage) message);
			break;

		case StateManagerMessage.TYPE_SYNCHRONIZE:
			processMessageSynchronize((SynchronizeMessage) message);
			break;

		case StateManagerMessage.TYPE_DATABASE_CRC:
			processMessageDatabaseCrc((DatabaseCrcMessage) message);
			break;

		case StateManagerMessage.TYPE_DATABASE_CRC_REQUEST:
			processMessageDatabaseCrcRequest((DatabaseCrcRequestMessage) message);
			break;

		default:

			UnitManager.Logging.logWarning("State Manager '" + toString()
					+ "' did not understand Message Type '" + message.getType()
					+ "'.  Ignorning Message.");
			break;

		}

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				try {
					stateManagerListeners.get(i)
							.stateManagerProcessMessageComplete(this, message);
				} catch (Exception e) {
					UnitManager.Logging
							.logSevere(
									"State Manager '"
											+ getName()
											+ "' has an Unknown Exception while calling a hook in stateManagerProcessMessageComplete.",
									e);
				}
			}
		}
	}

	private final void processMessageDatabaseCrcRequest(
			final DatabaseCrcRequestMessage message) {

		if (getStatus() == StateManagerStatus.MASTER) {
			sendDatabaseCrc(message.getDestInetAddress());

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Total state change requests: "
						+ incomingStateChangeRequests + " total dups: "
						+ incomingStateChangeDuplicates);
			}
			// CR-8998
			// recalculate current state request processing time
			// this access is synchronized with processMessageStateChangeRequest
			if (stateRequestQueueCount > 0 && stateRequestQueueTime > 0) {
				stateRequestProcessTime = (int) (stateRequestQueueTime / stateRequestQueueCount);
				// lower limit is normal pending queue retry time
				if (stateRequestProcessTime == 0) {
					stateRequestProcessTime = 1000;
				}
				// upper limit since this must fit into a short (16-bit) field
				// in status message
				if (stateRequestProcessTime > 32767) {
					stateRequestProcessTime = 32767;
				}
				stateRequestQueueTime = 0;
				stateRequestQueueCount = 0;
			}
		}
	}

	private final void processMessageDatabaseCrc(
			final DatabaseCrcMessage message) {

		StateManagerStatus stat = getStatus();

		if (stat != StateManagerStatus.MASTER
				&& masterStateManagerAddress != null
				&& message.getSourceInetAddress().equals(
						masterStateManagerAddress)) {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("State Manager '"
						+ UnitManager.ObjectModel.getUnit().getInstanceName()
						+ "' Got DatabaseCRC to process");
			}

			boolean success = false;

			switch (message.getDatabaseCrcType()) {
			case DatabaseCrcMessage.CRC_TYPE_BLOCKS:
				success = message.getDatabaseCrc() == stateDatabase
						.getDatabaseCrcBlock(true);
				break;

			case DatabaseCrcMessage.CRC_TYPE_STATES:
				success = message.getDatabaseCrc() == stateDatabase
						.getDatabaseCrc(true);
				break;

			case DatabaseCrcMessage.CRC_TYPE_VALUES:
				success = message.getDatabaseCrc() == stateDatabase
						.getDatabaseCrcValue(true);
				break;
			}

			if (!success) {
				if (isInfo()) {
					logInfo("State Manager '"
							+ UnitManager.ObjectModel.getUnit()
									.getInstanceName() + "' Not Synchronized!"
							+ " Message DatabaseCrc = "
							+ message.getDatabaseCrc()
							+ " Local DatabaseCrcBlock = "
							+ stateDatabase.getDatabaseCrcBlock(true));
				}
				if (stat == StateManagerStatus.SYNCHRONIZED) {
					setStatus(StateManagerStatus.RECOVER);
				}
				sendBlockCrcRequest(masterStateManagerAddress);
			} else if (stat == StateManagerStatus.ONLINE
					|| stat == StateManagerStatus.LOADING
					|| stat == StateManagerStatus.RECOVER) {
				setStatus(StateManagerStatus.SYNCHRONIZED);
				if (isInfo()) {
					logInfo("State Manager '"
							+ UnitManager.ObjectModel.getUnit()
									.getInstanceName() + "' Synchronized!");
				}
			}
		}
	}

	/**
	 * There are two methods available for synchronizing the databases. The
	 * method is chosen based on the crcResponseDepth value.
	 * 
	 * crcResponseDepth = 0 Method 1: Multicast synchronize packets, no
	 * throttle. (default) GSM -> multicast DatabaseCRC (Short message
	 * containing one crc of all the blocks.) LSM/s -> unicast BlockCRCRequest
	 * (Request/s made if LSM/s need to synchronize.)
	 * 
	 * GSM only accepts the first BlockCRCRequest, typically during first
	 * synchronize (loading) all the LSMs are asking for the same blocks.
	 * 
	 * GSM -> unicast BlockCRC (Large message containing a crc for each block.
	 * Sent out to one requester.) LSM -> unicast BlockRequest (Medium size
	 * message depending on database differences) GSM -> multicast Synchronize
	 * (One block of 10 states sent to all LSMs). GSM -> multicast Synchronize
	 * (Next block of 10 states if needed) (Continue to send Synchronize packets
	 * until all sent ,no throttle.)
	 * 
	 * Successful synchronization is determined on the LSMs receipt of the next
	 * DatabaseCRC.
	 * 
	 * crcResponseDepth > 0 Method 2: Unicast synchronize packets, throttled.
	 * GSM -> multicast DatabaseCRC (Short message containing one crc of all the
	 * blocks.) LSM/s -> unicast BlockCRCRequest (Request/s made if LSM/s need
	 * to synchronize.) GSM -> unicast BlockCRC (Large message containing a crc
	 * for each block. Sent out to each requester.) LSM/s -> unicast
	 * BlockRequest (Medium size message depending on database differences and
	 * throttle limit set by crcRequestDepth. Sent out by each requester.) GSM
	 * -> unicast Synchronize (One block of 10 states sent to requesting LSM).
	 * GSM -> unicast Synchronize (Next block of 10 states if needed) (Continue
	 * to send Synchronize packets until throttle limit, set by
	 * crcResponseDepth) GSM -> unicast BlockCRC (Invoke LSM to make next
	 * BlockRequest)
	 * 
	 * The unicast BlockRequest, Synchronize and BlockCRC continue whils't the
	 * crcs don't match. Successful synchronization is determined on the LSM's
	 * receipt of the next DatabaseCRC. This process must be repeated for every
	 * requesting LSM.
	 * 
	 * @author kknight
	 */
	protected final void processMessageBlockRequest(
			final BlockRequestMessage message) {

		if (getStatus() == StateManagerStatus.MASTER) {

			IntArray blocks = message.getBlocks(); // fill array with block
			// indexes from LSM
			byte group = 0;
			int len = (crcResponseDepth == 0) ? blocks.length : Math.min(
					crcResponseDepth, blocks.length);

			InetAddress dstip = message.getSourceInetAddress();
			group = dstip.getAddress()[2];

			for (int i = 0; i < len; i++) {

				int block = blocks.values[i];
				if (stateDatabase.getBlockCount() <= block) {
					UnitManager.Logging
							.logSevere("State Manager '"
									+ getName()
									+ "' bad block index in processMessageBlockRequest().");
					return;
				}
				int indexes[] = stateDatabase.getIndexesByBlock(block);

				// Required to clean dirty blocks -- do not remove
				int cacheBlockCrc = stateDatabase.getBlockCrc(block, true);

				try {
					// TST has buffer to Queue the messages. So delay not
					// required.
					// If VS is used in system, longer delay of 25 is required.
					if (group != TSTtoolGroup) {
						Thread.sleep(10);
					}

					electionTimer.startTimer(electionTime);
					systemTimeoutTimer.startTimer(systemTimeout);
				} catch (Exception e) {
					UnitManager.Logging
							.logSevere(
									"State Manager '"
											+ getName()
											+ "' Exception while calling sleep in processMessageBlockRequest().",
									e);
				}

				if (throttle)
					sendSynchronize(indexes, dstip);
				else
					sendSynchronize(indexes, multicastGroup);

				processStatus();
			}

			if (throttle)
				sendBlockCrc(dstip);
		}
	}

	private final void processMessageControl(final ControlMessage message) {

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				stateManagerListeners.get(i).stateManagerProcessControlMessage(
						this, message);
			}
		}

		final boolean isTarget = message.isTarget(UnitManager.ObjectModel
				.getUnit().getInetAddress())
				|| message.isGlobal();

		switch (message.getCommand()) {
		case Bully:
			if (isTarget) {
				stateDatabase.setNbrOfUpdates(message.getBully()); // save new
				// 'number
				// of
				// updates'
			}
			break;
		case Elect:
			if (isTarget) {
				if (getStatus() != StateManagerStatus.MASTER
						&& getMode() != StateManagerMode.PASSIVE
						&& getStatus() != StateManagerStatus.ERROR) {
					masterElect();
				}
			} else if (getStatus() == StateManagerStatus.MASTER) {
				masterReject();
			}
			break;
		case ElectPriority:
			if (isTarget) {
				electionPriority = message.getElectionPriority();
			}
			break;
		case Reject:
			if (isTarget && getStatus() == StateManagerStatus.MASTER) {
				masterReject();
			}
			break;
		}
	}

	private final void processMessageBlockCrc(final BlockCrcMessage message) {

		StateManagerStatus stat = getStatus();

		if (stat != StateManagerStatus.MASTER
				&& masterStateManagerAddress != null
				&& message.getSourceInetAddress().equals(
						masterStateManagerAddress)
				&& message.getDatabaseSignature() == local_databaseSignature
				&& message.getBlockSize() == stateDatabase.getBlockSize()) {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("State Manager '"
						+ UnitManager.ObjectModel.getUnit().getInstanceName()
						+ "' Got BlockCRC to process");
			}

			int[] localCrcs = null;
			int[] remoteCrcs = message.getBlockCrcs();
			int[] remoteBlockIndexes = message.getBlockIndexes();
			int BlockCount = message.getBlockCount();
			final IntArray blockRequest = new IntArray(BlockCount);
			int ri;

			if (getMode() == StateManagerMode.PASSIVE
					&& stat == StateManagerStatus.LOADING
					&& send_blockrequest_all_once) {
				/**
				 * The VenueStatus Tool and TST Tool do not load a local copy of
				 * Database.xml. Consequently, they have no knowledge of the
				 * state names. The database is allocated on receipt of the
				 * BlockCrc message from the GSM. The number of indexes in
				 * BlockCrc x 10 is the size/nbr of records in the database.
				 * 
				 * Every block in the BlockCrc is requested because a block of
				 * states with a value of null at the GSM produces the same crc
				 * as an initialized block on the tool, consequently, the tool
				 * will not request that particular block. This is bad because
				 * the tool needs to populate every state name and index in its
				 * newly allocated database.
				 * 
				 * Each block is transferred from the GSM as a Sync packet. The
				 * Sync packet contains a fixed number of states (typically 10).
				 * Each state contains an index, name, value and associated crc.
				 * 
				 * State Responses are used to maintain sync. A State Response
				 * only contains a state's name and value. The name is used to
				 * locate the index for updating the value.
				 */

				for (int i = 0; i < BlockCount; i++) {
					ri = remoteBlockIndexes[i];
					blockRequest.add(ri);
				}

				/**
				 * Since this requests every sync packet there is a higher
				 * probability of missing one. This boolean prevents
				 * re-requesting everything.
				 */
				send_blockrequest_all_once = false;
				sendBlockRequest(masterStateManagerAddress, blockRequest);
			} else {

				/**
				 * count is used for throttling, it ensures that the
				 * blockRequest message contains crcRequestDepth number of
				 * blocks.
				 */
				int count = 0;

				localCrcs = stateDatabase.getBlockCrcs(remoteBlockIndexes, 0,
						BlockCount, true);

				if (localCrcs == null) {

					UnitManager.Logging
							.logSevere("In State Manager '"
									+ toString()
									+ "', could not get Crc Blocks in from State Database '"
									+ stateDatabase.toString()
									+ "'.  Skipping Crc Block Check.");

				} else {

					// loop through all of the blocks in the BlockCRC message
					for (int i = 0; i < BlockCount; i++) {

						long remoteCrc = remoteCrcs[i];
						long localCrc = localCrcs[i];
						ri = remoteBlockIndexes[i];

						if (crcRequestDepth != 0 && count > crcRequestDepth) {
							break; // Exit when crcRequestDepth number of
							// required blocks reached.
						}

						if (remoteCrc != localCrc) {

							count++;
							blockRequest.add(ri); // block crcs don't match,
							// make request for this
							// block.

							if (UnitManager.Logging.isCore()) {
								StringBuilder sb = new StringBuilder();
								sb.append("State Manager '");
								sb.append(getName());
								sb
										.append("' received an invalid CRC check in block '");
								sb.append(i);
								sb.append("'.  Local Crc='");
								sb.append(localCrc);
								sb.append("', Remote Crc='");
								sb.append(remoteCrc);
								sb.append("'.  States Included='");
								int[] indexes = stateDatabase
										.getIndexesByBlock(ri);
								int len = indexes.length;
								for (int j = 0; j < len; j++) {
									int index = indexes[j];
									sb.append(stateDatabase.toString(index));
								}
								sb.append("'");
								UnitManager.Logging.logCore(sb.toString());
							}
						}
					}

					if (blockRequest.length == 0) {
						/**
						 * Received BlockCrc and there is no mismatch! The local
						 * database must have synchronized via State Responses.
						 */
						setStatus(StateManagerStatus.SYNCHRONIZED);

					} else {
						if (UnitManager.Logging.isCore()) {
							UnitManager.Logging.logCore("State Manager '"
									+ UnitManager.ObjectModel.getUnit()
											.getInstanceName()
									+ "' requesting: " + blockRequest.length
									+ " Blocks");
						}

						sendBlockRequest(masterStateManagerAddress,
								blockRequest);

						if (stat == StateManagerStatus.SYNCHRONIZED) {
							if (systemStates) {
								final StateMap stateMap = new StateMap();
								stateMap.put(stateCrcMismatchBlockCount, String
										.valueOf(blockRequest.length));

								requestStateChange(getName(), stateMap);
							}

							setStatus(StateManagerStatus.RECOVER);
						}
					}
				}
			}
		}
	}

	protected final void processMessageBlockCrcRequest(
			final BlockCrcRequestMessage message) {

		if (getStatus() == StateManagerStatus.MASTER
				&& ignoreBlockCRCRequests == false) {
			// Only service the first LSM that responds (multicast sync)
			if (!throttle)
				ignoreBlockCRCRequests = true;
			sendBlockCrc(message.getDestInetAddress());
		}
	}

	protected final void processMessageStateChangeRequest(
			final StateChangeRequestMessage request) {

		if (getStatus() == StateManagerStatus.MASTER) {

			final StateMap stateMap = request.getStateMap();
			final StateMap rulesMap = executeRule(stateMap);

			/*** update my local database ***/
			updateStateDatabase(rulesMap);
			/*** tell all the local handlers about the state changes ***/
			notifyHandlersStateChange(rulesMap);
			/*** tell everyone else about the state changes ***/
			sendStateChangeResponse(multicastGroup, rulesMap, request, false);

			// CR-8998
			// calculate a running average of time required to process state
			// change requests
			// this is synchronized with processMessageDatabaseCrcRequest
			if (request.getQueueTime() > 0) {
				stateRequestQueueTime = stateRequestQueueTime
						+ (UnitManager.Timing.getTimeAlive() - request
								.getQueueTime());
				stateRequestQueueCount++;
			}
		}
	}

	// LSM processing only
	protected final void processMessageStateChangeResponse(
			final StateChangeResponseMessage message) {

		removePending(message);

		if (!message.isDuplicate()) {
			final StateMap stateMap = message.getStateMap();

			updateStateDatabase(stateMap);

			notifyHandlersStateChange(stateMap);
		}
	}

	protected final void processMessageSynchronize(
			final SynchronizeMessage message) {

		if (masterStateManagerAddress == null) {

			UnitManager.Logging
					.logWarning("Unable to Process Message State Synchronize because No Master State Manager is Found.");

		} else if (!message.isSender()) {

			int stateCount = message.getStateCount();

			for (int remoteIndex = 0; remoteIndex < stateCount; remoteIndex++) {

				int localIndex = message.getIndex(remoteIndex);

				if (localIndex < stateDatabase.getCapacity()
						&& localIndex != -1) {

					long localAge, remoteAge;
					long localCrc = 0, remoteCrc = 0;
					String localName, localValue, remoteName, remoteValue;
					boolean remoteOutdated, valueChanged, crcChanged;

					remoteName = message.getName(remoteIndex);
					remoteAge = message.getAge(remoteIndex);
					remoteValue = message.getValue(remoteIndex);
					remoteCrc = message.getCrc(remoteIndex);

					localAge = UnitManager.Timing.getTimeAlive()
							- stateDatabase.getUpdateTime(localIndex)
							+ synchronizeAgePadding;

					// Use Cases:
					// 1- both initialized (use most recent)
					if (stateDatabase.getUpdateTime(localIndex) != 0
							&& remoteAge != 0) {
						remoteOutdated = remoteAge > localAge;
					}
					// 2- neither GSM or LSM is initialized (use GSM)
					// 3- GSM initialized but not LSM (use GSM)
					else if (stateDatabase.getUpdateTime(localIndex) == 0) {
						remoteOutdated = false;
					}
					// 4- LSM initialized but not GSM (use LSM)
					else {
						remoteOutdated = true;
					}

					localCrc = stateDatabase.getValueCrc(localIndex, true);
					localValue = stateDatabase.getValue(localIndex);

					valueChanged = localValue == null
							|| !localValue.equals(remoteValue);
					crcChanged = localCrc != remoteCrc;

					localName = stateDatabase.getName(localIndex);

					/*
					 * State Manager State is younger than Master State Manager
					 * Send Sync Back to Master
					 */
					if (getStatus() != StateManagerStatus.MASTER && crcChanged
							&& remoteOutdated
							&& getMode() != StateManagerMode.PASSIVE) {

						if (UnitManager.Logging.isCore()) {
							UnitManager.Logging
									.logCore("State Manager '"
											+ UnitManager.ObjectModel.getUnit()
													.getInstanceName()
											+ "' synchronize request state is "
											+ (remoteAge - localAge)
											+ " msec older in MASTER.  Sending synchronize state back to MASTER '"
											+ message + "'");
						}

						sendSynchronize(new int[] { localIndex },
								masterStateManagerAddress);
						/**
						 * Allow value and age update on a state if:- 1)
						 * crcChanged -> The value has changed (normal path) 2)
						 * mode is PASSIVE, status is LOADING -> if TST or VS
						 * and this is the first attempt at synchronizing then
						 * accept this sync packet. This is used to acquire all
						 * the state names for populating the tool's GUI.
						 */
					} else if (crcChanged
							|| (getMode() == StateManagerMode.PASSIVE && getStatus() == StateManagerStatus.LOADING)) {

						if (UnitManager.Logging.isCore()) {
							UnitManager.Logging.logCore("State Manager '"
									+ getName() + "' Synchronizing '" + message
									+ "'");
						}

						if (UnitManager.Logging.isCore()) {
							UnitManager.Logging
									.logCore("State Manager '"
											+ UnitManager.ObjectModel.getUnit()
													.getInstanceName()
											+ "Updating state upon receiving sync message: Name: "
											+ localName + " remoteValue: "
											+ remoteValue + " localValue: "
											+ localValue);
						}

						// for the benefit of the Venue Status Application
						if ((getMode() == StateManagerMode.PASSIVE)
								&& (!remoteName.equals(localName))) {
							stateDatabase.setName(localIndex, remoteName);
						}

						/**
						 * Save the new state
						 */
						stateDatabase.setValue(localIndex, remoteValue);

						if (databaseListeners.size() > 0) {
							fireDatabaseUpdateState(localIndex);
						}

						if (getStatus() == StateManagerStatus.MASTER) {

							if (UnitManager.Logging.isCore()) {
								UnitManager.Logging.logCore("State Manager '"
										+ UnitManager.ObjectModel.getUnit()
												.getInstanceName()
										+ "' MASTER RE-Synchronizing index: '"
										+ localIndex + "'");
							}

							// TODO: run the associated rule as well?

							sendSynchronize(new int[] { localIndex },
									multicastGroup);

						}

						if (valueChanged) {

							if (UnitManager.Logging.isCore()) {
								UnitManager.Logging
										.logCore("State Manager '"
												+ getName()
												+ "' notifying Handlers of State Synchronize '"
												+ message + "'");
							}

							final StateMap changes = new StateMap();
							changes.put(stateDatabase.getName(localIndex),
									stateDatabase.getValue(localIndex));
							notifyHandlersStateChange(changes);
						}
					}
				}
			}
		}
	}

	private final void fireDatabaseUpdateState(final int stateIndex) {
		synchronized (databaseListeners) {
			int len = databaseListeners.size();
			for (int i = 0; i < len; i++) {
				databaseListeners.get(i)
						.stateChanged(stateDatabase, stateIndex);
			}
		}
	}

	private final void processMessageStatus(final StatusMessage statusMessage) {

		InetAddress statusMessageIP = statusMessage.getSourceInetAddress();
		byte msggroup = statusMessageIP.getAddress()[2];
		StateManagerStatus ourStat = getStatus();
		StateManagerStatus msgStat = statusMessage.getStatus();

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				stateManagerListeners.get(i).stateManagerProcessStatusMessage(
						this, statusMessage);
			}
		}

		// Message from Master or Candidate (could be from self)
		if (msgStat == StateManagerStatus.MASTER
		 || msgStat == StateManagerStatus.CANDIDATE) {
			
			boolean concede = false;

			// reset timer if status from MASTER or CANDIDATE
			electionTimer.startTimer(electionTime);

			// if our status is master or candidate
			if (ourStat == StateManagerStatus.MASTER
			 || ourStat == StateManagerStatus.CANDIDATE) {
				
				// if message is from ourself
				if (statusMessage.isSender()) {
					// if we are master (not candidate) and
					// message is from a master (not candidate)
					if (ourStat == StateManagerStatus.MASTER
					 && msgStat == StateManagerStatus.MASTER) { 
						
						processChangedMaster(statusMessage);
						
						if (systemStates) {
							final String activeGsmIp = stateDatabase
									.getValue(stateActiveGsm);
	
							if (activeGsmIp == null
								|| !activeGsmIp.equals(UnitManager.ObjectModel
										.getUnit().getIpAddress())) {
	
								final StateMap stateMap = new StateMap();
	
								stateMap.put(stateActiveGsm, UnitManager.ObjectModel
										.getUnit().getIpAddress());
	
								forceStateChange(getName(), stateMap);
							}
						}
					}
				} else { // message from another contender
					// if (getMode() != StateManagerMode.LOCAL)
					concede = processMessageStatusConflict(statusMessage);
					
					// if we were master and conceded then save new master info
					if (concede) {
						processChangedMaster(statusMessage);
					}
				}
			} else { // we are a LSM then save new master info
				processChangedMaster(statusMessage);
			}
			
		} else if (msgStat == StateManagerStatus.ONLINE
					&& msggroup != TSTtoolGroup) {

			// Message from Online unit
			if (ourStat == StateManagerStatus.MASTER) {
				sendStatusMessage(statusMessageIP);
				sendDatabaseCrc(statusMessageIP);
			}

			int pvote = statusMessage.getPersistentDataCrc();

			if (pvote != 0) {
				persistentVote.vote(statusMessageIP, pvote);

				if (UnitManager.Logging.isCore()) {
					UnitManager.Logging.logCore("Persistent Vote: "
							+ persistentVote);
				}
			}

			synchronized (stateChangeRequestCache) {
				stateChangeRequestCache.flushIp(statusMessageIP);
			}
			synchronized (stateChangeResponseCache) {
				stateChangeResponseCache.flushIp(statusMessageIP);
			}
		}

		/**
		 * One vote is cast for each state manager that is running. The vote is
		 * never closed to capture late arrivals. Therefore, the majority is
		 * always known. The only short coming is during the following
		 * scenario:- 1) PSWs 1 - 16 are powered up, 1 - 15 have database
		 * signature #1, 16 has database signature #2. 2) PSWs 1 - 15 are in the
		 * majority and are allowed to participate. PSW 16 is in the minority
		 * and is forced after Error timeout to become isolated in Local mode.
		 * 3) PSWs 17 - 32 are powered up, 17 - 32 have database signature #2.
		 * Now PSW 16, 17 - 32 are in the majority. PSWs 1 - 15 are forced after
		 * Error timeout to become isolated in Local mode. PSWs 17 - 32 are
		 * allowed to participate. PSW 16 should participate but can not return
		 * from Local mode. 4) The update service forces PSWs 1 -15 to database
		 * signature #2 and resets them.
		 * 
		 * PSW 16 remains isolated in Local mode until it is power cycled!!!!
		 * 
		 * This will not happen if the Error timeout is long enough so that PSW
		 * 16 does not enter Local mode before PSWs 17 - 32 are running.
		 * 
		 * @author kknight
		 * 
		 */

		/**
		 * In LOCAL mode, the state manager is processing its own status
		 * messages. Consequently, if forced to LOCAL mode & ONLINE status via
		 * an ERROR status timeout we do not want to change the status back to
		 * ERROR.
		 * 
		 */
		if (getMode() != StateManagerMode.LOCAL
				&& getMode() != StateManagerMode.PASSIVE
				&& msggroup != TSTtoolGroup) {

			int svote = statusMessage.getSignatureCrc();

			if (svote != 0) {
				// Update system majority
				signatureVote.vote(statusMessageIP, svote);

				// our local database is in the system majority.
				if (signatureVote.getMajorityCrc() == local_databaseSignature) {
					
					// Previously, local database was in the minority
					if (ourStat == StateManagerStatus.ERROR) { 
						ourStat = StateManagerStatus.ONLINE;
						/**
						 * If existing status is ERROR then stop Error Timeout,
						 * the state manager's databaseSignature is now in the
						 * majority!
						 */
						// replace existing status: ERROR with new status: ONLINE
						setStatus(ourStat); 
					} else {
						// UnitManager.Logging.logInfo("Normal processMessageStatus() path");
					}
				} else { // we are in the minority
					if (majorSigErrorPosting) {
						// The ERROR log message will continue to be displayed.
						UnitManager.Logging
								.logSevere(" ERROR: majority signature = "
										+ signatureVote.getMajorityCrc()
										+ " local signature = "
										+ local_databaseSignature);
						majorSigErrorPosting = false;
					}
					/**
					 * Within setStatus() the following logic is applied:
					 * If existing status is not ERROR then start Error Timeout.
					 * If existing status is ERROR then allow Error Timeout to
					 * continue.
					 * 
					 * Note: If currently Master or Candidate then going to
					 * status == ERROR is a forced step down.
					 */
					// replaces existing status with new status: ERROR
					setStatus(StateManagerStatus.ERROR); 
				}
			}
		}

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				stateManagerListeners.get(i)
						.stateManagerProcessStatusMessageComplete(this,
								statusMessage);
			}
		}
	}

	private final void processChangedMaster(final StatusMessage statusMessage) {
		InetAddress statusMessageIP = statusMessage.getSourceInetAddress();

		// save address only if actually from master (can be from self)
		if (statusMessage.getStatus() == StateManagerStatus.MASTER) {
			if (masterStatusMessage == null
					|| !statusMessageIP.equals(masterStateManagerAddress)) {
				masterStatusMessage = statusMessage;
				masterStateManagerAddress = statusMessageIP;
				lastMasterId = statusMessage.getId();
				master_databaseSignature = statusMessage.getSignatureCrc();
				// Master has changed so clear any requests marked duplicate
				// and retry to new master
				cleanRetryPendingQueue();
			}
			currRequestQueueTime = statusMessage.getProcessQueueSize();
		}
	}
	

	/*
	 * Contention between two self-elected masters, two self-elected candidates
	 * or a self-elected candidate and a self-elected master are resolved in the
	 * following order:
	 * 
	 * 1) Election Priority (LCP configurable -- but usually all have same) 
	 * 2) Number of Updates (how current -- NOTE: this used to be 'bully')
	 * 3) Group membership (third octet in IP address: BDU, HTSE, MCD, HDAV or PSW)
	 * 4) Unit number (forth octet in IP address: 1 -> 32)
	 */
	private final boolean processMessageStatusConflict(final StatusMessage message) {

		final int remoteElectionPriority = message.getElectionPriority();
		InetAddress statusMessageIP = message.getSourceInetAddress();
		InetAddress masterAddress = null;
		byte remoteGroup = statusMessageIP.getAddress()[2];
		byte remoteUnit = statusMessageIP.getAddress()[3];
		byte localGroup = (byte) UnitManager.ObjectModel.getUnit()
				.getUnitGroup();
		byte localUnit = (byte) UnitManager.ObjectModel.getUnit()
				.getUnitNumber();
		int NbrOfDatabaseUpdates = stateDatabase.getNbrOfUpdates();
		boolean concede = true;

		if (remoteElectionPriority == electionPriority) {

			if (message.getBully() > NbrOfDatabaseUpdates) {

				logInfo("Election results: '"
						+ UnitManager.ObjectModel.getUnit().getInstanceName()
						+ "' Rejected self because remote updates = "
						+ message.getBully()
						+ " is greater than local updates = "
						+ NbrOfDatabaseUpdates);

				concede = true;

			} else if (message.getBully() == NbrOfDatabaseUpdates) {
				/*
				 * Prioritization scheme
				 * 
				 * Groups: BDU (highest) -> TC6000 (lowest)
				 * 
				 * Units: 32 (highest) -> 1 (lowest)
				 */
				concede = resolvePriority(getPriority(localGroup),
						getPriority(remoteGroup), localUnit, remoteUnit);

				if (concede) {
					logInfo("Election results: '"
							+ UnitManager.ObjectModel.getUnit()
									.getInstanceName()
							+ "' Rejected self based on local updates = remote updates = "
							+ NbrOfDatabaseUpdates + " local group = "
							+ localGroup + " remote group = " + remoteGroup
							+ " local unit is " + localUnit + " remote unit is"
							+ remoteUnit);
				}
			} else {
				UnitManager.Logging.logInfo("Election results: '"
						+ UnitManager.ObjectModel.getUnit().getInstanceName()
						+ "' Won because remote updates = "
						+ message.getBully() + " is less than local updates = "
						+ NbrOfDatabaseUpdates);
				
				concede = false;
			}

		} else if (electionPriority < remoteElectionPriority) {

			UnitManager.Logging.logWarning("Election results: '"
					+ UnitManager.ObjectModel.getUnit().getInstanceName()
					+ "' Rejected self based on Priority loss. " + message);
			
			concede = true;
		} else {
			UnitManager.Logging.logWarning("Election results: '"
					+ UnitManager.ObjectModel.getUnit().getInstanceName()
					+ "' Won based on Priority " + message);
			
			concede = false;
		}

		if (concede) {
			masterReject();
			
			// unnecessary as this is already done in masterReject()
			electionTimer.startTimer(electionTime); 
			
			// send GsmRaceCondition request to new master
			masterAddress = statusMessageIP;
		} else {
			// send GsmRaceCondition request to old master (self)
			masterAddress = masterStateManagerAddress;
		}
		
		// send GsmRaceCondition request to appropriate master
		if (systemStates) {
			sendStateChangeIncrementRequest(masterAddress,
					getName(), stateGsmRaceConditionCount, 1);
		}

		return concede;
	}

	private final byte getPriority(byte group) {
		if (group < numGroups) {
			return groupPriority[group];
		} else {
			return UnusedPriority;
		}
	}

	private final boolean resolvePriority(byte localPriority,
			byte remotePriority, byte localUnit, byte remoteUnit) {
		boolean concede;

		if (remotePriority > localPriority) {
			concede = true;
		} else if (remotePriority == localPriority && remoteUnit > localUnit) {
			concede = true;
		} else {
			concede = false;
		}
		return concede;
	}

	private final void processMode() {

		switch (getMode()) {

		case SYSTEM:
			if ((getStatus() == StateManagerStatus.SYNCHRONIZED || getStatus() == StateManagerStatus.MASTER)
					&& systemTimeoutTimer.hasExpired()) {
				UnitManager.Logging.logWarning("SYSTEM -> SINGLE Mode "
						+ UnitManager.ObjectModel.getUnit().getInstanceName());
				setMode(StateManagerMode.SINGLE);
			}
			break;

		case SINGLE:
			if (!systemTimeoutTimer.hasExpired()) {
				setMode(StateManagerMode.SYSTEM);
				// reset election timer to prevent disrupting system
				if (getStatus() == StateManagerStatus.MASTER) {
					UnitManager.Logging
							.logWarning("SINGLE -> SYSTEM Rejected State Manager '"
									+ toString()
									+ "' in Unit '"
									+ UnitManager.ObjectModel.getUnit()
											.toString()
									+ "' as Master State Manager");

					masterReject();
				} else {
					electionTimer.startTimer(electionTime);
				}
			}
			break;
		}
	}

	private final void processPendingStateChangeRequest(
			final StateChangeRequestMessage message) {

		synchronized (pendingQueue) {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Pending: '" + message);
			}

			if (pendingQueue.size() + 1 < pendingQueueSize) {

				pendingQueueErrorPosting = true;
				message.setRetryTime(UnitManager.Timing.getTimeAlive());
				pendingQueue.add(message);

			} else if (pendingQueueErrorPosting) {

				pendingQueueErrorPosting = false;
				UnitManager.Logging
						.logSevere("State Manager '"
								+ UnitManager.ObjectModel.getUnit()
										.getInstanceName()
								+ "' pending queue is full!  Ignoring incoming messages'");
			}
		}

		try {

			if (masterStateManagerAddress == null) {
				if (!noStateManagerLogged) {
					UnitManager.Logging
							.logWarning("State Manager '"
									+ getName()
									+ " could not find Master State Manager.  Keeping message on Pending Queue.  Request Message = '"
									+ message + "'");
					noStateManagerLogged = true;
				} else {
					if (isInfo()) {
						logInfo("State Manager '"
								+ getName()
								+ " could not find Master State Manager.  Keeping message on Pending Queue.  Request Message = '"
								+ message + "'");
					}
				}
			} else {
				message.setSendTime(UnitManager.Timing.getTimeAlive());
				send(message, masterStateManagerAddress);
				stateChangeRequests++;
				noStateManagerLogged = false;
			}

		} catch (final Exception e) {

			UnitManager.Logging.logSevere("State Manager '" + getName()
					+ "' exception sending message '" + message
					+ "'.  Keeping message on Pending Queue");

		}
	}

	/*** Handler calls this method on its thread, synchronization not required! ***/
	private final void processStateChangeRequest(final String requestor,
			final StateMap stateMap) {

		final StateChangeRequestMessage message = new StateChangeRequestMessage();
		message.nextId();
		message.setRequestor(requestor);
		message.setStateMap(stateMap);

		handlePendingConflict(message);
	}

	private final void processStateChangeRequest(final String requestor,
			final String state, final String value) {

		final StateMap stateMap = new StateMap();
		stateMap.put(state, value);

		processStateChangeRequest(requestor, stateMap);
	}

	private final void processStatus() {

		if (statusBroadcastTimer.hasExpired()) {
			sendStatusMessage(multicastGroup);
		}

		if (getStatus() == StateManagerStatus.LOADING 
				// && getMode() != StateManagerMode.PASSIVE  // never timeout LOADING if PASSIVE?
				&& loadingTimeout != 0
				&& loadingStopWatch.getElapsed() > loadingTimeout 
				&& loadingStopWatch.isRunning()) {			
			loadingStopWatch.stop();
			UnitManager.Logging.logWarning("'"
					+ UnitManager.ObjectModel.getUnit().getInstanceName()
					+ "'Synchronize with GSM failed after '"
					+ loadingStopWatch.getElapsed() + "' ms");
			/**
			 * After synchronization timeout the synchronized state is forced to
			 * allow services to start. On service startup the PSW provides a
			 * GUI to allow a USB key software update.
			 * 
			 * There is a chance that SYNCHRONIZED gets set to RECOVER if a
			 * DatabaseCRC is received before the serviceDelayTimer expires!
			 * Services are allowed to start if status is RECOVER.
			 * 
			 * @author kknight
			 * 
			 */
			setStatus(StateManagerStatus.SYNCHRONIZED);
		}

		if (getStatus() == StateManagerStatus.ERROR && errorTimeout != 0
				&& errorStopWatch.getElapsed() > errorTimeout
				&& errorStopWatch.isRunning()) {
			errorStopWatch.stop();
			UnitManager.Logging
					.logSevere("'"
							+ UnitManager.ObjectModel.getUnit()
									.getInstanceName()
							+ "'StateManager went to LOCAL mode after being in an ERROR state for '"
							+ errorStopWatch.getElapsed() + "' ms");
			setStatus(StateManagerStatus.ONLINE);
			setMode(StateManagerMode.LOCAL);
		}
	}

	// only called by QueueProcessor thread
	private final void processTimers() {

		StateManagerStatus stat = getStatus();
		StateManagerMode cmode = getMode();

		if (stat == StateManagerStatus.MASTER) {

			if (databaseBroadcastTimer.hasExpired()
					&& messageCounter
							.get(StateManagerMessage.TYPE_BLOCK_CRC_REQUEST) <= 0
					&& cmode == StateManagerMode.SYSTEM) {
				queueMessage(new DatabaseCrcRequestMessage(multicastGroup));
				databaseBroadcastTimer.startTimer(databaseBroadcastTime);
			}
		} else if (stat == StateManagerStatus.CANDIDATE) {
			if (candidateTimer.hasExpired()) // candidate timer is started after
				// electionTimer expiration or
				// control elect.
				masterElect(); // transition to MASTER
		} else {
			if (electionTimer.hasExpired()
					&& cmode != StateManagerMode.PASSIVE // PASSIVE mode is for
					// tools.
					&& stat != StateManagerStatus.ERROR // The local database
					// does not match the
					// system majority
					// database.
					&& (stat == StateManagerStatus.SYNCHRONIZED || // Persistent
					// data
					// majority
					// is
					// irrelevant
					// when
					// SYNCHRONIZED
					persistentVote.getMajorityCrc() == stateDatabase
							.getPersistentData().getCrc())
					&& signatureVote.getMajorityCrc() == local_databaseSignature
					|| cmode == StateManagerMode.LOCAL
					|| cmode == StateManagerMode.SINGLE) {
				masterElect(); // transition to CANDIDATE
			}
		}

		if (serviceDelayTimer.hasExpired() && serviceDelayFlag == false) {
			if (stat == StateManagerStatus.MASTER
					|| stat == StateManagerStatus.SYNCHRONIZED
					|| stat == StateManagerStatus.RECOVER) { // See comments
				// concerning
				// RECOVER in
				// processStatus()
				UnitManager.Logging.logWarning("StateManager '"
						+ UnitManager.ObjectModel.getUnit().getInstanceName()
						+ "' starting Services");

				Service[] services = UnitManager.ObjectModel.getComponents()
						.getServices();
				Service service;
				int len = services.length;
				for (int i = 0; i < len; i++) {
					service = services[i];
					if (service.getStartupType() == ServiceStartupType.SYNCHRONIZED
							&& service.getServiceState() == ServiceState.STOPPED) {
						try {
							service.start();

							if (UnitManager.Logging.isCore()) {
								UnitManager.Logging
										.logCore("StateManager '"
												+ UnitManager.ObjectModel
														.getUnit()
														.getInstanceName()
												+ "' synchronized and started Service '"
												+ service.getName() + "'");
							}
						} catch (final ServiceStartException e) {
							UnitManager.Logging.logSevere("StateManager '"
									+ UnitManager.ObjectModel.getUnit()
											.getInstanceName()
									+ "' failed to autostart Service '"
									+ service.getName() + "'", e);
						}
					}
				}
				notifyHandlersEvent(HandlerEvent.DATABASE_SYNCHRONIZED, null);
				serviceDelayFlag = true;
			}
		}

		processStatus();
		processMode();

		if (pendingQueueRetryTimer.hasExpired()) { // CR-8998
			retryPendingQueue();
			pendingQueueRetryTimer.restart();
		}
	}

	private final void queueMessage(final StateManagerMessage message) {

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Queued: " + message);
		}
		if (queueProcessor.isRunning()) {
			messageCounter.add(message.getType(), 1);
			queueProcessor.addQueue(message);
		} else {
			UnitManager.Logging.logSevere("State Manager '"
					+ UnitManager.ObjectModel.getUnit().getInstanceName()
					+ "'Queue Processor Stopped");
		}
	}

	public final void queueProcessorItemReceived(
			final QueueProcessor<StateManagerMessage> queue,
			final StateManagerMessage message) {

		if (queue == null) {
			UnitManager.Logging
					.logWarning("State Manager '"
							+ UnitManager.ObjectModel.getUnit()
									.getInstanceName()
							+ "' queue processor is null in queueProcessorItemReceived.  Skipping.");
			return;
		}

		if (message == null) {
			UnitManager.Logging.logWarning("State Manager '"
					+ UnitManager.ObjectModel.getUnit().getInstanceName()
					+ "' message received from processor is null.  Skipping");
			return;
		}

		try {

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging
						.logCore("Process Message START: " + message);
			}

			processMessage(message);

			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore("Process Message STOP: " + message);
			}

			processTimers();

		} catch (final Exception e) {

			UnitManager.Logging.logSevere(
					"Unknown excetion occured in State Manager '" + getName()
							+ "' in queueProcessorItemRecevied", e);

		} finally {

			messageCounter.sub(message.getType(), 1);

			if (message.getType() == StateManagerMessage.TYPE_BLOCK_REQUEST) {
				synchronized (blockRequestMap) {
					blockRequestMap.removeKey(message.getSourceInetAddress());
				}
			}

			if (queueProcessor.getQueueSize() == 0) {
				if (messageCounter.total() != 0) {
					messageCounter.reset();
				}
				synchronized (blockRequestMap) {
					if (blockRequestMap.size() != 0) {
						blockRequestMap.clear();
					}
				}
			}
		}
	}

	public final void queueProcessorTimeout(
			final QueueProcessor<StateManagerMessage> processor) {

		if (processor == null) {
			UnitManager.Logging
					.logWarning("State Manager '"
							+ getName()
							+ "' queue processor is null in queueProcessorTimeout  Skipping.");
			return;
		}

		try {

			processTimers();

		} catch (final Exception e) {

			UnitManager.Logging.logSevere(
					"Unknown exception occured in State Manager '" + getName()
							+ "' in queueProcessorTimeout.", e);
		}
	}

	public final void attachHandler(final Handler handler) {

		if (handler == null) {
			UnitManager.Logging.logWarning("State Manager '" + getName()
					+ "' could not attached null Handler.  Skipping");
			return;
		}

		synchronized (handlers) {

			handlers.add(handler);

			handler.setStateManager(this);

			switch (handler.getListenMode()) {

			case ALL:

				synchronized (listenAll) {
					listenAll.add(handler);
				}

			case CHOICE:

				synchronized (listenChoice) {

					/*** propertie's stateName with listen="true" ***/
					ArrayList<String> listenerStates = handler
							.getListenStates();
					String state;
					int len = listenerStates.size();

					for (int i = 0; i < len; i++) {
						state = listenerStates.get(i);

						ArrayList<Handler> list = listenChoice.get(state);

						if (list == null) {

							list = new ArrayList<Handler>();
							listenChoice.put(state, list);
							list.add(handler);

						} else if (!list.contains(handler)) {
							list.add(handler);
						}
					}
				}

				break;

			case NONE:

				break;

			default:

				break;
			}
		}
	}

	private final void removePending(final StateChangeResponseMessage response) {

		boolean removed = false;
		boolean conflict = false;

		if (response.getRequestIp().equals(
				UnitManager.ObjectModel.getUnit().getInetAddress())) {

			synchronized (pendingQueue) {

				final Iterator<StateChangeRequestMessage> itr = pendingQueue
						.iterator();

				while (itr.hasNext()) {

					final StateChangeRequestMessage request = itr.next();

					if (response.getRequestId() == request.getId()) {

						if (UnitManager.Logging.isCore()) {
							UnitManager.Logging.logCore("Removed Pending: "
									+ request);
						}

						if (response.isDuplicate()) {
							if (UnitManager.Logging.isCore()) {
								UnitManager.Logging
										.logCore("Pending duplicate received: "
												+ request);
							}
							/***
							 * Currently an inefficient work around. Might mark
							 * requests on the pending queue as duplicate
							 * requests in the future, and only process
							 * duplicate requests on the pending queue if the
							 * master changes
							 */
							request.setDuplicate(true);
							request.setRetryTime(UnitManager.Timing
									.getTimeAlive()
									+ (pendingRetryTime * 2));
						} else {
							if (UnitManager.Logging.isCore()) {
								UnitManager.Logging
										.logCore("QueueProcessor removed from Pending Queue: "
												+ request);
							}
							// CR-8998
							// determine how long it took to get this response
							// and use to throttle future pending retries
							if (request.getSendTime() > 0) {
								stateRequestResponseTime = stateRequestResponseTime
										+ (UnitManager.Timing.getTimeAlive() - request
												.getSendTime());
								stateRequestResponseCount++;
								stateRequestResponseAvg = (int) (stateRequestResponseTime / stateRequestResponseCount);
							}

							itr.remove();
							removed = true;
							if (request.isConflict()) {
								conflict = true;
							}
							break;
						}
					}
				}

				if (conflict
						|| (pendingQueue.size() == 0 && deferredQueue.size() > 0)
						|| deferredQueueRetryTimer.hasExpired()) { // CR-8998

					if ((pendingQueue.size() == 0 && deferredQueue.size() > 0)
							&& !conflict) {
						UnitManager.Logging
								.logWarning("Remove Pending Queue empty but deferred queue not! "
										+ deferredQueue.size()); // CR-8998
					}

					retryDeferredQueue();

					deferredQueueRetryTimer.restart(); // CR-8998
				}
			} // sync(pendingQueue)
		} // isRetryReady
	}

	public final void requestStateChange(final String requestor,
			final StateMap stateMap) {

		if (stateMap == null) {

			UnitManager.Logging.logWarning("A State Change Request from '"
					+ requestor
					+ "' attempted a null StateMap request.  Skipping.");

		} else if (requestor == null) {

			UnitManager.Logging.logWarning("A State Change Request '"
					+ stateMap
					+ "' was sent from an unknown source (null).  Skipping.");

		} else {

			processStateChangeRequest(requestor, stateMap);
		}
	}

	private final void retryDeferredQueue() {

		if (isRetryReady()) {

			synchronized (deferredQueue) {

				final Iterator<StateChangeRequestMessage> itr = deferredQueue
						.iterator();

				while (itr.hasNext()) {

					final StateChangeRequestMessage request = itr.next();

					if (!isStillPendingConflict(request)) {

						if (UnitManager.Logging.isCore()) {
							UnitManager.Logging.logCore("Remove Deferred: "
									+ request);
						}

						itr.remove();

						processPendingStateChangeRequest(request);
					}
				}
			}
		}
	}

	private final void cleanRetryPendingQueue() {
		synchronized (pendingQueue) {

			final Iterator<StateChangeRequestMessage> itr = pendingQueue
					.iterator();

			while (itr.hasNext()) {
				final StateChangeRequestMessage request = itr.next();

				if (request.isDuplicate()) {
					request.setDuplicate(false);
				}
			}

			// and retry the Pending queue to new master
			retryPendingQueue();
		}
	}

	private final void retryPendingQueue() {
		boolean removed = false;
		boolean conflict = false;
		int count = 0;

		if (isRetryReady()) {

			synchronized (pendingQueue) {

				final Iterator<StateChangeRequestMessage> itr = pendingQueue
						.iterator();

				while (itr.hasNext()) {

					if (retryDepth != 0 && count > retryDepth) {
						break;
					}

					final StateChangeRequestMessage request = itr.next();

					if ((UnitManager.Timing.getTimeAlive() - request
							.getRetryTime()) > pendingRetryTime) {

						if (pendingRetryCount != 0
								&& request.getRetry() >= pendingRetryCount) {

							// if (UnitManager.Logging.isCore()) {
							UnitManager.Logging
									.logWarning("Retry FAILED.  Removing: '"
											+ request + "'");
							// }

							itr.remove();
							removed = true;
							if (request.isConflict()) {
								conflict = true;
							}

						} else {

							request.setRetry(request.getRetry() + 1);
							request.setRetryTime(UnitManager.Timing
									.getTimeAlive());
							try {
								if (UnitManager.Logging.isCore()) {
									UnitManager.Logging.logCore("Retry "
											+ request.getRetry() + " of "
											+ pendingRetryCount + ": '"
											+ request + "'");
								}

								send(request, masterStateManagerAddress);
								pendingMessageRetryCount++;
								count++;

							} catch (final Exception e) {
								UnitManager.Logging.logSevere(
										"Could not send Message '" + request
												+ "' in State Manager '"
												+ toString() + "'", e);
							}
						}
					}
				}

				if (count > 0) {
					lastRetryTime = UnitManager.Timing.getTimeAlive();

					if (isDebug()) {
						logDebug("Pending Retry of " + count + " messages"
								+ " Pending Q size: " + pendingQueue.size()
								+ " Deferred Q size: " + deferredQueue.size()
								+ " Pending msg retries: "
								+ pendingMessageRetryCount
								+ " State change requests: "
								+ stateChangeRequests);
					}
				}

				if (conflict
						|| (pendingQueue.size() == 0 && deferredQueue.size() > 0)
						|| deferredQueueRetryTimer.hasExpired()) { // CR-8998
					// if (isCore()) {
					UnitManager.Logging
							.logCore("Removed pending message. Retry limit reached "
									+ " Pending Q size: "
									+ pendingQueue.size()
									+ " Deferred Q size: "
									+ deferredQueue.size());
					// }
					if ((pendingQueue.size() == 0 && deferredQueue.size() > 0)
							&& !conflict) {
						UnitManager.Logging
								.logWarning("retry Pending Queue empty but deferred queue not! "
										+ deferredQueue.size()); // CR-8998
					}

					if (deferredQueueRetryTimer.hasExpired()) {
						if (stateRequestResponseAvg > 0) {
							if (stateRequestResponseAvg < pendingRetryTime) {
								pendingRetryTime -= 1000;
								if (pendingRetryTime < 1000) {
									pendingRetryTime = 1000;
								}
							} else if (stateRequestResponseAvg >= pendingRetryTime
									&& stateRequestResponseCount > 0) {
								pendingRetryTime = stateRequestResponseAvg;
								if (pendingRetryTime > 15000) {
									pendingRetryTime = 15000;
								}
							}
						}

						if (stateRequestResponseCount > 0
								|| pendingRetryTime != 1000) {
							if (UnitManager.Logging.isCore()) {
								UnitManager.Logging
										.logCore("Avg state response time: "
												+ stateRequestResponseAvg
												+ " based on: "
												+ stateRequestResponseCount
												+ " samples - "
												+ "pending retry time: "
												+ pendingRetryTime);
							}
						}
						stateRequestResponseTime = 0;
						stateRequestResponseCount = 0;

						if (UnitManager.Logging.isProfile()) {
							UnitManager.Logging.logProfile("SM profiling: "
									+ " Status: "
									+ getStatus()
									+ " Mode: "
									+ getMode()
									+ " In queue depth: "
									+ queueProcessor.getQueueSize()
									+ " Requests received: "
									+ incomingStateChangeRequests
									+ " Requests processed: "
									+ stateChangeRequests
									+ " Duplicate requests: "
									+ incomingStateChangeDuplicates
									+ " Request process time: "
									+ stateRequestProcessTime
									+ " Response process time: "
									+ stateRequestResponseAvg
									+ " Master's request queue time: "
									+ currRequestQueueTime
									+ " Pending queue depth: "
									+ pendingQueue.size()
									+ " Deferred queue depth: "
									+ deferredQueue.size()
									+ " Pending retry time: "
									+ pendingRetryTime);
						}

						deferredQueueRetryTimer.restart();
					}

					retryDeferredQueue();
				}
			} // sync(pendingQueue)
		} // isRetryReady
	}

	public final void send(final StateManagerMessage message)
			throws ServiceIOException {
		send(message, multicastGroup);
	}

	public final void send(final StateManagerMessage message,
			final InetAddress inetAddress) throws ServiceIOException {

		if (message == null) {
			UnitManager.Logging.logWarning("State Manager '" + getName()
					+ "' could not send null message.  Skipping.");
			return;
		}

		if (inetAddress == null) {
			UnitManager.Logging.logWarning("State Manager '" + getName()
					+ "' could not send message '" + message
					+ "' to null destination.  Skipping.");
			return;
		}

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				stateManagerListeners.get(i).stateManagerSendMessage(this,
						message);
			}
		}

		switch (getMode()) {

		case SYSTEM:
		case PASSIVE:

			if (UnitManager.Logging.isCore()) {
				if (message.getType() != StateManagerMessage.TYPE_STATUS) {
					UnitManager.Logging.logCore("Send: " + inetAddress + ":"
							+ getPort() + ": " + message);
				}
			}

			super.send(message.toBytes(), inetAddress, getPort());

			break;

		case LOCAL:
		case SINGLE:

			if (UnitManager.Logging.isCore()) {
				if (message.getType() != StateManagerMessage.TYPE_STATUS) {
					UnitManager.Logging.logCore("Send: "
							+ UnitManager.ObjectModel.getUnit() + ":"
							+ getPort() + ": " + message);
				}
			}

			super.send(message.toBytes(), UnitManager.ObjectModel.getUnit()
					.getInetAddress(), getPort());

			break;
		}
	}

	private final void sendBlockCrcRequest(final InetAddress address) {

		BlockCrcRequestMessage message = new BlockCrcRequestMessage();
		message.nextId();
		message.setDestInetAddress(UnitManager.ObjectModel.getUnit()
				.getInetAddress());

		try {
			send(message, address);
		} catch (final ServiceIOException e) {
			UnitManager.Logging
					.logSevere("State Manager '" + toString()
							+ "' could not send Block Crc Request '" + message
							+ "'", e);
		}
	}

	private void sendDatabaseCrcRequest(final InetAddress inetAddress) {

		final DatabaseCrcRequestMessage message = new DatabaseCrcRequestMessage();
		message.nextId();
		message.setDestInetAddress(UnitManager.ObjectModel.getUnit()
				.getInetAddress());

		try {
			send(message, inetAddress);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("State Manager '" + toString()
					+ "' could not send Database Request '" + message + "'", e);
		}
	}

	private final void sendBlockRequest(final InetAddress inetAddress,
			final IntArray blocks) {

		final BlockRequestMessage message = new BlockRequestMessage();
		message.nextId();
		message.setBlocks(blocks);

		try {
			send(message, inetAddress);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("State Manager '" + toString()
					+ "' could not send Block Request '" + message + "'", e);
		}
	}

	public final void sendStateChangeIncrementRequest(
			final InetAddress inetAddress, final String requestor,
			final String counterStateName, final int incremenet) {

		processStateChangeRequest(requestor, counterStateName,
				getStateChangeIncrement(counterStateName, incremenet));
	}

	private final void sendStateChangeResponse(final InetAddress inetAddress,
			final StateMap response, final StateChangeRequestMessage request,
			final boolean duplicate) {

		try {

			final StateChangeResponseMessage message = new StateChangeResponseMessage();
			message.nextId();
			message.setRequestId(request.getId());
			message.setRequestor(request.getRequestor());
			message.setRequestIp(request.getSourceInetAddress());
			message.setStateMap(response);
			message.setDuplicate(duplicate);

			// put it into the response cache
			stateChangeResponseCache.put(message);

			send(message, inetAddress);

			if (!duplicate) {
				removePending(message);
			}

		} catch (final ServiceIOException e) {

			UnitManager.Logging.logSevere("State Manager '" + toString()
					+ "' could not send 'State Change' message", e);
		}
	}

	private final void sendStateChangeResponse(final InetAddress inetAddress,
			final StateChangeResponseMessage message) {

		try {

			send(message, inetAddress);

		} catch (final ServiceIOException e) {

			UnitManager.Logging.logSevere("State Manager '" + toString()
					+ "' could not send 'State Change' message", e);
		}
	}

	private final void sendStatusMessage(final InetAddress inetAddress) {

		StateManagerStatus stat = getStatus();
		final StatusMessage message = new StatusMessage();
		message.nextId();
		message.setBully(stateDatabase.getNbrOfUpdates());
		message.setElectionPriority(electionPriority);
		// message.setProcessQueueSize(queueProcessor.getQueueSize());
		if (stat == StateManagerStatus.MASTER) {
			message.setProcessQueueSize(stateRequestProcessTime); // CR-8998
		} else {
			short requestTime;
			if (stateRequestResponseAvg > 32767) {
				requestTime = 32767;
			} else {
				requestTime = (short) stateRequestResponseAvg;
			}
			message.setProcessQueueSize(requestTime); // CR-8998
		}

		message.setStatus(stat);

		int crc = (getMode() == StateManagerMode.PASSIVE) ? 0 : stateDatabase
				.getPersistentData().getCrc();

		message.setPersistentDataCrc(crc);
		message.setMajorityPersistentData(crc == persistentVote
				.getMajorityCrc());
		message.setSignatureCrc(local_databaseSignature);

		try {
			send(message, inetAddress);
		} catch (final ServiceIOException e) {
			UnitManager.Logging.logSevere("State Manager '"
					+ UnitManager.ObjectModel.getUnit().getInstanceName()
					+ "' errored sending Status Message '" + message + "'", e);
		}

		if (inetAddress.equals(multicastGroup)) {
			if (stat == StateManagerStatus.MASTER
					|| stat == StateManagerStatus.CANDIDATE) {
				statusBroadcastTimer.startTimer(masterStatusBroadcastTime);
			} else {
				statusBroadcastTimer.startTimer(statusBroadcastTime);
			}
		}
	}

	private final void sendDatabaseCrc(final InetAddress inetAddress) {

		if (getStatus() == StateManagerStatus.MASTER) {

			DatabaseCrcMessage message = new DatabaseCrcMessage();
			message.nextId();
			message.setDatabaseCrcType(DatabaseCrcMessage.CRC_TYPE_BLOCKS);
			message.setDatabaseCrc(stateDatabase.getDatabaseCrcBlock(true));
			message.setDatabaseSignature(local_databaseSignature);

			try {
				send(message, inetAddress);
				ignoreBlockCRCRequests = false; // Allow receipt of
				// BlockCRCRequests from LSMs
			} catch (final ServiceIOException e) {
				UnitManager.Logging.logSevere("State Manager '"
						+ UnitManager.ObjectModel.getUnit().getInstanceName()
						+ "' could not send Database Crc '" + message + "'", e);
			}

			databaseBroadcastTimer.startTimer(databaseBroadcastTime);
		}
	}

	/**
	 * The number of block crcs sent if limited by the crcDepth field.
	 * 
	 * @param inetAddress
	 */
	private final void sendBlockCrc(final InetAddress inetAddress) {

		if (getStatus() == StateManagerStatus.MASTER) {

			BlockCrcMessage message = new BlockCrcMessage();
			int blockCount = stateDatabase.getBlockCount();
			int count = (crcDepth == 0) ? blockCount : Math.min(crcDepth,
					blockCount);
			message.nextId();
			message.setDatabaseSize(stateDatabase.getSize());
			message.createBlockCrcs(count);
			message.setDatabaseSignature(local_databaseSignature);
			message.setBlockSize(stateDatabase.getBlockSize());
			for (int i = 0; i < count; i++) {
				message.addBlockCrc(crcIndex, stateDatabase.getBlockCrc(
						crcIndex, true));
				crcIndex = (crcIndex + 1) % blockCount;
			}

			try {
				send(message, inetAddress);
			} catch (final ServiceIOException e) {
				UnitManager.Logging.logSevere("State Manager '"
						+ UnitManager.ObjectModel.getUnit().getInstanceName()
						+ "' could not send Block Crc '" + message + "'", e);
			}

			databaseBroadcastTimer.startTimer(databaseBroadcastTime);
		}
	}

	private final void sendSynchronize(final int[] indexes,
			final InetAddress inetAddress) {

		try {
			int count = indexes.length; // blocksize
			if (indexes.length > 0) {
				final SynchronizeMessage message = new SynchronizeMessage();
				message.nextId();
				message.createStates(count);
				for (int i = 0; i < count; i++) {
					int index = indexes[i];
					message.setIndex(i, index);
					message.setName(i, stateDatabase.getName(index));
					if (stateDatabase.getUpdateTime(index) != 0) {
						/*
						 * Offset Age by 1mS to ensure Age = 0mS is only used to
						 * indicate non-updated data
						 */
						message
								.setAge(
										i,
										(UnitManager.Timing.getTimeAlive() - stateDatabase
												.getUpdateTime(index)) + 1);
					} else {
						message.setAge(i, 0); // indicate non-updated data
					}
					message.setValue(i, stateDatabase.getValue(index));
					message.setCrc(i, stateDatabase.getValueCrc(index, true));
				}

				send(message, inetAddress);
			}
		} catch (final Exception e) {

			UnitManager.Logging.logSevere("State Manager '" + toString()
					+ "' could not send 'State Synchronize' message", e);
		}
	}

	public final void setElectionPriority(final int electionPriority) {
		this.electionPriority = electionPriority;
	}

	public final synchronized void setMode(final StateManagerMode mode) {
		/* Remove this logic, since when we coming out of single mode we will reject
		 * ourself as master
		 * if (mode == StateManagerMode.SINGLE) {
			electionPriority = 1;
		}*/

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Mode Changed from " + this.mode
					+ " to " + mode);
		}

		this.mode = mode;

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				stateManagerListeners.get(i)
						.stateManagerModeChanged(this, mode);
			}
		}
	}

	public final void setMulticastGroup(final InetAddress multicastGroup) {
		this.multicastGroup = multicastGroup;
	}

	public final void setQueueTimeout(final int queueTimeout) {
		this.queueTimeout = queueTimeout;
	}

	private final synchronized void setStatus(
			final StateManagerStatus new_status) {

		final boolean statusChanged = this.status != new_status;

		if (new_status == StateManagerStatus.LOADING) {
			if (this.status != StateManagerStatus.LOADING) {
				loadingStopWatch.reset();
			}
		} else if (loadingStopWatch.isRunning()) {
			loadingStopWatch.stop();
		}

		/**
		 * new | saved | errorStopWatch | errorStopWatch status | status |
		 * running? | action
		 * ------------------------------------------------------ ERROR | ERROR
		 * | Don't Care | Do Nothing ERROR |!ERROR | Don't Care | Reset/Start
		 * !ERROR | Don't Care | True | Stop !ERROR | Don't Care | False | Do
		 * Nothing
		 */

		if (new_status == StateManagerStatus.ERROR) {
			if (this.status != StateManagerStatus.ERROR) {
				errorStopWatch.reset();

				if (UnitManager.Logging.isCore()) {
					UnitManager.Logging.logCore(" RESET ERROR ELAPSED TIMER "
							+ UnitManager.ObjectModel.getUnit()
									.getInstanceName());
				}
			}
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore(" CONTINUE ERROR ELAPSED TIMER "
						+ UnitManager.ObjectModel.getUnit().getInstanceName());
			}
		} else if (errorStopWatch.isRunning()) {
			errorStopWatch.stop();
			if (UnitManager.Logging.isCore()) {
				UnitManager.Logging.logCore(" STOP ERROR ELAPSED TIMER "
						+ UnitManager.ObjectModel.getUnit().getInstanceName());
			}
		}

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Status Changed from " + this.status
					+ " to " + new_status);
		}

		this.status = new_status;

		synchronized (stateManagerListeners) {
			int len = stateManagerListeners.size();
			for (int i = 0; i < len; i++) {
				stateManagerListeners.get(i).stateManagerStatusChanged(this,
						new_status);
			}
		}

		if (getMode() == StateManagerMode.SYSTEM
				&& (new_status == StateManagerStatus.MASTER || new_status == StateManagerStatus.SYNCHRONIZED)) {
			systemTimeoutTimer.startTimer(systemTimeout);
		}

		if (getServiceState() == ServiceState.RUNNING && statusChanged) {
			sendStatusMessage(multicastGroup); // There is a change in status,
			// transmit status message now!
		}

	}

	public final void setPostSystemStates(final boolean postSystemStates) {
		this.systemStates = postSystemStates;
	}

	public final boolean detachHandler(final Handler handler) {

		if (handler == null) {
			UnitManager.Logging.logWarning("State Manager '" + getName()
					+ "' could not detach null Handler.");
			return false;
		}

		boolean success = false;

		synchronized (handlers) {

			handler.setStateManager(null);
			success = handlers.remove(handler);

			switch (handler.getListenMode()) {

			case ALL:

				synchronized (listenAll) {
					listenAll.remove(handler);
				}

			case CHOICE:

				synchronized (listenChoice) {

					ArrayList<String> listenStates = handler.getListenStates();
					String state;
					int len = listenStates.size();

					for (int i = 0; i < len; i++) {
						state = listenStates.get(i);

						ArrayList<Handler> list = listenChoice.get(state);

						if (list != null && list.contains(handler)) {
							list.remove(handler);
						}
					}
				}

				break;

			case NONE:

				break;

			default:

				break;
			}
		}

		return success;
	}

	private final void updateStateDatabase(final StateMap changes) {

		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging.logCore("Update Database: " + changes);
		}

		synchronized (stateDatabase) {

			for (final Entry<String, String> entry : changes.entrySet()) {
				int index = stateDatabase.getIndex(entry.getKey());
				if (index != -1) {
					stateDatabase.setValue(index, entry.getValue());
					if (databaseListeners.size() > 0) {
						fireDatabaseUpdateState(index);
					}
				} else if (getMode() == StateManagerMode.PASSIVE) {
					setStatus(StateManagerStatus.LOADING);
				}
			}
		}
	}

	public final InetAddress getMulticastGroup() {
		return multicastGroup;
	}

	public final void setDatabaseSignature(final int databaseSignature) {
		this.local_databaseSignature = databaseSignature;
	}

	public final int getDatabaseSignature() {
		return local_databaseSignature;
	}

	public final StateManagerMode getDefaultMode() {
		return defaultMode;
	}

	public final double getLastRuleExecTime() {
		return lastRuleExecTime;
	}

	public final StateManagerQueueProcessor getQueueProcessor() {
		return queueProcessor;
	}
}
