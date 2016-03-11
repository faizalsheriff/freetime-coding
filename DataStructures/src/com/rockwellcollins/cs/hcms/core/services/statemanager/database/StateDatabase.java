package com.rockwellcollins.cs.hcms.core.services.statemanager.database;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.rockwellcollins.cs.hcms.core.Consts;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateMap;
import com.rockwellcollins.cs.hcms.core.utils.Crc32;

/**
 * Database is NOT thread-safe to increase performance. It is important not to
 * call any set accessors or calculate methods from different threads.
 * 
 * @author getownse
 * 
 */
public class StateDatabase {

	private static final int DEFAULT_DATABASE_BLOCK_SIZE = 10;

	private static final String ELEM_HCMS_DATABASE = "hcms:database";
	private static final String ATTR_BLOCK_SIZE = "blockSize";
	private static final String ELEM_HCMS_STATE = "hcms:state";
	private static final String ATTR_SYNCHRONIZED = "synchronized";
	private static final String ATTR_DESCRIPTION = "description";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_RULE_REF = "rule-ref";
	private static final String ATTR_PERSISTENT = "persistent";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_NAME = "name";

	private static final long serialVersionUID = 1L;

	transient private int capacity = 0;
	transient private int blockSize = 10;
	transient private int dirtyBlockCount = 0;
	transient private int dirtyCount = 0;
	transient private int updated = 0;

	// separate class for serializing database data
	StateData data = new StateData();
	
	transient private String[] values;
	transient private String[] descriptions;
	transient private boolean[] crcEnables;
	transient private boolean[] dirtyValues;
	transient private boolean[] dirtyStates;
	transient private boolean[] dirtyBlocks;
	transient private int[] valueCrcs;
	transient private int[] stateCrcs;
	transient private int[] blockCrcs;
	transient private long[] updateTimes;

	private transient PersistentData persistentData;

	private transient File persistentDataFile;
	
	private transient File databaseSerFile;

	private transient final HashMap<String, Integer> map = new HashMap<String, Integer>();
	

	//////////////////////////////////////////////////////
	//
	private synchronized final int getBlockIndex(final int index) {
		return (index / blockSize);
	}

	public synchronized final String getName(final int index) {
		return data.names[index];
	}

	public synchronized final void setName(final int index, final String name) {
		data.names[index] = name;
		dirtyStates[index] = true;
		if (dirtyBlocks[getBlockIndex(index)] == false) {
			dirtyBlockCount++;
		}
		dirtyBlocks[getBlockIndex(index)] = true;
		map.put(name, index);
		if (data.persistents[index]) {
			updatePersistent(index);
		}
	}

	public synchronized final String getValue(final String name) {
		int index = getIndex(name);
		if (index == -1) {
			return null;
		}
		return getValue(index);
	}

	public synchronized final String getValue(final int index) {
		return values[index];
	}

	public synchronized final String getDefaultValue(final String name) {
		int index = getIndex(name);
		if (index == -1) {
			return null;
		}
		return getDefaultValue(index);
	}

	public synchronized final String getDefaultValue(final int index) {
		return data.defaultValues[index];
	}

	public synchronized final long getUpdateTime(final int index) {
		return updateTimes[index];
	}
	
	public synchronized final int getNbrOfUpdates() {
		return updated; 
	}
	
	public synchronized final void setNbrOfUpdates(final int updates) {
		updated = updates; 
	}
	
	public synchronized final void resetNbrOfUpdates() {
		updated = 0; 
	}

	private synchronized final void checkConsistency() {
		Iterator<String> keys = persistentData.iterator();
		while (keys.hasNext()) {
			String name = keys.next();
			String dbValue = getValue(name);
			String pdValue = persistentData.get(name, null);
			if (!dbValue.equals(pdValue)) {
				UnitManager.Logging.logSevere("StateDatabase doesn't match PersistentData for state: " 
						+ name + " - Database val: " +  dbValue + " - Persistent Val: " + pdValue);
			}
		}
	}
	
