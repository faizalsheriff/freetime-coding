import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class Tester2 {

	public static void main(String[] args) {
		
		int a[] = {9,10,11,12,1,2,3,4,5};
		MergeManager merge=new MergeManager();
		merge.merge(a);
		
		
		int[] b={1,6,4,3,67};
		Mergesort mergeSort = new Mergesort();
		mergeSort.sort(b);
		
		
		int trenderIndex = findOrder(a);
		System.out.println(trenderIndex);
		
		
		fastestFeeds(a,3,2);
		
		int[] c={-1,3,4,5,-4,-6,8,9};
		c =reGroup(c);
		
		for(int ij:c)
			System.out.print(ij+",");
		

	}
	
	

public static int[] reGroup(int[] a){


int temp =0;
for(int i =0; i < a.length; i ++){
    if(a[i]>0 && (i+1) < a.length && a[i+1]<=0){
    
    
    //swap
    temp=a[i];
    a[i]=a[i+1];
    a[i+1]= temp;
    
    
    for(int j =i; j>=0 && j-1>=0 && a[j-1]>0; j--){
    
    //swap 
    temp=a[j];
    a[j]=a[j-1];
    a[j-1]= temp;
    
    
    }
    }
}

return a;
}

	
	public static int findOrder(int[] a){

		if(a.length<2){
		return -1; // no index found
		}

		int progMode=0; //-1 decreasing 1 increasing


		int prev=a[0];
		int cur = a[1];

		if(prev<cur){
		progMode=1;
		}else{
		progMode=-1;
		}

		for(int i =1; i < a.length; i ++){
		    cur=a[i];
		    if((prev<cur && progMode==1) || (prev>cur && progMode==-1)){
		            prev = cur;
		            continue;
		            }
		            else{
		            return i;
		            }
		     
		 


		}
		return -1;

		}
	
	
	
	public static void fastestFeeds(int[] a, int wSize, int k){

		Map<Integer, Integer> p = new TreeMap<Integer, Integer>();


		int start =0;
		int end =0;
		Iterator it=null;
		int counter = 0;
		//slide
		for(int i=0; i < a.length; i=i+wSize){
		start = i;
		end = end+wSize;
		System.out.println(start+"::"+end);
		    for(int j =start; j < end; j++){
		    p.put(a[j], a[j]);
		    }
		    
		    it = p.keySet().iterator();
		    System.out.println("Minimum window");
		    while(it.hasNext()){
		   
		    
		    
		    if(counter>=k){
		    p.clear();
		    break;
		    }
		    System.out.println(p.get(it.next()));
		    counter++;
		    }//eofwhile
		   
		    counter = 0;

		} 

		}

}
