package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * State Map is an sort ordered State Name to State Value Map
 * 
 * @author getownse
 * 
 */
public class StateMap extends TreeMap<String, String> implements
		SortedMap<String, String> {

	private static final long serialVersionUID = 1L;

	/**
	 * Return the union of this state map with given state map. All entries that
	 * contain the same key will be returned in a new state map.
	 * 
	 * @param stateMap
	 *            given state map
	 * @return new state map with intersection entries based on key
	 */
	public StateMap union(final StateMap stateMap) {
		final StateMap unionMap = new StateMap();
		StateMap smallMap, largeMap;

		if (stateMap.size() > this.size()) {
			largeMap = stateMap;
			smallMap = this;
		} else {
			largeMap = this;
			smallMap = stateMap;
		}

		for (final Entry<String, String> smallItem : smallMap.entrySet()) {
			if (largeMap.containsKey(smallItem.getKey())) {
				unionMap.put(smallItem.getKey(), smallItem.getValue());
			}
		}

		return unionMap;
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		sb.append("{");

		for (final Entry<String, String> entry : entrySet()) {
			if (sb.length() > 1) {
				sb.append(",");
			}
			sb.append("'" + entry.getKey() + "'='" + entry.getValue() + "'");
		}

		sb.append("}");

		return sb.toString();
	}
}
