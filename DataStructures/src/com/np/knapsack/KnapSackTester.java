package com.np.knapsack;

import java.util.ArrayList;

public class KnapSackTester {

	public static void main(String[] args) {
		
		
	 ArrayList<Element> a = new ArrayList<Element>();
	 
	 for(int i=1; i<4;i++){
		
		 a.add( new Element(i, i+1));
	 }
	 
	 KnapSack k = new KnapSack(5, a);
	 k.knapSackCalculator();
		 
	}

}
