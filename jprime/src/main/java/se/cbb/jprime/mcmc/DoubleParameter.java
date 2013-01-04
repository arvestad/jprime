package se.cbb.jprime.mcmc;

import se.cbb.jprime.io.SampleDouble;
import se.cbb.jprime.math.ScaleTransformation;

/**
 * Holds a floating point state parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleParameter implements RealParameter {

	/** Name. */
	protected String name;
	
	/** Current state. */
	protected double value;
	
	/** Cache. */
	protected double cache;
	
	/** Scale according to which internal value as been transformed. Null if not used. */
	protected ScaleTransformation scale;
	
	/**
	 * Constructor.
	 * @param name parameter's name.
	 * @param initVal initial value.
	 */
	public DoubleParameter(String name, double initVal) {
		this.name = name;
		this.value = initVal;
		this.cache = Double.NaN;
	}
	
	/**
	 * Constructor. Creates a state parameter where the value is held scaled
	 * internally according to some scale factor, but unscaled when sampling.
	 * @param name the parameter's name.
	 * @param scale the scale factor.
	 * @param initVal the initial value, (in scaled form).
	 */
	public DoubleParameter(String name, ScaleTransformation scale, double initVal) {
		this(name, initVal);
		this.scale = scale;
	}

	/**
	 * Caches the current value. May e.g. be used by a <code>Proposer</code>.
	 * @param indices is of no importance.
	 */
	@Override
	public void cache(int[] indices) {
		this.cache = this.value;
	}

	@Override
	public void clearCache() {
		this.cache = Double.NaN;
	}

	@Override
	public void restoreCache() {
		this.value = this.cache;
		this.cache = Double.NaN;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getNoOfSubParameters() {
		return 1;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue(SamplingMode mode) {
		return (this.scale == null ? SampleDouble.toString(this.value) :
			SampleDouble.toString(this.scale.getUnscaled(this.value)));
	}

	@Override
	public Class<?> getSampleType() {
		return SampleDouble.class;
	}

	@Override
	public double getValue(int idx) {
		// No bounds checking for the sake of speed.
		return this.value;
	}

	@Override
	public void setValue(int idx, double value) {
		// No bounds checking for the sake of speed.
		this.value = value;
	}
	
	/**
	 * Returns this parameter's current value.
	 * @return the value.
	 */
	public double getValue() {
		return this.value;
	}

	/**
	 * Sets this parameter's current value.
	 * @param value the new value.
	 */
	public void setValue(double value) {
		this.value = value;
	}

}
