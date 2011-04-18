package se.cbb.jprime.math;

/**
 * Class for floating point numbers where the value is kept in log-form internally. 
 * As such, it enables higher precision than a regular
 * double for e.g. very small probabilities 0 <= p << 1, although it can be used for values greater
 * than 1 and negative values as well.
 * 
 * @author Bengt Sennblad.
 * @author Joel Sjöstrand.
 */
public final class Probability implements Comparable<Probability> {
	
	/**
	 * Log-value, sign of actual value discarded. If the actual value is 0, this may be set to anything,
	 * (although most often it will be set to Double.NEGATIVE_INFINITY.).
	 */
	private double p;
	
	/** Sign of actual value: 1 = positive, 0 = zero, -1 = negative. */
	private int sign;

	/**
	 * Constructor. Sets the probability to 0.
	 */
	public Probability() {
		this.p = Double.NEGATIVE_INFINITY;       // Dummy.
		this.sign = 0;
	}
	
	/**
	 * Constructor.
	 * @param d the actual value, i.e. non-logged.
	 */
	public Probability(double d) {
		assert !Double.isNaN(d);
		//assert !Double.isInfinite(d);

		if (d > 0.0) {
			this.p = Math.log(d);
			this.sign = 1;
		}
		else if (d == 0.0) {
			this.p = Double.NEGATIVE_INFINITY;      // Dummy.
			this.sign = 0;
		}
		else {
			this.p = Math.log(-d);
			this.sign = -1;
		}
	}
	
	/**
	 * Constructor.
	 * @param i the actual value, i.e. non-logged.
	 */
	public Probability(int i) {
		this((double) i);
	}
	
	/**
	 * Copy constructor.
	 * @param prob the Probability object to copy.
	 */
	Probability(Probability prob) {
		assert !Double.isNaN(prob.p);
		//assert !Double.isInfinite(prob.p);
		this.p = prob.p;
		this.sign = prob.sign;
	}
	
	/**
	 * Constructor for creating an instance from an already logged value.
	 * @param logValue the log-value. Of no importance if sign == 0.
	 * @param sign the sign: 1 = positive, 0 = zero, -1 = negative.
	 */
	public Probability(double logProb, int sign) {
		assert !Double.isNaN(logProb);
		//assert !Double.isInfinite(logProb);
		assert (sign >= -1 && sign <= 1);
		if (sign == 0) {
			this.p = Double.NEGATIVE_INFINITY;    // Dummy.
			this.sign = 0;
		} else if (sign <= -1) {
			this.p = logProb;
			this.sign = -1;
		} else {
			this.p = logProb;
			this.sign = 1;
		}
	}
	
	/**
	 * Returns log(|v|) for the actual value v. If v==sign==0, the returned
	 * value may be anything (although commonly Double.NEGATIVE_INFINITY).
	 * @return the log-value.
	 */
	public double getLogValue() {
		return this.p;
	}
	
	/**
	 * Returns the sign of the actual value, where 1 = positive, 0 = zero, -1 = negative.
	 * @return the sign.
	 */
	public int getSign() {
		return this.sign;
	}

	/**
	 * Returns the actual value (non-logged). There may of course be
	 * a loss of precision (small values may e.g. be rounded to 0). 
	 * @return the actual value.
	 */
	public double getValue() {
		switch (this.sign) {
		case 1:
			return Math.exp(this.p);
		case 0:
			return 0.0;
		case -1:
			return -Math.exp(this.p);
		default:
			throw new ArithmeticException("Sign of Probability instance has illegal value.");
		}
	}
	
	/**
	 * Returns true if the actual non-log value is zero.
	 * @return true if zero; false if not zero.
	 */
	public boolean isZero() {
		return (this.sign == 0);
	}
	
