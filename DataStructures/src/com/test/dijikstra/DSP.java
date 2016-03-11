package com.test.dijikstra;

import java.util.HashMap;
import java.util.Iterator;

import com.test.dijikstra.Edge;
import com.test.dijikstra.ShortestPathTree;
import com.test.dijikstra.DirectedPriorityGraph;
import com.test.dijikstra.Vertex;

public class DSP {
	
	

	
	void arbitrarySort( String in, String so) {



		if(in==null || so==null || in.trim().length()==0 || so.trim().length()==0)
		return; // 

		char[] inout = in.toCharArray();
		char[] sout = so.toCharArray();
		HashMap arb = new HashMap();


		for (int i =0; i< inout.length; i++){
		if(arb.get(inout[i])==null){
		arb.put(inout[i], 1);
		}else{
		int val = (Integer) arb.get(inout[i]);
		arb.put(inout[i], val+1);
		}

		}//eof for


		//output

		for (int i =0; i< sout.length; i++){
		if(arb.get(sout[i])!=null){

		int counter=(Integer) arb.get(sout[i]);
		for(int j=0;j<counter;j++){
		System.out.print(sout[j]);
		}
		arb.remove(sout[i]);

		}
		}
		
		

		Iterator it = arb.keySet().iterator();

		while(it.hasNext()){
		String key = (String) it.next();
		int counter = (Integer) arb.get(key);

		for(int j=0;j<counter;j++){
		System.out.print(key);
		}


		}

		}
	
	
private DirectedPriorityGraph uG;
private ShortestPathTree spanT = new ShortestPathTree();
private Pi pi= new Pi();
private Vertex newN;

public void spGen(){ //shortest path gen

if(uG==null || uG.size()==0){//uG undirected Graph, a prioirty queue
return;
}
while(uG.hasNext()){

if( newN==null){
newN = uG.getRandom(); //get random vertex
uG.update(0, newN);  
}
else
newN = uG.extractMin();


//spanT.add(as);
for(Edge adj:newN.getAdj()){
//update the uG as values
//if(uG.has(adj.getVertex())){
	if((newN.value()+adj.getEdgeWeight())<adj.getVertex().value()){
			uG.update(newN.value()+adj.getEdgeWeight(), adj.getVertex());  //update(nodeWeight, node Value)
			//update pi function
			 
	}
//}
}

if(!spanT.hasNode(newN)){
spanT.add(newN);
//pi.add(adj, newN);  //pi(fromNode, toNode)
}else {
	System.out.println("It shoudl not have come here"+newN.label());
	}


}

}

public DirectedPriorityGraph getuG() {
	return uG;
}

public void setuG(DirectedPriorityGraph uG) {
	this.uG = uG;
}

public ShortestPathTree getSpanT() {
	return spanT;
}

public void setSpanT(ShortestPathTree spanT) {
	this.spanT = spanT;
}

public Pi getPi() {
	return pi;
}

public void setPi(Pi pi) {
	this.pi = pi;
}

public Vertex getNewN() {
	return newN;
}

public void setNewN(Vertex newN) {
	this.newN = newN;
}




}












