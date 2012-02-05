package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;

import org.jfree.util.PublicCloneable;

import se.cbb.jprime.mcmc.StateParameter;

/**
 * Holds an object for each vertex of a graph.
 * For caching purposes, there is, however, a requirement that the 
 * stored type <b>must implement <code>PublicCloneable</code> properly.</b>
 * <p/>
 * If sampling from this class, <code>getSampleType()</code> will return the stored
 * type (which should therefore typically implement <code>SampleType</code>),
 * and <code>getSampleValue()</code> will return a list [v0.toString(),...,vk.toString()]
 * for the values v0,...,vk.
 * 
 * @author Joel Sjöstrand.
 */
public class GenericMap<T extends PublicCloneable> implements GraphMap, StateParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected ArrayList<T> values;
	
	/** Cache vertices. */
	protected int[] cacheVertices = null;
	
	/** Cache values for affected vertices. */
	protected ArrayList<T> cacheValues = null;
	
	/**
	 * Constructor. Initialises all map values to null.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public GenericMap(String name, int size) {
		this.name = name;
		this.values = new ArrayList<T>(size);
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public GenericMap(String name, int size, T defaultVal) {
		this.name = name;
		this.values = new ArrayList<T>(size);
		for (int i = 0; i < size; ++i) {
			this.values.set(i, defaultVal);
		}
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 *             The internal list is copied element-wise from input.
	 */
	public GenericMap(String name, List<T> vals) {
		this.name = name;
		this.values = new ArrayList<T>(vals);
	}
	
	/**
	 * Copy constructor.
	 * @param map the map to be copied.
	 */
	public GenericMap(GenericMap<T> map) {
		this.name = map.name;
		this.values = new ArrayList<T>(map.values);
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
		return this.values.get(x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setAsObject(int x, Object value) {
		this.values.set(x, (T) value);
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public T get(int x) {
		return this.values.get(x);
	}
	
	/**
	 * Sets the element of a vertex.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, T val) {
		this.values.set(x, val);
	}

	@Override
	public int getNoOfSubParameters() {
		return this.values.size();
	}

	/**
	 * Caches a part of or the whole current map. May e.g. be used by a <code>Proposer</code>.
	 * Note that this will fail if the stored type does not implement <code>PublicCloneable</code> properly.
	 * @param vertices the vertices. Null will cache all values.
	 * @throws CloneNotSupportedException.
	 */
	@SuppressWarnings("unchecked")
	public void cache(int[] vertices) throws CloneNotSupportedException {
		if (vertices == null) {
			this.cacheValues = new ArrayList<T>(this.values.size());
			// Clone objects.
			for (T val  : this.values) {
				this.cacheValues.add((T) val.clone());
			}
		} else {
			this.cacheVertices = new int[vertices.length];
			System.arraycopy(vertices, 0, this.cacheVertices, 0, vertices.length);
			this.cacheValues = new ArrayList<T>(vertices.length);
			for (int i = 0; i < vertices.length; ++i) {
				this.cacheValues.add((T) this.values.get(vertices[i]).clone());
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
				this.values.set(this.cacheVertices[i], this.cacheValues.get(i));
			}
			this.cacheVertices = null;
			this.cacheValues = null;
		}
	}

	@Override
	public Class<?> getSampleType() {
		return (this.values.get(0).getClass());
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue() {
		StringBuilder sb = new StringBuilder(this.values.size() * 128);
		sb.append('[');
		for (T val : this.values) {
			sb.append(val.toString());
			sb.append(",");
		}
		sb.setCharAt(sb.length()-1, ']');
		return sb.toString();
	}

	@Override
	public int getSize() {
		return this.values.size();
	}

}
