package com.tst.javaoverriding;

import java.io.FileNotFoundException;
import java.util.HashSet;

public class OverridingTester {
	static int i =5;
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
	
		/*	ClassB b = new ClassB();
			ClassC c = new ClassC();
			newPrint(b);
			newPrint(c);
*/
		
		 
			 System.out.println(i++);
			 System.out.println(i);
			 System.out.println(++i);
			 System.out.println(++i+i++);
			 
			 HashSet myMap = new HashSet();
			 String s1 = new String("das");
			 String s2 = new String("das");
			 
			 System.out.println(myMap);



	}
	
	
	public static void newPrint(ClassA a) throws FileNotFoundException{
		a.printValue();
		 }
	
	
	

}
