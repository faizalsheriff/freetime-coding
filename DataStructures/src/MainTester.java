import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import com.datastructure.nonlinear.ArrToBST;
import com.datastructure.nonlinear.BSTasAVL;
import com.test.shortestpath.DijikstraShortestPathCalculator;
import com.test.shortestpath.Vertex;




public class MainTester {

	

	public static void main(String[] args) {
		
		/*int[] a={1,6,4,3,67,89,80,324,1,4,1,1,3,6,8,234,98,4,8,4454,88,9000,89,22};
		QuickSortInArray inArray = new QuickSortInArray(a);
		inArray.quicksort(0, a.length-1);
		display(inArray.getUnsortedList());*/
		int pos= 5;
		pos = pos>>5;
		System.out.println(pos);
		System.out.println(getMAX(10,5));
		
		 Random randomGenerator = new Random();
		 randomGenerator.setSeed(2);
		 int hashcode = "bcd".hashCode();
		 hashcode = hashcode % 4;
		 System.out.println("hashcode"+hashcode);
		 System.out.println("Random-->"+getRandom());
		 
		 
		 
		 
		 HashMap hm = new HashMap();
		 int a[] = {1,2,3,4,5,6,66,74,64,74,48};
		 int[] b = {7,8,9,9,12,3,4,5,6,7,8,9,10,11};
		 LinkedList l = new LinkedList();
		 
		 System.arraycopy(a, 6, b, 0, 3);
		 
		 System.out.println("cool stff"+(2+(2>>1)));
		 
		 ArrayList arb = new ArrayList();
		 
		 arb.add(1);
		 
		    for (int idx = 1; idx <= 10; ++idx){
		    	
		    
		      int randomInt = randomGenerator.nextInt(10);
		      //log("Generated : " + randomInt);
		      System.out.println(randomInt);
		    }
		    
		    //log("Done.");
		
		
		
		
		int[] ab={1,6,4,3,67,89,80};
		/*BinarySearchTree tree = new BinarySearchTree(a);
		tree.constructBST();
		tree.inOrder(tree.getBst());
		System.out.println();
		tree.postOrder(tree.getBst());
		System.out.println();
		tree.preOrder(tree.getBst());
		System.out.println();*/
		
		/*BSTasAVL bst = new BSTasAVL();
		for(int e:a ){
			bst.insert(e);
		}
		bst.inOrder(bst.getRoot());
		System.out.println(bst.isTreeBalancedCheck());
		bst.walkLeafHeight();*/
		/*int[] a1 = {0,1,2,3,4,5,6,7,8,9,89,97,890};
		ArrToBST arr = new ArrToBST(a1);
		arr.constructBST();
		com.datastructure.nonlinear.bst.Node root = arr.getRoot();
		arr.inOrder(root);*/
		
		
		//System.out.println(bc);
		
		
		/*Vertex vertexA = new Vertex("A");
		HashMap<String, Integer> adjacentVerticeMap = new HashMap<String, Integer>();
		adjacentVerticeMap.put("C", 2);
		adjacentVerticeMap.put("B", 6);
		adjacentVerticeMap.put("D", 5);
		vertexA.setAdjacentVerticeMap(adjacentVerticeMap);
		
		Vertex vertexC = new Vertex("C");
		HashMap<String, Integer> adjacentVerticeMapC = new HashMap<String, Integer>();
		adjacentVerticeMapC.put("D", 4);
		adjacentVerticeMapC.put("E", 4);
		vertexC.setAdjacentVerticeMap(adjacentVerticeMapC);
		
		Vertex vertexD = new Vertex("D");
		HashMap<String, Integer> adjacentVerticeMapD = new HashMap<String, Integer>();
	    adjacentVerticeMapD.put("E", 1);
		vertexD.setAdjacentVerticeMap(adjacentVerticeMapD);
		
		
		Vertex vertexE = new Vertex("E");
		HashMap<String, Integer> adjacentVerticeMapE = new HashMap<String, Integer>();
		
		adjacentVerticeMapE.put("A", 3);
		vertexE.setAdjacentVerticeMap(adjacentVerticeMapE);
		
		Vertex vertexB = new Vertex("B");
		HashMap<String, Integer> adjacentVerticeMapB = new HashMap<String, Integer>();
		
		adjacentVerticeMapB.put("A", 3);
		vertexB.setAdjacentVerticeMap(adjacentVerticeMapB);
		
		ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		vertexList.add(vertexA);
		vertexList.add(vertexC);
		vertexList.add(vertexD);
		
		vertexList.add(vertexE);
		vertexList.add(vertexB);
		
		DijikstraShortestPathCalculator st = new DijikstraShortestPathCalculator(vertexList);
		st.calculateShortestPaths();
		
		HashMap<String, Integer> result  =  st.getShortestDistanceMap();*/
		int[] arr = {-9, 3, 3, 4, 3, -7, -5, 1, -2, 5, 4, 2, -5, 4};
		kandane(arr);

	}

	private static void display(int[] unsortedList) {
		for (int i:unsortedList){
			System.out.print(i+",");
			
		}
		
	}
	
	
	
	public static void kandane(int[] arr)
	{
	  
	  int cummaltive = 0;
	  int max = 0;
	  int maxIndex = 0;
	  
	  //one pass
	  for(int i = 0; i< arr.length; i ++)
	  {
		  cummaltive = cummaltive+arr[i];
	       
	       if(cummaltive > max)
	       {
	            max = cummaltive;
	            maxIndex = i;
	       }
	  }
	  
	  //secomdpass loop till max pointer
	  
	  
	  for(int i = 0; i<=maxIndex; i ++)
	  {
	      System.out.println(arr[i]);
	  }
	  
	}
	public static int getMAX(int a, int b){
	
		int c = a-b;
		int k = (c>>31) & 0x1;
		int max = a-k*c;
		return max;
	}
	
	public static int getRandom(){
		int nbits = 4-1;
		int seed = 0;
		  long x = seed;
		  x ^= (x << 21);
		  x ^= (x >>> 35);
		  x ^= (x << 4);
		  seed = (int) x;
		  x &= ((1L << nbits) - 1);
		int ind = (int) x;
		  
		 //rem(ind);
		 return ind;
		}

}
