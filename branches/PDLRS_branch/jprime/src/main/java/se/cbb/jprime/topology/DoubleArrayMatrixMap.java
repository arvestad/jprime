package se.cbb.jprime.topology;

/**
 * Holds a double array for each pair (x,y) of vertices of a graph.
 * No generics for the sake of speed.
 * See also <code>GenericMap</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleArrayMatrixMap {
	
	/** The map values. */
	protected double[][][] values;
	
	/** Cache vertices. */
	protected int[] cacheVertices = null;
	
	/** Cache values for affected vertices. */
	protected double[][][] cacheValues = null;
	
	/**
	 * Constructor. Initialises all map values to a null array.
	 * @param size the size of the underlying graph, |V(G)|, i.e., the map will
	 *        have size*size elements.
	 */
	public DoubleArrayMatrixMap(int size) {
		this.values = new double[size][size][];
	}
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number in the
	 *        first and second dimensions.
	 */
	public DoubleArrayMatrixMap(String name, double[][][] vals) {
		this.values = vals;
	}
	
	/**
	 * Copy constructor.
	 * @param map the map to be copied.
	 */
	public DoubleArrayMatrixMap(DoubleArrayMatrixMap map) {
		this.values = new double[map.values.length][map.values.length][];
		for (int i = 0; i < this.values.length; ++i) {
			for (int j = 0; j < this.values.length; ++j) {
				this.values[i][j] = new double[map.values[i][j].length];
				System.arraycopy(map.values[i][j], 0, this.values[i][j], 0, this.values[i][j].length);
			}
		}
	}
	
	/**
	 * Returns the element of a pair of vertices (x,y).
	 * @param x the first vertex.
	 * @param y the second vertex.
	 * @return the values.
	 */
	public double[] get(int x, int y) {
		return this.values[x][y];
	}
	
	/**
	 * Returns the element of the array of a pair of vertices (x,y).
	 * No bounds checking.
	 * @param x the first vertex.
	 * @param y the second vertex.
	 * @param i the index in the element's array.
	 * @return the value.
	 */
	public double get(int x, int y, int i) {
		return this.values[x][y][i];
	}
	
	/**
	 * Sets the element of a pair of vertices (x,y).
	 * @param x the first vertex.
	 * @param y the second vertex.
	 * @param vals the values.
	 */
	public void set(int x, int y, double[] vals) {
		this.values[x][y] = vals;
	}
	
	/**
	 * Sets the element of the array of a pair of vertices (x,y).
	 * No bounds checking.
	 * @param x the first vertex.
	 * @param y the second vertex.
	 * @param i the index in the array of the vertex.
	 * @param val the value.
	 */
	public void set(int x, int y, int i, double val) {
		this.values[x][y][i] = val;
	}

	/**
	 * Caches a part of or the whole current map. May e.g. be used by a <code>Proposer</code>.
	 * @param vertices the vertices. Null will cache all values.
	 */
	public void cache(int[] vertices) {
		if (vertices == null) {
			this.cacheValues = new double[this.values.length][this.values.length][];
			for (int i = 0; i < this.values.length; ++i) {
				for (int j = 0; j < this.values.length; ++j) {
					if (this.values[i][j] != null) {
						this.cacheValues[i][j] = new double[this.values[i][j].length];
						System.arraycopy(this.values[i][j], 0, this.cacheValues[i][j], 0, this.values[i][j].length);
					}
				}
			}
		} else {
			this.cacheVertices = new int[vertices.length];
			System.arraycopy(vertices, 0, this.cacheVertices, 0, vertices.length);
			this.cacheValues = new double[vertices.length][vertices.length][];
			for (int i = 0; i < vertices.length; ++i) {
				for (int j = 0; j < vertices.length; ++j) {
					if (this.values[vertices[i]][vertices[j]] != null) {
						this.cacheValues[i][j] = new double[this.values[vertices[i]][vertices[j]].length];
						System.arraycopy(this.values[vertices[i]][vertices[j]], 0, this.cacheValues[i][j], 0, this.cacheValues[i][j].length);
					}
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
				for (int j = 0; j < this.cacheVertices.length; ++j) {
					this.values[this.cacheVertices[i]][this.cacheVertices[j]] = this.cacheValues[i][j];
				}
			}
			this.cacheVertices = null;
			this.cacheValues = null;
		}
	}
}
