package com.facebookpractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.TreeSet;

import com.facebookpractice.Miscellaneous.Color;

public class Miscellaneous {

	private static final int Status_AVALABLE = 17;
	ArrayList ar = new ArrayList();
	

	public void numtoStr(int[] num) {

		
		System.out.println(numtoStr(num, 0));

	}

	public String numtoStr(int[] num, int index) {
		StringBuilder text = new StringBuilder();

		if (index == num.length) {
			return "";
		} else {

			text.append(numtoStr(num, index + 1));

			switch (index) {
			case 5:

				text.append(getOnes(num[index]));
				text.append(" hundred and ");
				break;

			case 4:
				text.append(getTwos(num[index]));
				text.append(" ");
				break;

			case 3:

				text.append(getOnes(num[index]));
				text.append(" thousand and ");

				break;

			case 2:
				text.append(getOnes(num[index]));
				text.append(" hundred and ");
				break;

			case 1:
				text.append(getTwos(num[index]));
				break;

			case 0:
				text.append(getOnes(num[index]));
				break;

			}

			return text.toString();
		}
	}

	
	
	
	
	
	public int wordMatcher(String in, String[] book){

		char[] ip = in.toCharArray();
		char[] op = null;
		boolean flag = true;
		int count = 0;

		for(String pt:book){
		op = pt.toCharArray();

		if(ip.length == op.length && ip[0] == op[0] && ip[ip.length-1]==op[op.length-1]){
		flag = true;
		 for(int i = 0; i < ip.length; i ++){
		  if(op[i]!=ip[i]){
		  flag = false;
		  }
		  
		 }
		 if(flag){
		 count++;
		 }
		}



		}
		return count;

		}
	
	
	public String getTwos(int n) {

		String text = "";
		switch (n) {
		case 1:
			text = "ten";
			break;

		case 2:
			text = "twenty";
			break;

		case 3:
			text = "thirty";
			break;

		case 4:
			text = "forty";
			break;

		case 5:
			text = "fifty";
			break;

		case 6:
			text = "sixty";
			break;

		case 7:
			text = "seventy";
			break;

		case 8:
			text = "eighty";
			break;

		case 9:
			text = "ninety";
			break;
		}
		return text;

	}

	public String getOnes(int n) {

		String text = "";
		switch (n) {
		case 1:
			text = "one";
			break;

		case 2:
			text = "two";
			break;

		case 3:
			text = "three";
			break;

		case 4:
			text = "four";
			break;

		case 5:
			text = "five";
			break;

		case 6:
			text = "six";
			break;

		case 7:
			text = "seven";
			break;

		case 8:
			text = "eight";
			break;

		case 9:
			text = "nine";
			break;
		}

		return text;

	}
	
	
	
	
	public Hits calculate(HashMap<String, Integer> sol, HashMap<String, Integer> gue){

		int hit=0;
		int phit = 0;
		Iterator it = sol.keySet().iterator();

		String c=""; //color variable
		while(it.hasNext()){
		c = (String)it.next();

		if(gue.get(c)!= null){
		    if(((Integer)gue.get(c)) == ((Integer)sol.get(c)) ){
		    hit++;
		    
		    }else{
		    phit++;
		    }
		}
		}

		Hits h = new Hits(hit, phit);
		return h;
		}

		class Hits{

		private int hit;
		private int phit;

