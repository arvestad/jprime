package se.cbb.jprime.mcmc;

/**
 * Interface for statistics of <code>Proposer</code> objects with respect to
 * acceptance and rejection. Only provides access to the most basic numbers, although
 * implementing classes may add information like how the acceptance ratio has changed over time,
 * etc. 
 * 
 * @author Joel Sjöstrand.
 */
public interface ProposerStatistics {

	/**
	 * Returns the number of times the associated <code>Proposer</code> has suggested
	 * new states.
	 * @return the number of proposals.
	 */
	public int getNoOfProposals();
	
	/**
	 * Returns the number of times the associated <code>Proposer</code> has accepted
	 * new states.
	 * @return the number of accepted proposals.
	 */
	public int getNoOfAcceptedProposals();
	
	/**
	 * Returns the number of times the associated <code>Proposer</code> has rejected
	 * new states.
	 * @return the number of rejected proposals.
	 */
	public int getNoOfRejectedProposals();
}
