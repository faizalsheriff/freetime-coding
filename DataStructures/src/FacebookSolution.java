import java.util.LinkedHashMap;




public class FacebookSolution {
	//Representing  	// X as -1 and Open as -2 and G as 0
	
	
LinkedHashMap<Integer, Integer> gmap = new LinkedHashMap<Integer, Integer>();
	
	
	public void findSecurity(int[][] a){
		
		
		
		if(a==null || a.length==0) // corner case handling
			return;
		
		
		//scan the map O(NM)
		
		for(int i = 0; i< a.length; i++){
			
			 
			
			for(int j = 0; j < a.length; j++){
				 if(a[i][j] == -2){
			  if(gmap.get(i)==null){
			  gmap.put(i, 1);
			  }
			  else{
				 int value =  (Integer) gmap.get(i);
				 value++;
				 gmap.put(i, value);
			  }
				 }
				 
			 }
			 
			 
			 
		}
		
		walkGuard(a);
		
	}
	
	

		
		public void walkGuard(int a[][] ){
		//walk guard until map with open  is empty
		if(!gmap.isEmpty()){
		
		for(int i = 0; i< a.length; i++){
			
				for(int j = 0; j<a.length; j++){
					
					//for each open door i.e -2;

					
						if(a[i][j] == -2){
							
							//up
							if((i-1)>=0){
								if(a[i-1][j] !=-2 && a[i-1][j]!=-1){
									
									if(a[i][j] < 0 || a[i][j] > (Integer)a[i-1][j] + 1){
									a[i][j] =(Integer)a[i-1][j] + 1;
									decreseMap(i);
									}
									
								}
						    }
							
							
							//left
							if((j-1)>=0){
								if(a[i][j-1] !=-2 && a[i][j-1]!=-1){
								   
									if(a[i][j] < 0  || a[i][j]> a[i][j-1]+1){
									a[i][j] =(Integer)a[i][j-1] + 1;
									decreseMap(i);
									}
								
								}
						    }
							
							//right
							if((j+1)<a.length){
								if(a[i][j+1] !=-2  && a[i][j+1]!=-1){
									if(a[i][j] < 0  || a[i][j]> a[i][j+1]+1){
									a[i][j] =(Integer)a[i][j+1] + 1;
									decreseMap(i);
								     }
									}
						    }
							
							//down
							if((i+1)<a.length){
								if(a[i+1][j] !=-2 && a[i+1][j]!= -1){
									
									if(a[i][j] < 0 || a[i][j] > (Integer)a[i+1][j] + 1){
									a[i][j] =(Integer)a[i+1][j] + 1;
									decreseMap(i);
									}
								}
						    }
							
					}
					
					
				//}
		
		
		
	   }
	}
		
	
	
	
	
	
		//call recursively
		walkGuard(a);
	}else{
		//print map
		
		for(int i = 0; i< a.length; i++){
			System.out.print("\n");
			for(int j =0; j < a.length; j++){
				System.out.print(a[i][j]);
			}
			System.out.println("\n");
		}
	}
		}
		
	



	private void decreseMap(int i) {
		
		if(gmap.get(i)!=null){
			int value = (Integer)gmap.get(i);
			value--;
			if(value>0)
			gmap.put(i, value);
			else
				gmap.remove(i);
		}
		
	}
	

	public static void main(String[] args) {
		
		long prev = System.currentTimeMillis();
		FacebookSolution sol = new FacebookSolution ();
		 int hyu[][]={{-2,-2,0, -1, -2},
		    		{-2, -1, 0, -1,-1},
		    		{-2, 0, -1, -1, -2},
		    		{-2, 0, -1, -1, -2},
		    		{-2, 0, -2, -2, -2}
		    };
		    		
		 sol.findSecurity(hyu)	;
		 System.out.println(((System.currentTimeMillis()-prev)/1000));

	}

}
