package com.test.javathreads;

public class ThreadPractice {
	
	private MainObject mo;
	int i = 0;
	//Executor
	class MainObject {
		
		void print(String name){
			System.out.println(name+i);
			i++;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static void main(String[] args){
		ThreadPractice t = new ThreadPractice();
		
		final MainObject mo = t.new MainObject();
		Thread t1 = new Thread(new Runnable(){
			public void run(){
				for(int i =0; i<7; i++){
				synchronized(mo){
					mo.print(Thread.currentThread().getName());
					//mo.notifyAll();
					
				}
				//System.out.println("Out of synch block a");
				}
			}
		},"thread a"
		
		
		
		
				);
		
		Thread t2 = new Thread(new Runnable(){
			public void run(){
				for(int i =0; i<7; i++){
				synchronized(mo){
					mo.print(Thread.currentThread().getName());
				}
				//System.out.println("Out of synch block b");
				
				
				}
			}
		}, "thread b");
		
		t1.start();
		t2.start();
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

}
