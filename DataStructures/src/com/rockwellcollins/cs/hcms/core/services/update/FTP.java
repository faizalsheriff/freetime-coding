package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.UnitManager;

/**
 * The Class FTP executes the FTP Commands. Currently only GET is supported.
 * It runs the FTP command in a separate thread that dies once the FTP process
 * is completed.
 * 
 * @author Raja Sonnia Pattabiraman
 * 
 */
public class FTP implements Runnable {
	
	private UpdateService updateService;

	private FTPAdapter ftpAdapter;

	private String command;

	private String result;
	
	/**
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Sets the FTP adapter.
	 * 
	 * @param ftpAdapter the new FTP adapter
	 */
	public void setFTPAdapter(final FTPAdapter ftpAdapter) {
		this.ftpAdapter = ftpAdapter;
	}

	/**
	 * Sets the command.
	 * 
	 * @param command the new command
	 */
	public void setCommand(final String command) {
		this.command = command;
	}

	/**
	 * Gets the result.
	 * 
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Sets the result.
	 * 
	 * @param result the new result
	 */
	public void setResult(final String result) {
		this.result = result;
	}

	/**
	 * Executes the actual FTP command with the help of native API.
	 * This thread dies immediately after the FTP process.
	 */
	public void run() {
		if (command.equalsIgnoreCase("get")) {
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Starting GET... " + ftpAdapter.getRemoteFile());
			}
			final int returnValue = ftpAdapter.get();
			if (updateService.isInfo()) {
				updateService.logInfo("Update Service: Return value of GET... " + returnValue);
			}
			if (returnValue == 0) {
				result = "success";
			} else {
				UnitManager.Logging.logSevere("Update Service: Return value of GET... " + returnValue);
				result = "failed";
			}
		} else if (command.equalsIgnoreCase("put")) {
			UnitManager.Logging.logSevere("Update Service: FTP PUT is not implemented");
		} else {
			UnitManager.Logging.logSevere("Update Service: FTP: Unknown Command");
		}
	}
}
