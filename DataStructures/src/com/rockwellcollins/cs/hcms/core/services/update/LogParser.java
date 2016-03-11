package com.rockwellcollins.cs.hcms.core.services.update;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;

public class LogParser {

	public static final String NOT_AVAILABLE = "not available";

	private UpdateService updateService;

	private HardwareInfo hardwareDetails;

	private UpdateHandler updateHandler;

	private boolean isDeleted;

	public void setUpdateService(final UpdateService updateService) {
		this.updateService = updateService;
	}

	public void setHardwareDetails(final HardwareInfo hardwareDetails) {
		this.hardwareDetails = hardwareDetails;
	}

	public void setUpdateHandler(final UpdateHandler updateHandler) {
		this.updateHandler = updateHandler;
	}

	public void setDeleted(final boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public String checkDownloadAbort() {
		parseDownloadListFile();
		return parseDownloadAbortLog();
	}

	private ArrayList<String> parseDownloadListFile() {
		ArrayList<String> downloadFileList = null;

		final String downloadListFilePath = getFilePath(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_LIST_FILE);
		if (downloadListFilePath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Download list file: " + downloadListFilePath + " not available");
			}
			return downloadFileList;
		}

		final File downloadListFile = new File(downloadListFilePath);

		if (!downloadListFile.exists()) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Download list file: " + downloadListFilePath + " not available");
			}
			return downloadFileList;
		}

		try {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Reading: " + downloadListFilePath);
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(downloadListFile)));
			String line = null;
			downloadFileList = new ArrayList<String>();

			while ((line = br.readLine()) != null) {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Download list file entry: " + line);
				}
				if (line.length() > 0) {
					final StringTokenizer st = new StringTokenizer(line.trim(), "=");
					String key = null;
					String value = null;
					if (st.hasMoreTokens()) {
						key = st.nextToken().trim();

						if (st.hasMoreTokens()) {
							value = st.nextToken().trim();
						} else {
							value = "";
						}
					} else {
						key = "";
					}

					if (key.equalsIgnoreCase("DownloadCII") || key.equalsIgnoreCase("DownloadPatch")) {
						if (value != null && value.length() > 0) {
							downloadFileList.add(value);
						}
					}
				}
			}
			br.close();

			// Generate an abort log and delete all the partial downloaded files
			if (downloadFileList != null && downloadFileList.size() > 0) {
				generateDownloadAbortLog();
				deleteFiles(downloadFileList);
			}

			// Delete the dowload list file itself
			if (downloadListFile.delete()) {
				isDeleted = true;
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Deleted the download list file: " + downloadListFilePath);
				}
			} else {
				UnitManager.Logging.logSevere("Update Service: Failed to delete the download list file: " + downloadListFilePath);
			}

			// Delete the la.txt file as well
			// Deleting this file will make the Update Service resume the download
			// Only if the LA Message is still being sent from the host
			String laFilePath = getFilePath(updateService.getPathToLa(), UpdateService.LA_FILE);
			final File laFile = new File(laFilePath);
			if (laFile.exists()) {
				if (laFile.delete()) {
					isDeleted = true;
					if (updateService.isInfo()) {
						updateService.logInfo("Update Service: Deleted the LA timestamp file: " + laFilePath);
					}

					// Reading the deleted file again will cause the local variable set to null
					// The LAProcessor will lose the original time stamp causing it to process the
					// Same LA Message again, if it is still being sent from the host
					updateService.getLaProcessor().readOriginalTime(updateService.getPathToLa(), UpdateService.LA_FILE);
				} else {
					UnitManager.Logging.logSevere("Update Service: Failed to delete the LA timestamp file: " + laFilePath);
				}
			} else {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: LA timestamp file: " + laFilePath + " is not available to delete");
				}
			}
		} catch (FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: " + downloadListFilePath + " is not available", fnfe);
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read " + downloadListFilePath, ioe);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception occurred while reading: " + downloadListFilePath, e);
		}

		return downloadFileList;
	}

	private void generateDownloadAbortLog() {
		// Generate a new log file
		try {
			String downloadAbortFilePath = getFilePath(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_ABORT_LOG_FILE);
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Generating download abort log file: " + downloadAbortFilePath);
			}

			final File downloadAbortFile = new File(downloadAbortFilePath);

			final FileOutputStream fos = new FileOutputStream(downloadAbortFile);

			final PrintStream ps = new PrintStream(fos);

			// Line 1: Just log saying download was aborted
			if (UnitManager.Logging.isInfo()) {
				updateService.logInfo("Update Service: Writing to file: Installation aborted");
			}
			ps.println("Installation aborted during file transfer");

			if (UnitManager.Logging.isInfo()) {
				updateService.logInfo("Update Service: Flushing and closing the file: " + downloadAbortFile.getName());
			}
			ps.flush();
			ps.close();
		} catch (final FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: downloadabort.log file missing", fnfe);
			updateHandler.flushFiles(updateService.getPathToInstallLog(), true);
		}
	}

	private void deleteFiles(ArrayList<String> deleteFiles) {
		for (String fileName : deleteFiles) {
			final File file = new File(fileName);
			if (file.exists()) {
				if (file.delete()) {
					isDeleted = true;
					if (updateService.isInfo()) {
						updateService.logInfo("Update Service: Deleted the file: " + fileName);
					}
				} else {
					UnitManager.Logging.logSevere("Update Service: Failed to delete the file: " + fileName);
				}
			}
		}
	}

	private String parseDownloadAbortLog() {
		String status = null;
		String downloadAbortLogPath = getFilePath(updateService.getPathToInstallLog(), UpdateService.DOWNLOAD_ABORT_LOG_FILE);
		if (downloadAbortLogPath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file: " + downloadAbortLogPath + " not available");
				;
			}
			return status;
		}

		final File downloadAbortLog = new File(downloadAbortLogPath);

		if (!downloadAbortLog.exists()) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file: " + downloadAbortLogPath + " not available");
			}
			return status;
		}

		try {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Reading: " + downloadAbortLogPath);
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(downloadAbortLog)));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Log file entry: " + line);
				}
				if (line.length() > 0) {
					status = line;
				}
			}

			br.close();
		} catch (FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: " + downloadAbortLogPath + " is not available", fnfe);
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read " + downloadAbortLogPath, ioe);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception occurred while reading: " + downloadAbortLogPath, e);
		}
		return status;
	}

	public ArrayList<String> parseLCPHeader() {
		ArrayList<String> lcpFiles = null;
		String installLCPHeaderPath = getFilePath(updateService.getPathToInstallLog(), UpdateService.INSTALL_LCP_FILE);
		if (installLCPHeaderPath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file: " + installLCPHeaderPath + " not available");
				;
			}
			return lcpFiles;
		}

		final File installLCPHeader = new File(installLCPHeaderPath);

		if (!installLCPHeader.exists()) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file: " + installLCPHeaderPath + " not available");
			}
			return lcpFiles;
		}

		lcpFiles = new ArrayList<String>();
		try {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Reading: " + installLCPHeaderPath);
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(installLCPHeader)));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Log file entry: " + line);
				}
				if (line.length() > 0) {
					final StringTokenizer st = new StringTokenizer(line.trim(), "=");
					String key = null;
					String value = null;
					if (st.hasMoreTokens()) {
						key = st.nextToken().trim();

						if (st.hasMoreTokens()) {
							value = st.nextToken().trim();
						} else {
							value = "";
						}
					} else {
						key = "";
					}

					if (key.equalsIgnoreCase("UpdateCII") || key.equalsIgnoreCase("UpdatePatch")) {
						if (value != null && value.length() > 0) {
							lcpFiles.add(value);
						}
					}
				}
			}

			br.close();

			// Delete the LCP CII and Parent files which were burnt partially
			if (lcpFiles != null && lcpFiles.size() > 0) {
				deleteFiles(lcpFiles);
			}

			// Delete the LCP header file itself
			if (installLCPHeader.delete()) {
				isDeleted = true;
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Deleted the LCP header file: " + installLCPHeaderPath);
				}
			} else {
				UnitManager.Logging.logSevere("Update Service: Failed to delete the LCP header file: " + installLCPHeaderPath);
			}
		} catch (FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: " + installLCPHeaderPath + " is not available", fnfe);
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read " + installLCPHeaderPath, ioe);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception occurred while reading: " + installLCPHeaderPath, e);
		}
		return lcpFiles;
	}

	public ArrayList<InstallStatus> parseAbortLog(final boolean isDelete) {
		ArrayList<InstallStatus> installStatusList = null;
		String installLogPath = getFilePath(updateService.getPathToInstallLog(), UpdateService.INSTALL_ABORT_LOG_FILE);
		if (installLogPath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file: " + installLogPath + " not available");
				;
			}
			return installStatusList;
		}

		final File installLog = new File(installLogPath);

		if (!installLog.exists()) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file: " + installLogPath + " not available");
			}
			return installStatusList;
		}

		try {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Reading: " + installLogPath);
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(installLog)));
			String line = null;
			installStatusList = new ArrayList<InstallStatus>();

			while ((line = br.readLine()) != null) {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Log file entry: " + line);
				}
				if (line.length() > 0) {
					final StringTokenizer st = new StringTokenizer(line.trim(), "=");
					String key = null;
					String value = null;
					if (st.hasMoreTokens()) {
						key = st.nextToken().trim();

						if (st.hasMoreTokens()) {
							value = st.nextToken().trim();
						} else {
							value = "";
						}
					} else {
						key = "";
					}

					if (key.equalsIgnoreCase("LRU")) {
						InstallStatus installStatus = new InstallStatus();
						installStatus.setUpdateService(updateService);
						installStatus.setAborted(true);

						if (value != null && value.length() > 0) {
							installStatus.setLruType(value);
						} else {
							installStatus.setLruType(NOT_AVAILABLE);
						}
						installStatusList.add(installStatus);
					} else if (key.equalsIgnoreCase("UpdateTime")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setUpdateTime(value);
							} else {
								installStatus.setUpdateTime(NOT_AVAILABLE);
							}
						}
					} else if (key.equalsIgnoreCase("TotalSteps")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								try {
									installStatus.setTotalSteps(Integer.parseInt(value));
								} catch (NumberFormatException nfe) {
									UnitManager.Logging.logSevere("Update Service: Log file: " + installLogPath, nfe);
									installStatus.setTotalSteps(0);
								}
							} else {
								installStatus.setTotalSteps(0);
							}
						}
					} else if (key.equalsIgnoreCase("UpdateCII")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setCiiFile(value);
							} else {
								installStatus.setCiiFile(NOT_AVAILABLE);
							}
						}
					} else if (key.equalsIgnoreCase("UpdatePatch")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setLoadableFile(value);
							} else {
								installStatus.setLoadableFile(NOT_AVAILABLE);
							}
						}
					} else if (key.equalsIgnoreCase("AbortReason")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setAbortReason(value);
							} else {
								installStatus.setAbortReason(NOT_AVAILABLE);
							}
						}
					} else if (key.equalsIgnoreCase("OverallInstall")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setOverallInstall(value);
							} else {
								installStatus.setOverallInstall(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("LA")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setLA(value);
							} else {
								installStatus.setLA(NOT_AVAILABLE);
							}
						}
					}

				}
			}

			br.close();

			// Initialize all the variables that were not initialized while parsing
			// the log file
			if (installStatusList != null && installStatusList.size() > 0) {
				for (InstallStatus installStatus : installStatusList) {
					if (installStatus.getLruType() == null) {
						installStatus.setLruType(NOT_AVAILABLE);
					}
					if (installStatus.getUpdateTime() == null) {
						// UpdateTime should be always unique to make sure the
						// log file is not processed more than once by the
						// secondary LRU handlers like PPC Handler
						final Calendar c = Calendar.getInstance();
						final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
						sdf.format(c.getTime());
						installStatus.setUpdateTime(sdf.format(c.getTime()));
					}
					if (installStatus.getCiiFile() == null) {
						installStatus.setCiiFile(NOT_AVAILABLE);
					}
					if (installStatus.getLoadableFile() == null) {
						installStatus.setLoadableFile(NOT_AVAILABLE);
					}
					if (installStatus.getPartNumber() == null) {
						installStatus.setPartNumber(NOT_AVAILABLE);
					}
					if (installStatus.getBuildNumber() == null) {
						installStatus.setBuildNumber(NOT_AVAILABLE);
					}
					if (installStatus.getAbortReason() == null) {
						installStatus.setAbortReason(NOT_AVAILABLE);
					}
					if (installStatus.getOverallInstall() == null) {
						installStatus.setOverallInstall(NOT_AVAILABLE);
					}
				}
			}

			if (isDelete) {
				if (installStatusList != null && installStatusList.size() > 0) {
					for (InstallStatus installStatus : installStatusList) {
						deleteFiles(installStatus);
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: " + installLogPath + " is not available", fnfe);
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read " + installLogPath, ioe);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception occurred while reading: " + installLogPath, e);
		}
		return installStatusList;
	}

	public ArrayList<InstallStatus> parseAllInstallLogs(final boolean isDelete) {
		final ArrayList<InstallStatus> installStatusList = new ArrayList<InstallStatus>();

		final InstallStatus lruInstallStatus = parseInstallLog(hardwareDetails.getDeviceType(), isDelete);
		if (lruInstallStatus != null) {
			installStatusList.add(lruInstallStatus);
		}
		final InstallStatus lcpInstallStatus = parseInstallLog(updateService.getLCPType(), isDelete);
		if (lcpInstallStatus != null) {
			installStatusList.add(lcpInstallStatus);
		}
		final ArrayList<InstallStatus> secondaryInstallStatus = parseOtherInstallLog(UpdateService.SECONDARY_INSTALL_FILE, isDelete);
		if (secondaryInstallStatus != null && secondaryInstallStatus.size() > 0) {
			installStatusList.addAll(secondaryInstallStatus);
		}
		final ArrayList<InstallStatus> backupInstallStatus = parseOtherInstallLog(UpdateService.BACKUP_INSTALL_FILE, isDelete);
		if (backupInstallStatus != null && backupInstallStatus.size() > 0) {
			installStatusList.addAll(backupInstallStatus);
		}
		return installStatusList;
	}

	public InstallStatus parseInstallLog(final String lruType, final boolean isDelete) {
		InstallStatus installStatus = null;
		String installLogPath = null;
		if (lruType.equals(hardwareDetails.getDeviceType())) {
			installLogPath = getFilePath(updateService.getPathToInstallLog(), UpdateService.INSTALL_LOG_FILE);
		} else if (lruType.equals(updateService.getLCPType())) {
			installLogPath = getFilePath(updateService.getPathToInstallLog(), UpdateService.INSTALL_LCP_LOG_FILE);
		}

		if (installLogPath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Not a valid LRU type: " + lruType);
			}
			return installStatus;
		}

		final File installLog = new File(installLogPath);

		if (!installLog.exists()) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file: " + installLogPath + " is missing for: " + lruType);
			}
			return installStatus;
		}

		try {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Reading: " + installLogPath);
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(installLog)));

			String line = null;
			String updateTime = "";
			String totalSteps = "";
			String ciiFile = "";
			String loadableFile = "";
			String overallInstall = "";
			String LA = "";

			final StringBuilder probableCIIFileName = new StringBuilder();
			probableCIIFileName.append(lruType);
			probableCIIFileName.append(UpdateService.CII_EXTN);

			boolean isProbablyOldCIIFile = false;

			final StringBuilder installLogData = new StringBuilder();

			while ((line = br.readLine()) != null) {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Log file entry: " + line);
				}
				if (line.length() > 0) {
					final StringTokenizer st = new StringTokenizer(line.trim(), "=");
					String key = null;
					String value = null;
					if (st.hasMoreTokens()) {
						key = st.nextToken().trim();

						if (st.hasMoreTokens()) {
							value = st.nextToken().trim();
						} else {
							value = "";
						}
					} else {
						key = "";
					}

					if (key.endsWith("UpdateTime")) {
						updateTime = value;
					} else if (key.endsWith("TotalSteps")) {
						totalSteps = value;
					} else if (key.endsWith("UpdateCII")) {
						if (!value.endsWith(probableCIIFileName.toString())) {
							ciiFile = value;
						} else {
							isProbablyOldCIIFile = true;
						}
					} else if (key.endsWith("UpdatePatch")) {
						loadableFile = value;
					} else if (key.endsWith("OverallInstall")) {
						overallInstall = value;
					} else if (key.endsWith("LA")) {
						LA = value;
					} else {
						if (installLogData.length() > 0) {
							installLogData.append(" ");
						}
						installLogData.append(line);
					}
				}
			}

			br.close();

			installStatus = new InstallStatus();
			installStatus.setUpdateService(updateService);

			installStatus.setLruType(lruType);

			if (updateTime != null && updateTime.length() > 0) {
				installStatus.setUpdateTime(updateTime);
			} else {
				installStatus.setUpdateTime(NOT_AVAILABLE);
			}

			if (totalSteps != null && totalSteps.length() > 0) {
				try {
					installStatus.setTotalSteps(Integer.parseInt(totalSteps));
				} catch (NumberFormatException nfe) {
					UnitManager.Logging.logSevere("Update Service: Log file: " + installLogPath, nfe);
					installStatus.setTotalSteps(0);
				}
			} else {
				installStatus.setTotalSteps(0);
			}

			if (ciiFile != null && ciiFile.length() > 0) {
				installStatus.setCiiFile(ciiFile);
			} else {
				if (isProbablyOldCIIFile) {
					installStatus.setCiiFile(probableCIIFileName.toString());
				} else {
					installStatus.setCiiFile(NOT_AVAILABLE);
				}
			}

			if (loadableFile != null && loadableFile.length() > 0) {
				installStatus.setLoadableFile(loadableFile);
			} else {
				installStatus.setLoadableFile(NOT_AVAILABLE);
			}

			if (overallInstall != null && overallInstall.length() > 0) {
				installStatus.setOverallInstall(overallInstall);
			} else {
				installStatus.setOverallInstall(NOT_AVAILABLE);
			}

			if (LA != null && LA.length() > 0) {
				installStatus.setLA(LA);
			} else {
				installStatus.setLA(NOT_AVAILABLE);
			}

			installStatus.setProbablyOldCIIFile(isProbablyOldCIIFile);

			if (isDelete) {
				deleteFiles(installStatus);
			}
		} catch (FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: " + installLogPath + " is not available", fnfe);
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read " + installLogPath, ioe);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception occurred while reading: " + installLogPath, e);
		}
		return installStatus;
	}

	public ArrayList<InstallStatus> parseOtherInstallLog(final String fileName, final boolean isDelete) {
		boolean isSecondaryLru = false;
		boolean isBackupLru = false;
		ArrayList<InstallStatus> installStatusList = null;
		String installLogPath = null;
		boolean statesAlreadySetPresent = false;
		String statesAlreadySetValue = NOT_AVAILABLE;
		if (fileName.equals(UpdateService.SECONDARY_INSTALL_FILE)) {
			isSecondaryLru = true;
			installLogPath = getFilePath(updateService.getPathToInstallLog(), UpdateService.SECONDARY_INSTALL_FILE);
		} else if (fileName.equals(UpdateService.BACKUP_INSTALL_FILE)) {
			isBackupLru = true;
			installLogPath = getFilePath(updateService.getPathToInstallLog(), UpdateService.BACKUP_INSTALL_FILE);
		}

		if (installLogPath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Not a valid file name: " + fileName);
			}
			return installStatusList;
		}

		final File installLog = new File(installLogPath);

		if (!installLog.exists()) {
			if (updateService.isInfo()) {
				if (fileName.equals(UpdateService.SECONDARY_INSTALL_FILE)) {
					updateService.logInfo("Update Service: Log file missing for secondary LRU: " + installLogPath);
				} else if (fileName.equals(UpdateService.BACKUP_INSTALL_FILE)) {
					updateService.logInfo("Update Service: Log file missing for backup LRU: " + installLogPath);
				}
			}
			return installStatusList;
		}

		try {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Reading: " + installLogPath);
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(installLog)));
			String line = null;
			installStatusList = new ArrayList<InstallStatus>();

			while ((line = br.readLine()) != null) {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Log file entry: " + line);
				}
				if (line.length() > 0) {
					final StringTokenizer st = new StringTokenizer(line.trim(), "=");
					String key = null;
					String value = null;
					if (st.hasMoreTokens()) {
						key = st.nextToken().trim();

						if (st.hasMoreTokens()) {
							value = st.nextToken().trim();
						} else {
							value = "";
						}
					} else {
						key = "";
					}

					if (key.endsWith("LRU")) {
						InstallStatus installStatus = new InstallStatus();
						installStatus.setUpdateService(updateService);
						installStatus.setSecondaryLru(isSecondaryLru);
						installStatus.setBackupLru(isBackupLru);

						if (value != null && value.length() > 0) {
							installStatus.setLruType(value);
						} else {
							installStatus.setLruType(NOT_AVAILABLE);
						}
						installStatusList.add(installStatus);
					} else if (key.endsWith("UpdateTime")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setUpdateTime(value);
							} else {
								installStatus.setUpdateTime(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("TotalSteps")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								try {
									installStatus.setTotalSteps(Integer.parseInt(value));
								} catch (NumberFormatException nfe) {
									UnitManager.Logging.logSevere("Update Service: Log file: " + installLogPath, nfe);
									installStatus.setTotalSteps(0);
								}
							} else {
								installStatus.setTotalSteps(0);
							}
						}
					} else if (key.endsWith("UpdateCII")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setCiiFile(value);
							} else {
								installStatus.setCiiFile(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("UpdatePatch")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setLoadableFile(value);
							} else {
								installStatus.setLoadableFile(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("PartNumber")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setPartNumber(value);
							} else {
								installStatus.setPartNumber(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("BuildNumber")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setBuildNumber(value);
							} else {
								installStatus.setBuildNumber(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("ForceLoad")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setForceLoad(Boolean.parseBoolean(value));
							} else {
								installStatus.setForceLoad(false);
							}
						}
					} else if (key.endsWith("OverallInstall")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setOverallInstall(value);
							} else {
								installStatus.setOverallInstall(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("LA")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								installStatus.setLA(value);
							} else {
								installStatus.setLA(NOT_AVAILABLE);
							}
						}
					} else if (key.endsWith("StatesAlreadySet")) {
						InstallStatus installStatus = installStatusList.get(installStatusList.size() - 1);
						if (installStatus != null) {
							if (value != null && value.length() > 0) {
								statesAlreadySetPresent = true;
								statesAlreadySetValue = value;
								installStatus.setStatesAlreadySet(value);
							} else {
								installStatus.setStatesAlreadySet(NOT_AVAILABLE);
							}
						}
					}
				}
			}

			br.close();

			// Initialize all the variables that were not initialized while parsing
			// the log file
			if (installStatusList != null && installStatusList.size() > 0) {
				for (InstallStatus installStatus : installStatusList) {
					if (installStatus.getLruType() == null) {
						installStatus.setLruType(NOT_AVAILABLE);
					}
					if (installStatus.getUpdateTime() == null) {
						// UpdateTime should be always unique to make sure the
						// log file is not processed more than once by the
						// secondary LRU handlers like PPC Handler
						final Calendar c = Calendar.getInstance();
						final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss MM/dd/yy");
						sdf.format(c.getTime());
						installStatus.setUpdateTime(sdf.format(c.getTime()));
					}
					if (installStatus.getCiiFile() == null) {
						installStatus.setCiiFile(NOT_AVAILABLE);
					}
					if (installStatus.getLoadableFile() == null) {
						installStatus.setLoadableFile(NOT_AVAILABLE);
					}
					if (installStatus.getPartNumber() == null) {
						installStatus.setPartNumber(NOT_AVAILABLE);
					}
					if (installStatus.getBuildNumber() == null) {
						installStatus.setBuildNumber(NOT_AVAILABLE);
					}
					if (installStatus.getOverallInstall() == null) {
						// Older software generates installsecondary.txt and installback.txt
						// with no entry for OverallInstall in it
						installStatus.setOverallInstall("pass");
					}
					if (installStatus.getLA() == null) {
						installStatus.setLA(NOT_AVAILABLE);
					}
					if(statesAlreadySetPresent) {
						installStatus.setStatesAlreadySet(statesAlreadySetValue);
					} else if (installStatus.getStatesAlreadySet() == null) {
						installStatus.setStatesAlreadySet(NOT_AVAILABLE);
					}
				}
			}

			if (isDelete) {
				if (installStatusList != null && installStatusList.size() > 0) {
					for (InstallStatus installStatus : installStatusList) {
						deleteFiles(installStatus);
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: " + installLogPath + " is not available", fnfe);
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read " + installLogPath, ioe);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception occurred while reading: " + installLogPath, e);
		}
		return installStatusList;
	}

	private void deleteFiles(final InstallStatus installStatus) {
		if (updateService.isInfo()) {
			installStatus.printCurrentValues();
		}
		final String lruType = installStatus.getLruType();
		if (lruType == null) {
			UnitManager.Logging.logSevere("Update Service: LRU type is null. Cannot delete files.");
			return;
		}
		String ciiFile = null;
		if (installStatus.getCiiFile() != null && installStatus.getCiiFile().lastIndexOf(Consts.IOs.FILE_SEPARATOR) > 0) {
			ciiFile = installStatus.getCiiFile().substring(installStatus.getCiiFile().lastIndexOf(Consts.IOs.FILE_SEPARATOR) + 1);
		} else {
			ciiFile = installStatus.getCiiFile();
			if (ciiFile == null) {
				ciiFile = NOT_AVAILABLE;
			}
		}
		String loadableFile = null;
		if (installStatus.getLoadableFile() != null && installStatus.getLoadableFile().lastIndexOf(Consts.IOs.FILE_SEPARATOR) > 0) {
			loadableFile = installStatus.getLoadableFile().substring(installStatus.getLoadableFile().lastIndexOf(Consts.IOs.FILE_SEPARATOR) + 1);
		} else {
			loadableFile = installStatus.getLoadableFile();
			if (loadableFile == null) {
				loadableFile = NOT_AVAILABLE;
			}
		}

		// If the CII file that was just installed was not an old CII file
		// Then delete the old CII file, if present
		// The old CII file will be in the form of "lru.cii"
		// Example: psw.cii
		// The very first release for PSW had this type of file naming convention
		if (!installStatus.isProbablyOldCIIFile()) {
			final String probableCIIFileName = lruType + UpdateService.CII_EXTN;
			final String probableOldCIIPath = getFilePath(updateService.getPathToCii(), probableCIIFileName);
			final File probableOldCII = new File(probableOldCIIPath);
			if (probableOldCII.exists()) {
				if (probableOldCII.delete()) {
					isDeleted = true;
					if (updateService.isInfo()) {
						updateService.logInfo("Update Service: Deleted old probable CII file: " + probableOldCIIPath);
					}
				} else {
					UnitManager.Logging.logSevere("Update Service: Failed to delete the old probable CII file: " + probableOldCIIPath);
				}
			}
		}

		if (updateService.isInfo()) {
			updateService.logInfo("Update Service: Overall install status: " + installStatus.getOverallInstall());
		}
		// If the overall install status is NOT "pass", then delete
		// the newly installed files
		// Else delete the old files
		if (!installStatus.getOverallInstall().equalsIgnoreCase("pass")) {
			if (ciiFile != null && !ciiFile.equals(NOT_AVAILABLE) && ciiFile.length() > 0) {
				final String ciiFilePath = getFilePath(updateService.getPathToCii(), ciiFile);
				File cii = new File(ciiFilePath);

				if (cii.exists()) {
					if (cii.delete()) {
						isDeleted = true;
						if (updateService.isInfo()) {
							updateService.logInfo("Update Service: Deleted the new CII file: " + ciiFilePath);
						}
					} else {
						UnitManager.Logging.logSevere("Update Service: Failed to deleted the new CII file: " + ciiFilePath);
					}
				} else {
					if (updateService.isInfo()) {
						updateService.logInfo("Update Service: New CII file is not available: " + ciiFilePath);
					}
				}
			} else {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: New CII file is not available: " + ciiFile);
				}
			}

			if (loadableFile != null && !loadableFile.equals(NOT_AVAILABLE) && loadableFile.length() > 0) {
				final String loadableFilePath = getFilePath(updateService.getPathToLoadable(), loadableFile);
				File loadable = new File(loadableFilePath);

				if (loadable.exists()) {
					if (loadable.delete()) {
						isDeleted = true;
						if (updateService.isInfo()) {
							updateService.logInfo("Update Service: Deleted the new loadable file: " + loadableFilePath);
						}
					} else {
						UnitManager.Logging.logSevere("Update Service: Failed to deleted the new loadable file: " + loadableFilePath);
					}
				} else {
					if (updateService.isInfo()) {
						updateService.logInfo("Update Service: New loadable file is not available: " + loadableFilePath);
					}
				}
			} else {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: New loadable file is not available: " + loadableFile);
				}
			}
		} else {
			if (ciiFile != null && !ciiFile.equals(NOT_AVAILABLE) && ciiFile.length() > 0) {
				deleteWithExclude(lruType, updateService.getPathToCii(), ciiFile);
			} else {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: New CII file is not available: " + ciiFile);
				}
			}

			if (loadableFile != null && !loadableFile.equals(NOT_AVAILABLE) && loadableFile.length() > 0) {
				deleteWithExclude(lruType, updateService.getPathToLoadable(), loadableFile);
			} else {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: New loadable file is not available: " + loadableFile);
				}
			}
		}
	}

	private void deleteWithExclude(final String lruType, final String directoryPath, final String excludeFile) {
		// A given CII file or loadable file can follow one of the
		// two possible naming conventions:
		// Convention 1:
		// The file name can start the actual part number, followed by "_",
		// followed by the actual build number.
		// All files except for LCP follows this convention.
		// Example: 111-2222-333_44.cii
		// Convention 2:
		// The file name can start with its LRU type, followed by "_", followed by
		// the actual part number, followed by "_", followed by the actual build number.
		// Currently, only LCP follows this convention.
		// Example: lcp_111-2222-333444_55.cii
		String partNumberPattern = null;
		if (excludeFile.indexOf("_") > 0) {
			if (excludeFile.indexOf("_") == excludeFile.lastIndexOf("_")) {
				// Convention 1: File name contains only one "_"
				partNumberPattern = excludeFile.substring(0, excludeFile.indexOf("_") - 3);
			} else {
				// Convention 2: File name contains two "_"
				partNumberPattern = excludeFile.substring(excludeFile.indexOf("_") + 1, excludeFile.lastIndexOf("_") - 3);
			}
		} else {
			updateService.logInfo("Update Service: File name is not in expected format: " + excludeFile);
			return;
		}
		if (updateService.isInfo()) {
			updateService.logInfo("Update Service: pattern:       " + partNumberPattern);
			updateService.logInfo("Update Service: lruType:       " + lruType);
			updateService.logInfo("Update Service: directoryPath: " + directoryPath);
			updateService.logInfo("Update Service: excludeFile:   " + excludeFile);
		}

		// Obtain the list of files available.
		// Loop through all of the files and delete those files
		// that is of the same type with the currently installed file.
		// Retain only the newly installed file.
		final String excludeFileExtn = excludeFile.substring(excludeFile.lastIndexOf("."));
		final List<String> filesList = updateHandler.listFiles(directoryPath);
		if (filesList != null && filesList.size() > 0) {
			for (final String fileName : filesList) {
				final boolean isConvention1 = fileName.startsWith(partNumberPattern) && fileName.endsWith(excludeFileExtn) && !fileName.equals(excludeFile);
				final boolean isConvention2 = fileName.startsWith(lruType) && fileName.endsWith(excludeFileExtn) && !fileName.equals(excludeFile);
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: fileName:       " + fileName);
					updateService.logInfo("Update Service: isConvention1:  " + isConvention1);
					updateService.logInfo("Update Service: isConvention2:  " + isConvention2);
				}
				if (isConvention1 || isConvention2) {
					final String filePath = getFilePath(directoryPath, fileName);
					final File oldFile = new File(filePath);
					if (oldFile.exists()) {
						if (oldFile.delete()) {
							isDeleted = true;
							if (updateService.isInfo()) {
								updateService.logInfo("Update Service: Deleted the old file: " + filePath);
							}
						} else {
							UnitManager.Logging.logSevere("Update Service: Failed to delete the old file: " + filePath);
						}
					}
				}
			}
		} else {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: No files of type " + excludeFileExtn + " is available to delete at: " + directoryPath);
			}
		}
	}

	public void updateStatesAlreadySet(final String fileName, final String statesAlreadySet) {
		String installLogPath = getFilePath(updateService.getPathToInstallLog(), fileName);

		if (installLogPath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Not a valid file name: " + fileName);
			}
			return;
		}

		final File installLog = new File(installLogPath);

		if (!installLog.exists()) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Log file missing for secondary LRU: " + installLogPath);
			}
			return;
		}
		
		try {
			final FileWriter installFile = new FileWriter(installLog, true);
			final BufferedWriter writer = new BufferedWriter(installFile);
			final StringBuilder sb = new StringBuilder();
			
			sb.append("StatesAlreadySet = ");
			sb.append(statesAlreadySet);
			sb.append("\n");
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Writing to " + installLogPath + ": " + sb.toString());
			}
			writer.write(sb.toString());
			sb.delete(0, sb.length());
			writer.flush();
			writer.close();
			
			// Setting isDeleted to true will make the "sync" command
			// to be executed
			isDeleted = true;
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to write to " + installLogPath, ioe);
		}
	}

	private String getFilePath(final String directoryPath, final String fileName) {
		String filePath = null;
		if (directoryPath.endsWith(Consts.IOs.FILE_SEPARATOR)) {
			filePath = directoryPath + fileName;
		} else {
			filePath = directoryPath + Consts.IOs.FILE_SEPARATOR + fileName;
		}
		return filePath;
	}
	
	public HashMap<String, ParentCI> parseToBeInstalledFile() {
		HashMap<String, ParentCI> toBeInstalled = null;

		final String toBeInstalledFilePath = getFilePath(updateService.getPathToInstallLog(), UpdateService.TO_BE_INSTALLED_FILE);
		if (toBeInstalledFilePath == null) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: To be installed file: " + toBeInstalledFilePath + " not available");
			}
			return toBeInstalled;
		}

		final File toBeInstalledFile = new File(toBeInstalledFilePath);

		if (!toBeInstalledFile.exists()) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: To be installed file: " + toBeInstalledFilePath + " not available");
			}
			return toBeInstalled;
		}

		try {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Reading: " + toBeInstalledFilePath);
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(toBeInstalledFile)));
			String line = null;
			String lruType = null;
			toBeInstalled = new HashMap<String, ParentCI>();

			while ((line = br.readLine()) != null) {
				if (updateService.isInfo()) {
					updateService.logInfo("Update Service: Download list file entry: " + line);
				}
				if (line.length() > 0) {
					final StringTokenizer st = new StringTokenizer(line.trim(), "=");
					String key = null;
					String value = null;
					if (st.hasMoreTokens()) {
						key = st.nextToken().trim();

						if (st.hasMoreTokens()) {
							value = st.nextToken().trim();
						} else {
							value = "";
						}
					} else {
						key = "";
					}

					if (key.equalsIgnoreCase("LRU")) {
						if (value != null && value.length() > 0) {
							ParentCI parentCI = new ParentCI();
							parentCI.setLruType(value);
							toBeInstalled.put(value, parentCI);
							lruType = value;
						}
					} else if (key.equalsIgnoreCase("CPN")) {
						if (lruType!= null && value != null && value.length() > 0) {
							ParentCI parentCI = toBeInstalled.get(lruType);
							if(parentCI != null) {
								parentCI.setCpn(value);
							}
						}
					} else if (key.equalsIgnoreCase("BN")) {
						if (lruType!= null && value != null && value.length() > 0) {
							ParentCI parentCI = toBeInstalled.get(lruType);
							if(parentCI != null) {
								parentCI.setBuildNumber(value);
							}
						}
					} else if (key.equalsIgnoreCase("CII")) {
						if (lruType!= null && value != null && value.length() > 0) {
							ParentCI parentCI = toBeInstalled.get(lruType);
							if(parentCI != null) {
								parentCI.setCiiFileName(value);
							}
						}
					} else if (key.equalsIgnoreCase("Parent")) {
						if (lruType!= null && value != null && value.length() > 0) {
							ParentCI parentCI = toBeInstalled.get(lruType);
							if(parentCI != null) {
								parentCI.setParent811FileName(value);
							}
						}
					}
				}
			}
			br.close();
		} catch (FileNotFoundException fnfe) {
			UnitManager.Logging.logSevere("Update Service: " + toBeInstalledFilePath + " is not available", fnfe);
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read " + toBeInstalledFilePath, ioe);
		} catch (final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Exception occurred while reading: " + toBeInstalledFilePath, e);
		}

		return toBeInstalled;
	}
}
