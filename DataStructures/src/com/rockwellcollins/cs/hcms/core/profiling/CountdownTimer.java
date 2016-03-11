package com.rockwellcollins.cs.hcms.core.profiling;

/**
 * A Countdown Timer that is based on Units time since startup.
 * 
 * @author getownse
 * 
 */
public class CountdownTimer {

	protected static final long NANO2MILLI_DIVISOR = 1000000;
	protected static final long MILLI2NANO_MULTIPLIER = NANO2MILLI_DIVISOR;
	protected long start;
	protected long timer;
	protected long remaining;

	/**
	 * Expired is calculated by current time alive - start time > timer
	 * 
	 * @return true if the timer has expired, otherwise false
	 */
	public final boolean hasExpired() {
		synchronized (this) {
			return System.nanoTime() - start >= timer;
		}
	}

	/**
	 * Sets the start time to the current time
	 */
	public final void restart() {
		synchronized (this) {
			start = System.nanoTime();
		}
	}
	@Deprecated
	public final void reset() {
		synchronized (this) {
			start = System.nanoTime();
		}
	}

	/**
	 * Sets the time in MS that will pass before the timer is considered
	 * 'expired'
	 * 
	 * @param timeMs
	 * time in ms
	 */
	public final void startTimer(final long timeMs) {
		synchronized (this) {
			start = System.nanoTime();
			timer = timeMs*MILLI2NANO_MULTIPLIER;
		}
	}
	@Deprecated
	public final void setTimer(final long timeMs) {
		synchronized (this) {
			start = System.nanoTime();
			timer = timeMs*MILLI2NANO_MULTIPLIER;
		}
	}

	public final long getRemainingTime() {
		synchronized (this) {
			remaining = (timer - (System.nanoTime() - start))/NANO2MILLI_DIVISOR;
		}
		if (remaining < 0) {
			return 0;
		}
		return remaining;
	}

}
