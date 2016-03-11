package com.datastructures.linear;

import java.util.ArrayList;
import java.util.List;

public class MyStack{

private List<Object> stack = new ArrayList<Object>();
private static final int INDEX_0 = 0;

public Object pop(){
try{
return (Object)stack.get(INDEX_0);
}finally{
stack.remove(INDEX_0);
}
}



public void push(Object newEle){
stack.add(newEle);
}


public Object peep(int seekPos) {
if(seekPos<stack.size())
return (Object)stack.get(seekPos);
else
return null;
}



public int size(){
	return stack.size();
}

}




