package se.cbb.jprime.mcmc;

import java.util.ArrayList;

/**
 * Extends the proposer statistics by being able to track acceptance
 * ratios in more detail over time. This is done by dividing the
 * iteration span in a set of (approximately) equidistant "windows".
 * <p/>
 * In addition, it tracks the distribution of accepted/rejected proposals
 * broken down on the number of perturbed parameters.
 * 
 * @author Joel Sjöstrand.
 */
public class FineProposerStatistics extends ProposerStatistics implements IterationListener {
	
	/** Number of accepted proposals in each window. */
	protected int[] noOfAcceptedPerWindow;
	
	/** Number of rejected proposals in each window. */
	protected int[] noOfRejectedPerWindow;
	
	/** The current window (i.e. "bucket"). */
	protected int currentWindow;
	
	/** Number of accepted proposals per number of parameters. */
	protected ArrayList<Integer> noOfAcceptedByNoOfParams;

	/** Number of accepted proposals per number of parameters. */
	protected ArrayList<Integer> noOfRejectedByNoOfParams;
	
	/**
	 * Constructor.
	 * @param iter the iteration to which the fine-grained acceptance ratio refers.
	 * @param noOfWindows the number of intervals in which to track acceptance ratios.
	 */
	public FineProposerStatistics(Iteration iter, int noOfWindows) {
		super();
		if (noOfWindows < 0) {
			throw new IllegalArgumentException("Must have non-negative number of windows for fine proposer statistics.");
		}
		this.noOfAcceptedPerWindow = new int[noOfWindows];
		this.noOfRejectedPerWindow = new int[noOfWindows];
		this.currentWindow = 0;
		this.noOfAcceptedByNoOfParams = new ArrayList<Integer>(8);
		this.noOfRejectedByNoOfParams = new ArrayList<Integer>(8);
		iter.addIterationListener(this);
	}
	
	@Override
	public void wasIncremented(Iteration iter) {
		int sz = noOfAcceptedPerWindow.length;
		this.currentWindow = Math.min((iter.getIteration() / iter.getTotalNoOfIterations()) * sz, sz - 1);
	}
	
	@Override
	public void increment(boolean wasAccepted, Proposal proposal) {
		super.increment(wasAccepted, proposal);
		ArrayList<Integer> al;
		if (wasAccepted) {
			this.noOfAcceptedPerWindow[this.currentWindow]++;
			al = this.noOfAcceptedByNoOfParams;
		} else {
			this.noOfRejectedPerWindow[this.currentWindow]++;
			al = this.noOfRejectedByNoOfParams;
		}
		int noOfParams = proposal.getNoOfPerturbedParameters();
		while (noOfParams >= al.size()) {
			al.add(new Integer(0));
		}
		al.set(noOfParams, al.get(noOfParams));
	}
	
	/**
	 * Returns the number of windows, i.e., "buckets".
	 * @return the number of windows.
	 */
	public int getNoOfWindows() {
		return this.noOfAcceptedPerWindow.length;
	}
	
	/**
	 * Returns the acceptance ratio for a specific window (with window 0
	 * referring to the window at iteration 0 and so forth).
	 * @param window the window.
	 * @return the acceptance ratio in that window.
	 */
	public double getAcceptanceRatio(int window) {
		return (this.noOfAcceptedPerWindow[window] / (double) (this.noOfAcceptedPerWindow[window] +
				this.noOfRejectedPerWindow[window]));
	}
	
	/**
	 * Returns the number of accepted proposals for a certain window.
	 * @param window the window.
	 * @return the number of accepted proposals.
	 */
	public int getNoOfAcceptedProposals(int window) {
		return this.noOfAcceptedPerWindow[window];
	}
	
	/**
	 * Returns the number of rejected proposals for a certain window.
	 * @param window the window.
	 * @return the number of rejected proposals.
	 */
	public int getNoOfRejectedProposals(int window) {
		return this.noOfRejectedPerWindow[window];
	}
	
	/**
	 * Returns a list of the number of accepted proposals indexed by the number
	 * of perturbed parameters.
	 * @return the number of proposals.
	 */
	public ArrayList<Integer> getNoOfAcceptedProposalsByNoOfParams() {
		return this.noOfAcceptedByNoOfParams;
	}
	
	/**
	 * Returns a list of the number of rejected proposals indexed by the number
	 * of perturbed parameters.
	 * @return the number of proposals.
	 */
	public ArrayList<Integer> getNoOfRejectedProposalsByNoOfParams() {
		return this.noOfRejectedByNoOfParams;
	}
}
