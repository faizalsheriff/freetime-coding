package com.rateurbeat.riskparser.utils;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Parser {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Gson gson = new Gson();
		String str = "{\"ErrorMessageHashMap\":{}," +
				"\"Risk\":[" +
				"{\"riskType\":\"CVD\",\"risk\":44.0,\"riskPercentile\":44,\"comparisonRisk\":44.0,\"ratingForAge\":3,\"rating\":3}," +
				"{\"riskType\":\"UpperBoundCVD\",\"risk\":44.0,\"riskPercentile\":44,\"comparisonRisk\":44.0,\"ratingForAge\":3,\"rating\":3}," +
				"{\"riskType\":\"LowerBoundCVD\",\"risk\":44.0,\"riskPercentile\":44,\"comparisonRisk\":44.0,\"ratingForAge\":3,\"rating\":3}" +
				"]," +
						"\"Recommendation\":18, " +
						"\"Interventions\":" +
						"{" +
						"\"IncreaseInRisk\":44.0," +
						"\"PercentReductionInRiskWithMedication\":44.0," +
						"\"PercentReductionInRiskWithAdditionalModerateExercise\":44.0," +
						"\"PercentReductionInRiskWithAdditionalVigorousExercise\":44.0," +
						"\"PercentReductionInRiskWithWeightLoss\":44.0," +
						"\"PoundsOfWeightLossRequired\":44," +
						"\"PercentReductionWithSmokingCessation\":44.0," +
						"\"PercentReductionWithAllInterventions\":44.0" +
						"}," +
						"\"ElevatedBloodPressure\":true," +
						"\"ElevatedCholesterol\":true," +
						"\"WarningCode\":3," +
						"\"DoctorRecommendation\":13}";
		String errStr = "{\"ErrorMessageHashMap\":{\"1\":[\"Invalid/Unsupported Format: age\",\"Invalid/Unsupported Format: hdl\",\"Invalid/Unsupported Format: ldl\"], \"2\":[\"Invalid/Unsupported Format: age\",\"Invalid/Unsupported Format: hdl2\",\"Invalid/Unsupported Format: ldl2\"]},\"Risk\":null,\"Recommendation\":0,\"Interventions\":null,\"ElevatedBloodPressure\":false,\"ElevatedCholesterol\":false,\"WarningCode\":0,\"DoctorRecommendation\":0}";
		
		try {
			System.out.println("String is " + str);
			Info info = gson.fromJson(str, Info.class);
			System.out.println("Info class parsed.." + info.getRecommendation() + info.getRecommendation() + info.getWarningCode() + info.isElevatedBloodPressure() + info.isElevatedCholesterol());
			int i = 0;
			if(info.getRisk() != null) {
				for(RiskInfo rInfo : info.getRisk() ) {
					System.out.println("Risk Type " + i++ + " type="+ rInfo.riskType);
					System.out.println("Risk Rating For Age "+rInfo.ratingForAge);
					System.out.println("Risk Comparison Risk "+rInfo.comparisonRisk);
					System.out.println("Risk Index"+rInfo.risk);
					System.out.println("Risk Percentile For Age "+rInfo.riskPercentile);
					//System.out.println("Risk Type "+rInfo.riskType);
				}
			}
			if(info.getInterventions() != null) {
				System.out.println("WeightLossRequired = " + info.getInterventions().getPoundsOfWeightLossRequired());
			}
			if(info.getErrorMessageHashMap() != null) {
				i =0;
				for(String[] s : info.getErrorMessageHashMap().values()) {
					for(String st : s) {
						System.out.println("Error " + i + " str=" + st);
					}
					i++;
				}
			}
		}
		catch(JsonSyntaxException ex) {
			System.out.println("Exception " + ex.getMessage());
		}
		
	}

}
