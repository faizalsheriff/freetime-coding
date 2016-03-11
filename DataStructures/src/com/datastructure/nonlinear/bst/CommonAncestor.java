package com.datastructure.nonlinear.bst;

public class CommonAncestor {
	
	CommonAncestor(){
		
	}
	Node1 ancestor;
	public Node1 findLCA(Node1 root, Node1 left, Node1 right){

		   if(root == null)
		   return null;

		    if(root.value == left.value && left.value == right.value)
		        {
		            return root;
		    
		        }
		        
		    Node1 leftT = findLCA(root.left, left, right);
		    
		    Node1 rightT = findLCA(root.right, left, right);
		    
		   
		    if(leftT!=null && rightT !=null){
		    	 System.out.println("Processing"+ leftT.value+"::"+rightT.value);
		    if(leftT.parent == rightT.parent){
		    	ancestor = root;
		    return root;
		    }
		    else if (rightT.value == right.value)
			return root;
			else if(leftT.value == left.value)
			return root;
		    else if (leftT.value == right.value)
		    return root;
		    else if(rightT.value == left.value)
		    return root;
		    else
		    return root;
		    }else if (leftT!=null){
		    	 System.out.println("Processing"+ leftT.value);
		    	return leftT;
		    }else if(rightT!=null){
		    	 System.out.println("Processing"+rightT.value);
		    	return rightT;
		    }
		    else
		    	return root;
		   

		}
	
	
	public Node1 lowestCommonAncestor(Node1 root, Node1 a, Node1 b) {
	    if (root == null) {
	        return null;
	    }
	    
	    if (root.equals(a) || root.equals(b)) { 
	        // if at least one matched, no need to continue
	        // this is the LCA for this root
	        return root;
	    }
	 
	    Node1 l = lowestCommonAncestor(root.left, a, b);
	    Node1 r = lowestCommonAncestor(root.right, a, b);
	 
	    if (l != null && r != null) {
	    	return root;  // nodes are each on a seaparate branch
	    }
	 
	    // either one node is on one branch, 
	    // or none was found in any of the branches
	    return r != null ? r : l;
	}
	
	
	public static void main (String[] args){
		CommonAncestor c = new CommonAncestor();
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
		
		
		Node1 ancestor = c.findLCA(n, n.left.left, n.left.right.right);
		
		if(ancestor==null){
			System.out.println("No ancesstor found");
		}else{
			System.out.println("Ancesstor found"+ancestor.value);
		}
			
		
		
		ancestor = c.lowestCommonAncestor(n, n.left.left, n.left.right.right);

		if(ancestor==null){
			System.out.println("C No ancesstor found");
		}else{
			System.out.println(" C Ancesstor found"+ancestor.value);
		}
		
	}
	

}




class Node1 {

int value;
Node1 right;
Node1 left;
Node1 parent;

 

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + value;
	return result;
}
@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	Node1 other = (Node1) obj;
	if (value != other.value)
		return false;
	return true;
}



}


class Top {
	 public Top(String s) { System.out.print("B"); }
	}
 class Bottom2 extends Top {
	 public Bottom2(String s) {
		 super(s);
		 System.out.print("D");
		 
	 }
	 public static void main(String [] args) {
	 new Bottom2("C");
	 System.out.println(" ");
	} }

