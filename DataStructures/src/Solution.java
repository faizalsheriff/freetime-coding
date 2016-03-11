import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;;
public class Solution {
	
	class Test{
		
		int n;
		int k;
		
		ArrayList<Integer> al = new ArrayList<Integer>();
		
	}
    public static void main(String args[] ) throws Exception {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */
    	ArrayList<Test> tes = new ArrayList<Test>();
        Solution sol = new Solution();
        Scanner scan = new Scanner(System.in);
        int n = Integer.parseInt(scan.nextLine());
        int i = 0;
     
        try{
        
        while(i<n){
        	
        	Test t1 = sol.new Test();
        	String s = scan.nextLine();
        	
        	String[] t = s.split(" ");
        	
        	
        	if(t.length==2){
        	t1.n = Integer.parseInt(t[0]);
        	t1.k = Integer.parseInt(t[1]);
        	}else {
        		throw new Exception("Input is not in desired format");
        	}
        	
        	
        	
        	String s2 = scan.nextLine();
        	String[] num = s2.split(" ");
        	int counter = 0;
        	
        	while(counter < num.length){
        		t1.al.add(Integer.parseInt(num[counter]));
        		counter++;
        	}
        	
        	//System.out.println(t1.n+":"+t1.k+"::"+t1.al.size());
        	tes.add(t1);
        	
        	i++;
        	
        	
        }
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        
        for(Test tc:tes){
        	
        	if(tc.k < tc.n){
        		
        	calculateMoves(tc);
        	}else{
        		System.out.println(0);
        	}
        	
        }
        
        scan.close();
      
        }
	private static void calculateMoves(Test tc) {
		int move = 0;
		int i = 0;
		
		while (tc.k<tc.n){
			
			if ((i +1)< tc.al.size()){
				
				move = move+ tc.al.get(i+1) - tc.al.get(i);
				tc.n--;
				i++;
				tc.al.remove(i);
				
			}else{
				i = 0;
			}
			
			
			
			
		}
		System.out.println(move);
		
	}
}
      
      
      
      
        
       
