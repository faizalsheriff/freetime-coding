package com.test.mst;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MinimumSpanningTreeGenerator {

	
	private HashMap<String,Vertex> undirectedGraph= null;
	private Vertex arbitrarySource=null;
	private HashMap<String,Vertex> spanningTreeNodes= new HashMap<String,Vertex>();
	private ArrayList<PIFunction> piFunction = new ArrayList<PIFunction>();
	MinimumSpanningTreeGenerator(HashMap<String,Vertex> undirectedGraph){
	this.undirectedGraph = undirectedGraph;	
	}
	
	
	public void generateMinimumSpanningTree(){
		Vertex currentVertex=null;
		Iterator<String> nodeLabels = undirectedGraph.keySet().iterator();
		
		while (nodeLabels.hasNext()){
			
			if(arbitrarySource==null){
				arbitrarySource = undirectedGraph.get(nodeLabels.next());
				currentVertex= arbitrarySource;
				spanningTreeNodes.put(arbitrarySource.getLabel(), arbitrarySource);
			}
			
			String[] verticeLabels= currentVertex.getAllAdjacentVertices();
			
			for(String vertexLabel:verticeLabels){
				Vertex unporcessedVertex = undirectedGraph.get(vertexLabel);
				int edgeWeight = currentVertex.getAdjacentVertexWeight(vertexLabel);
				if(spanningTreeNodes.get(vertexLabel)==null && unporcessedVertex.getValue()>edgeWeight){
					
					unporcessedVertex.setValue(edgeWeight);
					undirectedGraph.put(vertexLabel, unporcessedVertex);
					addToPI(currentVertex.getLabel(),vertexLabel,edgeWeight);
					}
				}
			
			undirectedGraph.remove(currentVertex.getLabel());
			currentVertex = currentVertex.extractMinVertex();
			nodeLabels = undirectedGraph.keySet().iterator();
			
		}
		
	}


	private void addToPI(String fromLabel, String toLabel,
			int edgeWeight) {
		
		for(int index=0;index<piFunction.size();index++){
			PIFunction function = piFunction.get(index);
			if(function.getNodeLabelFrom().equalsIgnoreCase(fromLabel) || function.getNodeLabelFrom().equalsIgnoreCase(toLabel)
					|| function.getNodeLabelTo().equalsIgnoreCase(fromLabel) || function.getNodeLabelTo().equalsIgnoreCase(toLabel)
					){
				if(function.getWeight()>edgeWeight){
					function.setWeight(edgeWeight);
					piFunction.remove(index);
					piFunction.add(function);
				}
			}else{
				
				piFunction.add(new PIFunction(fromLabel,toLabel,edgeWeight));
			}
			
		}
		
	}
	
}
