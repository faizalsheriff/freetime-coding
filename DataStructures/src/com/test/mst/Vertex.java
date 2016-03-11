package com.test.mst;

import java.util.HashMap;
import java.util.Iterator;

public class { {
	
	private String label;
	private int value;
	private HashMap<String, Integer> adjacentVertices = new HashMap<String, Integer>();
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String[] getAllAdjacentVertices() {
		
		return (String[]) adjacentVertices.keySet().toArray();
	}
	public int getAdjacentVertexWeight(String vertexLabel) {
		
		return adjacentVertices.get(vertexLabel);
	}
	public Vertex extractMinVertex() {
		Iterator<String> iterator = adjacentVertices.keySet().iterator();
		Vertex leastWeight=new Vertex();
		leastWeight.setValue(999999999);
		int currentWeight = 0;
		String currentLabel="";
		while (iterator.hasNext())
		{
			currentLabel=iterator.next();
			currentWeight = adjacentVertices.get(currentLabel);
			 if( leastWeight.getValue()>currentWeight){
				 leastWeight.setValue(currentWeight);
				 leastWeight.setLabel(currentLabel);
			 }
			
		}
		return leastWeight;
	}
	
	

}
