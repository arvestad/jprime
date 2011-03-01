package se.cbb.jprime.mcmc;

/**
 * Base class for statistics of <code>Proposer</code> objects with respect to
 * acceptance and rejection. Only provides access to the most basic numbers, although
 * subclasses may add information like how the acceptance ratio has changed over time,
 * etc. 
 * 
 * @author Joel Sjöstrand.
 */
public class ProposerStatistics {

	/** Number of accepted proposals. */
	protected int noOfAccepted;
	
	/** Number of rejected proposals. */
	protected int noOfRejected;
	
	/**
	 * Constructor.
	 */
	public ProposerStatistics() {
		this.noOfAccepted = 0;
		this.noOfRejected = 0;
	}
	
	/**
	 * Returns the number of times the associated <code>Proposer</code> has suggested
	 * new states.
	 * @return the number of proposals.
	 */
	public int getNoOfProposals() {
		return (this.noOfAccepted + this.noOfRejected);
	}
	
	/**
	 * Returns the number of times the associated <code>Proposer</code> has accepted
	 * new states.
	 * @return the number of accepted proposals.
	 */
	public int getNoOfAcceptedProposals() {
		return this.noOfAccepted;
	}
	
	/**
	 * Returns the number of times the associated <code>Proposer</code> has rejected
	 * new states.
	 * @return the number of rejected proposals.
	 */
	public int getNoOfRejectedProposals() {
		return this.noOfRejected;
	}
	
	/**
	 * Returns the number of accepted proposals divided by the total number of proposals.
	 * @return the acceptance ratio.
	 */
	public double getAcceptanceRatio() {
		return (this.noOfAccepted / (double) (this.noOfAccepted + this.noOfRejected));
	}
	
	/**
	 * Adds a proposal outcome.
	 * @param wasAccepted true is new state was accepted; false if rejected.
	 */
	public void increment(boolean wasAccepted) {
		if (wasAccepted) {
			this.noOfAccepted++;
		} else {
			this.noOfRejected++;
		}
	}
}
