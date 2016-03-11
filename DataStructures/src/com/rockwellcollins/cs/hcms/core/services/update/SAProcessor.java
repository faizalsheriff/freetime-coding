package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.collections.QueueListener;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.services.handlers.HandlerPropertyNotFoundException;
import com.rockwellcollins.cs.hcms.core.services.handlers.PropertyMap;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManagerStatus;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;

/**
 * The Class SAProcessor is used to process all the SA Messages
 * that are being received and set the load status information in
 * terms of states.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * @see SAMessage
 * 
 */
public class SAProcessor implements QueueListener<SAMessage> {
	
	private UpdateService updateService;
	
	private UpdateHandler updateHandler;
	
	private StatusQueueProcessor statusQueueProcessor;
	
	private TreeMap<String, SAMessage> statusDetails;
	
	private HashMap<String, Long> statusTimeoutDetails;
	
	private int statusTimeout;
	
	private int statusQueueTimeout;
	
	private StateManager stateManager;
	
	private String savedLruList = "unknown";
	private String savedSystemLoadStatus = "unknown";
	private String savedDisableLoadStatus = "unknown";
	
	/**
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Sets the update handler.
	 * 
	 * @param updateHandler the new update handler
	 */
	public void setUpdateHandler(UpdateHandler updateHandler) {
		this.updateHandler = updateHandler;
	}
	
	/**
	 * Sets the status queue processor.
	 * 
	 * @param statusQueueProcessor the new status queue processor
	 */
	public void setStatusQueueProcessor(StatusQueueProcessor statusQueueProcessor) {
		this.statusQueueProcessor = statusQueueProcessor;
	}
	
	/**
	 * Sets the status timeout.
	 * 
	 * @param statusTimeout the new status timeout
	 */
	public void setStatusTimeout(int statusTimeout) {
		this.statusTimeout = statusTimeout;
	}

	/**
	 * Gets the status timeout.
	 * 
	 * @return the status timeout
	 */
	private int getStatusTimeout() {
		return statusTimeout;
	}

	/**
	 * Sets the status queue timeout.
	 * 
	 * @param statusQueueTimeout the new status queue timeout
	 */
	public void setStatusQueueTimeout(int statusQueueTimeout) {
		this.statusQueueTimeout = statusQueueTimeout;
	}
	
	/**
	 * Sets the state manager.
	 * 
	 * @param stateManager the new state manager
	 */
	public void setStateManager(StateManager stateManager) {
		this.stateManager = stateManager;
	}
	
	private TreeMap<String, SAMessage> getStatusDetails() {
		if(statusDetails == null) {
			statusDetails = new TreeMap<String, SAMessage>();
		}
		return statusDetails;
	}
	
	private HashMap<String, Long> getStatusTimeoutDetails() {
		if(statusTimeoutDetails == null) {
			statusTimeoutDetails = new HashMap<String, Long>();
		}
		return statusTimeoutDetails;
	}
	
	/**
	 * Status details put.
	 * 
	 * @param ipAddress the ip address
	 * @param sa the sa
	 */
	private synchronized void statusDetailsPut(final String ipAddress, final SAMessage sa) {
		getStatusDetails().put(ipAddress, sa);
	}
	
	/**
	 * Status details contains key.
	 * 
	 * @param ipAddress the ip address
	 * 
	 * @return true, if successful
	 */
	private synchronized boolean statusDetailsContainsKey(final String ipAddress) {
		return getStatusDetails().containsKey(ipAddress);
	}
	
	/**
	 * Status details get.
	 * 
	 * @param ipAddress the ip address
	 * 
	 * @return the SA message
	 */
	private synchronized SAMessage statusDetailsGet(final String ipAddress) {
		return getStatusDetails().get(ipAddress);
	}
	
	/**
	 * Status details size.
	 * 
	 * @return the int
	 */
	private synchronized int statusDetailsSize() {
		return getStatusDetails().size();
	}
	
	/**
	 * Status timeout details put.
	 * 
	 * @param ipAddress the ip address
	 * @param time the time
	 */
	private synchronized void statusTimeoutDetailsPut(final String ipAddress, final Long time) {
		getStatusTimeoutDetails().put(ipAddress, time);
	}
	
	/**
	 * Status timeout details get.
	 * 
	 * @param ipAddress the ip address
	 * 
	 * @return the long
	 */
	private synchronized Long statusTimeoutDetailsGet(final String ipAddress) {
		return getStatusTimeoutDetails().get(ipAddress);
	}
	
