package se.cbb.jprime.math;

/**
 * Class for floating point numbers where the value is kept in log-form internally.
 * As such, it enables higher precision than a regular
 * double for e.g. very small probabilities 0 <= p << 1, although it can be used for values greater
 * than 1 and negative values as well. This class is called <code>Probability</code>
 * in the original PrIME C++ package.
 * <p/>
 * NOTE: "log" here only refers to the internal representation;
 * one should think of a <code>LogDouble</code> instance as a floating point number like any other.
 * 
 * @author Bengt Sennblad.
 * @author Joel Sjöstrand.
 */
public final class LogDouble implements Comparable<LogDouble> {
	
	/**
	 * Log-value, sign of actual value discarded. If the actual value is 0, this may be set to anything,
	 * (although most often it will be set to Double.NEGATIVE_INFINITY.).
	 */
	private double p;
	
	/** Sign of actual value: 1 = positive, 0 = zero, -1 = negative. */
	private int sign;

	/**
	 * Constructor. Sets the LogDouble to 0.
	 */
	public LogDouble() {
		this.p = Double.NEGATIVE_INFINITY;       // Dummy.
		this.sign = 0;
	}
	
	/**
	 * Constructor.
	 * @param d the actual value, i.e. non-logged.
	 */
	public LogDouble(double d) {
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
	public LogDouble(int i) {
		this((double) i);
	}
	
	/**
	 * Copy constructor.
	 * @param prob the LogDouble object to copy.
	 */
	LogDouble(LogDouble prob) {
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
	public LogDouble(double logProb, int sign) {
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
			throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
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
	 * Adds another LogDouble to this LogDouble. Does not yield a new instance; for that purpose,
	 * see <code>addToNew()</code>.
	 * @param q the LogDouble to add.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble add(LogDouble q) {
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
			throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Adds a double to this LogDouble. Does not yield a new instance; for that purpose,
	 * see <code>addToNew()</code>.
	 * @param q the double to add.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble add(double q) {
		return this.add(new LogDouble(q));
	}
	
	/**
	 * Produces a new LogDouble instance as the sum of this LogDouble and another.
	 * See also <code>add()</code>.
	 * @param q the LogDouble to add.
	 * @return a new LogDouble.
	 */
	public LogDouble addToNew(LogDouble q) {
		return (new LogDouble(this)).add(q);
	}
	
	/**
	 * Produces a new LogDouble instance as the sum of this LogDouble and a double.
	 * See also <code>add()</code>.
	 * @param q the double to add.
	 * @return a new LogDouble.
	 */
	public LogDouble addToNew(double q) {
		return (new LogDouble(this)).add(q);
	}
	
	/**
	 * Adds another LogDouble to this LogDouble. Does not yield a new instance; for that purpose,
	 * see <code>subToNew()</code>.
	 * @param q the LogDouble to subtract.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble sub(LogDouble q) {
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
			throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Adds a double to this LogDouble. Does not yield a new instance; for that purpose,
	 * see <code>subToNew()</code>.
	 * @param q the double to subtract.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble sub(double q) {
		return this.sub(new LogDouble(q));
	}
	
	/**
	 * Produces a new LogDouble instance as this LogDouble minus another.
	 * See also <code>sub()</code>.
	 * @param q the LogDouble to subtract.
	 * @return a new LogDouble.
	 */
	public LogDouble subToNew(LogDouble q) {
		return (new LogDouble(this)).sub(q);
	}
	
	/**
	 * Produces a new LogDouble instance as this LogDouble minus a double.
	 * See also <code>sub()</code>.
	 * @param q the double to subtract.
	 * @return a new LogDouble.
	 */
	public LogDouble subToNew(double q) {
		return (new LogDouble(this)).sub(q);
	}
	
	/**
	 * Multiplies this LogDouble with another LogDouble. Does not yield a new instance; for that purpose,
	 * see the <code>multToNew()</code> method.
	 * @param q LogDouble to multiply with.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble mult(LogDouble q) {
		this.sign *= q.sign;
		this.p = (this.sign == 0 ? 0.0 : this.p + q.p);
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Multiplies this LogDouble with a double. Does not yield a new instance; for that purpose,
	 * see the <code>multToNew()</code> method.
	 * @param q double to multiply with.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble mult(double q) {
		return this.mult(new LogDouble(q));
	}
	
	/**
	 * Produces a new LogDouble instance as this LogDouble times another.
	 * See also <code>mult()</code>.
	 * @param q the LogDouble to multiply with.
	 * @return a new LogDouble.
	 */
	public LogDouble multToNew(LogDouble q) {
		return (new LogDouble(this)).mult(q);
	}
	
	/**
	 * Produces a new LogDouble instance as this LogDouble times a double.
	 * See also <code>mult()</code>.
	 * @param q the double to multiply with.
	 * @return a new LogDouble.
	 */
	public LogDouble multToNew(double q) {
		return (new LogDouble(this)).mult(q);
	}

	/**
	 * Divides this LogDouble with another LogDouble. Does not yield a new instance; for that purpose,
	 * see the <code>divToNew()</code> method.
	 * @param q LogDouble to divide with.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble div(LogDouble q) {
		if (q.sign == 0) {
			throw new ArithmeticException("Division by zero attempted in LogDouble.");
		}
		this.sign *= q.sign;
		this.p = (this.sign == 0 ? 0.0 : this.p - q.p);
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Divides this LogDouble with a double. Does not yield a new instance; for that purpose,
	 * see the <code>divToNew()</code> method.
	 * @param q double to divide with.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble div(double q) {
		return this.div(new LogDouble(q));
	}
	
	/**
	 * Produces a new LogDouble instance as this LogDouble divided by another.
	 * See also <code>div()</code>.
	 * @param q the LogDouble to divide by.
	 * @return a new LogDouble.
	 */
	public LogDouble divToNew(LogDouble q) {
		return (new LogDouble(this)).div(q);
	}
	
	/**
	 * Produces a new LogDouble instance as this LogDouble divided by a double.
	 * See also <code>div()</code>.
	 * @param q the double to divide by.
	 * @return a new LogDouble.
	 */
	public LogDouble divToNew(double q) {
		return (new LogDouble(this)).div(q);
	}

	/**
	 * Changes the sign of this LogDouble. See also <code>negToNew()</code>,
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble neg() {
		this.sign = -this.sign;
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new LogDouble equal to this but with opposite sign. See also
	 * <code>neg()</code>.
	 * @return a new LogDouble.
	 */
	public LogDouble negToNew() {
		return (new LogDouble(this)).neg();
	}
	
	/**
	 * Raises this LogDouble to a power. See also <code>powToNew()</code>.
	 * @param i the power.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble pow(int i) {
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
	 * Returns a new LogDouble equal to this raised to a power.
	 * See also <code>pow()</code>.
	 * @param i the power.
	 * @return a new LogDouble.
	 */
	public LogDouble powToNew(int i) {
		return (new LogDouble(this)).pow(i);
	}
	
	/**
	 * Raises this LogDouble to a power. See also <code>powToNew()</code>.
	 * @param d the power.
	 * @return this LogDouble, not a new instance.
	 */
	public LogDouble pow(double d) {
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
			throw new ArithmeticException("Cannot raise negative LogDouble to negative float power since" +
					" complex number representation not supported (even if power is in fact an integer).");
		}
		assert !Double.isNaN(this.p);
		//assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new LogDouble equal to this raised to a power.
	 * See also <code>pow()</code>.
	 * @param d the power.
	 * @return a new LogDouble.
	 */
	public LogDouble powToNew(double d) {
		return (new LogDouble(this)).pow(d);
	}
	
	/**
	 * Sets the value of this LogDouble to e^v where v is the old value.
	 * See also <code>expToNew()</code>.
	 * @return this LogDouble.
	 */
	public LogDouble exp() {
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
	 * Returns a new LogDouble with value e^v where v is the value of this LogDouble.
	 * See also <code>exp()</code>.
	 * @return a new LogDouble.
	 */
	public LogDouble expToNew() {
		return (new LogDouble(this)).exp();
	}
	
	/**
	 * Sets the value of this LogDouble to ln(v) where v is the old value.
	 * For v==sign==0, sets the new value to negative infinity.
	 * See also <code>logToNew()</code> and <code>getLogValue()</code>.
	 * @return this LogDouble.
	 */
	public LogDouble log() {
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
	 * Returns a new LogDouble with value ln(v) where v is the value of this LogDouble.
	 * See also <code>log()</code>.
	 * @return a new LogDouble.
	 */
	public LogDouble logToNew() {
		return (new LogDouble(this)).log();
	}
	
	@Override
	public int compareTo(LogDouble q) {
		if (this.sign == q.sign) {
			// We don't require sign==0 => p==Double.NEGATIVE_INFINITY.
			return (this.sign == 0 || this.p == q.p ? 0 : (this.sign * this.p > q.sign * q.p ? 1 : -1));
		}
		return (this.sign < q.sign ? -1 : 1);
	}
	
	/**
	 * Returns true if this LogDouble is greater than another LogDouble q.
	 * @param q the LogDouble to compare with.
	 * @return true if greater than q.
	 */
	public boolean greaterThan(LogDouble q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return false;
			case 1:  return (this.p > q.p);
			case -1: return (this.p < q.p);
			default: throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
			}
		}
		return (this.sign > q.sign);
	}
	
	/**
	 * Returns true if this LogDouble is greater than a double q.
	 * @param q the double to compare with.
	 * @return true if greater than q.
	 */
	public boolean greaterThan(double q) {
		return this.greaterThan(new LogDouble(q));
	}
	
	/**
	 * Returns true if this LogDouble is greater than or equals another LogDouble q.
	 * @param q the LogDouble to compare with.
	 * @return true if greater than or equals q.
	 */
	public boolean greaterThanOrEquals(LogDouble q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return true;
			case 1:  return (this.p >= q.p);
			case -1: return (this.p <= q.p);
			default: throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
			}
		}
		return (this.sign > q.sign);
	}
	
	/**
	 * Returns true if this LogDouble is greater than or equals a double q.
	 * @param q the double to compare with.
	 * @return true if greater than or equals q.
	 */
	public boolean greaterThanOrEquals(double q) {
		return this.greaterThanOrEquals(new LogDouble(q));
	}
	
	/**
	 * Returns true if this LogDouble is less than another LogDouble q.
	 * @param q the LogDouble to compare with.
	 * @return true if less than q.
	 */
	public boolean lessThan(LogDouble q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return false;
			case 1:  return (this.p < q.p);
			case -1: return (this.p > q.p);
			default: throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
			}
		}
		return (this.sign < q.sign);
	}
	
