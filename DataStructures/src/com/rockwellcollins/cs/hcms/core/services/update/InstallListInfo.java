package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.rockwellcollins.cs.hcms.core.services.update.messages.CAMessage;

/**
 * The Class InstallListInfo describes what to install and what not to install.
 * It also has the FTP details to download the files from appropriate location.
 * 
 * @author Raja Sonnia Pattabiraman
 * 
 */
public class InstallListInfo {
	
	private UpdateService updateService;

	private String ftpUserName;

	private String ftpPassword;

	private String ftpIPAddress;

	private int ftpPortNumber;

	private String pathToLoadable;

	private String pathToCII;

	private HashMap<String, ParentCI> parentCIsToInstall;

	private HashMap<String, List<ChildCI>> childCIsToInstall;

	private HashMap<String, List<ChildCI>> childCIsNotToInstall;

	private List<CAMessage> prevailingVotes;
	
	private boolean isNfs;
	
	private boolean forceLoad;
	
	private boolean isSelfLoad;
	
	private boolean isRebootWait;
	
	/**
	 * Sets the update service.
	 * 
	 * @param updateService the new update service
	 */
	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	/**
	 * Checks if is force load.
	 * 
	 * @return true, if is force load
	 */
	public boolean isForceLoad() {
		return forceLoad;
	}

	/**
	 * Sets the force load.
	 * 
	 * @param forceLoad the new force load
	 */
	public void setForceLoad(final boolean forceLoad) {
		this.forceLoad = forceLoad;
	}

	/**
	 * Checks if is nfs.
	 * 
	 * @return true, if is nfs
	 */
	public boolean isNfs() {
		return isNfs;
	}

	/**
	 * Sets the nfs.
	 * 
	 * @param isNfs the new nfs
	 */
	public void setNfs(final boolean isNfs) {
		this.isNfs = isNfs;
	}

	/**
	 * Checks if is self load.
	 * 
	 * @return true, if is self load
	 */
	public boolean isSelfLoad() {
		return isSelfLoad;
	}

	/**
	 * Sets the self load.
	 * 
	 * @param isSelfLoad the new self load
	 */
	public void setSelfLoad(boolean isSelfLoad) {
		this.isSelfLoad = isSelfLoad;
	}

	/**
	 * Checks if is reboot wait.
	 * 
	 * @return true, if is reboot wait
	 */
	public boolean isRebootWait() {
		return isRebootWait;
	}

	/**
	 * Sets the reboot wait.
	 * 
	 * @param isNfs the new reboot wait
	 */
	public void setRebootWait(boolean isRebootWait) {
		this.isRebootWait = isRebootWait;
	}

	/**
	 * Gets the prevailing votes.
	 * 
	 * @return the prevailing votes
	 */
	public List<CAMessage> getPrevailingVotes() {
		return prevailingVotes;
	}

	/**
	 * Sets the prevailing votes.
	 * 
	 * @param prevailingVotes the new prevailing votes
	 */
	public void setPrevailingVotes(final List<CAMessage> prevailingVotes) {
		this.prevailingVotes = prevailingVotes;
	}

	/**
	 * Gets the child CIs not to install.
	 * 
	 * @return the child CIs not to install
	 */
	public HashMap<String, List<ChildCI>> getChildCIsNotToInstall() {
		return childCIsNotToInstall;
	}

	/**
	 * Sets the child CIs not to install.
	 * 
	 * @param childCIsNotToInstall the child CIs not to install
	 */
	public void setChildCIsNotToInstall(final HashMap<String, List<ChildCI>> childCIsNotToInstall) {
		this.childCIsNotToInstall = childCIsNotToInstall;
	}

	/**
	 * Gets the child CIs to install.
	 * 
	 * @return the child CIs to install
	 */
	public HashMap<String, List<ChildCI>> getChildCIsToInstall() {
		return childCIsToInstall;
	}

	/**
	 * Sets the child CIs to install.
	 * 
	 * @param childCIsToInstall the child CIs to install
	 */
	public void setChildCIsToInstall(final HashMap<String, List<ChildCI>> childCIsToInstall) {
		this.childCIsToInstall = childCIsToInstall;
	}

	/**
	 * Gets the parent CIs to install.
	 * 
	 * @return the parent CIs to install
	 */
	public HashMap<String, ParentCI> getParentCIsToInstall() {
		return parentCIsToInstall;
	}

	/**
	 * Sets the parent CIs to install.
	 * 
	 * @param parentCIsToInstall the parent CIs to install
	 */
	public void setParentCIsToInstall(final HashMap<String, ParentCI> parentCIsToInstall) {
		this.parentCIsToInstall = parentCIsToInstall;
	}

	/**
	 * Gets the ftp IP address.
	 * 
	 * @return the ftp IP address
	 */
	public String getFtpIPAddress() {
		return ftpIPAddress;
	}

	/**
	 * Sets the ftp IP address.
	 * 
	 * @param ftpIPAddress the new ftp IP address
	 */
	public void setFtpIPAddress(final String ftpIPAddress) {
		this.ftpIPAddress = ftpIPAddress;
	}

	/**
	 * Gets the ftp password.
	 * 
	 * @return the ftp password
	 */
	public String getFtpPassword() {
		return ftpPassword;
	}

