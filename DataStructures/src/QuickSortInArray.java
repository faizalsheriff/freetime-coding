
public class QuickSortInArray {
	
	
	private int leftCounter;
	private int rightCounter;
	private int index_0;
	private int index_last;
	private int unsortedList[];
	
	QuickSortInArray(int[] array)
	{
		this.unsortedList=array;
	}
	
	public int[] getUnsortedList() {
		return unsortedList;
	}

	public void setUnsortedList(int[] unsortedList) {
		this.unsortedList = unsortedList;
	}

	public void quicksort( int index0, int indexLast){
		
		//calculate pivot index
		int pivotElement = unsortedList[index0+(indexLast-index0)/2];
		leftCounter= index0;
		rightCounter = indexLast;
		
		while(leftCounter <= rightCounter){
			
			
			while(unsortedList[leftCounter]<pivotElement){
				leftCounter++;
				
			}
			
			
			while(unsortedList[rightCounter]>pivotElement){
				rightCounter--;
				
			}
			
			
			if(leftCounter<=rightCounter){
				exchangeData(unsortedList,leftCounter, rightCounter);
				leftCounter++;
				rightCounter--;
				
			}
		}
		
		if(index0<rightCounter){
			quicksort(index0,rightCounter);

			
		}
		
		if(leftCounter<indexLast){
			quicksort(leftCounter,indexLast);
		}
		
		
		
	}
	


	private void exchangeData(int[] unsortedList, int leftIndex,
			int rightindex) {
		
		int temp=unsortedList[leftIndex];
		unsortedList[leftIndex] = 	unsortedList[rightindex] ;
		unsortedList[rightindex] = temp;
		}























	
	
	
	
	
	

}
