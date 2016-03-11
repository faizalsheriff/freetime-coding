package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.update.messages.CAMessage;
import com.rockwellcollins.cs.hcms.core.services.update.messages.EAMessage;

/**
 * The Class ElectionProcessor processes all the votes received via the CA
 * Messages for various LRU types and LCP. It elects a prevailing version of
 * software for every LRU type and a prevailing version of LCP. Those LRUs
 * that have the prevailing software and LCP are the winners. Finally it
 * checks whether the current LRU itself is a winner. If not it compares
 * the prevailing version with the existing installed version for
 * any changes in the CII files. If any are there, it makes an attempt to
 * load the prevailing version from the other LRUs. This entire process of
 * voting will be kicked off only if self load is enabled.
 *
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * @see ConformityChecker
 * @see ParentCI
 * @see ChildCI
 *
 */
public class ElectionProcessor {

	private UpdateService updateService;

	private ConformityChecker conformityChecker;

	private HashMap<String, ParentCI> parentCIs;

	private HashMap<String, HashMap<String, List<String>>> votes;

	private HashMap<String, List<String>> lcpVotes;

	private HashMap<String, CAMessage> voters;

	private HardwareInfo hardwareDetails;
	
	private List<String> primaryPackagesList;

	private List<String> secondaryLruList;

	private List<String> backupLruList;

	private boolean isElectionAborted;

	private EAMessage eaMessage;

	private int logPosition;

	private StateManager stateManager;

