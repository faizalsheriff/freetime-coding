package com.test.shortestpath;

import java.util.HashMap;

public class Vertex {
	
	private String label;
	private HashMap<String, Integer> adjacentVerticeMap;
	
	
	public Vertex(String name) {
		this.label=name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public HashMap<String, Integer> getAdjacentVerticeMap() {
		return adjacentVerticeMap;
	}
	public void setAdjacentVerticeMap(HashMap<String, Integer> adjacentVerticeMap) {
		this.adjacentVerticeMap = adjacentVerticeMap;
	}


}
