package com.rockwellcollins.cs.hcms.core.collections;

public class IntArray {

	private int capacity;
	public int length;
	public int[] values;

	public IntArray(final int capacity) {
		this.capacity = capacity;
		length = 0;
		values = new int[capacity];
	}

	public final void add(final int i) {
		if (length >= capacity) {
			resize(capacity << 1);
		}
		values[length++] = i;
	}

	public final int capacity() {
		return capacity;
	}

	public final void resize(int capacity) {
		int[] dst = new int[capacity];
		length = Math.min(capacity, length);
		System.arraycopy(values, 0, dst, 0, length);
		values = dst;
		this.capacity = capacity;
	}

	public final int[] trim() {
		int[] dst = new int[length];
		System.arraycopy(values, 0, dst, 0, length);
		return dst;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (length > 0) {
			for (int i = 0; i < length - 1; i++) {
				sb.append(values[i]);
				sb.append(",");
			}
			sb.append(values[length - 1]);
		}
		sb.append("}");
		return sb.toString();
	}
}
