package se.cbb.jprime.mcmc;

/**
 * Represents a simple fixed tuning parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class ConstantTuningParameter implements TuningParameter {

	/** The value. */
	protected double value;

	/**
	 * Constructor.
	 * @param value the value.
	 */
	public ConstantTuningParameter(double value) {
		this.value = value;
	}
	
	@Override
	public double getValue() {
		return this.value;
	}

	@Override
	public double getMinValue() {
		return this.value;
	}

	@Override
	public double getMaxValue() {
		return this.value;
	}

}
