package com.rockwellcollins.cs.hcms.core.collections;

/**
 * A static container for a single instance of the Singleton
 */
final public class LRUCacheSingletonWrapper {
	private static final int CACHE_SIZE = 100;
	
	/**
	 * A reference to the current instance.
	 */
	private static LRUCache<String, Integer> _instance = null;
	
	private LRUCacheSingletonWrapper() {
	}
	
	/**
	 * This is the accessor for the Singleton.
	 */
	static public synchronized LRUCache<String, Integer> instance() {
		if (null == _instance) {
			_instance = new LRUCache<String, Integer>(CACHE_SIZE);
		}
		
		return _instance;
	}
}
