package com.test.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class SetExploration {
	
	
	private Comparator myCompartaor;
	
	
	class MyHashSet<E> extends HashSet<Object>{
		
		private HashMap<E,E> bMap; 
		private int size;
		

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		MyHashSet(){
			bMap = new HashMap<E,E>(16);
			
		}
		
		
		MyHashSet(int size){
			this.size = size; 
			bMap = new HashMap<E,E>(size);
			
		}
		
		
		
		@Override
		public boolean add(Object a){
			
			if(bMap.size()== this.size()){
				grow();
			}
			 bMap.put((E)a, (E)a);
			return true;
			
		}
		
		
		public boolean retainAll(MyHashSet<E> a){
			Set<E> s = new HashSet<E>();
			
			Iterator<E> it = bMap.keySet().iterator();
			while(it.hasNext()){
				s.add(it.next());
			}
			
			 it = a.bMap.keySet().iterator();
			
			 
			 Set<E> s2 = new HashSet<E>();
				
			 while(it.hasNext()){
					s2.add(it.next());
				}
			 
			 return s.retainAll(s2);
		}


		private void grow() {
			 HashMap<E,E> bMap = new HashMap<E, E>(this.bMap.size()+this.size);
			 bMap.putAll(this.bMap);
			 this.bMap = bMap;
			 
			
		}
		
		
		
		
	}
	
	
	public void exploreHashSet(){
		
		 Set<Integer> mySet1 = new HashSet<Integer>();
		 Set<Integer> mySet2 = new HashSet<Integer>();
		 
		 for(int i =0; i< 10; i++){
		 mySet1.add(i);
		 }
		 
		 for(int i =8; i< 17; i++){
			 mySet2.add(i);
			 }
		 
		 
		 mySet1.retainAll(mySet2);
		 Iterator it = mySet1.iterator();
		 
		 while(it.hasNext()){
			 System.out.println(it.next());
		 }
		 
		 
		 
		
	}
	
	
	class Person{
		
		String name;
		String rollNo;
		String grade;
		
		
		Person(String rollno,  String nam, String grade){
			this.name = nam;
			this.rollNo = rollno;
			this.grade = grade;
			
		}
		
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Person)){
				return false;
			}
			
			return this.rollNo.equals(((Person)obj).rollNo);
		}
		
		@Override
		public int hashCode() {
			
			return this.rollNo.hashCode();
		}
		
	}
	
	public void exploreLinkedHashSet(){
		LinkedHashSet<Person> s = new LinkedHashSet<Person>();
		s.add(new Person("cs1401","arun","1st year"));
		s.add(new Person("cs1402","arun","1st year"));
		s.add(new Person("cs1403","arun","1st year"));
		s.add(new Person("cs1404","arun","1st year"));
		s.add(new Person("cs1405","arun","1st year"));
		s.add(new Person("cs1401","arun","1st year"));
		
		
		Iterator it = s.iterator();
		
		while(it.hasNext()){
			Person ref = (Person)it.next();
			System.out.println(ref.rollNo);
			
		}
		
	}
	
	
	public void exploreConcurrentHashMap(){
		
		 ConcurrentHashMap<Integer, Integer> cHM = new ConcurrentHashMap<Integer, Integer>();
		 
		 for(int i =0; i< 10; i++){
			 cHM.put(i,i);
			 }
		 
		 
		 Iterator<Integer> it = cHM.keySet().iterator();
		 int i =0;
		 while(it.hasNext()){
			i = it.next();
			
			if(i>6){
				it.remove();
				System.out.println(i+" is removed");
			}
		 }
		 
		
	}
	
	class CompareByRollNoAsc implements Comparator<Person>{

		@Override
		public int compare(Person o1, Person o2) {
			
			return o1.rollNo.compareTo(o2.rollNo);
		}
		
		
		
	}
	
	
	class CompareByName implements Comparator<Person>{

		@Override
		public int compare(Person o1, Person o2) {
			
			return o1.name.compareTo(o2.name);
		}
		
		
		
	}
	
	public void exploreTreeSet(){
		
		 TreeSet<Person> s = new TreeSet<Person>(new CompareByRollNoAsc());
		
			s.add(new Person("cs14011","arun","1st year"));
			s.add(new Person("cs14002","arun","1st year"));
			s.add(new Person("cs14013","arun","1st year"));
			s.add(new Person("cs14014","arun","1st year"));
			s.add(new Person("cs14005","arun","1st year"));
			s.add(new Person("cs14001","arun","1st year"));
		 
		
		 
		 
		 Iterator<Person> it = s.iterator();
		 Person i =null;
		 while(it.hasNext()){
			i = it.next();
			
			System.out.println(i.rollNo);
		 }
		 
		
	}
	
	
	public static void main(String[] args){
		SetExploration s = new SetExploration();
		s.exploreHashSet();
		
		s.exploreConcurrentHashMap();
		
		s.exploreLinkedHashSet();
		
		s.exploreTreeSet();
	
	}
	

}
