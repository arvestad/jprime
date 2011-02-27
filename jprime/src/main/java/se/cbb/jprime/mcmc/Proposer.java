package se.cbb.jprime.mcmc;

import java.util.Set;

/**
 * Interface for objects perturbing the values of one or more
 * <code>StateParameter</code> objects, e.g. MCMC parameters.
 * Each <code>Proposer</code> is also associated with:
 * <ul>
 * <li>a <code>ProposerWeight</code> which dictates how often it will in
 *     fact be invoked to perform a perturbation (the weight may e.g. change over time).</li>
 * <li>a <code>ProposerStatistics</code> which keeps track of how the often proposed states
 *     have been accepted or rejected (possibly including more detailed info like acceptance ratio changes
 *     over time).</li>
 * </ul>
 * @author Joel Sjöstrand.
 */
public interface Proposer {

	/**
	 * Returns the parameters perturbed by this object.
	 * @return the parameters.
	 */
	public Set<StateParameter> getParameters();
	
	/**
	 * Returns the number of parameters.
	 * @return the number of parameters.
	 */
	public int getNoOfParameters();
	
	/**
	 * Returns the total number of sub-parameters contained within the
	 * parameters perturbed by this object. If a scalar parameter, 1 should
	 * be returned.
	 * @return the total number of sub-parameters.
	 */
	public int getNoOfSubParameters();
	
	/**
	 * Returns the weight object associated with this object.
	 * See also <code>getWeight()</code>.
	 * @return the weight object.
	 */
	public ProposerWeight getProposerWeight();
	
	/**
	 * Shorthand for retrieving the current weight of the
	 * <code>ProposerWeight</code> associated with this object.
	 * @return the current weight.
	 */
	public double getWeight();
	
	/**
	 * Returns the proposal statistics of this object.
	 * @return the statistics.
	 */
	public ProposerStatistics getStatistics();
}