	private synchronized final void updatePersistent(final int index) {
		persistentData.put(data.names[index], values[index]);
		if (UnitManager.Logging.isCore()) {
			UnitManager.Logging
					.logCore("StateDatabase saving persistent state '"
							+ data.names[index] + "' to value '" + values[index]
							+ "'");
		}
		checkConsistency();  // for debugging; remove for release
		persistentData.save(true);
	}

	public synchronized final void setValue(final int index, final String value) {
		setValue(index, value, true);
		if (data.persistents[index]) {
			updatePersistent(index);
		}
	}

	public synchronized final void setValue(final String name,
			final String value) {
		int index = getIndex(name);
		if (index != -1) {
			setValue(index, value);
		}
	}

	public synchronized void setValue(final int index, final String value,
			boolean updateTime) {
		if (updateTime) {
			updateTimes[index] = UnitManager.Timing.getTimeAlive();
			updated++;
		}
		if (dirtyValues[index] == false) {
			dirtyCount++;
		}
		values[index] = value;
		dirtyValues[index] = true;
		dirtyStates[index] = true;
		if (dirtyBlocks[getBlockIndex(index)] == false) {
			dirtyBlockCount++;
		}
		dirtyBlocks[getBlockIndex(index)] = true;
	}

	public synchronized void setState(final int index, final String name,
			final String type, final String value, final String rule,
			final boolean persistent, final boolean crcEnable,
			final boolean updateTime) {
		if (updateTime) {
			updateTimes[index] = UnitManager.Timing.getTimeAlive();
		}
		data.names[index] = name;
		data.types[index] = type;
		//data.defaultValues[index] = value;
		values[index] = value;
		data.rules[index] = rule;
		data.persistents[index] = persistent;
		crcEnables[index] = crcEnable;
		dirtyStates[index] = true;
		if (dirtyValues[index] == false) {
			dirtyCount++;				
		}
		dirtyValues[index] = true;
		if (dirtyBlocks[getBlockIndex(index)] == false) {
			dirtyBlockCount++;
		}
		dirtyBlocks[getBlockIndex(index)] = true;
		map.put(name, index);
	}

	public synchronized final String getType(final int index) {
		return data.types[index];
	}

	public synchronized final void setType(final int index, final String type) {
		data.types[index] = type;
		dirtyStates[index] = true;
		if (dirtyBlocks[getBlockIndex(index)] == false) {
			dirtyBlockCount++;
		}
		dirtyBlocks[getBlockIndex(index)] = true;
	}

	public synchronized final String getRule(final int index) {
		return data.rules[index];
	}

	public synchronized final void setRule(final int index, final String rule) {
		data.rules[index] = rule;
		dirtyStates[index] = true;
		if (dirtyBlocks[getBlockIndex(index)] == false) {
			dirtyBlockCount++;
		}
		dirtyBlocks[getBlockIndex(index)] = true;
	}

	public synchronized final boolean isPersistent(final int index) {
		return data.persistents[index];
	}

	public synchronized final void setPersistent(final int index,
			final boolean persistent) {
		data.persistents[index] = persistent;
		dirtyStates[index] = true;
		if (dirtyBlocks[getBlockIndex(index)] == false) {
			dirtyBlockCount++;
		}
		dirtyBlocks[getBlockIndex(index)] = true;
	}

	public synchronized final boolean isCrcEnable(final int index) {
		return crcEnables[index];
	}

	public synchronized final void setCrcEnable(final int index,
			final boolean crcEnable) {
		if (!crcEnable) {
			stateCrcs[index] = 0;
			valueCrcs[index] = 0;
		}
		dirtyStates[index] = true;
		if (dirtyValues[index] == false) {
			dirtyCount++;				
		}
		dirtyValues[index] = true;
		if (dirtyBlocks[getBlockIndex(index)] == false) {
			dirtyBlockCount++;
		}
		dirtyBlocks[getBlockIndex(index)] = true;
		crcEnables[index] = crcEnable;
	}

