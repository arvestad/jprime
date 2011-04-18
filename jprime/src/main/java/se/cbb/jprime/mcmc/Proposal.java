package se.cbb.jprime.mcmc;

import java.util.Set;

/**
 * Interface for detailing a state parameter initiated by a single <code>Proposer</code>.
 * The entire state change consists of one or possibly more of these objects.
 * 
 * @author Joel Sjöstrand.
 */
public interface Proposal {
	
	/**
	 * Returns the object which did the perturbation which yielded this object.
	 * @return the responsible proposer.
	 */
	public Proposer getProposer();
	
	/**
	 * Returns a list of all parameters which were in fact perturbed (or at least attempted to be perturbed)
	 * by the responsible <code>Proposer</code> for this state change.
	 * @return the perturbed parameters.
	 */
	public Set<StateParameter> getPerturbedParameters();
	
	/**
	 * Returns the number of parameters which were in fact perturbed (or at least attempted to be perturbed)
	 * by the responsible <code>Proposer</code> for this state change.
	 * @return the number of perturbed parameters.
	 */
	public int getNoOfPerturbedParameters();
	
	/**
	 * Returns the total number of sub-parameters which were in fact perturbed
	 * by the responsible <code>Proposer</code> for this state change.
	 * @return the total number of perturbed sub-parameters.
	 */
	public int getNoOfPerturbedSubParameters();
	
	/**
	 * Returns true if the proposal is indeed valid.
	 * @return true if valid, false if invalid.
	 */
	public boolean isValid();
	
}
