package com.test.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Comparator;

public class MapExploration {
	
	class Person{
		int id;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		String firstName;
		String lastName;
		public String firstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String lastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		
		
	}
	
	
	class Marks{
		int q1;
		int q2;
		int q3;
		int q4;
		int q5;
		int q6;
		int avg;
		
		public int getAvg() {
			return ((q1+q2+q3+q4+q5+q6)/6);
		}
		
		public int getQ1() {
			return q1;
		}
		public void setQ1(int q1) {
			this.q1 = q1;
		}
		public int getQ2() {
			return q2;
		}
		public void setQ2(int q2) {
			this.q2 = q2;
		}
		public int getQ3() {
			return q3;
		}
		public void setQ3(int q3) {
			this.q3 = q3;
		}
		public int getQ4() {
			return q4;
		}
		public void setQ4(int q4) {
			this.q4 = q4;
		}
		public int getQ5() {
			return q5;
		}
		public void setQ5(int q5) {
			this.q5 = q5;
		}
		public int getQ6() {
			return q6;
		}
		public void setQ6(int q6) {
			this.q6 = q6;
		}
		
		
		
	}
	
	class Users{
		String name;
		Set<String> intrestList;
		
		Users(String name, Set<String> intrest){
			this.name = name;
			this.intrestList = intrest;
		}
	}
	
	private Map<Person, Marks> mySort = new TreeMap<Person, Marks>(new Comparator<Person>(){

		public int compare(Person a, Person b){
		return (a.firstName()+","+a.lastName()).compareTo((b.firstName()+","+b.lastName()));

		}
		});
	
	
	
	public HashMap<String, List<Users>> bucketify(List<Users> l){
		
		Iterator<Users> it = l.iterator();
		Users userRef = null;
		
		HashMap<String, List<Users>> bucket = new HashMap<String, List<Users>>();
		while(it.hasNext()){
			userRef = it.next();
			
			if(userRef.intrestList == null || userRef.intrestList.size() == 0)
				continue;
			
			
			List<Users> userList = null;
			for(String intrest: userRef.intrestList){
				if(bucket.containsKey(intrest)){
					userList = bucket.get(intrest);
				
					userList.add(clone(userRef));
				}else{
					
					ArrayList<Users> newUserL = new ArrayList<Users>();
					newUserL.add(clone(userRef));
					bucket.put(intrest, newUserL);
				}
					
				
			}
			
		}
		return bucket;
		
	}
	
	private Users clone(Users userRef) {
		
		String name = userRef.name;
		 Set<String> userlist = new HashSet<String>();
		 userlist.addAll(userRef.intrestList);
		 Users u = new Users(name, userlist);
		 
		return u;
	}

	public  void main(String args[]){
		MapExploration tester = new MapExploration();
		
		List<Users> userList = new ArrayList<Users>();
		
		
		Set<String> intrestSet = new HashSet<String>();
		intrestSet.add("Photography");
		intrestSet.add("Animation");
		intrestSet.add("Poetry");
		
		Users u = tester.new Users("Sushil Xj", intrestSet);
		userList.add(u);
		
		
	
		intrestSet = new HashSet<String>();
		intrestSet.add("Sociology");
		intrestSet.add("Animation");
		intrestSet.add("Games");
		
		u = tester.new Users("Derik Beldwin", intrestSet);
		userList.add(u);
		
		
		
		intrestSet = new HashSet<String>();
		intrestSet.add("Coding");
		intrestSet.add("Animation");
		intrestSet.add("Painting");
		
		u = tester.new Users("Doko Mojo", intrestSet);
		userList.add(u);
		
		
		
		intrestSet = new HashSet<String>();
		intrestSet.add("Biology");
		intrestSet.add("Animation");
		intrestSet.add("Coding");
		
		u = tester.new Users("Tang Jang", intrestSet);
		userList.add(u);
		
		HashMap<String, List<Users>> x = tester.bucketify(userList);
		
		Iterator<String> i = x.keySet().iterator();
		
		List<Users> pointer = null;
		String intrestName=null;
		while(i.hasNext()){
			intrestName = i.next();
			pointer = x.get(intrestName);
			System.out.println("====Printing users of intrest ===="+intrestName);
			for(Users name :pointer){
				System.out.println(name.name);
				
			}
			
			System.out.println("++++++++++++++++++++++++++++++++++++++++++\n");
			
		}
		
		
		
		
	}
	
	

}
