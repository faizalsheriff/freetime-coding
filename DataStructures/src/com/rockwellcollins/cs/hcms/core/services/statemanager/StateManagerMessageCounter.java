package com.rockwellcollins.cs.hcms.core.services.statemanager;

public class StateManagerMessageCounter {

	private int[] counter;

	private int totalCount;

	public StateManagerMessageCounter(final int size) {
		counter = new int[size];
		reset();
	}

	public synchronized void add(final int index, final int size) {
		counter[index] = counter[index] + size;
		totalCount = totalCount + size;
	}

	public synchronized void sub(final int index, final int size) {
		counter[index] = counter[index] - size;
		totalCount = totalCount - size;
	}

	public synchronized int get(final int index) {
		return counter[index];
	}

	public synchronized int total() {
		return totalCount;
	}

	public synchronized void reset() {
		for (int i = 0; i < counter.length; i++) {
			counter[i] = 0;
		}
	}
}
