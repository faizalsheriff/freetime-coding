package com.test.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ListExplorartion {
	
	
	
	public List<?> merge2(List<?> src, int index, List<?> list2){
		
		if(src==null || list2==null){
			return null;
		}
		
		
		if(src instanceof LinkedList){
			
			if(index<=src.size()){
			src.addAll(index, (Collection)list2);
			return src;
			}else{
				return null;
			}
			
		}else{
			
			LinkedList<Object> n = new LinkedList<Object>();
			/*if(index<src.size()){
				int counter = 0;
				add(src,n, counter,index);
			
				n.addAll(list2);
				add(src,n,index,src.size());
				
				
			}else{*/
			
				n.addAll(src);
				n.addAll(index, list2);
				
			//}
			
			return n;
		}
		
		
		
	}
	
	
	
	public void removeDuplicates(List<Integer> l){
		
		
		Collections.sort(l);
		
		int cur = Integer.MAX_VALUE;
		int prev = 0;
		for(int i=0; i< l.size(); i++){
			
			cur = l.get(i);
			if(cur == prev){
				prev = l.get(i);
				l.remove(i);
			}else {
				prev = l.get(i);
			}
			
		
		}
		
		
		//display
		for (Integer i: l){
			System.out.println("Wal test"+i);
		}
		
		
		
	}
	
	
	public void removeDuplicateApp2(List<Integer> l){
		Set<Integer> s = new HashSet<Integer>();
		
		s.addAll(l);
		
		//display
		
		
		for(Integer i:s){
			System.out.println("Wal test"+i);
		}
		
		
		
	}
	
	
	

	
	public void removeTest(LinkedList<String> l){
	
		if(l==null){
			return;
		}else{
			
			l.offer("ABC");
			l.offerFirst("DOC");
			l.offerLast("DDLK");
			
			l.addFirst("Deku");
			l.addLast("beku");
			
		    Collections.sort(l);
		    
		    
		    Iterator it = l.descendingIterator();
		    
		    while(it.hasNext()){
		    	System.out.println(it.next());
		    }
			
			
			
			l.remove();
			l.removeFirst();
			l.removeFirstOccurrence((Object)"ABC");
			l.removeLast();
			
			
			l.poll();
			l.pollFirst();
			l.pollLast();
			
				
			
			
			
			
		}
	}
	
	public void removet(ArrayList<String> l){
		
		if(l==null){
			return;
		}else{
			
			
			
		    Collections.sort(l);
		    
		    
		    Iterator<String> it = l.listIterator();
		    
		    while(it.hasNext()){
		    	System.out.println(it.next());
		    }
			
			
			
		
		
			
				
			
			
			
			
		}
	}
	

	
	
	public void addBorder(LinkedList l, int n){
		l.add(n,13);
		
		for(Object o: l){
			System.out.println((Integer)o);
		}
		
	}
	
	
public List<?> merge(List<?> src, int index, List<?> list2){
		
		if(src==null || list2==null){
			return null;
		}
		
		
		if(src instanceof LinkedList){
			
			if(index<=src.size()){
			src.addAll(index, (Collection)list2);
			return src;
			}else{
				return null;
			}
			
		}else{
			
			LinkedList<Object> n = new LinkedList<Object>();
			if(index<src.size()){
				int counter = 0;
				add(src,n, counter,index);
			
				n.addAll(list2);
				add(src,n,index,src.size());
				
				
			}else{
				n.addAll(src);
				n.addAll(list2);
			}
			
			return n;
		}
		
		
  }


	private void add(List<?> src, LinkedList<Object> dst, int start, int end ) {
		while(start<end){
		dst.add((Object)src.get(start));
		start++;
		}
	}
	
	
	public static void main(String[] args){
		ListExplorartion e = new ListExplorartion();
		
		
		
		
		LinkedList l1 = new LinkedList();
		
		l1.add("lokoo");
		l1.add("dokoo");
		l1.add("jokoo");
		l1.add("mokoo");
		l1.add("nokoo");
		l1.add("pokoo");
		
		e.removeTest(l1);
		
		
		
		LinkedList l = new LinkedList();
		l.add(0);
		l.add(1);
		l.add(2);
		l.add(6);
		l.add(6);
		l.add(5);
		
		
		e.removeDuplicates(l);
		
		
		l.add(6);
		l.add(6);
		l.add(5);
		
		
		e.removeDuplicateApp2(l);
		
		//e.removeTest(l);
		
		l.add(0);
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);
		l.add(5);
		
		
		e.addBorder(l, 6);
		
		ArrayList<Integer> s = new ArrayList<Integer>();
		
		
		for(int i=0;i<150000;i++){
			s.add(i);
		}
		
		
		ArrayList<Integer> s2 = new ArrayList<Integer>();
		
		for(int i=150000;i<1150000;i++){
			s2.add(i);
		}
		
		
		long time1 = System.currentTimeMillis();
		
		List result = e.merge(s,103,s2);
		
		System.out.println((System.currentTimeMillis() - time1));
		
		
time1 = System.currentTimeMillis();
		
		 result = e.merge2(s,1033,s2);
		
		System.out.println((System.currentTimeMillis() - time1));
		
		
	/*	for(Object o: result){
			System.out.println((Integer)o);
		}
	*/	
		
	}
	

}
