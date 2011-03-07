package se.cbb.jprime.prm;

import se.cbb.jprime.math.IntegerInterval;

/**
 * Interface for discrete valued PRM attributes.
 * Any value should be able to represented as an integer.
 * 
 * @author Joel Sjöstrand.
 */
public interface DiscreteAttribute extends ProbAttribute {

	/**
	 * Returns the interval of this attribute.
	 * @return the interval.
	 */
	public IntegerInterval getInterval();
	
	/**
	 * Returns the number of valid values of this attribute.
	 * @return the number of values.
	 */
	public int getIntervalSize();
	
	/**
	 * For k valid values, returns an attribute value as an integer transformed to
	 * be in the range [0,k-1],
	 * irrespective of the internal representation.
	 * @param idx the index.
	 * @return the value as a non-negative integer.
	 */
	public int getEntityAsNormalisedInt(int idx);
	
	/**
	 * Setter mirroring <code>getEntityAsNormalisedInt()</code>.
	 * @param idx the index.
	 * @param value, the value as a non-negative integer.
	 */
	public void setEntityAsNormalisedInt(int idx, int value);
	
	/**
	 * Adds an entity as a normalised integer. See <code>getEntityAsNormalisedInt()</code>
	 * for more info.
	 * @param value the value to add.
	 */
	public void addEntityAsNormalisedInt(int value);
	
	/**
	 * For latent attributes, returns the current estimation of
	 * an entity's probability distribution, i.e., its soft completion.
	 * Indexing complies
	 * with the <code>getEntityAsNormalisedInt()</code> method.
	 * @param idx the index.
	 * @return the estimated probability distribution.
	 */
	public double[] getEntityProbDistribution(int idx);
	
	/**
	 * For latent attributes, sets the current estimation of
	 * an entity's probability distribution, i.e., its soft completion.
	 * Indexing complies with the <code>getEntityAsNormalisedInt()</code> method.
	 * @param idx the index.
	 * @param probDist the soft completion.
	 */
	public void setEntityProbDistribution(int idx, double[] probDist);
}
