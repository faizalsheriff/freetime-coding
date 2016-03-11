package com.rockwellcollins.cs.hcms.core.profiling;

//import java.beans.Statement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.DefaultUnitManagerLoader;
import com.rockwellcollins.cs.hcms.core.Unit;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessor;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessorException;
import com.rockwellcollins.cs.hcms.core.collections.ThreadProcessorListener;
import com.rockwellcollins.cs.hcms.core.profiling.tests.CPUTest;
import com.rockwellcollins.cs.hcms.core.profiling.tests.ExampleTest;
import com.rockwellcollins.cs.hcms.core.profiling.tests.SystemStateChangeTest;
import com.rockwellcollins.cs.hcms.core.profiling.tests.StateChangeTest;
import com.rockwellcollins.cs.hcms.core.profiling.tests.DatabaseCRCTest;
import com.rockwellcollins.cs.hcms.core.profiling.tests.StateManagerStatusTest;
import com.rockwellcollins.cs.hcms.core.profiling.tests.StateManagerQueueTest;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartupType;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangedArgs;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyMap;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyMapChangedArgs;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;

public class ProfileHandler extends Handler {

	private static final long serialVersionUID = 1L;

	public static final String SETTING_TESTS = "profile handler tests";

	private static final String SETTING_TEST_PROPERTY_ENABLE_NAME = "test property enable name";
	private static final String SETTING_TEST_PROPERTY_RESPONSE_NAME = "test property response name";
	private static final String SETTING_PROFILE_MAX_WAIT_TIME = "profile handler default wait time";
	private static final String SETTING_PROFILE_VERSION_FILENAME = "profile handler version filename";
	private static final String SETTING_PROFILE_LOCAL_PORT = "profile handler local port";
	private static final String SETTING_PROFILE_SERVER_PORT = "profile handler server port";
	private static final String SETTING_PROFILE_SERVER_HOST = "profile handler server host";
	private static final String SETTING_PROFILE_UNIT_TYPE = "profile handler unit type";
	private static final String SETTING_PROFILE_BUFFER_SIZE = "profile handler buffer size";

	private final ArrayList<ProfileTest> profileTests = new ArrayList<ProfileTest>();
	private String unitPartNumber = "Unknown Part Number";
	private String unitBuildNumber = "Unknown Build Number";
	private String unitType = "Unknown Type";
	private transient ThreadProcessor processor;
	private long waitTime;
	private long maxWaitTime = 1000;
	private String versionFilename = "/usr/local/HCMS/app/version.txt";
	private transient DatagramSocket socket;
	private int port = 55055;
	private String serverHost = "localhost";
	private int serverPort = 44444;
	private transient DatagramPacket packet;
	private byte buffer[];
	private int bufferSize = 256;
	private String TestPropertyResponseName = "ProfileTestResp";
	private String TestPropertyEnableName = "ProfileTestEnable";
	private boolean SystemStateChangeTestEnable = false;
	private int myAddress = 0;


