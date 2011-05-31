package se.cbb.jprime.mcmc;

import java.util.Set;
import java.util.TreeSet;

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

	/** Child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Cache. */
	protected Double cache;
	
	/** Scale according to which internal value as been transformed. Null if not used. */
	protected ScaleTransformation scale;
	
	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor.
	 * @param name parameter's name.
	 * @param initVal initial value.
	 */
	public DoubleParameter(String name, double initVal) {
		this.name = name;
		this.value = initVal;
		this.dependents = new TreeSet<Dependent>();
		this.cache = null;
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
	
	@Override
	public boolean isDependentSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public Set<Dependent> getChildDependents() {
		return this.dependents;
	}

	@Override
	public void cache(boolean willSample) {
		this.cache = new Double(this.value);
	}

	@Override
	public void update(boolean willSample) {
		if (Math.abs(this.value - this.cache.doubleValue()) > 1e-20) {
			this.changeInfo = new ChangeInfo(this, "Proposed: " + this.value + ", old: " + this.cache.doubleValue());
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.cache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.value = this.cache.doubleValue();
		this.cache = null;
		this.changeInfo = null;
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}
	
	@Override
	public void setChangeInfo(ChangeInfo info) {
		// We don't really care since we can find out ourselves...
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
	public String getSampleValue() {
		return (this.scale == null ? SampleDouble.toString(this.value) :
			SampleDouble.toString(this.scale.getUnscaled(this.value)));
	}

	@Override
	public SampleType getSampleType() {
		return new SampleDouble();
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
