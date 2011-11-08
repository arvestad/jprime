package se.cbb.jprime.mcmc;

/**
 * Represents a <code>TuningParameter</code> which increases or decreases linearly
 * with the iteration number.
 * 
 * @author Joel Sjöstrand.
 */
public class LinearTuningParameter implements TuningParameter, IterationListener {

	/** Iteration. */
	protected Iteration iter;
	
	/** The value at iteration 0. */
	protected double startValue;
	
	/** The value at the last iteration. */
	protected double endValue;
	
	/** The current weight. */
	protected double value;
	
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
		this.iter = iter;
		this.startValue = startValue;
		this.endValue = endValue;
		this.value = startValue;
		this.iter.addIterationListener(this);
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
	public void incrementPerformed(int iterCurr, int iterTotal) {
		this.value = this.startValue + (this.endValue - this.startValue) *
			(iterCurr / ((double) iterTotal));
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append("LINEAR TUNING PARAMETER\n");
		sb.append("Value range: ").append(this.startValue).append(" to ").append(this.endValue).append("\n");
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		return null;
	}

}
