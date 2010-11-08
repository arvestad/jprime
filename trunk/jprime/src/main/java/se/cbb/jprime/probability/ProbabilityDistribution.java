package se.cbb.jprime.probability;

/**
 * Base interface for all probability distributions
 * (continuous, discrete, composite, 1-D, 2-D, etc.).
 * 
 * @author Joel Sjöstrand.
 */
public interface ProbabilityDistribution {

	/**
	 * Returns the probability distribution's name.
	 * @return the name.
	 */
	public String getName();
	
	/**
	 * Returns the number of parameters defining the
	 * distribution, including those governing boundaries.
	 * @return the number of parameters.
	 */
	public int getNoOfParameters();
	
	/**
	 * Returns the dimensionality of the distribution.
	 * @return the number of dimensions.
	 */
	public int getNoOfDimensions();
}
