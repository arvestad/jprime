package se.cbb.jprime.mcmc;

/**
 * Implementation of an constant thinner, i.e., where every j-th
 * iteration should be sampled for some invariant j. The starting
 * iteration 0 is also naturally sampled.
 * 
 * @author Joel Sjöstrand.
 */
public class ConstantThinner implements Thinner, IterationListener {

	/** Dictates how often sampling should be performed. */
	private int factor;
	
	/** Underlying iteration. */
	private Iteration iter;
	
	/** Flag for if sampling should occur or not. */
	private boolean doSample;
	
	/**
	 * Constructor.
	 * @param iter the iteration on which sampling is based.
	 * @param factor how often to sample, i.e. every factor-th iteration.
	 */
	public ConstantThinner(Iteration iter, int factor) {
		if (factor <= 0) {
			throw new IllegalArgumentException("Cannot sample using non-positive thinning factor.");
		}
		this.factor = factor;
		this.iter = iter;
		this.doSample = (iter.getIteration() % factor == 0);
		iter.addIterationListener(this);
	}
	
	/**
	 * Returns a prediction of the total number of samples based on
	 * the underlying iteration starting at 0.
	 * @return the total number of predicted samples.
	 */
	public int getTotalNoOfSamples() {
		return (this.iter.getTotalNoOfIterations() / this.factor) + 1;
	}
	
	@Override
	public boolean doSample() {
		return this.doSample;
	}

	@Override
	public void incrementPerformed(Iteration iter) {
		this.doSample = (iter.getIteration() % factor == 0);
	}

}
