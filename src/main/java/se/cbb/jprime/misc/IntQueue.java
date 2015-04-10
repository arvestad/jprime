package se.cbb.jprime.misc;

/**
 * Regular queue implementation for integers (avoids
 * wrapper requirement of e.g. LinkedList&lt;Integer&gt;).
 * 
 * @author Joel Sjöstrand.
 */
public class IntQueue {

	/** Shorthand integer which may be used to denote null/empty. */
	public static final int NULL = Integer.MIN_VALUE;
	
	/**
	 * Single-link list node wrapper.
	 */
	private class QueueNode {
		int val;
		QueueNode next;
		
		public QueueNode(int val) {
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
	public IntQueue() {
	}
	
	/**
	 * Constructor. Creates a queue filled with the ints
	 * of an array, first element first-in-line.
	 * @param vals integers to fill queue with.
	 */
	public IntQueue(int[] vals) {
		if (vals != null) {
			for (int i : vals) {
				this.put(i);
			}
		}
	}
	
	/**
	 * Appends an integer.
	 * @param i element to queue.
	 */
	public void put(int i) {
		QueueNode n = new QueueNode(i);
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
	public int get() {
		int i = this.first.val;
		this.first = this.first.next;
		if (this.first == null) {
			this.last = null;
		}
		return i;
	}
	
	/**
	 * Retrieves the first element without dequeueing.
	 * If the queue is empty, returns IntQueue.NULL.
	 * @return the first element.
	 */
	public int peek() {
		if (this.isEmpty())
			return IntQueue.NULL;
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
