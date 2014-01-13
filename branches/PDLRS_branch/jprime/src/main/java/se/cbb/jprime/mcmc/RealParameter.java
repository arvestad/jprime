package se.cbb.jprime.mcmc;

/**
 * Simple interface for real-valued state parameters.
 * Enables parameters with only one sub-parameter and those with several (arrays)
 * to be treated similarly.
 * 
 * @author Joel Sjöstrand.
 */
public interface RealParameter extends StateParameter {

	/**
	 * Returns the value of a certain sub-parameter.
	 * @param idx the index of the sub-parameter.
	 * @return the sub-parameter's value.
	 */
	public double getValue(int idx);
	
	/**
	 * Sets the value of a certain sub-parameter.
	 * @param idx the index of the sub-parameter.
	 * @param value the new value.
	 */
	public void setValue(int idx, double value);
	
	/**
	 * Caches a part of or the whole current parameter. May e.g. be used by a <code>Proposer</code>.
	 * @param indices the indices of sub-parameters to cache. Null will cache all values.
	 */
	public void cache(int[] indices);
	
	/**
	 * Clears the cached parameter. May e.g. be used by a <code>Proposer</code>.
	 */
	public void clearCache();
	
	/**
	 * Replaces the current parameter with the cached parameter, and clears the latter.
	 * If there is no cache, nothing will happen and the current values remain.
	 * May e.g. be used by a <code>Proposer</code>.
	 */
	public void restoreCache();
}
