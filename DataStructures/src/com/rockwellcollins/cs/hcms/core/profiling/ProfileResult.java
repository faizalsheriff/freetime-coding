package com.rockwellcollins.cs.hcms.core.profiling;

public class ProfileResult {
	double min;
	double max;
	double value;
	double time;
	double grade;
	String notes;
	boolean valid = false;

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getGrade() {
		return grade;
	}

	public void setGrade(double grade) {
		this.grade = grade;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void clear() {
		grade = 0;
		notes = "";
		time = 0;
		max = 0;
		min = 0;
		value = 0;
		valid = false;
	}

	public void setResult(final ProfileResult result) {
		grade = result.grade;
		notes = result.notes;
		time = result.time;
		max = result.max;
		min = result.min;
		value = result.value;
		valid = true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("min:");
		sb.append(min);
		sb.append("max:");
		sb.append(max);
		sb.append("value:");
		sb.append(value);
		sb.append("time:");
		sb.append(time);
		sb.append("grade:");
		sb.append(grade);
		sb.append("notes:'");
		sb.append(notes);
		sb.append("'}");
		return sb.toString();
	}
}
