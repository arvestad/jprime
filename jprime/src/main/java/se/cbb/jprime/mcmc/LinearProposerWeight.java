package se.cbb.jprime.mcmc;

/**
 * Represents a <code>ProposerWeight</code> which increases or decreases linearly
 * with the iteration number.
 * 
 * @author Joel Sjöstrand.
 */
public class LinearProposerWeight implements ProposerWeight, IterationListener {

	/** The weight at iteration 0. */
	private double startWeight;
	
	/** The weight at the last iteration. */
	private double endWeight;
	
	/** The current weight. */
	private double weight;
	
	/**
	 * Constructor.
	 * @param iter the iteration object on which the current weight is based.
	 * @param startWeight weight at iteration 0. Must be >= 0.
	 * @param endWeight weight at the last iteration. Must be >= 0.
	 */
	public LinearProposerWeight(Iteration iter, double startWeight, double endWeight) {
		if (iter == null) {
			throw new IllegalArgumentException("Iteration object for linear proposer weight must not be null.");
		}
		if (startWeight < 0.0 || endWeight < 0.0) {
			throw new IllegalArgumentException("Start or end weight for proposer out-of-range.");
		}
		iter.addIterationListener(this);
		this.startWeight = startWeight;
		this.endWeight = endWeight;
		this.weight = startWeight;
	}
	
	@Override
	public double getWeight() {
		return this.weight;
	}

	@Override
	public double getMinWeight() {
		return Math.min(this.startWeight, this.endWeight);
	}

	@Override
	public double getMaxWeight() {
		return Math.max(this.startWeight, this.endWeight);
	}

	@Override
	public void wasIncremented(Iteration iter) {
		this.weight = this.startWeight + (this.endWeight - this.startWeight) *
			(iter.getCurrentIteration() / ((double) iter.getTotalNoOfIterations() - 1));
	}

}
