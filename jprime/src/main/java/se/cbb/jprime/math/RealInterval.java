package se.cbb.jprime.math;

import java.util.Random;

/**
 * Represents a 1-D real valued interval of the type [a,b), (a,b], (-inf,inf), etc.
 * 
 * @author Joel Sjöstrand.
 */
public class RealInterval {

	/** Interval bound types. */
	public enum Type {
		/** (a,a) = (a,a] = ... = empty set. */  EMPTY,
		/** [a,a]. */                            DEGENERATE,
		/** (a,b). */                            OPEN,
		/** [a,b]. */                            CLOSED,
		/** [a,b). */                            LEFT_CLOSED_RIGHT_OPEN,
		/** (a,b]. */                            LEFT_OPEN_RIGHT_CLOSED,
		/** (a,inf). */                          LEFT_OPEN,
		/** [a,inf). */                          LEFT_CLOSED,
		/** (-inf,b). */                         RIGHT_OPEN,
		/** (-inf,b]. */                         RIGHT_CLOSED,
		/** (-inf,inf). */                       UNBOUNDED
	}
	
	/** Lower bound. */
	private double a;
	
	/** Upper bound */
	private double b;
	
	/** True if lower bound not in interval or a==-inf. */
	private boolean isLeftOpen;
	
	/** True if upper bound not in interval or b==inf. */
	private boolean isRightOpen;
	
	/**
	 * Constructor.
	 * @param a lower bound, possibly Double.NEGATIVE_INFINITY.
	 * @param b upper bound, possibly Double.POSITIVE_INFINITY.
	 * @param isLeftOpen true if lower bound is not itself valid, e.g. (a,b].
	 * @param isRightOpen true if upper bound is not itself valid, e.g. [a,b).
	 */
	public RealInterval(double a, double b, boolean isLeftOpen, boolean isRightOpen) {
		this.a = a;
		this.b = b;
		this.isLeftOpen = isLeftOpen;
		this.isRightOpen = isRightOpen;
	}
	
	/**
	 * Constructor. Creates an unbounded interval.
	 */
	public RealInterval() {
		this.a = Double.NEGATIVE_INFINITY;
		this.b = Double.POSITIVE_INFINITY;
		this.isLeftOpen = true;
		this.isRightOpen = true;
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
	 * @return the boundary type.
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
	
	/**
	 * Returns a random double uniformly drawn from this interval.
	 * Not extensively tested, and with several theoretic idiosyncrasies:
	 * <ul>
	 * <li>Cases where the span (b-a) > Double.MAX_VALUE are not supported.
	 *     This goes for some similar cases as well, e.g., (a,inf) for finite a < 0.</li>
	 * <li>For [a,b], endpoints will be returned half as often as any other number.</li>
	 * <li>For (-inf,inf), 0 will be returned twice as often as any other number.</li>
	 * </ul>
	 * 
	 * @param rng a (possibly pseudo) random number generator.
	 * @return a random double drawn using rng.
	 */
	public double getRandom(Random rng) {
		Type t = this.getType();
		if (t == Type.EMPTY) {
			throw new IllegalArgumentException("Cannot draw random double from empty interval.");
		}
		
		// We don't support some overflow-prone cases yet.
		double diff = this.b - this.a;
		if ((diff < 0) ||
				((t == Type.LEFT_OPEN || t == Type.LEFT_CLOSED) && this.a < 0) ||
				((t == Type.RIGHT_OPEN || t == Type.RIGHT_CLOSED) && this.b > 0)) {
			throw new IllegalArgumentException("Do not yet support random number from interval of this span.");
		}
		
		if (t == Type.DEGENERATE) {
			return this.a;
		}
		
		// Generate a PRN in (0,1) if we have any finite bound (a,... or ...,b),
		// otherwise generate a PRN in [0,1).
		double d = rng.nextDouble();
		if ((this.isLeftOpen && !Double.isInfinite(this.a)) ||
			(this.isRightOpen && !Double.isInfinite(this.b))) {
			while (d == 0) {
				d = rng.nextDouble();
			}
		}
		
		double lo = (Double.isInfinite(this.a) ? -Double.MAX_VALUE : this.a);
		double up = (Double.isInfinite(this.b) ? Double.MAX_VALUE : this.b);
		
		switch (t) {
		
		case OPEN:
		case LEFT_CLOSED_RIGHT_OPEN:
		case LEFT_OPEN:
		case LEFT_CLOSED:
		case RIGHT_OPEN:
			// Cases when we have (,) or [,).
			return ((up - lo) * d + lo);
			
		case LEFT_OPEN_RIGHT_CLOSED:
		case RIGHT_CLOSED:
			// Cases when we have (,].
			return ((up - lo) * (1.0 - d) + lo);
			
		case CLOSED:
			// Case when we have [,].
			// Under ideal precision we will return endpoints half as often.
			return (rng.nextBoolean() ?
					(up - lo) * d + lo :
					(up - lo) * (1.0 - d) + lo);
			
		case UNBOUNDED:
			// Case when we have (-inf,inf). Beware of overflow.
			// Under ideal precision we will return 0 twice as often,
			// and never +-Double.MAX_VALUE.
			return (rng.nextBoolean() ?
					d * Double.MAX_VALUE :
					-d * Double.MAX_VALUE);
			
		default:
			throw new IllegalArgumentException("Unknown error when producing random number in interval.");
		}
	}
}