	public synchronized final String getDescription(final int index) {
		return descriptions[index];
	}

	public synchronized final void setDescription(final int index,
			final String description) {
		descriptions[index] = description;
	}

	public synchronized final int getBlockCrc(final int blockIndex,
			final boolean useCache) {
		//if (blockIndex >= getBlockCount()) {
		//	return -1;
		//}
		if (!useCache || dirtyBlocks[blockIndex]) {
			int crc = Crc32.init();
			int start = blockIndex * blockSize;
			int end =  Math.min(start + blockSize, data.size);
			for (int i = start; i < end; i++) {
				//crc = Crc32.update(crc, getStateCrc(i, useCache));
				crc = Crc32.update(crc, getValueCrc(i, useCache));
			}
			blockCrcs[blockIndex] = Crc32.value(crc);
			if (dirtyBlocks[blockIndex] == true) {
				dirtyBlockCount--;
			}
			dirtyBlocks[blockIndex] = false;
		}
		return blockCrcs[blockIndex];
	}

	public synchronized final int getValueCrc(final int index,
			final boolean useCache) {
		if (!useCache || (dirtyValues[index] && crcEnables[index])) {
			int crc = Crc32.init();
			try {
				crc = Crc32.update(crc, values[index]
						.getBytes(Consts.CHARACTER_SET));
			} catch (final UnsupportedEncodingException e) {
				UnitManager.Logging.logSevere("Character '"
						+ Consts.CHARACTER_SET
						+ "' not supported by StateDatabase", e);
			}
			valueCrcs[index] = Crc32.value(crc);
			if (dirtyValues[index] == true) {
				dirtyCount--;				
			}
			dirtyValues[index] = false;
			// NOTE:
			// there many be other dirty states in this block
			// so can't mark this block as clean here
		}
		return valueCrcs[index];
	}

