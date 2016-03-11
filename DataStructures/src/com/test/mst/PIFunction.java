package com.test.mst;

public class PIFunction {
	
	private String nodeLabelFrom;
	private String nodeLabelTo;
	private int weight;
	public PIFunction(String fromLabel, String toLabel, int edgeWeight) {
		
		this.nodeLabelFrom = fromLabel;
		this.nodeLabelTo = toLabel;
		this.weight = edgeWeight;
		
	}
	public String getNodeLabelFrom() {
		return nodeLabelFrom;
	}
	public void setNodeLabelFrom(String nodeLabelFrom) {
		this.nodeLabelFrom = nodeLabelFrom;
	}
	public String getNodeLabelTo() {
		return nodeLabelTo;
	}
	public void setNodeLabelTo(String nodeLabelTo) {
		this.nodeLabelTo = nodeLabelTo;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	

}
