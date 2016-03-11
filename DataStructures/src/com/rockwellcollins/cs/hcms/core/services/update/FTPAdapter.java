package com.rockwellcollins.cs.hcms.core.services.update;

import com.rockwellcollins.cs.hcms.core.services.adapter.Adapter;

/**
 * The Class FTPAdapter is an abstract class that exposes methods for basic
 * FTP commands - GET, PUT, DIR, STATUS.
 * 
 * @author Raja Sonnia Pattabiraman
 * 
 */
public abstract class FTPAdapter extends Adapter {

	private static final long serialVersionUID = 1L;

	private String userName;

	private String password;

	private String remoteIP;

	private String remotePort;

	private String remoteDir;

	private String remoteFile;

	private String localDir;

	private String localFile;

	/**
	 * Executes the FTP GET command.
	 * 
	 * @return the int
	 */
	public abstract int get();

	/**
	 * Executes the FTP PUT command.
	 * 
	 * @return the int
	 */
	public abstract int put();

	/**
	 * Lists files in the current directory.
	 * 
	 * @return the int
	 */
	public abstract int dir();

	/**
	 * Obtains the current status of the FTP process.
	 * 
	 * @return the status
	 */
	public abstract String getStatus();

	/**
	 * Gets the dir list.
	 * 
	 * @return the dir list
	 */
	public abstract String getDirList();

	/**
	 * Gets the user name.
	 * 
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param userName the new user name
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password the new password
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Gets the remote IP.
	 * 
	 * @return the remote IP
	 */
	public String getRemoteIP() {
		return remoteIP;
	}

	/**
	 * Sets the remote IP.
	 * 
	 * @param remoteIP the new remote IP
	 */
	public void setRemoteIP(final String remoteIP) {
		this.remoteIP = remoteIP;
	}

	/**
	 * Gets the remote port.
	 * 
	 * @return the remote port
	 */
	public String getRemotePort() {
		return remotePort;
	}

	/**
	 * Sets the remote port.
	 * 
	 * @param remotePort the new remote port
	 */
	public void setRemotePort(final String remotePort) {
		this.remotePort = remotePort;
	}

	/**
	 * Gets the remote dir.
	 * 
	 * @return the remote dir
	 */
	public String getRemoteDir() {
		return remoteDir;
	}

	/**
	 * Sets the remote dir.
	 * 
	 * @param remoteDir the new remote dir
	 */
	public void setRemoteDir(final String remoteDir) {
		this.remoteDir = remoteDir;
	}

	/**
	 * Gets the remote file.
	 * 
	 * @return the remote file
	 */
	public String getRemoteFile() {
		return remoteFile;
	}

	/**
	 * Sets the remote file.
	 * 
	 * @param remoteFile the new remote file
	 */
	public void setRemoteFile(final String remoteFile) {
		this.remoteFile = remoteFile;
	}

	/**
	 * Gets the local dir.
	 * 
	 * @return the local dir
	 */
	public String getLocalDir() {
		return localDir;
	}

	/**
	 * Sets the local dir.
	 * 
	 * @param localDir the new local dir
	 */
	public void setLocalDir(final String localDir) {
		this.localDir = localDir;
	}

	/**
	 * Gets the local file.
	 * 
	 * @return the local file
	 */
	public String getLocalFile() {
		return localFile;
	}

	/**
	 * Sets the local file.
	 * 
	 * @param localFile the new local file
	 */
	public void setLocalFile(final String localFile) {
		this.localFile = localFile;
	}
}
