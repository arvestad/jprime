package se.cbb.jprime.misc;

/**
 * Ordered triple of ints.
 * 
 * @author Joel Sjöstrand.
 */
public class IntTriple {
	
	/** First object in tuple. */
    public final int first;
    
    /** Second object in tuple. */
    public final int second;

    /** Third object in tuple. */
    public final int third;
    
    /**
     * Creates a triple of ints.
     * @param first first int.
     * @param second second int.
     * @param third third int.
     */
    public IntTriple(int first, int second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    /**
     * Returns true if this triple equals another triple.
     * @param t the triple to compare with.
     * @return true if the same values.
     */
    public boolean equals(IntTriple t) {
    	return (this.first == t.first && this.second == t.second && this.third == t.third);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + first;
		result = prime * result + second;
		result = prime * result + third;
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
		IntTriple other = (IntTriple) obj;
		return (this.first == other.first && this.second == other.second && this.third == other.third);
	}

	@Override
	public String toString() {
		return "IntTriple[" + first + "," + second + "," + third + "]";
	}
    
}