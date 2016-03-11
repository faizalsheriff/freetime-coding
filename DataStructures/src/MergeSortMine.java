import java.util.ArrayList;

public class MergeSortMine{

private int[] unsortedList;

MergeSortMine(int[] unsortList){
this.unsortedList = unsortList;
}


public int[] sort(){


return sort(unsortedList,0, unsortedList.length-1);

}

public int[] sort(int[] unsortedL, int start, int end){

int splitIndex = (end-start)/2;

if(splitIndex<2){
	
return merge(unsortedL[start], unsortedL[end]);
}

int[] left = sort(getSubArr(unsortedL,start,splitIndex), start,splitIndex);
int[] right = sort(getSubArr(unsortedL, splitIndex+1, end), splitIndex+1, end);
return merge(left,right);
}



private int[] getSubArr(int[] unsortedL, int start, int end) {
	int arrayLength = end-start+1;
	
	int[] subarr= new int[arrayLength];
	
	for(int i =0; start<=end;i++,start++){
		subarr[i]=unsortedL[start];
		
	}
	return subarr;
}


private int[] merge(int leftE, int rightE) {
	
	if(leftE<rightE){
		return new int[]{leftE,rightE};
	}else{
		return new int[]{rightE,leftE};
	}

}


public int[] merge(int[] left, int[] right){


ArrayList<Integer> tempArray = new ArrayList<Integer>();
try{
//intialize array
for(int i =0;i<left.length;i++)
{
tempArray.add(i);
}


for(int rightE: right){
    for(int leftE:tempArray){
    if(rightE>leftE){
    continue;
    }else{
    tempArray.add(rightE);
    }
    }

}
 return toPrimitiveInt(tempArray);

}finally{
tempArray = null;
}
}


private int[] toPrimitiveInt(ArrayList<Integer> tempArray) {
	
	int[] temp = new int[tempArray.size()];
	
	for(int index= 0; index<tempArray.size();index++){
		temp[index]= tempArray.get(index);
	
	}
	
	
	return temp;
}

}