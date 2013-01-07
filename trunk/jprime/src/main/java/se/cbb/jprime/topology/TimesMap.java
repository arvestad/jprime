package se.cbb.jprime.topology;

/**
 * Specialisation of a DoubleMap to hold times of an ultrametric tree.
 * Provides access to both absolute times of vertices and time span of arcs.
 * Observe that the ordinary values holds the absolute vertex times. If you
 * need the arc times, there is a convenience method that returns such a map.
 * 
 * @author Joel Sjöstrand.
 */
public class TimesMap extends DoubleMap {
	
	/** Arc times, as opposed to absolute vertex times. */
	protected double[] arcTimes;
	
	/** Cache values for affected arc times. */
	protected double[] cacheArcTimes = null;
	
	/**
	 * Constructor. Takes as input a list of absolute times (vertex times) and
	 * relative times (arc times). It is assumed that the times are in fact ultrametric
	 * and compatible.
	 * @param name the name of the map.
	 * @param vt the absolute times.
	 * @param at the relative times.
	 */
	public TimesMap(String name, double[] vt, double[] at) {
		super(name, vt);	// Absolute times are stored in ordinary array.
		this.arcTimes = at;
		if (vt == null || at == null) {
			throw new IllegalArgumentException("Times array is missing.");
		}
	}
	
	/**
	 * Returns the absolute time (vertex time) of a vertex.
	 * Identical to get(x).
	 * @param x the vertex.
	 * @return the vertex time.
	 */
	public double getVertexTime(int x) {
		return this.values[x];
	}
	
	/**
	 * Returns the relative time (arc time) of an arc, indexed by the arc's head x,
	 * i.e. time(p(x))-time(x). If x is the root, the "top time" is returned.
	 * @param x the arc's head.
	 * @return the arc time.
	 */
	public double getArcTime(int x) {
		return this.arcTimes[x];
	}

	
	/**
	 * Returns the absolute time (vertex time) of a vertex.
	 * Identical to getVertexTime(x).
	 * @param x the vertex.
	 * @return the vertex time.
	 */
	@Override
	public double get(int x) {
		return this.values[x];
	}
	
	/**
	 * Unsupported method. Use getVertexTimes() and getArcTimes() instead for low-level manipulation of times.
	 * @param x the vertex.
	 * @param val the new value.
	 */
	@Override
	public void set(int x, double val) {
		throw new UnsupportedOperationException("Cannot set absolute time (vertex time) of a vertex without also " +
				"changing corresponding arc times. Use getVertexTimes() and getArcTimes() instead for low-level " +
				"manipulation of the underlying values.");
	}
	
	/**
	 * Unsupported method. Use getVertexTimes() and getArcTimes() instead for low-level manipulation of times.
	 * @param x the vertex.
	 * @param val the new value.
	 */
	@Override
	public void setAsObject(int x, Object val) {
		this.set(x, ((Double) val).doubleValue());
	}
	
	/**
	 * Returns the actual vertex times of this map for low-level manipulation.
	 * User must ensure vertex times and arc times are ultrametric and compatible
	 * after changes to its elements. See also sister method <code>getArcTimes()</code>.
	 * @return the internal vertex times.
	 */
	public double[] getVertexTimes() {
		return this.values;
	}
	
	/**
	 * Returns the actual arc times of this map for low-level manipulation.
	 * User must ensure vertex times and arc times are ultrametric and compatible
	 * after changes to its elements. See also sister method <code>getVetexTimes()</code>.
	 * @return the internal arc times.
	 */
	public double[] getArcTimes() {
		return this.arcTimes;
	}
	
	/**
	 * Returns the total arc time of the entire tree.
	 * @return the total timespan.
	 */
	public double getTotalArcTime() {
		double tot = 0.0;
		for (double at : this.arcTimes) {
			tot += at;
		}
		return tot;
	}
	
	@Override
	public void cache(int[] vertices) {
		super.cache(vertices);
		if (vertices == null) {
			this.cacheArcTimes = new double[this.arcTimes.length];
			System.arraycopy(this.arcTimes, 0, this.cacheArcTimes, 0, this.arcTimes.length);
		} else {
			this.cacheArcTimes = new double[vertices.length];
			for (int i = 0; i < vertices.length; ++i) {
				this.cacheArcTimes[i] = this.arcTimes[vertices[i]];
			}
		}
	}

	@Override
	public void clearCache() {
		super.clearCache();
		this.cacheArcTimes = null;
	}

	@Override
	public void restoreCache() {
		if (this.cacheArcTimes == null) {
		} else if (this.cacheVertices == null) {
			this.arcTimes = this.cacheArcTimes;
			this.cacheArcTimes = null;
		} else {
			for (int i = 0; i < this.cacheVertices.length; ++i) {
				this.arcTimes[this.cacheVertices[i]] = this.cacheArcTimes[i];
			}
			this.cacheArcTimes = null;
		}
		super.restoreCache();
	}
	
	/**
	 * Returns a map of the arc times for when such a specific need arises.
	 * @return the arc times in a map.
	 */
	public DoubleMap getArcTimesMap() {
		return new DoubleMap(name, this.arcTimes);
	}
	
}
