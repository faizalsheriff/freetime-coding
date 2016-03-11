package com.datastructure.nonlinear.bst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DistanceFromLeaf {

	



DNode head = null;
DNode prev = null;
public void findKDistanceFromLeaf(int k, Node1 n)
{

  if(n == null)
  return;
  
  ArrayList<Integer> list = new ArrayList<Integer>();
  Set<Integer> set = new HashSet<Integer>();
  list.add(n.value);
 // int height = 0;
  walkTree(list,set, n, k);
  
 
}








private void walkTree(ArrayList<Integer> path, Set<Integer> set, Node1 n, int k) 
{
	if(n == null)
		return ;
	
	//height++;
	
	if(n.left == null && n.right == null)
	{
		if(path.size() - 1 < k){
			set.add(path.get((path.size()-1)-k));
		}
		return;
	}
		
	else{
		ArrayList<Integer> leftPath = new ArrayList<Integer>();
		leftPath.addAll(path);
		leftPath.add(n.value);
		walkTree(leftPath, set, n.left,k);
		
		ArrayList<Integer> rightPath = new ArrayList<Integer>();
		rightPath.addAll(path);
		rightPath.add(n.value);
		walkTree(rightPath, set, n.right,k);
	}
	
}








public static void main (String[] args){
	
	 /*     _______3______
	       /              \
	    ___5__          ___1__
	   /      \        /      \
	   6      _2       0       8
	         /  \
	         7   4 
	*/
	         
	   BSTToDD c = new BSTToDD();
		Node1 n = new Node1();
		n.value = 3;
		
		Node1 l1 = new Node1();
		l1.value = 5;
		Node1 r1 = new Node1();
		r1.value = 1;
		n.left = l1;
		n.right = r1;
		n.left.parent = n;
		n.right.parent = n;
		
		Node1 l1l2 = new Node1();
		l1l2.value = 6;
		Node1 l1r2 = new Node1();
		l1r2.value = 2;
		l1.left = l1l2;
		l1.right = l1r2;
		
		l1l2.parent = l1;
		l1r2.parent = l1;
		
		Node1 r1l2 = new Node1();
		Node1 r1r2 = new Node1();
		r1l2.value = 0;
		r1r2.value = 8;
		
		r1.left = r1l2;
		r1.right = r1r2;
		r1l2.parent=r1;
		r1r2.parent=r1;
		
		Node1 l1r2r3 = new Node1();
		Node1 l1r2l3 = new Node1();
		
		l1r2l3.value = 7;
		l1r2r3.value = 4;
		
		l1r2.left = l1r2l3;
		l1r2.right = l1r2r3;
		l1r2l3.parent = l1r2;
		l1r2r3.parent = l1r2;
		
		
		c.transformToDouble(n);
		
		ArrayList l = new ArrayList();
		l.add(1);
	}



}



