package se.cbb.jprime.apps.analysisextraction;
/**
 * Float-type parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class FloatParameter extends Parameter {
	
	/** Posterior mean. */
	public Double mean;
	
	/** Posterior standard deviation. */
	public Double stdDev;
	
	/** Posterior minimum. */
	public Double min;
	
	/** Posterior maximum. */
	public Double max;
	
	/**
	 * Best state w.r.t. probability density. Preferably, refers to all visited
	 * states rather than sampled states.
	 */
	public Double bestState;
	
	/**
	 * Constructor.
	 * @param name the parameter name.
	 * @param type the parameter type, e.g. 'Tree'.
	 */
	public FloatParameter(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "float";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.mean != null)
			sb.append(this.name + " mean: " + this.mean + '\n');
		if (this.stdDev != null)
			sb.append(this.name + " std dev: " + this.stdDev + '\n');
		if (this.min != null)
			sb.append(this.name + " min: " + this.min + '\n');
		if (this.max != null)
			sb.append(this.name + " max: " + this.max + '\n');
		if (this.bestState != null)
			sb.append(this.name + " best state: " + this.bestState + '\n');
		return sb.toString();
	}
}
