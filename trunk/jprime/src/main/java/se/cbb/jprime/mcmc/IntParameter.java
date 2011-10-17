package se.cbb.jprime.mcmc;

import se.cbb.jprime.io.SampleInt;

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
		this.cache = null;
	}

	/**
	 * Caches the current value. May e.g. be used by a <code>Proposer</code>.
	 */
	public void cache() {
		this.cache = new Integer(this.value);
	}

	/**
	 * Clears the cached value and change info. May e.g. be used by a <code>Proposer</code>.
	 */
	public void clearCache() {
		this.cache = null;
		this.changeInfo = null;
	}

	/**
	 * Replaces the current value with the cached value, and clears the latter and the change info.
	 * May e.g. be used by a <code>Proposer</code>.
	 */
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
		this.changeInfo = info;
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
		return SampleInt.toString(this.value);
	}

	@Override
	public Class<?> getSampleType() {
		return SampleInt.class;
	}

	@Override
	public Dependent[] getParentDependents() {
		return null;
	}

}
