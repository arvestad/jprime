package se.cbb.jprime.misc;

/**
 * Generic ordered pair implementation.
 * 
 * @author Joel Sjöstrand.
 *
 * @param <A> first object's type.
 * @param <B> second object's type.
 */
public class Pair<A, B> {
	
	/** First object in tuple. */
    public final A first;
    
    /** Second object in tuple. */
    public final B second;

    /**
     * Creates a pair of objects.
     * @param first first object.
     * @param second second object.
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Returns true if this pair equals another pair (this corresponding to the equal() method
     * evaluating to true for the individual elements).
     * @param t the tuple to compare with.
     * @return true if equals() evaluates to true for the individual elements.
     */
    public boolean equals(Pair<A, B> t) {
    	return (this.first.equals(t.first) && this.second.equals(t.second));
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
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
		
		Pair<?, ?> other = (Pair<?, ?>) obj;
	    return (this.first.equals(other.first) && this.second.equals(other.second));
	}

	@Override
	public String toString() {
		return "Pair<" + first.getClass().getName() + "," + second.getClass().getName() +
		">[" + first + "," + second + "]";
	}
}