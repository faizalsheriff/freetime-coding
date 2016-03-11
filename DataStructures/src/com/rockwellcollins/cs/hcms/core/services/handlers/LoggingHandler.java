/*
 *
 * Copyright 2007-2011 Rockwell Collins, Inc. All Rights Reserved
 * NOTICE: The contents of this medium are proprietary to Rockwell
 * Collins, Inc. and shall not be disclosed, disseminated, copied,
 * or used except for purposes expressly authorized in written by
 * Rockwell Collins, Inc.
 *
 */
package com.rockwellcollins.cs.hcms.core.services.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;

/**
 * The Class LoggingHandler. This class LoggingHandler logs all the registered
 * states, values for this Handler in a circular log file. The File size will be
 * configurable in LCP. Logging Handler also logs additional States associated
 * with a Triggering State
 */
public class LoggingHandler extends Handler {

	private static final long serialVersionUID = 1L;

	/** The Constant logFileMaxSize. */
	private long logFileMaxSize = 0;

	/** The Constant logFileName. */
	private String logFileName = null;

	/** The Constant MSG_START_DEL Message Start Delimiter. */
	private static final char MSG_START_DEL = '\n';

	/** The Constant LH_LOG_FILE_NAME. */
	public static final String LH_LOG_FILE_NAME = "lh_log_file_name";

	/** The Constant LH_LOG_FILE_SIZE. */
	public static final String LH_LOG_FILE_SIZE = "lh_log_file_size";

	/** The Character on which to split the property to get the message type */
	public static final String PRANTHESIS_START = "\\(";

	private static final String CLEAR_LOG_DIR = "ClearLogDirectory";
	
	/**
	 * Regular expression to match if the message type is property formed or not
	 */
	public static final String REGEX_PARANTEHSIS = ".*\\(.*\\).*";

	/** The Space Character used to replace the junk log entries. */
	private static final char SPACE = ' ';

	/** The Constant MODE. */
	private static final String MODE = "rw";

	/** The Logging Directory */
	private String LogDirectory = "./";

	/** The state name **/
	private String stateName;

	/** The state value. */
	private String stateValue;

	/** The write error count. */
	private static int writeErrorCount = 0;

	/** The Log file instance. */
	private transient RandomAccessFile circLog;
	
	/** Time Difference in Milli Seconds */
	private static long mcdTimeDiff = 0;
	
	/** Flag to control the MCD time */
	private boolean mcdTimeProcessed = false;

	/**
	 * Stores the starting position in the file from which messages can be
	 * logged after reserving bytes as the following sequence: xxxxxxxxyyyyyyyyz
	 * xxxxxxxx -> 8 Bytes to store the offset of the oldest entry in the log
	 * yyyyyyyy -> 8 Bytes to store the message ID z -> 1 byte for the new line
	 * character
	 */
	private final static long LOG_START_POS = (2 * (Long.SIZE / 8)) + 1;

	/**
	 * The current pointer will point to the oldest log entry. Since first 8
	 * bytes are reserved to store oldest entry position so its initial value is
	 * set to 9 (Size of Long + 1).
	 */
	private long current = LOG_START_POS;

	/**
	 * The junk characters (incomplete or partial log entries) that remain in
	 * the circular log.
	 */
	private String junk = null;

	/** The BIT State for Flash Write Error. */
	private String LOGH_FLASH_WRITE_ERROR = "LOGH_FLASH_WRITE_ERROR";

	/** The BIT State for Flash Write Error Count. */
	private String LOGH_FLASH_WRITE_ERROR_COUNT = "LOGH_FLASH_WRITE_ERROR_COUNT";

	/** Format for time stamp in the log. */
	private static String TIMESTAMP_FORMAT = "[MMM dd, yyyy H:mm.ss.SSS]";

	/** Message ID. */
	private long messageID = 0;

	/** The boolean value that indicates whether data offloading is occuring */
	private boolean isOffloadingData = false;

