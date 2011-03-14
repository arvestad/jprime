package se.cbb.jprime.mcmc;

import se.cbb.jprime.math.Probability;

/**
 * Interface for detailing a state parameter change.
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
	
}
