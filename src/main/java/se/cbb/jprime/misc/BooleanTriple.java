package se.cbb.jprime.misc;

/**
 * Ordered triple of booleans.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanTriple {
	
	/** First object in tuple. */
    public final boolean first;
    
    /** Second object in tuple. */
    public final boolean second;

    /** Third object in tuple. */
    public final boolean third;
    
    /**
     * Creates a triple of booleans.
     * @param first first boolean.
     * @param second second boolean.
     * @param third third boolean.
     */
    public BooleanTriple(boolean first, boolean second, boolean third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    /**
     * Returns true if this triple equals another triple.
     * @param t the triple to compare with.
     * @return true if the same values.
     */
    public boolean equals(BooleanTriple t) {
    	return (this.first == t.first && this.second == t.second && this.third == t.third);
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (first ? 1231 : 1237);
		result = prime * result + (second ? 1231 : 1237);
		result = prime * result + (third ? 1231 : 1237);
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
		BooleanTriple other = (BooleanTriple) obj;
		return (this.first == other.first && this.second == other.second && this.third == other.third);
	}

	@Override
	public String toString() {
		return "BooleanTriple[" + first + "," + second + "," + third + "]";
	}
}
