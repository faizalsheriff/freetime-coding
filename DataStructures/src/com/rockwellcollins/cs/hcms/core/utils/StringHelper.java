package com.rockwellcollins.cs.hcms.core.utils;

import java.util.Iterator;
import java.util.List;

/**
 * Helper class for string manipulation
 * 
 * @author getownse
 * 
 */
public final class StringHelper {

	/**
	 * Join a collection of objects (using toString) together with given
	 * delimiter
	 * 
	 * @param collection
	 *            given collection
	 * @param delimiter
	 *            given delimiter
	 * @return single string with objects separated by delimiter
	 */
	public static String join(final List<?> collection, final String delimiter) {

		final StringBuilder stringBuilder = new StringBuilder();

		final Iterator<?> iter = collection.iterator();

		if (iter.hasNext()) {

			stringBuilder.append(iter.next());

			while (iter.hasNext()) {
				stringBuilder.append(delimiter);
				stringBuilder.append(iter.next());
			}
		}

		return stringBuilder.toString();
	}

	/**
	 * Creates a readable string representing a byte array with given delimiter
	 * 
	 * @param data
	 *            data to create a string representation from
	 * @param offset
	 *            starting offset within the data
	 * @param length
	 *            length of data (starting at given offset)
	 * @param delimiter
	 *            delimiter to use
	 * @return single string with byte array data delimited by given delimiter
	 */
	public static String join(final byte[] data, final int offset,
			final int length, final String delimiter) {

		final StringBuilder stringBuilder = new StringBuilder();

		if (data.length > 0 && offset < data.length) {

			int end = offset + length;

			if (end > data.length) {
				end = data.length;
			}

			for (int i = offset; i < end; i++) {
				stringBuilder.append(data[i]);
				stringBuilder.append(delimiter);
			}

			stringBuilder.append(data[end - 1]);
		}

		return stringBuilder.toString();
	}

	/**
	 * Creates a readable string representing a byte array with given delimiter
	 * 
	 * @param data
	 *            data to create a string representation from
	 * @param delimiter
	 *            delimiter to use
	 * @return single string with byte array data delimited by given delimiter
	 */
	public static String join(final byte[] data, final String delimiter) {

		final StringBuilder stringBuilder = new StringBuilder();

		if (data.length > 0) {

			for (int i = 0; i < data.length - 1; i++) {
				stringBuilder.append(data[i]);
				stringBuilder.append(delimiter);
			}

			stringBuilder.append(data[data.length - 1]);
		}

		return stringBuilder.toString();
	}

	/**
	 * Join array of strings together with a given delimiter
	 * @param stringArray array of strings to join
	 * @param delimiter delimiter to separate stringArrays
	 * @return single string of joined streamArrays with delimiter
	 */
	public static String join(final String[] stringArray, final String delimiter) {

		final StringBuilder stringBuilder = new StringBuilder();

		if (stringArray.length > 0) {

			for (int i = 0; i < stringArray.length - 1; i++) {
				stringBuilder.append(stringArray[i]);
				stringBuilder.append(delimiter);
			}

			stringBuilder.append(stringArray[stringArray.length - 1]);
		}

		return stringBuilder.toString();
	}
}
