package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.update.messages.SAMessage;

public class StatusAnnouncer implements Runnable {
	
	private SAMessage sa;
	
	private int saMessageInterval;
	
	private int sendLimit;
	
	private UpdateService updateService;
	
	private boolean isRunning;
	
	/**
	 * Gets the sa.
	 * 
	 * @return the sa
	 */
	public SAMessage getSa() {
		return sa;
	}

	/**
	 * Sets the sa.
	 * 
	 * @param sa the new sa
	 */
	public void setSa(final SAMessage sa) {
		this.sa = sa;
	}
	
	/**
	 * Sets the sa message interval.
	 * 
	 * @param saMessageInterval the new sa message interval
	 */
	public void setSaMessageInterval(final int saMessageInterval) {
		this.saMessageInterval = saMessageInterval;
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
		if(sa == null) {
			isRunning = false;
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service: StatusAnnouncer - SA is null. StatusAnnouncer cannot start"));
			return;
		}
		int i = 0;
		while(isRunning && i < sendLimit) {
			try {
				i++;
				updateService.send(sa);
				Thread.sleep(saMessageInterval);
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