	/**
	 * Sets the update service.
	 *
	 * @param updateService the new update service
	 */
	public void setUpdateService(final UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Sets the conformity checker.
	 *
	 * @param conformityChecker the new conformity checker
	 */
	public void setConformityChecker(final ConformityChecker conformityChecker) {
		this.conformityChecker = conformityChecker;
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
	 * Sets the voters.
	 *
	 * @param voters the voters
	 */
	public void setVoters(final HashMap<String, CAMessage> voters) {
		this.voters = voters;
	}

	/**
	 * Sets the votes.
	 *
	 * @param votes the votes
	 */
	public void setVotes(final HashMap<String, HashMap<String, List<String>>> votes) {
		this.votes = votes;
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
	 * Sets the hardware details.
	 *
	 * @param hardwareDetails the new hardware details
	 */
	public void setHardwareDetails(final HardwareInfo hardwareDetails) {
		this.hardwareDetails = hardwareDetails;
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
	 * Sets the secondary lru list.
	 *
	 * @param secondaryLruList the new secondary lru list
	 */
	public void setSecondaryLruList(final List<String> secondaryLruList) {
		this.secondaryLruList = secondaryLruList;
	}

	/**
	 * Sets the state manager.
	 *
	 * @param stateManager the new state manager
	 */
	public void setStateManager(StateManager stateManager) {
		this.stateManager = stateManager;
	}

	/**
	 * Process all the votes received and finally checks the election result.
	 */
	public void processVotes() {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Starting the election with the votes received");
			updateService.logInfo("Update Service: isElectionAborted: " + isElectionAborted);
			updateService.logInfo("Update Service: logPosition: " + logPosition);
		}
		isElectionAborted = false;
		electSwPartNumbers();
		if(!isElectionAborted) {
			electLcpPartNumber();
		}
		if(!isElectionAborted) {
			checkElectionResult();
		}
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: processVotes completed");
			updateService.logInfo("Update Service: isElectionAborted: " + isElectionAborted);
			updateService.logInfo("Update Service: logPosition: " + logPosition);
		}
	}

	/**
	 * Elects a prevailing software part number for every LRU type.
	 */
	private void electSwPartNumbers() {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Electing software part numbers");
		}
		// Loop for every LRU type
		for(final Entry<String, HashMap<String, List<String>>> votesEntry : votes.entrySet()) {
			final String lruType = votesEntry.getKey();
			final HashMap<String, List<String>> lruVotes = votesEntry.getValue();

			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Electing software part numbers for: " + lruType);
			}

			if(lruVotes == null || lruVotes.size() <= 0) {
				isElectionAborted = true;
				if(logPosition != 1) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 1");
					}
					logPosition = 1;
					UnitManager.Logging.logSevere("Update Service: Failed to elect the software part number");
					UnitManager.Logging.logSevere("Update Service: Aborting the election");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}

			int maxCount = 0;
			String prevailingPN = null;

			// Loop for every part number
			// After the loop there should be only part number in the HashMap
			// All other losing part numbers should be removed from the HashMap
			Iterator<Entry<String, List<String>>> partNumbers = lruVotes.entrySet().iterator();
			while(partNumbers.hasNext()) {
				final Entry<String, List<String>> partNumbersEntry = partNumbers.next();
				final String partNumber = partNumbersEntry.getKey();
				final List<String> votesReceived = partNumbersEntry.getValue();
				int votesSize = 0;
				if(votesReceived != null) {
					votesSize = votesReceived.size();
				}
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Part number: " + partNumber + " : Votes size: " + votesSize);
				}
				if(votesSize > maxCount) {
					maxCount = votesReceived.size();
					prevailingPN = partNumber;
				} else if(votesSize != 0 && votesSize == maxCount) {
					prevailingPN = maxPartNumber(prevailingPN, partNumber);
				} else {
					partNumbers.remove();
					lruVotes.remove(partNumber);
				}
			}
			// Remove all the part numbers except the prevailingPN
			if(prevailingPN != null && prevailingPN.length() > 0) {
				partNumbers = lruVotes.entrySet().iterator();
				while(partNumbers.hasNext()) {
					final Entry<String, List<String>> partNumbersEntry = partNumbers.next();
					final String partNumber = partNumbersEntry.getKey();
					if(!partNumber.equals(prevailingPN)) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Removing: " + partNumber + " : as it is not a prevailing part number: " + prevailingPN);
						}
						partNumbers.remove();
						lruVotes.remove(partNumber);
					}
				}
			}

			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: LRU: " + lruType + " : Part number: " + prevailingPN + " : Max count: " + maxCount + " : Part numbers size: " + lruVotes.size());
			}
			// maxCount should be greater than 0
			// prevailingPN should not null and should not be empty
			// lruVotes size should be one as there can be only one winner
			if(maxCount == 0 || prevailingPN == null || prevailingPN.length() <= 0 || lruVotes.size() != 1) {
				isElectionAborted = true;
				if(logPosition != 2) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 2");
					}
					logPosition = 2;
					UnitManager.Logging.logSevere("Update Service: Failed to elect the software part number");
					UnitManager.Logging.logSevere("Update Service: Aborting the election");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}
			electSwBuildNumbers(lruType, lruVotes, prevailingPN);
			if(isElectionAborted) {
				return;
			}
		}
		if(logPosition == 1 || logPosition == 2) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
			}
			logPosition = 0;
		}
	}

	/**
	 * Elects a prevailing software build number for every LRU type.
	 * The voting for build number will be done among the winners of part numbers.
	 *
	 * @param lruType the lru type
	 * @param lruVotes the lru votes
	 * @param prevailingPN the prevailing PN
	 */
	private void electSwBuildNumbers(final String lruType, final HashMap<String, List<String>> lruVotes, final String prevailingPN) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Electing software build numbers");
		}
		final List<String> ipAddresses = lruVotes.get(prevailingPN);
		final Iterator<String> ipAddressesItr = ipAddresses.iterator();

		final HashMap<String, List<String>> buildVotes = new HashMap<String, List<String>>();
		int maxCount = 0;
		String prevailingBN = null;

		while(ipAddressesItr.hasNext()) {
			final String ipAddress = ipAddressesItr.next();
			final CAMessage ca = voters.get(ipAddress);
			final ParentCI parentCI = ca.getParentCIs().get(lruType);

			if(parentCI == null) {
				isElectionAborted = true;
				if(logPosition != 3) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 3");
					}
					logPosition = 3;
					UnitManager.Logging.logSevere("Update Service: Failed to elect the build number as Parent CI is null for: " + lruType);
					UnitManager.Logging.logSevere("Update Service: Aborting the election");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}
			if(logPosition == 3) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
				}
				logPosition = 0;
			}
			final String buildNumber = parentCI.getBuildNumber();
			List<String> votesReceived = buildVotes.get(buildNumber);
			if(votesReceived == null) {
				votesReceived = new ArrayList<String>();
				buildVotes.put(buildNumber, votesReceived);
			}
			votesReceived.add(ipAddress);

			final int votesSize = votesReceived.size();
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Build number: " + buildNumber + " : Votes size: " + votesSize);
			}
			if(votesSize > maxCount) {
				maxCount = votesSize;
				prevailingBN = buildNumber;
			} else if(votesSize != 0 && votesSize == maxCount) {
				prevailingBN = maxBuildNumber(prevailingBN, buildNumber);
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Not the max build number: " + buildNumber);
				}
			}
		}

		// maxCount should be greater than 0
		// prevailingBN should not null and should not be empty
		if(maxCount == 0 || prevailingBN == null || prevailingBN.length() <= 0) {
			isElectionAborted = true;
			if(logPosition != 4) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 4");
				}
				logPosition = 4;
				UnitManager.Logging.logSevere("Update Service: Failed to elect the software build number");
				UnitManager.Logging.logSevere("Update Service: Aborting the election");
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Already logged: " + logPosition);
				}
			}
			return;
		}
		if(logPosition == 4) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
			}
			logPosition = 0;
		}
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: LRU: " + lruType + " : Build number: " + prevailingBN + " : Max count: " + maxCount);
		}

		final List<String> winners = buildVotes.get(prevailingBN);

		// winners should not be null and the size should be greater than zero
		if(winners == null || winners.size() <= 0) {
			isElectionAborted = true;
			if(logPosition != 5) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 5");
				}
				logPosition = 5;
				UnitManager.Logging.logSevere("Update Service: Failed to elect the software build number");
				UnitManager.Logging.logSevere("Update Service: Aborting the election");
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Already logged: " + logPosition);
				}
			}
			return;
		}
		if(logPosition == 5) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
			}
			logPosition = 0;
		}
		lruVotes.put(prevailingPN, winners);
	}

	/**
	 * Elects a prevailing LCP part number. The voting will be done among the
	 * winners of both prevailing software part number and software build number.
	 */
	private void electLcpPartNumber() {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Electing LCP part number");
		}
		final HashMap<String, List<String>> partNumberVotes = new HashMap<String, List<String>>();
		int maxCount = 0;
		String prevailingPN = null;

		boolean isRegularLCPFound = false;
		for(final Entry<String, HashMap<String, List<String>>> votesEntry : votes.entrySet()) {
			final String lruType = votesEntry.getKey();
			if(secondaryLruList.contains(lruType) || primaryPackagesList.contains(lruType)) {
				continue;
			}

			final HashMap<String, List<String>> lruVotes = votesEntry.getValue();
			if(lruVotes == null || lruVotes.size() != 1 ) {
				isElectionAborted = true;
				if(logPosition != 6) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 6");
					}
					logPosition = 6;
					UnitManager.Logging.logSevere("Update Service: Failed to elect the LCP part number");
					UnitManager.Logging.logSevere("Update Service: 0 or multiple votes are available for: " + lruType);
					UnitManager.Logging.logSevere("Update Service: Aborting the election");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}
			if(logPosition == 6) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
				}
				logPosition = 0;
			}
			final List<String> ipAddresses = lruVotes.values().iterator().next();

			if(ipAddresses == null || ipAddresses.size() <= 0) {
				isElectionAborted = true;
				if(logPosition != 7) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 7");
					}
					logPosition = 7;
					UnitManager.Logging.logSevere("Update Service: Failed to elect the LCP part number");
					UnitManager.Logging.logSevere("Update Service: Votes are null for: " + lruType);
					UnitManager.Logging.logSevere("Update Service: Aborting the election");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}
			if(logPosition == 7) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
				}
				logPosition = 0;
			}
			final Iterator<String> ipAddressesItr = ipAddresses.iterator();
			while(ipAddressesItr.hasNext()) {
				final String ipAddress = ipAddressesItr.next();
				final CAMessage ca = voters.get(ipAddress);
				final ParentCI parentCI = ca.getParentCIs().get(UpdateService.LCP_TYPE);

				if(!ca.isRegularLCPType()) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Regular LCP vote not available at: " + ipAddress);
					}
					continue;
				}
				
				if(parentCI == null) {
					isElectionAborted = true;
					if(logPosition != 8) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 8");
						}
						logPosition = 8;
						UnitManager.Logging.logSevere("Update Service: Failed to elect the part number as LCP Parent CI is null for: " + ipAddress);
						UnitManager.Logging.logSevere("Update Service: Aborting the election");
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Already logged: " + logPosition);
						}
					}
					return;
				}
				
				isRegularLCPFound = true;
				if(logPosition == 8) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
					}
					logPosition = 0;
				}
				final String partNumber = parentCI.getCpn();
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Part number votes contains: " + partNumber + " : "  + partNumberVotes.containsKey(partNumber));
				}
				List<String> votesReceived = partNumberVotes.get(partNumber);
				if(votesReceived == null) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Votes received is null for part number: " + partNumber);
					}
					votesReceived = new ArrayList<String>();
					partNumberVotes.put(partNumber, votesReceived);
				}
				if(!votesReceived.contains(ipAddress)) {
					votesReceived.add(ipAddress);
					final int votesSize = votesReceived.size();
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Part number: " + partNumber + " : Votes size: " + votesSize);
					}
					if(votesSize > maxCount) {
						maxCount = votesSize;
						prevailingPN = partNumber;
					} else if(votesSize != 0 && votesSize == maxCount) {
						prevailingPN = maxPartNumber(prevailingPN, partNumber);
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Not the max part number: " + partNumber);
						}
					}
				}
			}
		}
		
		if(!isRegularLCPFound) {
			isElectionAborted = true;
			if(logPosition != 8) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 8");
				}
				logPosition = 8;
				UnitManager.Logging.logSevere("Update Service: Failed to elect the part number as there is no vote for regular LCP Parent CI");
				UnitManager.Logging.logSevere("Update Service: Aborting the election");
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Already logged: " + logPosition);
				}
			}
			return;
		}

		// maxCount should be greater than 0
		// prevailingPN should not null and should not be empty
		if(maxCount == 0 || prevailingPN == null || prevailingPN.length() <= 0) {
			isElectionAborted = true;
			if(logPosition != 9) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 9");
				}
				logPosition = 9;
				UnitManager.Logging.logSevere("Update Service: Failed to elect the LCP part number");
				UnitManager.Logging.logSevere("Update Service: Aborting the election");
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Already logged: " + logPosition);
				}
			}
			return;
		}
		if(logPosition == 9) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
			}
			logPosition = 0;
		}
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: LRU: LCP : Part number: " + prevailingPN + " : Max count: " + maxCount);
		}

		final List<String> winners = partNumberVotes.get(prevailingPN);

		// winners should not be null and the size should be greater than zero
		if(winners == null || winners.size() <= 0) {
			isElectionAborted = true;
			if(logPosition != 10) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 10");
				}
				logPosition = 10;
				UnitManager.Logging.logSevere("Update Service: Failed to elect the LCP part number");
				UnitManager.Logging.logSevere("Update Service: Aborting the election");
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Already logged: " + logPosition);
				}
			}
			return;
		}
		if(logPosition == 10) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
			}
			logPosition = 0;
		}
		electLcpBuildNumber(prevailingPN, winners);
	}

	/**
	 * Elects a prevailing LCP build number. The voting will be done among
	 * the winners of prevailing LCP part number.
	 *
	 * @param ipAddresses the ip addresses
	 */
	private void electLcpBuildNumber(final String prevailingPN, final List<String> ipAddresses) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Electing LCP build number");
		}
		final Iterator<String> ipAddressesItr = ipAddresses.iterator();

		final HashMap<String, List<String>> buildVotes = new HashMap<String, List<String>>();
		int maxCount = 0;
		String prevailingBN = null;

		while(ipAddressesItr.hasNext()) {
			final String ipAddress = ipAddressesItr.next();
			final CAMessage ca = voters.get(ipAddress);
			final ParentCI parentCI = ca.getParentCIs().get(UpdateService.LCP_TYPE);

			if(parentCI == null) {
				isElectionAborted = true;
				if(logPosition != 11) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 11");
					}
					logPosition = 11;
					UnitManager.Logging.logSevere("Update Service: Failed to elect the build number as LCP Parent CI is null for: " + ipAddress);
					UnitManager.Logging.logSevere("Update Service: Aborting the election");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}
			if(logPosition == 11) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
				}
				logPosition = 0;
			}

			final String buildNumber = parentCI.getBuildNumber();
			List<String> votesReceived = buildVotes.get(buildNumber);
			if(votesReceived == null) {
				votesReceived = new ArrayList<String>();
				buildVotes.put(buildNumber, votesReceived);
			}
			votesReceived.add(ipAddress);

			final int votesSize = votesReceived.size();
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Build number: " + buildNumber + " : Votes size: " + votesSize);
			}
			if(votesSize > maxCount) {
				maxCount = votesSize;
				prevailingBN = buildNumber;
			} else if(votesSize != 0 && votesSize == maxCount) {
				prevailingBN = maxBuildNumber(prevailingBN, buildNumber);
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Not the max build number: " + buildNumber);
				}
			}
		}

		// maxCount should be greater than 0
		// prevailingBN should not null and should not be empty
		if(maxCount == 0 || prevailingBN == null || prevailingBN.length() <= 0) {
			isElectionAborted = true;
			if(logPosition != 12) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 12");
				}
				logPosition = 12;
				UnitManager.Logging.logSevere("Update Service: Failed to elect the software build number");
				UnitManager.Logging.logSevere("Update Service: Aborting the election");
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Already logged: " + logPosition);
				}
			}
			return;
		}
		if(logPosition == 12) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
			}
			logPosition = 0;
		}
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: LRU: LCP : Build number: " + prevailingBN + " : Max count: " + maxCount);
		}

		final List<String> lcpWinners = buildVotes.get(prevailingBN);

		// winners should not be null and the size should be greater than zero
		if(lcpWinners == null || lcpWinners.size() <= 0) {
			isElectionAborted = true;
			if(logPosition != 13) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 13");
				}
				logPosition = 13;
				UnitManager.Logging.logSevere("Update Service: Failed to elect the software build number");
				UnitManager.Logging.logSevere("Update Service: Aborting the election");
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Already logged: " + logPosition);
				}
			}
			return;
		}
		if(logPosition == 13) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
			}
			logPosition = 0;
		}

		updateWinners(prevailingPN, prevailingBN, lcpWinners);
	}

	/**
	 * Updates winners list with only those who have the prevailing LCP.
	 *
	 * @param winners the winners
	 */
	private void updateWinners(final String prevailingPN, final String prevailingBN, final List<String> lcpWinners) {
		eaMessage = new EAMessage();
		eaMessage.setUpdateService(updateService);
		eaMessage.setLruType(hardwareDetails.getDeviceType());
		eaMessage.setIpAddress(hardwareDetails.getIpAddress());
		eaMessage.setLruInstance(updateService.getLruInstance());

		final HashMap<String, ElectionResult> result = new HashMap<String, ElectionResult>();
		final List<String> loserIPAddress = new ArrayList<String>();

		lcpVotes = new HashMap<String, List<String>>();
		Iterator<Entry<String, HashMap<String, List<String>>>> votesEntryIterator = votes.entrySet().iterator();
		while(votesEntryIterator.hasNext()) {
			final Entry<String, HashMap<String, List<String>>> votesEntry = votesEntryIterator.next();
			final String lruType = votesEntry.getKey();
			final HashMap<String, List<String>> lruVotes = votesEntry.getValue();

			final List<String> ipAddresses = lruVotes.values().iterator().next();
			if(ipAddresses == null || ipAddresses.size() <= 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: No winners available for: " + lruType);
					updateService.logInfo("Update Service: Removing: " + lruType + " entry from the votes collection");
				}
				votesEntryIterator.remove();
				continue;
			}
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Updating winners for LRU: " + lruType);
				updateService.logInfo("Update Service: Prevailing LCP part number: " + prevailingPN);
				updateService.logInfo("Update Service: Prevailing LCP build number: " + prevailingBN);
			}
			final Iterator<String> ipAddressesItr = ipAddresses.iterator();
			while(ipAddressesItr.hasNext()) {
				final String ipAddress = ipAddressesItr.next();
				final CAMessage ca = voters.get(ipAddress);
				final String actualLruType = ca.getHardwareDetails().getDeviceType();
				final boolean isRegularLCPType = ca.isRegularLCPType();
				final String lcpType = ca.getLcpType();
				final String lcpTypeToBeUsed = updateService.getLruToLCPTypes().get(lruType);
				boolean isWinner = false;
				
				if(lruType.equals(actualLruType) || (lcpTypeToBeUsed != null && lcpTypeToBeUsed.equals(UpdateService.NO_LCP))) {
					// If LRU type that contributed the vote is the same as actual device type
					// Then the LRU has voted for itself
					// OR
					// If the LRU type is not associated with any LCP
					// Then this LRU type is a Primary Package
					// The LRU that contributed the vote will be declared as a winner for 
					// This Primary Package, only if it is a winner for its own LCP
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Voting for itself or primary package: " + ipAddress);
					}
					if(isRegularLCPType) {
						// The LRU that contributed this vote uses regular LCP
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Voter is using regular LCP: " + ipAddress);
						}
						if(lcpWinners.contains(ipAddress)) {
							// This LRU is a winner for its own LSP and LCP
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter is already a winner: " + ipAddress);
							}
							isWinner = true;
						} else {
							// This LRU is a loser for its LCP
							// This LRU has to update its LCP
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter is a loser: " + ipAddress);
							}
							isWinner = false;
						}
					} else {
						// The LRU that contributed this vote is not using the regular LCP
						// Obtain the regular LCP's part number and build number first
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Voter is NOT using regular LCP: " + ipAddress);
						}
						final ParentCI lcpParentCI = ca.getParentCIs().get(lcpType);
						if(lcpParentCI == null) {
							// There is no type of LCP installed
							// This LRU has to update its LCP
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: LCP Parent CI is null at: " + ipAddress);
							}
							isWinner = false;
						} else {
							final String regLCPPN = lcpParentCI.getRegLCPPartNumber();
							final String regLCPBN = lcpParentCI.getRegLCPBuildNumber();
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter's regular LCP PN: " + ipAddress + ": " + regLCPPN);
								updateService.logInfo("Update Service: Voter's regular LCP BN: " + ipAddress + ": " + regLCPBN);
							}
							if(regLCPPN != null && regLCPBN != null
									&& regLCPPN.equals(prevailingPN) && regLCPBN.equals(prevailingBN)) {
								// This LRU is a winner for its own LSP and LCP
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Voter is a winner as the PN and BN are matching: " + ipAddress);
								}
								isWinner = true;
							} else {
								// This LRU is a loser for its own LCP
								// This LRU has to update its LCP
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Voter is a loser as the PN and BN are not matching: " + ipAddress);
								}
								isWinner = false;
							}
						}
					}
					if(isWinner == false && lruType.equals(actualLruType)) {
						// Make sure all the votes contributed by this 
						// LRU for other LRU types are removed from the winners list
						// Because this LRU itself needs to update its own LCP
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Adding the IP address to the list of losers: " + ipAddress);
						}
						loserIPAddress.add(ipAddress);
					}
				} else {
					// If LRU type voted for is not the same as actual device type
					// The LRU has voted for its backup copy
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Voter contributed for backup copy: " + ipAddress);
					}
					if(isRegularLCPType && lcpTypeToBeUsed == null) {
						// The LRU that contributed the vote as well as the backup copy,
						// Both uses regular LCP
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Both voter and its  backup uses regular LCP at: " + ipAddress);
						}
						if(lcpWinners.contains(ipAddress)) {
							// This LRU is a winner for its backup copy
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter's backup is already a winner: " + ipAddress);
							}
							isWinner = true;
						} else {
							// This LRU is a loser for its backup copy
							// This LRU has to update its backup copy
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter's backup is a loser: " + ipAddress);
							}
							isWinner = false;
						}
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Voter and its backup are not using the same LCP type: " + ipAddress);
						}
						ParentCI lcpParentCI = null;
						if(lcpTypeToBeUsed == null) {
							// The backup copy is associated with regular LCP
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter's backup is using regular LCP: " + ipAddress);
							}
							lcpParentCI = ca.getParentCIs().get(UpdateService.LCP_TYPE);
						} else {
							// The backup copy is associated with a non-regular LCP
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter's backup is NOT using regular LCP: " + ipAddress + ": " + lcpTypeToBeUsed);
							}
							lcpParentCI = ca.getParentCIs().get(lcpTypeToBeUsed);
						}
						if(lcpParentCI == null) {
							// The backup copy is missing its LCP
							// This LRU has to update its backup copy
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: LCP Parent CI is null at: " + ipAddress);
							}
							isWinner = false;
						} else {
							final String regLCPPN = lcpParentCI.getRegLCPPartNumber();
							final String regLCPBN = lcpParentCI.getRegLCPBuildNumber();
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Voter's backup's regular LCP PN: " + ipAddress + ": " + regLCPPN);
								updateService.logInfo("Update Service: Voter's backup's regular LCP BN: " + ipAddress + ": " + regLCPBN);
							}
							if(regLCPPN != null && regLCPBN != null
									&& regLCPPN.equals(prevailingPN) && regLCPBN.equals(prevailingBN)) {
								// This LRU is a winner for its backup copy's LSP and LCP
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Voter's backup is a winner as the PN and BN are matching: " + ipAddress);
								}
								isWinner = true;
							} else {
								// This LRU is a loser for its backup copy's LSP and LCP
								// This LRU has to update its backup copy
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Voter's backup is a loser as the PN and BN are not matching: " + ipAddress);
								}
								isWinner = false;
							}
						}
					}
				}
				
				if(!isWinner) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Removing the winner: " + ipAddress + " LRU: " + lruType + ", as it lost the LCP voting");
					}
					ipAddressesItr.remove();
				} else {
					String lcpEntry = null;
					if(lcpTypeToBeUsed == null) {
						lcpEntry = UpdateService.LCP_TYPE;
					} else {
						lcpEntry = lcpTypeToBeUsed;
					}
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Winner: " + ipAddress + " LRU Type: " + lruType + " LCP Type: " + lcpEntry);
					}
					List<String> winners = lcpVotes.get(lcpEntry);
					if(winners == null) {
						 winners = new ArrayList<String>();
						lcpVotes.put(lcpEntry, winners);
					}
					if(!winners.contains(ipAddress)) {
						winners.add(ipAddress);
					}
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Winner: " + ipAddress + " LRU Type: " + lruType);
					}
				}
			}
			// If there are no winners for a LRU type, remove the entry for that LRU type
			if(ipAddresses.size() <= 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: No winners available for: " + lruType);
					updateService.logInfo("Update Service: Removing: " + lruType + " entry from the votes collection");
				}
				votesEntryIterator.remove();
			}
		}
		
		// Loop through all the votes again to remove
		// Any loser's IP Address and to form the election result
		votesEntryIterator = votes.entrySet().iterator();
		while(votesEntryIterator.hasNext()) {
			final Entry<String, HashMap<String, List<String>>> votesEntry = votesEntryIterator.next();
			final String lruType = votesEntry.getKey();
			final HashMap<String, List<String>> lruVotes = votesEntry.getValue();
			final List<String> ipAddresses = lruVotes.values().iterator().next();
			
			final ElectionResult electionResult = new ElectionResult();
			electionResult.setLruType(lruType);
			String winningPartNumber = null;
			String winningBuildNumber = null;
			
			// Remove any entry from a loser's IP Address
			if(loserIPAddress != null && loserIPAddress.size() > 0) {
				for(final String loserIP : loserIPAddress) {
					if(ipAddresses.contains(loserIP)) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Removing the entry for loser: " + loserIP + "from LRU: " + lruType);
						}
						ipAddresses.remove(loserIP);
					}
				}
			}
			
			// If there are no winners for a LRU type, remove the entry for that LRU type
			if(ipAddresses.size() <= 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: No winners available for: " + lruType);
					updateService.logInfo("Update Service: Removing: " + lruType + " entry from the votes collection");
				}
				votesEntryIterator.remove();
				continue;
			} else {
				if(winningPartNumber == null) {
					winningPartNumber = voters.get(ipAddresses.get(0)).getParentCIs().get(lruType).getCpn();
					electionResult.setPartNumber(winningPartNumber);
				}
				if(winningBuildNumber == null) {
					winningBuildNumber = voters.get(ipAddresses.get(0)).getParentCIs().get(lruType).getBuildNumber();
					electionResult.setBuildNumber(winningBuildNumber);
				}
				electionResult.setWinners(ipAddresses);
			}
			result.put(lruType, electionResult);
		}
		eaMessage.setResult(result);
	}

	/**
	 * Chooses a maximum part number. The last 3 digits of the part number will be
	 * considered.
	 *
	 * @param partNumber1 the part number1
	 * @param partNumber2 the part number2
	 *
	 * @return the string
	 */
	private String maxPartNumber(final String partNumber1, final String partNumber2) {
		try {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Choosing max part number between: " + partNumber1 + " and " + partNumber2);
			}
			final String part1 = partNumber1.substring(0, partNumber1.length() - 3);
			final String part2 = partNumber1.substring(0, partNumber1.length() - 3);

			if (part1.equals(part2)) {
				String number1 = partNumber1.substring(partNumber1.length() - 3);
				String number2 = partNumber2.substring(partNumber2.length() - 3);

				number1 = number1.toUpperCase();
				number2 = number2.toUpperCase();

				int no1 = 0;
				int no2 = 0;
				boolean isNo1Alpha = false;
				boolean isNo2Alpha = false;
				final char c1 = number1.charAt(0);
				final char c2 = number2.charAt(0);

				if (c1 > 64 && c1 < 90) {
					isNo1Alpha = true;
					number1 = number1.substring(1);
				}
				no1 = Integer.parseInt(number1);

				if (c2 > 64 && c2 < 90) {
					isNo2Alpha = true;
					number2 = number2.substring(1);
				}
				no2 = Integer.parseInt(number2);

				if (isNo1Alpha && isNo2Alpha) {
					if (no1 == no2) {
						if (c1 == c2) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Both the part numbers are equal - Choosing first number: " + partNumber1);
							}
							return partNumber1;
						} else if (c1 > c2) {
							return partNumber1;
						} else {
							return partNumber2;
						}
					} else if (no1 > no2) {
						return partNumber1;
					} else {
						return partNumber2;
					}
				} else {
					if (no1 == no2) {
						if (!isNo1Alpha && !isNo2Alpha) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Both the part numbers are equal - Choosing first number: " + partNumber1);
							}
							return partNumber1;
						} else if (!isNo1Alpha) {
							return partNumber1;
						} else {
							return partNumber2;
						}
					} else if (no1 > no2) {
						return partNumber1;
					} else {
						return partNumber2;
					}
				}
			} else {
				return partNumber1;
			}
		} catch(final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Error choosing the max part number between: " + partNumber1 + " and " + partNumber2, e);
			return partNumber1;
		}
	}

	/**
	 * Chooses a maximum build number.
	 *
	 * @param buildNumber1 the build number1
	 * @param buildNumber2 the build number2
	 *
	 * @return the string
	 */
	private String maxBuildNumber(final String buildNumber1, final String buildNumber2) {
		try {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Choosing max part number between: " + buildNumber1 + " and " + buildNumber2);
			}
			if(buildNumber1.length() <= 0) {
				UnitManager.Logging.logSevere("Update Service: Build number 1 is empty - Returning build number 2: " + buildNumber2);
				return buildNumber2;
			} else if(buildNumber2.length() <= 0) {
				UnitManager.Logging.logSevere("Update Service: Build number 2 is empty - Returning build number 1: " + buildNumber1);
				return buildNumber1;
			}

			if(buildNumber1.indexOf('-') > 0 || buildNumber2.indexOf('-') > 0) {
				String initialBuildNumber1 = null;
				String initialBuildNumber2 = null;

				String finalBuildNumber1 = null;
				String finalBuildNumber2 = null;

				if(buildNumber1.indexOf('-') > 0) {
					initialBuildNumber1 = buildNumber1.substring(0, buildNumber1.indexOf('-'));
					finalBuildNumber1 = buildNumber1.substring(buildNumber1.indexOf('-') + 1);
				} else {
					initialBuildNumber1 = buildNumber1;
					finalBuildNumber1 = "0";
				}

				if(buildNumber2.indexOf('-') > 0) {
					initialBuildNumber2 = buildNumber2.substring(0, buildNumber2.indexOf('-'));
					finalBuildNumber2 = buildNumber2.substring(buildNumber2.indexOf('-') + 1);
				} else {
					initialBuildNumber2 = buildNumber2;
					finalBuildNumber2 = "0";
				}

				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: initialBuildNumber1: " + initialBuildNumber1);
					updateService.logInfo("Update Service: initialBuildNumber2: " + initialBuildNumber2);

					updateService.logInfo("Update Service: finalBuildNumber1: " + finalBuildNumber1);
					updateService.logInfo("Update Service: finalBuildNumber2: " + finalBuildNumber2);
				}

				final int initialB1 = Integer.parseInt(initialBuildNumber1);
				final int initialB2 = Integer.parseInt(initialBuildNumber2);

				if(initialB1 == initialB2) {
					final int finalB1 = Integer.parseInt(finalBuildNumber1);
					final int finalB2 = Integer.parseInt(finalBuildNumber2);

					if(finalB1 > finalB2) {
						return buildNumber1;
					} else if(finalB2 > finalB1) {
						return buildNumber2;
					} else {
						updateService.logInfo("Update Service: Both the build numbers are equal - Returning build number 1: " + buildNumber1);
						return buildNumber1;
					}
				} else {
					if(initialB1 > initialB2) {
						return buildNumber1;
					} else {
						return buildNumber2;
					}
				}
			} else {
				final int b1 = Integer.parseInt(buildNumber1);
				final int b2 = Integer.parseInt(buildNumber2);

				if(b1 > b2) {
					return buildNumber1;
				} else if(b2 > b1) {
					return buildNumber2;
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Both the build numbers are equal - Returning build number 1: " + buildNumber1);
					}
					return buildNumber1;
				}
			}
		} catch(final Exception e) {
			UnitManager.Logging.logSevere("Update Service: Error choosing the max build number between: " + buildNumber1 + " and " + buildNumber2, e);
			return buildNumber1;
		}
	}
	
	/**
	 * Iterates through the winner list and returns the first valid IP address
	 * 
	 * @return first valid IP address
	 */
	private String getWinnerIP(final String type, final List<String> ipAddresses) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Getting LRU winner IP Address for: " + type);
		}
		if(ipAddresses == null || ipAddresses.size() <= 0) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Winner IP Address is null as there are no winners for: " + type);
			}
			return null;
		}
		
		int i = 0;
		boolean ipValid = false;
		String ipAddress = null;
		while (!ipValid && i < ipAddresses.size()) {
			ipAddress = ipAddresses.get(i);
			ipValid = isValidIP(ipAddress);
			i++;
		}
		
		if(ipValid) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Winner IP Address: " + ipAddress + " for: " + type);
			}
			return ipAddress;
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Winner IP Address is null for: " + type);
			}
			return null;
		}
	}
	
	/**
	 * Checks whether the given IP address has 4 or more triplets
	 * 
	 * @param ipAddress the ip address
	 * 
	 * @return true, if the IP address has 4 or more triplets
	 */
	private boolean isValidIP(final String ipAddress) {
		String[] ipTriplet = ipAddress.split("\\.");
		if (ipTriplet.length > 3) {
			return true;
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Skipping bad IP Address: " + ipAddress);
				updateService.logInfo("Update Service: Invalid length: Triplet length: " + ipTriplet.length);
			}
			return false;
		}
	}

	/**
	 * Checks election result for the local LRU type, LCP, secondary LRUs and
	 * back up LRUs. If the current LRU is not a winner for any of these, then
	 * it will make an attempt to install those software from the winners.
	 */
	private void checkElectionResult() {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Checking election result");
		}
		final String deviceType = hardwareDetails.getDeviceType();
		final String deviceIpAddress = hardwareDetails.getIpAddress();
		final HashMap<String, List<String>> lruVotes = votes.get(deviceType);
		
		if(lruVotes == null || lruVotes.size() <= 0 ) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: No votes available for: " + deviceType);
				updateService.logInfo("Update Service: Checking the election result for: LCP");
			}

			if(lruVotes == null || lruVotes.size() <= 0) {
				sendEAMessage(eaMessage, false);
			}
			
			if(lcpVotes == null || lcpVotes.size() <= 0 
					|| lcpVotes.get(updateService.getLCPType()) == null 
					|| lcpVotes.get(updateService.getLCPType()).size() <= 0) {
				if(logPosition != 14) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 14");
					}
					logPosition = 14;
					UnitManager.Logging.logSevere("Update Service: No votes available for LCP type: " + updateService.getLCPType());
					UnitManager.Logging.logSevere("Update Service: Aborting the election result");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}
			if(logPosition == 14) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
				}
				logPosition = 0;
			}
			List<String> winners = lcpVotes.get(updateService.getLCPType());
			final String ipAddress = getWinnerIP(updateService.getLCPType(), winners);
			if(!winners.contains(deviceIpAddress) && ipAddress != null) {
				sendEAMessage(eaMessage, false);
				final CAMessage ca = voters.get(ipAddress);
				final ParentCI lcpParentCI = ca.getParentCIs().get(updateService.getLCPType());

				final boolean isLcpCompatible = checkLcpCompatibility(lcpParentCI, null);

				if(!isLcpCompatible) {
					if(logPosition != 15) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 15");
						}
						logPosition = 15;
						UnitManager.Logging.logSevere("Update Service: The elected LCP is found incompatible with the installed software");
						UnitManager.Logging.logSevere("Update Service: Cannot load the LCP: " + lcpParentCI.getCpn() + " : " + lcpParentCI.getBuildNumber());
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Already logged: " + logPosition);
						}
					}
					return;
				}
				if(logPosition == 15) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
					}
					logPosition = 0;
				}
				final HashMap<String, ParentCI> newParentCIs = new HashMap<String, ParentCI>();
				newParentCIs.put(updateService.getLCPType(), lcpParentCI);
				final InstallListInfo installList = conformityChecker.getInstallList(newParentCIs, false);
				if (installList != null && installList.getParentCIsToInstall().size() > 0) {
					final List<CAMessage> prevailingVotes = new ArrayList<CAMessage>();
					for(final String prevailingVoter : winners) {
						final CAMessage prevailingCa = voters.get(prevailingVoter);
						prevailingVotes.add(prevailingCa);
					}
					installList.setPrevailingVotes(prevailingVotes);
					installList.setSelfLoad(true);
					updateService.caInstall(ca, installList);
					return;
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Finished checking the election result");
						updateService.logInfo("Update Service: Nothing to install for LCP");
					}
				}
			} else {
				if(logPosition != 16) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 16");
					}
					logPosition = 16;
					if(winners.contains(deviceIpAddress)) {
						UnitManager.Logging.logSevere("Update Service: Current LRU IP address is already a winner of LCP: " + deviceIpAddress);
						UnitManager.Logging.logSevere("Update Service: Still no votes available for current LRU: " + deviceType);
						UnitManager.Logging.logSevere("Update Service: Aborting the election result");
					} else {
						UnitManager.Logging.logSevere("Update Service: Not able to obtain a valid winner IP address for LCP");
						UnitManager.Logging.logSevere("Update Service: Aborting the election result");
					}
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				return;
			}
			if(logPosition == 16) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
				}
				logPosition = 0;
			}
		} else {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: Checking the election result for: " + deviceType);
			}
			final String prevailingPN = lruVotes.keySet().iterator().next();
			final List<String> ipAddresses = lruVotes.get(prevailingPN);
			if(ipAddresses == null || ipAddresses.size() <= 0) {
				if(logPosition != 17) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 17");
					}
					logPosition = 17;
					UnitManager.Logging.logSevere("Update Service: No votes available for prevailing PN: " + prevailingPN);
					UnitManager.Logging.logSevere("Update Service: Aborting the election result");
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Already logged: " + logPosition);
					}
				}
				sendEAMessage(eaMessage, false);
				return;
			}
			if(logPosition == 17) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
				}
				logPosition = 0;
			}

			final String ipAddress = getWinnerIP(deviceType, ipAddresses);
			if(!ipAddresses.contains(deviceIpAddress) && ipAddress != null) {
				sendEAMessage(eaMessage, false);
				
				final CAMessage ca = voters.get(ipAddress);
				final ParentCI lruParentCI = ca.getParentCIs().get(deviceType);
				final ParentCI lcpParentCI = ca.getParentCIs().get(updateService.getLCPType());
				if(lruParentCI == null || lcpParentCI == null) {
					if(logPosition != 18) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 18");
						}
						logPosition = 18;
						UnitManager.Logging.logSevere("Update Service: The elected LRU or LCP Parent CI is null at: " + ipAddress);
						UnitManager.Logging.logSevere("Update Service: Aborting the election result");
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Already logged: " + logPosition);
						}
					}
					return;
				}
				if(logPosition == 18) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
					}
					logPosition = 0;
				}

				final boolean isHardwareCompatible = checkHardwareCompatibility(lruParentCI);

				if(!isHardwareCompatible) {
					if(logPosition != 19) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 19");
						}
						logPosition = 19;
						UnitManager.Logging.logSevere("Update Service: The elected software is found incompatible with the installed hardware");
						UnitManager.Logging.logSevere("Update Service: Cannot load the LRU software: " + lruParentCI.getCpn() + " : " + lruParentCI.getBuildNumber());
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Already logged: " + logPosition);
						}
					}
					return;
				}
				if(logPosition == 19) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
					}
					logPosition = 0;
				}

				final boolean isLcpCompatible = checkLcpCompatibility(lcpParentCI, lruParentCI);

				if(!isLcpCompatible) {
					if(logPosition != 20) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 20");
						}
						logPosition = 20;
						UnitManager.Logging.logSevere("Update Service: The elected software is found incompatible with the elected LCP");
						UnitManager.Logging.logSevere("Update Service: Cannot load the LRU software: " + lruParentCI.getCpn() + " : " + lruParentCI.getBuildNumber());
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Already logged: " + logPosition);
						}
					}
					return;
				}
				if(logPosition == 20) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
					}
					logPosition = 0;
				}
				final HashMap<String, ParentCI> newParentCIs = new HashMap<String, ParentCI>();
				newParentCIs.put(deviceType, lruParentCI);
				newParentCIs.put(updateService.getLCPType(), lcpParentCI);
				final InstallListInfo installList = conformityChecker.getInstallList(newParentCIs, false);
				if (installList != null && installList.getParentCIsToInstall().size() > 0) {
					final List<CAMessage> prevailingVotes = new ArrayList<CAMessage>();
					for(final String prevailingVoter : ipAddresses) {
						final CAMessage prevailingCa = voters.get(prevailingVoter);
						prevailingVotes.add(prevailingCa);
					}
					installList.setPrevailingVotes(prevailingVotes);
					installList.setSelfLoad(true);
					updateService.caInstall(ca, installList);
					return;
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Finished checking the election result");
						updateService.logInfo("Update Service: Nothing to install for LRU software and LCP");
					}
				}
			} else {
				if(ipAddresses.contains(deviceIpAddress)) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Current LRU IP address is already a winner: " + deviceIpAddress);
						updateService.logInfo("Update Service: Nothing to install for LCP and LRU software: " + deviceType);
					}
					if(stateManager != null) {
						if(stateManager.getMode() != stateManager.getDefaultMode()) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Setting the State Manager to its default mode: " + stateManager.getDefaultMode());
							}
							stateManager.setMode(stateManager.getDefaultMode());
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: State Manager is already in its default mode: " + stateManager.getDefaultMode());
							}
						}
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: State Manager reference is null");
						}
					}
					if(logPosition == 21) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
						}
						logPosition = 0;
					}
				} else {
					sendEAMessage(eaMessage, false);
					if(logPosition != 21) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 21");
						}
						logPosition = 21;
						UnitManager.Logging.logSevere("Update Service: Not able to obtain a valid winner IP address for: " + deviceType);
						UnitManager.Logging.logSevere("Update Service: Aborting the election result");
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Already logged: " + logPosition);
						}
					}
					return;
				}
			}
		}
		
		if(primaryPackagesList != null && primaryPackagesList.size() > 0) {
			for(final String primaryPackage : primaryPackagesList) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Checking the election result for: " + primaryPackage);
				}
				final HashMap<String, List<String>> primaryPackageVotes = votes.get(primaryPackage);
				if(primaryPackageVotes != null && primaryPackageVotes.size() > 0) {
					final String prevailingPN = primaryPackageVotes.keySet().iterator().next();
					final List<String> ipAddresses = primaryPackageVotes.get(prevailingPN);
					if(ipAddresses == null || ipAddresses.size() <= 0) {
						if(logPosition != 22) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 21");
							}
							logPosition = 22;
							UnitManager.Logging.logSevere("Update Service: No votes available for prevailing PN: " + prevailingPN);
							UnitManager.Logging.logSevere("Update Service: Aborting the election result");
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Already logged: " + logPosition);
							}
						}
						sendEAMessage(eaMessage, false);
						return;
					}
					if(logPosition == 22) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
						}
						logPosition = 0;
					}
					final String ipAddress = getWinnerIP(primaryPackage, ipAddresses);
					if(!ipAddresses.contains(deviceIpAddress) && ipAddress != null) {
						sendEAMessage(eaMessage, false);

						final CAMessage ca = voters.get(ipAddress);
						final ParentCI lruParentCI = ca.getParentCIs().get(primaryPackage);
						if(lruParentCI == null) {
							if(logPosition != 23) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 22");
								}
								logPosition = 23;
								UnitManager.Logging.logSevere("Update Service: The elected LRU Parent CI is null at: " + ipAddress);
								UnitManager.Logging.logSevere("Update Service: Aborting the election result only for " + primaryPackage);
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							continue;
						}
						if(logPosition == 23) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
							}
							logPosition = 0;
						}

						final boolean isHardwareCompatible = checkHardwareCompatibility(lruParentCI);

						if(!isHardwareCompatible) {
							if(logPosition != 24) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 19");
								}
								logPosition = 24;
								UnitManager.Logging.logSevere("Update Service: The elected software is found incompatible with the installed hardware");
								UnitManager.Logging.logSevere("Update Service: Cannot load the LRU software: " + lruParentCI.getCpn() + " : " + lruParentCI.getBuildNumber());
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							return;
						}
						if(logPosition == 24) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
							}
							logPosition = 0;
						}

						final HashMap<String, ParentCI> newParentCIs = new HashMap<String, ParentCI>();
						newParentCIs.put(primaryPackage, lruParentCI);
						final InstallListInfo installList = conformityChecker.getInstallList(newParentCIs, false);
						if (installList != null && installList.getParentCIsToInstall().size() > 0) {
							final List<CAMessage> prevailingVotes = new ArrayList<CAMessage>();
							for(final String prevailingVoter : ipAddresses) {
								final CAMessage prevailingCa = voters.get(prevailingVoter);
								prevailingVotes.add(prevailingCa);
							}
							installList.setPrevailingVotes(prevailingVotes);
							installList.setSelfLoad(true);
							updateService.caInstall(ca, installList);
							return;
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Finished checking the election result");
								updateService.logInfo("Update Service: Nothing to install for: " + primaryPackage);
							}
						}
					} else {
						if(ipAddresses.contains(deviceIpAddress)) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current LRU IP address is already a winner: " + deviceIpAddress);
								updateService.logInfo("Update Service: Nothing to install for primary package software: " + primaryPackage);
							}
							if(logPosition == 25) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
								}
								logPosition = 0;
							}
						} else {
							sendEAMessage(eaMessage, false);
							if(logPosition != 25) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 19");
								}
								logPosition = 25;
								UnitManager.Logging.logSevere("Update Service: Not able to obtain a valid winner IP address for: " + deviceType);
								UnitManager.Logging.logSevere("Update Service: Aborting the election result");
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							return;
						}
					}
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: No votes available for secondary LRU: " + primaryPackage);
					}
				}
			}
		}
		
		if(secondaryLruList != null && secondaryLruList.size() > 0) {
			for(final String secondaryLru : secondaryLruList) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Checking the election result for: " + secondaryLru);
				}
				final HashMap<String, List<String>> secondaryLruVotes = votes.get(secondaryLru);
				if(secondaryLruVotes != null && secondaryLruVotes.size() > 0) {
					final String prevailingPN = secondaryLruVotes.keySet().iterator().next();
					final List<String> ipAddresses = secondaryLruVotes.get(prevailingPN);
					if(ipAddresses == null || ipAddresses.size() <= 0) {
						if(logPosition != 26) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 21");
							}
							logPosition = 26;
							UnitManager.Logging.logSevere("Update Service: No votes available for prevailing PN: " + prevailingPN);
							UnitManager.Logging.logSevere("Update Service: Aborting the election result");
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Already logged: " + logPosition);
							}
						}
						sendEAMessage(eaMessage, false);
						return;
					}
					if(logPosition == 26) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
						}
						logPosition = 0;
					}
					final String ipAddress = getWinnerIP(secondaryLru, ipAddresses);
					if(!ipAddresses.contains(deviceIpAddress) && ipAddress != null) {
						sendEAMessage(eaMessage, false);
						
						final CAMessage ca = voters.get(ipAddress);
						final ParentCI lruParentCI = ca.getParentCIs().get(secondaryLru);
						if(lruParentCI == null) {
							if(logPosition != 27) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 22");
								}
								logPosition = 27;
								UnitManager.Logging.logSevere("Update Service: The elected LRU Parent CI is null at: " + ipAddress);
								UnitManager.Logging.logSevere("Update Service: Aborting the election result only for " + secondaryLru);
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							continue;
						}
						if(logPosition == 27) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
							}
							logPosition = 0;
						}

						final HashMap<String, ParentCI> newParentCIs = new HashMap<String, ParentCI>();
						newParentCIs.put(secondaryLru, lruParentCI);
						final InstallListInfo installList = conformityChecker.getInstallList(newParentCIs, false);
						if (installList != null && installList.getParentCIsToInstall().size() > 0) {
							final List<CAMessage> prevailingVotes = new ArrayList<CAMessage>();
							for(final String prevailingVoter : ipAddresses) {
								final CAMessage prevailingCa = voters.get(prevailingVoter);
								prevailingVotes.add(prevailingCa);
							}
							installList.setPrevailingVotes(prevailingVotes);
							installList.setSelfLoad(true);
							updateService.caInstall(ca, installList);
							return;
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Finished checking the election result");
								updateService.logInfo("Update Service: Nothing to install for: " + secondaryLru);
							}
						}
					} else {
						if(ipAddresses.contains(deviceIpAddress)) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current LRU IP address is already a winner: " + deviceIpAddress);
								updateService.logInfo("Update Service: Nothing to install for secondary LRU software: " + secondaryLru);
							}
							if(logPosition == 28) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
								}
								logPosition = 0;
							}
						} else {
							sendEAMessage(eaMessage, false);
							if(logPosition != 28) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 22");
								}
								logPosition = 28;
								UnitManager.Logging.logSevere("Update Service: Not able to obtain a valid winner IP address for: " + deviceType);
								UnitManager.Logging.logSevere("Update Service: Aborting the election result");
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							return;
						}
					}
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: No votes available for secondary LRU: " + secondaryLru);
					}
				}
			}
		}

		if(backupLruList != null && backupLruList.size() > 0) {
			for(final String backupLru : backupLruList) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Checking the election result for: " + backupLru);
				}
				final HashMap<String, List<String>> backupLruVotes = votes.get(backupLru);
				if(backupLruVotes != null && backupLruVotes.size() > 0) {
					final String prevailingPN = backupLruVotes.keySet().iterator().next();
					final List<String> ipAddresses = backupLruVotes.get(prevailingPN);
					if(ipAddresses == null || ipAddresses.size() <= 0) {
						if(logPosition != 29) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 23");
							}
							logPosition = 29;
							UnitManager.Logging.logSevere("Update Service: No votes available for prevailing PN: " + prevailingPN);
							UnitManager.Logging.logSevere("Update Service: Aborting the election result");
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Already logged: " + logPosition);
							}
						}
						sendEAMessage(eaMessage, false);

						return;
					}
					if(logPosition == 29) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
						}
						logPosition = 0;
					}
					final String ipAddress = getWinnerIP(backupLru, ipAddresses);
					if(!ipAddresses.contains(deviceIpAddress) && ipAddress != null) {
						sendEAMessage(eaMessage, false);
						
						final CAMessage ca = voters.get(ipAddress);
						final ParentCI lruParentCI = ca.getParentCIs().get(backupLru);
						String backupLCPType = updateService.getLruToLCPTypes().get(backupLru);
						ParentCI lcpParentCI = null;
						boolean isLCPBound = true;
						if(backupLCPType == null) {
							backupLCPType = UpdateService.LCP_TYPE;
						} else if(backupLCPType.equals(UpdateService.NO_LCP)){
							// Primary packages saved as backup are not 
							// Associated with any LCP
							// Skip all the LCP checks
							isLCPBound = false;
						}
						if(backupLCPType.equals(updateService.getLCPType())) {
							lcpParentCI = parentCIs.get(updateService.getLCPType());
						} else {
							lcpParentCI = ca.getParentCIs().get(backupLCPType);
						}
						if(lruParentCI == null) {
							if(logPosition != 30) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 24");
								}
								logPosition = 30;
								UnitManager.Logging.logSevere("Update Service: The elected LRU Parent CI is null at: " + ipAddress);
								UnitManager.Logging.logSevere("Update Service: Aborting the election result only for " + backupLru);
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							continue;
						}
						if(logPosition == 30) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
							}
							logPosition = 0;
						}

						final boolean isHardwareCompatible = checkHardwareCompatibility(lruParentCI);

						if(!isHardwareCompatible) {
							if(logPosition != 31) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 25");
								}
								logPosition = 31;
								UnitManager.Logging.logSevere("Update Service: The elected software is found incompatible with the installed hardware");
								UnitManager.Logging.logSevere("Update Service: Cannot load the backup LRU software: " + lruParentCI.getCpn() + " : " + lruParentCI.getBuildNumber());
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							return;
						}
						if(logPosition == 31) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
							}
							logPosition = 0;
						}


						if(lcpParentCI == null && isLCPBound) {
							if(logPosition != 32) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 26");
								}
								logPosition = 32;
								if(backupLCPType.equals(updateService.getLCPType())) {
									UnitManager.Logging.logSevere("Update Service: Found installed LCP Parent CI is null while checking for backup LRU: " + backupLru);
									UnitManager.Logging.logSevere("Update Service: Cannot do self load without any LCP installed");
									UnitManager.Logging.logSevere("Update Service: Aborting the election result");
									return;
								} else {
									UnitManager.Logging.logSevere("Update Service: The elected LCP Parent CI is null at: " + ipAddress);
									UnitManager.Logging.logSevere("Update Service: Aborting the election result only for " + backupLru);
									continue;
								}
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
						}
						if(logPosition == 32) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
							}
							logPosition = 0;
						}

						if(isLCPBound) {
							final boolean isLcpCompatible = checkLcpCompatibility(lcpParentCI, lruParentCI);

							if(!isLcpCompatible) {
								if(logPosition != 33) {
									if(updateService.isInfo()) {
										updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 27");
									}
									logPosition = 33;
									UnitManager.Logging.logSevere("Update Service: The elected software is found incompatible with the elected LCP");
									UnitManager.Logging.logSevere("Update Service: Cannot load the backup software: " + backupLru + " : " + lruParentCI.getCpn() + " : " + lruParentCI.getBuildNumber());
								} else {
									if(updateService.isInfo()) {
										updateService.logInfo("Update Service: Already logged: " + logPosition);
									}
								}
								continue;
							}
							if(logPosition == 33) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
								}
								logPosition = 0;
							}
						}

						final HashMap<String, ParentCI> newParentCIs = new HashMap<String, ParentCI>();
						newParentCIs.put(backupLru, lruParentCI);
						if(isLCPBound) {
							newParentCIs.put(backupLCPType, lcpParentCI);
						}
						final InstallListInfo installList = conformityChecker.getInstallList(newParentCIs, false);
						if (installList != null && installList.getParentCIsToInstall().size() > 0) {
							final List<CAMessage> prevailingVotes = new ArrayList<CAMessage>();
							for(final String prevailingVoter : ipAddresses) {
								final CAMessage prevailingCa = voters.get(prevailingVoter);
								prevailingVotes.add(prevailingCa);
							}
							installList.setPrevailingVotes(prevailingVotes);
							installList.setSelfLoad(true);
							updateService.caInstall(ca, installList);
							return;
						} else {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Finished checking the election result");
								updateService.logInfo("Update Service: Nothing to install for: " + backupLru);
							}
						}
					} else {
						if(ipAddresses.contains(deviceIpAddress)) {
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service: Current LRU IP address is already a winner: " + deviceIpAddress);
								updateService.logInfo("Update Service: Nothing to install for backup LRU software: " + backupLru);
							}
							if(logPosition == 34) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Current log position: " + logPosition + " is being set to 0");
								}
								logPosition = 0;
							}
						} else {
							sendEAMessage(eaMessage, false);
							if(logPosition != 34) {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Old log position: " + logPosition + " New log position: 27");
								}
								logPosition = 34;
								UnitManager.Logging.logSevere("Update Service: Not able to obtain a valid winner IP address for: " + deviceType);
								UnitManager.Logging.logSevere("Update Service: Aborting the election result");
							} else {
								if(updateService.isInfo()) {
									updateService.logInfo("Update Service: Already logged: " + logPosition);
								}
							}
							return;
						}
					}
				} else {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: No votes available for backup LRU: " + backupLru);
					}
				}
			}
		}
		sendEAMessage(eaMessage, true);
	}

	private void sendEAMessage(final EAMessage eaMessage, final boolean isWinner) {
		try {
			if(eaMessage != null) {
				eaMessage.setWinner(isWinner);
				updateService.send(eaMessage);
			}
		} catch (ServiceIOException sioe) {
			UnitManager.Logging.logSevere("Update Service: Not able to send the EAMessage", sioe);
		}
	}

	/**
	 * Checks the hardware compatibility.
	 *
	 * @param newParentCI the new parent CI
	 *
	 * @return true, if successful
	 */
	private boolean checkHardwareCompatibility(final ParentCI newParentCI) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Checking hardware compatibility");
		}

		final List<String> hardwarePartNumbers = newParentCI.getHardwarePartNumbers();

		if(hardwarePartNumbers == null || hardwarePartNumbers.size() <= 0) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: New software is compatible with all hardwares");
			}
			return true;
		}

		final String deviceType = hardwareDetails.getDeviceType();

		String hardwarePartNumber = null;
		if(deviceType.equals(newParentCI.getLruType()) || primaryPackagesList.contains(newParentCI.getLruType())) {
			hardwarePartNumber = hardwareDetails.getHardwarePartNumber();
			if(hardwarePartNumber == null || hardwarePartNumber.length() <= 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Installed hardware part number not available");
					updateService.logInfo("Update Service: Returns as hardware incompatible");
				}
				return false;
			}

			if(hardwarePartNumbers.contains(hardwarePartNumber)) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: New software is compatible with the installed hardware: " + hardwarePartNumber);
				}
				return true;
			} else {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: New software is not compatible with the installed hardware: " + hardwarePartNumber);
				}
				return false;
			}
		} else {
			final HashMap<String, List<String>> hwPartNumbers = updateService.getHwPartNumbers();
			if(hwPartNumbers == null || hwPartNumbers.size() < 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Installed hardware part numbers are not available for any LRU");
					updateService.logInfo("Update Service: Returns as hardware incompatible");
				}
				return false;
			}

			final List<String> localHwPartNumbers = hwPartNumbers.get(newParentCI.getLruType());

			if(localHwPartNumbers == null || localHwPartNumbers.size() < 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Installed hardware part numbers are not available for: " + newParentCI.getLruType());
					updateService.logInfo("Update Service: Returns as hardware incompatible");
				}
				return false;
			}

			for(final String localHwPartNumber : localHwPartNumbers) {
				if(!hardwarePartNumbers.contains(localHwPartNumber)) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service: Installed Hardware: " + localHwPartNumber + " is not present in the hardware part number list");
					}
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Checks the LCP compatibility.
	 *
	 * @param lcpParentCI the lcp parent CI
	 * @param newParentCI the new parent CI
	 *
	 * @return true, if successful
	 */
	private boolean checkLcpCompatibility(final ParentCI lcpParentCI, final ParentCI newParentCI) {
		if(updateService.isInfo()) {
			updateService.logInfo("Update Service: Checking LCP compatibility");
		}

		final String deviceType = hardwareDetails.getDeviceType();

		if(lcpParentCI == null) {
			if(updateService.isInfo()) {
				updateService.logInfo("Update Service: The new LCP Parent CI is not available");
				updateService.logInfo("Update Service: Returns as LCP incompatible");
			}
			return false;
		}

		if(newParentCI == null) {
			if(parentCIs == null || parentCIs.size() <= 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: There are no installed Parent CIs available");
					updateService.logInfo("Update Service: Cannot check the LCP compatibility");
					updateService.logInfo("Update Service: Returns as LCP incompatible");
				}
				return false;
			}
			final ParentCI parentCI = parentCIs.get(deviceType);
			if(parentCI == null) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: There is no installed Parent CI available for: " + deviceType);
					updateService.logInfo("Update Service: Cannot check the LCP compatibility");
					updateService.logInfo("Update Service: Returns as LCP incompatible");
				}
				return false;
			}
			final String expected = lcpParentCI.getReleaseNumber();
			final String actual = parentCI.getReleaseNumber();

			if(expected == null || actual == null) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Release number(s) are null");
					updateService.logInfo("Update Service: Returns as LCP incompatible");
				}
				return false;
			}

			int eLastIndexOfDot = expected.lastIndexOf(".");
			int aLastIndexOfDot = actual.lastIndexOf(".");

			if(eLastIndexOfDot <= 0 || aLastIndexOfDot <= 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Release numbers are not in expected format");
					updateService.logInfo("Update Service: Returns as LCP incompatible");
				}
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
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: The new LCP is incompatible with the installed software: " + deviceType);
					updateService.logInfo("Update Service: Expected: " + expectedSub + " : Actual: " + actualSub);
				}
				return false;
			}
		} else {
			final String expected = lcpParentCI.getReleaseNumber();
			final String actual = newParentCI.getReleaseNumber();

			if(expected == null || actual == null) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Release number(s) are null");
					updateService.logInfo("Update Service: Returns as LCP incompatible");
				}
				return false;
			}

			int eLastIndexOfDot = expected.lastIndexOf(".");
			int aLastIndexOfDot = actual.lastIndexOf(".");

			if(eLastIndexOfDot <= 0 || aLastIndexOfDot <= 0) {
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: Release numbers are not in expected format");
					updateService.logInfo("Update Service: Returns as LCP incompatible");
				}
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
				if(updateService.isInfo()) {
					updateService.logInfo("Update Service: The new LCP is incompatible with the new software: " + deviceType);
					updateService.logInfo("Update Service: Expected: " + expectedSub + " : Actual: " + actualSub);
				}
				return false;
			}
		}

		return true;
	}
}
