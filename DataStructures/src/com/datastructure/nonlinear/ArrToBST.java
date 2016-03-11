package com.datastructure.nonlinear;


import com.datastructure.nonlinear.bst.Node;

public class ArrToBST {



private int sortAr[];
private Node root;

public ArrToBST(int[] a){
this.sortAr=a;
}

public void constructBST(){

if(sortAr==null){
return;
}else{
int mid = sortAr.length/2;
this.root=new Node(sortAr[mid]);
if(mid-1>0)
root.setLeft(recur(0, mid-1));

if(mid+1<sortAr.length-1)
root.setRight(recur(mid+1, sortAr.length-1));
}
}


public Node recur(int start, int last){
int mid = start+(last-start)/2;

if(start<mid && mid <last){
Node root = new Node(sortAr[mid]);

if(mid-1>start)
root.setLeft(recur(start, mid-1));
else if(mid-1==start)
	root.setLeft(new Node(sortAr[mid-1]));

if(mid+1<last)
root.setRight(recur(mid+1, last));
else if(mid+1==last)
	root.setRight(new Node(sortAr[mid+1]));

return root;
}else if(start<mid){

Node left = new Node(sortAr[start]);
Node root = new Node(sortAr[mid]);
root.setLeft(left);
return root;

}else if(mid<last){
Node right = new Node(sortAr[last]);

Node root = new Node(sortAr[mid]);
root.setRight(right);
return root;

}else{
	System.out.println("It should not come here"+mid+start+last);
	return null;
}

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
return root;
}


}