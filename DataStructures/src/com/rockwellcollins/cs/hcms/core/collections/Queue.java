package com.rockwellcollins.cs.hcms.core.collections;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * A synchronized FIFO queue.
 * @author getownse
 *
 * @param <T> Type of item.
 */
public class Queue<T> extends LinkedBlockingQueue<T> {

	private static final long serialVersionUID = 1L;

}
