package se.cbb.jprime.mcmc;

/**
 * Interface for parameters, e.g. the states of an MCMC chain.
 * A parameter may e.g. be a vector or matrix, in which the individual
 * elements are referred to as "sub-parameters".
 * 
 * @author Joel Sjöstrand.
 */
public interface Parameter {

	/**
	 * Returns the name of the parameter.
	 * @return the name.
	 */
	public String getName();
	
	/**
	 * Returns the number of sub-parameters, e.g. the number of 
	 * elements if the parameters is a vector. For scalars,
	 * 1 should be returned.
	 * @return the number of sub-parameters; 1 for scalar parameters.
	 */
	public int getNoOfSubParameters();
}
