package com.test.dijikstra;

public class Edge {
	
	
	public Edge(int w, Vertex v){
		this.edgeWeight = w;
		this.vertex = v;
	}
	private int edgeWeight;
	private Vertex vertex;
	public int getEdgeWeight() {
		return edgeWeight;
	}
	public void setEdgeWeight(int edgeWeight) {
		this.edgeWeight = edgeWeight;
	}
	public Vertex getVertex() {
		return vertex;
	}
	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}
	
	

}
