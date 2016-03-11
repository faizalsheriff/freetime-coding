import java.util.ArrayList;



public class Palindrome {

	
	private ArrayList<String> palindromPosib = new ArrayList<String>();
	public void findPalin(){

	String p = "abba";

	palindrome(p,0);
	for (String t : palindromPosib){

	System.out.println(t);

	}
	}

	public ArrayList<String> palindrome(String s, int index){

	if(index == s.length()){
	ArrayList<String> empty = new ArrayList<String>();
	empty.add("");
	return empty;
	}
	else{

	ArrayList<String> subs = palindrome(s, index+1);
	ArrayList<String> newSubs = new ArrayList<String>();

	
	//newSubs.addAll(subs);
	newSubs.add("");
StringBuilder st = new StringBuilder();

	for(String t: subs){
		st.append(t);
	    st.append(s.charAt(index));

	if(isPalindrome(st.toString())){
	palindromPosib.add(st.toString());
	}

	newSubs.add(st.toString());
	st.delete(0, st.length());

	}

	return newSubs;
	}
	}

	public boolean isPalindrome(String a){
	return a.equals(reverse(a));
	}


	public String reverse(String a){
		System.out.println("__"+a);
	char t[] = a.toCharArray();
	char r[] = new char[t.length];

	for (int i = t.length-1,j=0; i >= 0&&j<t.length; i --, j++){
	r[j]= t[i];

	}
	System.out.println("++"+String.copyValueOf(r));
	return String.copyValueOf(r);
	}
	//abaca


	public static void main(String[] args) {
		Palindrome p = new Palindrome();
		p.findPalin();

	}

}
