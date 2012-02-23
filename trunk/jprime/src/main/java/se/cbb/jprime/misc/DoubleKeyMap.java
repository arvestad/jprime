package se.cbb.jprime.misc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extends a linked hash map for long keys by allowing double
 * keys to be converted to long keys.
 * For double keys, key k' will return the value of
 * stored key k if abs(k'-k) &lt; eps, where eps is in the order of 5e-8.
 * The map is suitable for caching purposes, since the elements are kept
 * in access-order, and the map has a maximum size.
 * <p/>
 * Note: 1) Small double keys k such that abs(k) < eps may be disallowed
 * for safety reasons (if it is undesirable that small keys k and k' should
 * in fact both hash to the same element).
 * 
 * @author Joel Sjöstrand.
 *
 * @param <V> value type.
 */
public class DoubleKeyMap<V> extends LinkedHashMap<Long, V> {

	/** Eclipse-generated UID. */
	private static final long serialVersionUID = 2380005103918112618L;

	/** Precision for key value k in that actually used key k'=(long)(k*PRECISION). */
	public static final int PRECISION = 10000000;
	
	/** The maximum number of entries. */
	private int maxEntries;
	
	/** If to disallow very small keys. */
	private boolean disallowEpsKeys;
	
	@Override
    protected boolean removeEldestEntry(Map.Entry<Long,V> eldest) {
       return super.size() > this.maxEntries;
    }
	
	/**
	 * Constructor.
	 * @param initialCapacity initial capacity.
	 * @param maxEntries maximum number of simultaneous entries that are stored. The least recently
	 * accessed object is removed for this threshold not to be exceeded.
	 * @param disallowEpsKeys if true, will refuse to insert an element on <code>put(key,val)</code> if abs(key) is very small.
	 * No exception will be thrown, however.
	 */
	public DoubleKeyMap(int initialCapacity, int maxEntries, boolean disallowEpsKeys) {
		super(initialCapacity, 0.75f, true);
		this.maxEntries = maxEntries;
		this.disallowEpsKeys = disallowEpsKeys;
	}
	
	/**
	 * Inserts an element into the map using a double key.
	 * @param key the key.
	 * @param value the value.
	 * @return null if the key did not exist; the old value if the key did exist.
	 */
	public V put(double key, V value) {
		Long longKey = Math.round(key * PRECISION);  // Compute safe key.
		if (longKey == 0 && disallowEpsKeys) {
			// We simply don't insert it.
			return null;
		}
		return super.put(longKey, value);
	}
	
	/**
	 * Returns the value of a key and, if a value exists, marks it as recently used.
	 * @param key the key.
	 * @return the value; null if no such key exists.
	 */
	public V get(double key) {
		Long longKey = Math.round(key * PRECISION);  // Compute safe key.
		return super.get(longKey);
	}
	
	/**
	 * Returns true if the key is contained.
	 * @param key the key.
	 * @return true if contained; false if not contained.
	 */
	public boolean containsKey(double key) {
		Long longKey = Math.round(key * PRECISION);  // Compute safe key.
		return super.containsKey(longKey);
	}
	
	/**
	 * Removes an element.
	 * @param key the key.
	 * @return null if key was not found; the value if element was removed.
	 */
	public V remove(double key) {
		Long longKey = Math.round(key * PRECISION);  // Compute safe key.
		return super.remove(longKey);
	}
	
	/**
	 * Returns if small keys abs(k)&lt;eps are disallowed in the map.
	 * @return true if small keys disallowed; false if allowed.
	 */
	public boolean epsKeysAreDisallowed() {
		return this.disallowEpsKeys;
	}
	
	/**
	 * Sets whether small keys abs(k)&lt;eps should be disallowed in the map.
	 * @param areDisallowed true if small keys should be disallowed; false if allowed.
	 */
	public void setEpsKeysAreDisallowed(boolean areDisallowed) {
		this.disallowEpsKeys = areDisallowed;
	}
	
	/**
	 * Returns the maximum number of simultaneously stored elements.
	 * @return the max number.
	 */
	public int getMaxNoOfElements() {
		return this.maxEntries;
	}
	
	/**
	 * Sets the maximum number of simultaneously stored elements.
	 * @param maxEntries the max number.
	 */
	public void setMaxNoOfElements(int maxEntries) {
		this.maxEntries = maxEntries;
	}
	
}
