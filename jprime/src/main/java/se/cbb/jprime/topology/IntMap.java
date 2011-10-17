package se.cbb.jprime.topology;

import se.cbb.jprime.io.SampleIntArray;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds an int for each vertex of a graph. No generics for the sake of speed.
 * 
 * @author Joel Sjöstrand.
 */
public class IntMap implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected int[] values;
	
	/** Cache. */
	protected int[] cache = null;

	/** Details the current change. Set by a Proposer. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor. Initialises all map values to 0.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public IntMap(String name, int size) {
		this.name = name;
		this.values = new int[size];
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public IntMap(String name, int size, int defaultVal) {
		this.name = name;
		this.values = new int[size];
		for (int i = 0; i < this.values.length; ++i) {
			values[i] = defaultVal;
		}
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 */
	public IntMap(String name, int[] vals) {
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
		return new Integer(this.values[x]);
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = ((Integer) value).intValue();
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public int get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, int val) {
		this.values[x] = val;
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

	/**
	 * Caches the whole current map. May e.g. be used by a <code>Proposer</code>.
	 */
	public void cache() {
		this.cache = new int[this.values.length];
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
	public Class<?> getSampleType() {
		return SampleIntArray.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue() {
		return SampleIntArray.toString(this.values);
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