	/**
	 * Compare SA messages.
	 * 
	 * @param lastStatus the last status
	 * @param currentStatus the current status
	 * 
	 * @return true, if successful
	 */
	private boolean compareSAMessages(final SAMessage lastStatus, final SAMessage currentStatus) {
		boolean isEqual = (lastStatus.getIpAddress().equals(currentStatus.getIpAddress()));
		isEqual = isEqual && (lastStatus.getLruType().equals(currentStatus.getLruType()));
		isEqual = isEqual && (lastStatus.getLruInstance().equals(currentStatus.getLruInstance()));
		isEqual = isEqual && (lastStatus.getTotalSteps() == currentStatus.getTotalSteps());
		isEqual = isEqual && (lastStatus.getStepNumber() == currentStatus.getStepNumber());
		isEqual = isEqual && (lastStatus.getStepName().equals(currentStatus.getStepName()));
		isEqual = isEqual && (lastStatus.getSubStepName().equals(currentStatus.getSubStepName()));
		isEqual = isEqual && (lastStatus.getSubStepPercentage().equals(currentStatus.getSubStepPercentage()));
		return isEqual;
	}
	
	/**
	 * This method will be called whenever a SA Message is received.
	 * It updates the load status as a state only if the current LRU 
	 * is the master state manager.
	 * 
	 * @param processor The queue processor processing the event
	 * @param item the queue item
	 */
	public void queueProcessorItemReceived(final QueueProcessor<SAMessage> processor, final SAMessage item) {
		// Don't process final status more than once if there is 
		// NO difference between the current and old messages
		if (item.getStepNumber() == item.getTotalSteps()) {
			if(statusDetailsContainsKey(item.getIpAddress())) {
				final SAMessage lastStatus = statusDetailsGet(item.getIpAddress());
				if (lastStatus.getStepNumber() == lastStatus.getTotalSteps()) {
					if(compareSAMessages(lastStatus, item)) {
						return;
					}
				}
			}
		}
		
		statusDetailsPut(item.getIpAddress(), item);
		statusTimeoutDetailsPut(item.getIpAddress(), UnitManager.Timing.getTimeAlive());
		if (stateManager != null && stateManager.getStatus() == StateManagerStatus.MASTER) {
			setStatus();
		}
		
		final String loadStatusIP = updateHandler.getLoadStatusIp();
		if(loadStatusIP != null && loadStatusIP.length() > 0) {
			setSoftwareLoadStatus(loadStatusIP);
		}
		statusQueueProcessor.setTimeout(statusQueueTimeout);
	}

	/**
	 * This method will be called whenever there is at least one LRU 
	 * whose load status is not marked as completed. When there is no
	 * LRU whose load status is incomplete, this method will not be called.
	 * The timeout for this method will be set to 0 when all the LRU's load
	 * status is marked as complete.
	 * 
	 * @param processor the queue processor processing the event
	 */
	public void queueProcessorTimeout(final QueueProcessor<SAMessage> processor) {
		final String loadStatusIP = updateHandler.getLoadStatusIp();
		if(loadStatusIP != null && loadStatusIP.length() > 0) {
			setSoftwareLoadStatus(loadStatusIP);
		}
	}
	
