package se.cbb.jprime.misc;

/**
 * Generic ordered triple implementation.
 * 
 * @author Joel Sjöstrand.
 *
 * @param <A> first object's type.
 * @param <B> second object's type.
 * @param <C> third object's type.
 */
public class Triple<A, B, C> {
	
	/** First object in tuple. */
    public final A first;
    
    /** Second object in tuple. */
    public final B second;

    /** Third object in tuple. */
    public final C third;
    
    /**
     * Creates a triple of objects.
     * @param first first object.
     * @param second second object.
     * @param third third object.
     */
    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    /**
     * Returns true if this triple equals another triple (this corresponding to the equal() method
     * evaluating to true for the individual elements).
     * @param t the tuple to compare with.
     * @return true if equals() evaluates to true for the individual elements.
     */
    public boolean equals(Triple<A, B, C> t) {
    	return (this.first.equals(t.first) && this.second.equals(t.second) && this.third.equals(t.third));
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
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
		Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
	    return (this.first.equals(other.first) && this.second.equals(other.second) &&
	    		this.third.equals(other.third));
	}
    
	@Override
	public String toString() {
		return "Triple<" + first.getClass().getName() + "," + second.getClass().getName() + "," +
		third.getClass().getName() + ">[" + first + "," + second + "," + third + "]";
	}
}