	public static void main(String[] args) throws Exception {

		/** Setup Handler **/
		ProfileHandler handler = new ProfileHandler();
		handler.setSetting(SETTING_PROFILE_VERSION_FILENAME, System
				.getenv("HOME")
				+ "/version.txt");
		handler.setSetting(SETTING_PROFILE_UNIT_TYPE, "PC");
		handler.setSetting(SETTING_PROFILE_SERVER_HOST, "172.22.112.154");
		handler.setSetting(SETTING_PROFILE_SERVER_PORT, "44444");
		handler.setStartupType(ServiceStartupType.TRUE);

		/** Setup Test **/
		ExampleTest test = new ExampleTest();
		test.setPollTime(10000);

		CPUTest cpuTest = new CPUTest();
		cpuTest.setPollTime(2000);

		StateChangeTest scTest = new StateChangeTest();
		scTest.setPollTime(2000);
		scTest.setName("StateChangeTest");

		DatabaseCRCTest dbCRCTest = new DatabaseCRCTest();
		dbCRCTest.setPollTime(2000);
		dbCRCTest.setName("DatabaseCRCTest");

		StateManagerStatusTest smSyncTest = new StateManagerStatusTest();
		smSyncTest.setPollTime(2000);
		smSyncTest.setSetting(smSyncTest.getTypeSetting(), "SYNC");
		smSyncTest.setName("StateManagerSyncTest");

		StateManagerStatusTest smMasterTest = new StateManagerStatusTest();
		smMasterTest.setPollTime(2000);
		smMasterTest.setSetting(smMasterTest.getTypeSetting(), "MASTER");
		smMasterTest.setName("StateManagerMasterTest");

		StateManagerQueueTest smqTest = new StateManagerQueueTest();
		smqTest.setPollTime(2000);
		smqTest.setName("StateManagerQueueTest");

		/** Add Tests **/
		handler.getComponents().add(test);
		handler.getComponents().add(cpuTest);
		handler.getComponents().add(scTest);
		handler.getComponents().add(smSyncTest);
		handler.getComponents().add(smMasterTest);
		handler.getComponents().add(smqTest);
		handler.getComponents().add(dbCRCTest);

		/** Create Unit **/
		Unit unit = new Unit();
		unit.setInetAddress(InetAddress.getByName("localhost"));

		/** Create State Manager **/
		StateManager sm = new StateManager();
		sm.attachHandler(handler);

		/** UnitManager Loading **/
		DefaultUnitManagerLoader loader = new DefaultUnitManagerLoader();
		UnitManager.ObjectModel.add(unit);
		UnitManager.ObjectModel.add(sm);
		UnitManager.ObjectModel.add(handler);
		UnitManager.ObjectModel.add(test);
		UnitManager.ObjectModel.add(cpuTest);
		UnitManager.ObjectModel.add(scTest);
		UnitManager.ObjectModel.add(smSyncTest);
		UnitManager.ObjectModel.add(smMasterTest);
		UnitManager.ObjectModel.add(smqTest);
		UnitManager.ObjectModel.add(dbCRCTest);
		UnitManager.execute(loader);

		// System.out.println("Unit MAC Address: " + unit.getMacAddress());
		System.out.println("Unit Part Number: " + handler.unitPartNumber);
		System.out.println("Unit Build Number: " + handler.unitBuildNumber);
		System.out.println("Unit Type: " + handler.unitType);
	}

	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		setSetupPriority(11);

		processor = new ThreadProcessor("Profile Handler '" + getName()
				+ "' Thread Processor.", new ThreadProcessorListener() {

			public void threadProcessorAction(ThreadProcessor processor)
					throws InterruptedException {
				Unit u = UnitManager.ObjectModel.getUnit();
				ProfileTest test;
				ProfileResult result;
				JSONObject json;
				waitTime = maxWaitTime;
				while (processor.isRunning()) {
					long timestamp = UnitManager.Timing.getTimeAlive();
					synchronized (profileTests) {
						int len = profileTests.size();
						StateManager sm = UnitManager.ObjectModel
								.getStateManager();
						for (int i = 0; i < len; i++) {
							test = profileTests.get(i);
							long remainingTime = test.pollTime
									- (timestamp - test.timestamp);
							if (remainingTime <= 0) {
								result = test.run();
								if (result.isValid()) {
								try {
									json = new JSONObject();
									json.put(ProfileProtocol.GRADE,
											result.grade);
									json.put(ProfileProtocol.MAX, result.max);
									json.put(ProfileProtocol.MIN, result.min);
									json.put(ProfileProtocol.TIME, result.time);
									json.put(ProfileProtocol.VALUE,
											result.value);
									json.put(ProfileProtocol.NOTES,
											result.notes);
									json
											.put(
													ProfileProtocol.MASTER,
													getStateManager()
															.getStatus() == StateManagerStatus.MASTER);
									json.put(ProfileProtocol.UNIT_BUILD,
											unitBuildNumber);
									json.put(ProfileProtocol.UNIT_PART,
											unitPartNumber);
									json.put(ProfileProtocol.UNIT_MAC, u
											.getMacAddress());
									json.put(ProfileProtocol.UNIT_TYPE,
											unitType);
									json.put(ProfileProtocol.NAME, test
											.getName());
									packet.setData(json.toString().getBytes());
									socket.send(packet);
								} catch (Exception e) {
									UnitManager.Logging.logSevere(
											"ProfileHandler '" + getName()
													+ "' running test '"
													+ test.getName()
													+ "' with result '"
													+ result
													+ "' threw an exception.",
											e);
								} finally {
									test.timestamp = timestamp;
								}
								}
							}
							waitTime = Math.min(test.pollTime, remainingTime);
						}
					}
					if (waitTime > 0) {
						Thread.sleep(Math.max(waitTime, 100));
					}
				}
			}
		});
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		/*** Load Settings ****/
		TestPropertyResponseName = getSetting(SETTING_TEST_PROPERTY_RESPONSE_NAME, TestPropertyResponseName);
		TestPropertyEnableName = getSetting(SETTING_TEST_PROPERTY_ENABLE_NAME, TestPropertyEnableName);
		maxWaitTime = getSetting(SETTING_PROFILE_MAX_WAIT_TIME, maxWaitTime);
		versionFilename = getSetting(SETTING_PROFILE_VERSION_FILENAME,
				versionFilename);
		port = getSetting(SETTING_PROFILE_LOCAL_PORT, port);
		unitType = getSetting(SETTING_PROFILE_UNIT_TYPE, unitType);
		serverHost = getSetting(SETTING_PROFILE_SERVER_HOST, serverHost);
		serverPort = getSetting(SETTING_PROFILE_SERVER_PORT, serverPort);
		bufferSize = getSetting(SETTING_PROFILE_BUFFER_SIZE, bufferSize);

