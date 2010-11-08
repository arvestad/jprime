package se.cbb.jprime.misc;

/**
 * Pair implementation for twin primitive booleans.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanPair {

	/** First boolean in tuple. */
    public final boolean first;
    
    /** Second object in tuple. */
    public final boolean second;

    /**
     * Creates a pair of booleans.
     * @param first first boolean.
     * @param second second boolean.
     */
    public BooleanPair(boolean first, boolean second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Returns true if this pair equals another pair.
     * @param p the pair to compare with.
     * @return true if the same values.
     */
    public boolean equals(BooleanPair p) {
    	return (this.first == p.first && this.second == p.second);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (first ? 1231 : 1237);
		result = prime * result + (second ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BooleanPair other = (BooleanPair) obj;
		return (this.first == other.first && this.second == other.second);
	}

	@Override
	public String toString() {
		return "BooleanPair[" + first + "," + second + "]";
	}
    
}
