package unittests;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastructure.nonlinear.BST;
import com.datastructure.nonlinear.BSTasAVL;
import com.datastructures.linear.*;
import com.datastructures.linear.linkedlist.LinkedListMergeSort;
import com.datastructures.linear.linkedlist.Node;
import com.test.SORT.QuickSortInArray;

public class DataStructureTester {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testQuickSort() {
		int[] b={1,6,4,3,67};
		
		QuickSortInArray quickSortInArray = new QuickSortInArray(b);
		quickSortInArray.quicksort();
		int nextIndex = 0;
		
			assertTrue("inout == output ",b.length==quickSortInArray.getUnsortedList().length);
			
			b= quickSortInArray.getUnsortedList();
			
			for(int i=0;i<quickSortInArray.getUnsortedList().length-1;i++){
				nextIndex=i+1;
				if(nextIndex<=(quickSortInArray.getUnsortedList().length-1)){
					assertTrue("Aray sorted in ascending order", b[i]<b[nextIndex]);
					
				}
			}
		
	}
	
	
	
	@Test
	public void testStack(){

	int[] src={3,4,5,63,45};
	//int[] testSrc=new int[src.length];
	MyStack stack= new MyStack();

	for (int ele:src){
	stack.push((Object)new Integer(ele));
	}

	
	//stack.pop();

	assertTrue("Pass- input equals output integrity is maintained", stack.size()==src.length);


	}
	
	
	
	@Test
	public void testQueue(){

	int[] a= {2,3,4,5,6};
	MyQueue queue = new MyQueue();
	for (int ele:a){
	queue.add(ele);
	}

	int top = queue.remove();

	assertTrue("FIFO Test is passed", top==a[0]);


	}
	
	
	@Test
	public void addLinkedList(){
	MyLinkedList linkedList = new MyLinkedList();
	
	linkedList.add(1);
	linkedList.add(2);
	linkedList.add(3);
	linkedList.add(4);
	linkedList.add(9);
	
	
	MyLinkedList linkedListb = new MyLinkedList();
	linkedListb.add(1);
	linkedListb.add(2);
	linkedListb.add(3);
	linkedListb.add(8);
	linkedListb.add(9);
	Node b= linkedListb.getRoot();
	
	
	Node ans = linkedList.addNodes(b);
	//linkedList.eliminatDups();
	
	//linkedList.walk();
	//linkedList.walkStaright();
	//System.out.println(linkedList.peepN(5));
	
	/*int origSize= linkedList.size();
	System.out.println(origSize);
	assertTrue("Element Found ",linkedList.removeNext()==3);
	assertTrue("Element Found",linkedList.removeNext()==5);
	
	//System.out.println(linkedList.size()-2);
	assertTrue((origSize-2)==linkedList.size());*/
	
	
	for(Node o=ans; o!=null;o=o.getNext()){
		System.out.print(o.getValue());
	}
	
	
		
	}
	
	
	@Test
	public void addDoublyLinkedList(){
	MyDoublyLinkedList linkedList = new MyDoublyLinkedList();
	
	linkedList.add(3);
	linkedList.add(5);
	linkedList.add(6);
	linkedList.add(7);
	linkedList.add(9);
	
	
	int origSize= linkedList.size();
	System.out.println(origSize);
	try {
		assertTrue("Element Found ",linkedList.remove(3)==3);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		assertTrue("Element Found",linkedList.remove(5)==5);
	} catch (Exception e) {
		
		e.printStackTrace();
	}
	
	assertTrue(linkedList.size()==3);
	
	try {
		assertTrue("Element Found",linkedList.remove(25)==25);
	} catch (Exception e) {
		
		e.printStackTrace();
	}
	
}
	
	@Test
	public void balancedBinaryTree(){
	
		BST b = new BST();
	    int a[]={2,1,3,5,6};
	
	    for(int i:a){
	    	b.add(i);
	    }
	    
	   b.inOrder();
	
	}
	
	@Test
	public void singleLinkedListSort()
	{
	Node linkedList = new Node(5);	
	Node last = linkedList;
		 Random randomGenerator = new Random();
		    for (int idx = 1; idx <= 5; ++idx){
		      int randomInt = randomGenerator.nextInt(100);
		      Node n = new Node(randomInt);
		      last.setNext(n);
		      last = last.getNext();
		      System.out.print("Generated : " + randomInt);
		      
		    }
		    
		    
		    LinkedListMergeSort mys= new LinkedListMergeSort(linkedList);
		    try {
				mys.sort();
				mys.display();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	@Test
	public void bstTree(){
		int[] a={1,6,4,3,67,89,80};
		BSTasAVL tree = new BSTasAVL();
		
		for(int el:a)
			tree.add(el);
		
		
		tree.inOrder();
		System.out.println();
		
		
	}
}
	
	
	


