package se.cbb.jprime.misc;

/**
 * Multidimensional string array.
 * 
 * @author Joel Sjöstrand.
 */
public class StringMultiArray implements MultiArray {

	/** Maximum number of elements allowed in multidimensional array. */
	public static final int MAX_CARDINALITY = 2000000;
	
	/** Lengths in each dimension of array, child first. */
	private final int[] lengths;
	
	/** Length factors for computing array index. Contents: 1,|X|,|X|*|Y|,... */
	private final int[] lengthFactors;
	
	/** Values. */
	private final String[] values;
	
	@Override
	public Class<?> getComponentType() {
		return String.class;
	}

	@Override
	public int[] getLengths() {
		return this.lengths;
	}

	@Override
	public int getRank() {
		return this.lengths.length;
	}

	/**
	 * Constructor. Creates a multiarray of desired dimension.
	 * We require that the dimensionality is > 0 and that all dimension
	 * lengths > 0.
	 * @param lengths the length of each dimension.
	 */
	public StringMultiArray(int[] lengths) {
		if (lengths == null || lengths.length == 0) {
			throw new IllegalArgumentException("Cannot create empty or scalar multiarray.");
		}
		for (int i : lengths) {
			if (i <= 0) {
				throw new IllegalArgumentException("Cannot create multiarray with empty or " +
						"negative dimension.");
			}
		}
		
		int k = lengths.length;
		this.lengths = lengths;
		this.lengthFactors = new int[k];
		
		// Compute factors.
		this.lengthFactors[0] = 1;
		for (int i = 1; i< k; ++i) {
			this.lengthFactors[i] = this.lengths[i - 1] * this.lengthFactors[i - 1];
		}
		
		// Create space for values.
		int n = this.lengths[k] * this.lengthFactors[k];
		if (n > MAX_CARDINALITY) {
			throw new IllegalArgumentException("Multiarray exceeds maximum allowed size.");
		}
		this.values = new String[n];
	}
	
	/**
	 * Returns the value for a certain index.
	 * No bounds checking.
	 * @param index the index.
	 * @return the value at the index.
	 */
	public String get(int[] index) {
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			idx += index[i] * this.lengthFactors[i];
		}
		return this.values[idx];
	}
	
	/**
	 * Identical to <code>get(.)</code>, but performs bounds checking.
	 * @param index the index.
	 * @return the value.
	 */
	public String getSafe(int[] index) {
		if (index == null || index.length != this.lengths.length) {
			throw new IllegalArgumentException("Cannot retrieve multiarray value due to invalid index length.");
		}
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			if (index[i] < 0 || index[i] >= this.lengths[i]) {
				throw new IllegalArgumentException("Cannot retrieve multiarray value due to out-of-range index.");
			}
			idx += index[i] * this.lengthFactors[i];
		}
		return this.values[idx];
	}
	
	/**
	 * Sets the value for a certain index.
	 * No bounds checking.
	 * @param index the index.
	 * @param value the value to set at the index.
	 */
	public void set(int[] index, String value) {
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			idx += index[i] * this.lengthFactors[i];
		}
		this.values[idx] = value;
	}
	
	/**
	 * Identical to <code>set(.,.)</code>, but performs bounds checking.
	 * @param index the index.
	 * @param value the value to set at the index.
	 */
	public void setSafe(int[] index, String value) {
		if (index == null || index.length != this.lengths.length) {
			throw new IllegalArgumentException("Cannot set multiarray value due to invalid index length.");
		}
		int idx = 0;
		for (int i = 0; i < this.lengthFactors.length; ++i) {
			if (index[i] < 0 || index[i] >= this.lengths[i]) {
				throw new IllegalArgumentException("Cannot set multiarray value due to out-of-range index.");
			}
			idx += index[i] * this.lengthFactors[i];
		}
		this.values[idx] = value;
	}
}
