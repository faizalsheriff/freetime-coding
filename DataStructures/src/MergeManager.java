import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;





public class MergeManager{

private Object[] origarr=null;
 

public Object[] merge(int[] orig){
int[] origarr=orig;	
int start=0;
int end = orig.length-1;
int mid=0;
boolean asc= false;
boolean dsc=false;
for(int index=0;index<=end;index++){
        //find sort direction
        if((index+1)<=end){//sanity check length>=2
        
        if(orig[index]<orig[index+1]){
        asc=true;
        }else{
        dsc=true;
        }
        
        if(asc&&dsc){
        	mid = index;
        	return arrayCopy(origarr, start, mid, end);
        
        }
        }
        
}
if(asc&&!dsc){
	System.out.println("Ascending");
}else if(dsc&&!dsc){
	System.out.println("Descending");
}

return this.origarr;


}


public Object[] arrayCopy(int[] origarr, int start, int mid, int end){




LinkedList<Integer> leftlist=new LinkedList<Integer>();
LinkedList<Integer> rightlist=new LinkedList<Integer>();
for(int index=0;index<=mid;index++){
	leftlist.add(origarr[index]);
}

for(int index=mid+1;index<=end;index++){
	rightlist.add(origarr[index]);
}

int rightElem=0;

ListIterator<Integer> iterate = rightlist.listIterator();
ListIterator<Integer> leftiterate = leftlist.listIterator();

while(iterate.hasNext()){
	rightElem =iterate.next(); 
    while(leftiterate.hasNext()){
    	if(rightElem<leftiterate.next()){
    		leftlist.add(leftiterate.previousIndex(), rightElem);
    		rightlist.remove(iterate.previousIndex());
    		leftiterate = leftlist.listIterator();
    		iterate = rightlist.listIterator();
    		break;
    	}
    }
}
leftlist.addAll(rightlist);

return leftlist.toArray();
}


}