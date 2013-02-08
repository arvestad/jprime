package se.cbb.jprime.topology;

import java.util.Arrays;

import se.cbb.jprime.io.SampleBooleanArray;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds a boolean for each vertex of a graph. No generics for the sake of speed.
 * See also <code>GenericMap</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanMap implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected boolean[] values;
	
	/** Cache vertices. */
	protected int[] cacheVertices = null;
	
	/** Cache values for affected vertices. */
	protected boolean[] cacheValues = null;
	
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
	
	/**
	 * Copy constructor.
	 * @param map the map to be copied.
	 */
	public BooleanMap(BooleanMap map) {
		this.name = map.name;
		this.values = new boolean[map.values.length];
		System.arraycopy(map.values, 0, this.values, 0, this.values.length);
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
	 * Caches a part of or the whole current map. May e.g. be used by a <code>Proposer</code>.
	 * @param vertices the vertices. Null will cache all values.
	 */
	public void cache(int[] vertices) {
		if (vertices == null) {
			this.cacheValues = new boolean[this.values.length];
			System.arraycopy(this.values, 0, this.cacheValues, 0, this.values.length);
		} else {
			this.cacheVertices = new int[vertices.length];
			System.arraycopy(vertices, 0, this.cacheVertices, 0, vertices.length);
			this.cacheValues = new boolean[vertices.length];
			for (int i = 0; i < vertices.length; ++i) {
				this.cacheValues[i] = this.values[vertices[i]];
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
	 * If there is no cache, nothing will happen and the current values remain.
	 * May e.g. be used by a <code>Proposer</code>.
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
	public int getNoOfSubParameters() {
		return this.values.length;
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
	public String getSampleValue(SamplingMode mode) {
		return SampleBooleanArray.toString(this.values);
	}

	@Override
	public int getSize() {
		return this.values.length;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.values);
	}
	
}
