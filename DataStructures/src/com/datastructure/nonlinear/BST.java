package com.datastructure.nonlinear;

import com.datastructure.nonlinear.bst.Node;

public class BST{

private Node root;

public void add(int ne){
Node newN = new Node(ne);
if(root==null){
root=newN;
}else{
insert (newN);
}
}


private void insert(Node newN){

findPos(root, root, newN);


}


public void findPos(Node parent, Node root, Node nw){
if(nw.getValue()<root.getValue()){
handleLeftTree(parent, root.getLeft(), nw);
}else{
handleRightTree(parent, root.getRight(), nw);
}
}


public void handleRightTree(Node parent, Node right, Node nw){
if(right==null){
parent.setRight(nw);
right=parent.getRight();
right.setParent(parent);//letting right know it's parent
if(right.getParent().getLeft()!=null){
right.getParent().setBalanced(true);
}
}
else{

if(right.isBalanced()){//node is already balanced
if(nw.getValue()>=right.getValue()){
findPos(right, right.getRight(),nw);
}else if (nw.getValue()<nw.getValue()){
findPos(right, right.getRight(),nw);
}
}else{//node is not balanced
handleUnbalancedNode(right,nw);
}


}


}

public void handleLeftTree(Node parent, Node left, Node nw){
if(left==null){
parent.setLeft(nw);
left=parent.getLeft();
left.setParent(parent);
if(left.getParent().getRight()!=null)
left.getParent().setBalanced(true);

}else{//left is not empty

if(parent.isBalanced()){//node is already balanced
if(nw.getValue()<left.getValue()){
handleLeftTree(left, left.getLeft(),nw);
}else if (nw.getValue()>=nw.getValue()){
handleRightTree(left, left.getRight(),nw);
}
}else{//node is not balanced
handleUnbalancedNode(parent, nw);
}
}
}


public void handleUnbalancedNode(Node parent, Node nw){
if(nw.getValue()<parent.getValue() && parent.getLeft()==null){
handleLeftTree(parent, parent.getLeft(),nw);
}else if(nw.getValue()<parent.getValue() && parent.getLeft()!=null){
rightRotate(parent.getLeft(),nw);
}else if (nw.getValue()>=parent.getValue() && parent.getRight()==null){
handleRightTree(parent, parent.getRight(),nw);
}else if(nw.getValue()>=parent.getValue() && parent.getRight()!=null){
leftRotate(parent.getRight(),nw);
}
}


public void rightRotate(Node left, Node nw){
	Node right = new Node(left.getParent().getValue());
	left.getParent().setRight(right);

	
if(nw.getValue()>=left.getValue()){ //new has to be root

left.getParent().setValue(nw.getValue());

}else{ //new is left, left is parent parent is right 
left.getParent().setValue(left.getValue());
left.setValue(nw.getValue());
}

right.setParent(left.getParent());
left.getParent().setBalanced(true);

}


public void leftRotate(Node right, Node nw){
	Node left= new Node(right.getParent().getValue());
	right.getParent().setLeft(left);

if(nw.getValue()<right.getValue()){ //new has to be root
	right.getParent().setValue(nw.getValue());
}else{ //new is right
	right.getParent().setValue(right.getValue());
	right.setValue(nw.getValue());
}

left.setParent(right.getParent());
right.getParent().setBalanced(true);


}


public void inOrder(){
	inOrder(root);
}


public void inOrder(Node Root)
{
	
	
    if(Root != null)
    {
        inOrder(Root.getLeft());
        System.out.print(Root.getValue() + " ");
        inOrder(Root.getRight());
    }
}




}