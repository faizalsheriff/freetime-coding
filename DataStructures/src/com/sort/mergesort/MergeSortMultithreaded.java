package com.sort.mergesort;



import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MergeSortMultithreaded {
	
	private static final Random RAND = new Random(42);   // random number generator
	private static ExecutorService pools = Executors.newCachedThreadPool();
	private static MergeSortMultithreaded m = new MergeSortMultithreaded();
	public static void main(String[] args) throws Throwable {
		int LENGTH = 6;   // initial length of array to sort
		int RUNS   =  16;   // how many times to grow by 2?

		//for (int i = 1; i <= RUNS; i++) {
			int[] a = createRandomArray(115);
			display(a);
			System.out.println("sorting");

			// run the algorithm and time how long it takes
			long startTime1 = System.currentTimeMillis();
			parallelMergeSort(a);
			long endTime1 = System.currentTimeMillis();
			
			/*if (!isSorted(a)) {
				throw new RuntimeException("not sorted afterward: " + Arrays.toString(a));
			}*/

			System.out.printf("%10d elements  =>  %6d ms \n", LENGTH, endTime1 - startTime1);
			LENGTH *= 2;   // double size of array for next time
		//}
	}
	
	private static void parallelMergeSort(int[] a) {
		if (a.length >= 2) {
			// split array in half
			int[] left  = Arrays.copyOfRange(a, 0, a.length / 2);
			int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);
			
			
			
			
			Callable<int[]> callableL = m.new MergeSorter(left);
			Callable<int[]> callableR = m.new MergeSorter(right);
			
			Future<int[]> futureL = pools.submit(callableL);
			Future<int[]> futureR = pools.submit(callableR);
			
			
			try {
				int[] sortedLeft = futureL.get();
				int[] soretdRight =  futureR.get();
				Callable<int[]> callableMerge = m.new Mergable(sortedLeft, soretdRight,a) ;
				Future<int[]> futureMerged = pools.submit(callableMerge);
				
				futureMerged=pools.submit(callableMerge);
				
				int[] soretdFinal =futureMerged.get();
				
				display(soretdFinal);
				pools.shutdown();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			
			
		}
		
	}
	
	private static void display(int[] sortedFinal) {
		for(int i: sortedFinal){
			System.out.println(i);
		}
		
	}

	// Arranges the elements of the given array into sorted order
		// using the "merge sort" algorithm, which splits the array in half,
		// recursively sorts the halves, then merges the sorted halves.
		// It is O(N log N) for all inputs.
	
	 class MergeSorter implements Callable<int[]>{
		private int[] a;
		MergeSorter(int[] ab){
			this.a = ab;
		}
	

		@Override
		public int[] call() throws Exception {
			if (a.length >= 2) {
				// split array in half
				int[] left  = Arrays.copyOfRange(a, 0, a.length / 2);
				int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);
				
				Callable<int[]> callableL = new MergeSorter(left);
				Callable<int[]> callableR = new MergeSorter(right);
				
				Future<int[]> futureL = pools.submit(callableL);
				Future<int[]> futureR = pools.submit(callableR);
				
				
				try {
					int[] sortedLeft = futureL.get();
					int[] soretdRight =  futureR.get();
					Callable<int[]> callableMerge = new Mergable(sortedLeft, soretdRight,a) ;
					Future<int[]> futureMerged = pools.submit(callableMerge);
					
					futureMerged=pools.submit(callableMerge);
					
					int[] soretdFinal =futureMerged.get();
					
					//display(soretdFinal);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			return a;
		}
		
	}
	
	
	 class Mergable implements Callable<int[]>{
		 
		 private int[] left;
		 private int[] right;
		 private int[] a;

		public Mergable(int[] sortedLeft, int[] sortedRight, int[] ar) {
			this.left=sortedLeft;
			this.right=sortedRight;
			this.a=ar;
		}

		@Override
		public int[] call() throws Exception {
			int i1 = 0;
			int i2 = 0;
			for (int i = 0; i < a.length; i++) {
				if (i2 >= right.length || (i1 < left.length && left[i1] < right[i2])) {
					a[i] = left[i1];
					i1++;
				} else {
					a[i] = right[i2];
					i2++;
				}
			};
			return a;
		}
	}
	
	
	

	// Creates an array of the given length, fills it with random
		// non-negative integers, and returns it.
		public static int[] createRandomArray(int length) {
			int[] a = new int[length];
			for (int i = 0; i < a.length; i++) {
				a[i] = RAND.nextInt(1000000);
				// a[i] = RAND.nextInt(40);
			}
			return a;
		}
		
		
		
		
		public static void reposition(int[] a){


			//pass the list
			for (int i =0; i< a.length; i++){

			    if(a[i]>=0){
			        continue;
			    }else{
			    //swap case
			            for(int j = i+1; j<a.length; j++){
			            if(a[j]<0){
			                //swap
			                int temp=a[i];
			                a[i] = a[j];
			                a[j] = temp;
			            break;
			            }
			            }

			        }


			}


			}
		
		
		/*ExecutorService pool = Executors.newFixedThreadPool(3);
		Set<Future<Integer>> set = new HashSet<Future<Integer>>();
		for (String word: argd) {
		Callable<Integer> callable = new WordLengthCallable(word);
		Future<Integer> future = pool.submit(callable);
		set.add(future);
		}
		int sum = 0;
		for (Future<Integer> future : set) {
		try {
			sum += future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		System.out.printf("The sum of lengths is %s%n", sum);
		System.exit(sum);*/

}
