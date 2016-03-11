package com.test.shortestpath;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Set;

public class DijikstraShortestPathCalculator {
	
	private ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
	private Vertex source =null;
	private HashMap<String,Integer> shortestDistanceMap = new HashMap<String,Integer>();
	
	public HashMap<String, Integer> getShortestDistanceMap() {
		return shortestDistanceMap;
	}


	public void setShortestDistanceMap(HashMap<String, Integer> shortestDistanceMap) {
		this.shortestDistanceMap = shortestDistanceMap;
	}


	private int currentDistance = 0;
	public DijikstraShortestPathCalculator(ArrayList<Vertex> vertexList){	
	this.vertexList = vertexList;
	}
	
	
	public void calculateShortestPaths()
	{
		for (Vertex vertex:vertexList){
			processShortestPath (vertex) ;
		}
	}


	private void processShortestPath(Vertex vertex) {
		int distance=0;
		int curLocalShortesDistance=999999999;
		Integer mapShortestDistance;
		if(source == null){
			source = vertex;
			shortestDistanceMap.put(source.getLabel(),0 );
		}
		HashMap<String,Integer> adjacenterticesMap = vertex.getAdjacentVerticeMap();
		
		Set<String> adjacentVertexLabels = adjacenterticesMap.keySet();
		for(String adjacentVerexLabel:adjacentVertexLabels){
			
			distance=adjacenterticesMap.get(adjacentVerexLabel);
			if(distance<curLocalShortesDistance){
				curLocalShortesDistance= distance;
			}
			distance=distance+currentDistance;
			mapShortestDistance=shortestDistanceMap.get(adjacentVerexLabel);
			
			if(mapShortestDistance==null){
				shortestDistanceMap.put(adjacentVerexLabel,distance);
			}else if(mapShortestDistance!=null 
					&& distance<mapShortestDistance){
				
				shortestDistanceMap.put(adjacentVerexLabel,mapShortestDistance );
				
			}
			
		}
		
		currentDistance = currentDistance+curLocalShortesDistance;
		
		
		
	}
	
	
	
	

}
