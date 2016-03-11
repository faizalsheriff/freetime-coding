package com.test.mst.spanningtree;


import java.util.ArrayList;

import java.util.ListIterator;


public class Pi {
	
	
	private ArrayList<PiMap> fromTo= new ArrayList<PiMap>();

	public void remove(Vertex to) {
		
		
		ListIterator<PiMap> it = fromTo.listIterator();
		
		PiMap ref;
         while(it.hasNext()){
        	ref = it.next();
        	if(ref.f.compareLabel(to)==0 || ref.t.compareLabel(to)==0){
        		it.remove();
        		//return;
        	}
        	 
         }
         
         
		
      
		
	}
	
	
public void add(Vertex newN, Vertex vertex) {
		
		
		PiMap piM = new PiMap(newN, vertex);
		
		fromTo.add(piM);
		
	}
	

	

	class PiMap{
		
		private Vertex f;
		private Vertex t;
	
		
		public PiMap(Vertex from, Vertex to) {
			// TODO Auto-generated constructor stub
			this.f = from;
			this.t = to;
		}

	
	}
	
	
	
	


}

