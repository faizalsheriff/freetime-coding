package com.rockwellcollins.cs.hcms.core.services.statemanager.database;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;
import com.rockwellcollins.cs.hcms.core.utils.Crc32;

/**
 * Persistent Data for persisting a StateMap
 * 
 * @author getownse
 * 
 */
public class PersistentData implements Iterable<String> {

	private static final long serialVersionUID = 1L;

	private final HashMap<String, Boolean> marks = new HashMap<String, Boolean>();

	private StateMap stateMap = new StateMap();

	private File file;
	
	private boolean dirty = true;
	
	private int savedCrc = 0;
	
	protected StateDatabase database;
	
	public synchronized void setStateDatabase(StateDatabase database) {
			this.database = database;
	}

	protected synchronized void setStateMap(final StateMap stateMap) {
		this.stateMap = stateMap;
		dirty = true;
	}

	/**
	 * changed to fetch current values from state database vs. duplicate copy
	 * @return
	 */
	protected synchronized void refreshStateMap() {
		Iterator<String> keys = this.iterator();
		while (keys.hasNext()) {
			String name = keys.next();
			if (!stateMap.get(name).equalsIgnoreCase(database.getValue(name))) {
				UnitManager.Logging.logSevere("Persistent Data state: "
						+ name
						+ " value: "
						+ stateMap.get(name)
						+ " mismatch with Database value: "
						+ database.getValue(name));
				stateMap.put(name, database.getValue(name));
				dirty = true;
			}
		}
	}

	protected synchronized StateMap getStateMap() {
		return stateMap;
	}

	public synchronized boolean load() {

		boolean result = false;

		if (file != null && file.exists()) {

			final Object serObj = UnitManager.IO.deserialize(file);

			if (serObj instanceof StateMap) {
				stateMap = (StateMap) serObj;
				result = true;
			} else {
				UnitManager.Logging
						.logSevere("Could not load Serialized Persistent Data.");
			}
		}
		else {
			UnitManager.Logging
				.logSevere("Serialized Persistent Data file doesn't exist.");
		}
		dirty = true;

		return result;
	}

	public final synchronized boolean load(final File file) {
		this.file = file;
		return load();
	}

	public final synchronized void setFile(final File file) {
		this.file = file;
	}

	public final synchronized File getFile() {
		return file;
	}

	public synchronized boolean save(boolean refresh) {
		if(refresh) {
			refreshStateMap();
		}
		
		return UnitManager.IO.serialize(getStateMap(), file);
	}

	public synchronized boolean save() {
		return UnitManager.IO.serialize(getStateMap(), file);
	}

	public final synchronized boolean save(final File file) {
		this.file = file;
		return save(false);
	}

	public synchronized String get(final String stateName,
			final String defaultValue) {

		String value = getStateMap().get(stateName);

		if (value == null) {
			value = defaultValue;
		}

		return value;
	}

	public synchronized String remove(final String stateName) {
		dirty = true;
		return getStateMap().remove(stateName);
	}

	public synchronized void put(final String stateName, final String stateValue) {
		dirty = true;
		getStateMap().put(stateName, stateValue);
	}

	public synchronized void clear() {
		dirty = true;
		getStateMap().clear();
	}

	public synchronized boolean delete() {
		return UnitManager.IO.rm(file);
	}

	public synchronized Iterator<String> iterator() {
		return getStateMap().keySet().iterator();
	}

	public synchronized int getCrc() {
		return calculateCrc();
	}

	private int calculateCrc() {
		
		if (dirty == true) {
			int crc = Crc32.init();
	
			for (Map.Entry<String, String> entry : getStateMap().entrySet()) {
				try {
					crc = Crc32.update(crc, entry.getKey().getBytes(
							Consts.CHARACTER_SET));
					crc = Crc32.update(crc, entry.getValue().getBytes(
							Consts.CHARACTER_SET));
				} catch (UnsupportedEncodingException e) {
					UnitManager.Logging.logSevere(e);
				}
			}
			savedCrc = Crc32.value(crc);
			dirty = false;
		}
		return savedCrc;
	}

	public synchronized void clean() {
		ArrayList<String> removeList = new ArrayList<String>();
		for (String name : stateMap.keySet()) {
			if (!marks.containsKey(name)) {
				removeList.add(name);
			}
		}
		for (String name : removeList) {
			stateMap.remove(name);
		}
		dirty = true;
	}

	public void mark(final String stateName) {
		marks.put(stateName, true);
	}

	public void resetMarks() {
		marks.clear();
	}
}
