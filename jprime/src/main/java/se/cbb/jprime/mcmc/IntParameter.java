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
	protected int cache;
	
	/**
	 * Constructor.
	 * @param name parameter's name.
	 * @param initVal initial value.
	 */
	public IntParameter(String name, int initVal) {
		this.name = name;
		this.value = initVal;
		this.cache = -1;
	}

	/**
	 * Caches the current value. May e.g. be used by a <code>Proposer</code>.
	 */
	public void cache() {
		this.cache = this.value;
	}

	/**
	 * Clears the cached value. May e.g. be used by a <code>Proposer</code>.
	 */
	public void clearCache() {
		this.cache = -1;
	}

	/**
	 * Replaces the current value with the cached value, and clears the latter.
	 * May e.g. be used by a <code>Proposer</code>.
	 */
	public void restoreCache(boolean willSample) {
		this.value = this.cache;
		this.cache = -1;
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
		return SampleInt.toString(this.value);
	}

	@Override
	public Class<?> getSampleType() {
		return SampleInt.class;
	}

}
