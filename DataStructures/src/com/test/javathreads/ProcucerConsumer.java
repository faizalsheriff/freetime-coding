package com.test.javathreads;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcucerConsumer {
	private Queue<Integer> bQueue = new LinkedList<Integer>();
	private final int LIMIT = 20;
	private AtomicInteger counter = new AtomicInteger();
	class Producer{
		int i = 0;
		public void add(){
			if(bQueue.add(i)){
				System.out.println("added"+i);
			}
			i++;
			
		}
		
		
	}
	
	class Consumer{
		int i = 0;
		public void remove(){
			System.out.println(bQueue.poll());
			
			
		}
		
		
	}
	
	
	public static void main(String[] args){
		final ProcucerConsumer pc = new ProcucerConsumer();
		
		final Producer p = pc.new Producer();
		final Consumer c = pc.new Consumer();
		
		
		Thread t1 = new Thread(new Runnable(){
			public void run(){
				
				while(pc.counter.get()<pc.LIMIT){
					try {
						Thread.sleep(1350);
					} catch (InterruptedException e1) {
						
						e1.printStackTrace();
					}
					if(pc.bQueue.size() < 10){
					synchronized(pc.bQueue){
						p.add();
						pc.bQueue.notify();
					}
					}else{
						try{
							synchronized(pc.bQueue){
						
								pc.bQueue.wait();
							}
						}catch(InterruptedException e){
							
						}
					}
					
					pc.counter.getAndIncrement();
				}
				
				
				
			}
			
			
		},"Producer");
		
		
		Thread t2 = new Thread(new Runnable(){
			public void run(){
				
				while(pc.counter.get()< pc.LIMIT){
					
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(pc.bQueue.size() > 0){
						
					
					
						synchronized(pc.bQueue){
						c.remove();
						//pc.bQueue.notify();
						}
					}
					else{
						synchronized(pc.bQueue){
						//pc.bQueue.notify();
						try {
							pc.bQueue.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
					}
					
				
				}
				
				
			}
			
			
		},"Consumer");
		
		t1.start();
		t2.start();
		
		
	}
	

}
