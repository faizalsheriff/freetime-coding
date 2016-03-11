import java.io.*;
import java.util.Scanner;
import java.util.Stack;;
public class CopyOfSolution {
    public static void main(String args[] ) throws Exception {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */
        
        Scanner scan = new Scanner(System.in);
        
        int n = Integer.parseInt(scan.nextLine());
         int i=0;
        int diff1=0;
        String str= (String)scan.nextLine();
        String b[] = str.split(" ");
        int[] a= new int[b.length];
        for(String s : b){
            a[i]= Integer.parseInt(s);
            i++;
        
        }
      
      
      
      
        
        if(n>=2 &&a.length>=2){
           diff1= a[1]-a[0];
           
            
        }
        
        
        int start = a[0];
        i=0;
        while(i<n){
        
            
            	if(a[i]!=start)
                break;
                
            	
            	start = start+diff1;
                i++;
                
            
        
        
            
            System.out.println(start);
        
    }
}
}