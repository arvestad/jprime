package se.cbb.jprime.mcmc;

import java.util.Set;

import se.cbb.jprime.math.Probability;

/**
 * Interface for detailing a state parameter initiated by a single <code>Proposer</code>.
 * The entire state change may consist of one or possibly more of these objects.
 * Typically, for Metropolis-Hastings compliant proposals, the
 * "backward" and "forward" proposal densities can be retrieved. 
 * 
 * @author Joel Sjöstrand.
 */
public interface Proposal {
	
	/**
	 * Returns the probability density Q(x';x) for
	 * obtaining the new value x' given the old value x.
	 * May return null if Metropolis-Hastings not applicable.
	 * @return the "forward" probability density.
	 */
	public Probability getForwardProposalDensity();
	
	/**
	 * Returns the probability density Q(x;x') for
	 * obtaining the old value x given the new value x'.
	 * May return null if Metropolis-Hastings not applicable.
	 * @return the "backward" probability density.
	 */
	public Probability getBackwardProposalDensity();
	
	/**
	 * Returns the ratio Q(x;x')/Q(x';x) for the old state x and the new state
	 * x', i.e. the ratio between the "backward" and "forward" proposal densities
	 * respectively.
	 * May return null if Metropolis-Hastings not applicable.
	 * @return the ratio between the "backward" and "forward" proposal densities.
	 */
	public Probability getProposalDensityRatio();
	
	/**
	 * Returns the object which did the perturbation which yielded this object.
	 * @return the responsible proposer.
	 */
	public Proposer getProposer();
	
	/**
	 * Returns a list of all parameters which were in fact perturbed
	 * by the responsible <code>Proposer</code> for this state change.
	 * @return the perturbed parameters.
	 */
	public Set<StateParameter> getParameters();
	
	/**
	 * Returns the number of parameters which were in fact perturbed
	 * by the responsible <code>Proposer</code> for this state change.
	 * @return the number of perturbed parameters.
	 */
	public int getNoOfParameters();
	
}
