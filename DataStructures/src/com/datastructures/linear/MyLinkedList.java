package com.datastructures.linear;

import com.datastructures.linear.linkedlist.Node;

public class MyLinkedList{

private Node root;
private Node lastNode;
private int size=0;

public void add(int nodeValue){

if(root ==null){
root = new Node(nodeValue);
lastNode = root;
size++;
}else{
lastNode.setNext(new Node(nodeValue));
lastNode = lastNode.getNext();
size++;
}

}


public int removeNext(){

if(root!=null){
	int value=root.getValue();
    size--;
    root=root.getNext();
	return value;
}else{
return (Integer)null;
}

}

public int peep(int nodeValue)throws Exception{
Node node=null;
while(root.hasNext()){
node=root.getNext(); 
if(node.getValue()==nodeValue){

return node.getValue();
}

}
return (Integer) null;
}



public void eliminatDups(){

	for(Node o=root; o!=null;o=o.getNext()){
		for(Node i=o.getNext();i!=null;i=i.getNext()){
			if(i.getNext()==null)
				continue;
			
	if(o.getValue()==i.getValue()){
		i.setValue(i.getNext().getValue());
		i.setNext(i.getNext().getNext());	
	}

	}
	}
	}



public void walk(){
	for(Node o=root; o!=null;o=o.getNext()){
		System.out.println(o.getValue());
	}
}

public int peepN(int n){

	//assuming we have size of the node

	if(n>size)
	return -1;

	else if(size==n){
	return root.getValue();
	}

	else{
	n=size-n;

	int val=-1;

	for(Node i = root;n>0; i= i.getNext(),n--){
	val=i.getValue();
	}

	return val;
	}


	}

public int size() {
	
	return size;
}

public Node addNodes(Node b){
	return addNodes(root, b);
}

public Node addNodes(Node a, Node b){

if(a==null)
return b;

else if(b==null)
return a;

else{

int r=0;
boolean isA= true;
boolean isB= true;

Node ia=a; //index
Node ib=b; //index

while(isA || isB){
	
if(isA && isB)	{
ia.setValue(ia.getValue()+ib.getValue()+r);

if(ia.getValue()>10){
r=ia.getValue()/10;
ia.setValue(ia.getValue()%10);
}else{
	r=0;
}


}else if(isB){ //b is longer than a
	Node n = new Node(0);
	//set new node as current node
	ia.setNext(n);
	ia= ia.getNext();
	ia.setValue(ib.getValue()+r);
	if(ia.getValue()>10){
	r=ia.getValue()/10;
	ia.setValue(ia.getValue()%10);
	}else{
		r=0;
	}
	
}else{ //a is longer than a
	
	ia.setValue(ia.getValue()+r);
	if(ia.getValue()>10){
		r=ia.getValue()/10;
		ia.setValue(ia.getValue()%10);
		}else{
			r=0;
		}
	
}

if(ia.getNext()!=null)
	ia= ia.getNext();
else
	isA=false;

if(ib.getNext()!=null)
	ib=ib.getNext();
else
	isB=false;

}

if(r>0){
	Node n = new Node(r);
	//set new node as current node
	ia.setNext(n);
}

}

return a;
}


public Node getRoot(){
	return root;

}


public void walkStaright() {
	for(Node o=root; o!=null;o=o.getNext()){
		System.out.print(o.getValue());
	}
	
}




}


