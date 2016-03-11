import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FindPalin {

	public static void main(String[] args) {

		/*
		 * char[] a ={'4','5','9','3','9','5','4'}; System.out.println(new
		 * FindPalin().isPalindrom(a));
		 * 
		 * HashMap m = new HashMap();
		 */
		
		System.out.println("cool" + isPalindrome(748847));
		
		
		setTest();
	}

	/*
	 * public boolean findSimplePalin(int a) {
	 * 
	 * }
	 */
	
	public static void setTest()
	{
		List mynode = new ArrayList();
		
		mynode.add("node1");
		mynode.add("node2");
		mynode.add("node4");
		mynode.add("node4");
		mynode.add(4);
		mynode.add(5);
		mynode.add(4);
		
		Set myset = new HashSet();
		
		myset.addAll((Collection)mynode);
		for(Object o: myset)
		{
			System.out.println(o);
		}
		
		
		
		
		
		
	}
	
	public static boolean isPalindrome(int n) {

		int divL = 1;

		// find maximum divisor
		while ((n / divL) > 0) {
			divL = divL * 10;
		}
		divL = divL / 10;

		int valL = 0;
		int valR = 0;
		// find palin
		while (n > 1) {
			valL = n / divL;
			valR = n % 10;

			// System.out.println(valL + "::" + valR);
			if (valL != valR) {
				return false;
			}

			// strip left
			n = n - (valL * divL);
			n = n - valR;

			divL = divL / 100;

			// System.out.println("after left  strip" + n);
			// strip right
			// n = n - ((n/divL)*divL);
			n = n / 10;

			// System.out.println(n);
			// The value stripped after left is equivalent to palindrome

		}

		return true;
	}

	public boolean isPalindrom(char[] palin) {

		if (palin.length < 2)
			return true;

		int left = 0;
		int right = palin.length - 1;

		while (right > left) {
			if (palin[left] != palin[right])
				return false;

			right--;
			left++;
		}

		return true;

	}

	
	// graph traversal o(n)
	public static HashMap<Node, List<Node>> cloneMap(LinkedList<Node> queue) {

		// have the nodes in a queue
		// iterate the queue add to the map, <node, list<adjacent nodes>>
		// if nodes are already present do a union of adjacent nodes
		HashMap<Node, List<Node>> map = new HashMap<Node, List<Node>>();

		for (Node n : queue) {

			if (!map.containsKey(n)) {
				map.put(n, n.getAdjacentNode());
			} else {

				Set<Node> adjacentNode = (Set<Node>) map.get(n);

				for (Node adj : adjacentNode) {
					adjacentNode.add(adj);
				}
			}
		}
		
		return map;

	}

	public boolean findPalin(int a) {
		// find max 10th decimal
		if (a < 11)
			return false;

		int divL = 10;
		// one pass to find max 10
		while ((a / divL) > 0) {
			divL = divL * 10;
		}
		divL = divL / 10;

		int divR = 10;

		int l = 0;
		int r = 0;

		// 787
		// second pass to find palin
		while (a > 0 && divL > 0) {

			l = a / divL;
			r = a % divR;
			System.out.println(l + "::" + r);
			if (l == r) {
				a = a - (l * divL);
				a = a - (r * divR);
				// a = a /divR;
				divL = divL / 100;

				continue;

			} else {
				return false;

			}
		}

		return true;

	}

	
	
	public void printCombinations(int[] input){ // 2,3
		
		List<List<String>> list = new ArrayList<ArrayList<String>>(); //
		
		for(int i : input)
		{
			if(i = 2){
				list.add(); // ABC
			}else {
				list.     // DEF
			}
		}
		
		
	//	List<List> l = list.get(0);
		
		int i =0;
		
		List<List<String>> out = new ArrayList<ArrayLis<String>>();
		for(List l: list){
			for(String s: l){
				out.add(e)
			}
			out.add(c)
			for(List l : List)
			{
				
				out.add(l.get(i)); //AD AE AF BD BE BE CD CE CF
				//display(out);
				//System.out.println(c, l.get(i));
				i++;
			}
		}
		//display out
		display(out);
	}
	
	
	
	
	
	public void getAllComibination(int[] a){
		
		List<String> input = new ArrayList<String>();
		
		for(int p : a){
			if(p ==1){
				input.add("ABC");
			}
			else
			{
				input.add("DEF");
			}
		}
		
		int index = 0;
		
		ArrayList<String> emptySet = new ArrayList<String>();
		emptySet.add("");
		emptySet.add("");
		emptySet.add("");
		
		//ArrayList<String> resultSet = new ArrayList<String>();
		while(index < input.size()){
			
			emptySet = (ArrayList<String>)recursivelyAdd(input.get(index), emptySet);
			index++;
		}
		
	}
	
	
	public List<String> recursivelyAdd(String src, List<String> dest){
		
		List<String> out = new ArrayList<String>();
 		for(int i=0; i< src.length(); i++){
 			String s = String.valueOf(src.charAt(i));
			for(String d: dest){
				s = s+d;
				out.add(s);
			}
		}
	return out;
	}
	
	//abc  def   abc 
	//ad ae af  bd be bf
	//ada aea afa
	
	public void display(List<String> c )
	{
		System.out.println();
	}
	
	
}
