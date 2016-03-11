import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WalmartCompareComparable {
	
	
	
	
	public static void main (String[] args){
		
		
		List<Coordinates> o = new ArrayList<Coordinates>();
		o.add(new Coordinates(0, 5));
		o.add(new Coordinates(2, 9));
		o.add(new Coordinates(8, 10));
		o.add(new Coordinates(6, 9));
	
		Collections.sort(o);
		
		for(Coordinates a: o) {
			System.out.println(a.getX()+"::"+a.getY());
		}
		
		System.out.println(o.get(0).getX()+"::"+o.get(0).getY());
	}

}



class MyObject implements Comparable <MyObject>
{
private int age;
private String name;
    
    public int compareTo(MyObject myobject){
    
     return this.age-myobject.age;
    
    }

}






class Coordinates implements Comparable<Coordinates>
{

private int x;
private int y;
private int freq;

    public Coordinates(int x1, int y1) 
    {
       this.x = x1;
       this.y = y1;
       
       if(x>y)
           freq = x-y;
       else
           freq = y-x;
    }    
    
    
    public int getX() {
		return x;
	}


	public void setX(int x) {
		this.x = x;
	}


	public int getY() {
		return y;
	}


	public void setY(int y) {
		this.y = y;
	}


	public int getFreq() {
		return freq;
	}


	public void setFreq(int freq) {
		this.freq = freq;
	}


	public int compareTo(Coordinates c)
    {
        
        return this.freq - c.freq;
        
        
        
    }
    
    
}








