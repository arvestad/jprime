package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.Iterator;

import se.cbb.jprime.misc.MultiArray;

/**
 * Represents a multidimensional array of value counts
 * for a set of dependencies connecting a single child attribute and
 * its parents. The dependency input collection may have cardinality 0, in
 * which case only the occurrences of the child's values are counted.
 * 
 * @author Joel Sjöstrand.
 */
public class Counts implements MultiArray {

	/** Maximum number of elements allowed in multidimensional array. */
	public static final int MAX_CARDINALITY = 1000000;
	
	/** The dependency entities which to count. */
	private final Dependencies dependencies;
	
	/** Lengths in each dimension of array, child first. */
	private final int[] lengths;
	
	/** Length factors for computing array index. Contents: 1,|X|,|X|*|Y|,... */
	private final int[] lengthFactors;
	
	/** Counts. */
	private final int[] counts;
	
	/** Offsets for normalising values to start at 0. */
	private final int[] offsets;
	
	/**
	 * Constructor.
	 * @param dependencies the dependencies and their entities.
	 * @param doCount true to count occurrences right away.
	 */
	public Counts(Dependencies dependencies, boolean doCount) {
		if (!dependencies.isDiscrete()) {
			throw new IllegalArgumentException("Cannot count discrete occurrences when there are non-discrete dependencies.");
		}
		
		this.dependencies = dependencies;
		Collection<Dependency> deps = dependencies.getAll();
		int k = deps.size();
		this.lengths = new int[k + 1];
		this.lengthFactors = new int[k + 1];
		this.offsets = new int[k + 1];
		
		// Indexing using the child first.
		DiscreteAttribute ch = (DiscreteAttribute) dependencies.getChild();
		this.lengths[0] = ch.getInterval().getSize();
		this.lengthFactors[0] = 1;
		this.offsets[0] = -ch.getInterval().getLowerBound();
		
		// Add parents in order of appearance.
		Iterator<Dependency> it = deps.iterator();
		for (int i = 1; it.hasNext(); ++i) {
			DiscreteAttribute par = (DiscreteAttribute) it.next().getParent();
			lengths[i] = par.getInterval().getSize();
			this.lengthFactors[i] = this.lengths[i - 1] * this.lengthFactors[i - 1];
			this.offsets[i] = -par.getInterval().getLowerBound();
		}
		
		// Create space for counts.
		int n = this.lengths[k] * this.lengthFactors[k];
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
	 * single entity of each parent for each child entity. It is allowed to have
	 * 0 dependencies.
	 */
	public void count() {
		DiscreteAttribute ch = (DiscreteAttribute) this.dependencies.getChild();
		int n = ch.getNoOfEntities();
		int[] vals = new int[this.lengths.length];
		
		// Make array for swifter access.
		Dependency[] deps = new Dependency[this.dependencies.getSize()];
		deps = this.dependencies.getAll().toArray(deps);
		
		for (int i = 0; i < n; ++i) {
			vals[0] = ch.getEntityAsInt(i);
			for (int j = 0; j < deps.length; ++j) {
				DiscreteAttribute par = (DiscreteAttribute) deps[j].getParent();
				vals[j + 1] = (deps[j].hasIndex() ?
						par.getEntityAsInt(deps[j].getSingleParentEntityIndexed(i)) :
						par.getEntityAsInt(deps[j].getSingleParentEntity(i)));
			}
			this.increment(vals);
		}
	}
	
	/**
	 * Increments the counter at a certain configuration of child value and parent values.
	 * @param values the child value at element 0, then the parent values in
	 *        the order returned by Dependencies (possibly none).
	 */
	private void increment(int[] values) {
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			idx += (values[i] + this.offsets[i]) * this.lengthFactors[i];
		}
		++(this.counts[idx]);
	}
	
	/**
	 * Returns the count for a certain configuration of child value and parent values.
	 * No bounds checking.
	 * @param childVal the child value.
	 * @param parentVal the parent values in the order returned by Dependencies, possibly empty,
	 *        but not null.
	 * @return the count.
	 */
	public int get(int childVal, int[] parentVal) {
		int idx = (childVal + this.offsets[0]);
		for (int i = 1; i <= parentVal.length; ++i) {
			idx += (parentVal[i - 1] + this.offsets[i]) * this.lengthFactors[i];
		}
		return this.counts[idx];
	}
	
	/**
	 * Returns the count for a certain configuration of child value and parent values.
	 * No bounds checking.
	 * @param values the child value at element 0, then the parent values in
	 *        the order returned by Dependencies, possibly empty, but not null.
	 * @return the count.
	 */
	public int get(int[] values) {
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			idx += (values[i] + this.offsets[i]) * this.lengthFactors[i];
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
