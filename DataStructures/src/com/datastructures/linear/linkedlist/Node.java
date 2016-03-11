package com.datastructures.linear.linkedlist;

public class Node{
private int value;
private Node nextNode;

Node(int value, Node n){
	this.value = value ;
	this.nextNode = n;
}
public int getValue(){
return value;
}

public void setValue(int v){
this.value=v;
}

public Node (int value){
this.value= value;
}


public void setNext(Node nextNode){
this.nextNode = nextNode;
}

public Node getNext(){
return this.nextNode;
}

public boolean hasNext(){
return (this.getNext()!=null);
}



} 
