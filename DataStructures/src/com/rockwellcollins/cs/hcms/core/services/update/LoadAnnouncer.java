package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.update.messages.LAMessage;

/**
 * The Class LoadAnnouncer sends Load Announcement to all LRUs. This class
 * is used only when new software is available in a removable media plugged
 * in to a LRU. The LRU that has the removable media checks for the new 
 * software availability and forms a new LA message which is sent to all 
 * other LRUs through this class.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * @see MAProcessor
 *  
 */
public class LoadAnnouncer implements Runnable {
	
	private LAMessage la;
	
	private int laMessageInterval;
	
	private int sendLimit;
	
	private UpdateService updateService;
	
	private boolean isRunning;
	
	/**
	 * Gets the la.
	 * 
	 * @return the la
	 */
	public LAMessage getLa() {
		return la;
	}

	/**
	 * Sets the la.
	 * 
	 * @param la the new la
	 */
	public void setLa(final LAMessage la) {
		this.la = la;
	}
	
	/**
	 * Sets the la message interval.
	 * 
	 * @param laMessageInterval the new la message interval
	 */
	public void setLaMessageInterval(final int laMessageInterval) {
		this.laMessageInterval = laMessageInterval;
	}

	/**
	 * Sets the send limit.
	 * 
	 * @param sendLimit the new send limit
	 */
	public void setSendLimit(final int sendLimit) {
		this.sendLimit = sendLimit;
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
	 * Sets the announcer thread as running.
	 * 
	 * @param isRunning the new running
	 */
	public void setRunning(final boolean isRunning) {
		this.isRunning = isRunning;
	}

	/**
	 * Sends the LA message for every few seconds that is configured
	 * to sleep in between. It sends the LA Message as long as the 
	 * send limit.
	 */
	public void run() {
		if(la == null) {
			isRunning = false;
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service - LoadAnnouncer: LA is null. LoadAnnouncer cannot start"));
			return;
		}
		int i = 0;
		while(isRunning && i < sendLimit) {
			try {
				i++;
				updateService.send(la);
				Thread.sleep(laMessageInterval);
				la.setCurrentTime(String.valueOf(UnitManager.Timing.getTimeAlive()));
			}
			catch(final InterruptedException ie) {
				UnitManager.Logging.logSevere("Update Service: LoadAnnouncer: Thread interrupted while sleeping", ie);
			}
			catch(final ServiceIOException sioe) {
				UnitManager.Logging.logSevere("Update Service: LoadAnnouncer: Error while sending LA Message", sioe);
			}
		}
	}
	
	/**
	 * Stops the LoadAnnouncer thread from sending LA message.
	 */
	public void stop() {
		isRunning = false;
	}
}
