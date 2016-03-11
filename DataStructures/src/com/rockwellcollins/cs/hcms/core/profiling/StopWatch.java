package com.rockwellcollins.cs.hcms.core.profiling;

/**
 * Standard start/stop watch.
 * 
 * @author getownse
 * 
 */
public class StopWatch {
	static final long NANO2MILLI_DIVISOR = 1000000;
	boolean running;
	long startTime;
	long stopTime;

	/**
	 * Gets the current elapsed time, does not stop the stop watch. If the watch
	 * is stopped, it will give the time from the start time to the stop time.
	 * Otherwise, it will give the current running time.
	 * 
	 * @return time elapsed in ms
	 */
	public final long getElapsed() {

		long elapsedTime;

		synchronized (this) {
			if (running) {
				elapsedTime = (System.nanoTime() - startTime)/NANO2MILLI_DIVISOR;
			} else {
				elapsedTime = (stopTime - startTime)/NANO2MILLI_DIVISOR;
			}
		}

		return elapsedTime;
	}

	/**
	 * @return true if watch is running, false otherwise
	 */
	public final boolean isRunning() {
		synchronized (this) {
			return running;
		}
	}

	/**
	 * Restarts the watch by calling stop then start back to back.
	 */
	public final void reset() {
		synchronized (this) {
			stop();
			start();
		}
	}

	/**
	 * Starts the stop watch.
	 */
	public final void start() {
		synchronized (this) {
			startTime = System.nanoTime();
			running = true;
		}
	}

	/**
	 * Stops the stop watch.
	 */
	public final void stop() {
		synchronized (this) {
			this.stopTime = System.nanoTime();
			this.running = false;
		}
	}
}
