package com.datastructure.nonlinear.bst;

public class SerializeDeserialize {
	
	SerializeDeserialize(){
		
	}
	String serializedtree="";
	int i =0;
	public void serialize(Node1 root){
		walkInOrder	(root);
		serializedtree = serializedtree.substring(0, serializedtree.lastIndexOf(','));
	}
	
	public Node1 deserialize(){
		String[] tmp = serializedtree.split(",");
		
		if(tmp!=null && tmp.length > 0){
			
			Node1 n =null;
			return walkInOrder(n, tmp);
		}
		else return null;
	    
	}
	
	private void walkInOrder(Node1 root) {
	    if (root == null) {
	    	serializedtree = serializedtree+"#,";
	        return;
	    }
	    
	    
	    //walk inorder
	    serializedtree = serializedtree+root.value+",";
	    
	    walkInOrder(root.left);
	    
	    walkInOrder(root.right);
	    
	}
	
	private Node1 walkInOrder(Node1 root, String[] e) {
		
		
	    if (e[i].equals("#")) {
	    	
	        return null;
	    }else {
	    	root = new Node1();
	    	root.value = Integer.valueOf(e[i]);
	    	Node1 n = null;
	    	++i;
	    	if(i<e.length)
	    	root.left = walkInOrder(n,e);
	    	
	    	++i;
	    	if(i<e.length)
	    	root.right = walkInOrder(n,e);
	    	return root;
	    	
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
	         
		SerializeDeserialize c = new SerializeDeserialize();
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
		
		
		c.serialize(n);
		
		System.out.println(c.serializedtree);
		
		Node1 n2 = c.deserialize();
		
		display("r", n2);
		
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






