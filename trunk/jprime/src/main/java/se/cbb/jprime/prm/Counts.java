package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.Iterator;

import se.cbb.jprime.misc.MultiArray;

/**
 * Represents a multidimensional array of counts
 * for a set of dependencies connecting a single child attribute and
 * its parents.
 * 
 * @author Joel Sjöstrand.
 */
public class Counts implements MultiArray {

	/** Maximum number of elements allowed in multidimensional array. */
	public static final int MAX_CARDINALITY = 1000000;
	
	/** The dependency entities which to count. */
	private final Dependency[] dependencies;
	
	/** Lengths in each dimension of array, child first. */
	private final int[] lengths;
	
	/** Length factors for computing array index. Contents: 1,|Y|,|Y|*|Z|,... */
	private final int[] lengthFactors;
	
	/** Counts. */
	private final int[] counts;
	
	/**
	 * Constructor.
	 * @param dependencies the dependencies and their entities.
	 * @param doCount true to count occurrences.
	 */
	public Counts(Dependencies dependencies, boolean doCount) {
		if (dependencies.isDiscrete()) {
			throw new IllegalArgumentException("Cannot count discrete occurrences when there are non-discrete dependencies.");
		}
		
		// Store dependencies in array.
		Collection<Dependency> deps = dependencies.getAll();
		Iterator<Dependency> depit = deps.iterator();
		this.dependencies = new Dependency[deps.size()];
		for (int i = 0; i < this.dependencies.length; ++i) {
			this.dependencies[i] = depit.next();
		}
		
		this.lengths = new int[this.dependencies.length + 1];
		this.lengthFactors = new int[this.dependencies.length + 1];
		
		// Indexing using the child first, then parents in order of appearance.
		DiscreteAttribute ch = (DiscreteAttribute) dependencies.getChild();
		this.lengths[0] = ch.getInterval().getSize();
		this.lengthFactors[0] = 1;
		for (int i = 0; i < this.dependencies.length; ++i) {
			DiscreteAttribute par = (DiscreteAttribute) this.dependencies[i].getParent();
			lengths[i + 1] = par.getInterval().getSize();
			this.lengthFactors[i + 1] = this.lengths[i + 1] * this.lengthFactors[i];
		}
		
		// Create space for counts.
		int n = this.lengths[0] * this.lengthFactors[this.lengthFactors.length-1];
		if (n > MAX_CARDINALITY) {
			throw new IllegalArgumentException("Cannot create counts for such extensive dependencies.");
		}
		this.counts = new int[n];	
		if (doCount) {
			this.count();
		}
	}
	
	/**
	 * Constructor. Will immediately count occurrences.
	 * @param dependencies the dependencies and their entities.
	 */
	public Counts(Dependencies dependencies) {
		this(dependencies, true);
	}
	
	/**
	 * Counts the occurrences. At the moment, it is assumed that there is a
	 * single entity of each parent for each child entity.
	 */
	public void count() {
		DiscreteAttribute ch = (DiscreteAttribute) this.dependencies[0].getChild();
		int n = ch.getNoOfEntities();
		int[] vals = new int[this.lengths.length];
		for (int i = 0; i < n; ++i) {
			vals[0] = ch.getEntityAsInt(i);
			for (int j = 0; j < this.dependencies.length; ++j) {
				Dependency dep = this.dependencies[j];
				DiscreteAttribute par = (DiscreteAttribute) dep.getParent();
				vals[j + 1] = (dep.hasIndex() ?
						par.getEntityAsInt(dep.getSingleParentEntityIndexed(i)) :
						par.getEntityAsInt(dep.getSingleParentEntity(i)));
			}
			this.increment(vals);
		}
	}
	
	/**
	 * Increments the counter at a certain configuration of child value and parent values.
	 * @param values the child value at element 0, then the parent values in
	 *        the order returned by Dependencies.
	 */
	private void increment(int[] values) {
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			idx += values[i] * this.lengthFactors[i];
		}
		++(this.counts[idx]);
	}
	
	/**
	 * Returns the count for a certain configuration of child value and parent values.
	 * No bounds checking.
	 * @param childVal the child value.
	 * @param parentVal the parent values in the order returned by Dependencies.
	 * @return the count.
	 */
	public int get(int childVal, int[] parentVal) {
		int idx = childVal;
		for (int i = 0; i < parentVal.length; ++i) {
			idx += parentVal[i] * this.lengthFactors[i + 1];
		}
		return this.counts[idx];
	}
	
	/**
	 * Returns the count for a certain configuration of child value and parent values.
	 * No bounds checking.
	 * @param values the child value at element 0, then the parent values in
	 *        the order returned by Dependencies.
	 * @return the count.
	 */
	public int get(int[] values) {
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			idx += values[i] * this.lengthFactors[i];
		}
		return this.counts[idx];
	}
	
	@Override
	public Class<?> getComponentType() {
		return Integer.TYPE;
	}

	@Override
	public int[] getLengths() {
		return this.counts;
	}

	@Override
	public int getRank() {
		return this.lengths.length;
	}
	
}
