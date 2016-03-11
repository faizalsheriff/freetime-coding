package com.test.dijikstra;

import java.util.ArrayList;

public class DSPTester {
	
	public static void main(String[] args) {
		
	    ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		
		Vertex v1 = new Vertex(1);
		Vertex v2 = new Vertex(2);
		Vertex v3 = new Vertex(3);
		Vertex v4 = new Vertex(4);
		
		
		ArrayList<Edge> adj1 = new ArrayList<Edge>();
		Edge e1 =  new Edge(6,v2);
		Edge e2 =  new Edge(1,v3);
		adj1.add(e1);
		adj1.add(e2);
		v1.setAdj(adj1);
		
		
		ArrayList<Edge> adj2 = new ArrayList<Edge>();
		
		Edge e3 =  new Edge(6,v1);
		Edge e4 =  new Edge(7,v4);
		Edge e5 =  new Edge(8,v3);
		
		
		adj2.add(e3);
		adj2.add(e4);
		adj2.add(e5);
		v2.setAdj(adj2);
		
		
		

		ArrayList<Edge> adj3 = new ArrayList<Edge>();
		
		Edge e6 =  new Edge(1,v1);
		Edge e7 =  new Edge(9,v4);
		Edge e8 =  new Edge(8,v2);
		
		
		adj3.add(e6);
		adj3.add(e7);
		adj3.add(e8);
		v3.setAdj(adj3);
		
		
		

		ArrayList<Edge> adj4 = new ArrayList<Edge>();
		
		
		Edge e10 =  new Edge(9,v3);
		Edge e11 =  new Edge(7,v2);
		
		
		adj4.add(e10);
		adj4.add(e11);
	
		v4.setAdj(adj4);
		
		
		vertices.add(v1);
		vertices.add(v2);
		vertices.add(v3);
		vertices.add(v4);
		
		DSP mst = new DSP();
		
		DirectedPriorityGraph ug = new DirectedPriorityGraph(vertices);
		mst.setuG(ug);
		mst.spGen();
		ShortestPathTree sp = mst.getSpanT();
		
		for (Vertex i:sp.getMinNodeList()){
			System.out.println(i.label()+"::"+i.value());
		}
		
		
		//mst.getSpanT();
		
		
		
		
	}

}
