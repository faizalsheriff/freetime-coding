package com.rockwellcollins.cs.hcms.core.profiling;

import java.util.ArrayList;
import java.util.List;

/**
 * Operates exactly like a relay stop watch. Start will begin the time. Stop
 * will stop the type. Mark will add an entry at the current running time with a
 * given name.
 * 
 * This class is used mainly for profiling.
 * 
 * @author getownse
 * 
 */
public class RelayStopWatch extends StopWatch {

	/**
	 * Data Structure for a single time marker.
	 * 
	 * @author getownse
	 * 
	 */
	public class Mark {

		private String name;

		private long time;

		/**
		 * Create a new mark with given name and current running time that the
		 * mark occurred
		 * 
		 * @param name
		 *            mark name
		 * @param time
		 *            mark time
		 */
		public Mark(final String name, final long time) {
			setName(name);
			setTime(time);
		}

		/**
		 * @return name of mark
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @return time in ms the mark occurred
		 */
		public long getTime() {
			return this.time;
		}

		private void setTime(final long timeMs) {
			this.time = timeMs;
		}

		private void setName(final String name) {
			this.name = name;
		}
	}

	private List<Mark> marks = new ArrayList<Mark>();

	/**
	 * Add a mark to the mark list with given name. The current running time in mS
	 * will be stamped with the mark.
	 * 
	 * @param name
	 *            name of mark
	 */
	public void mark(final String name) {
		synchronized (this) {
			if (isRunning()) {
				marks
						.add(new Mark(name, ((System.nanoTime()
								- startTime)/NANO2MILLI_DIVISOR)));
			}
		}
	}

	/**
	 * @return list of all the marks
	 */
	public final List<Mark> getMarks() {
		return marks;
	}
	
	public final void clearMarks() {
		marks.clear();
	}

	@Override
	public String toString() {

		final long elapsed = getElapsed();
		long lastTime = 0;

		final StringBuilder sb = new StringBuilder();

		for (final Mark mark : getMarks()) {
			if (lastTime > 0) {
				sb.append("[" + mark.getName() + ":" + mark.getTime() + ",+"
						+ (mark.getTime() - lastTime) + "]");
			} else {
				sb.append("[" + mark.getName() + ":" + mark.getTime() + "]");
			}
			lastTime = mark.getTime();
		}

		if (!isRunning()) {
			sb.append("[Stop:" + (stopTime - startTime)/NANO2MILLI_DIVISOR + "]");
		} else {
			sb.append("[Elapsed:" + elapsed + "]");
		}

		return sb.toString();
	}
}
