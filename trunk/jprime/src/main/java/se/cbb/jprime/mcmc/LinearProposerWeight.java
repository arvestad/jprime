package se.cbb.jprime.mcmc;

/**
 * Represents a <code>ProposerWeight</code> which increases or decreases linearly
 * with the iteration number.
 * 
 * @author Joel Sjöstrand.
 */
public class LinearProposerWeight extends LinearTuningParameter implements ProposerWeight {
	
	/**
	 * Constructor.
	 * @param iter the iteration object on which the current weight is based.
	 * @param startWeight weight at iteration 0. Must be >= 0.
	 * @param endWeight weight at the last iteration. Must be >= 0.
	 */
	public LinearProposerWeight(Iteration iter, double startWeight, double endWeight) {
		super(iter, startWeight, endWeight);
		if (startWeight < 0.0 || endWeight < 0.0) {
			throw new IllegalArgumentException("Start or end value for proposer weight out-of-range.");
		}
	}
	
	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("LINEAR PROPOSER WEIGHT\n");
		sb.append(prefix).append("Value range: ").append(this.startValue).append(" to ").append(this.endValue).append("\n");
		return sb.toString();
	}
	
	@Override
	public String getPostInfo(String prefix) {
		return null;
	}

}
