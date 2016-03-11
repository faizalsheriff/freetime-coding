package com.test.dijikstra;

import java.util.ArrayList;



public class Vertex implements Comparable<Vertex>{

private int value = Integer.MAX_VALUE; //postive infinity
private ArrayList<Edge> adj = new ArrayList<Edge>();
private int label;


Vertex(int label){
	this.label= label;
}

public int label() {
	return label;
}


public void setLabel(int label) {
	this.label = label;
}


public ArrayList<Edge> getAdj() {
	
	return adj;
}


public int value() {
	return value;
}


public void setValue(int value) {
	this.value = value;
}


public void setAdj(ArrayList<Edge> adj) {
	this.adj = adj;
}

public int compareLabel(Vertex v) {
	if(this.label> v.label){
		return 1;
	}else if(this.label< v.label){
		return -1;
	}else{
	return 0;
	}
}


@Override
public int compareTo(Vertex v) {
	
	if(this.value> v.value){
		return 1;
	}else if(this.value< v.value){
		return -1;
	}else{
	return 0;
	}
}








}