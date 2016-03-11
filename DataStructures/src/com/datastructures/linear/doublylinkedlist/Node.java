package com.datastructures.linear.doublylinkedlist;

public class Node{
private int value;
private Node nextNode;
private Node previousNode;
private Node currentNodePtr;




public void setNext(Node nextNode){
this.nextNode = nextNode;
}

public Node getNext(){
return this.nextNode;
}


public void setPrevious(Node prevNode){
this.previousNode = prevNode;
}

public Node getPrevious(){
return this.previousNode;
}


public int getNodeValue(){
return value;
}

public boolean hasNext(){
return (this.getNext()!=null);
}

public Node (int value){
this.value= value;
this.currentNodePtr = this;
}

public Node getCurrentNodePtr() {
	return currentNodePtr;
}

public void setCurrentNodePtr(Node currentNodePtr) {
	this.currentNodePtr = currentNodePtr;
}
} 
