package util;

import java.util.LinkedList;

//Warning: the code in question, although it apparently works, it 
//could backfire. There are additional methods that can add more 
//elements to the queue (such as addAll()) that ignore this size 
//check. For more details see Effective Java 2nd Edition - Item 16: Favor composition over inheritance
@SuppressWarnings("serial")
public class LimitedQueue<E> extends LinkedList<E> {
    private int limit;
    private boolean reverse;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }
    
    public void setReverse() {
    	reverse = true;
    }

    @Override
    public boolean add(E o) {
        if (!reverse) {
        	super.add(o);
        	while (size() > limit) { super.remove(); }
    	} else {
    		super.addFirst(o);
    		while (size() > limit) { super.removeLast(); }
    	}
    		
        return true;
    }
}