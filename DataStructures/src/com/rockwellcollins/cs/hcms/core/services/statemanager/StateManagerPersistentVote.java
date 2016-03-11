package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StateManagerPersistentVote {

	private Map<InetAddress, Integer> crcMap;

	private Map<Integer, Integer> voteMap;

	private int majorityCrc;

	public StateManagerPersistentVote() {
	}

	public void vote(final InetAddress inetAddress, final int crc) {

		if (!getCrcMap().containsKey(inetAddress) && crc != 0) {

			getCrcMap().put(inetAddress, crc);

			Integer votes = getVoteMap().get(crc);

			if (votes == null) {
				votes = 1;
				getVoteMap().put(crc, votes);
			} else {
				votes = votes + 1;
				getVoteMap().put(crc, votes);
			}

			if (majorityCrc == 0) {
				majorityCrc = crc;
			} else {
				final Integer majorityCount = getVoteMap().get(majorityCrc);

				if (majorityCount != null && votes > majorityCount) {
					majorityCrc = crc;
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
