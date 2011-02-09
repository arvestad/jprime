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
	 * Returns an attribute value as an integer.
	 * @param idx the index.
	 * @return the value.
	 */
	public int getEntityAsInt(int idx);
	
	/**
	 * Returns an attribute value as an integer transformed to be >=0.
	 * I.e., for an k originally defined in the range [a,b], returns k'=k-a (thus
	 * in the range [0,b-a]).
	 * @param idx the index.
	 * @return the value, guaranteed to be non-negative.
	 */
	public int getEntityAsIntNormalised(int idx);
	
}
