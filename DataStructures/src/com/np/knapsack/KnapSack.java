package com.np.knapsack;

import java.util.ArrayList;

public class KnapSack{

private int MAX = 0;

private ArrayList<Element> list = new ArrayList<Element>();

KnapSack(int max, ArrayList<Element> list){
this.MAX = max;
this.list.add(new Element(0,0));
this.list.addAll(list);
}

public void knapSackCalculator(){


int size = list.size();


int [][] mat = new int[size][size];
boolean [][] sol = new boolean [size][size];
int option1 = mat[0][0];
int option2 = Integer.MIN_VALUE;
for(int o=1;o<=size-1;o++){
	for(int i =1; i<=size-1; i++){
option1 = mat[o-1][i];

if(list.get(o).weight<=i){
option2 = list.get(o).value+ mat[o-1][i-list.get(0).weight];
}
mat[o][i] = Math.max(option2, option1); //max value
sol[o][i] = option2>option1?true:false;
}
}
//bottom up
for(int o =size-1, i = size-1, w= MAX; o>0 && w >0; o--){
	if(sol[o][i]){
		System.out.println (list.get(o).weight+"::"+list.get(o).value);
		w = w- list.get(o).weight;
            }
}

}

}



class Element{
public Element(int i, int j) {
		this.weight = i;
		this.value= j;
	}
int weight;
            int value;

}


