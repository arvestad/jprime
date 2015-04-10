package se.cbb.jprime.mcmc;

import se.cbb.jprime.io.Sampleable;

/**
 * Interface for state parameters, for instance in an MCMC chain.
 * A state parameter may e.g. be a vector or matrix, in which the individual
 * elements are referred to as "sub-parameters".
 * <p/>
 * A state parameter is a <code>Dependent</code>, albeit typically a source in the DAG.
 * Even though there may be interconnections between
 * parameters (e.g. the times <i>t</i> of a tree <i>S</i>), it is often assumed that these are independent
 * (although a <code>Proposer</code> which changes <i>S</i> usually also perturbs <i>t</i>).
 * <p/>
 * Importantly, a <code>StateParameter</code> which is perturbed is not by itself responsible for
 * caching and restoring its contents. Rather, this is up to the <code>Proposer</code> which carried
 * out the perturbation.
 *  
 * @author Joel Sjöstrand.
 */
public interface StateParameter extends Dependent, Sampleable {

	/**
	 * Returns the name of the parameter.
	 * @return the name.
	 */
	public String getName();
	
	/**
	 * Returns the number of sub-parameters, e.g. the number of 
	 * elements if the parameter is a vector. For scalars,
	 * 1 should be returned.
	 * @return the number of sub-parameters; 1 for scalar parameters.
	 */
	public int getNoOfSubParameters();
	
}
