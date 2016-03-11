package com.rockwellcollins.cs.hcms.core.services.update;

public class InstallStatus {
	
	private UpdateService updateService;
	
	private String lruType;
	
	private String updateTime;
	
	private String ciiFile;
	
	private String loadableFile;
	
	private int totalSteps;
	
	private String overallInstall;
	
	private String LA;
	
	private String statesAlreadySet;

	private boolean isProbablyOldCIIFile;
	
	private String partNumber;
	
	private String buildNumber;
	
	private boolean isForceLoad;
	
	private boolean isSecondaryLru;
	
	private boolean isBackupLru;
	
	private boolean isAborted;
	
	private String abortReason;
	
	public void setUpdateService(final UpdateService updateService) {
		this.updateService = updateService;
	}

	public String getLruType() {
		return lruType;
	}

	public void setLruType(final String lruType) {
		this.lruType = lruType;
	}

	public int getTotalSteps() {
		return totalSteps;
	}

	public void setTotalSteps(final int totalSteps) {
		this.totalSteps = totalSteps;
	}

	public String getOverallInstall() {
		return overallInstall;
	}

	public void setOverallInstall(final String overallInstall) {
		this.overallInstall = overallInstall;
	}

	public String getLA() {
		return LA;
	}

	public void setLA(final String LA) {
		this.LA = LA;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getCiiFile() {
		return ciiFile;
	}

	public void setCiiFile(String ciiFile) {
		this.ciiFile = ciiFile;
	}

	public String getLoadableFile() {
		return loadableFile;
	}

	public void setLoadableFile(String loadableFile) {
		this.loadableFile = loadableFile;
	}
	
	public String getStatesAlreadySet() {
		return statesAlreadySet;
	}

	public void setStatesAlreadySet(String statesAlreadySet) {
		this.statesAlreadySet = statesAlreadySet;
	}
	
	public boolean isProbablyOldCIIFile() {
		return isProbablyOldCIIFile;
	}

	public void setProbablyOldCIIFile(boolean isProbablyOldCIIFile) {
		this.isProbablyOldCIIFile = isProbablyOldCIIFile;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public boolean isForceLoad() {
		return isForceLoad;
	}

	public void setForceLoad(boolean isForceLoad) {
		this.isForceLoad = isForceLoad;
	}

	public boolean isSecondaryLru() {
		return isSecondaryLru;
	}

	public void setSecondaryLru(boolean isSecondaryLru) {
		this.isSecondaryLru = isSecondaryLru;
	}

	public boolean isBackupLru() {
		return isBackupLru;
	}

	public void setBackupLru(boolean isBackupLru) {
		this.isBackupLru = isBackupLru;
	}

	public boolean isAborted() {
		return isAborted;
	}

	public void setAborted(boolean isAborted) {
		this.isAborted = isAborted;
	}

	public String getAbortReason() {
		return abortReason;
	}

	public void setAbortReason(String abortReason) {
		this.abortReason = abortReason;
	}
	
	public void printCurrentValues() {
		if (updateService.isInfo()) {
			updateService.logInfo("*******************************************");
			updateService.logInfo("Printing current values of InstallStatus");
			updateService.logInfo("lruType:               " + lruType);
			updateService.logInfo("updateTime:            " + updateTime);
			updateService.logInfo("ciiFile:               " + ciiFile);
			updateService.logInfo("loadableFile:          " + loadableFile);
			updateService.logInfo("totalSteps:            " + totalSteps);
			updateService.logInfo("overallInstall:        " + overallInstall);
			updateService.logInfo("statesAlreadySet:      " + statesAlreadySet);
			updateService.logInfo("isProbablyOldCIIFile:  " + isProbablyOldCIIFile);
			updateService.logInfo("partNumber:            " + partNumber);
			updateService.logInfo("buildNumber:           " + buildNumber);
			updateService.logInfo("isForceLoad:           " + isForceLoad);
			updateService.logInfo("isSecondaryLru:        " + isSecondaryLru);
			updateService.logInfo("isBackupLru:           " + isBackupLru);
			updateService.logInfo("isAborted:             " + isAborted);
			updateService.logInfo("abortReason:           " + abortReason);
			updateService.logInfo("*******************************************");
		}
	}
}
