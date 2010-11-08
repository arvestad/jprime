package se.cbb.jprime.misc;

/**
 * Pair implementation for twin primitive chars.
 * 
 * @author Joel Sjöstrand.
 */
public class CharPair {

	/** First char in tuple. */
    public final char first;
    
    /** Second object in tuple. */
    public final char second;

    /**
     * Creates a pair of chars.
     * @param first first char.
     * @param second second char.
     */
    public CharPair(char first, char second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Returns true if this pair equals another pair.
     * @param p the pair to compare with.
     * @return true if the same values.
     */
    public boolean equals(CharPair p) {
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
		CharPair other = (CharPair) obj;
		return (this.first == other.first && this.second == other.second);
	}

	@Override
	public String toString() {
		return "CharPair[" + first + "," + second + "]";
	}
	
}
