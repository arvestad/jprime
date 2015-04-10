package se.cbb.jprime.mcmc;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Base class for statistics of <code>Proposer</code> objects (and potentially others) with respect to
 * acceptance and rejection. If desired, one may increment the acceptance/rejections counter by
 * also providing a string key for a specific sub-category that should be monitored.
 * <p/>
 * See also <code>FineProposerStatistics</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class ProposerStatistics implements InfoProvider {

	/** Overall number of accepted proposals. */
	protected int noOfAccepted;
	
	/** Overall number of rejected proposals. */
	protected int noOfRejected;
	
	/** Hash for key-specific acceptance/rejections. */
	protected TreeMap<String, int[]>  accRejByKey;
	
	/**
	 * Constructor.
	 */
	public ProposerStatistics() {
		this.noOfAccepted = 0;
		this.noOfRejected = 0;
		this.accRejByKey = new TreeMap<String, int[]>();
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
	 * @param wasAccepted true if new state was accepted; false if rejected.
	 */
	public void increment(boolean wasAccepted) {
		if (wasAccepted) {
			++this.noOfAccepted;
		} else {
			++this.noOfRejected;
		}
	}
	
	/**
	 * Adds a proposal outcome.
	 * @param wasAccepted true if new state was accepted; false if rejected.
	 * @param category a specific sub-category to which the acceptance/rejection belongs.
	 */
	public void increment(boolean wasAccepted, String category) {
		this.increment(wasAccepted);
		int[] cat = this.accRejByKey.get(category);
		if (cat == null) {
			cat = new int[2];
			cat[wasAccepted ? 0 : 1] = 1;
			this.accRejByKey.put(category, cat);
		} else {
			cat[wasAccepted ? 0 : 1] += 1;
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
		if (!this.accRejByKey.isEmpty()) {
			sb.append(prefix).append("Acceptance ratios for sub-categories:\n");
			prefix += '\t';
			for (Entry<String, int[]> kv : this.accRejByKey.entrySet()) {
				int acc = kv.getValue()[0];
				int rej = kv.getValue()[1];
				sb.append(prefix).append(kv.getKey()).append('\t').append(acc).append(" / ").append(acc+rej).append(" = ").append(acc/(double)(acc+rej)).append("\n");
			}
		}
		return sb.toString();
	}
}
