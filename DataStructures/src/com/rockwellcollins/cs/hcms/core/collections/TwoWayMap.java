package com.rockwellcollins.cs.hcms.core.collections;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A 2 directional map. A lookup table is created for both key to value, and
 * value to key.
 * 
 * @author getownse
 * 
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public class TwoWayMap<K, V> implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<K, V> map;

	private Map<V, K> reverseMap;

	/**
	 * Get the key of a specific value (Value to Key Map)
	 * 
	 * @param value
	 *            given value
	 * @return resulting key
	 */
	public K getKey(final V value) {
		synchronized (this) {
			return getReverseMap().get(value);
		}
	}

	protected Map<K, V> getMap() {

		synchronized (this) {

			if (map == null) {
				setMap(new HashMap<K, V>());
			}

			return map;
		}
	}

	protected void setMap(final Map<K, V> map) {
		this.map = map;
	}

	protected void setReverseMap(final Map<V, K> reverseMap) {
		this.reverseMap = reverseMap;
	}

	protected Map<V, K> getReverseMap() {

		synchronized (this) {

			if (reverseMap == null) {
				setReverseMap(new HashMap<V, K>());
			}

			return reverseMap;
		}
	}

	/**
	 * Get the value of a specific key (Key to Value map)
	 * 
	 * @param key
	 *            requesting key
	 * @return resulting value
	 */
	public V getValue(final K key) {
		synchronized (this) {
			return getMap().get(key);
		}
	}

	/**
	 * Add a key/value pair to both look up tables. (will add key->value and
	 * value->key)
	 * 
	 * @param key
	 *            given key
	 * @param value
	 *            given value
	 */
	public void put(final K key, final V value) {

		synchronized (this) {
			getMap().put(key, value);
			getReverseMap().put(value, key);
		}
	}

	/**
	 * Remove entry in both look up tables where key is equal to given key
	 * 
	 * @param key
	 *            given key
	 */
	public void removeKey(final K key) {
		synchronized (this) {

			final V value = getMap().get(key);

			if (key != null) {
				getMap().remove(key);
				getReverseMap().remove(value);
			}
		}
	}

	/**
	 * Remove entry in both look up tables where value is equal to given value
	 * 
	 * @param value
	 *            given value
	 */
	public void removeValue(final V value) {
		synchronized (this) {

			final K key = getReverseMap().get(value);

			if (key != null) {
				getMap().remove(key);
				getReverseMap().remove(value);
			}
		}
	}

	/**
	 * Clear both look up tables.
	 */
	public void clear() {
		synchronized (this) {
			getMap().clear();
			getReverseMap().clear();
		}
	}

	/**
	 * @return size of the look up tables
	 */
	public int size() {
		synchronized (this) {
			return getMap().size();
		}
	}

	/**
	 * Verify a given key exists
	 * 
	 * @param key
	 *            given key
	 * @return true if key exists
	 */
	public boolean containsKey(final K key) {
		synchronized (this) {
			return getMap().containsKey(key);
		}
	}

	/**
	 * Verify a given value exists
	 * 
	 * @param value
	 *            given value
	 * @return true if value exists
	 */
	public boolean containsValue(final V value) {
		synchronized (this) {
			return getReverseMap().containsKey(value);
		}
	}

	/**
	 * Rebuilds the Value->Key lookup table, based on the Key->Value map. This
	 * method will resynch the two look up tables if they get out of sync.
	 */
	public void rehash() {

		synchronized (this) {
			getReverseMap().clear();

			for (final Entry<K, V> entry : getMap().entrySet()) {
				getReverseMap().put(entry.getValue(), entry.getKey());
			}
		}
	}
}
