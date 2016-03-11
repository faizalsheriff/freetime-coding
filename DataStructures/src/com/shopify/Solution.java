package com.shopify;

import java.util.HashMap;

class Solution {
    public int solution(int[] A) {
        // write your code in Java SE 6
        
        if(A.length == 0){
        return -999; //invalid
        }
        
        //scan to jump forever
        boolean isInfinite = true;
        for(int i =0; i < A.length; i++){
            System.out.println(A[i]);
        
            if(i+A[i] > A.length ){
            isInfinite = false;
            }
        
        }
        
        int index = 0;
        int jumps=0;
        if(!isInfinite){
        
            while(index<A.length){
            
                index = index+A[index];
                System.out.println(index);
                jumps++;
            }
        return jumps;
        }else{
        return -1;
        }
        
        
    }
    
    
    
    public int solution1(int[] A) {
        // write your code in Java SE 6
        
        if(A.length==0)
            return 0;
        
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        
        for(int i = 0; i< A.length; i++){
            map.put(Math.abs(i), i);
            
        }
        
        return map.size();
        
    }
    
    
    boolean isTriplet = false;
    public int solution3(int[] A) {
        // write your code in Java SE 6
        
        for(int ak: A){
        System.out.print(ak+",");
        }
        
        if(A.length < 3  ){
        
        return 0;
        }
        int i =0;
        for(; i<A.length-2; i++){
        
        tripLet(i, i+1, i+2, A.length, A);
           
         }
        
        if(isTriplet){
        return 1;
        }else{
        return 0;
        }
        
    }
    
    
    
    private void tripLet(int p, int q, int r, int N,  int[] A){
    
        System.out.println(p+"::"+q+"::"+r);
        
        if(this.isTriplet){
        return;
        }
        
        if(p>=0 && p< q && q< r && r< N ){
            if((A[p] + A[q] > A[r]) && (A[q] + A[r] > A[p]) && (A[r] + A[p] > A[q])){
            this.isTriplet = true;
            }
        }else{
            
            
              
                tripLet(p, q+1, q+2, A.length, A);
            	tripLet(p, q, r+1, A.length, A);
              //  tripLet(p, q+1, r+1, A.length, A);
               
              
            	
            }
        
    
    }
    
    
    
}

