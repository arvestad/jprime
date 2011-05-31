package se.cbb.jprime.mcmc;

import java.util.Set;
import java.util.TreeSet;

/**
 * Holds a boolean state parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanParameter implements StateParameter {

	/** Name. */
	protected String name;
	
	/** Current state. */
	protected boolean value;

	/** Child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Cache. */
	protected Boolean cache;
	
	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor.
	 * @param name parameter's name.
	 * @param initVal initial value.
	 */
	public BooleanParameter(String name, boolean initVal) {
		this.name = name;
		this.value = initVal;
		this.dependents = new TreeSet<Dependent>();
		this.cache = null;
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
		this.cache = new Boolean(this.value);
	}

	@Override
	public void update(boolean willSample) {
		if (this.value != this.cache.booleanValue()) {
			this.changeInfo = new ChangeInfo(this, "Proposed: " + this.value + ", old: " + !this.value);
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.cache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.value = this.cache.booleanValue();
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
		return SampleBoolean.toString(this.value);
	}

	@Override
	public SampleType getSampleType() {
		return new SampleBoolean();
	}

}
