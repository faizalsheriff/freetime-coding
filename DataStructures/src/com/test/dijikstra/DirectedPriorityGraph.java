package com.test.dijikstra;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;

import com.test.dijikstra.Vertex;

public class DirectedPriorityGraph {
private ArrayList<Vertex> vertices = new ArrayList<Vertex>();
HashMap<Integer, Vertex> tracker = new HashMap<Integer, Vertex>(); 


public DirectedPriorityGraph(ArrayList<Vertex> vertice){
	
this.vertices = vertice;
//int lab=0;
for(Vertex v: vertices){
//v.setLabel(lab);
tracker.put(v.label(), v);
//lab++ ;//internal lable to identify the node
}

}


public boolean hasNext() {
	
	return vertices.size()>0?true:false;
}


public int size() {
	
	return vertices.size();
}


public Vertex getRandom(){
int nbits = vertices.size()-1;
int seed =1;
  long x = seed;
  x ^= (x << 21);
  x ^= (x >>> 35);
  x ^= (x << 4);
  seed = (int) x;
  x &= ((1L << nbits) - 1);
int ind = (int) x;
  Vertex n = vertices.get(ind);
 //rem(ind);
 return n;
}


public Vertex extractMin(){
Collections.sort(vertices);
Vertex top = vertices.get(0);
rem(0);
return top;
}


private void rem(int index){
tracker.remove((Object)vertices.get(index).label());
vertices.remove(index);
}


public boolean has(Vertex vertex) {
	
	return tracker.get(vertex.label())!=null?true:false;
}


public void update(int edgeWeight, Vertex vertex) {
	
	ListIterator<Vertex> it = vertices.listIterator();
	Vertex ptr = null;
	while(it.hasNext()){
	ptr = it.next();
	
	if(ptr.compareLabel(vertex)==0){
		ptr.setValue(edgeWeight);
		return;
	}
		
	}
	
	
}
 
}
