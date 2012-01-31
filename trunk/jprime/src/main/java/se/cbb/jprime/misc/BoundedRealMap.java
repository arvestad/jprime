package se.cbb.jprime.misc;

import java.util.HashMap;

/**
 * Represents a map where values of some type K are mapped using real-values, i.e.,
 * with &lt;double,K&gt; key-value pairs.
 * Since the keys are represented by doubles, key k' will return the value of
 * stored key k if abs(k'-k) &lt; eps.
 * The cache also implements a "garbage collection" by holding a maximum number of values,
 * where the most recently accessed ones are kept.
 * Most operations such as put, get and contains should be constant in time.
 * <p/>
 * TODO: Make this implement <code>Map&lt;K,V&gt;</code>, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class BoundedRealMap<K> {
	
	/**
	 * Wrapper. Holds a key-value pair along with references
	 * to previous and next element w.r.t. access.
	 */
	class Holder {
		Long key;
		K value;
		Holder nextOlder;
		Holder nextNewer;
		
		Holder(Long key, K value, Holder nextOlder) {
			this.key = key;
			this.value = value;
			this.nextOlder = nextOlder;
			this.nextNewer = null;
		}
		
	}
	
	/** Precision for key value k in that actually used key k'=(long)(k*PRECISION). */
	public static final int PRECISION = 1000000;
	
	/** Maximum no. of values. */
	private int maxSz;
	
	/** The values sorted by key. */
	private HashMap<Long, Holder> values;

	/** Oldest element. */
	private Holder oldest;
	
	/** Newest element. */
	private Holder newest;
	
	/**
	 * Constructor.
	 * @param maxNoOfElements maximum number of elements in map. Oldest element w.r.t. access is
	 * removed when adding an element that would cause this size to be exceeded.
	 */
	public BoundedRealMap(int maxNoOfElements) {
		if (maxNoOfElements <= 0) {
			throw new IllegalArgumentException("Cannot create RealMap with zero capacity.");
		}
		this.maxSz = maxNoOfElements;
		this.values = new HashMap<Long, Holder>(maxSz);
		this.oldest = null;
		this.newest = null;
	}
	
	/**
	 * Inserts an element into the map. If the max size is reached, the oldest element
	 * w.r.t. access will be removed.
	 * @param key the key.
	 * @param value the value.
	 * @return null if the key did not exist; the old value if the key did exist.
	 */
	public K put(double key, K value) {
		Long intKey = Math.round(key * PRECISION);  // Compute safe key.
		Holder h = this.values.get(intKey);
		if (h == null) {
			if (this.values.size() >= this.maxSz) {
				// Remove oldest element.
				this.oldest = this.oldest.nextOlder;
				if (this.oldest != null) {
					this.oldest.nextOlder = null;
				}
			}
			// Insert new element.
			h = new Holder(intKey, value, this.newest);
			if (this.oldest == null) {
				this.oldest = h;
				this.newest = h;
			} else {
				this.newest.nextNewer = h;
				this.newest = h;
			}
			this.values.put(intKey, h);
			return null;
		} else {
			// Change intrinsic value of wrapper.
			K oldVal = h.value;
			h.value = value;
			this.moveToEndOfList(h);
			return oldVal;
		}
	}
	
	/**
	 * Returns the value of a key and, if a value exists, marks it as recently used.
	 * @param key the key.
	 * @return the value; null if no such key exists.
	 */
	public K get(double key) {
		Long intKey = Math.round(key * PRECISION);  // Compute safe key.
		Holder h = this.values.get(intKey);
		if (h != null) {
			this.moveToEndOfList(h);
			return h.value;
		}
		return null;
	}
	
	/**
	 * Moves an existing element to the end of the linked-list (i.e., it is newest).
	 * @param h element.
	 */
	private void moveToEndOfList(Holder h) {
		if (h.nextOlder != null) {
			h.nextOlder.nextNewer = h.nextNewer;
		} else if (h.nextNewer != null) {
			this.oldest = h.nextNewer;
		}
		if (h.nextNewer != null) {
			h.nextNewer.nextOlder = h.nextOlder;
		}
		if (h != this.newest) {
			h.nextOlder = this.newest;
		}
		h.nextNewer = null;
		this.newest = h;
	}
	
	/**
	 * Returns true if the map is empty.
	 * @return true if empty; false if non-empty.
	 */
	public boolean isEmpty() {
		return this.values.isEmpty();
	}
	
	/**
	 * Returns true if the key is contained.
	 * @param key the key.
	 * @return true if contained; false if not contained.
	 */
	public boolean containsKey(double key) {
		Long intKey = Math.round(key * PRECISION);  // Compute safe key.
		return this.values.containsKey(intKey);
	}
	
	/**
	 * Returns true if the value is contained. NOTE: Linear time performance.
	 * @param value the value.
	 * @return true if contained; false if not contained.
	 */
	public boolean containsValue(K value) {
		for (Holder h : this.values.values()) {
			if (h.value.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes an element.
	 * @param key the key.
	 * @return null if key was not found; the value if element was removed.
	 */
	public K remove(double key) {
		Long intKey = Math.round(key * PRECISION);  // Compute safe key.
		Holder h = this.values.remove(intKey);
		if (h != null) {
			if (h.nextOlder != null) {
				h.nextOlder.nextNewer = h.nextNewer;
			}
			if (h.nextNewer != null) {
				h.nextNewer.nextOlder = h.nextOlder;
			}
			if (h == this.oldest) {
				this.oldest = h.nextNewer;
			}
			if (h == this.newest) {
				this.newest = h.nextOlder;
			}
			return h.value;
		}
		return null;
	}
	
	/**
	 * Returns the size of the map.
	 * @return the size.
	 */
	public int size() {
		return this.values.size();
	}
	
	/**
	 * Clears the map.
	 */
	public void clear() {
		this.values.clear();
		this.oldest = null;
		this.newest = null;
	}

}
