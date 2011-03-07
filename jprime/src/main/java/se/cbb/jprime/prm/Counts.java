package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.Iterator;

import se.cbb.jprime.math.IntegerInterval;
import se.cbb.jprime.misc.MultiArray;

/**
 * Represents a multidimensional array of value counts
 * for a set of dependencies connecting a single child attribute and
 * its parents. The dependency input collection may have cardinality 0, in
 * which case only the occurrences of the child's values are counted.
 * <p/>
 * More formally, holds |(v_p1,...,v_pk,v_c)| for all possible realisations of tuples
 * (v_p1,...,v_pk,v_c) where v_pi refer to parent i's value and v_c to the child's.
 * Additionally, the sum over all child values, |(v_p1,...,v_pk)|), is stored for
 * quick access.
 * 
 * @author Joel Sjöstrand.
 */
public class Counts implements MultiArray {

	/** Maximum number of elements allowed in multidimensional array. */
	public static final int MAX_CARDINALITY = 1000000;
	
	/** The dependency entities which to count. */
	private Dependencies dependencies;
	
	/** Shorthand for this.dependencies.size(). */
	private int noOfParents;
	
	/** Lengths of array, child last, i.e., |v(p1)|,...,|v(pk)|,|v(c)|. */
	private int[] lengths;
	
	/** Length factors for computing array index. Contents: 1,lengths[0],lengths[0]*lengths[1],... */
	private int[] lengthFactors;
	
	/** Counts. */
	private int[] counts;
	
	/** Counts summed over child values. Not applicable when the number of parents is 0. */ 
	private int[] summedCounts;
	
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
		this.noOfParents = deps.size();
		this.lengths = new int[this.noOfParents + 1];
		this.lengthFactors = new int[this.noOfParents + 1];
		
		// Add parents in order of appearance.
		Iterator<Dependency> it = deps.iterator();
		int prevLength = 1;
		int prevLengthFactor = 1;
		for (int i = 0; i < this.noOfParents; ++i) {
			IntegerInterval range = ((DiscreteAttribute) it.next().getParent()).getInterval();
			this.lengths[i] = range.getSize();
			this.lengthFactors[i] = prevLength * prevLengthFactor;
			prevLength = this.lengths[i];
			prevLengthFactor = this.lengthFactors[i];
		}
		
		// Child corresponds to last index.
		IntegerInterval range = ((DiscreteAttribute) dependencies.getChild()).getInterval();
		this.lengths[this.noOfParents] = range.getSize();
		this.lengthFactors[this.noOfParents] = prevLength * prevLengthFactor;
		
		// Create space for counts.
		int n = this.lengths[this.noOfParents] * this.lengthFactors[this.noOfParents];
		if (n > MAX_CARDINALITY) {
			throw new IllegalArgumentException("Cannot create counts for such an extensive dependency range of values.");
		}
		this.counts = new int[n];
		this.summedCounts = new int[this.lengthFactors[this.noOfParents]];
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
		int[] vals = new int[this.noOfParents + 1];
		
		// Make array for swifter access.
		Dependency[] deps = new Dependency[this.noOfParents];
		deps = this.dependencies.getAll().toArray(deps);
		boolean hasIndex[] = new boolean[this.noOfParents];
		for (int i = 0; i < this.noOfParents; ++i) {
			hasIndex[i] = deps[i].hasIndex();
		}
		
		// Process all child entities and count.
		DiscreteAttribute ch = (DiscreteAttribute) this.dependencies.getChild();
		int n = ch.getNoOfEntities();
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < this.noOfParents; ++j) {
				DiscreteAttribute par = (DiscreteAttribute) deps[j].getParent();
				vals[j] = (hasIndex[j] ?
						par.getEntityAsNormalisedInt(deps[j].getSingleParentEntityIndexed(i)) :
						par.getEntityAsNormalisedInt(deps[j].getSingleParentEntity(i)));
			}
			vals[this.noOfParents] = ch.getEntityAsNormalisedInt(i);
			this.increment(vals);
		}
	}
	
	/**
	 * Increments the counter at a certain configuration of child value and parent values.
	 * The conversion to integers must comply the attributes' method <code>getEntityAsNormalisedInt()</code>.
	 * @param values the parent values in the order returned by Dependencies (possibly none), then
	 *        the child value.
	 */
	private void increment(int[] values) {
		int idx = 0;
		for (int i = 0; i < this.noOfParents; ++i) {
			idx += values[i] * this.lengthFactors[i];
		}
		++(this.summedCounts[idx]);
		idx += values[this.noOfParents] * this.lengthFactors[this.noOfParents];
		++(this.counts[idx]);
	}
	
	/**
	 * Returns the count for a certain configuration of child value and parent values.
	 * No bounds checking.
	 * The conversion to integers must comply the attributes' method <code>getEntityAsNormalisedInt()</code>.
	 * @param parentVal the parent values in the order returned by Dependencies, possibly empty,
	 *        but not null.
	 * @param childVal the child value.
	 * @return the count.
	 */
	public int get(int[] parentVal, int childVal) {
		int idx = 0;
		for (int i = 0; i < this.noOfParents; ++i) {
			idx += parentVal[i] * this.lengthFactors[i];
		}
		idx += childVal * this.lengthFactors[this.noOfParents];
		return this.counts[idx];
	}
	
	/**
	 * Returns the count for a certain configuration of parent values and child value.
	 * No bounds checking.
	 * The conversion to integers must comply the attributes' method <code>getEntityAsNormalisedInt()</code>.
	 * @param values the the parent values in the order returned by Dependencies (possibly none), then
	 *        the child value. 
	 * @return the count.
	 */
	public int get(int[] values) {
		int idx = 0;
		for (int i = 0; i <= this.noOfParents; ++i) {
			idx += values[i] * this.lengthFactors[i];
		}
		return this.counts[idx];
	}
	
	/**
	 * Returns the count for a certain configuration of parent values (summed over possible child values).
	 * No bounds checking, implying that if no parents exist, an IndexOutOfBoundsException will be thrown.
	 * The conversion to integers must comply the attributes' method <code>getEntityAsNormalisedInt()</code>.
	 * @param values the parent values in the order returned by Dependencies. A child value may be appended at the
	 *        end, but will be discarded.
	 * @return the count.
	 */
	public int getSum(int[] values) {
		int idx = 0;
		for (int i = 0; i < this.noOfParents; ++i) {
			idx += values[i] * this.lengthFactors[i];
		}
		return this.summedCounts[idx];
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
