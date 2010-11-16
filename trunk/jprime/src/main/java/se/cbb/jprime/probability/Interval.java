package se.cbb.jprime.probability;

/**
 * Represents a 1-D interval of the type [a,b), (a,b], (-inf,inf), etc.
 * 
 * @author Joel Sjöstrand.
 */
public class Interval {

	/** Interval bound types. */
	public enum Type {
		EMPTY,                  // (a,a) = (a,a] = ... = empty set.
		DEGENERATE,             // [a,a].
		OPEN,                   // (a,b).
		CLOSED,                 // [a,b].
		LEFT_CLOSED_RIGHT_OPEN, // [a,b)
		LEFT_OPEN_RIGHT_CLOSED, // (a,b]
		LEFT_OPEN,              // (a,inf).
		LEFT_CLOSED,            // [a,inf).
		RIGHT_OPEN,             // (-inf,b).
		RIGHT_CLOSED,           // (-inf,b].
		UNBOUNDED               // (-inf,inf).
	}
	
	/** Lower bound. */
	private double a;
	
	/** Upper bound */
	private double b;
	
	/** True if lower bound not in interval or a==-inf. */
	private boolean isLeftOpen;
	
	/** True if upper bound not in interval or b==inf. */
	private boolean isRightOpen;
	
	/** Constructor. */
	public Interval(double a, double b, boolean isLeftOpen, boolean isRightOpen) {
		this.a = a;
		this.b = b;
		this.isLeftOpen = isLeftOpen;
		this.isRightOpen = isRightOpen;
	}

	/**
	 * Returns the lower bound.
	 * @return the bound.
	 */
	public double getLowerBound() {
		return a;
	}

	/**
	 * Sets the lower bound.
	 * @param a the bound.
	 */
	public void setLowerBound(double a) {
		this.a = a;
	}

	/**
	 * Returns the upper bound.
	 * @return the bound.
	 */
	public double getUpperBound() {
		return b;
	}

	/**
	 * Sets the upper bound.
	 * @param b the bound.
	 */
	public void setUpperBound(double b) {
		this.b = b;
	}

	/**
	 * Returns true if lower bound not in interval or a==-inf.
	 * @return
	 */
	public boolean isLeftOpen() {
		return (isLeftOpen || a == Double.NEGATIVE_INFINITY);
	}

	/**
	 * Sets the lower bound type.
	 * @param isLeftOpen the bound type.
	 */
	public void setLeftOpen(boolean isLeftOpen) {
		this.isLeftOpen = isLeftOpen;
	}

	/**
	 * Returns true if upper bound not in interval or b==inf.
	 * @return
	 */
	public boolean isRightOpen() {
		return (isRightOpen || b == Double.POSITIVE_INFINITY);
	}

	/**
	 * Sets the upper bound type.
	 * @param isRightOpen the bound type.
	 */
	public void setRightOpen(boolean isRightOpen) {
		this.isRightOpen = isRightOpen;
	}
	
	/**
	 * Returns the boundary type of this interval.
	 * @return
	 */
	public Type getType() {
		if (a > b) {
			return Type.EMPTY;
		}
		if (a == b) {
			if (Double.isInfinite(a)) {
				return Type.EMPTY;
			}
			return (isLeftOpen || isRightOpen ? Type.EMPTY : Type.DEGENERATE);
		}
		if (a == Double.NEGATIVE_INFINITY) {
			if (b == Double.POSITIVE_INFINITY) {
				return Type.UNBOUNDED;
			}
			return (isRightOpen ? Type.RIGHT_OPEN : Type.RIGHT_CLOSED);
		}
		if (b == Double.POSITIVE_INFINITY) {
			return (isLeftOpen ? Type.LEFT_OPEN : Type.LEFT_CLOSED);
		}
		return Type.CLOSED;
	}
	
}
