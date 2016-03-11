package com.datastructures.linear.linkedlist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.TreeMap;

public class AuxillaryExercises {
	
	public static void main(String args[]){
	
	LinkedList<Integer> l = new LinkedList<Integer>();
	
		l.add(3);
		l.addFirst(2);
		l.add(0,6);
		
		//System.out.println(l.get(0));
		
		List<ArrayList<Integer>> bList = new ArrayList<ArrayList<Integer>> ();
		
		List<Integer> i = new ArrayList<Integer> ();
		
		i.add(2);
		i.add(3);
		i.add(4);
		i.add(5);
		bList.add((ArrayList<Integer>) i);
		
		List<Integer> j  = new ArrayList<Integer> ();
		
		j.add(6);
		j.add(7);
		j.add(8);
		j.add(9);
		bList.add((ArrayList<Integer>) j);
		
		
		List<Integer> k  = new ArrayList<Integer> ();
		
		k.add(10);
		k.add(11);
		k.add(12);
		k.add(13);
		bList.add((ArrayList<Integer>) k);
		
		PriorityQueue<Integer> sortedL  = new AuxillaryExercises().mergeL(bList);
		 
		Integer ptr=null;
		while((ptr=sortedL.poll()) !=null){
			System.out.println(ptr);
		}
			
		int[] xyz = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,};
		AuxillaryExercises aux = new AuxillaryExercises();
		long beofre = System.currentTimeMillis();
		System.out.println(aux.intCountZero(xyz)+"in"+(System.currentTimeMillis()-beofre));
		
		 beofre = System.currentTimeMillis();
		System.out.println(aux.intCountZeroBinary(xyz)+"in"+(System.currentTimeMillis()-beofre));
		
		
		List<Item> lo = new ArrayList<Item>();
		lo.add(new Item("Shoe"));
		lo.add(new Item("Coat"));
		lo.add(new Item("Shoe"));
		lo.add(new Item("Boot"));
		
		lo.add(new Item("Shoe"));
		lo.add(new Item("Coat"));
		lo.add(new Item("Shoe"));
		lo.add(new Item("Shoe"));
		
		lo.add(new Item("Hanger"));
		lo.add(new Item("Coat"));
		lo.add(new Item("Shoe"));
		lo.add(new Item("Boot"));
		
		
		aux.countFrequency(lo);
		
		
	}
	
	
	
	public int intCountZero(int[] a){
		  int i =0;
		    for(; i< a.length; i++){
		          if(a[i]==0)
		           break;
		        
		    }
		    
		    return a.length-i;

		}
	
	
	public  int intCountZeroBinary(int[] a){
	return a.length-countBinary(a,0,a.length);
	
	}
	
	private int countBinary(int[] a, int l, int h){
		if(l>h)
			return -1;
		
		int mid = l+(h-l)/2;
		if(a[mid]==0 && a[mid+1] == 0 && a[mid-1]==1){
			return mid;
		}else if(a[mid] == 1){
			return countBinary(a,mid+1, h);
		}else{
			return countBinary(a,l, mid-1);
		}
	}
	
		
		public PriorityQueue<Integer> mergeL(List<ArrayList<Integer>> sortedL){

			PriorityQueue<Integer> mergedL = new PriorityQueue<Integer>(16, new Comparator<Integer>(){
				
				public int compare(Integer a, Integer b){
					return b-a;
				}
			}
			 ) ;

			        for(List<Integer> l:sortedL){
			             //merge(mergeL, l);    
			             for(Integer i:l){
			             mergedL.add(i);
			             }
			        }
			        
			        return mergedL;
			}
		
		
		public void countFrequency(List<Item> l){

			Map<String, Integer> m = new TreeMap<String, Integer>();

			if(l==null || l.size()==0 ){
			return;
			}

			int val=0;
			      for(Item p:l){
			       if(m.containsKey(p.getType())){
			       
			          val = m.get(p.getType());
			          val++;
			          
			          m.put(p.getType(), val);
			       }else{
			          m.put(p.getType(), 1);
			       }
			      
			      }
			      
			      
			      ArrayList<Integer> lmo = new ArrayList<Integer>(m.values());
			    
			      Collections.sort(lmo, new Comparator<Integer>(){
			    	  public int compare(Integer a, Integer b)
			    	  {
			    		  return b-a;
			    	  }
			      });
			      
			     
			      
			      Iterator<String> it = m.keySet().iterator();
			      
			      while(it.hasNext()){
			    	  String key = it.next();
			    	  System.out.println(key+"::"+m.get(it.next()));
			      }
			      
			      
			    
			}

}


class Item{
	
	private String type;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	Item(){
		
	}
	
	Item(String type){
		this.type = type;
	}
}