	/**
	 * Returns true if this LogDouble is less than a double q.
	 * @param q the double to compare with.
	 * @return true if less than q.
	 */
	public boolean lessThan(double q) {
		return this.lessThan(new LogDouble(q));
	}
	
	/**
	 * Returns true if this LogDouble is less than or equals another LogDouble q.
	 * @param q the LogDouble to compare with.
	 * @return true if less than or equals q.
	 */
	public boolean lessThanOrEquals(LogDouble q) {
		if (this.sign == q.sign) {
			switch (this.sign) {
			case 0:  return true;
			case 1:  return (this.p <= q.p);
			case -1: return (this.p >= q.p);
			default: throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
			}
		}
		return (this.sign < q.sign);
	}
	
	/**
	 * Returns true if this LogDouble is less than or equals a double q.
	 * @param q the double to compare with.
	 * @return true if less than or equals q.
	 */
	public boolean lessThanOrEquals(double q) {
		return this.lessThanOrEquals(new LogDouble(q));
	}

	/**
	 * Overridden hash code. Two LogDouble instances are considered equal if they represent
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
	 * Overridden equality method. Two LogDouble instances are considered equal if they represent
	 * the same number. Beware, though, floating point equality comparisons are always dangerous...
	 * @see java.lang.Object#equals(java.lang.Object).
	 * @param obj the object to compare with.
	 * @return true if representing the same number, otherwise false.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null || getClass() != obj.getClass()) { return false; }
		LogDouble q = (LogDouble) obj;
		if (this.sign != q.sign) { return false; }
		return (this.sign == 0 || this.p == q.p);
	}

	/**
	 * Helper. Adds a LogDouble to this object, sign issue assumed to be resolved already.
	 * @param q LogDouble to add.
	 */
	private void add_(LogDouble q) {
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
	 * Helper. Subtracts a LogDouble from this object, sign issue assumed to be resolved already.
	 * @param q LogDouble to subtract.
	 */
	private void sub_(LogDouble q) {
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
	 * Returns the LogDouble as a string, most commonly in logged form. More
	 * specifically, where p := log(abs(actual value)), the following is returned:
	 * <ul>
	 * <li>sign = 1: returns p.</li>
	 * <li>sign = 0: returns -Double.MAX_VALUE.</li>
	 * <li>sign = -1: returns "neg(p)".
	 * </ul>
	 * @see java.lang.Object#toString().
	 * @return the LogDouble as a string.
	 */
	@Override
	public String toString() {
		switch (this.sign) {
		case 1:  return Double.toString(this.p);
		case 0:  return ('-' + Double.toString(Double.MAX_VALUE));
		case -1: return ("neg(" + Double.toString(this.p) + ")");
		default: throw new ArithmeticException("Sign of LogDouble instance has illegal value.");
		}
	}
	
	/**
	 * Tries to parse a string, mirroring the behaviour of toString().
	 * @param s the string.
	 * @return the contained LogDouble.
	 */
	public static LogDouble parseLogDouble(String s) {
		s = s.trim();
		// TODO: Replace with regexps.
		if (s.startsWith("neg(") && s.endsWith(")")) {
			return new LogDouble(Double.parseDouble(s.substring(4, s.length() - 1)), -1);
		}
		if (s.startsWith("-") && Double.parseDouble(s.substring(1)) == Double.MAX_VALUE) {
			return new LogDouble();
		}
		return new LogDouble(Double.parseDouble(s), 1);
	}

}
