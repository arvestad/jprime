package se.cbb.jprime.misc;

/**
 * Regular queue implementation for chars (avoids
 * wrapper requirement of e.g. LinkedList&lt;Boolean&gt;).
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanQueue {
	
	/**
	 * Single-link list node.
	 */
	private class QueueNode {
		boolean val;
		QueueNode next;
		
		public QueueNode(boolean val) {
			this.val = val;
			this.next = null;
		}
	}
	
	/** Reference to first-in-line. */
	private QueueNode first = null;
	
	/** Reference to last-in-line. */
	private QueueNode last = null;
	
	/**
	 * Constructor. Creates an empty queue.
	 */
	public BooleanQueue() {
	}
	
	/**
	 * Constructor. Creates a queue filled with the booleans
	 * of an array, first element first-in-line.
	 * @param vals booleans to fill queue with.
	 */
	public BooleanQueue(boolean[] vals) {
		if (vals != null) {
			for (boolean b : vals) {
				this.put(b);
			}
		}
	}
	
	/**
	 * Appends a boolean.
	 * @param b element to queue.
	 */
	public void put(boolean b) {
		QueueNode n = new QueueNode(b);
		if (this.isEmpty()) {
			this.first = n;
		} else {
			this.last.next = n;
		}
		this.last = n;
	}
	
	/**
	 * Retrieves and dequeues the first element.
	 * If empty, will generate a NullPointerException.
	 * @return the first element, now removed.
	 */
	public boolean get() {
		boolean b = this.first.val;
		this.first = this.first.next;
		if (this.first == null) {
			this.last = null;
		}
		return b;
	}
	
	/**
	 * Retrieves the first element without dequeueing.
	 * If the queue is empty, returns false.
	 * @return the first element.
	 */
	public boolean peek() {
		if (this.isEmpty())
			return false;
		return this.first.val;
	}
	
	/**
	 * Returns true if queue is empty.
	 * @return true if empty.
	 */
	public boolean isEmpty() {
		return (this.first == null);
	}
	
	/**
	 * Counts the number of elements. Linear time, since no internal count held.
	 * @return the number of elements.
	 */
	public int getSize() {
		QueueNode n = this.first;
		int count = 0;
		while (n != null) {
			n = n.next;
			++count;
		}
		return count;
	}
}