	/**
	 * Writes to Log file.
	 * 
	 * @param tState
	 *            The triggering state
	 * @param tValue
	 *            The triggering state value
	 * @param additionalStates
	 *            The additional states
	 */
	private void writeToLog(String tState, String tValue,
			HashMap<String, String> additionalStates, String msgType)
			throws Exception {
		StringBuffer logMessage = new StringBuffer("TriggerState: {");
		logMessage.append(tState + "=" + tValue + "}");

		if (additionalStates != null && additionalStates.size() > 0) {
			logMessage.append("Additional states:");
			logMessage.append(additionalStates.toString());
		}
		logMessage.append(MSG_START_DEL);
		try {
			writeToCircularLog(logMessage.toString(), msgType);
		} catch (IOException ex) {
			++writeErrorCount;
			this.setProperty(LOGH_FLASH_WRITE_ERROR, ex.getMessage());
			this
					.setProperty(LOGH_FLASH_WRITE_ERROR_COUNT, ""
							+ writeErrorCount);
			UnitManager.Logging.logSevere(ex);
			throw ex;

		} catch (Exception ex) {
			UnitManager.Logging
					.logSevere("Exception while writing to LOG file "
							+ ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Fetch all the additional states for a particular Triggering State.
	 * 
	 * @param propName
	 *            The propertyName that links to a setting list
	 * @param additionalStates
	 *            the additional states
	 * 
	 * @return The Map of additional States for a State.
	 */
	private void fetchAdditionalStates(String propName, String msgType,
			HashMap<String, String> additionalStates) {
		String[] settingList = getSettingList(propName);
		for (String setting : settingList) {
			String state = getStateName(setting + msgType);
			String value = UnitManager.ObjectModel.getStateManager()
					.getStateDatabase().getValue(state);
			additionalStates.put(state, value);
		}
	}

	/**
	 * Write States and Values to a circular log.
	 * 
	 * @param msg
	 *            The message to be logged.
	 * 
	 * @throws Exception
	 *             The exception
	 */
	protected void writeToCircularLog(String msg, String msgType)
			throws Exception {
		String msgToLog = null;
		long msgLen = 0;
		try {
			circLog = new RandomAccessFile(LogDirectory + "/" + logFileName,
					MODE);
			if (circLog.length() == 0 || circLog.length() < LOG_START_POS) {
				/*
				 * If file is written for the first time, OR if the header bytes
				 * are missing, start logging from the beginning. And reset
				 * messageID = 0
				 */
				circLog.seek(0);
				circLog.writeLong(LOG_START_POS);
				circLog.writeLong(0);
				circLog.writeByte(MSG_START_DEL);
			}
			circLog.seek(0);
			current = circLog.readLong();
			messageID = circLog.readLong();
			if (messageID > Integer.MAX_VALUE) {
				messageID = 0;
			}

			msgToLog = getTimeStamp() + getMessageID(++messageID) + msgType
					+ msg;
			msgLen = msgToLog.length();

			if (logFileMaxSize - current < msgLen) {
				current = LOG_START_POS;
			}
			circLog.seek(current);
			circLog.writeBytes(msgToLog);
			current = circLog.getFilePointer();
			junk = circLog.readLine();
			if (junk != null && junk.length() > 0
					&& (junk.indexOf(MSG_START_DEL) == -1)) {
				circLog.seek(current);
				for (int k = 0; k < junk.length(); k++) {
					circLog.writeByte(SPACE);
				}
			}
		} catch (FileNotFoundException fnfEx) {
			UnitManager.Logging.logSevere(fnfEx);
			throw fnfEx;
		} catch (Exception ex) {
			UnitManager.Logging.logSevere(ex);
			throw ex;
		} finally {
			try {
				circLog.seek(0);
				circLog.writeLong(current);
				circLog.writeLong(messageID);
				circLog.writeByte(MSG_START_DEL);
				circLog.close();
			} catch (IOException ex) {
				UnitManager.Logging.logSevere("File IO Exception "
						+ ex.getMessage());
				throw ex;
			}
		}
	}

	/**
	 * Gets the formatted time stamp.
	 * 
	 * @return The time stamp
	 */
	private static String getTimeStamp() {
		long currentTime = System.currentTimeMillis();
		currentTime = currentTime+ mcdTimeDiff;
		Date d1 = new Date(currentTime);
		Locale currentLocale = Locale.getDefault();
		SimpleDateFormat simpleFormat = new SimpleDateFormat(TIMESTAMP_FORMAT,
				currentLocale);
		return simpleFormat.format(d1);
	}

	/**
	 * Gets the message ID of the last message in the log.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return the message ID
	 */
	private static String getMessageID(long id) {
		return " (" + id + ") ";
	}

	@Override
	protected void onPropertyChanged(Object source, PropertyChangedArgs args) {
		// TODO Auto-generated method stub
		String propName = args.getPropertyName();
		if(propName.equalsIgnoreCase(CLEAR_LOG_DIR) && args.getPropertyValue().equalsIgnoreCase("true")) {
			UnitManager.Logging.logWarning("LoggingHandler->Clear Log Directory triggered.");
			clearLogDirectory();
		}
		
		// Added for live timestamp - Start
		if(propName.equalsIgnoreCase("mcdLiveTime")) {
			long mcdTimeMsec = 0;
			if(args.getPropertyValue().length()>0) {
			 mcdTimeMsec = Long.parseLong(args.getPropertyValue());
			}
			if(!mcdTimeProcessed && (mcdTimeMsec > 0)) {
				UnitManager.Logging.logWarning("Received MCD Live Time");
				mcdTimeDiff = mcdTimeMsec - UnitManager.Timing.getTimeAlive();
				UnitManager.Logging.logWarning("Time Difference (msec): " + mcdTimeDiff);
				UnitManager.Logging.setTimeDifference(mcdTimeDiff);
				mcdTimeProcessed = true;
			}
		}
		// Added for live timestamp - End

		if (propName.equals("DataOffload")) {
			String propertyValue = args.getPropertyValue().toLowerCase();
			if (propertyValue.equals("true")) {
				// disable logging when data offload is occurring
				isOffloadingData = true;
			} else {
				// enable logging when data offload is completed
				isOffloadingData = false;
			}
		} else if (isOffloadingData == false && !propName.equalsIgnoreCase("mcdLiveTime")) {
			UnitManager.Logging.logDebug("LOGH:: Got State change notification"
					+ args.getPropertyName());
			try {
				HashMap<String, String> additionalStates = new HashMap<String, String>();
				String[] propertyInfo;
				String msgType = null, propertyName = null;
				String tempPropertyName = args.getPropertyName();
				if (tempPropertyName.matches(REGEX_PARANTEHSIS)) {
					propertyInfo = tempPropertyName.split(PRANTHESIS_START);
					msgType = "(" + propertyInfo[1];
					propertyName = propertyInfo[0];
				} else {
					msgType = "()";
					propertyName = tempPropertyName;
				}

				stateName = getStateName(args.getPropertyName());
				stateValue = args.getPropertyValue();
				// Now fetch the companion states.
				fetchAdditionalStates(propertyName, msgType, additionalStates);
				writeToLog(stateName, stateValue, additionalStates, msgType);

			} catch (Exception ex) {
				UnitManager.Logging.logSevere(ex.getMessage());
			}
		}

	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		// TODO Auto-generated method stub
		super.onSetup(source, args);

		/** import the settings */
		importSettings();
	}

	@Override
	protected void onStarted(Object source, ServiceStartArgs args)
			throws ServiceStartException {
		// TODO Auto-generated method stub
		super.onStarted(source, args);
		LogDirectory = UnitManager.ObjectModel.getUnit().getLogDirectory()
				.getAbsolutePath();
	}

	public void importSettings() {
		// CR 6615: Removed hard-coded instance name from handler setting
		logFileName = UnitManager.ObjectModel.getUnit().getInstanceName() 
						+ "_" + getSetting(LH_LOG_FILE_NAME, "");
		
		logFileMaxSize = Long.parseLong(getSetting(LH_LOG_FILE_SIZE, ""));
	}

	@Override
	protected void onPropertyChangedTimeout(Object source,
			PropertyChangeTimeoutArgs args) {
		// TODO Auto-generated method stub

	}

	// TBR Added for testing.
	/*
	 * public static void main(String[] arg) throws Exception{
	 * 
	 * LoggingHandler circularLogImpl = new LoggingHandler();
	 * circularLogImpl.logFileMaxSize = 10241024; circularLogImpl.logFileName =
	 * "CircularLog.log";
	 * 
	 * HashMap<String, String> map = new HashMap<String, String>();
	 * map.put("AState1", "AValue1"); map.put("AState2", "AValue2");
	 * map.put("AState3", "AValue3"); map.put("AState4", "AValue4");
	 * map.put("AState5", "AValue5"); map.put("AState6", "AValue6"); // for (int
	 * i = 1; i <= 1; i++) { // circularLogImpl.writeToCircularLog( ": " // +
	 * System.nanoTime() + " : " + Math.random() // + "@" + "\n"); // } for(int
	 * i=0; i<999; i++ ){ circularLogImpl.writeToLog("State", "Value", map); } }
	 */
	private void clearLogDirectory() {
		if(isDebug()) {
			UnitManager.Logging.logDebug("LoggingHandler->clearLogDir()...");
		}
		File varlog = UnitManager.ObjectModel.getUnit().getLogDirectory();
		boolean fileDelete = false;
		String[] children = varlog.list();
		   for (int i=0; i<children.length; i++) {
			   if(isDebug()) {
				   UnitManager.Logging.logDebug("LoggingHandler->clearLogDir(): file: '"+children[i]+"'");
			   }
			   if(isInfo()) {
				   UnitManager.Logging.logInfo("LoggingHandler->ClearLogDir(): Removing file.."+children[i]);
			   }
			   // new File(varlog,children[i]).delete();
			   fileDelete = deleteDirectory(new File(varlog,children[i]));
			   if(isInfo()) {
				   UnitManager.Logging.logInfo("LoggingHandler->Removing the file '" + children[i] + "' was '" + fileDelete + "'");
			   }
		   }
	}
	private boolean deleteDirectory(File dir) {
	    if (!dir.isDirectory()) {
	    	UnitManager.Logging.logWarning("LoggingHandler removing the File/Directory: '"+dir.getName()+"'");
	    	return dir.delete();
	    }
	    return false;
	}
}
