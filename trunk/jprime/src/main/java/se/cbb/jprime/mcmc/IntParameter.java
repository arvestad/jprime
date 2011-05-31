package se.cbb.jprime.mcmc;

import java.util.Set;
import java.util.TreeSet;

/**
 * Holds an integer state parameter.
 * 
 * @author Joel Sjöstrand.
 */
public class IntParameter implements StateParameter {

	/** Name. */
	protected String name;
	
	/** Current state. */
	protected int value;

	/** Dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Cache. */
	protected Integer cache;
	
	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor.
	 * @param name parameter's name.
	 * @param initVal initial value.
	 */
	public IntParameter(String name, int initVal) {
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
		this.cache = new Integer(this.value);
	}

	@Override
	public void update(boolean willSample) {
		if (this.value != this.cache.intValue()) {
			this.changeInfo = new ChangeInfo(this, "Proposed: " + this.value + ", old: " + this.cache.intValue());
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.cache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.value = this.cache.intValue();
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
		return SampleInteger.toString(this.value);
	}

	@Override
	public SampleType getSampleType() {
		return new SampleInteger();
	}

}
