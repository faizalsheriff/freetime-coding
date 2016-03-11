package com.test.dijikstra;

import java.util.ArrayList;
import java.util.ListIterator;

public class ShortestPathTree {
	
	private ArrayList<Vertex> minNodeList = new ArrayList<Vertex>();
	
	

	public boolean hasNode(Vertex newN) {
		
		ListIterator<Vertex> it = minNodeList.listIterator();
		Vertex v = null;
		
		while(it.hasNext()){
			v = it.next();
			if(v.compareLabel(newN)==0)
				return true;
		}
		
		return false;
	}

	public void add(Vertex newN) {
		minNodeList.add(newN);
		
	}

	public Vertex get(Vertex newN) {
		ListIterator<Vertex> it = minNodeList.listIterator();
		Vertex v = null;
		
		while(it.hasNext()){
			v = it.next();
			if(v.compareLabel(newN)==0)
				return v;
		}
		return null;
	}

	public void update(Vertex newN, int value) {
		ListIterator<Vertex> it = minNodeList.listIterator();
		Vertex v = null;
		
		while(it.hasNext()){
			v = it.next();
			if(v.compareLabel(newN)==0)
				v.setValue(value);
		}
		
		
	}

	public ArrayList<Vertex> getMinNodeList() {
		return minNodeList;
	}
	
	

}
