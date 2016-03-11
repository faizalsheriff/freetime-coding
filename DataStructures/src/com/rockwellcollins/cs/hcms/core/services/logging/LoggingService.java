package com.rockwellcollins.cs.hcms.core.services.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.ComponentSetupArgs;
import com.rockwellcollins.cs.hcms.core.ComponentSetupException;
import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.Queue;
import com.rockwellcollins.cs.hcms.core.services.MessagingService;
import com.rockwellcollins.cs.hcms.core.services.MessagingServiceReceiveMessageArgs;
import com.rockwellcollins.cs.hcms.core.services.MulticastService;
import com.rockwellcollins.cs.hcms.core.services.MulticastServiceTimeoutArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.ServiceState;
import com.rockwellcollins.cs.hcms.core.services.messaging.Message;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerMode;

public class LoggingService extends MessagingService {

	private static final long serialVersionUID = 1L;
	
	public static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static final String SETTING_LOG_QUEUE_SIZE = "log queue size";
	public static final String SETTING_LOG_EXCEPTION_FILTER = "log exception filter";
	public static final String SETTING_LOG_SOURCE_FILTER = "log source filter";
	public static final String SETTING_LOG_MESSAGE_FILTER = "log message filter";
	public static final String SETTING_LOG_TYPE_FILTER = "log type filter";
	public static final String SETTING_LOG_FILE_NUMBER = "log file number";
	public static final String SETTING_LOG_FILE_SIZE = "log file size";
	public static final String SETTING_LOG_FILE_EXTENSION = "log file extension";
	public static final String SETTING_LOG_FILE_NAME = "log file name";
	public static final String SETTING_MULTICAST_TOOL_PORT = "multicast tool port";
	public static final String SETTING_LOG_TO_ALL = "log to all";

	public static final String JSON_DATE = "date";
	public static final String JSON_TYPE = "type";
	public static final String JSON_SOURCE = "source";
	public static final String JSON_MESSAGE = "message";
	public static final String JSON_EXCEPTION = "exception";

	transient private int currentFile = 0;
	transient private Pattern sourcePattern;
	transient private Pattern typePattern;
	transient private Pattern messagePattern;
	transient private Pattern exceptionPattern;
	transient private Queue<Message> queue;

	private int queueSize = 10;
	private String fileName = "LoggingService";
	private String fileExtension = "log";
	private int logSize = 1024;
	private int logFiles = 10;
	private boolean logging = true;
	private int toolPort;
	private boolean logToAll = true;

	private boolean filterMessage(final Message message) {

		boolean filterPass = false;

		try {

			if (message == null) {
				UnitManager.Logging
						.logSevere("Message to be Filtered by Logging Service '"
								+ toString() + "' is null.");
			}

			else if (sourcePattern.matcher(
					message.getJsonObject().optString(JSON_SOURCE, ""))
					.matches()
					&& typePattern.matcher(
							message.getJsonObject().optString(JSON_TYPE, "")
									.toString()).matches()
					&& messagePattern
							.matcher(
									message.getJsonObject().optString(
											JSON_MESSAGE, "")).matches()
					&& exceptionPattern.matcher(
							message.getJsonObject().optString(JSON_EXCEPTION,
									"")).matches()) {

				filterPass = true;
			}
		} catch (final Exception e) {
			UnitManager.Logging.logStdOut("Exception in Logging Service: "
					+ UnitManager.Logging.getStackTrace(e));
		}

		return filterPass;
	}

	private File getFile() {
		return getFile(this.currentFile);
	}

	private File getFile(final int fileNumber) {

		final File file = new File(UnitManager.ObjectModel.getUnit()
				.getLogDirectory(), fileName + "." + fileNumber + "."
				+ fileExtension);

		return file;
	}

	protected void logMessage(final Message message) {

		synchronized (this) {

			File file = getFile();

			final StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("[");
			stringBuilder.append(DATEFORMAT.format(new Date(System.currentTimeMillis()+UnitManager.Logging.getTimeDifference())));
			stringBuilder.append("] ");
			stringBuilder.append(message);
			stringBuilder.append(Consts.IOs.LINE_SEPARATOR);

			if (file.exists()) {
				if (file.length() < logSize) {
					try {

						final FileWriter fout = new FileWriter(file, true);
						fout.append(stringBuilder.toString());
						fout.flush();
						fout.close();

					} catch (final IOException e) {

						UnitManager.Logging
								.logStdOut("Exception Writing File in Logging Service '"
										+ toString()
										+ "'. Exception: "
										+ e.toString());
					}

					return;
				} else {
					currentFile = (currentFile + 1) % logFiles;
					file = getFile();
				}
			}

			try {

				final FileWriter fout = new FileWriter(file, false);
				fout.append(stringBuilder.toString());
				fout.flush();
				fout.close();

			} catch (final IOException e) {

				UnitManager.Logging
						.logStdOut("Exception Logging to File in Logging Service '"
								+ toString() + "'.  Exception: " + e.toString());

			}
		}
	}

	protected void processMessage(final Message message) {
		if (filterMessage(message)) {
			logMessage(message);
		}
	}

