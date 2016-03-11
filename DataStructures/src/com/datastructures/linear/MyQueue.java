package com.datastructures.linear;

import java.util.ArrayList;
import java.util.List;


public class MyQueue{


private List queue = new ArrayList();

public void add(int element){
queue.add(element);
}

public int remove(){
return (Integer) queue.remove(0);
}


public int size(){
return queue.size();
}


public int peek(int index) throws ArrayIndexOutOfBoundsException{
if(index<queue.size()){
return (Integer)queue.get(index);
}else{
throw new ArrayIndexOutOfBoundsException("look up index is higher than the queue size, try with lower index value");
}

}

}
