package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.math.Scale;

/**
 * Holds a floating point state parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleParameter implements StateParameter {

	/** Name. */
	private String name;
	
	/** Current state. */
	private double value;

	/** Dependents. */
	private ArrayList<Dependent> dependents;
	
	/** Cache. */
	private Double cache;
	
	/** Scale according to which internal value as been transformed. Null if not used. */
	private Scale scale;
	
	/** Defines how the parameter is scaled / unscaled. */
	private Scale.Dependency scaleDep;
	
	/**
	 * Constructor.
	 * @param name parameter's name.
	 * @param initVal initial value.
	 */
	public DoubleParameter(String name, double initVal) {
		this.name = name;
		this.value = initVal;
		this.dependents = new ArrayList<Dependent>();
		this.cache = null;
	}
	
	/**
	 * Constructor. Creates a state parameter where the value is held scaled
	 * internally according to
	 * some scale factor, but unscaled when sampling.
	 * @param name the parameter's name.
	 * @param scale the scale factor.
	 * @param scaleDep the parameter's relationship to the scale factor.
	 * @param initVal the initial value, (in scaled form).
	 */
	public DoubleParameter(String name, Scale scale, Scale.Dependency scaleDep, double initVal) {
		this(name, initVal);
		this.scale = scale;
		this.scaleDep = scaleDep;
	}
	
	@Override
	public boolean isSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public List<Dependent> getDependents() {
		return this.dependents;
	}

	@Override
	public void cache(boolean willSample) {
		this.cache = new Double(this.value);
	}

	@Override
	public void update(boolean willSample) {
	}

	@Override
	public void clearCache(boolean willSample) {
		this.cache = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.value = this.cache.doubleValue();
	}

	@Override
	public PerturbationInfo getPerturbationInfo() {
		return null;
	}

	@Override
	public void setPerturbationInfo(PerturbationInfo info) {
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
		return ("" + this.value);
	}

	@Override
	public void appendSampleHeader(StringBuilder sb) {
		sb.append(this.name);
	}

	@Override
	public void appendSampleValue(StringBuilder sb) {
		sb.append(this.value);
	}

}
