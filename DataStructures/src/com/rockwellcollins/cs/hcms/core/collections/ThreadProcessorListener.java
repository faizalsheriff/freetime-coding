package com.rockwellcollins.cs.hcms.core.collections;

/**
 * Callback for Thread Processor. Used when a Thread Processor is started, the
 * callback is executed infinitely until the Thread Processor is stopped. Make
 * sure there is a blocking call or a sleep inside the method.
 * 
 * @author getownse
 * @see ThreadProcessor
 */
public interface ThreadProcessorListener {
	/**
	 * Thread Processor calls this method infinitely with no delay. Make sure a
	 * blocking call or delay in introduced to this method when deriving.
	 * 
	 * @param processor the processor who made the callback
	 * @throws InterruptedException
	 */
	void threadProcessorAction(ThreadProcessor processor)
			throws InterruptedException;
}
