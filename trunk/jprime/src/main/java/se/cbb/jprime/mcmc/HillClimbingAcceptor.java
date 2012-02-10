package se.cbb.jprime.mcmc;

import java.util.List;

import se.cbb.jprime.math.LogDouble;

/**
 * Hill-climbing proposal acceptor. Only accepts a proposed state x' which has higher likelihood
 * than the old state x.
 * 
 * @author Joel Sjöstrand.
 */
public class HillClimbingAcceptor implements ProposalAcceptor {

	/** Max number of non-improvements. */
	private int maxTries;
	
	/** Number of straight non-improvements. */
	private int noOfTries;
	
	/** Precision for improvement comparison. */
	private double precision = 1.0 + 1e-32;
	
	/**
	 * Constructor.
	 * @param maxTries maximum number of rejected iterations before optimum deemed reached.
	 */
	public HillClimbingAcceptor(int maxTries) {
		this.maxTries = maxTries;
		this.noOfTries = 0;
	}
	
	/**
	 * Returns true if a proposed state x' has a higher likelihood than the old state x. Does not require any proposal
	 * information nor a pseudo-random number generator. If the optimum is deemed to have been reached, 
	 * a <code>RunAbortedException</code> is thrown.
	 * @param proposedStateLikelihood the likelihood P(x').
	 * @param oldStateLikelihood the likelihood P(x).
	 * @param proposals details the proposals made for going from x to x'. May be null.
	 * @return true if suggested state accepted; false if rejected.
	 */
	@Override
	public boolean acceptProposedState(LogDouble proposedStateLikelihood,
			LogDouble oldStateLikelihood, List<Proposal> proposals) throws RunAbortedException {
		// If we do have proposal information, just verify it's OK.
		if (proposals != null) {
			for (Proposal prop : proposals) {
				if (!prop.isValid()) { return false; }
			}
		}
		//System.out.println("Proposed: " + proposedStateLikelihood);
		//System.out.println("Old: " + oldStateLikelihood);
		//System.out.println("Old*p: " + oldStateLikelihood.multToNew(this.precision));
		boolean isImproved =
			proposedStateLikelihood.greaterThan(oldStateLikelihood.multToNew(this.precision));
		if (isImproved) {
			this.noOfTries = 0;
		} else {
			this.noOfTries++;
		}
		if (this.noOfTries > this.maxTries) {
			throw new RunAbortedException("Optimum reached: " + this.noOfTries + " iterations without improvement.");
		}
		return isImproved;
	}
	
	@Override
	public String getPreInfo(String prefix) {
		return (prefix + "HILL-CLIMBING ACCEPTOR\n");
	}

	@Override
	public String getPostInfo(String prefix) {
		return (prefix + "HILL-CLIMBING ACCEPTOR\n");
	}

	/**
	 * Sets the precision p such that a new state k' is deemed an
	 * improvement over old state k if likelihood(k')>p*likelihood(k).
	 * @param p the precision.
	 */
	public void setPrecision(double p) {
		this.precision = p;
	}
	
	/**
	 * Returns the precision p such that a new state k' is deemed an
	 * improvement over old state k if likelihood(k')>p*likelihood(k).
	 * @return the precision.
	 */
	public double getPrecision() {
		return this.precision;
	}
	
}
