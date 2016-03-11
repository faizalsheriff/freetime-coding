package com.rockwellcollins.cs.hcms.core.services.handlers;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class HandlerQueue {

	private final BlockingQueue<HandlerQueueItem> queue;

	public HandlerQueue(final int capacity) {
		queue = new ArrayBlockingQueue<HandlerQueueItem>(capacity);
	}

	public HandlerQueueItem poll(final long timeout, final TimeUnit unit)
			throws InterruptedException {
		return queue.poll(timeout, unit);
	}

	public void put(final HandlerQueueItem stateMap)
			throws InterruptedException {
		queue.put(stateMap);
	}
	
	public boolean add(final HandlerQueueItem stateMap)
	throws IllegalStateException {
		return (queue.add(stateMap));
	}

	public int size() {
		return queue.size();
	}

	public HandlerQueueItem take() throws InterruptedException {
		return queue.take();
	}
	
	public void clear() {
		queue.clear();
	}
}
