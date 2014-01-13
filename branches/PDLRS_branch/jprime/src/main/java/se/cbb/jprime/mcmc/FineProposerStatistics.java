package se.cbb.jprime.mcmc;

import java.util.Map.Entry;

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
		this.iter.addIterationListener(this);
	}
	
	@Override
	public void incrementPerformed(int iterCurr, int iterTotal) {
		int sz = noOfAcceptedPerWindow.length;
		this.currentWindow = (int) Math.min((iterCurr / (double) iterTotal) * sz, sz - 1);
	}
	
	@Override
	public void increment(boolean wasAccepted) {
		super.increment(wasAccepted);
		if (wasAccepted) {
			this.noOfAcceptedPerWindow[this.currentWindow]++;
		} else {
			this.noOfRejectedPerWindow[this.currentWindow]++;
		}
	}
	
	@Override
	public void increment(boolean wasAccepted, String category) {
		super.increment(wasAccepted, category);
		if (wasAccepted) {
			this.noOfAcceptedPerWindow[this.currentWindow]++;
		} else {
			this.noOfRejectedPerWindow[this.currentWindow]++;
		}
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
	
	@Override
	public String getPreInfo(String prefix) {
		return (prefix + "FINE-DETAILED PROPOSER STATISTICS\n");
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
