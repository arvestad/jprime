package se.cbb.jprime.mcmc;

import se.cbb.jprime.io.Sampleable;

/**
 * Interface for parameters, e.g. the states of an MCMC chain.
 * A parameter may e.g. be a vector or matrix, in which the individual
 * elements are referred to as "sub-parameters".
 * <p/>
 * A parameter is also a <code>Dependent</code>, although it is generally assumed (but not required)
 * that it is
 * a source in the corresponding dependency DAG. Even though there may be interconnections between
 * parameters (e.g. the times t of a tree S), it is often assumed that these are independent
 * (although a Proposer which changes S usually also perturbs t). Typically, methods will be invoked
 * in the same order as for a regular <code>Dependent</code>:
 * <ol>
 * <li>This object is about to be perturbed by a <code>Proposer</code>: <code>this.cache()</code>.</li>
 * <li>This object has been perturbed: <code>this.update()</code>.
 * <li>
 *   <ul>
 *     <li>Changes have been accepted: <code>this.clearCache()</code>.</li>
 *     <li>Changes have been rejected: <code>this.restoreCache()</code>.</li>
 *   </ul>
 * </li>
 * </ol>
 *  
 * @author Joel Sjöstrand.
 */
public interface StateParameter extends Dependent, Sampleable, MCMCSerializable {

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
	 * @param info disclosing the change of this object.
	 */
	public void setChangeInfo(ChangeInfo info);
	
}
