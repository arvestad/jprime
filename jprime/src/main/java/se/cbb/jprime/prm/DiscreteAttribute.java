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
	 * Returns an attribute value as an integer.
	 * @param idx the index.
	 * @return the value.
	 */
	public int getEntityAsInt(int idx);
	
}