	/**
	 * Adds another Probability to this Probability. Does not yield a new instance; for that purpose,
	 * see <code>addToNew()</code>.
	 * @param q the Probability to add.
	 * @return this Probability, not a new instance.
	 */
	public Probability add(Probability q) {
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		assert !Double.isNaN(q.p);
		//assert !Double.isInfinite(q.p);
		switch (this.sign * q.sign) {
		case 1:
			add_(q);	// Sign should not change.
			break;
		case 0:
			this.p = (this.sign == 0 ? q.p : this.p);
			this.sign = (this.sign == 0 ? q.sign : this.sign);
			break;
		case -1:
			sub_(q);
			break;
		default:
			throw new ArithmeticException("Sign of Probability instance has illegal value.");
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as the sum of this Probability and another.
	 * See also <code>add()</code>.
	 * @param q the Probability to add.
	 * @return a new Probability.
	 */
	public Probability addToNew(Probability q) {
		return (new Probability(this)).add(q);
	}
	
	/**
	 * Adds another Probability to this Probability. Does not yield a new instance; for that purpose,
	 * see <code>subToNew()</code>.
	 * @param q the Probability to subtract.
	 * @return this Probability, not a new instance.
	 */
	public Probability sub(Probability q) {
		switch (this.sign * q.sign) {
		case 1:
			sub_(q);
			break;
		case 0:
			this.p = (this.sign == 0 ? q.p : this.p);
			this.sign = (this.sign == 0 ? -q.sign : this.sign);
			break;
		case -1:
			add_(q);
			this.sign = (this.sign == 1 ? -1 : this.sign);
			break;
		default:
			throw new ArithmeticException("Sign of Probability instance has illegal value.");
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as this Probability minus another.
	 * See also <code>sub()</code>.
	 * @param q the Probability to subtract.
	 * @return a new Probability.
	 */
	public Probability subToNew(Probability q) {
		return (new Probability(this)).sub(q);
	}
	
	/**
	 * Multiplies this Probability with another Probability. Does not yield a new instance; for that purpose,
	 * see the <code>multToNew()</code> method.
	 * @param q Probability to multiply with.
	 * @return this Probability, not a new instance.
	 */
	public Probability mult(Probability q) {
		this.sign *= q.sign;
		this.p = (this.sign == 0 ? 0.0 : this.p + q.p);
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as this Probability times another.
	 * See also <code>mult()</code>.
	 * @param q the Probability to multiply with.
	 * @return a new Probability.
	 */
	public Probability multToNew(Probability q) {
		return (new Probability(this)).mult(q);
	}

	/**
	 * Divides this Probability with another Probability. Does not yield a new instance; for that purpose,
	 * see the <code>divToNew()</code> method.
	 * @param q Probability to divide with.
	 * @return this Probability, not a new instance.
	 */
	public Probability div(Probability q) {
		if (q.sign == 0) {
			throw new ArithmeticException("Division by zero attempted in Probability.");
		}
		this.sign *= q.sign;
		this.p = (this.sign == 0 ? 0.0 : this.p - q.p);
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as this Probability divided by another.
	 * See also <code>div()</code>.
	 * @param q the Probability to divide by.
	 * @return a new Probability.
	 */
	public Probability divToNew(Probability q) {
		return (new Probability(this)).div(q);
	}

	/**
	 * Changes the sign of this Probability. See also <code>negToNew()</code>,
	 * @return this Probability, not a new instance.
	 */
	public Probability neg() {
		this.sign = -this.sign;
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new Probability equal to this but with opposite sign. See also
	 * <code>neg()</code>.
	 * @return a new Probability.
	 */
	public Probability negToNew() {
		return (new Probability(this)).neg();
	}
	
	/**
	 * Raises this Probability to a power. See also <code>powToNew()</code>.
	 * @param i the power.
	 * @return this Probability, not a new instance.
	 */
	public Probability pow(int i) {
		if (this.sign == 1) {
			this.p *= i;
		} else if (this.sign == 0) {
			if (i == 0) {
				this.p = 0;
				this.sign = 1;
			} else if (i < 0) {
				this.p = Double.POSITIVE_INFINITY;   // Well...
				this.sign = 1;
			}
		} else {
			this.p *= i;
			this.sign = (i % 2 == 0 ? 1 : -1);
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new Probability equal to this raised to a power.
	 * See also <code>pow()</code>.
	 * @param i the power.
	 * @return a new Probability.
	 */
	public Probability powToNew(int i) {
		return (new Probability(this)).pow(i);
	}
	
	/**
	 * Raises this Probability to a power. See also <code>powToNew()</code>.
	 * @param d the power.
	 * @return this Probability, not a new instance.
	 */
	public Probability pow(double d) {
		if (this.sign == 1) {
			this.p *= d;
		} else if (this.sign == 0) {
			if (d == 0.0) {
				this.p = 0;
				this.sign = 1;
			} else if (d < 0.0) {
				this.p = Double.POSITIVE_INFINITY;   // Well...
				this.sign = 1;
			}
		} else {
			throw new ArithmeticException("Cannot raise negative Probability to negative float power since" +
					" complex number representation not supported (even if power is in fact an integer).");
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new Probability equal to this raised to a power.
	 * See also <code>pow()</code>.
	 * @param d the power.
	 * @return a new Probability.
	 */
	public Probability powToNew(double d) {
		return (new Probability(this)).pow(d);
	}
	
	/**
	 * Sets the value of this Probability to e^v where v is the old value.
	 * See also <code>expToNew()</code>.
	 * @return this Probability.
	 */
	public Probability exp() {
		if (this.sign == 0) {
			this.p = 0;
			this.sign = 1;
		} else {
			this.p = this.getValue();
			this.sign = 1;
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new probability with value e^v where v is the value of this Probability.
	 * See also <code>exp()</code>.
	 * @return a new Probability.
	 */
	public Probability expToNew() {
		return (new Probability(this)).exp();
	}
	
	/**
	 * Sets the value of this Probability to ln(v) where v is the old value.
	 * For v==sign==0, sets the new value to negative infinity.
	 * See also <code>logToNew()</code> and <code>getLogValue()</code>.
	 * @return this Probability.
	 */
	public Probability log() {
		if (this.sign == 0) {
			this.p = Double.POSITIVE_INFINITY;
			this.sign = -1;
		} else if (this.sign < 0) {
			throw new ArithmeticException("Cannot take the natural logarithm of a non-positive number.");
		} else {
			if (this.p > 0.0) {
				this.p = Math.log(this.p);
				this.sign = 1;
			}
			else if (this.p == 0.0) {
				this.p = Double.NEGATIVE_INFINITY;      // Dummy.
				this.sign = 0;
			}
			else {
				this.p = Math.log(-this.p);
				this.sign = -1;
			}
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new probability with value ln(v) where v is the value of this Probability.
	 * See also <code>log()</code>.
	 * @return a new Probability.
	 */
	public Probability logToNew() {
		return (new Probability(this)).log();
	}
	
	@Override
	public int compareTo(Probability q) {
		if (this.sign == q.sign) {
			// We don't require sign==0 => p==Double.NEGATIVE_INFINITY.
			return (this.sign == 0 || this.p == q.p ? 0 : (this.sign * this.p > q.sign * q.p ? 1 : -1));
		}
		return (this.sign < q.sign ? -1 : 1);
	}
	
	/**
	 * Returns true if this Probability is greater than another Probability q.
	 * @param q the Probability to compare with.
	 * @return true if greater than q.
	 */
	public boolean greaterThan(Probability q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return false;
			case 1:  return (this.p > q.p);
			case -1: return (this.p < q.p);
			default: throw new ArithmeticException("Sign of Probability instance has illegal value.");
			}
		}
		return (this.sign > q.sign);
	}
	
	/**
	 * Returns true if this Probability is greater than or equals another Probability q.
	 * @param q the Probability to compare with.
	 * @return true if greater than or equals q.
	 */
	public boolean greaterThanOrEquals(Probability q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return true;
			case 1:  return (this.p >= q.p);
			case -1: return (this.p <= q.p);
			default: throw new ArithmeticException("Sign of Probability instance has illegal value.");
			}
		}
		return (this.sign > q.sign);
	}
	
	/**
	 * Returns true if this Probability is less than another Probability q.
	 * @param q the Probability to compare with.
	 * @return true if less than q.
	 */
	public boolean lessThan(Probability q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return false;
			case 1:  return (this.p < q.p);
			case -1: return (this.p > q.p);
			default: throw new ArithmeticException("Sign of Probability instance has illegal value.");
			}
		}
		return (this.sign < q.sign);
	}
	
	/**
	 * Returns true if this Probability is less than or equals another Probability q.
	 * @param q the Probability to compare with.
	 * @return true if less than or equals q.
	 */
	public boolean lessThanOrEquals(Probability q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return true;
			case 1:  return (this.p <= q.p);
			case -1: return (this.p >= q.p);
			default: throw new ArithmeticException("Sign of Probability instance has illegal value.");
			}
		}
		return (this.sign < q.sign);
	}

	/**
	 * Overridden hash code. Two Probability instances are considered equal if they represent
	 * the same number.
	 * @see java.lang.Object#hashCode().
	 * @return the hash code.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(p);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + sign;
		return result;
	}

	/**
	 * Overridden equality method. Two Probability instances are considered equal if they represent
	 * the same number.
	 * @see java.lang.Object#equals(java.lang.Object).
	 * @param obj the object to compare with.
	 * @return true if represinting the same number, otherwise false.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null || getClass() != obj.getClass()) { return false; }
		Probability q = (Probability) obj;
		if (this.sign != q.sign) { return false; }
		return (this.sign == 0 || this.p == q.p);
	}

	/**
	 * Helper. Adds a Probability to this object, sign issue assumed to be resolved already.
	 * @param q Probability to add.
	 */
	private void add_(Probability q) {
		// Joelgs: Don't know too much about this method with regards to performance
		// or numeric considerations -- ported from PrIME.
		if (this.p > q.p) {
			p = this.p + StrictMath.log1p(Math.exp(q.p - this.p));
		} else {
			p = q.p + StrictMath.log1p(Math.exp(this.p - q.p));
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
	}
	
	/**
	 * Helper. Subtracts a Probability from this object, sign issue assumed to be resolved already.
	 * @param q Probability to subtract.
	 */
	private void sub_(Probability q) {
		// Joelgs: Don't know too much about this method with regards to performance
		// or numeric considerations -- ported from PrIME.
		// In particular, notice use of log1pl instead of log1p in original class...
		if (this.p > q.p) {
			this.p = this.p + StrictMath.log1p(-Math.exp(q.p - this.p));
		} else if (this.p == q.p) {
			this.sign = 0;
			this.p = 0.0;   // Dummy.
		} else {
			this.p = q.p + StrictMath.log1p(-Math.exp(this.p - q.p));
			this.sign *= -1;
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
	}

	/**
	 * Returns the Probability as a string, most commonly in logged form. More
	 * specifically, where p := log(abs(actual value)), the following is returned:
	 * <ul>
	 * <li>sign = 1: returns p.</li>
	 * <li>sign = 0: returns -Double.MAX_VALUE.</li>
	 * <li>sign = -1: returns "neg(p)".
	 * </ul>
	 * @see java.lang.Object#toString().
	 * @return the Probability as a string.
	 */
	@Override
	public String toString() {
		switch (this.sign) {
		case 1:  return Double.toString(this.p);
		case 0:  return ('-' + Double.toString(Double.MAX_VALUE));
		case -1: return ("neg(" + Double.toString(this.p) + ")");
		default: throw new ArithmeticException("Sign of Probability instance has illegal value.");
		}
	}
	
	/**
	 * Tries to parse a string, mirroring the behaviour of toString().
	 * @param s the string.
	 * @return the contained Probability.
	 */
	public static Probability parseProbability(String s) {
		s = s.trim();
		// TODO: Replace with regexps.
		if (s.startsWith("neg(") && s.endsWith(")")) {
			return new Probability(Double.parseDouble(s.substring(4, s.length() - 1)), -1);
		}
		if (s.startsWith("-") && Double.parseDouble(s.substring(1)) == Double.MAX_VALUE) {
			return new Probability();
		}
		return new Probability(Double.parseDouble(s), 1);
	}

}
