package com.rockwellcollins.cs.hcms.core.collections;

/**
 * Queue Listener contains the Queue Processor callback methods.
 * @author getownse
 *
 * @param <T> Type of queue item
 * @see QueueProcessor
 */
public interface QueueListener<T> {
	/**
	 * Method called when a Queue item has been received.
	 * @param processor The queue processor processing the event
	 * @param item the queue item
	 */
	void queueProcessorItemReceived(QueueProcessor<T> processor, T item);

	/**
	 * Method called when a Queue Processor has gone a configured amount of time
	 * without receiving a queue item
	 * 
	 * @param processor the queue processor processing the event
	 */
	void queueProcessorTimeout(QueueProcessor<T> processor);
}
