package se.cbb.jprime.mcmc;

/**
 * Simple interface for real-valued state parameters.
 * Enables parameters with only one sub-parameter and those with several (arrays)
 * to be treated similarly.
 * 
 * @author Joel Sjöstrand.
 */
public interface RealParameter extends StateParameter {

	/**
	 * Returns the value of a certain sub-parameter.
	 * @param idx the index of the sub-parameter.
	 * @return the sub-parameter's value.
	 */
	public double getValue(int idx);
	
	/**
	 * Sets the value of a certain sub-parameter.
	 * @param idx the index of the sub-parameter.
	 * @param value the new value.
	 */
	public void setValue(int idx, double value);
	
}
