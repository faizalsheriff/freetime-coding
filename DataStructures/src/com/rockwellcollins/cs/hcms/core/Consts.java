package com.rockwellcollins.cs.hcms.core;

/**
 * Core Framework global constants
 * 
 * @author getownse
 * 
 */
public final class Consts {
	/**
	 * IO Constants for Platform
	 * 
	 * @author getownse
	 * 
	 */
	public final static class IOs {
		/**
		 * Platform File Separator
		 */
		public static final String FILE_SEPARATOR = System
				.getProperty("file.separator");

		/**
		 * Platform Path Separator
		 */
		public static final String PATH_SEPARATOR = System
				.getProperty("path.separator");

		/**
		 * Platform Line Separator
		 */
		public static final String LINE_SEPARATOR = System
				.getProperty("line.separator");
	}

	public final static class SystemProperties {
		public static final String MAC_ADDRESS = System
				.getProperty("macAddress");
	}

	public final static String CHARACTER_SET = "UTF-8";

	private Consts() {

	}
}
