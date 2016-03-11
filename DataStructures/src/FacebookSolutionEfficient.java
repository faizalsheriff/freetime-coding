




public class FacebookSolutionEfficient {
	//Representing  	// X as -1 and Open as -2 and G as 0
	
	
//LinkedHashMap<Integer, Integer> gmap = new LinkedHashMap<Integer, Integer>(); sq
	
	
	public void findSecurity(int[][] a){
		
		
		
		if(a==null || a.length==0 || !isSqr(a)) // corner case handling
			return;
		
		
		//scan the map O(NM)
		
		for(int i = 0; i< a.length; i++){
			
			 
			
			for(int j = 0; j < a.length; j++){
				 if(a[i][j] == 0){
					 walkGuard(a, i, j);
				 }
				 
			 }
			 
			 
			 
		}
		
		
		//display
		
for(int i = 0; i< a.length; i++){
			
	System.out.print("\n");
			
			for(int j = 0; j < a.length; j++){
				
				System.out.print(a[i][j]+" ");
				 
			 }
			 
			
			System.out.print("\n");
			 
			 
		}

		
		
		
	}
	
	

		
		public void walkGuard(int a[][], int row, int col ){
		//walk guard and calculate move
		int up = row-1;
		int down = row+1;
		int left = col-1;
		int right = col+1;
		
		
							
							//up
							if(up>=0){
								if(a[up][col] ==-2){
									
									a[up][col] = a[row][col]+1;
									walkGuard(a, up, col);
									
								}else if(a[up][col]>0 &&  a[row][col]+1 < a[up][col]){
									
									a[up][col] = a[row][col]+1;
									walkGuard(a, up, col);
								}
						    }
							
							
							//left
							if(left>=0){
								if(a[row][left] ==-2){
									
									a[row][left] = a[row][col]+1;
									walkGuard(a, row, left);
									
								}else if(a[row][left]>0 &&  a[row][col]+1 < a[row][left]){
									
									a[row][left] = a[row][col]+1;
									walkGuard(a, row, left);
								}
						    }
							
							
							//right
							if(right<a.length){
									if(a[row][right] ==-2){
									
									a[row][right] = a[row][col]+1;
									walkGuard(a, row, right);
									
								}else if(a[row][right]>0 &&  a[row][col]+1 < a[row][right]){
									
									a[row][right] = a[row][col]+1;
									walkGuard(a, row, right);
								}
						    }
							
							//down
							if(down<a.length){
								if(a[down][col] == -2){
									
									a[down][col] = a[row][col]+1;
									walkGuard(a, down, col);
									
								}else if(a[down][col]>0 &&  a[row][col]+1 < a[down][col]){
									
									a[down][col] = a[row][col]+1;
									walkGuard(a, down, col);
								}
						    }
							
				
					
					
				
		
		
		
	   
	}
		
	
	
		boolean isSqr(int[][] sq) {
			
			
			if (sq.length > 0) {
			
				int prev = sq.length;
				int i = 0;
				for (; (i < sq.length && prev == sq[i].length); i++);
				
				if (i == sq.length) {
					return true;
				}
			}
			return false;
		}

	
	


	

	public static void main(String[] args) {
		FacebookSolutionEfficient sol = new FacebookSolutionEfficient ();
		 int hyu[][]={{-2,-2,0, -1, -2},
		    		{-2, -1, 0, -1,-1},
		    		{-2, 0, -1, -1, -2},
		    		{-2, 0, -1, -1, -2},
		    	    {-2, 0, -2, -2, -2}
		    		
		    };
		    		
		 sol.findSecurity(hyu)	;

	}

}