		public Hits(int h, int p){
		this.hit = h;
		this.phit  = p;
		}

		 
		}
		
		


ArrayList<ArrayList<Integer>> co = new ArrayList<ArrayList<Integer>>();
public void subSes(int num){

int no = num -1;
int j = 0;
ArrayList<ArrayList<Integer>> superS = new ArrayList<ArrayList<Integer>>();
for(int i = no; i>0; i --){
j = num -i;
ArrayList<Integer> sub = new ArrayList<Integer>();
sub.add(j);
sub.add(i);
superS.add(sub); 
}


combinations(superS); // finds all combinations

//handling base add 11
/*ArrayList<Integer> base = new ArrayList<Integer>(); 
for(int b=1;b<= num; b++ )
{
base.add(1);
}

co.add(base);*/


//display
for(ArrayList<Integer> ele:co){
	System.out.print("\n{");
for(Integer elem:ele){
	System.out.print(elem+", ");
}

System.out.print("}\n");
}

}


public void combinations(ArrayList<ArrayList<Integer>> subs){

if(subs.size()==0){
return;
}

ArrayList<Integer> copyList = new ArrayList<Integer>();
copyList.addAll( subs.get(0));
co.add(copyList); //todo
int top=0;
int nxt=0;
ArrayList<Integer> ar = subs.get(0);

while(ar.get(1)>0){

top = ar.get(1);
nxt = top -1;
top = top - nxt;


ar.remove(1);
ar.add(1, nxt);
ar.add(2, top);

if(nxt > 0){
ArrayList<Integer> newSub = new ArrayList<Integer>();
newSub.addAll(ar);
co.add(newSub);




}



}

subs.remove(0);
combinations(subs);

}



/*{1, 1, 1, 1, 1, }

{2, 0, 1, 1, 1, }

{2, 2, 1, }

{2, 1, 1, 1, }

{3, 0, 1, 1, }

{3, 1, 1, }

{4, 0, 1, }

{1, 1, 1, 1, 1, }*/



public int substract(int a, int b){
	b= ~b | 1;
	System.out.println(Integer.toBinaryString(b)+":::"+b);
	 return sum(a, b);
}

public int sum(int a, int b){
	if(b==0)
		return a ;
	 int sum = a^b;
	 int carry =(a&b) << 1; 
	 return sum(sum, carry);
}


class Point implements Comparable<Point>{

float x;
float y;
double d;

public Point(float x, float y){
this.x = x;
this.y = y;
}

public int compareTo(Point dis){
if(this.d>dis.d)
return 1;
else if (this.d == dis.d){
return 0;
}else{
return -1;
}
}

}




public void findShortest(ArrayList<Point> p, int k){

if(p == null || k == 0){
return ;
}

for(Point r:p){ // n
r.d = (Math.sqrt((r.x*r.x)+(r.y*r.y)) );

}

Collections.sort(p); //nlogn

for(int i=0; i <k ; i++){
System.out.println(p.get(i).x+","+p.get(i).y);
}

}




public Point minimalCircle(ArrayList<Point> c){

float minx = 0;
float maxx = 0;
float miny = 0;
float maxy = 0;
	
	Point min = new Point (0, 0);
	Point max = new Point(0 , 0);

for(Point r : c){

if(r.x>maxx){
maxx=r.x;
max.x = r.x;
max.y = r.y;
}

if(r.x<minx){
minx = r.x;
min.x = r.x ;
min.y = r.y;
}


if(r.y>maxy){
maxy=r.y;
max.x = r.x;
max.y = r.y;
}

if(r.y<miny){
minx = r.y;
min.x = r.x ;
min.y = r.y;
}


}

//unsigned
float pminx = minx;
float pmaxx = maxx;
float pmaxy = maxy;
float pminy = miny;

if(pminx<0){
pminx = pminx*-1;
}

if(pmaxx<0){
pmaxx = pmaxx*-1;
}

if(pminy<0){
pminy = pminy*-1;
}

if(pmaxy<0){
pmaxy = pmaxy*-1;
}

//calculate distance

float radx = (pmaxx-pminx)/2;
float rady = (pmaxy-pminy)/2;

float originx = 0;
float originy = 0;
Point newC =null;
if(rady>radx){
originx = maxx-rady;
originy = maxy-rady;

newC = new Point(originx, originy);


double realRmax= ((max.x-originx) * (max.x-originx)) + ((max.y-originy) * (max.y-originy));
double realRmin= ((min.x-originx) * (min.x-originx)) + ((min.y-originy) * (min.y-originy));
//calculate radius
if(realRmax > realRmin){
	newC.d  =Math.ceil(Math.sqrt(realRmax));
}else{
	newC.d  = Math.ceil(Math.sqrt(realRmax));	
}

}else{

originx = maxx-radx;
originy = maxy-radx;

newC = new Point(originx, originy);
double realRmax= ((max.x-originx) * (max.x-originx)) + ((max.y-originy) * (max.y-originy));
double realRmin= ((min.x-originx) * (min.x-originx)) + ((min.y-originy) * (min.y-originy));
//calculate radius
if(realRmax > realRmin){
	newC.d  = Math.sqrt(realRmax);
}else{
	newC.d  = Math.sqrt(realRmax);	
}
}

return newC;

}


public boolean isOnorInCirc(ArrayList<Point> p, Point o){

System.out.println("Origin"+o.x+","+o.y);
if(o.x<0){
	o.x = o.x*-1;
	}



	if(o.y<0){
	o.y = o.y*-1;
	}
	

for(Point i: p){
	System.out.println("Processing .."+i.x+","+i.y);
	
	if(i.x<0){
		i.x = i.x*-1;
		}



		if(i.y<0){
		i.y = i.y*-1;
		}
		
		
	
System.out.println(((i.x - o.x) * (i.x - o.x) )  + ((i.y-o.y) *  (i.y-o.y) ));

if((((i.x - o.x) * (i.x - o.x) )  + ((i.y-o.y) *  (i.y-o.y) ))<= (o.d * o.d)){
continue;
}else{
	System.out.println("-ve"+i.x+","+i.y);
return false;
}
}
return true;
}


public String longSS(ArrayList<String> a){


TreeMap<String, String> lss = new TreeMap<String, String>();


//o(n)
for(String ele : a){

   for(int i = 0; i < ele.length(); i++){
    lss.put(String.valueOf(ele.charAt(i)), String.valueOf(ele.charAt(i))); 
   }
    
    
}


Iterator<String> it = lss.keySet().iterator();
StringBuilder temp = new StringBuilder();

while(it.hasNext()){
temp.append(it.next());
}

String lssS = temp.toString();



Collections.sort(a);


for(String r: a){
if(r.contains(lssS)){
return r;
}
}
return "";
} 




private Word max = new Word("",0);
private Word ndmax = new Word ("",0);



class Word {
	private String word;
	private int len;
	
