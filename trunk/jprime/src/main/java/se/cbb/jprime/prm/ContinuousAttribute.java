package se.cbb.jprime.prm;

import se.cbb.jprime.math.RealInterval;

/**
 * Interface for real-valued PRM attributes.
 * 
 * @author Joel Sjöstrand.
 */
public interface ContinuousAttribute extends ProbabilisticAttribute {

	/**
	 * Returns the interval of this attribute.
	 * @return the interval.
	 */
	public RealInterval getInterval();
	
}
