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
		// Notify kids if there was a change.
		if (this.value != this.cache.intValue()) {
			ChangeInfo info = new ChangeInfo(this);
			for (Dependent dep : this.dependents) {
				dep.addParentChangeInfo(info, willSample);
			}
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.cache = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.value = this.cache.intValue();
		this.cache = null;
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

	@Override
	public void addParentChangeInfo(ChangeInfo info, boolean willSample) {
		throw new UnsupportedOperationException("IntParameter cannot have parent dependents.");
	}

}