	Word(String s, int len){
		this.word = s;
		this.len = len;
	}
	
}

class LenSorter implements Comparator<Word>{
	
	

	@Override
	public int compare(Word o1, Word o2) {
		
		return o2.len - o1.len;
	}
	
}

public void findLL(ArrayList<Word> d){
           if(d==null || d.size()==0)
           return;

           Collections.sort(d, new LenSorter());
           
	StringBuilder prev = new StringBuilder();
	/*for(Word s: d){
		if(!isMatch(prev.toString(),s.word)){
			Word w = new Word(s, s.word.length());
			add(w); //todo define array list
			
			if(prev.toString().length()==0)
				prev.append(s);
			
			else if(prev.toString().length()<s.length()){
			prev.delete(0, prev.toString().length());
			prev.append(s);		
			}
			
			
		}else{
			
		}*/
		
	}
	//System.out.println(max.word+"::"+ndmax.word);


public boolean isMatch(String s1, String s2){
	HashMap<String, Integer> m = new HashMap<String, Integer>();
	for(int i =0; i< s1.length(); i++){
		m.put(String.valueOf(s1.charAt(i)), 1);
	}
	
	for(int i =0; i< s2.length(); i++){
		if(m.get(String.valueOf(s2.charAt(i))) != null){
			return true;
		}
	}
	return false;
}
public void add(Word w){
	if(w.len>max.len){
		Word t = max;
		max = w;
		ndmax = t;
	}else if(w.len>ndmax.len){
		ndmax = w;
	}
	//uW.add(w);
}







int Status_AVAILABLE = 12;

public void moveItems(ArrayList<Integer> a, ArrayList<Integer> b) {
	int temp = 0;
	for (int i = 0; i < a.size(); i++) {
		temp = a.get(i);
		if (temp == Status_AVALABLE) {
			b.add(temp);
			a.remove(i);
		}
	}
	

  
}


public void addProduct(HashMap<String, Integer> prodMap, String newProd){
	
	if(prodMap == null || prodMap.size() == 0) // corner case
		return;
	
	
	if(newProd == null || newProd.trim().length() == 0){ // sanity check
		return;
	}
	
	int prodCount = 0;
	int COUNT_1 = 1; // to do declare in file dedicated for constants 
	if(prodMap.get(newProd)!=null){
		prodCount = prodMap.get(newProd);
		prodCount++;
		prodMap.put(newProd, prodCount); //o(1)
	}else{
		prodMap.put(newProd, COUNT_1);
	}
	
	
	

}




class Color {
	
