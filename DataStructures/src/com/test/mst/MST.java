package com.test.mst;

import com.test.mst.spanningtree.Edge;
import com.test.mst.spanningtree.Pi;
import com.test.mst.spanningtree.ShortestPathTree;
import com.test.mst.spanningtree.UndirectedPriorityGraph;
import com.test.mst.spanningtree.Vertex;

public class MST {
	
	

private UndirectedPriorityGraph uG;
private ShortestPathTree spanT;
private Pi pi;
private Vertex newN;

public void mspGen(){ //mininmum spanning tree generator

if(uG==null || uG.size()==0){//uG undirected Graph, a prioirty queue
return;
}
while(uG.hasNext()){

if( newN==null)
newN = uG.getRandom(); //get random vertex
else
newN = uG.extractMin();

System.out.println("Processing"+newN.label());

//spanT.add(as);
for(Edge adj:newN.getAdj()){
//update the uG as values
if(!uG.has(adj.getVertex())){
	if(adj.getEdgeWeight()<adj.getVertex().value()){
		System.out.println("Updateing "+adj.getVertex().label()+"'s value "+adj.getVertex().value()+" with "+adj.getEdgeWeight());
			uG.update(adj.getEdgeWeight(), adj.getVertex());  //update(nodeWeight, node Value)
			//update pi function
			pi.remove(adj.getVertex());
			pi.add(newN, adj.getVertex()); 
	}
}
}

if(!spanT.hasNode(newN)){
spanT.add(newN);
//pi.add(adj, newN);  //pi(fromNode, toNode)
}else {
	System.out.println("It shoudl not have come here");
	}


}
}

public UndirectedPriorityGraph getuG() {
	return uG;
}

public void setuG(UndirectedPriorityGraph uG) {
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












