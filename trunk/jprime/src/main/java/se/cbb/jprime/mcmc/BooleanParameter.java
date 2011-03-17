package se.cbb.jprime.mcmc;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a boolean state parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanParameter implements StateParameter {

	/** Name. */
	private String name;
	
	/** Current state. */
	private boolean value;

	/** Dependents. */
	private ArrayList<Dependent> dependents;
	
	/** Cache. */
	private Boolean cache;
	
	/**
	 * Constructor.
	 * @param name parameter's name.
	 * @param initVal initial value.
	 */
	public BooleanParameter(String name, boolean initVal) {
		this.name = name;
		this.value = initVal;
		this.dependents = new ArrayList<Dependent>();
		this.cache = null;
	}
	
	@Override
	public boolean isSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public List<Dependent> getChildDependents() {
		return this.dependents;
	}

	@Override
	public void cache(boolean willSample) {
		this.cache = new Boolean(this.value);
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
		this.value = this.cache.booleanValue();
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
		return SampleBoolean.toString(this.value);
	}

	@Override
	public void appendSampleHeader(StringBuilder sb) {
		sb.append(this.name);
	}

	@Override
	public void appendSampleValue(StringBuilder sb) {
		sb.append(SampleBoolean.toString(this.value));
	}

	@Override
	public SampleType getSampleType() {
		return new SampleBoolean();
	}

	@Override
	public boolean isSource() {
		return true;
	}

	@Override
	public List<Dependent> getParentDependents() {
		return null;
	}

}
