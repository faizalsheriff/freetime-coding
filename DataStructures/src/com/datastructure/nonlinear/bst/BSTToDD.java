package com.datastructure.nonlinear.bst;

public class BSTToDD {

	



DNode head = null;
DNode prev = null;
public void transformToDouble(Node1 n)
{

  if(n == null)
  return;
  
  // DNode dd= createDDList(n, head);
  
  head = new DNode();
 // prev = null;
  
  DNode dd = createCircularDDList1(n, head);
  
  
  /* if(dd == null)
   {
	   System.out.println("Nothing");
	   return;
   }else if(head.prev == head.next)
	   System.out.println(dd.value);
   */
   for(DNode ptr= head.prev;  ptr!=null; ptr= ptr.next )
	   System.out.println(ptr.value);
}


/*private DNode walkInOrder(Node d)
{
  if(d== null)
  return null;
  
  
   DNode left = walkInOrder(d.left);
   DNode D = new DNode(d.value);
   if(left!=null)
   {
   
  
           left.right = D ;
           if(head == null)
           {
           head = new DNode();
           head.left = left;
           left.left = head;
           }
           else{
           
           left.left = head.right; //prev
           }
   }
   
   d.right = walkInOrder(d.right);
   
   head.right = d.right;// always point header to last
   d.left = left; //back pointer
   return left;
   
}




private DNode traverseModInOrder(Node n)
{
    DNode current = new DNode(n.value);
    current.prev = createDDList(current, n.left, head);
    current.next = createDDList(current, n.right, head);
}
*/

private DNode createDDList(Node1 current, DNode head)
{
  if(current == null)
  return null;
  
  DNode cur = new DNode(current.value);
  cur.prev = createDDList(current.left, head);
  //set the next pointer of left to point current
  
  if (cur.prev !=null)
  {
  DNode prev =  cur.prev;
  prev.next = cur;
  }
  cur.next = createDDList(current.right, head);
  //set the prev pointer of right to point to current
  
  
  if( cur.next != null)
  {
	  DNode next = cur.next;
	  next.prev = cur;
  }

  
  if(head == null)
  {
   head = new DNode();
   head.prev = cur;
  }
  if(cur.next == null)
	  head.next = cur;
  else
	  head.next = cur.next;  
  
  return cur;
  
}



private DNode createCircularDDList(Node1 cur, DNode head)
{
	if(cur == null)
	return null;
	
	DNode prev = createCircularDDList(cur.left, head);
	
	DNode current = new DNode(cur.value);
	
	if(head.prev == null)
	{
		head.prev = current;
	}
	
	head.next = current;
	
	//assign links
	if(prev != null)
	{
		current.prev = prev;
		prev.next = current;
	}
	
	DNode next = createCircularDDList(cur.right, head);
	
	if(next != null)
	{
		current.next = next;
		next.prev = current;
		return next;
	}
	
	return current;
}



private DNode createCircularDDList1(Node1 cur, DNode head)
{
	if(cur == null)
	return null;
	
    createCircularDDList1(cur.left, head);
	
	DNode current = new DNode(cur.value);
	
	if(head.prev == null)
	{
		head.prev = current;
	}
	
	head.next = current;
	
	//assign links
	if(prev != null)
	{
		current.prev = prev;
		prev.next = current;
	}
	
	prev = current;
	
	createCircularDDList1(cur.right, head);
	
	
	return current;
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
		
	}



}

class DNode {
	
	DNode prev;
	DNode next;
	int value;
	public DNode(int value) {
		super();
		this.value = value;
	}
	public DNode() {
		super();
	}
	
	
}

