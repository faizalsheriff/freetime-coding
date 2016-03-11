package com.rockwellcollins.cs.hcms.core.collections;

/**
 * A cache item is the element contained in the Cache structure. It wraps the
 * actual cached item and also has a dirty property that is used to quickly
 * remove the cached item without having to dynamically changed the size of the
 * cache.
 * 
 * @author getownse
 * 
 * @param <T> the type of item
 * 
 * @see Cache
 */
public class CacheItem<T> {

	private final T item;

	private boolean dirty;

	/**
	 * The cached item
	 * @param item cached item of type T
	 */
	public CacheItem(final T item) {
		this.item = item;
		this.dirty = false;
	}

	/**
	 * If a cached item is dirty, it will not be returned as found in the cache
	 * @return true if the item is dirty and will not be considered in the cache
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Sets an item as dirty, and will not be considered in the cache
	 * @param dirty
	 */
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * Returns the wrapped cached item of type T
	 * @return cached item
	 */
	public T getItem() {
		return item;
	}
}
