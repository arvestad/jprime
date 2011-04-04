package se.cbb.jprime.mcmc;

/**
 * Represents a <code>TuningParameter</code> which increases or decreases linearly
 * with the iteration number.
 * 
 * @author Joel Sjöstrand.
 */
public class LinearTuningParameter implements TuningParameter, IterationListener {

	/** The value at iteration 0. */
	private double startValue;
	
	/** The value at the last iteration. */
	private double endValue;
	
	/** The current weight. */
	private double value;
	
	/**
	 * Constructor.
	 * @param iter the iteration object on which the current value is based.
	 * @param startValue value at iteration 0.
	 * @param endValue value at the last iteration.
	 */
	public LinearTuningParameter(Iteration iter, double startValue, double endValue) {
		if (iter == null) {
			throw new IllegalArgumentException("Iteration object for linear proposer weight must not be null.");
		}
		if (Double.isInfinite(startValue) || Double.isInfinite(endValue)) {
			throw new IllegalArgumentException("Start or end weight for linear tuning parameter out-of-range.");
		}
		iter.addIterationListener(this);
		this.startValue = startValue;
		this.endValue = endValue;
		this.value = startValue;
	}
	
	@Override
	public double getValue() {
		return this.value;
	}

	@Override
	public double getMinValue() {
		return Math.min(this.startValue, this.endValue);
	}

	@Override
	public double getMaxValue() {
		return Math.max(this.startValue, this.endValue);
	}

	@Override
	public void wasIncremented(Iteration iter) {
		this.value = this.startValue + (this.endValue - this.startValue) *
			(iter.getIteration() / ((double) iter.getTotalNoOfIterations()));
	}

}