	/**
	 * Sets the status details in terms of states.
	 */
	private void setStatus() {
		try {
			final String pDisableSoftwareLoadStatus = UpdateHandler.Properties.DISABLE_SOFTWARE_LOAD_STATUS.getPropertyName();
			final String pSystemLoadStatus = UpdateHandler.Properties.SYSTEM_LOAD_STATUS.getPropertyName();
			final String pLrusLoadStatusList = UpdateHandler.Properties.LRUS_LOAD_STATUS_LIST.getPropertyName();
			
			int grandTotalSteps = 0;
			int grandCurrentSteps = 0;
			boolean isLaProcessNotComplete = false;
			if (statusDetailsSize() > 0) {
				for (final Entry<String, SAMessage> statusDetailsEntry : getStatusDetails().entrySet()) {
					final SAMessage sa = statusDetailsEntry.getValue();
					grandTotalSteps = grandTotalSteps + sa.getTotalSteps();
					grandCurrentSteps = grandCurrentSteps + sa.getStepNumber();
					final String subStepName = sa.getSubStepName();
					if(!subStepName.startsWith(LAProcessor.LA_PROCESS_COMPLETE)) {
						isLaProcessNotComplete = true;
					}
				}
				final PropertyMap propertyMap = new PropertyMap();
				String disableSoftwareLoadStatus = null;
				String systemLoadStatus = null;
				final String lruList = updateHandler.convertStatusToStateValue(getStatusDetails());
				if(lruList != null) {
					disableSoftwareLoadStatus = "false";
					if (grandTotalSteps == 0) {
						if(isLaProcessNotComplete) {
							systemLoadStatus = "0";
						} else {
							systemLoadStatus = "100";
						}
					} else {
						final int systemStatus = (int)(((float)grandCurrentSteps / (float)grandTotalSteps) * 100);
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Grand total steps: " + grandTotalSteps);
							updateService.logInfo("Update Service - SAProcessor: Grand current steps: " + grandCurrentSteps);
							updateService.logInfo("Update Service - SAProcessor: System status: " + systemStatus);
						}
						systemLoadStatus = String.valueOf(systemStatus);
					}

					if(!updateHandler.getProperty(pDisableSoftwareLoadStatus).equals(disableSoftwareLoadStatus)) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Setting " + pDisableSoftwareLoadStatus + " to: " + disableSoftwareLoadStatus);
						}
						if(!savedDisableLoadStatus.equals(disableSoftwareLoadStatus)) {
							savedDisableLoadStatus = disableSoftwareLoadStatus;
							propertyMap.put(pDisableSoftwareLoadStatus, disableSoftwareLoadStatus);
						}
					}
					if(!updateHandler.getProperty(pSystemLoadStatus).equals(systemLoadStatus)) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Setting " + pSystemLoadStatus + " to: " + systemLoadStatus);
						}
						if(!savedSystemLoadStatus.equals(systemLoadStatus)) {
							savedSystemLoadStatus = systemLoadStatus;
							propertyMap.put(pSystemLoadStatus, systemLoadStatus);
						}
					}
					if(!updateHandler.getProperty(pLrusLoadStatusList).equals(lruList)) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Setting " + pLrusLoadStatusList + " to: " + lruList);
						}
						if(!savedLruList.equals(lruList)) {
							savedLruList = lruList;
							propertyMap.put(pLrusLoadStatusList, lruList);
						}
					}
					if(propertyMap.size() > 0) {
						updateHandler.setProperty(propertyMap);
					}
				} else {
					UnitManager.Logging.logSevere("Update Service - SAProcessor: Failed set status details as a state");
					if(!updateHandler.getProperty(pDisableSoftwareLoadStatus).equals("true")) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Setting " + pDisableSoftwareLoadStatus + " to: true");
							updateService.logInfo("Update Service - SAProcessor: Setting " + pLrusLoadStatusList + " to: ");
						}
						propertyMap.put(pDisableSoftwareLoadStatus, "true");
						propertyMap.put(pLrusLoadStatusList, "");
						updateHandler.setProperty(propertyMap);
					}
				}
			} else {
				if(!updateHandler.getProperty(pDisableSoftwareLoadStatus).equals("true")) {
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - SAProcessor: Setting " + pDisableSoftwareLoadStatus + " to: true");
						updateService.logInfo("Update Service - SAProcessor: Setting " + pLrusLoadStatusList + " to: ");
					}
					final PropertyMap propertyMap = new PropertyMap();
					propertyMap.put(pDisableSoftwareLoadStatus, "true");
					propertyMap.put(pLrusLoadStatusList, "");
					updateHandler.setProperty(propertyMap);
				}
			}
		} catch(HandlerPropertyNotFoundException hpnfe) {
			UnitManager.Logging.logSevere(hpnfe);
		} catch(Exception e) {
			UnitManager.Logging.logSevere("Update Service - SAProcessor: Error while setting the states for load status list", e);
		}
	}
	
	/**
	 * Gets the SA message.
	 * 
	 * @param ipAddress the ip address
	 * 
	 * @return the SA message
	 */
	public void setSoftwareLoadStatus(final String ipAddress) {
		try {
			final String pLoadStatusText = UpdateHandler.Properties.LOAD_STATUS_TEXT.getPropertyName();;
			final String pOverAllStatus = UpdateHandler.Properties.OVERALL_STATUS.getPropertyName();;
			final String pOverAllStatusText = UpdateHandler.Properties.OVERALL_STATUS_TEXT.getPropertyName();;
			final String pSubStepStatus = UpdateHandler.Properties.SUBSTEP_STATUS.getPropertyName();;
			final String pSubStepStatusText = UpdateHandler.Properties.SUBSTEP_STATUS_TEXT.getPropertyName();;
			
			if(statusDetails != null) {
				final SAMessage sa = statusDetailsGet(ipAddress);
				if (sa == null) {
					if (updateService.isInfo()) {
						updateService.logInfo("Update Service - SAProcessor: SA Message is null for: " + ipAddress);
					}
					final PropertyMap propertyMap = new PropertyMap();
					propertyMap.put(pLoadStatusText, "Status Not Available " + ipAddress);
					propertyMap.put(pOverAllStatus, "0");
					propertyMap.put(pOverAllStatusText, "");
					propertyMap.put(pSubStepStatus, "0");
					propertyMap.put(pSubStepStatusText, "");
					updateHandler.setProperty(propertyMap);
				} else {
					String loadStatusText;
					String overAllStatus;
					String overAllStatusText;
					String subStepStatus;
					String subStepStatusText;
					final PropertyMap propertyMap = new PropertyMap();
					
					overAllStatusText = sa.getStepNumber() + "/" + sa.getTotalSteps() + " - " + sa.getStepName();
					
					final String subStepPercentage = sa.getSubStepPercentage();
					if (subStepPercentage == null || subStepPercentage.trim().length() == 0) {
						subStepStatus = "0";
					} else {
						if (subStepPercentage.indexOf("%") > 0) {
							subStepStatus = subStepPercentage.substring(0, subStepPercentage.indexOf("%"));
						} else {
							subStepStatus = "0";
						}
					}
					
					subStepStatusText = sa.getSubStepName();
					
					if (sa.getTotalSteps() == 0) {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Setting overAllStatus to 100 for: " + ipAddress);
						}
						if(subStepStatus.equals("100") || sa.getSubStepName().startsWith(LAProcessor.LA_PROCESS_COMPLETE)) {
							overAllStatus = "100";
							loadStatusText = sa.getLruInstance() + " (" + sa.getIpAddress() + ")" + ": Completed";
						} else {
							overAllStatus = "0";
							loadStatusText = sa.getLruInstance() + " (" + sa.getIpAddress() + ")" + ": Loading...";
						}
					} else {
						final int overAllStatusValue = (int)(((float)sa.getStepNumber() / (float)sa.getTotalSteps()) * 100);
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Setting overAllStatus to: " + overAllStatusValue + " for: " + ipAddress);
						}
						overAllStatus = String.valueOf(overAllStatusValue);
						if(overAllStatus.equals("100")) {
							loadStatusText = sa.getLruInstance() + " (" + sa.getIpAddress() + ")" + ": Completed";
						} else {
							loadStatusText = sa.getLruInstance() + " (" + sa.getIpAddress() + ")" + ": Loading...";
						}
					}
					
					if(sa.getStepNumber() != sa.getTotalSteps()) {
						long lastUpdateTime = statusTimeoutDetailsGet(ipAddress);
						long currentTime = UnitManager.Timing.getTimeAlive();
						if ((currentTime - lastUpdateTime) > getStatusTimeout()) {
							loadStatusText = sa.getLruInstance() + " (" + sa.getIpAddress() + ")" + ": Unknown";
							if(updateService.isInfo()) {
								updateService.logInfo("Update Service - SAProcessor: Status got timedout for: " + ipAddress);
							}
						}
					} else {
						if(updateService.isInfo()) {
							updateService.logInfo("Update Service - SAProcessor: Load already marked as complete for: " + ipAddress);
						}
					}
					
					if(updateService.isInfo()) {
						updateService.logInfo("Update Service - SAProcessor: Status info for: " + ipAddress);
						updateService.logInfo("Update Service - SAProcessor: loadStatusText: " + loadStatusText);
						updateService.logInfo("Update Service - SAProcessor: overAllStatus: " + overAllStatus);
						updateService.logInfo("Update Service - SAProcessor: overAllStatusText: " + overAllStatusText);
						updateService.logInfo("Update Service - SAProcessor: subStepStatus: " + subStepStatus);
						updateService.logInfo("Update Service - SAProcessor: subStepStatusText: " + subStepStatusText);
					}
					if(!updateHandler.getProperty(pLoadStatusText).equals(loadStatusText)) {
						propertyMap.put(pLoadStatusText, loadStatusText);
					}
					if(!updateHandler.getProperty(pOverAllStatus).equals(overAllStatus)) {
						propertyMap.put(pOverAllStatus, overAllStatus);
					}
					if(!updateHandler.getProperty(pOverAllStatusText).equals(overAllStatusText)) {
						propertyMap.put(pOverAllStatusText, overAllStatusText);
					}
					if(!updateHandler.getProperty(pSubStepStatus).equals(subStepStatus)) {
						propertyMap.put(pSubStepStatus, subStepStatus);
					}
					if(!updateHandler.getProperty(pSubStepStatusText).equals(subStepStatusText)) {
						propertyMap.put(pSubStepStatusText, subStepStatusText);
					}
					if(propertyMap.size() > 0) {
						updateHandler.setProperty(propertyMap);
					}
				}
			} else {
				UnitManager.Logging.logSevere("Update Service - SAProcessor: Status details is null");
				final PropertyMap propertyMap = new PropertyMap();
				propertyMap.put(pLoadStatusText, "Status Not Available for: " + ipAddress);
				propertyMap.put(pOverAllStatus, "0");
				propertyMap.put(pOverAllStatusText, "");
				propertyMap.put(pSubStepStatus, "0");
				propertyMap.put(pSubStepStatusText, "");
				updateHandler.setProperty(propertyMap);
			}
		} catch(HandlerPropertyNotFoundException hpnfe) {
			UnitManager.Logging.logSevere(hpnfe);
		} catch(Exception e) {
			UnitManager.Logging.logSevere("Update Service - SAProcessor: Error while setting the load status states for: " + ipAddress, e);
		}
	}
}
