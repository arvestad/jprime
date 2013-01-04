package se.cbb.jprime.topology;

import se.cbb.jprime.io.SampleDoubleArray;
import se.cbb.jprime.mcmc.RealParameter;

/**
 * Holds a double for each vertex of a graph. No generics for the sake of speed.
 * See also <code>GenericMap</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleMap implements GraphMap, RealParameter {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected double[] values;
	
	/** Cache vertices. */
	protected int[] cacheVertices = null;
	
	/** Cache values for affected vertices. */
	protected double[] cacheValues = null;
	
	/**
	 * Constructor. Initialises all map values to 0.0.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public DoubleMap(String name, int size) {
		this.name = name;
		this.values = new double[size];
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public DoubleMap(String name, int size, double defaultVal) {
		this(name, size);
		for (int i = 0; i < this.values.length; ++i) {
			values[i] = defaultVal;
		}
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 */
	public DoubleMap(String name, double[] vals) {
		this.name = name;
		this.values = vals;
	}
	
	/**
	 * Copy constructor.
	 * @param map the map to be copied.
	 */
	public DoubleMap(DoubleMap map) {
		this.name = map.name;
		this.values = new double[map.values.length];
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
		return new Double(this.values[x]);
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = ((Double) value).doubleValue();
	}

	/**
	 * Returns the element of a vertex.
	 * @param x the vertex.
	 * @return the value.
	 */
	public double get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex.
	 * @param x the vertex.
	 * @param val the value.
	 */
	public void set(int x, double val) {
		this.values[x] = val;
	}

	@Override
	public int getNoOfSubParameters() {
		return this.values.length;
	}

	@Override
	public void cache(int[] vertices) {
		if (vertices == null) {
			this.cacheValues = new double[this.values.length];
			System.arraycopy(this.values, 0, this.cacheValues, 0, this.values.length);
		} else {
			this.cacheVertices = new int[vertices.length];
			System.arraycopy(vertices, 0, this.cacheVertices, 0, vertices.length);
			this.cacheValues = new double[vertices.length];
			for (int i = 0; i < vertices.length; ++i) {
				this.cacheValues[i] = this.values[vertices[i]];
			}
		}
	}

	@Override
	public void clearCache() {
		this.cacheVertices = null;
		this.cacheValues = null;
	}

	@Override
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
		return SampleDoubleArray.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue(SamplingMode mode) {
		return SampleDoubleArray.toString(this.values);
	}

	@Override
	public double getValue(int idx) {
		return this.values[idx];
	}

	@Override
	public void setValue(int idx, double value) {
		this.values[idx] = value;
	}

	@Override
	public int getSize() {
		return this.values.length;
	}
}
