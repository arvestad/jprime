package se.cbb.jprime.misc;

/**
 * Pair implementation for twin primitive doubles.
 * 
 * @author Joel Sjöstrand.
 */
public class DoublePair {

	/** First double in tuple. */
    public final double first;
    
    /** Second object in tuple. */
    public final double second;

    /**
     * Creates a pair of doubles.
     * @param first first double.
     * @param second second double.
     */
    public DoublePair(double first, double second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Returns true if this pair equals another pair.
     * @param p the pair to compare with.
     * @return true if the same values.
     */
    public boolean equals(DoublePair p) {
    	return (Double.doubleToLongBits(this.first) == Double.doubleToLongBits(p.first) &&
    		Double.doubleToLongBits(this.second) == Double.doubleToLongBits(p.second));
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
		DoublePair other = (DoublePair) obj;
		return (Double.doubleToLongBits(this.first) == Double.doubleToLongBits(other.first) &&
	    		Double.doubleToLongBits(this.second) == Double.doubleToLongBits(other.second));
	}

	@Override
	public String toString() {
		return "DoublePair[" + first + "," + second + "]";
	}
    
	
    
}
