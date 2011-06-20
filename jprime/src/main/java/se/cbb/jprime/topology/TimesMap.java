package se.cbb.jprime.topology;

/**
 * Specialisation of a DoubleMap to hold times of an ultrametric tree.
 * Provides access to both absolute times of vertices and time span of arcs.
 * 
 * @author Joel Sjöstrand.
 */
public class TimesMap extends DoubleMap {
	
	double[] arcTimes;
	
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
	
	@Override
	public void set(int x, double val) {
		throw new UnsupportedOperationException("Cannot set absolute time (vertex time) of a vertex without also " +
				"changing corresponding arc times. Use getVertexTimes() and getArcTimes() for low-level " +
				"manipulation instead.");
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
}