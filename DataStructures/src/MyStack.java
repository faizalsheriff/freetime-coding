import java.security.InvalidKeyException;

public class MyStack {

	private Object[] stack;
	private int lastElementPointer = 0;

	public MyStack() {
	}

	public MyStack(int size) throws InvalidKeyException{

		if (size == 0) {
			throw new InvalidKeyException(); // need to handle it through a special
									// exception class
		}

		stack = new Object[size];
	}

	public void push(Object e) {
		if (lastElementPointer >= stack.length) {
			// create new array obj
			arrayCopy(stack);
			stack[lastElementPointer++] = e;

		} else {
			stack[lastElementPointer++] = e;
		}

	}

	public Object pop(){
        if(stack.length>0){
        // create new array obj
        Object poppedValue= stack[--lastElementPointer];
        --lastElementPointer;
        return poppedValue;
        }else{
       return -9999;
        }
        
}

	public Object peep(int index) {
		if (index < lastElementPointer) {
			// create new array obj
			return stack[index];

		} else {
			throw new ArrayIndexOutOfBoundsException();
		}

	}

	private void arrayCopy(Object[] src) {

		Object[] dst = new Object[src.length + 16];

		for (int inde = 0; inde < src.length; inde++) {
			dst[inde] = src[inde];
		}
		this.stack = dst;

	}

}