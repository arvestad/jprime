package se.cbb.jprime.misc;

/**
 * Generic ordered quadruple implementation.
 * 
 * @author Joel Sjöstrand.
 *
 * @param <A> first object's type.
 * @param <B> second object's type.
 * @param <C> third object's type.
 * @param <D> fourth object's type.
 */
public class Quadruple<A, B, C, D> {
	
	/** First object in tuple. */
    public final A first;
    
    /** Second object in tuple. */
    public final B second;

    /** Third object in tuple. */
    public final C third;
    
    /** Fourth object in tuple. */
    public final D fourth;
    
    /**
     * Creates a triple of objects.
     * @param first first object.
     * @param second second object.
     * @param third third object.
     * @param fourth fourth object.
     */
    public Quadruple(A first, B second, C third, D fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }
    
    /**
     * Returns true if this quadruple equals another quadruple (this corresponding to the equal() method
     * evaluating to true for the individual elements).
     * @param t the tuple to compare with.
     * @return true if equals() evaluates to true for the individual elements.
     */
    public boolean equals(Quadruple<A, B, C, D> t) {
    	return (this.first.equals(t.first) && this.second.equals(t.second) &&
    			this.third.equals(t.third) && this.fourth.equals(t.fourth));
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
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
		Quadruple<?, ?, ?, ?> other = (Quadruple<?, ?, ?, ?>) obj;
	    return (this.first.equals(other.first) && this.second.equals(other.second) &&
	    		this.third.equals(other.third) && this.fourth.equals(other.fourth));
	}
    
	@Override
	public String toString() {
		return "Quadruple<" + first.getClass().getName() + "," + second.getClass().getName() + "," +
		third.getClass().getName() + "," + fourth.getClass().getName() + ">[" +
		first + "," + second + "," + third + "]";
	}
}
