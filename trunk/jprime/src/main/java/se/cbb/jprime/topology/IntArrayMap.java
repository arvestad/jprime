package se.cbb.jprime.topology;

import se.cbb.jprime.io.SampleIntArrayArray;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds an int array for each vertex of a graph. No generics for the sake of speed.
 * See also <code>GenericMap</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class IntArrayMap implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected int[][] values;
	
	/** Cache vertices. */
	protected int[] cacheVertices = null;
	
	/** Cache values for affected vertices. */
	protected int[][] cacheValues = null;
	
	/**
	 * Constructor. Initialises all map values to a null array.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public IntArrayMap(String name, int size) {
		this.name = name;
		this.values = new int[size][];
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number in the first dimension.
	 */
	public IntArrayMap(String name, int[][] vals) {
		this.name = name;
		this.values = vals;
	}
	
	/**
	 * Copy constructor.
	 * @param map the map to be copied.
	 */
	public IntArrayMap(IntArrayMap map) {
		this.name = map.name;
		this.values = new int[map.values.length][];
		for (int i = 0; i < this.values.length; ++i) {
			if (map.values[i] != null) {
				this.values[i] = new int[map.values[i].length];
				System.arraycopy(map.values[i], 0, this.values[i], 0, this.values[i].length);
			}
		}
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
		this.values[x] = (int[]) value;
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public int[] get(int x) {
		return this.values[x];
	}
	
	/**
	 * Returns the element of the array of a vertex.
	 * No bounds checking.
	 * @param x the vertex.
	 * @param i the index in the array of the vertex.
	 * @return the value.
	 */
	public int get(int x, int i) {
		return this.values[x][i];
	}
	
	/**
	 * Sets the element of a vertex.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, int[] val) {
		this.values[x] = val;
	}
	
	/**
	 * Sets the element of the array of a vertex.
	 * No bounds checking.
	 * @param x the vertex.
	 * @param i the index in the array of the vertex.
	 * @param val the value.
	 */
	public void set(int x, int i, int val) {
		this.values[x][i] = val;
	}

	@Override
	public int getNoOfSubParameters() {
		int cnt = 0;
		for (int[] vec : this.values) {
			cnt += (vec == null ? 0 : vec.length);
		}
		return cnt;
	}

	/**
	 * Caches a part of or the whole current map. May e.g. be used by a <code>Proposer</code>.
	 * @param vertices the vertices. Null will cache all values.
	 */
	public void cache(int[] vertices) {
		if (vertices == null) {
			this.cacheValues = new int[this.values.length][];
			for (int i = 0; i < this.values.length; ++i) {
				if (this.values[i] != null) {
					this.cacheValues[i] = new int[this.values[i].length];
					System.arraycopy(this.values[i], 0, this.cacheValues[i], 0, this.values[i].length);
				}
			}
		} else {
			this.cacheVertices = new int[vertices.length];
			System.arraycopy(vertices, 0, this.cacheVertices, 0, vertices.length);
			this.cacheValues = new int[vertices.length][];
			for (int i = 0; i < vertices.length; ++i) {
				if (this.values[vertices[i]] != null) {
					this.cacheValues[i] = new int[this.values[vertices[i]].length];
					System.arraycopy(this.values[vertices[i]], 0, this.cacheValues[i], 0, this.cacheValues[i].length);
				}
			}
		}
	}

	/**
	 * Clears the cached map. May e.g. be used by a <code>Proposer</code>.
	 */
	public void clearCache() {
		this.cacheVertices = null;
		this.cacheValues = null;
	}

	/**
	 * Replaces the current map with the cached map, and clears the latter.
	 * May e.g. be used by a <code>Proposer</code>.
	 * If there is no cache, nothing will happen and the current values remain.
	 */
	public void restoreCache() {
		if (this.cacheValues == null) {
			return;
		}
		if (this.cacheVertices == null) {
			this.values = this.cacheValues;
			this.cacheValues = null;
		} else {
			for (int i = 0; i < this.cacheVertices.length; ++i) {
				this.values[this.cacheVertices[i]] = this.cacheValues[i];
			}
			this.cacheVertices = null;
			this.cacheValues = null;
		}
	}

	@Override
	public Class<?> getSampleType() {
		return SampleIntArrayArray.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue(SamplingMode mode) {
		return SampleIntArrayArray.toString(this.values);
	}

	@Override
	public int getSize() {
		return this.values.length;
	}

}
