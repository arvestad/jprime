package se.cbb.jprime.mcmc;

/**
 * Base class for statistics of <code>Proposer</code> objects with respect to
 * acceptance and rejection. Only provides access to the most basic numbers, although
 * subclasses may add information like how the acceptance ratio has changed over time,
 * etc.
 * <p/>
 * See also <code>FineProposerStatistics</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class ProposerStatistics implements InfoProvider {

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
	 * Returns the number of rejected proposals divided by the total number of proposals.
	 * @return the rejection ratio.
	 */
	public double getRejectionRatio() {
		return (this.noOfRejected / (double) (this.noOfAccepted + this.noOfRejected));
	}
	
	/**
	 * Adds a proposal outcome.
	 * @param wasAccepted true is new state was accepted; false if rejected.
	 * @param noOfParams the number of perturbed parameters or sub-parameters (the choice is up to the parent Proposer).
	 */
	public void increment(boolean wasAccepted, int noOfParams) {
		if (wasAccepted) {
			++this.noOfAccepted;
		} else {
			++this.noOfRejected;
		}
	}

	@Override
	public String getPreInfo(String prefix) {
		return (prefix + "PROPOSER STATISTICS\n");
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("PROPOSER STATISTICS\n");
		sb.append(prefix).append("Acceptance ratio: ").append(this.noOfAccepted).append(" / ").append(this.getNoOfProposals()).append(" = ").append(this.getAcceptanceRatio()).append("\n");
		return sb.toString();
	}
}
