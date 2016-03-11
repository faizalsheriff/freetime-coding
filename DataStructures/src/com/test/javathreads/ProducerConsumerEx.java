package com.test.javathreads;

import java.util.LinkedList;
import java.util.Queue;


public class ProducerConsumerEx{

private Queue<Integer> q = new LinkedList<Integer>();



class Producer {

public void produce(int elem){
 
 q.add(elem);

} 
}


class Consumer{

public int consumeTop(){
 return q.poll();
}
}


public static void main(String[] args){
final ProducerConsumerEx px = new ProducerConsumerEx();

final Consumer c = px.new Consumer();
final Producer p = px.new Producer();

Thread t1 = new Thread(
new Runnable(){
public void run(){
	
	while(true){
synchronized(px.q){
if(!px.q.isEmpty()){
System.out.println(c.consumeTop());



try{
Thread.sleep(1400);
}catch(InterruptedException e){
}
px.q.notify();
}else{
try {
	px.q.wait();
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}


}
}
}
},"Consumer");


Thread t2 = new Thread(
new Runnable(){
	
	public void run(){
int i =1;
while(true){
synchronized(px.q){
if(px.q.isEmpty()){
	p.produce(i);
System.out.println("Produced");
i++;



try{
Thread.sleep(1400);
}catch(InterruptedException e){
}
px.q.notify();
}else {

try {
	px.q.wait();
} catch (InterruptedException e) {
	
	e.printStackTrace();
}
}

}
}
}
},"Producer");

t1.start();
t2.start();

}




}
