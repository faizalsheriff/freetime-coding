package com.rockwellcollins.cs.hcms.core.services.update;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.ServiceIOException;
import com.rockwellcollins.cs.hcms.core.services.update.messages.MAMessage;

/**
 * The Class MediaAnnouncer sends Media Announcement to all LRUs. This class
 * is used only when new software is available in a removable media plugged
 * in to a LRU. The LRU where the user browses through the UI to start the 
 * load forms a new MA Message which is sent to all other LRUs through this
 * class.
 * 
 * @author Raja Sonnia Pattabiraman
 * @see UpdateService
 * @see MAProcessor
 * 
 */
public class MediaAnnouncer implements Runnable {
	
	private MAMessage ma;
	
	private int maMessageInterval;
	
	private int sendLimit;
	
	private UpdateService updateService;
	
	private boolean isRunning;
	
	private String ipAddress;
	
	/**
	 * Sets the ip address.
	 * 
	 * @param ipAddress the new ip address
	 */
	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
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
	 * Sets the ma.
	 * 
	 * @param ma the new ma
	 */
	public void setMa(final MAMessage ma) {
		this.ma = ma;
	}

	/**
	 * Sets the ma message interval.
	 * 
	 * @param maMessageInterval the new ma message interval
	 */
	public void setMaMessageInterval(final int maMessageInterval) {
		this.maMessageInterval = maMessageInterval;
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

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if(ma == null) {
			isRunning = false;
			UnitManager.Logging.logSevere("Update Service - MediaAnnouncer: MA is null. MediaAnnouncer cannot start");
			return;
		}
		boolean isMulticast = false;
		int i = 0;
		if(ipAddress == null || ipAddress.length() <= 0) {
			isMulticast = true;
		} else {
			isMulticast = false;
		}
		
		while(isRunning && i < sendLimit) {
			try {
				i++;
				if(isMulticast) {
					updateService.send(ma);
				} else {
					updateService.send(ma, InetAddress.getByName(ipAddress), updateService.getPort());
				}
				Thread.sleep(maMessageInterval);
				ma.setCurrentTime(String.valueOf(UnitManager.Timing.getTimeAlive()));
			} catch(final InterruptedException ie) {
				UnitManager.Logging.logSevere("Update Service: MediaAnnouncer: Thread interrupted while sleeping", ie);
			} catch(final ServiceIOException sioe) {
				UnitManager.Logging.logSevere("Update Service: MediaAnnouncer: Error while sending LA Message", sioe);
			} catch (final UnknownHostException uhe) {
				UnitManager.Logging.logSevere(uhe);
			}
		}
		setRunning(false);
	}
	
	/**
	 * Stop.
	 */
	public void stop() {
		setRunning(false);
	}
}
