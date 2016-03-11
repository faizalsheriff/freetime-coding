package com.test.javathreads;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
public class ProducerConsumerJava5 {
	private int counter = 0;
	private final int size = 20;
	
	private BlockingQueue<Integer> bQueue =  new LinkedBlockingQueue<Integer>(size);
	
	class Consumer{
		public void remove(){
			try {
				System.out.println(bQueue.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

	class Producer{
		public void add(int i){
			System.out.println("adding "+i);
			try {
				bQueue.put(i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		final ProducerConsumerJava5 pc = new ProducerConsumerJava5();
		final Producer p = pc.new Producer();
		final Consumer c = pc.new Consumer();
		
		Thread t1 = new Thread(new Runnable(){
			
			public void run(){
				
				while(pc.counter<pc.size){
					
					p.add(pc.counter);
					pc.counter++;
					
				}
				
			}
			
		}, "Producer");
		
		
		Thread t2 = new Thread(new Runnable(){
			
			public void run(){
				
				while(pc.counter<pc.size){
					
					c.remove();
					//pc.counter++;
					
				}
				
			}
			
		}, "Consumer");
		
		t1.start();
		t2.start();

	}

}
