package com.recursion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class Recursion {
	
	
	public static ArrayList<ArrayList<Integer>> getSubsets(ArrayList<Integer> set,
			 int index) {
			 ArrayList<ArrayList<Integer>> allsubsets;
			 if (set.size() == index) {
			 allsubsets = new ArrayList<ArrayList<Integer>>();
			 allsubsets.add(new ArrayList<Integer>()); // Empty set
			 } else {
			 allsubsets = getSubsets(set, index + 1);
			 int item = set.get(index);
			 ArrayList<ArrayList<Integer>> moresubsets =
						new ArrayList<ArrayList<Integer>>();
			 for (ArrayList<Integer> subset : allsubsets) {
				 ArrayList<Integer> newsubset = new ArrayList<Integer>();
				 newsubset.addAll(subset); //
						newsubset.add(item);
						moresubsets.add(newsubset);
			 }
			allsubsets.addAll(moresubsets);
			}
			 return allsubsets;
			 }
		
	
	
	//Print all subset of set 


	public static ArrayList<ArrayList<Integer>> subsets(ArrayList<Integer> arr){

	if(arr== null || arr.size()==0){
	return null; // Error Condition handling
	}

	return retSubsets(arr, 0);

	}



	public static ArrayList<ArrayList<Integer>> retSubsets(ArrayList<Integer> arr, int index){

	if(arr.size()==index){ //end
	ArrayList<ArrayList<Integer>> allsusb = new ArrayList<ArrayList<Integer>>();
	ArrayList<Integer> subs = new ArrayList<Integer>();
	//subs.add(""); // null set
	allsusb.add(subs);
	return allsusb;
	}else {
	ArrayList<ArrayList<Integer>> allsub = retSubsets(arr, index+1);
	
	ArrayList<ArrayList<Integer>> newSub = new ArrayList<ArrayList<Integer>>();
	
	for(ArrayList<Integer> indSub:allsub){
	ArrayList<Integer> newSubset = new ArrayList<Integer>();
	newSubset.addAll(indSub); //clone
	newSubset.add(arr.get(index)); // adding element indexed @ index to subs as an e
	
	newSub.add(newSubset);
	}
	allsub.addAll(newSub);
	
	return allsub;  
	}

	}



	//print all combination of a string


	public static ArrayList<String> subStrings(String s){

	if(s==null || s.trim().length() ==0)
	return null; //error case

	char strArr[] = s.toCharArray();

	return subString(strArr,0);

	}


	public static ArrayList<String> subString(char[] input, int index){

	if(index == input.length)
	{
	ArrayList<String> arr = new ArrayList<String>();
	arr.add("");
	return arr;
	}else{

	ArrayList<String> arr = subString(input, index+1);
	ArrayList<String> subwords = new ArrayList<String>();

	for(String word: arr){
		// to add cha[index] in all possible positions identified by i between chars of the //word 
	for(int i=0; i<=word.length();i++){
	subwords.add(addchar(word, i, input[index])); //constructing recursively the //subwords
	}
	}
	return subwords;
	}
	}

	
	
	
	public static String addchar(String in, int pos, char s){

		
		StringBuilder res = new StringBuilder();
		res.append(in.substring(0,pos));
		res.append(s);
		res.append(in.substring(pos));
		//System.out.println(res.toString());
		return res.toString();
		}

	
	

int columnForRow[] = new int [16]; 

boolean check(int row) {
for (int i = 0; i < row; i++) {
int diff = Math.abs(columnForRow[i] - columnForRow[row]); 
if (diff == 0 || diff == row - i) return false;
}
return true; 
}

void PlaceQueen(int row){
	if (row == 16) {
printBoard();
return; 
}
for(int i = 0; i < 16; i++)
{
	columnForRow[row]=i; 
	if(check(row)){
		PlaceQueen(row+1); 
}

}
} 

	
	
	private void printBoard() {
		for(int i = 0; i < 16; i++)
		{	
	System.out.print("  "+columnForRow[i]+" ");
		}
		System.out.println("\n........\n");
	}



	public static void main(String[] args){
		
		char[][] st = {{'w', 's', 'r', 't', 'g', 'g'}, 
		{'a', 'a', 'c', 'h', 'i', 'n', '|' },
		{'k', 'c',  'h', 'u', 'j',  'j' },
		{'o',  'h', 'i',  'n', 'y', 'q' }
		};

		
		Solution sol = new Solution();
		sol.findMatch(st);
		
		/*ArrayList<Integer> set=new ArrayList<Integer>();
		set.add(1);
		set.add(2);
		set.add(3);
	
		
		//ArrayList<ArrayList<Integer>> subset = getSubsets(set, 0);
		ArrayList<ArrayList<Integer>> subset = subsets(set);
		
		for(ArrayList<Integer> sub:subset){
			System.out.print("\n{");
			for(Integer ele:sub)
				System.out.print(ele);
			System.out.print("}\n");
		}
		
		
		String s ="abcd";
		
		ArrayList<String> perm = subStrings(s);
		Collections.sort(perm);
		for(String st:perm)

			System.out.println(st);*/
		
	//	Recursion r = new Recursion();
		
		//r.PlaceQueen(0);
	}
			

}



class Solution{
private HashMap<String, Integer> uq = new HashMap<String, Integer>();

public void findMatch(char[][] s){

/*if(!isSquare(s)){
return;
}*/

int len = s.length;

ArrayList<String> subS = new ArrayList<String>();
subS.add("");

walk(s, 0 ,0, len,subS);

display();

}



public void walk(char[][] s, int r, int c, int len, ArrayList<String> pr){

if(r < 0 || r>=len || c<0 | c>=len){
return;
}

ArrayList<String> newSub = new ArrayList<String>();


for(String sind:pr){
	//will use buffer here
	String prev = sind+String.valueOf(s[r][c]);
	newSub.add(prev);

addMap(prev);
}

newSub.add("");

walk(s, r+1, c, len, newSub);
walk(s, r, c+1, len, newSub);
walk(s, r+1, c+1, len, newSub);


}

private void addMap(String k){

if(!uq.containsKey(k))
uq.put(k,1);
else{
int c = uq.get(k);
c++;
uq.put(k,c);
}

}

public void display(){
Iterator<String> it = uq.keySet().iterator();


while(it.hasNext()){
String key = it.next();
System.out.println(key+"::"+uq.get(key));
}
}

}