package se.cbb.jprime.mcmc;

import java.util.List;

/**
 * Interface for selecting one or more MCMC proposers for actual
 * perturbation based on their current weights.
 *  
 * @author Joel Sjöstrand.
 */
public interface ProposerSelector {

	/**
	 * Selects a subset of a list of proposers which can then be used
	 * for actual perturbations. It is up to implementing classes to
	 * decide whether they e.g. return only a single object or multiple
	 * objects. Furthermore, the returned proposers are guaranteed to
	 * be acting on disjoint state parameters.
	 * @param proposers the list of proposers to choose among.
	 * @return a subset of proposers, no pairs acting on the same state parameter.
	 */
	public List<Proposer> selectProposersDisjointly(List<Proposer> proposers);
}
