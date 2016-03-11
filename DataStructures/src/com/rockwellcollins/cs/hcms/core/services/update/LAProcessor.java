package com.rockwellcollins.cs.hcms.core.services.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.update.messages.LAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;

/**
 * The Class LAProcessor processes any Load Announcement that is received and
 * decides on what to be installed. It checks for the hardware compatibility and
 * LCP compatibility. The compatibility check can be disabled by setting the
 * "is disable compatibility" flag while sending the LA Message. If the
 * compatibility check is on it rejects the new software if they are not
 * hardware compatible or LCP compatibile. If the same LA message comes
 * continuously, it will process them only once. It saves the timestamp of
 * the LA Message when it receives it for the first time. It does a comparision
 * on old and new CII files to decide on what to install with the help of
 * ConformityChecker. The comparision happens only when the LA message says
 * force load flag is false. If the force load flag is set to true, there will
 * be no comparision among the CII files and everything in the new CII file
 * will be installed.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * @see ConformityChecker
 * @see ParentCI
 * @see Installer
 * @see InstallListInfo
 */
public class LAProcessor {
	
	private UpdateService updateService;

	private SAMessage sa;

	private String originalTimeOfLA;
	
	private String pathToLA;
	
	private ConformityChecker conformityChecker;
	
	private HardwareInfo hardwareDetails;
	
	private HashMap<String, ParentCI> parentCIs;
	
	private List<String> primaryPackagesList;
	
	private List<String> secondaryLruList;
	
	private List<String> backupLruList;
	
	/** The Constant LA_PROCESS_COMPLETE. */
	public static final String LA_PROCESS_COMPLETE = "LA process complete: ";

	/**
	 * Sets the conformity checker.
	 * 
	 * @param conformityChecker the new conformity checker
	 */
	public void setConformityChecker(final ConformityChecker conformityChecker) {
		this.conformityChecker = conformityChecker;
	}

	/**
	 * Sets the hardware details.
	 * 
	 * @param hardwareDetails the new hardware details
	 */
	public void setHardwareDetails(final HardwareInfo hardwareDetails) {
		this.hardwareDetails = hardwareDetails;
	}

	/**
	 * Sets the parent CIs.
	 * 
	 * @param parentCIs the parent CIs
	 */
	public void setParentCIs(final HashMap<String, ParentCI> parentCIs) {
		this.parentCIs = parentCIs;
	}
	
	/**
	 * Sets the primary packages list.
	 * 
	 * @param primaryPackagesList the new primary packages list
	 */
	public void setPrimaryPackagesList(List<String> primaryPackagesList) {
		this.primaryPackagesList = primaryPackagesList;
	}

	/**
	 * Sets the backup lru list.
	 * 
	 * @param backupLruList the new backup lru list
	 */
	public void setBackupLruList(final List<String> backupLruList) {
		this.backupLruList = backupLruList;
	}

	/**
	 * Sets the secondary lru list.
	 * 
	 * @param secondaryLruList the new secondary lru list
	 */
	public void setSecondaryLruList(final List<String> secondaryLruList) {
		this.secondaryLruList = secondaryLruList;
	}

	/**
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(final UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Gets the update service.
	 * 
	 * @return the update service
	 */
	public UpdateService getUpdateService() {
		return updateService;
	}

