package com.rockwellcollins.cs.hcms.core.services.update;

import java.util.List;

public class ElectionResult {
	
	private String lruType;
	
	private String partNumber;
	
	private String buildNumber;
	
	private List<String> winners;

	public String getLruType() {
		return lruType;
	}

	public void setLruType(String lruType) {
		this.lruType = lruType;
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

	public List<String> getWinners() {
		return winners;
	}

	public void setWinners(List<String> winners) {
		this.winners = winners;
	}
}