	// TODO: make method for use in signature using defaultValues vs. values
	public synchronized final int getStateDefaultCrc(final int index,
			final boolean useCache) {
		if (!useCache || (crcEnables[index] && dirtyStates[index])) {
			try {
				int crc = Crc32.init();
				crc = Crc32.update(crc, data.names[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, data.types[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, data.defaultValues[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, data.rules[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, data.persistents[index] ? 1 : 0);
				stateCrcs[index] = Crc32.value(crc);
				dirtyStates[index] = false;
			} catch (final UnsupportedEncodingException e) {
				UnitManager.Logging.logSevere("Character '"
						+ Consts.CHARACTER_SET
						+ "' not supported by StateDatabase", e);
			}
		}
		return stateCrcs[index];
	}
	
	
	public synchronized final int getStateCrc(final int index,
			final boolean useCache) {
		if (!useCache || (crcEnables[index] && dirtyStates[index])) {
			try {
				int crc = Crc32.init();
				crc = Crc32.update(crc, data.names[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, data.types[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, values[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, data.rules[index]
						.getBytes(Consts.CHARACTER_SET));
				crc = Crc32.update(crc, data.persistents[index] ? 1 : 0);
				stateCrcs[index] = Crc32.value(crc);
				dirtyStates[index] = false;
			} catch (final UnsupportedEncodingException e) {
				UnitManager.Logging.logSevere("Character '"
						+ Consts.CHARACTER_SET
						+ "' not supported by StateDatabase", e);
			}
		}
		return stateCrcs[index];
	}

	public synchronized final int getIndex(final String stateName) {
		Integer i = null;

		synchronized (map) {
			i = map.get(stateName);
		}

		if (i == null) {
			UnitManager.Logging
					.logWarning("StateDatabase getIndex(String stateName) could not reference State Name '"
							+ stateName
							+ "' as an available State in the State Database.");

			return -1;
		}

		return i;
	}

	public synchronized final void add(final String name, final String value,
			final String type, final String rule, final boolean persistent,
			final String description, final boolean crcenable) {

		int i = data.size;
		data.size = data.size + 1;

		if (data.size > capacity) {
			resize((capacity * 3) / 2 + 1);
		}

		data.names[i] = name;
		values[i] = value;
		data.defaultValues[i] = value;
		data.types[i] = type;
		data.rules[i] = rule;
		data.persistents[i] = persistent;
		descriptions[i] = description;
		crcEnables[i] = crcenable;
		dirtyValues[i] = true;
		dirtyCount++;
		dirtyStates[i] = true;
		valueCrcs[i] = 0;
		stateCrcs[i] = 0;
		updateTimes[i] = 0;
		try {
			if (dirtyBlocks[getBlockIndex(i)] == false) {
				dirtyBlockCount++;
			}
			dirtyBlocks[getBlockIndex(i)] = true;
			blockCrcs[getBlockIndex(i)] = 0;			
		} catch (ArrayIndexOutOfBoundsException e) {
			UnitManager.Logging.logSevere("StateDatabase add excetion at state index: " + i 
					+ " block index: " + getBlockIndex(i));
		}

		map.put(name, i);
	}

	public synchronized final void rehash() {
		synchronized (map) {
			map.clear();
			for (int i = 0; i < data.size; i++) {
				map.put(data.names[i], i);
			}
		}
	}

	// blockSize must be set before this is called
	private synchronized final void initXml(final int capacity) {
		UnitManager.Logging.logWarning("Database init capacity: " + capacity);
		data = new StateData(capacity);
		data.size = 0; // this will be incremented as XML is parsed

		values = new String[capacity];
		crcEnables = new boolean[capacity];
		dirtyValues = new boolean[capacity];
		dirtyStates = new boolean[capacity];
		descriptions = new String[capacity];
		valueCrcs = new int[capacity];
		stateCrcs = new int[capacity];
		updateTimes = new long[capacity];
		this.capacity = capacity;
		
		int blockCapacity = ((capacity -1) / blockSize) + 1;
		dirtyBlocks = new boolean[blockCapacity];
		blockCrcs = new int[blockCapacity];
		
		map.clear();
	}

	// blockSize must be set first
	public synchronized final void alloc(final int capacity) {
		initXml(capacity);
		for (int i = 0; i < capacity; i++) {
			data.names[i] = "";
			values[i] = "-1";
			data.defaultValues[i] = "";
			data.types[i] = "";
			data.rules[i] = "";
			data.persistents[i] = false;
			crcEnables[i] = true;
			dirtyValues[i] = true;
			dirtyStates[i] = true;
			descriptions[i] = "";
			valueCrcs[i] = 0;
			stateCrcs[i] = 0;
			updateTimes[i] = 0;
		}
		int blockCapacity = ((capacity - 1) / blockSize) + 1;
		dirtyBlockCount = 0;
		for (int j = 0; j < blockCapacity; j++) {
			dirtyBlocks[j] = true;
			dirtyBlockCount++;
			blockCrcs[j] = 0;			
		}
		data.size = 0;
		this.capacity = capacity;
		rehash();
	}

	public synchronized final void resize(final int capacity) {
		UnitManager.Logging.logWarning("Database resize for: " + capacity);
		data.names = copyOf(data.names, capacity);
		values = copyOf(values, capacity);
		data.defaultValues = copyOf(data.defaultValues, capacity);
		data.types = copyOf(data.types, capacity);
		data.rules = copyOf(data.rules, capacity);
		data.persistents = copyOf(data.persistents, capacity);
		crcEnables = copyOf(crcEnables, capacity);
		descriptions = copyOf(descriptions, capacity);
		valueCrcs = copyOf(valueCrcs, capacity);
		stateCrcs = copyOf(stateCrcs, capacity);
		dirtyValues = copyOf(dirtyValues, capacity);
		dirtyStates = copyOf(dirtyStates, capacity);
		updateTimes = copyOf(updateTimes, capacity);
		this.capacity = capacity;
		
		int blockCapacity = ((capacity - 1) / blockSize) + 1;
		blockCrcs = copyOf(blockCrcs, blockCapacity);
		dirtyBlocks = copyOf(dirtyBlocks, blockCapacity);
	}

	public synchronized void invalidateAge() {
		for (int i = 0; i < data.size; i++) {
			updateTimes[i] = 0;
		}
	}

	private static final String[] copyOf(final String[] src, final int newLen) {
		String[] dst = new String[newLen];
		System.arraycopy(src, 0, dst, 0, Math.min(src.length, newLen));
		return dst;
	}

	private static final boolean[] copyOf(final boolean[] src, final int newLen) {
		boolean[] dst = new boolean[newLen];
		System.arraycopy(src, 0, dst, 0, Math.min(src.length, newLen));
		return dst;
	}

	private static final int[] copyOf(final int[] src, final int newLen) {
		int[] dst = new int[newLen];
		System.arraycopy(src, 0, dst, 0, Math.min(src.length, newLen));
		return dst;
	}

	private static final long[] copyOf(final long[] src, final int newLen) {
		long[] dst = new long[newLen];
		System.arraycopy(src, 0, dst, 0, Math.min(src.length, newLen));
		return dst;
	}

	public synchronized final int[] getIndexesByBlock(final int blockIndex) {

		int start = blockIndex * blockSize;
		int end = Math.min(start + blockSize, data.size);
		int[] indexes = new int[end - start];
		int index = 0;
		for (int i = start; i < end; i++) {
			indexes[index++] = i;
		}
		return indexes;
	}

	public synchronized final int getSize() {
		return data.size;
	}

	public synchronized final int getCapacity() {
		return capacity;
	}

	public synchronized final int getBlockValueCrc(final int blockIndex,
			final boolean useCache) {
		int crc = Crc32.init();
		int start = blockIndex * blockSize;
		int end = Math.min(start + blockSize, data.size);
		for (int i = start; i < end; i++) {
			crc = Crc32.updateInt(crc, getValueCrc(i, useCache));
		}
		return Crc32.value(crc);
	}

	public String toString(final int index) {
		StringBuilder sb = new StringBuilder();
		sb.append("{Name='");
		sb.append(data.names[index]);
		sb.append("',Value='");
		sb.append(values[index]);
		sb.append("',Type='");
		sb.append(data.types[index]);
		sb.append("',Rule='");
		sb.append(data.rules[index]);
		sb.append("',Persistent='");
		sb.append(data.persistents[index]);
		sb.append("',CrcEnable='");
		sb.append(crcEnables[index]);
		sb.append("',Description='");
		sb.append(descriptions[index]);
		sb.append("',ValueCrc='");
		sb.append(valueCrcs[index]);
		sb.append("',StateCrc='");
		sb.append(stateCrcs[index]);
		sb.append("',BlockCrc='");
		sb.append(blockCrcs[getBlockIndex(index)]);
		sb.append("',DirtyValue='");
		sb.append(dirtyValues[index]);
		sb.append("',DirtyState='");
		sb.append(dirtyStates[index]);
		sb.append("',DirtyBlock='");
		sb.append(dirtyBlocks[getBlockIndex(index)]);
		sb.append("',UpdateTime='");
		sb.append(updateTimes[index]);
		sb.append("'}");
		return sb.toString();
	}

	public synchronized final int getBlockStateCrc(final int blockIndex,
			final boolean cache) {
		int crc = Crc32.init();
		int start = blockIndex * blockSize;
		int end = Math.min(start + blockSize, data.size);
		for (int i = start; i < end; i++) {
			crc = Crc32.updateInt(crc, getStateCrc(i, cache));
		}
		return Crc32.value(crc);
	}

	public synchronized final int getBlockCount() {
		if (data.size == 0) {
			return 0;
		}
		return (data.size - 1) / blockSize + 1;
	}

	public synchronized final void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public synchronized final int[] getBlockValueCrcs(final int[] blockIndexes,
			final int offset, final int length, final boolean useCache) {
		int results[] = new int[length - offset];
		int end = Math.min(offset + length, blockIndexes.length);
		int index = 0;
		for (int i = offset; i < end; i++) {
			results[index] = getBlockValueCrc(blockIndexes[i], useCache);
			index++;
		}
		return results;
	}

	public synchronized final int[] getBlockStateCrcs(final int[] blockIndexes,
			final int offset, final int length, final boolean useCache) {
		int results[] = new int[length - offset];
		int end = Math.min(offset + length, blockIndexes.length);
		int index = 0;
		for (int i = offset; i < end; i++) {
			results[index] = getBlockStateCrc(blockIndexes[i], useCache);
			index++;
		}
		return results;
	}

	public synchronized final int[] getBlockCrcs(final int[] blockIndexes,
			final int offset, final int length, final boolean useCache) {
		int results[] = new int[length - offset];
		int end = Math.min(offset + length, blockIndexes.length);
		int index = 0;
		for (int i = offset; i < end; i++) {
			results[index] = getBlockCrc(blockIndexes[i], useCache);
			index++;
		}
		return results;
	}

	public synchronized final int[] getBlockValueCrcs(final boolean useCache) {
		int[] bs = new int[data.size / blockSize];
		int len = bs.length;
		for (int i = 0; i < len; i++) {
			bs[i] = getBlockValueCrc(i, useCache);
		}
		return bs;
	}

	public synchronized final int[] getBlockStateCrcs(final boolean useCache) {
		int[] blocks = new int[data.size / blockSize];
		int len = blocks.length;
		for (int i = 0; i < len; i++) {
			blocks[i] = getBlockStateCrc(i, useCache);
		}
		return blocks;
	}

	public synchronized final int getBlockSize() {
		return blockSize;
	}

	public synchronized final int dirtyCount() {
		return dirtyCount;
	}

	public synchronized final int dirtyBlockCount() {
		return dirtyBlockCount;
	}

	// changed to use blocks
	public synchronized final int getDatabaseCrcBlock(final boolean useCache) {
		int crc = Crc32.init();
		for (int i = 0; i < getBlockCount(); i++) {
			crc = Crc32.update(crc, getBlockCrc(i, useCache));
		}
		
		if (dirtyBlockCount != 0) {
			UnitManager.Logging.logSevere("State Manager dirty block count not zero: " +
					dirtyBlockCount );
			dirtyBlockCount = 0;
		}
		return Crc32.value(crc);
	}

	public synchronized final int getDatabaseCrc(final boolean useCache) {
		int crc = Crc32.init();
		for (int i = 0; i < data.size; i++) {
			crc = Crc32.update(crc, getStateCrc(i, useCache));
		}
		/* Add DB Block Size to the final DB CRC, for DB signature, i.e don't use cache */
		if (!useCache) {
			crc = Crc32.update(crc, this.blockSize);
		}
		return Crc32.value(crc);
	}

	public synchronized final int getDefaultDatabaseCrc(final boolean useCache) {
		int crc = Crc32.init();
		for (int i = 0; i < data.size; i++) {
			crc = Crc32.update(crc, getStateDefaultCrc(i, useCache));
		}
		/* Add DB Block Size to the final DB CRC, for DB signature, i.e don't use cache */
		if (!useCache) {
			crc = Crc32.update(crc, this.blockSize);
		}
		return Crc32.value(crc);
	}

	public synchronized final int getDatabaseCrcValue(final boolean useCache) {
		int crc = Crc32.init();
		for (int i = 0; i < data.size; i++) {
			crc = Crc32.update(crc, getValueCrc(i, useCache));
		}
		return Crc32.value(crc);
	}

	public synchronized final boolean loadSerializedDatabase(final File serializedDatabaseFile,
			final int databaseSize) {

		this.databaseSerFile = serializedDatabaseFile;
		
		boolean result = false;

		if (serializedDatabaseFile != null && serializedDatabaseFile.exists()) {

			final Object serObj = UnitManager.IO.deserialize(serializedDatabaseFile);

			if (serObj instanceof StateData) {
				data = (StateData) serObj;
				
				UnitManager.Logging.logWarning(data.toString());

				if (data.validate()) {
					// allocate all fields not de-serialized
					initSer(data.size);
	
					// if serialized database bigger than expected per XML setting
					if (data.size > databaseSize) {
						//resize((data.size * 3) / 2 + 1);
						UnitManager.Logging.logWarning("Serialized database bigger than setting: "
								+ databaseSize);
					}
	
					// initialized all fields not de-serialized
					for (int i = 0; i < data.size; i++) {
						values[i] = data.defaultValues[i];
						descriptions[i] = "";
						crcEnables[i] = true;
						dirtyValues[i] = true;
						dirtyCount++;
						dirtyStates[i] = true;
						valueCrcs[i] = 0;
						stateCrcs[i] = 0;
						updateTimes[i] = 0;
						try {
							if (dirtyBlocks[getBlockIndex(i)] == false) {
								dirtyBlockCount++;
							}
							dirtyBlocks[getBlockIndex(i)] = true;
							blockCrcs[getBlockIndex(i)] = 0;			
						} catch (ArrayIndexOutOfBoundsException e) {
							UnitManager.Logging.logSevere("StateDatabase add exception at state index: " + i 
									+ " block index: " + getBlockIndex(i));
						}
						map.put(data.names[i], i);
					}
					
					result = true;
				} else {
					UnitManager.Logging
						.logWarning("Serialized database did not validate: " + data);
				}
			} else {
				UnitManager.Logging
						.logSevere("Could not load Serialized Database.");
			}
		}
		else {
			UnitManager.Logging
				.logSevere("Serialized Database file doesn't exist.");
		}

		return result;
	}
	
	// blockSize must be set before this is called
	// allocates all fields not serialized
	private synchronized final void initSer(final int capacity) {
		/*****************
		data.size = capacity;
		data.names = new String[capacity];
		data.types = new String[capacity];
		data.rules = new String[capacity];
		data.persistents = new boolean[capacity];
		data.defaultValues = new String[capacity];
		*******************/
		
		values = new String[capacity];
		crcEnables = new boolean[capacity];
		dirtyValues = new boolean[capacity];
		dirtyStates = new boolean[capacity];
		descriptions = new String[capacity];
		valueCrcs = new int[capacity];
		stateCrcs = new int[capacity];
		updateTimes = new long[capacity];
		this.capacity = capacity;
		
		int blockCapacity = ((capacity -1) / blockSize) + 1;
		dirtyBlocks = new boolean[blockCapacity];
		blockCrcs = new int[blockCapacity];
		
		map.clear();
	}

	public synchronized final boolean saveDatabase(final File databaseSerFile) {
		this.databaseSerFile = databaseSerFile;
		return saveDatabase();
	}

	// this only works if passed object is serializable
	public synchronized final boolean saveDatabase() {
		boolean result = UnitManager.IO.serialize(this.data, databaseSerFile);
		if (result) {
			//if (UnitManager.Logging.isCore())
				UnitManager.Logging.logWarning("Database serialized to: "
						+ databaseSerFile);			
		}
		return result;
	}

	public synchronized final boolean loadPersistentData() {

		if (persistentData == null) {
			persistentData = new PersistentData();
		}

		persistentData.setStateDatabase(this);
		boolean result = persistentData.load(persistentDataFile);
		
		if(!result) {
			UnitManager.Logging.logSevere("PersistentData load from file failed!");
		}
		
		persistentData.resetMarks();

		for (int i = 0; i < data.size; i++) {

			String name = data.names[i];
			String value = values[i];

			String pValue = persistentData.get(name, null);

			if (data.persistents[i]) {
				if (pValue == null) {
					persistentData.put(name, value);
					persistentData.mark(name);
					if (UnitManager.Logging.isCore()) {
						UnitManager.Logging
								.logCore("StateDatabase adding state '" + name
										+ "' with value '" + value
										+ "' to persistent data.");
					}
				} else {
					values[i] = pValue;
					if (dirtyValues[i] == false) {
						dirtyCount++;				
					}
					dirtyValues[i] = true;
					dirtyStates[i] = true;
					if (dirtyBlocks[getBlockIndex(i)] == false) {
						dirtyBlockCount++;
					}
					dirtyBlocks[getBlockIndex(i)] = true;
					persistentData.mark(name);
					if (UnitManager.Logging.isCore()) {
						UnitManager.Logging
								.logCore("StateDatabase changing state '"
										+ name + "' to persistent value '"
										+ pValue + "'");
					}
				}
			} else if (pValue != null) {
				persistentData.remove(name);
				if (UnitManager.Logging.isCore()) {
					UnitManager.Logging
							.logCore("State Database removing persistent state '"
									+ name + "'");
				}
			}
		}

		persistentData.clean();
		persistentData.save(false);
		return result;
	}

	public synchronized final boolean loadXml(final File databaseXml, final int databaseSize) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		boolean success = databaseXml != null && databaseXml.exists() && databaseSize > 0;

		if (success) {
			initXml(databaseSize);
			try {
				SAXParser parser = factory.newSAXParser();
				parser.parse(databaseXml, new DefaultHandler() {
					@Override
					public void startElement(String uri, String localName,
							String name, Attributes attributes)
							throws SAXException {

						if (ELEM_HCMS_STATE.equals(name)) {

							String aname = attributes.getValue(ATTR_NAME);
							String avalue = attributes.getValue(ATTR_VALUE);
							String apersistent = attributes
									.getValue(ATTR_PERSISTENT);
							String arule = attributes.getValue(ATTR_RULE_REF);
							String atype = attributes.getValue(ATTR_TYPE);
							String adescription = attributes
									.getValue(ATTR_DESCRIPTION);
							String asynchronized = attributes
									.getValue(ATTR_SYNCHRONIZED);

							if (aname == null)
								aname = "";
							if (avalue == null)
								avalue = "";
							if (apersistent == null)
								apersistent = "false";
							if (arule == null)
								arule = "";
							if (atype == null)
								atype = "";
							if (adescription == null)
								adescription = "";
							// if (asynchronized == null) LCP control of crcEnable disabled
								asynchronized = "true";

							add(aname, avalue, atype, arule, Boolean
									.parseBoolean(apersistent), adescription,
									Boolean.parseBoolean(asynchronized));

						} else if (ELEM_HCMS_DATABASE.equals(name)) {
							String ablockSize = attributes
									.getValue(ATTR_BLOCK_SIZE);
							try {
								setBlockSize(Integer.parseInt(ablockSize));
							} catch (NumberFormatException e) {
								UnitManager.Logging
										.logSevere("Database could not convert blockSize '"
												+ ablockSize
												+ "' to an Integer.  Defaulting to "
												+ DEFAULT_DATABASE_BLOCK_SIZE);
							}
						}

					}
				});
			} catch (SAXException e) {
				UnitManager.Logging.logSevere("State Database failed to load: "
						+ databaseXml);
				success = false;
			} catch (IOException e) {
				UnitManager.Logging.logSevere("State Database failed to load: "
						+ databaseXml);
				success = false;
			} catch (ParserConfigurationException e) {
				UnitManager.Logging.logSevere("State Database failed to load: "
						+ databaseXml);
				success = false;
			}
		}

		UnitManager.Logging.logWarning("Database loadXML final size: " + data.size);
		return success;
	}

	public synchronized final void setPersistentData(
			PersistentData persistentData) {
		this.persistentData = persistentData;
	}

	public synchronized final void setPersistentDataFile(
			final File persistentDataFile) {
		this.persistentDataFile = persistentDataFile;
	}
	
	public synchronized final void setSerDatabaseFile(
			final File serDatabaseFile) {
		this.databaseSerFile = serDatabaseFile;
	}

	public synchronized final File getSerDatabaseFile() {
		return this.databaseSerFile;
	}

	public synchronized final void setSize(final int size) {
		if (size > capacity) {
			resize(size);
		}
		data.size = size;
	}

	public synchronized final PersistentData getPersistentData() {
		return persistentData;
	}
}