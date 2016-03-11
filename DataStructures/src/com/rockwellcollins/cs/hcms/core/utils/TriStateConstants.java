package com.rockwellcollins.cs.hcms.core.utils;

public enum TriStateConstants {
	TRUE("true"), FALSE("false"), UNKNOWN("unknown");

	private String value;

	private TriStateConstants(String s) {
		value = s;
	}

	public String getValue() {
		return value;
	}
}
