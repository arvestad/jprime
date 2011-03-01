package se.cbb.jprime.mcmc;

/**
 * Extends the proposer statistics by being able to track acceptance
 * ratios in more detail over time. This is done by dividing the
 * iteration span in a set of (approximately) equidistant "windows".
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
		iter.addIterationListener(this);
	}
	
	@Override
	public void wasIncremented(Iteration iter) {
		int sz = noOfAcceptedPerWindow.length;
		this.currentWindow = Math.min((iter.getIteration() / iter.getTotalNoOfIterations()) * sz, sz - 1);
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
}