	/**
	 * Checks whether the LA message is a new one or a already processed
	 * one by looking at the timestamp. Also checks whether the LA message
	 * is meant for this IP address. It simply rejects the LA message that
	 * is not for this IP address.
	 * 
	 * @param la the LAMessage
	 */
	public void processLAMessage(final LAMessage la) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Received a LA Message");
		}
		boolean isInstall = false;

		// Check whether the LA message is a new one or already a processed one
		if (la.getOriginalTime().equals(la.getCurrentTime())) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Received a new LA Message");
				la.printCurrentValues();
			}
			isInstall = true;
		} else if (originalTimeOfLA != null && originalTimeOfLA.equals(la.getOriginalTime())) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Already processed the LA Message");
			}
			isInstall = false;
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Missed the LA Message earlier and received it now");
				la.printCurrentValues();
			}
			isInstall = true;
		}

		if (isInstall) {
			sendSA(0, "Processing the LA Message", "Received a new LA Message", false);
			
			if (updateService.isInstalling()) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Installation already in progress. Attempting to abort it.");
				}
				sendSA(0, "Installation already in progress", "Attempting to abort the already running installation process", false);
				Installer installer = updateService.getInstaller();
				Thread installerThread = updateService.getInstallerThread();
				if(installer == null || installerThread == null) {
					sendSA(0, "Installation already in progress", "Failed to abort - Cannot load any software now", false);
					return;
				}
				installer.stopInstaller();
				while (installerThread.isAlive()) {
					try {
						Thread.sleep(1000);
						if (installer.isPointOfNoReturn()) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Installer reached the point of no return");
							}
							sendSA(0, "Installation already in progress", LA_PROCESS_COMPLETE + "Installer reached the point of no return", true);
							return;
						}
					} catch (final InterruptedException e) {
						UnitManager.Logging.logSevere(e);
					}
				}
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Aborted the already running installation process");
				}
				sendSA(0, "Installation already in progress", "Aborted the already running installation process", false);
			}

			originalTimeOfLA = la.getOriginalTime();

			writeOriginalTime(pathToLA, UpdateService.LA_FILE);

			boolean isMyIp = false;

			final String myIpAddress = hardwareDetails.getIpAddress();

			final String ipAddressToInstall = la.getLruIpAddress();

			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: LA Message is for IP: " + ipAddressToInstall);
				updateService.logInfo("Update Service: Local IP Address:     " + myIpAddress);
			}

			// Check whether the LA message is destined only for the current LRU's IP or for every LRU
			if (ipAddressToInstall != null && ipAddressToInstall.length() > 0) {
				if (myIpAddress.equals(ipAddressToInstall)) {
					isMyIp = true;
				} else {
					isMyIp = false;
				}
			} else {
				isMyIp = true;
			}

			if (isMyIp == true) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Have to process the LA Message");
				}

				final boolean forceLoadFlag = la.isForceLoadFlag();

				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Force Load Flag: " + forceLoadFlag);
				}

				final HashMap<String, ParentCI> newParentCIs = la.getParentCIs();

				// Check whether Parent CIs are available in the LA Message
				if (newParentCIs == null || newParentCIs.size() == 0) {
					if (la.isSelfLoadDisableFlag()) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Disabling the self load process");
						}
						updateService.setSelfLoadFlag(false);
						sendSA(0, "Nothing to install", LA_PROCESS_COMPLETE + "Disabled the Self Load process", true);
					} else {
						UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: Not able to process LA Message. There are no Parent CIs in the LA message."));
						sendSA(0, "Nothing to install", LA_PROCESS_COMPLETE + "Invalid LA Message - No Parent CIs in the LA message", true);
					}
				} else {
					install(la);
				}
			} else {
				sendSA(0, "Nothing to install", LA_PROCESS_COMPLETE + "LA Message is not for this IP", true);
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: LA: LA Message is not for this IP - No need to install the new software");
				}
			}
		} else {
			if (!updateService.isInstalling()) {
				sendSA(0, "Nothing to install", LA_PROCESS_COMPLETE + "Already processed the LA Message", false);
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: LA: Already processed the LA Message - Nothing new to install");
				}
			}
		}
	}

	/**
	 * Processes the LA message by looking through the Parent CIS and checks
	 * for the hardware and LCP compatibility. It also does the comparision of
	 * CII files if the force load flag is false and finally decides what needs 
	 * to be installed and starts off the installation process. It simply returns
	 * back if nothing is there to install.
	 * 
	 * @param la the LAMessage
	 */
	public void install(final LAMessage la) {
		final HashMap<String, ParentCI> newParentCIs = la.getParentCIs();

		// Check any of the LRU software needs to be downloaded
		boolean isCurrentLru = false;
		for (final String lruType : newParentCIs.keySet()) {
			if (hardwareDetails.getDeviceType().equals(lruType) || primaryPackagesList.contains(lruType) || secondaryLruList.contains(lruType) || backupLruList.contains(lruType)) {
				isCurrentLru = true;
				break;
			}
		}

		// Check any of the LCP types needs to be downloaded
		boolean isLcp = false;
		if(newParentCIs.containsKey(updateService.getLCPType())) {
			// LCP for the local LRU is available to load
			isLcp = true;
		} else {
			for(final String backupLru : backupLruList) {
				// Check whether LCP for any backup LRU is available to load
				if(parentCIs.containsKey(backupLru)) {
					// Load the LCP only if the corresponding backup LRU is 
					// Already downloaded and saved as a backup
					String backupLCPType = updateService.getLruToLCPTypes().get(backupLru);
					if(backupLCPType == null) {
						backupLCPType = UpdateService.LCP_TYPE;
					}
					if(newParentCIs.containsKey(backupLCPType)) {
						isLcp = true;
						break;
					}
				}
			}
		}
		
		if(isCurrentLru || isLcp) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: LA: Processing the LA Message");
			}
			sendSA(0, "Processing the LA Message", "Checking which LRU files needs to be installed", false);
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: LA: Finished processing the LA Message : No known LRU types to install");
			}
			sendSA(0, "Nothing to install", LA_PROCESS_COMPLETE + "No known LRU types to install", true);
			return;
		}

		if (isLcp && !isCurrentLru) {
			boolean isLcpCompatible = true;
			String lcpFail = null;
			if(!la.isDisableCompatibility()) {
				if(newParentCIs.containsKey(updateService.getLCPType())) {
					isLcpCompatible = checkLcpCompatibility(hardwareDetails.getDeviceType(), newParentCIs.get(updateService.getLCPType()));
				}
				if(isLcpCompatible) {
					for(final String backupLru : backupLruList) {
						if(parentCIs.containsKey(backupLru)) {
							String backupLCPType = updateService.getLruToLCPTypes().get(backupLru);
							if(backupLCPType == null) {
								backupLCPType = UpdateService.LCP_TYPE;
							}
							if(newParentCIs.containsKey(backupLCPType)) {
								isLcpCompatible = checkLcpCompatibility(backupLru, newParentCIs.get(backupLCPType));
								if(!isLcpCompatible) {
									lcpFail = backupLCPType;
									break;
								}
							}
						}
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: LA: Disable compatibility check - TRUE");
				}
				isLcpCompatible = true;
			}
			
			if (isLcpCompatible) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: LA: LCP compatibility check - PASS");
				}
				sendSA(0, "Processing the LA Message", "LCP compatibility check - PASS", false);
				checkConformity(la, newParentCIs);
			} else {
				UnitManager.Logging.logSevere("Update Service: LA: Cannot install the new software. LCP compatibility check: FAIL for: " + lcpFail);
				sendSA(0, "Cannot install the new software", LA_PROCESS_COMPLETE + "LCP compatibility check - FAIL for: " + lcpFail, true);
			}
		} else if (isCurrentLru) {
			boolean isHardwareCompatible;
			if(!la.isDisableCompatibility()) {
				// Do the hardware compatibility for all LRUs and not just the local LRU
				isHardwareCompatible = checkHardwareCompatibility(newParentCIs);
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: LA: Disable compatibility check - TRUE");
				}
				isHardwareCompatible = true;
			}
			
			if (isHardwareCompatible) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: LA: Hardware compatibility check - PASS");
				}
				sendSA(0, "Processing the LA Message", "Hardware compatibility check - PASS", false);
				checkConformity(la, newParentCIs);
			} else {
				UnitManager.Logging.logSevere("Update Service: LA: Cannot install the new software. LA Message hardware compatibility check: FAIL.");
				sendSA(0, "Cannot install the new software", LA_PROCESS_COMPLETE + "Hardware compatibility check: FAIL.", true);
			}
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: LA: Finished processing the LA Message : No known LRU types to install");
			}
			sendSA(0, "Nothing to install", LA_PROCESS_COMPLETE + "No known LRU types to install", true);
			return;
		}
	}

	/**
	 * Checks the LCP compatibility.
	 * 
	 * @param lcpParentCI the lcp parent CI
	 * 
	 * @return true, if successful
	 */
	private boolean checkLcpCompatibility(final String lruType, final ParentCI lcpParentCI) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Checking LCP compatibility");
		}
		
		if(lcpParentCI == null) {
			UnitManager.Logging.logSevere("Update Service: The new LCP Parent CI is not available");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		
		if(parentCIs == null || parentCIs.size() <= 0) {
			UnitManager.Logging.logSevere("Update Service: There are no installed Parent CIs available");
			UnitManager.Logging.logSevere("Update Service: Cannot check the LCP compatibility");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		
		final ParentCI parentCI = parentCIs.get(lruType);
		if(parentCI == null) {
			UnitManager.Logging.logSevere("Update Service: There is no installed Parent CI available for: " + lruType);
			UnitManager.Logging.logSevere("Update Service: Cannot check the LCP compatibility");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		final String expected = lcpParentCI.getReleaseNumber();
		final String actual = parentCI.getReleaseNumber();
		
		if(expected == null || actual == null) {
			UnitManager.Logging.logSevere("Update Service: Release number(s) are null");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		
		int eLastIndexOfDot = expected.lastIndexOf(".");
		int aLastIndexOfDot = actual.lastIndexOf(".");
		
		if(eLastIndexOfDot <= 0 || aLastIndexOfDot <= 0) {
			UnitManager.Logging.logSevere("Update Service: Release numbers are not in expected format");
			UnitManager.Logging.logSevere("Update Service: Returns as LCP incompatible");
			return false;
		}
		
		String expectedSub = expected.substring(0, eLastIndexOfDot);
		String actualSub = actual.substring(0, aLastIndexOfDot);
		int exceptedNumber = 0;
		try {
			exceptedNumber = Integer.parseInt(expectedSub.substring(0, 1));
		} catch(NumberFormatException nfe) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Cannot convert the release number: " + expected + " to an int");
			}
		}
		// Backward compatibility with Release 3.0 and above
		if(!expectedSub.equals(actualSub) && exceptedNumber <= 2) {
			UnitManager.Logging.logSevere("Update Service: The new LCP is incompatible with the installed software: " + lruType);
			UnitManager.Logging.logSevere("Update Service: Expected: " + expectedSub + " : Actual: " + actualSub);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks the hardware compatibility.
	 * 
	 * @param newParentCIs the new parent CIs
	 * 
	 * @return true, if successful
	 */
	private boolean checkHardwareCompatibility(final HashMap<String, ParentCI> newParentCIs) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Checking hardware compatibility");
		}
		
		for(final Entry<String, ParentCI> newParentCIEntry : newParentCIs.entrySet()) {
			final String lruType = newParentCIEntry.getKey();
			final ParentCI newParentCI = newParentCIEntry.getValue();
			
			if(lruType.equals(updateService.getLCPType()) || secondaryLruList.contains(lruType)) {
				continue;
			}
			
			if(newParentCI == null) {
				UnitManager.Logging.logSevere("Update Service: New Parent CI is null for: " + lruType);
				UnitManager.Logging.logSevere("Update Service: Returns as hardware incompatible");
				return false;
			}
			
			final List<String> hardwarePartNumbers = newParentCI.getHardwarePartNumbers();
			if(hardwarePartNumbers != null && hardwarePartNumbers.size() > 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: New software for " + lruType + " is compatible only with selected number of hardware: " + hardwarePartNumbers.size());
					for(String partNumber : hardwarePartNumbers) {
						updateService.logInfo("Update Service: Hardware: " + partNumber + " part number");
					}
				}
				HashMap<String, List<String>> hwPartNumbers = updateService.getHwPartNumbers();
				if(hwPartNumbers == null || hwPartNumbers.size() < 0) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Installed hardware part numbers are not available for any LRU");
						updateService.logInfo("Update Service: Including only the current LRU's hardware part number");
					}
					if(hardwareDetails.getHardwarePartNumber() == null || hardwareDetails.getHardwarePartNumber().length() <= 0) {
						UnitManager.Logging.logSevere("Update Service: Installed hardware part numbers are not available for any LRU including the local LRU");
						UnitManager.Logging.logSevere("Update Service: Returns as hardware incompatible");
						return false;
					}
					hwPartNumbers = new HashMap<String, List<String>>();
					List<String> partNumbers = new ArrayList<String>();
					partNumbers.add(hardwareDetails.getHardwarePartNumber());
					hwPartNumbers.put(hardwareDetails.getDeviceType(), partNumbers);
				}
				
				List<String> localHwPartNumbers = null;
				if(primaryPackagesList.contains(lruType)) {
					// For primary packages use the local LRU's part numbers
					localHwPartNumbers = hwPartNumbers.get(hardwareDetails.getDeviceType());
				} else {
					localHwPartNumbers = hwPartNumbers.get(lruType);
				}
				
				if(localHwPartNumbers == null || localHwPartNumbers.size() < 0) {
					UnitManager.Logging.logSevere("Update Service: Installed hardware part numbers are not available for: " + lruType);
					UnitManager.Logging.logSevere("Update Service: Returns as hardware incompatible");
					return false;
				}

				for(final String localHwPartNumber : localHwPartNumbers) {
					if(!hardwarePartNumbers.contains(localHwPartNumber)) {
						UnitManager.Logging.logSevere("Update Service: Installed Hardware: " + localHwPartNumber + " is not present in the hardware part number list");
						return false;
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: New software is compatible for all hardware: " + lruType);
				}
			}
		}
		return true;
	}

	/**
	 * Checks the conformity of the CII files with the help of 
	 * ConformityChecker.
	 * 
	 * @param la the la
	 * @param newParentCIs the new parent C is
	 * 
	 * @return true, if successful
	 */
	private boolean checkConformity(final LAMessage la, final HashMap<String, ParentCI> newParentCIs) {
		sendSA(0, "Processing the LA Message", "Checking against the already installed CII file", false);
		final InstallListInfo installList = conformityChecker.getInstallList(newParentCIs, la.isForceLoadFlag());
		if (installList != null && installList.getParentCIsToInstall().size() > 0) {
			updateService.laInstall(la, installList);
			return false;
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: LA: Finished processing the LA Message : Nothing to install");
			}
			// Revert the state manager back to its default mode
			// If the loading was started via a media the LRU will switch to
			// LOCAL mode while waiting for other LRUs to copy the files
			if(updateService.getStateManager() != null) {
				if(updateService.getStateManager().getMode() != updateService.getStateManager().getDefaultMode()) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Setting the State Manager to its default mode: " + updateService.getStateManager().getDefaultMode());
					}
					updateService.getStateManager().setMode(updateService.getStateManager().getDefaultMode());
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: State Manager is already in its default mode: " + updateService.getStateManager().getDefaultMode());
					}
				}
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: State Manager reference is null");
				}
			}
			sendSA(0, "Nothing to install", LA_PROCESS_COMPLETE + "Already have same files loaded", true);
			return true;
		}
	}

	/**
	 * Sends Status Announcement periodically to report the
	 * current status.
	 * 
	 * @param stepNumber the step number
	 * @param subStepName the sub step name
	 */
	private void sendSA(final int stepNumber, final String stepName, final String subStepName, final boolean isFinal) {
		if (sa == null) {
			sa = new SAMessage();
		}
		sa.setUpdateService(updateService);
		sa.setIpAddress(hardwareDetails.getIpAddress());
		sa.setLruType(hardwareDetails.getDeviceType());
		sa.setLruInstance(updateService.getLruInstance());
		sa.setTotalSteps(0);
		sa.setStepNumber(stepNumber);
		sa.setStepName(stepName);
		sa.setSubStepName(subStepName);
		if(subStepName.startsWith(LA_PROCESS_COMPLETE)) {
			sa.setSubStepPercentage("100%");
		} else {
			sa.setSubStepPercentage("0%");
		}
		sa.setStatusAvailable(true);
		sa.setRunning(false);
		if(!isFinal) {
			updateService.sendStatus(sa);
		} else {
			// Send the final status more than once
			int duration = updateService.getFinalStatusDuration();
			int frequency = updateService.getFinalStatusFrequency();
			int timeElapsed = 0;
			while(timeElapsed < duration) {
				updateService.sendStatus(sa);
				try {
					Thread.sleep(frequency);
				} catch (InterruptedException e) {
					UnitManager.Logging.logSevere("Update Service: Error while sending the final install status");
				}
				timeElapsed = timeElapsed + frequency;
			}
		}
	}

	/**
	 * Writes and saves the original timestamp of the LA message.
	 * 
	 * @param laFileName the la file name
	 * @param pathToLA the path to LA
	 */
	private void writeOriginalTime(final String pathToLA, final String laFileName) {
		String laFilePath = null;
		try {
			if (pathToLA.endsWith(Consts.IOs.FILE_SEPARATOR)) {
				laFilePath = pathToLA + laFileName;
			} else {
				laFilePath = pathToLA + Consts.IOs.FILE_SEPARATOR + laFileName;
			}
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Writing originalTimeOfLA to: " + laFilePath);
			}

			final FileWriter laFile = new FileWriter(laFilePath, false);

			final PrintWriter writer = new PrintWriter(laFile);

			writer.write(originalTimeOfLA);

			writer.flush();
			writer.close();
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to write to: " + laFilePath, ioe);
		}
		updateService.getUpdateHandler().flushFiles(pathToLA, true);
	}

	/**
	 * Reads original time of the last LA message received during
	 * the startup.
	 * 
	 * @param laFileName the la file name
	 * @param pathToLA the path to LA
	 */
	public void readOriginalTime(final String pathToLA, final String laFileName) {
		this.pathToLA = pathToLA;
		
		String laFilePath = null;
		try {
			if (pathToLA.endsWith(Consts.IOs.FILE_SEPARATOR)) {
				laFilePath = pathToLA + laFileName;
			} else {
				laFilePath = pathToLA + Consts.IOs.FILE_SEPARATOR + laFileName;
			}

			final File la = new File(laFilePath);

			if (la.exists()) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Reading originalTimeOfLA from: " + laFilePath);
				}

				final FileReader laFile = new FileReader(la);

				final BufferedReader reader = new BufferedReader(laFile);

				final String line = reader.readLine();
				if(line != null && line.length() > 0) {
					originalTimeOfLA = line.trim();
				} else {
					originalTimeOfLA = null;
				}

				reader.close();
			} else {
				originalTimeOfLA = null;
			}
		} catch (final IOException ioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to read from: " + laFilePath, ioe);
		}
	}
}
