package se.cbb.jprime.topology;

import se.cbb.jprime.io.SampleBooleanArray;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds a boolean for each vertex of a graph. No generics for the sake of speed.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanMap implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected boolean[] values;
	
	/** Cache. */
	protected boolean[] cache = null;

	/** Details the current change. Set by a Proposer. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public BooleanMap(String name, int size, boolean defaultVal) {
		this.name = name;
		this.values = new boolean[size];
		for (int i = 0; i < this.values.length; ++i) {
			values[i] = defaultVal;
		}
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 */
	public BooleanMap(String name, boolean[] vals) {
		this.name = name;
		this.values = vals;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Object getAsObject(int x) {
		return new Boolean(this.values[x]);
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = ((Boolean) value).booleanValue();
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public boolean get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex/arc.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, boolean val) {
		this.values[x] = val;
	}

	/**
	 * Caches the whole current map. May e.g. be used by a <code>Proposer</code>.
	 */
	public void cache() {
		this.cache = new boolean[this.values.length];
		System.arraycopy(this.values, 0, this.cache, 0, this.values.length);
	}

	/**
	 * Clears the cached map and change info. May e.g. be used by a <code>Proposer</code>.
	 */
	public void clearCache() {
		this.cache = null;
		this.changeInfo = null;
	}

	/**
	 * Replaces the current map with the cached map, and clears the latter and the change info.
	 * May e.g. be used by a <code>Proposer</code>.
	 */
	public void restoreCache() {
		this.values = this.cache;
		this.cache = null;
		this.changeInfo = null;
	}

	@Override
	public int getNoOfSubParameters() {
		return this.values.length;
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
	public Class<?> getSampleType() {
		return SampleBooleanArray.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue() {
		return SampleBooleanArray.toString(this.values);
	}

	@Override
	public int getSize() {
		return this.values.length;
	}

	@Override
	public Dependent[] getParentDependents() {
		return null;
	}
}
