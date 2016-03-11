package com.test.javathreads;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Latches {
	
	
	
	private  CyclicBarrier barrier = null;
			
			
			
			public void init(int n){
				barrier = new CyclicBarrier(n);
	}
			
			
	
			
   public void initiateWorkerThreads(int n){
	   
	   
	   ExecutorService exectors = Executors.newFixedThreadPool(n);
	   
	   for(int i =0; i < n; i ++)
	   {
		   final int name = i; 
		   exectors.submit(new Runnable(){
			   
			   
			  public void run()
			  {
				  System.out.println("Thread  "+name+"approaching barrier");
				  try {
					barrier.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  
				  System.out.println("Thread  "+name+"crossed barrier");
			  }
		   });
	   }
   }
	
	
   
  public static void main (String[] args)
  {
	  Latches l = new Latches();
	  l.init(3);
	  l.initiateWorkerThreads(3);
  }
	

}