	protected String color;
	
	
	// size as the key and all the items having the same size is the value
	private HashMap<Integer, ArrayList<Item>> itemsBySize = new HashMap<Integer, ArrayList<Item>>();

	public HashMap<Integer, ArrayList<Item>> getItemsBySize() {
		return itemsBySize;
	}

	public void setItemsBySize(HashMap<Integer, ArrayList<Item>> itemsBySize) {
		this.itemsBySize = itemsBySize;
	}
	
	
}


class Red extends Color{
	
	private static final String COLOR = "Red";
	
	public Red(){
		color = COLOR;
	}
	
	
	// Other methods specific for red class
	
	
	
	
}


class Green extends Color{
	
	private static final String COLOR = "Green";
	
	public Green(){
		color = COLOR;
	}
	
	// Other methods specific for Green class
	
	
	
	
	
}


class Blue extends Color{
	
	private static final String COLOR = "Blue";
	
	public Blue(){
		color = COLOR;
	}
	
	
	// Other methods specific for blue class
	
	
	
}


class Item{
	
	
	private String color;
	private int size;
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
}


class ColorContainer{
	
	// this list will have objects by color 
	
	private ArrayList<Color> coloredItems = new ArrayList<Color>();

	
	
	public ArrayList<Color> getColoredItems() {
		return coloredItems;
	}