	/**
	 * Sets the ftp password.
	 * 
	 * @param ftpPassword the new ftp password
	 */
	public void setFtpPassword(final String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	/**
	 * Gets the ftp port number.
	 * 
	 * @return the ftp port number
	 */
	public int getFtpPortNumber() {
		return ftpPortNumber;
	}

	/**
	 * Sets the ftp port number.
	 * 
	 * @param ftpPortNumber the new ftp port number
	 */
	public void setFtpPortNumber(final int ftpPortNumber) {
		this.ftpPortNumber = ftpPortNumber;
	}

	/**
	 * Gets the ftp user name.
	 * 
	 * @return the ftp user name
	 */
	public String getFtpUserName() {
		return ftpUserName;
	}

	/**
	 * Sets the ftp user name.
	 * 
	 * @param ftpUserName the new ftp user name
	 */
	public void setFtpUserName(final String ftpUserName) {
		this.ftpUserName = ftpUserName;
	}

	/**
	 * Gets the path to loadable.
	 * 
	 * @return the path to loadable
	 */
	public String getPathToLoadable() {
		return pathToLoadable;
	}

	/**
	 * Sets the path to loadable.
	 * 
	 * @param pathToLoadable the new path to loadable
	 */
	public void setPathToLoadable(final String pathToLoadable) {
		this.pathToLoadable = pathToLoadable;
	}

	/**
	 * Gets the path to CII.
	 * 
	 * @return the path to CII
	 */
	public String getPathToCII() {
		return pathToCII;
	}

	/**
	 * Sets the path to CII.
	 * 
	 * @param pathToCII the new path to CII
	 */
	public void setPathToCII(final String pathToCII) {
		this.pathToCII = pathToCII;
	}

	/**
	 * Prints the current values.
	 */
	public void printCurrentValues() {
		if (updateService.isInfo()) {
			updateService.logInfo("\n*******************************************\n");
			updateService.logInfo("Printing current values of InstallListInfo");
			updateService.logInfo("ftpUserName:                       " + ftpUserName);
			updateService.logInfo("ftpPassword:                       " + ftpPassword);
			updateService.logInfo("ftpIPAddress:                      " + ftpIPAddress);
			updateService.logInfo("ftpPortNumber:                     " + ftpPortNumber);
			updateService.logInfo("pathToCII:                         " + pathToCII);
			updateService.logInfo("pathToLoadable:                    " + pathToLoadable);
			updateService.logInfo("isNfs:                             " + isNfs);
			updateService.logInfo("forceLoad:                         " + forceLoad);
			updateService.logInfo("isSelfLoad:                        " + isSelfLoad);
			updateService.logInfo("isRebootWait:                      " + isRebootWait);

			updateService.logInfo("parentCIsToInstall:                " + parentCIsToInstall.size());

			for (final Entry<String, ParentCI> parentCIEntry : parentCIsToInstall.entrySet()) {
				final ParentCI parentCI = parentCIEntry.getValue();
				updateService.logInfo("	parentCI.getLruType():             " + parentCI.getLruType());
				updateService.logInfo("	parentCI.getCpn():                 " + parentCI.getCpn());
				updateService.logInfo("	parentCI.isPrimitive():            " + parentCI.isPrimitive());
				updateService.logInfo("	parentCI.getParent811FileName():   " + parentCI.getParent811FileName());
				updateService.logInfo("	parentCI.getMd5Value():            " + parentCI.getMd5Value());
				updateService.logInfo("	parentCI.getDescription():         " + parentCI.getDescription());
				updateService.logInfo("	parentCI.getBuildNumber():         " + parentCI.getBuildNumber());

				if (!parentCI.isPrimitive()) {
					final List<ChildCI> toInstall = childCIsToInstall.get(parentCI.getLruType());
					if (toInstall != null) {
						updateService.logInfo("	ChildCIs To Install:               " + toInstall.size());
						for (final ChildCI childCI : toInstall) {
							updateService.logInfo("		childCI.getChildCIType():          " + childCI.getChildCIType());
							updateService.logInfo("		childCI.getCpn():                  " + childCI.getCpn());
							updateService.logInfo("		childCI.getFileName():             " + childCI.getChild811FileName());
							updateService.logInfo("		childCI.getMd5Value():             " + childCI.getMd5Value());
							updateService.logInfo("		childCI.getBuildNumber():          " + childCI.getBuildNumber());
						}
					} else {
						updateService.logInfo("	ChildCIs To Install:               0");
					}

					final List<ChildCI> notToInstall = childCIsNotToInstall.get(parentCI.getLruType());
					if (notToInstall != null) {
						updateService.logInfo("	ChildCIs Not To Install:           " + notToInstall.size());
						for (final ChildCI childCI : notToInstall) {
							updateService.logInfo("		childCI.getChildCIType():          " + childCI.getChildCIType());
							updateService.logInfo("		childCI.getCpn():                  " + childCI.getCpn());
							updateService.logInfo("		childCI.getFileName():             " + childCI.getChild811FileName());
							updateService.logInfo("		childCI.getMd5Value():             " + childCI.getMd5Value());
							updateService.logInfo("		childCI.getBuildNumber():          " + childCI.getBuildNumber());
						}
					} else {
						updateService.logInfo("	ChildCIs Not To Install:           0");
					}
				}
				updateService.logInfo("\n");
			}
			updateService.logInfo("\n*******************************************\n");
		}
	}
}
