package com.rockwellcollins.cs.hcms.core.services.update.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.update.ElectionResult;
import com.rockwellcollins.cs.hcms.core.services.update.UpdateService;

public class EAMessage extends UpdateServiceMessage {
	
	private UpdateService updateService;
	
	private String lruType;
	
	private String ipAddress;
	
	private String lruInstance;
	
	private HashMap<String, ElectionResult> result;
	
	private boolean isWinner;
	
	private static final String JSON_LRU_TYPE = "lru type";
	
	private static final String JSON_IP_ADDRESS = "ip";
	
	private static final String JSON_LRU_INSTANCE = "instance";
	
	private static final String JSON_RESULT = "result";
	
	private static final String JSON_PART_NUMBER = "pn";
	
	private static final String JSON_BUILD_NUMBER = "bn";
	
	private static final String JSON_WINNERS = "winners";
	
	private static final String JSON_IS_WINNER = "is winner";
	
	/** The Constant VERSION. */
	public static final String VERSION = "1.0";
	
	public EAMessage(){
		super();
		setType(UpdateServiceMessageTypes.ELECTION_ANNOUNCEMENT);
		setMessageVersion(VERSION);
	}
	
	public EAMessage(final JSONObject jsonObject) {
		super(jsonObject);
		initialize();
	}
	
	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	private void initialize() {
		lruType = getJsonObject().optString(JSON_LRU_TYPE);
		
		ipAddress = getJsonObject().optString(JSON_IP_ADDRESS);
		
		lruInstance = getJsonObject().optString(JSON_LRU_INSTANCE);
		
		result = new HashMap<String, ElectionResult>();
		final JSONObject jResult = getJsonObject().optJSONObject(JSON_RESULT);
		if (jResult != null) {
			final Iterator<?> jResultKeys = jResult.keys();
			while (jResultKeys.hasNext()) {
				final ElectionResult electionResult = new ElectionResult();
				final String lruType = (String) jResultKeys.next();
				final JSONObject jElectionResult = jResult.optJSONObject(lruType);
				electionResult.setLruType(jElectionResult.optString(JSON_LRU_TYPE));
				electionResult.setPartNumber(jElectionResult.optString(JSON_PART_NUMBER));
				electionResult.setBuildNumber(jElectionResult.optString(JSON_BUILD_NUMBER));
				final JSONArray jWinners = jElectionResult.optJSONArray(JSON_WINNERS);
				if(jWinners != null && jWinners.length() > 0) {
					final ArrayList<String> winners = new ArrayList<String>();
					for(int i = 0;i < jWinners.length();i++) {
						winners.add(jWinners.optString(i));
					}
					electionResult.setWinners(winners);
				}
				result.put(lruType, electionResult);
			}
		}
		
		isWinner = getJsonObject().optBoolean(JSON_IS_WINNER);
	}
	
	public String getLruType() {
		return lruType;
	}
	
	public void setLruType(final String lruType) {
		this.lruType = lruType;
		try {
			getJsonObject().put(JSON_LRU_TYPE, lruType);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
		try {
			getJsonObject().put(JSON_IP_ADDRESS, ipAddress);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	public String getLruInstance() {
		return lruInstance;
	}
	
	public void setLruInstance(final String lruInstance) {
		this.lruInstance = lruInstance;
		try {
			getJsonObject().put(JSON_LRU_INSTANCE, lruInstance);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	public HashMap<String, ElectionResult> getResult() {
		return result;
	}
	
	public void setResult(final HashMap<String, ElectionResult> result) {
		this.result = result;
		try {
			final JSONObject jResult = new JSONObject();

			for (final Entry<String, ElectionResult> resultEntry : result.entrySet()) {
				final String lruType = resultEntry.getKey();
				final ElectionResult electionResult = resultEntry.getValue();
				final JSONObject jElectionResult = new JSONObject();
				jElectionResult.put(JSON_LRU_TYPE, electionResult.getLruType());
				jElectionResult.put(JSON_PART_NUMBER, electionResult.getPartNumber());
				jElectionResult.put(JSON_BUILD_NUMBER, electionResult.getBuildNumber());
				final List<String> winners = electionResult.getWinners();
				if(winners != null && winners.size() > 0) {
					final JSONArray jWinners = new JSONArray(winners.toArray());
					jElectionResult.put(JSON_WINNERS, jWinners);
				}
				jResult.put(lruType, jElectionResult);
			}
			getJsonObject().put(JSON_RESULT, jResult);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	public boolean isWinner() {
		return isWinner;
	}
	
	public void setWinner(final boolean isWinner) {
		this.isWinner = isWinner;
		try {
			getJsonObject().put(JSON_IS_WINNER, isWinner);
		} catch (final JSONException je) {
			UnitManager.Logging.logSevere(je);
		}
	}
	
	public void printCurrentValues() {
		if (updateService.isInfo()) {
			updateService.logInfo("*******************************************");
			updateService.logInfo("Printing current values of EAMessage");
			updateService.logInfo("EA Message Version:         " + getMessageVersion());
			updateService.logInfo("LRU Type:                   " + getLruType());
			updateService.logInfo("IP Address:                 " + getIpAddress());
			updateService.logInfo("LRU Instance:               " + getLruInstance());
			if(result != null && result.size() > 0) {
				for(Entry<String, ElectionResult> resultEntry : getResult().entrySet()) {
					final String lruType = resultEntry.getKey();
					final ElectionResult electionResult = resultEntry.getValue();
					updateService.logInfo("    LRU Type:                   " + lruType);
					if(electionResult.getWinners() != null && electionResult.getWinners().size() > 0) {
						updateService.logInfo("    Part Number:                " + electionResult.getPartNumber());
						updateService.logInfo("    Build Number:               " + electionResult.getBuildNumber());
						for(String winner : electionResult.getWinners()) {
							updateService.logInfo("    Winner:                     " + winner);
						}
					} else {
						updateService.logInfo("    Winner:                     No winners");
					}
				}
			} else {
				updateService.logInfo("Result:                     null");
			}
			updateService.logInfo("Is Winner:                  " + isWinner());
			updateService.logInfo("*******************************************");
		}
	}
}
