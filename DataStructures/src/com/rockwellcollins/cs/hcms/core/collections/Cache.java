package com.rockwellcollins.cs.hcms.core.collections;

import java.util.ArrayList;

/**
 * A structure to temporarily store references to Objects of type T. Once the
 * max size of the Cache has been reach, Objects at the tail of the list will
 * start to be dropped. FIFO.
 * 
 * @author getownse
 * 
 * @param <T>
 *            Class of the cached Objects
 */
public class Cache<T> {

	private int size;

	private int index;

	protected final ArrayList<CacheItem<T>> list;

	/**
	 * Create a new Cache with a given size
	 * 
	 * @param size
	 *            size of the Cache
	 */
	public Cache(final int size) {
		this.size = size;
		list = new ArrayList<CacheItem<T>>(size);
	}

	/**
	 * Set the size of the Cache
	 * 
	 * @param size
	 *            Size of the Cache
	 */
	public void setSize(final int size) {
		synchronized (this) {
			this.size = size;
		}
	}

	/**
	 * Puts an Object into the Cache at the head of the list.
	 * 
	 * @param item
	 *            Item to put on the Cache
	 */
	public void put(final T item) {

		synchronized (this) {

			if (size > 0) {
				if (index >= list.size()) {
					list.add(index, new CacheItem<T>(item));
				} else {
					list.set(index, new CacheItem<T>(item));
				}
				index = (index + 1) % size;
			}
		}

	}

	/**
	 * Checks the Cache to see if it has a specific object. The Object is equal
	 * if the method 'itemEqual' is true. If the Object exists in the Cache, the
	 * cached Object is return. Notice! The Object being passed in this function
	 * does not have to EQUAL or be the EXACT Object as the object in the cache,
	 * it only has to return true when 'itemEqual' is called.
	 * 
	 * @param item
	 *            Item to check against the Cache
	 * @return The first item that 'itemEqual' returns true, otherwise null
	 */
	public T get(final T item) {

		T result = null;

		synchronized (this) {

			int len = list.size();

			for (int i = 0; i < len; i++) {
				CacheItem<T> itr = list.get(i);
				if (itemEqual(itr.getItem(), item) && !itr.isDirty()) {
					result = itr.getItem();
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Cache uses 'itemEqual' (this method) to determine if 2 items are equal.
	 * This method is usually overridden by a derived class.
	 * 
	 * @param item1
	 *            The first item to compare
	 * @param item2
	 *            The second item to compare
	 * @return true if items are equal, false otherwise
	 */
	public boolean itemEqual(final T item1, final T item2) {
		return item1.equals(item2);
	}

	/**
	 * Returns true if the given Item is found in the Cache by itemEqual
	 * returning true.
	 * 
	 * @param item
	 *            Item to check against the cache
	 * @return true if item is found, false otherwise
	 */
	public boolean contains(final T item) {
		return get(item) != null;
	}
}
