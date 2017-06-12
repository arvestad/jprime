package se.cbb.jprime.misc;

/**
 * Ordered triple of doubles.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleTriple {
	
	/** First object in tuple. */
    public final double first;
    
    /** Second object in tuple. */
    public final double second;

    /** Third object in tuple. */
    public final double third;
    
    /**
     * Creates a triple of doubles.
     * @param first first double.
     * @param second second double.
     * @param third third double.
     */
    public DoubleTriple(double first, double second, double third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    /**
     * Returns true if this triple equals another triple.
     * @param t the triple to compare with.
     * @return true if the same values.
     */
    public boolean equals(DoubleTriple t) {
    	return (Double.doubleToLongBits(this.first) == Double.doubleToLongBits(t.first) &&
        		Double.doubleToLongBits(this.second) == Double.doubleToLongBits(t.second) &&
        		Double.doubleToLongBits(this.third) == Double.doubleToLongBits(t.third));
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(first);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(second);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(third);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		DoubleTriple other = (DoubleTriple) obj;
		return (Double.doubleToLongBits(this.first) == Double.doubleToLongBits(other.first) &&
        		Double.doubleToLongBits(this.second) == Double.doubleToLongBits(other.second) &&
        		Double.doubleToLongBits(this.third) == Double.doubleToLongBits(other.third));
	}

	@Override
	public String toString() {
		return "DoubleTriple[" + first + "," + second + "," + third + "]";
	}
    
}
