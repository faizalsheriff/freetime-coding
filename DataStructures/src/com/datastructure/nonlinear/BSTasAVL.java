package com.datastructure.nonlinear;

import java.util.ArrayList;
import java.util.Collections;

import com.datastructure.nonlinear.bst.Node;




public class BSTasAVL{


private Node root;
private ArrayList<Integer> leafHeight = new ArrayList<Integer>();

public void insert(int ne){

if(root == null){

Node n = new Node(ne);
root = n;

}

else{
findPos(root, ne);

}

}


public void findPos(Node parent, int ne){
if(ne>=parent.getValue()){
handleRightSubtree(parent, ne);
}else{
handleLeftSubtree(parent, ne);
}

}


public void handleRightSubtree(Node p, int n){


if(p.isBalanced()){

if(n>=p.getValue()){
findPos(p.getRight(), n);
}else{
findPos(p.getLeft(), n);

}

}else{ // p is not balanced

if(p.getRight()==null){ //right s available
Node nwR = new Node(n);
p.setRight(nwR);
nwR.setParent(p);
nwR.setHeight(p.getHeight()+1);

}else{ // p is not balanced and right not available

leftRotate(p,n);
}

}
}



public void leftRotate(Node p, int n){

Node left = new Node(p.getValue()); 
p.setLeft(left);
left.setHeight(p.getHeight()+1);

if(n>p.getRight().getValue()){// new is right. right is root. root is left 
p.setValue(p.getRight().getValue());
p.getRight().setValue(n);

}else{ //new is root 
p.setValue(n);

}
p.setBalanced(true); //mark node as balanced

}



public void handleLeftSubtree(Node p, int n){


if(p.isBalanced()){

if(n<p.getValue()){
findPos(p.getLeft(), n);
}else{
findPos(p.getRight(), n);

}

}else{ // p is not balanced

if(p.getLeft()==null){ //left s available
Node nwL = new Node(n);
p.setLeft(nwL);
nwL.setParent(p);
nwL.setHeight(p.getHeight()+1);

}else{ // p is not balanced and right not available

rightRotate(p,n);
}

}
}



public void rightRotate(Node p, int n){

Node right = new Node(p.getValue()); 
p.setRight(right);
right.setHeight(p.getHeight()+1);

if(n>p.getLeft().getValue()){// new is left. left is root. root is right 
p.setValue(p.getLeft().getValue());
p.getLeft().setValue(n);

}else{ //new is root 
p.setValue(n);

}
p.setBalanced(true); //mark node as balanced

}

public void inOrder(Node root)
{
    if(root != null)
    {
        inOrder(root.getLeft());
        System.out.println(root.getValue() + " -->"+root.getHeight());
        inOrder(root.getRight());
    }
}

public Node getRoot(){
	return this.root;
}

public boolean isTreeBalancedCheck()
{
	
	inOrderWalk(root);
	Collections.sort(leafHeight);
	if(leafHeight!=null && leafHeight.get(leafHeight.size()-1)-leafHeight.get(0)>1){
	
		return false;
	}else{
	 return true;
	}
}


public void inOrderWalk(Node root)
{
    if(root != null)
    {
    	inOrderWalk(root.getLeft());
        System.out.println(root.getValue() + " -->"+root.getHeight());
        if(root.getLeft()==null && root.getRight()==null)// leaf{
        	{
        	leafHeight.add(root.getHeight());
        	
        	}
        inOrderWalk(root.getRight());
    }
   
  }


public void walkLeafHeight(){
	for(int i : leafHeight)
		System.out.println(i);
}

}