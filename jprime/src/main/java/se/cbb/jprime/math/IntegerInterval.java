package se.cbb.jprime.math;

import java.util.Random;

/**
 * Represents a 1-D integer valued interval of the type [a,b], (-inf,inf), etc.
 * Negative and positive infinity are represented by Integer.MIN_VALUE and
 * Integer.MAX_VALUE respectively.
 * 
 * @author Joel Sjöstrand.
 */
public class IntegerInterval {

	/** Interval bound types. */
	public enum Type {
		/** Empty set. */      EMPTY,
		/** a=b. */            DEGENERATE,
		/** a,...,b. */        CLOSED,
		/** a,...,inf. */      LOWER_BOUNDED,
		/** -inf,...,b. */     UPPER_BOUNDED,
		/** -inf,...,inf. */   UNBOUNDED
	}
	
	/** Lower bound. */
	private int a;
	
	/** Upper bound */
	private int b;
	
	/**
	 * Constructor.
	 * @param a lower bound. Integer.MIN_VALUE is interpreted as negative infinity.
	 * @param b upper bound. Integer.MAX_VALUE is interpreted as positive infinity.
	 */
	public IntegerInterval(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	/**
	 * Constructor. Creates an unbounded interval.
	 */
	public IntegerInterval() {
		this.a = Integer.MIN_VALUE;
		this.b = Integer.MAX_VALUE;
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
	public void setLowerBound(int a) {
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
	public void setUpperBound(int b) {
		this.b = b;
	}
	
	/**
	 * Returns the number of elements of this interval, 
	 * e.g. 5 for [0,4] or 0 for [3,3].
	 * @return the number of elements of the interval.
	 */
	public int getSize() {
		return (this.b -  this.a + 1);
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
			return (a == Integer.MIN_VALUE || a == Integer.MAX_VALUE ? Type.EMPTY : Type.DEGENERATE);
		}
		if (a == Integer.MIN_VALUE) {
			return (b == Integer.MAX_VALUE ? Type.UNBOUNDED : Type.UPPER_BOUNDED);
		}
		if (b == Integer.MAX_VALUE) {
			return Type.LOWER_BOUNDED;
		}
		return Type.CLOSED;
	}
	
	/**
	 * Returns true if a specified value is within the interval.
	 * @param value the value.
	 * @return true if within; false if outside.
	 */
	public boolean isWithin(int value) {
		return (value >= this.a && value <= this.b);
	}
	
	/**
	 * Returns a random int uniformly drawn from this interval.
	 * @param rng a (possibly pseudo) random number generator.
	 * @return a random int drawn using rng.
	 */
	public int getRandom(Random rng) {
		if (this.a > this.b) {
			throw new IllegalArgumentException("Cannot draw random int from empty interval.");
		}
		// Naïve implementation which at least avoids overflow caveats.
		int diff = this.b - this.a;
		if (diff >= 0 && diff != Integer.MAX_VALUE) {
			return (this.a + rng.nextInt(diff + 1));
		}
		int i;
		do {
			i = rng.nextInt();
		} while (i < this.a || i > this.b);
		return i;
	}
	
}
