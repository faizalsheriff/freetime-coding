package com.datastructure.nonlinear.bst;

public class MirrorBinaryTrees {
	
	MirrorBinaryTrees(){
		
	}
	String serializedtree="";
	int i =0;
	
	
	public boolean isMirror(Node1 n, Node1 m){
		if(n==null || m == null)
			return false;
		
		
		
		
		
		
		return walkBreadthwise(n,m);
		
		
	}
	
	
	private boolean walkBreadthwise(Node1 n, Node1 m) {
		
		if(n==null && m==null)
			return true;
		else if(n==null || m==null || n.value != m.value )
			return false;
		else
		return walkBreadthwise(n.left, m.right)  && walkBreadthwise(n.right, m.left);
	}


	public static void main (String[] args){
		
	 /*     _______3______						
	       /              \
	    ___5__          ___1__
	   /      \        /      \
	   6      _2       0       8
	         /  \
	         7   4 
	         
	         
	         
	        _______3______						
	       /              \
	       1              5
	       /\             /\ 
	    8     0          2   6
	                    /\
	                   4  7
	*/                   
	         
		MirrorBinaryTrees c = new MirrorBinaryTrees();
		
		Node1 m = new Node1();
		m.value = 3;
		
		Node1 mr1 = new Node1();
		mr1.value = 5;
		Node1 ml1 = new Node1();
		ml1.value = 1;
		m.left = ml1;
		m.right = mr1;
		
		
		Node1 mr1r2 = new Node1();
		mr1r2.value = 6;
		Node1 mr1l2 = new Node1();
		mr1l2.value = 2;
		mr1.left = mr1l2;
		mr1.right = mr1r2;
		
		
		Node1 ml1l2 = new Node1();
		Node1 ml1r2 = new Node1();
		ml1r2.value = 0;
		ml1l2.value = 8;
		
		ml1.left = ml1l2;
		ml1.right = ml1r2;
		
		
		Node1 mr1l2r3 = new Node1();
		Node1 mr1l2l3 = new Node1();
		
		mr1l2r3.value = 7;
		mr1l2l3.value = 4;
		
		mr1l2.left = mr1l2l3;
		mr1l2.right = mr1l2r3;
	
		
		
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
		l1r2.value = 7;
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
		
		
		//c.serialize(n);
		
		System.out.println(c.isMirror(n, m));
		
		//Node1 n2 = c.deserialize();
		
		//display("r", n2);
		
	}

	private static void display(String p, Node1 root) {
		   if (root == null) {
		    	
		        return;
		    }
		    
		    
		    //walk inorder
		   if(p.equals("r"))
		   System.out.println("-"+root.value+"-");
		   else if(p.equals("l")){
		   System.out.println(" /");
		   System.out.println("/");
		   System.out.println("-"+root.value+"-");
		   }
		   else if(p.equals("r")){
			   System.out.println(" \t \\");
			   System.out.println("\t   \\");
			   System.out.println("-"+root.value+"-");
			   }
		    
		   display("l",root.left);
		   
		 
		    
		   display("r", root.right);
	}
	

}






