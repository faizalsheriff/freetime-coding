package com.rateurbeat.riskparser.utils;

import java.util.Hashtable;

public class Info{
	
	private Hashtable<Integer, String[]> ErrorMessageHashMap;
	private RiskInfo[] Risk;
	private int Recommendation;
	private InterventionInfo Interventions;
	private boolean ElevatedBloodPressure;
	private boolean ElevatedCholesterol;
	private int WarningCode;
	private int DoctorRecommendation;
	public Hashtable<Integer, String[]> getErrorMessageHashMap() {
		return ErrorMessageHashMap;
	}
	public void setErrorMessageHashMap(Hashtable<Integer, String[]> errorMessageHashMap) {
		ErrorMessageHashMap = errorMessageHashMap;
	}
	public RiskInfo[] getRisk() {
		return Risk;
	}
	public void setRisk(RiskInfo[] risk) {
		Risk = risk;
	}
	public int getRecommendation() {
		return Recommendation;
	}
	public void setRecommendation(int recommendation) {
		Recommendation = recommendation;
	}
	public InterventionInfo getInterventions() {
		return Interventions;
	}
	public void setInterventions(InterventionInfo interventions) {
		Interventions = interventions;
	}
	public boolean isElevatedBloodPressure() {
		return ElevatedBloodPressure;
	}
	public void setElevatedBloodPressure(boolean elevatedBloodPressure) {
		ElevatedBloodPressure = elevatedBloodPressure;
	}
	public boolean isElevatedCholesterol() {
		return ElevatedCholesterol;
	}
	public void setElevatedCholesterol(boolean elevatedCholesterol) {
		ElevatedCholesterol = elevatedCholesterol;
	}
	public int getWarningCode() {
		return WarningCode;
	}
	public void setWarningCode(int warningCode) {
		WarningCode = warningCode;
	}
	public int getDoctorRecommendation() {
		return DoctorRecommendation;
	}
	public void setDoctorRecommendation(int doctorRecommendation) {
		DoctorRecommendation = doctorRecommendation;
	}
}
	