package se.cbb.jprime.misc;

import java.text.StringCharacterIterator;

/**
 * Regular queue implementation for chars (avoids
 * wrapper requirement of e.g. LinkedList&lt;Character&gt;).
 * Also, there is a convenience character returned by peek() even if the queue is empty.
 * 
 * @author Joel Sjöstrand.
 */
public class CharQueue {

	/**
	 * Shorthand for '\\uFFFF', i.e. the "no char" value.
	 * Used to to indicate null references when e.g. peeking empty queue. 
	 */ 
	public static final char NULL = StringCharacterIterator.DONE;
	
	/**
	 * Single-link list node.
	 */
	private class QueueNode {
		char val;
		QueueNode next;
		
		public QueueNode(char val) {
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
	public CharQueue() {
	}
	
	/**
	 * Constructor. Creates a queue filled with the chars
	 * of a string, first character first-in-line.
	 * @param str characters to fill queue with.
	 */
	public CharQueue(String str) {
		if (str != null) {
			for (int i = 0; i < str.length(); ++i) {
				this.put(str.charAt(i));
			}
		}
	}
	
	/**
	 * Appends a character.
	 * @param c element to queue.
	 */
	public void put(char c) {
		QueueNode n = new QueueNode(c);
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
	public char get() {
		char c = this.first.val;
		this.first = this.first.next;
		if (this.first == null) {
			this.last = null;
		}
		return c;
	}
	
	/**
	 * Retrieves the first element without dequeueing.
	 * If the queue is empty, returns CharQueue.NULL.
	 * @return the first element.
	 */
	public char peek() {
		if (this.isEmpty())
			return StringCharacterIterator.DONE;
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

	/**
	 * Adds all chars of a string.
	 * @param str the string.
	 */
	public void put(String str) {
		for (int i = 0; i < str.length(); ++i) {
			this.put(str.charAt(i));
		}
	}
}
