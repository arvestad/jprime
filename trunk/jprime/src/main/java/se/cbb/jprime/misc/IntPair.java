package se.cbb.jprime.misc;

/**
 * Pair implementation for twin primitive ints.
 * 
 * @author Joel Sjöstrand.
 */
public class IntPair {

	/** First int in tuple. */
    public final int first;
    
    /** Second object in tuple. */
    public final int second;

    /**
     * Creates a pair of ints.
     * @param first first int.
     * @param second second int.
     */
    public IntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns true if this pair equals another pair.
     * @param p the pair to compare with.
     * @return true if the same values.
     */
    public boolean equals(IntPair p) {
    	return (this.first == p.first && this.second == p.second);
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + first;
		result = prime * result + second;
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
		IntPair other = (IntPair) obj;
		return (this.first == other.first && this.second == other.second);
	}

	@Override
	public String toString() {
		return "IntPair[" + first + ","+ second + "]";
	}
	
}
