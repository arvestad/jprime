package se.cbb.jprime.apps.dltrs;

/**
 * Consider the point set of a discretised epoch tree. This map
 * stores data for the Cartesian product of two such sets,
 * i.e. one value for every pair of points (well, not quite, only for
 * the upper point to the lower, as in a triangular matrix).
 * <p/>
 * Data is stored in a (concatenated triangular) matrix where element (i,j) contains all map values
 * from time i to time j.
 * Such an element is in turn a (concatenated) matrix
 * containing the values from all points at time i to all points at
 * time j with respect to the different arcs.
 * <p/>
 * If the discretisation topology of the epochs on which the map is
 * based changes, the map is invalidated and a new instance replacing
 * it must be created.
 * <p/>
 * Points are referenced by triplets: epoch number, time index in epoch
 * and edge index in epoch. Values are, naturally, retrieved using
 * two points.
 * <p/>
 * The map includes functionality for caching and restoring values.
 * 
 * @author Joel Sjöstrand.
 */
class EpochPtPtMap {
	
	/** No. of (non-unique) times. */
	private int noOfTimes;
	
	/** For each epoch, the offset in value matrix with regard to times. */
	private int[] m_offsets;
	
	/** For each time-to-time as a concatenated triangular matrix, holds the arc-to-arc values as a concatenated matrix. */
	private double[][] m_vals;
	
	/** Cached values. */
	private double[][] m_valsCache = null;
	
	/**
	 * Constructor.
	 * @param ed the discretised epoch tree.
	 */
	public EpochPtPtMap(EpochDiscretiser ed) {
		
		int noOfEps = ed.getNoOfEpochs();
		
		// Compute offsets.
		m_offsets = new int[noOfEps + 1];
		m_offsets[0] = 0;
		for (int i = 0; i < noOfEps; ++i) {
			m_offsets[i+1] = m_offsets[i] + ed.getEpoch(i).getNoOfTimes();
		}
		
		// Create and fill value matrix.
		noOfTimes = m_offsets[m_offsets.length-1];
		m_vals = new double[noOfTimes*(noOfTimes+1)/2][];
		for (int i = 0; i < noOfEps; ++i) {
			Epoch iep = ed.getEpoch(i);
			int wdi = iep.getNoOfArcs();
			for (int s = 0; s < iep.getNoOfTimes(); ++s) {
				for (int j = i; j < noOfEps; ++j) {
					Epoch jep = ed.getEpoch(j);
					int wdj = jep.getNoOfArcs();
					for (int t = (i==j) ? s : 0; t < jep.getNoOfTimes(); ++t) {
						int it = m_offsets[i] + s;
						int jt = m_offsets[j] + t;
						m_vals[it * (2 * noOfTimes - it - 1) / 2 + jt] = new double[wdi * wdj];
					}
				}
			}
		}
	}
	
	/**
	 * Copy-constructor. Cached values are not copied.
	 */
	public EpochPtPtMap(EpochPtPtMap ptPtMap) {
		m_offsets = new int[ptPtMap.m_offsets.length];
		System.arraycopy(ptPtMap.m_offsets, 0, m_offsets, 0, m_offsets.length);
		m_vals = new double[ptPtMap.m_vals.length][];
		for (int i = 0; i < m_vals.length; ++i) {
			m_vals[i] =  new double[ptPtMap.m_vals[i].length];
			System.arraycopy(ptPtMap.m_vals[i], 0, m_vals[i], 0, m_vals[i].length);
		}
	}
	
	/**
	 * Sets all values from points of a certain time to points
	 * of a certain time. The input matrix should be provided
	 * in form of a vector where matrix rows have been concatenated.
	 * @param i epoch index of lower time 1.
	 * @param s time index in epoch of time 1.
	 * @param j epoch index of upper time 2.
	 * @param t time index in epoch of time 2.
	 * @param vec vector to copy from.
	 * @param start the start index in vec.
	 */
	public void set(int i, int s, int j, int t, double[] vec, int start) {
		int it = m_offsets[i] + s;
		int jt = m_offsets[j] + t;
		double[] v = m_vals[it * (2 * noOfTimes - it - 1) / 2 + jt];
		System.arraycopy(vec, start, v, 0, v.length);
	}
	
	/**
	 * Sets all values from points of a certain time to points
	 * of a certain time. The input matrix should be provided
	 * in form of a vector where matrix rows have been concatenated.
	 * If a value to be set is smaller than a specified bound, the
	 * bound is used instead.
	 * @param i epoch index of lower time 1.
	 * @param s time index in epoch of time 1.
	 * @param j epoch index of upper time 2.
	 * @param t time index in epoch of time 2.
	 * @param vec vector to copy from.
	 * @param start the start index in vec.
	 * @param lowerBound the lower bound.
	 */
	public void setWithMin(int i, int s, int j, int t, double[] vec, int start, double lowerBound)
	{
		int it = m_offsets[i] + s;
		int jt = m_offsets[j] + t;
		double[] v = m_vals[it * (2 * noOfTimes - it - 1) / 2 + jt];
		for (int k = 0; k < v.length; ++k, ++start) {
			v[k] = Math.max(vec[start], lowerBound);
		}
	}
	
