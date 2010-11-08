package se.cbb.jprime.misc;

/**
 * Regular queue implementation for doubles (avoids
 * wrapper requirement of e.g. LinkedList&lt;Double&gt;).
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleQueue {
	
	/**
	 * Single-link list node wrapper.
	 */
	private class QueueNode {
		double val;
		QueueNode next;
		
		public QueueNode(double val) {
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
	public DoubleQueue() {
	}
	
	/**
	 * Constructor. Creates a queue filled with the doubles
	 * of an array, first element first-in-line.
	 * @param vals doubles to fill queue with.
	 */
	public DoubleQueue(double[] vals) {
		if (vals != null) {
			for (double d : vals) {
				this.put(d);
			}
		}
	}
	
	/**
	 * Appends an double.
	 * @param d element to queue.
	 */
	public void put(double d) {
		QueueNode n = new QueueNode(d);
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
	public double get() {
		double d = this.first.val;
		this.first = this.first.next;
		if (this.first == null) {
			this.last = null;
		}
		return d;
	}
	
	/**
	 * Retrieves the first element without dequeueing.
	 * If the queue is empty, returns Double.NaN
	 * (for which one checks by Double.isNaN(val)).
	 * @return the first element.
	 */
	public double peek() {
		if (this.isEmpty())
			return Double.NaN;
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
