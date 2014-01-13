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

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("CONSTANT TUNING PARAMETER\n");
		sb.append(prefix).append("Value: ").append(this.value).append('\n');
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		return (prefix + "CONSTANT TUNING PARAMETER\n");
	}

}
