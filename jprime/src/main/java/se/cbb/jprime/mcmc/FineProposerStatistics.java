package se.cbb.jprime.mcmc;

import java.util.ArrayList;

import se.cbb.jprime.misc.Pair;

/**
 * Extends the proposer statistics by being able to track acceptance
 * ratios in more detail over time. This is done by dividing the
 * iteration span in a set of (approximately) equally sized "windows".
 * <p/>
 * In addition, it tracks the distribution of accepted/rejected proposals
 * w.r.t. the number of perturbed parameters at each iteration.
 * 
 * @author Joel Sjöstrand.
 */
public class FineProposerStatistics extends ProposerStatistics implements IterationListener {
	
	/** Iteration. */
	protected Iteration iter;
	
	/** Number of accepted proposals in each window. */
	protected int[] noOfAcceptedPerWindow;
	
	/** Number of rejected proposals in each window. */
	protected int[] noOfRejectedPerWindow;
	
	/** The current window (i.e. "bucket"). */
	protected int currentWindow;
	
	/** Number of accepted and rejected proposals per number of parameters. */
	protected ArrayList<Pair<Integer,Integer>> noOfAccRejByNoOfParams;
	
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
		this.iter = iter;
		this.noOfAcceptedPerWindow = new int[noOfWindows];
		this.noOfRejectedPerWindow = new int[noOfWindows];
		this.currentWindow = 0;
		this.noOfAccRejByNoOfParams = new ArrayList<Pair<Integer,Integer>>(8);
		this.iter.addIterationListener(this);
	}
	
	@Override
	public void incrementPerformed(int iterValue) {
		int sz = noOfAcceptedPerWindow.length;
		this.currentWindow = Math.min((iterValue / this.iter.getTotalNoOfIterations()) * sz, sz - 1);
	}
	
	@Override
	public void increment(boolean wasAccepted, Proposal proposal) {
		super.increment(wasAccepted, proposal);
		if (wasAccepted) {
			this.noOfAcceptedPerWindow[this.currentWindow]++;
		} else {
			this.noOfRejectedPerWindow[this.currentWindow]++;
		}
		int noOfParams = proposal.getNoOfPerturbedParameters();
		while (noOfParams >= this.noOfAccRejByNoOfParams.size()) {
			this.noOfAccRejByNoOfParams.add(new Pair<Integer,Integer>(0,0));
		}
		Pair<Integer,Integer> ar = this.noOfAccRejByNoOfParams.get(noOfParams);
		ar = (wasAccepted ? new Pair<Integer,Integer>(ar.first + 1, ar.second) : new Pair<Integer, Integer>(ar.first, ar.second + 1));
		this.noOfAccRejByNoOfParams.set(noOfParams, ar);
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
	 * Returns the number of accepted proposals indexed by the number
	 * of perturbed parameters.
	 * @param noOfParams the number of parameters.
	 * @return the number of accepted proposals.
	 */
	public int getNoOfAcceptedProposalsByNoOfParams(int noOfParams) {
		return this.noOfAccRejByNoOfParams.get(noOfParams).first;
	}
	
	/**
	 * Returns the number of rejected proposals indexed by the number
	 * of perturbed parameters.
	 * @param noOfParams the number of parameters.
	 * @return the number of rejected proposals.
	 */
	public int getNoOfRejectedProposalsByNoOfParams(int noOfParams) {
		return this.noOfAccRejByNoOfParams.get(noOfParams).second;
	}
	
	/**
	 * Returns the acceptance ratio of the subset of proposals which concern a certain number of
	 * perturbed parameters.
	 * @param noOfParams the number of parameters.
	 * @return the acceptance ratio.
	 */
	public double getAcceptanceRatioByNoOfParams(int noOfParams) {
		Pair<Integer,Integer> p = this.noOfAccRejByNoOfParams.get(noOfParams);
		return (p.first / (double) (p.first + p.second));
	}
	
	@Override
	public String getPreInfo(String prefix) {
		return null;
	}
	
	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("FINE-DETAILED PROPOSER STATISTICS\n");
		sb.append(prefix).append("Acceptance ratio: ").append(this.noOfAccepted).append(" / ").append(this.getNoOfProposals()).append(" = ").append(this.getAcceptanceRatio()).append("\n");
		sb.append(prefix).append("Acceptance ratios per window:\n");
		String prefixt = prefix + '\t';
		for (int i = 0; i < this.noOfAcceptedPerWindow.length; ++i) {
			int a = this.noOfAcceptedPerWindow[i];
			int r = this.noOfRejectedPerWindow[i];
			sb.append(prefixt).append(i + 1).append('\t').append(a).append(" / ").append(a + r).append(" = ").append(a / (double) (a + r)).append("\n");
		}
		if (this.noOfAccRejByNoOfParams.size() > 1) {
			sb.append(prefix).append("Acceptance ratio per number of perturbed parameters:\n");
			for (int i = 0; i < this.noOfAccRejByNoOfParams.size(); ++i) {
				Pair<Integer,Integer> p = this.noOfAccRejByNoOfParams.get(i);
				sb.append(prefixt).append(i).append('\t').append(p.first).append(" / ").append(p.first + p.second).append(" = ").append(p.first / (double) (p.first + p.second)).append("\n");
			}
		}
		return sb.toString();
	}
}