	public void setColoredItems(ArrayList<Color> coloredItems) {
		this.coloredItems = coloredItems;
	}
	
	
}


int count2 = 0;

public void preProcess(int num) {

if(num == 0)
return;

//find largets divisor in terms of 10
int temp = num;
int d = 1;

while(temp >0){
temp = num / d;
d = d*10;
}

d = d/10;

findNum2(num, d);




}


public void findNum2(int num, int d){
	System.out.println(count2);

if(d > 10){
int quo =  (num/d);

int temp=(19*quo*(d/100));

if(quo>2){

temp = temp+d;

}else if(quo == 2){


temp  = temp + (num % d);

}
count2 = count2+temp;

findNum2(num % d, d/10);
}else if(d ==10)   {
 
 int quo = num/d;
 
int temp= quo;

if(quo>2){

temp = temp+d;

}else if(quo == 2){


temp  = temp + (num % d);

}
count2 = count2+temp;

findNum2(num % d, d/10);


 
}




}



HashMap<String, Integer> map = new HashMap<String, Integer>();
public void preProcessPage(String[] page){



for(int i = 0; i< page.length; i ++){

if(map.get(page[i])== null){

map.put(page[i], i);
}
}

}


public int findShortesDistance(String word1, String word2){

int val1 = map.get(word1);
int val2 = map.get(word2);

if(val2 > val1)
return (val2-val1-1); // no of words between 
else {
return (val1-val2-1);
}

}



public class lengthComparator implements Comparator<String>{

public int compare(String a, String b){

if(a.length() < b.length()){
return 1;

}else if(a.length() == b.length()){
return 0;
}else{

return -1;
}
}
}

public String longestW(ArrayList<String> words){


Collections.sort(words, new lengthComparator());

String s = "";

for(int i = 0; i<words.size(); i++){

s = words.get(i);

for(int j = i+1; j < words.size(); j++){
s = s.replace(words.get(j), "");
}

if(s.trim().length() == 0){
return words.get(i);
}

}


return "NOTHINGS";
}


public void transform(String w1, String w2, HashMap<String, String> d){


HashMap<Integer, String> transforWord = new HashMap<Integer, String>();

if(w1.length()!= w2.length()){
return; // corner case
}

char[] cw1 = w1.toCharArray();
char[] cw2 = w2.toCharArray();

char[] cur =new char[w1.length()]; //todo change string buider



for(int i=0; i<cw1.length ; i++){


cur = cw1.clone();
if(transforWord.get(i) == null){
cur[i]=cw2[i];




if(d.get(String.valueOf(cur, 0, cur.length))!=null){
	System.out.println("---"+String.valueOf(cur, 0, cur.length));
transforWord.put(i, String.valueOf(cur, 0, cur.length));
cw1 = cur;

if(cw1 == cw2){
break;
}
 i = 0;

}


}
}


Iterator it = transforWord.keySet().iterator();

while(it.hasNext()){
	
	System.out.println(transforWord.get(it.next()));

}

}

public void findLargeSub(int[] a){
int sum = 0;
ArrayList<Integer> subs = new ArrayList<Integer>();

for(int i =0; i < a.length; i++){

    for(int j =i; j < a.length; j++){
     sum = sum + a[j];
     subs.add(a[j]);
     
     if(sum == 0){
     display(subs);
     subs.clear();
     break;
     }
     
    }
    subs.clear();
    sum = 0;
}
}

public void display(ArrayList<Integer> a){
for (int e: a)
System.out.print(e);

System.out.println("|");
}




	public static void main(String[] args) {
		Miscellaneous m = new Miscellaneous();
		
		
		int[] a = { 9, 9, 9, 9};
		m.numtoStr(a);
		
		HashMap<String,Integer> map = new HashMap<String, Integer>();
		map.put("R", 1);
		map.put("B", 2);
		
		map.put("G", 3);
		map.put("Y", 4);
		
		HashMap<String,Integer> map1 = new HashMap<String, Integer>();
		map1.put("R", 1);
		map1.put("G", 2);
		
		map1.put("O", 3);
		map1.put("L", 4);
		
		
		
		Hits h = m.calculate(map, map1);
		
		System.out.println(h.hit+"::"+h.phit);
		
		
		String[] book = {"However", "reason", "while", "searching", "the", "current", "reason", "mailing" ,"list" ,"fo" ,"an" ,"unrelated", "reason" ,"curent"};
		
		System.out.println(m.wordMatcher("current", book));	
		
		m.subSes(4);
		
	 System.out.println(m.substract(6,5)); 
	 ArrayList<Point> points = new ArrayList<Point>();
	 
	 Point p = null;
			 
	 for(int i= 14; i >-14 ; i-- ){
		 p = m.new Point(i, i+2);
		
		points.add(p);
	 }
	 
	 m.findShortest(points, 3);
	 Point org = m.minimalCircle(points);	
	 System.out.println(org.x+"::"+org.y+"::"+org.d);
	 
	 System.out.println(m.isOnorInCirc(points,org));
	 
	 ArrayList<String> ls = new ArrayList<String>();
	 ls.add("abcdef");
	 ls.add("acdef");
	 ls.add("abcdefghijklmniopqrstuvy");
	 ls.add("pqrst");
	 ls.add("syuiopbred");
	 ls.add("abcdefghijklmnopqrstuvwxyz");
	 ls.add("abcklef");
	 ls.add("acmef");
	 ls.add("abcdefghijklmnopqqrst");
	 
	 
	 String temp = m.longSS(ls);
	 System.out.println("hey"+temp);
	 
	 ls = new ArrayList<String>();
	 ls.add("boy");
	 ls.add("girl");
	 ls.add("soy");
	 ls.add("mom");
	 ls.add("dad");
	 ls.add("aunty");
	 ls.add("monkey");
	 
	 
	 ArrayList<Integer> ab = new ArrayList<Integer>();
	 
	 ab.add(1);
	 ab.add(12);
	 ab.add(13);
	 ab.add(14);
	 ab.add(17);
	 ab.add(16);
	 ab.add(17);
	 ab.add(18);
	 ab.add(18);
	 
	 
	ArrayList<Integer> ba = new ArrayList<Integer>();
	 
	m.moveItems(ab, ba);
	
	 
	
	
	
	
	 
	 //m.findLL(ls);
	m.addProduct(map,"G");
	 
	
	
Iterator<String> it = map.keySet().iterator();
	
	while(it.hasNext()){
		System.out.print(map.get(it.next()));
	}
	
	m.preProcess(132);
	
	System.out.println(m.count2);
	
	String[] s= {"abc","efg", "hij", "klm", "mno", "iop", "uioy", "tyc", "tyc", "rxcv", "lmnop", "esty"};
    m.preProcessPage(s);
    int n = m.findShortesDistance("abc", "mno");
    System.out.println(n);
    
    
    ArrayList words = new ArrayList();
    words.add("Sackyui");
    words.add("Sack");
    words.add("Buickk");
    words.add("SackyuiBuickk");
    
    System.out.println(m.longestW(words));
    
    
    HashMap<String, String> dictionary = new HashMap<String, String>();
    dictionary.put("LAMP", "LAMP");
    dictionary.put("LIMP", "LIMP");
    dictionary.put("LIME", "LIME");
    dictionary.put("LIKE", "LIKE");
    
    m.transform("DAMP", "LIKE", dictionary);
    
    int[] bba={1, -2, 1, -3, 4, 1};
    m.findLargeSub(bba);
    
    int hyu[][]={{-2,-2,0, -1},
    		{-2, -1, 0, -1},
    		{-2, 0, -1, -1},
    		{-2, 0, -1, -1}
    };
    		
    m.findSecurity(hyu)	;
   
	 
	}
	
