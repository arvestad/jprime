package se.cbb.jprime.mcmc;

import java.util.Collection;
import java.util.HashSet;

/**
 * Base class for storing information on changes of state
 * parameters and other data structures. This enables e.g. optimised
 * updates of objects that depend on the perturbed object.
 * Info can be stored as a string and, optionally, for arrays, a list
 * of the affected indices.
 * 
 * @author Joel Sjöstrand.
 */
public class ChangeInfo {

	/** The object which the information refers to. */
	protected Dependent changed;
	
	/** String which can be used for discriminating between changes in simple cases. */
	protected String info;
	
	/** List of indices which array-based structures can use to detail affected elements. */
	protected int[] affectedElements;
	
	/**
	 * Constructor.
	 * @param changed the object which changed.
	 */
	public ChangeInfo(Dependent changed) {
		this(changed, "", null);
		this.info = null;
	}
	
	/**
	 * Constructor.
	 * @param changed the object which changed.
	 * @param info details or ID for the change. May be null.
	 */
	public ChangeInfo(Dependent changed, String info) {
		this(changed, info, null);
	}
	
	/**
	 * Constructor for array-based objects, where the indices of affected elements
	 * can be specified.
	 * @param changed the object which changed.
	 * @param info details or ID for the change. May be null.
	 * @param affectedElements the indices of affected elements. May be null.
	 */
	public ChangeInfo(Dependent changed, String info, int[] affectedElements) {
		this.changed = changed;
		this.info = info;
		this.affectedElements = affectedElements;
	}
	
	/**
	 * Constructor for array-based objects, where the indices of affected elements
	 * can be specified.
	 * @param changed the object which changed.
	 * @param affectedElements the indices of affected elements. May be null.
	 * @param info details or ID for the change. May be null.
	 */
	public ChangeInfo(Dependent changed, Collection<Integer> affectedElements, String info) {
		this.changed = changed;
		this.info = info;
		this.affectedElements = new int[affectedElements.size()];
		int i = 0;
		for (int x : affectedElements) {
			this.affectedElements[i++] = x;
		}
	}
	
	/**
	 * Helper. Returns the union of (possibly overlapping) arrays of indices. If any input array is
	 * null, null is returned.
	 * The returned elements have been "uniqified" however.
	 * @param elements lists of indices.
	 * @return the union of the lists.
	 */
	public static int[] getUnion(int[][] indices) {
		HashSet<Integer> unionhs = new HashSet<Integer>();
		for (int[] arr : indices) {
			if (arr == null) { return null; }
			for (int x : arr) { unionhs.add(x); }
		}
		int[] union = new int[unionhs.size()];
		int i = 0;
		for (int x : unionhs) { union[i++] = x; }
		return union;
	}
	
	/**
	 * Returns the object which the information refers to.
	 * @return the changed object.
	 */
	public Dependent getChanged() {
		return this.changed;
	}
	
	/**
	 * Returns a message or ID detailing the change.
	 * @return the info.
	 */
	public String getInfo() {
		return this.info;
	}
	
	/**
	 * For array-based objects which have changed, may return a list
	 * of affected elements. Typically, null should be interpreted as lacking
	 * information, in which case all elements can be considered affected.
	 * @return the affected elements; typically all if null.
	 */
	public int[] getAffectedElements() {
		return this.affectedElements;
	}
}
