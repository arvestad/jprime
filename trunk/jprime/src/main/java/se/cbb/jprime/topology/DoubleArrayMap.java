package se.cbb.jprime.topology;

import se.cbb.jprime.io.SampleDoubleArrayArray;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds a double array for each vertex of a graph. No generics for the sake of speed.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleArrayMap implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected double[][] values;
	
	/** Cache. */
	protected double[][] cache = null;

	/** Details the current change. Set by a Proposer. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor. Initialises all map values to a null array.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public DoubleArrayMap(String name, int size) {
		this.name = name;
		this.values = new double[size][];
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number in the first dimension.
	 */
	public DoubleArrayMap(String name, double[][] vals) {
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
		return this.values[x];
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = (double[]) value;
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public double[] get(int x) {
		return this.values[x];
	}
	
	/**
	 * Returns the element of the array of a vertex.
	 * No bounds checking.
	 * @param x the vertex.
	 * @param i the index in the array of the vertex.
	 * @return the value.
	 */
	public double get(int x, int i) {
		return this.values[x][i];
	}
	
	/**
	 * Sets the element of a vertex.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, double[] val) {
		this.values[x] = val;
	}
	
	/**
	 * Sets the element of the array of a vertex.
	 * No bounds checking.
	 * @param x the vertex.
	 * @param i the index in the array of the vertex.
	 * @param val the value.
	 */
	public void set(int x, int i, double val) {
		this.values[x][i] = val;
	}

	@Override
	public int getNoOfSubParameters() {
		int cnt = 0;
		for (double[] vec : this.values) {
			cnt += (vec == null ? 0 : vec.length);
		}
		return cnt;
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
		this.cache = new double[this.values.length][];
		for (int i = 0; i < this.values.length; ++i) {
			this.cache[i] = new double[this.values[i].length];
			System.arraycopy(this.values[i], 0, this.cache[i], 0, this.values[i].length);
		}
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
		return SampleDoubleArrayArray.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue() {
		return SampleDoubleArrayArray.toString(this.values);
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
