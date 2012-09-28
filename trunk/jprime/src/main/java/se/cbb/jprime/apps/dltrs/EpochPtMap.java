package se.cbb.jprime.apps.dltrs;

/**
 * Map for storing data for each point of a discretised epoch tree.
 * <p/>
 * Values are stored as follows:
 * For each discretised time (of which there may be two since adjacent
 * epochs each have points on the border), there is a vector of values
 * corresponding to each arc at that time.
 * These vectors are stored in an enclosing vector which covers the times
 * of all epochs appended after each other.
 * <p/>
 * If the discretisation topology of the epochs on which the map is based
 * changes, the map is invalidated (and a new map must be created).
 * <p/>
 * Points are referenced by triplets: epoch number, time index in epoch
 * and arc index in epoch.
 * <p/>
 * The map includes functionality for caching and restoring values.
 * 
 * @author Joel Sjöstrand.
 */
class EpochPtMap {
	
	/** For each epoch, the offset in value vector with regard to times. */
	private int[] m_offsets;
	
	/**
	 * Values for all points. Each element in outer vector
	 * corresponds to a time.
	 */
	private double[][] m_vals;
	
	/** Cached values. */
	private double[][] m_valsCache = null;
	
	/**
	 * Constructor. Creates a map filled with default elements.
	 * @param ed the discretised tree.
	 */
	public EpochPtMap(EpochDiscretiser ed) {
	
		// Compute offsets.
		m_offsets = new int[ed.getNoOfEpochs() + 1];
		m_offsets[0] = 0;
		for (int i = 0; i < ed.getNoOfEpochs(); ++i) {
			m_offsets[i+1] = m_offsets[i] + ed.getEpoch(i).getNoOfTimes();
		}
		
		// Create all default values.
		m_vals = new double[m_offsets[m_offsets.length-1]][];
		int k = 0;
		for (int i = 0; i < ed.getNoOfEpochs(); ++i) {
			int noOfTms = ed.getEpoch(i).getNoOfTimes();
			int noOfEdges = ed.getEpoch(i).getNoOfArcs();
			for (int j = 0; j < noOfTms; ++j) {
				m_vals[k++] = new double[noOfEdges];
			}
		}
	}
	
	/**
	 * Copy-constructor. Cached values are not copied.
	 */
	public EpochPtMap(EpochPtMap ptMap) {
		m_offsets = new int[ptMap.m_offsets.length];
		System.arraycopy(ptMap.m_offsets, 0, m_offsets, 0, ptMap.m_offsets.length);
		m_vals = new double[ptMap.m_vals.length][];
		for (int i = 0; i < m_vals.length; ++i) {
			m_vals[i] = new double[ptMap.m_vals[i].length];
			System.arraycopy(ptMap.m_vals[i], 0, m_vals[i], 0, ptMap.m_vals[i].length);
		}
	}

	
	/**
	 * Returns the value at a certain point.
	 * @param i the epoch index.
	 * @param j the time index in epoch.
	 * @param k the arc index in epoch.
	 * @return the value at the point.
	 */
	public double get(int i, int j, int k) {
		return (m_vals[m_offsets[i] + j][k]);
	}
	
    /**
	 * Returns all values for a certain time in a certain epoch.
	 * @param i the epoch index.
	 * @param j the time index in epoch.
	 * @return the values for all concerned arcs.
	 */
	public double[] get(int i, int j) {
		return (m_vals[m_offsets[i] + j]);
	}
    
	/**
	 * Sets values for all points for a certain time in an epoch.
	 * @param i the epoch index.
	 * @param j the time index in epoch.
	 * @param start the start index in vec.
	 * @param vec the vector to copy from.
	 */
	public void set(int i, int j, double[] vec, int start) {
		double[] v = m_vals[m_offsets[i] + j];
		System.arraycopy(vec, start, v, 0, v.length);
	}
	
	/**
	 * Sets values for all points for a certain time in an epoch.
	 * If a value to be set is smaller than a specified bound, the
	 * bound is used instead.
	 * @param i the epoch index.
	 * @param j the time index in epoch.
	 * @param vec the vector to copy from.
	 * @param start the start index in vec.
	 * @param lowerBound the lower bound.
	 */
	public void setWithMin(int i, int j, double[] vec, int start, double lowerBound) {
		double[] v = m_vals[m_offsets[i] + j];
		for (int a = 0; a < v.length; ++a, ++start) {
			v[a] = Math.max(vec[start], lowerBound);
		}
	}
	
	/**
	 * Sets values for all points for a certain time in an epoch.
	 * If a value to be set is greater than a specified bound, the
	 * bound is used instead.
	 * @param i the epoch index.
	 * @param j the time index in epoch.
	 * @param vec the vector to copy from.
	 * @param start the start index in vec.
	 * @param upperBound the upper bound.
	 */
	public void setWithMax(int i, int j, double[] vec, int start, double upperBound) {
		double[] v = m_vals[m_offsets[i] + j];
		for (int a = 0; a < v.length; ++a, ++start) {
			v[a] = Math.min(vec[start], upperBound);
		}
	}
	
	/**
	 * Sets the value of a certain time and arc in a certain epoch.
	 * @param i the epoch index.
	 * @param j the time index in epoch.
	 * @param k the arc index in epoch.
	 * @param val the value.
	 */
	public void set(int i, int j, int k, double val) {
		m_vals[m_offsets[i] + j][k] = val;
	}
	
	/**
	 * Returns the value for a specified arc for
	 * the last time of a specified epoch.
	 * @param i the epoch index.
	 * @param k the arc index in epoch.
	 * @return the value.
	 */
	public double getForLastTime(int i, int k) {
		return (m_vals[m_offsets[i+1] - 1][k]);
	}
	
	/**
	 * Returns the values for a time below a specified one.
	 * @param i the epoch index.
	 * @param k the arc index in epoch.
	 * @return the values.
	 */
	public double[] getBelow(int i, int k) {
		return (m_vals[m_offsets[i] + k - 1]);
	}
    
	/**
	 * Returns the topmost value, i.e. the value at
	 * the very last time of the last epoch.
	 * @return the topmost value.
	 */
	public double getTopmost() {
		return m_vals[m_vals.length-1][0];
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
	 * Saves current values in a cache.
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
     * Index 0 is printed last.
     * @return a string representation.
     */
	@Override
    public String toString() {
    	StringBuilder oss = new StringBuilder(65536);
    	int noOfEps = m_offsets.length - 1;
    	for (int epi = noOfEps-1; epi >= 0; --epi) {
    		int noOfTimes = m_offsets[epi+1] - m_offsets[epi];
			for (int tm = noOfTimes-1; tm >= 0; --tm) {
				oss.append("# (").append(epi).append(',').append(tm).append("): ");
				double[] v = m_vals[m_offsets[epi] + tm];
				for (int e = 0; e < v.length; ++e) {
					oss.append(v[e]).append(' ');
				}
				oss.append('\n');
			}
		}
    	return oss.toString();
    }
	
}