	@Override
	protected void onSetup(Object source, ComponentSetupArgs args)
			throws ComponentSetupException {

		super.onSetup(source, args);

		// CR 6615: Removed hard-coded instance name from handler setting
		fileName = UnitManager.ObjectModel.getUnit().getInstanceName()
					+ "_" + getSetting(SETTING_LOG_FILE_NAME, fileName);
		
		fileExtension = getSetting(SETTING_LOG_FILE_EXTENSION, fileExtension);
		logSize = getSetting(SETTING_LOG_FILE_SIZE, logSize);
		logFiles = getSetting(SETTING_LOG_FILE_NUMBER, logFiles);
		toolPort = getSetting(SETTING_MULTICAST_TOOL_PORT, 60100);
		logToAll = getSetting(SETTING_LOG_TO_ALL, true);

		typePattern = Pattern.compile(
				getSetting(SETTING_LOG_TYPE_FILTER, ".*"), Pattern.MULTILINE
						| Pattern.DOTALL);
		messagePattern = Pattern.compile(getSetting(SETTING_LOG_MESSAGE_FILTER,
				".*"), Pattern.MULTILINE | Pattern.DOTALL);
		sourcePattern = Pattern.compile(getSetting(SETTING_LOG_SOURCE_FILTER,
				".*"), Pattern.MULTILINE | Pattern.DOTALL);
		exceptionPattern = Pattern.compile(getSetting(
				SETTING_LOG_EXCEPTION_FILTER, ".*"), Pattern.MULTILINE
				| Pattern.DOTALL);
		queueSize = getSetting(SETTING_LOG_QUEUE_SIZE, queueSize);

		long lowestFileModified = 0;

		for (int i = 0; i < logFiles; i++) {

			final File file = getFile(i);

			if (!file.exists()) {
				break;
			}

			if (lowestFileModified == 0) {
				lowestFileModified = file.lastModified();
				currentFile = i;
			}

			if (lowestFileModified < file.lastModified()) {
				lowestFileModified = file.lastModified();
				currentFile = i;
			}
		}
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		setSetupPriority(2);

		queue = new Queue<Message>();
		currentFile = 0;
	}

	@Override
	protected void onReceivedMessage(final Object source,
			final MessagingServiceReceiveMessageArgs args) {

		if (isLogging()) {
			processMessage(args.getMessage());
		}
	}

	@Override
	protected void onReceivedTimeout(MulticastService multicastService,
			MulticastServiceTimeoutArgs targs) {

		super.onReceivedTimeout(multicastService, targs);

		processQueue();
	}

	private final void queueMessage(final Message message) {
		synchronized (queue) {
			if (queue.size() < queueSize) {
				queue.add(message);
			} else {
				UnitManager.Logging.logStdOut("Logging Service Queue Full: "
						+ message);
			}
		}
	}

	private final boolean processQueue() {

		Message msg;

		synchronized (queue) {

			while ((msg = queue.peek()) != null) {
				try {
					if (logToAll) {
						// send to general multicast address (all units)
						super.send(msg);
					} else {
						// send to tool multicast port
						super.send(msg, super.getMulticastGroup(), toolPort);
						
						// send to self only
						super.send(msg, UnitManager.ObjectModel.getUnit().getInetAddress(), 
								super.getPort());
					}
					queue.take();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	public final static Message createMessage(final String date,
			final LoggingType type, final String source, final String message,
			final String exception) {
		final Message msg = new Message();

		try {
			msg.getJsonObject().put(JSON_DATE, date);
			msg.getJsonObject().put(JSON_TYPE, type.toString());
			msg.getJsonObject().put(JSON_SOURCE, source);
			msg.getJsonObject().put(JSON_MESSAGE, message);
			msg.getJsonObject().put(JSON_EXCEPTION, exception);
		} catch (final JSONException e) {
			UnitManager.Logging.logStdOut("Exception in Logging Service: "
					+ UnitManager.Logging.getStackTrace(e));
		}

		return msg;
	}

	public final void setLogging(final boolean logging) {
		this.logging = logging;
	}

	public final boolean isLogging() {
		return logging;
	}

	@Override
	public final void send(Message message) {
		
		if(UnitManager.ObjectModel.getStateManager()!=null) {
			if (UnitManager.ObjectModel.getStateManager().getMode() == StateManagerMode.PASSIVE) {
				return;
			}
		}
		if (processQueue() && getServiceState() == ServiceState.RUNNING) {
			try {
				if (logToAll) {
					// send to general multicast address (all units)
					super.send(message);
				} else {
					// send to tool multicast port
					super.send(message, super.getMulticastGroup(), toolPort);
					
					// send to self only
					super.send(message, UnitManager.ObjectModel.getUnit().getInetAddress(), 
							super.getPort());
				}
			} catch (final ServiceIOException e) {
				queueMessage(message);
			}
		} else {
			queueMessage(message);
		}
	}

	public final void setSourcePattern(final Pattern sourcePattern) {
		this.sourcePattern = sourcePattern;
	}

	public final Pattern getSourcePattern() {
		return sourcePattern;
	}

	public final void setTypePattern(final Pattern typePattern) {
		this.typePattern = typePattern;
	}

	public final Pattern getTypePattern() {
		return typePattern;
	}

	public final void setMessagePattern(final Pattern messagePattern) {
		this.messagePattern = messagePattern;
	}

	public final Pattern getMessagePattern() {
		return messagePattern;
	}

	public final void setExceptionPattern(final Pattern exceptionPattern) {
		this.exceptionPattern = exceptionPattern;
	}

	public final Pattern getExceptionPattern() {
		return exceptionPattern;
	}

	public final void setLogSize(final int logSize) {
		this.logSize = logSize;
	}

	public final int getLogSize() {
		return logSize;
	}

	public final void setLogFiles(final int logFiles) {
		this.logFiles = logFiles;
	}

	public final int getLogFiles() {
		return logFiles;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public final String getFileName() {
		return fileName;
	}

	public final void setFileExtension(final String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public final String getFileExtension() {
		return fileExtension;
	}
}
