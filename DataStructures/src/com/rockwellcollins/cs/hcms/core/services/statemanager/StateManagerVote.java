package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StateManagerVote {

	private Map<InetAddress, Integer> crcMap; // key = IP, value = crc (nbr of entries =  nbr of State Managers)

	private Map<Integer, Integer> voteMap; // key = crc, value = vote (typically, there is only one entry)

	private int majorityCrc;

	public StateManagerVote() {
	}

	public void vote(final InetAddress inetAddress, final int crc) {

		if (!getCrcMap().containsKey(inetAddress) && crc != 0) { 
			/**
			 * IP not in crcMap (not voted) and crc is valid.
			 */
			getCrcMap().put(inetAddress, crc); // Add IP/key crc/value to crcMap. One vote per state manager!

			/**
			 * Update vote for this crc
			 */
			Integer votes = getVoteMap().get(crc); // Return number of votes/value for this crc/key
			if (votes == null) {
				votes = 1; // No vote/value entry for this crc/key, therefore set to 1
				getVoteMap().put(crc, votes); // Save vote of 1 for this crc
			} else {
				votes = votes + 1; // Aggregate
				getVoteMap().put(crc, votes); // Save incremented vote for this crc
			}
			
			/**
			 * Update majority crc
			 */
			if (majorityCrc == 0) {
				majorityCrc = crc;
			} else {
				final Integer majorityCount = getVoteMap().get(majorityCrc); // get vote on the majorityCrc

				if (majorityCount != null && votes > majorityCount) {
					majorityCrc = crc; // new majority crc
				}
			}
		}
	}

	protected int getMajorityCrc() {
		return majorityCrc;
	}

	protected Map<InetAddress, Integer> getCrcMap() {
		if (crcMap == null) {
			crcMap = new HashMap<InetAddress, Integer>();
		}
		return crcMap;
	}

	protected Map<Integer, Integer> getVoteMap() {
		if (voteMap == null) {
			voteMap = new HashMap<Integer, Integer>();
		}
		return voteMap;
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		for (final Entry<InetAddress, Integer> ip : getCrcMap().entrySet()) {
			sb.append("{");
			sb.append(ip.getKey());
			sb.append("=");
			sb.append(ip.getValue());
			sb.append(",");
			sb.append(getVoteMap().get(ip.getValue()));
			sb.append(",");
			sb.append(ip.getValue().equals(getMajorityCrc()));
			sb.append("}");
		}

		return sb.toString();
	}
}