	LinkedHashMap gmap = new LinkedHashMap<Integer, Integer>();
	
	
	public void findSecurity(int[][] a){
		
		
		
		
		
		for(int i = 0; i< a.length; i++){
			//int openCount = 0;
			 
			
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
	
	
	// X as -1 and Open as -2 and G as 0
		
		public void walkGuard(int a[][] ){
		
		if(!gmap.isEmpty()){
		
		for(int i = 0; i< a.length; i++){
			
				for(int j = 0; j<a.length; j++){
					
					//if(a[i][j] !=-2 && a[i][j]!=-1){
					
						if(a[i][j] == -2){
							
							if((i-1)>=0){
								if(a[i-1][j] !=-2 && a[i-1][j]!=-1){
									a[i][j] =(Integer)a[i-1][j] + 1;
									decreseMap(i);
								}
						    }
							
							
							
							if((j-1)>=0){
								if(a[i][j-1] !=-2 && a[i][j-1]!=-1){
									a[i][j] =(Integer)a[i][j-1] + 1;
									decreseMap(i);
								}
						    }
							
							if((j+1)<a.length){
								if(a[i][j+1] !=-2  && a[i][j+1]!=-1){
									a[i][j] =(Integer)a[i][j+1] + 1;
									decreseMap(i);
								}
						    }
							
							if((i+1)<a.length){
								if(a[i+1][j] !=-2 && a[i+1][j]!= -1){
									a[i][j] =(Integer)a[i+1][j] + 1;
									decreseMap(i);
								}
						    }
							
					}
					
					
				//}
		
		
		
	   }
	}
		
	/*for(int i = 0; i< a.length; i++){
		System.out.print("\n");
			for(int j =0; j < a.length; j++){
				System.out.print(a[i][j]);
			}
		}*/
	
	
	
	
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
}
