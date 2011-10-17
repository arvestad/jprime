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
 * Importantly, a <code>Proposer</code> acting on a state parameter is responsible for
 * caching and possibly restoring its value. However, after a perturbation (but prior to
 * accepting or rejecting the state), the <code>Proposer</code> must invoke <code>setChangeInfo()</code>
 * so that child dependents may know what has happened.
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
	
	/**
	 * Sets info detailing what has changed in this object (so that
	 * this info can be passed on to any children).
	 * This should be invoked by a <code>Proposer</code> when it has performed a
	 * perturbation. If not, the object may not notice it has been changed.
	 * Also, the <code>Proposer</code> should clear it after state has been
	 * accepted or rejected.
	 * @param info disclosing the change of this object.
	 */
	public void setChangeInfo(ChangeInfo info);
	
}
