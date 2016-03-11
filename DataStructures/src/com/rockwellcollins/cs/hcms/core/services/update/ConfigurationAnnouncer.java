package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.update.messages.CAMessage;

/**
 * The Class ConfigurationAnnouncer sends the Configuration Announcement to all
 * the LRUs. The message is sent as a UDP multicast. The message will be sent 
 * for every configured number of milli seconds in a separate thread.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see CAMessage
 * 
 */
public class ConfigurationAnnouncer implements Runnable {

	private CAMessage ca;

	private int caMessageInterval;

	private UpdateService updateService;

	private boolean isRunning;

	/**
	 * Gets the ca.
	 * 
	 * @return the ca
	 */
	public CAMessage getCa() {
		return ca;
	}

	/**
	 * Sets the ca.
	 * 
	 * @param ca the new ca
	 */
	public void setCa(final CAMessage ca) {
		this.ca = ca;
	}

	/**
	 * Gets the ca message interval.
	 * 
	 * @return the ca message interval
	 */
	public int getCaMessageInterval() {
		return caMessageInterval;
	}

	/**
	 * Sets the ca message interval.
	 * 
	 * @param caMessageInterval the new ca message interval
	 */
	public void setCaMessageInterval(final int caMessageInterval) {
		this.caMessageInterval = caMessageInterval;
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
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(final UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Checks if is running.
	 * 
	 * @return true, if is running
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Sets the running.
	 * 
	 * @param isRunning the new running
	 */
	public void setRunning(final boolean isRunning) {
		this.isRunning = isRunning;
	}

	/**
	 * The run method will run forever until the stop method is called.
	 * It sends the Configuration Announcement message for every
	 * configured number of milliseconds.
	 */
	public void run() {
		if (ca == null) {
			isRunning = false;
			UnitManager.Logging.logSevere(new UpdateServiceException("Update Service - ConfigurationAnnouncer: CA is null. ConfigurationAnnouncer cannot start"));
			return;
		}
		while (isRunning) {
			try {
				updateService.send(ca);
				Thread.sleep(caMessageInterval);
			} catch (final InterruptedException ie) {
				UnitManager.Logging.logSevere("Update Service: ConfigurationAnnouncer: Thread interrupted while sleeping", ie);
			} catch (final ServiceIOException sioe) {
				UnitManager.Logging.logSevere("Update Service: ConfigurationAnnouncer: Error while sending CA Message", sioe);
			}
		}
	}

	/**
	 * Stops the Configuration Announcer thread.
	 */
	public void stop() {
		isRunning = false;
	}
}
