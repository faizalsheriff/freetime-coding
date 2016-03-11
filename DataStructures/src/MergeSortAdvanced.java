import java.util.ArrayList;
import java.util.Arrays;

public class MergeSortAdvanced{

private int[] unsortedList;

MergeSortAdvanced(int[] unsortList){
this.unsortedList = unsortList;
}


public int[] sort(){


return sort(unsortedList);

}

public int[] sort(int[] unsortedL){
	int start=0;
	int end=unsortedL.length;

int splitIndex = (end-start/2)+1;

if(splitIndex<=2){
	
return merge(unsortedL[start], unsortedL[end]);
}


return merge(sort(Arrays.copyOfRange(unsortedL, start,splitIndex)),sort(Arrays.copyOfRange(unsortedL, splitIndex+1, end)));
}



private int[] getSubArr(int[] unsortedL, int start, int splitIndex) {
	int[] subarr= new int[splitIndex-start+1];
	
	for(int i =0; start<=splitIndex;i++){
		subarr[i]=unsortedL[start];
		start++;
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