	/**
	 * Sets all values from points of a certain time to points
	 * of a certain time. The input matrix should be provided
	 * in form of a vector where matrix rows have been concatenated.
	 * If a value to be set is greater than a specified bound, the
	 * bound is used instead.
	 * @param i epoch index of lower time 1.
	 * @param s time index in epoch of time 1.
	 * @param j epoch index of upper time 2.
	 * @param t time index in epoch of time 2.
	 * @param vec vector to copy from.
	 * @param start the start index in vec.
	 * @param upperBound the upper bound.
	 */
	public void setWithMax(int i, int s, int j, int t, double[] vec, int start, double upperBound) {
		int it = m_offsets[i] + s;
		int jt = m_offsets[j] + t;
		double[] v = m_vals[it * (2 * noOfTimes - it - 1) / 2 + jt];
		for (int k = 0; k < v.length; ++k, ++start) {
			v[k] = Math.min(vec[start], upperBound);
		}
	}
	
	/**
	 * Resets all values in entire map to the specified value.
	 * @param defaultVal the value to be set.
	 */
	public void reset(double defaultVal) {
		for (int i = 0; i < m_vals.length; ++i) {
			for (int j = 0; j < m_vals[i].length; ++j) {
				m_vals[i][j] = defaultVal;
			}
		}
	}
	
	/**
	 * Returns a certain point-to-point value.
	 * @param i epoch index of lower time 1.
	 * @param s time index in epoch of time 1.
	 * @param a edge index in epoch of time 1.
	 * @param j epoch index of upper time 2.
	 * @param t time index in epoch of time 2.
	 * @param b edge index in epoch of time 2.
	 * @return the value.
	 */
	public double get(int i, int s, int a, int j, int t, int b) {
		int it = m_offsets[i] + s;
		int jt = m_offsets[j] + t;
		double[] v = m_vals[it * (2 * noOfTimes - it - 1) / 2 + jt];
		int noOfArcs = m_offsets.length - 1 - j;
    	return v[a * noOfArcs + b];
    }
	
	/**
	 * Sets a certain point-to-point value.
	 * @param i epoch index of lower time 1.
	 * @param s time index in epoch of time 1.
	 * @param a edge index in epoch of time 1.
	 * @param j epoch index of upper time 2.
	 * @param t time index in epoch of time 2.
	 * @param b edge index in epoch of time 2.
	 * @param val the value.
	 */
	public void set(int i, int s, int a, int j, int t, int b, double val) {
		int it = m_offsets[i] + s;
		int jt = m_offsets[j] + t;
		double[] v = m_vals[it * (2 * noOfTimes - it - 1) / 2 + jt];
		int noOfArcs = m_offsets.length - 1 - j;
    	v[a * noOfArcs + b] = val;
    }
	
	/**
	 * Saves current values in a cache. Cached values can be
	 * restored with a call to restoreCache().
	 */
	public void cache() {
		m_valsCache = new double[m_vals.length][];
		for (int i = 0; i < m_vals.length; ++i) {
			m_valsCache[i] = new double[m_vals[i].length];
			System.arraycopy(m_vals[i], 0, m_valsCache[i], 0, m_vals[i].length);
		}
	}
	
	/**
	 * Restores cached values.
	 */
	public void restoreCache() {
		m_vals = m_valsCache;
		m_valsCache = null;
	}
	
	/**
	 * Disables last made cache.
	 */
	public void clearCache() {
		m_valsCache = null;
	}
	
	/**
     * Returns a string representation of the map.
     * @return a string representation.
     */
	@Override
	public String toString() {
		StringBuilder oss = new StringBuilder(65536);
		oss.append("# (epoch,index) to (epoch,time):\n");
		
		// Epoch e.
		for (int e = 0; e < m_offsets.length - 1; ++e) {
			
			// Time it in epoch e.
			for (int it = m_offsets[e]; it < m_offsets[e+1]; ++it) {
			
				// Epoch f.
				for (int f = e; f < m_offsets.length - 1; ++f) {
				
					// Time jt in epoch f.
					for (int jt = (e==f) ? it : m_offsets[f]; jt < m_offsets[f+1]; ++jt) {
						
						oss.append("# (").append(e).append(',').append(it-m_offsets[e]).append(") to ");
						oss.append('(').append(f).append(',').append(jt-m_offsets[f]).append("):");
						oss.append('\n');
						double[] v = m_vals[it * (2 * noOfTimes - it - 1) / 2 + jt];
						int eArcs = m_offsets.length - 1 - e;
						for (int k = 0; k < eArcs; ++k) {
							oss.append("# ");
							int fArcs = m_offsets.length - 1 - f;
							for (int l = 0; l < fArcs; ++l){
								oss.append(v[k * fArcs + l]).append(' ');
							}
							oss.append('\n');
						}
					}
				}
			}
		}
		return oss.toString();
	}
    
}