		/*** Create Cached Fields ***/
		buffer = new byte[bufferSize];
		packet = new DatagramPacket(buffer, bufferSize);
		try {
			packet.setAddress(InetAddress.getByName(serverHost));
		} catch (UnknownHostException e) {
			throw new ComponentSetupException("ProfileHandler '" + getName()
					+ "' could not find Server Host '" + serverHost + "'", e);
		}
		packet.setPort(serverPort);

		/*** Load Tests ***/
		synchronized (profileTests) {
			for (ProfileTest test : getComponents().getByClass(
					ProfileTest.class)) {
				profileTests.add(test);
			}
		}

		/*** Get Software Version ***/
		calcSoftwareVersion();
		
		/*** Get my address based upon 3rd & 4th IP address octet ***/
		myAddress = (UnitManager.ObjectModel.getUnit().getUnitGroup() * 100) + UnitManager.ObjectModel.getUnit().getUnitNumber();

		/*** Open Socket ***/
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			throw new ComponentSetupException("ProfileHandler '" + getName()
					+ "' could not open port '" + port + "'", e);
		}
	}

	@Override
	protected void onStarted(Object source, ServiceStartArgs args)
			throws ServiceStartException {
		super.onStarted(source, args);

		try {
			if (socket == null || packet == null) {
				throw new ServiceStartException(
						"Profile Handler '"
								+ getName()
								+ "' failed to start.  Socket was not properly created.");
			}
			processor.start();
		} catch (ThreadProcessorException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	@Override
	protected void onStopped(Object source, ServiceStopArgs args)
			throws ServiceStopException {
		super.onStopped(source, args);
		try {
			processor.stop();
		} catch (ThreadProcessorException e) {
			UnitManager.Logging.logSevere(e);
		}
	}

	private void calcSoftwareVersion() {
		try {
			File versionFile = new File(versionFilename);
			String line;
			if (versionFile != null && versionFile.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(
						versionFile));
				while ((line = in.readLine()) != null) {
					if (line.startsWith("rc.partnumber")) {
						unitPartNumber = line.substring(13).trim();
					} else if (line.startsWith("rc.buildnumber")) {
						unitBuildNumber = line.substring(14).trim();
					}
				}
				in.close();
			}
		} catch (final Exception e) {
			UnitManager.Logging.logSevere(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.handlers.HandlerTemplate#onPropertyChanged(java.lang.Object, com.rockwellcollins.cs.hcms.core.services.handlers.PropertyChangedArgs)
	 */
	@Override
	protected void onPropertyChanged(Object source, PropertyChangedArgs args) {
		String propName = args.getPropertyName();
		String propValue = args.getPropertyValue();
		try {
			if (propName.equals(TestPropertyEnableName)) {
				if (new Integer(propValue) == myAddress ) {
					if (SystemStateChangeTestEnable == false) {
						SystemStateChangeTestEnable = true;
						ProfileTest test;
						int len = profileTests.size();
						for (int i = 0; i < len; i++) {
							test = profileTests.get(i);
							if (test instanceof SystemStateChangeTest) {
								// enable test
								((SystemStateChangeTest)test).enableTest(true);;
							}
						}
					}
				}
				else {
					if (SystemStateChangeTestEnable == true) {
						SystemStateChangeTestEnable = false;
						ProfileTest test;
						int len = profileTests.size();
						for (int i = 0; i < len; i++) {
							test = profileTests.get(i);
							if (test instanceof SystemStateChangeTest) {
								// disable test
								((SystemStateChangeTest)test).enableTest(false);;
							}
						}
					}
				}
			}
			
			if (SystemStateChangeTestEnable == true) {
				if (propName.equals(TestPropertyResponseName)) {
					ProfileTest test;
					int len = profileTests.size();
					for (int i = 0; i < len; i++) {
						test = profileTests.get(i);
						if (test instanceof SystemStateChangeTest) {
							// record time of response
							((SystemStateChangeTest)test).setResponseTime();
						}
					}
				}
			}
		} catch (Exception e) {
			UnitManager.Logging.logSevere("Exception at ProfileHandler onPropertyChanged." + e);
		}
	}

	/**
	 * Process property change event.
	 * 
	 * @param propertyName the property name
	 * @param propertyValue the property value
	 */
	public void processPropertyChangeEvent(String propertyName, String propertyValue) {
		UnitManager.Logging.logWarning("ProcessPropertyChangeEvent()->The State change wasnt expected to reach here...");
		PropertyMap stateChangeMap = new PropertyMap();
		stateChangeMap.put(propertyName , propertyValue);
		processPropertyChangeEvent(stateChangeMap);
	}

	/* (non-Javadoc)
	 * @see com.rockwellcollins.cs.hcms.core.services.handlers.HandlerTemplate#onPropertyMapChanged(java.lang.Object, com.rockwellcollins.cs.hcms.core.services.handlers.PropertyMapChangedArgs)
	 */
	@Override
	protected void onPropertyMapChanged(Object source,
			PropertyMapChangedArgs args) {

		super.onPropertyMapChanged(source, args);
		processPropertyChangeEvent(args.getPropertyMap());
	}
	
	public void processPropertyChangeEvent(PropertyMap propertyMap) {
		try {
			if(propertyMap.containsKey(TestPropertyEnableName)) {
				String propValue = propertyMap.get(TestPropertyEnableName);
				if (new Integer(propValue) == myAddress ) {
					if (SystemStateChangeTestEnable == false) {
						SystemStateChangeTestEnable = true;
						ProfileTest test;
						int len = profileTests.size();
						for (int i = 0; i < len; i++) {
							test = profileTests.get(i);
							if (test instanceof SystemStateChangeTest) {
								// enable test
								((SystemStateChangeTest)test).enableTest(true);;
							}
						}
					}
				}
				else {
					if (SystemStateChangeTestEnable == true) {
						SystemStateChangeTestEnable = false;
						ProfileTest test;
						int len = profileTests.size();
						for (int i = 0; i < len; i++) {
							test = profileTests.get(i);
							if (test instanceof SystemStateChangeTest) {
								// disable test
								((SystemStateChangeTest)test).enableTest(false);;
							}
						}
					}
				}
			}
		
			if (SystemStateChangeTestEnable == true) {
				if(propertyMap.containsKey(TestPropertyResponseName)) {
					ProfileTest test;
					int len = profileTests.size();
					for (int i = 0; i < len; i++) {
						test = profileTests.get(i);
						if (test instanceof SystemStateChangeTest) {
							// record time of response
							((SystemStateChangeTest)test).setResponseTime();
						}
					}
				}
			}
		} catch (Exception e) {
			UnitManager.Logging.logSevere("Exception at ProfileHandler onPropertyChanged." + e);
		}
	}
}
