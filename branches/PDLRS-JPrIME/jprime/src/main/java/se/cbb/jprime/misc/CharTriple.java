package se.cbb.jprime.misc;

/**
 * Ordered triple of chars.
 * 
 * @author Joel Sjöstrand.
 */
public class CharTriple {
	
	/** First object in tuple. */
    public final char first;
    
    /** Second object in tuple. */
    public final char second;

    /** Third object in tuple. */
    public final char third;
    
    /**
     * Creates a triple of chars.
     * @param first first char.
     * @param second second char.
     * @param third third char.
     */
    public CharTriple(char first, char second, char third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    /**
     * Returns true if this triple equals another triple.
     * @param t the triple to compare with.
     * @return true if the same values.
     */
    public boolean equals(CharTriple t) {
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
		CharTriple other = (CharTriple) obj;
		return (this.first == other.first && this.second == other.second && this.third == other.third);
	}

	@Override
	public String toString() {
		return "CharTriple[" + first + "," + second + "," + third + "]";
	}
}