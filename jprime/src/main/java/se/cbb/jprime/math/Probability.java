package se.cbb.jprime.math;

/**
 * Class for holding a floating point number in a logged form. As such, it enables higher precision than a regular
 * double for e.g. very small probabilities 0 <= p << 1, although it can be used for negative numbers as well.
 * 
 * @author Bengt Sennblad.
 * @author Joel Sjöstrand.
 */
public final class Probability implements Comparable<Probability> {
	
	/**
	 * Log-value, sign of actual value discarded. If the actual value is 0, this may be set to anything,
	 * (although most often it will be set to 0.0).
	 */
	private double p;
	
	/** Sign of actual value: 1 = positive, 0 = zero, -1 = negative. */
	private int sign;

	/**
	 * Constructor. Sets the probability to 0.
	 */
	public Probability() {
		this.p = 0.0;       // Dummy.
		this.sign = 0;
	}
	
	/**
	 * Constructor.
	 * @param d the actual value, i.e. non-logged.
	 */
	public Probability(double d) {
		assert !Double.isNaN(d);
		assert !Double.isInfinite(d);

		if (d > 0.0) {
			this.p = Math.log(d);
			this.sign = 1;
		}
		else if (d == 0.0) {
			this.p = 0.0;      // Dummy.
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
		assert !Double.isInfinite(prob.p);
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
		assert !Double.isInfinite(logProb);
		assert (sign >= -1 && sign <= 1);
		if (sign == 0) {
			this.p = 0.0;    // Dummy.
			this.sign = 0;
		} else if (sign <= -1) {
			this.p = logProb;
			this.sign = -1;
		} else {
			this.p = logProb;
			this.p = 1;
		}
	}
	
	/**
	 * Returns the log-value, sign of actual value discarded. If the actual value == sign == 0,
	 * the returned value may be anything.
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
	 * Returns the value non-logged. There may of course be
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
	 * Adds another Probability to this Probability. Does not yield a new instance; for that purpose,
	 * see <code>addToNew()</code>.
	 * @param q the Probability to add.
	 * @return this Probability, not a new instance.
	 */
	public Probability add(Probability q) {
		assert !Double.isNaN(this.p);
		assert !Double.isInfinite(this.p);
		assert !Double.isNaN(q.p);
		assert !Double.isInfinite(q.p);
		switch (this.sign * q.sign) {
		case 1:
			add_(q);	// Sign should not change.
			break;
		case 0:
			this.p = (this.sign == 0 ? q.p : this.p);
			this.sign = (this.sign == 0 ? q.sign : this.sign);
			break;
		case -1:
			subtract_(q);
			break;
		default:
			throw new ArithmeticException("Sign of Probability instance has illegal value.");
		}
		assert !Double.isNaN(this.p);
		assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as the sum of this Probability and another.
	 * See also <code>add()</code>.
	 * @param q the Probability to add.
	 * @return a new Probability.
	 */
	public Probability addAsNew(Probability q) {
		return (new Probability(this)).add(q);
	}
	
	/**
	 * Adds another Probability to this Probability. Does not yield a new instance; for that purpose,
	 * see <code>subtractToNew()</code>.
	 * @param q the Probability to subtract.
	 * @return this Probability, not a new instance.
	 */
	public Probability subtract(Probability q) {
		switch (this.sign * q.sign) {
		case 1:
			subtract_(q);
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
		assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as this Probability minus another.
	 * See also <code>subtract()</code>.
	 * @param q the Probability to subtract.
	 * @return a new Probability.
	 */
	public Probability subtractAsNew(Probability q) {
		return (new Probability(this)).subtract(q);
	}
	
	/**
	 * Multiplies this Probability with another Probability. Does not yield a new instance; for that purpose,
	 * see the <code>multiplyToNew()</code> method.
	 * @param q Probability to multiply with.
	 * @return this Probability, not a new instance.
	 */
	public Probability multiply(Probability q) {
		this.sign *= q.sign;
		this.p = (this.sign == 0 ? 0.0 : this.p + q.p);
		assert !Double.isNaN(this.p);
		assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as this Probability times another.
	 * See also <code>multiply()</code>.
	 * @param q the Probability to multiply with.
	 * @return a new Probability.
	 */
	public Probability multiplyAsNew(Probability q) {
		return (new Probability(this)).multiply(q);
	}

	/**
	 * Divides this Probability with another Probability. Does not yield a new instance; for that purpose,
	 * see the <code>divideToNew()</code> method.
	 * @param q Probability to divide with.
	 * @return this Probability, not a new instance.
	 */
	public Probability divide(Probability q) {
		if (q.sign == 0) {
			throw new ArithmeticException("Division by zero attempted in Probability.");
		}
		this.sign *= q.sign;
		this.p = (this.sign == 0 ? 0.0 : this.p - q.p);
		assert !Double.isNaN(this.p);
		assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Produces a new Probability instance as this Probability divided by another.
	 * See also <code>divide()</code>.
	 * @param q the Probability to divide by.
	 * @return a new Probability.
	 */
	public Probability divideAsNew(Probability q) {
		return (new Probability(this)).divide(q);
	}

	/**
	 * Changes the sign of this Probability.
	 * @return this Probability, not a new instance.
	 */
	public Probability negate() {
		this.sign = -this.sign;
		assert !Double.isNaN(this.p);
		assert !Double.isInfinite(this.p);
		return this;
	}
	
	/**
	 * Returns a new Probability equal to this but with opposite sign
	 * @return a new Probability.
	 */
	public Probability negateAsNew() {
		return (new Probability(this)).negate();
	}
	
	@Override
	public int compareTo(Probability q) {
		if (this.sign == q.sign) {
			// We don't require sign==0 => p==0.
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
		case 1:  return ("" + this.p);
		case 0:  return ("-" + Double.MAX_VALUE);
		case -1: return ("neg(" + this.p + ")");
		default: throw new ArithmeticException("Sign of Probability instance has illegal value.");
		}
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
		assert !Double.isInfinite(this.p);
	}
	
	
	public void subtract_(Probability q) {
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
		assert !Double.isInfinite(this.p);
	}

	
//	class Probability;
//	  // Forward declarations of friend functions
//	     Probability pow(const Probability& p, const double& n); // Why refs?
//	     Probability exp(const Probability& p);
//	     Probability log(const Probability& p);
//	     Probability probFact(unsigned u);
//	     Probability probBinom(unsigned u1, unsigned u2);
//
//
//
//	  class Probability 
//	  {
//	  public:
//
//	    //---------------------------------------------------------------------
//	    //
//	    //Power and exp of a Probability and unary negation operator
//	    //
//	    //---------------------------------------------------------------------
//	    //   friend Probability pow(const Probability& p, const unsigned& n);
//	    //   friend Probability pow(const Probability& p, const int& n);
//	    friend Probability pow(const Probability& p, const double& n); // Why refs?
//	    friend Probability exp(const Probability& p);
//	    friend Probability log(const Probability& p);
//
//	    //---------------------------------------------------------------------
//	    //
//	    //Factorial and binomial ("u1 choose u2") of sunsigneds  
//	    //using logarithms - returniong Probabilities
//	    //
//	    //---------------------------------------------------------------------
//	    friend Probability probFact(unsigned u);
//	    friend Probability probBinom(unsigned u1, unsigned u2);
//
//	  public:
//	    //---------------------------------------------------------------------
//	    // mpi serialization functions
//	    //---------------------------------------------------------------------
//	    friend class boost::serialization::access; 
//	  
//	    template<class Archive> 
//	    void serialize(Archive & ar, const unsigned int version)
//	    {
//	      ar & p;
//	      ar & sign;
//	    }
//
//	  private:
//	    //----------------------------------------------------------------------
//	    //
//	    //Helper arithmetics finctions
//	    //
//	    //----------------------------------------------------------------------
//	  
//	    //Helper for addition and subtraction of two Probabilities
//	    //---------------------------------------------------------------------
//	    void add(const Probability& q);
//	    void subtract(const Probability& q);
//
//

	
	
	
	////////////////////////////////////////////////////////////////
	
	
//	  //---------------------------------------------------------------------
//	  //
//	  //Power and exp of a Probability and unary negation operator
//	  //
//	  //---------------------------------------------------------------------
//	  // Probability 
//	  // pow(const Probability& p, const unsigned& d)       
//	  // { 
//	  //   if(p.sign == 1)
//	  //     {
//	  //       Probability q(p);
//	  //       q.p = d * p.p;
//	  //       return q;
//	  //     }
//	  //   else if(p.sign == 0)
//	  //     {
//	  //       return p;
//	  //     }
//	  //   else
//	  //     {				// What? Can (-0.4)^2.1 really be a complex number?
//	  // 				// And what about (-0.4)^2 ?    /arve
//	  //       throw AnError("Probability.pow(int d) with a negative Probability "
//	  // 		    "may imply an imaginary number; this is not handled by "
//	  // 		    "Probability (...yet)");
//	  //     }
//	  // };
//	  // Probability 
//	  // pow(const Probability& p, const int& d)       
//	  // { 
//	  //   if(p.sign == 1)
//	  //     {
//	  //       Probability q(p);
//	  //       q.p = d * p.p;
//	  //       return q;
//	  //     }
//	  //   else if(p.sign == 0)
//	  //     {
//	  //       return p;
//	  //     }
//	  //   else
//	  //     {				// What? Can (-0.4)^2.1 really be a complex number?
//	  // 				// And what about (-0.4)^2 ?    /arve
//	  //       throw AnError("Probability.pow(int d) with a negative Probability "
//	  // 		    "may imply an imaginary number; this is not handled by "
//	  // 		    "Probability (...yet)");
//	  //     }
//	  // };
//
//	  Probability 
//	  pow(const Probability& p, const double& d)       
//	  { 
//	    assert(isnan(d) == false);
//	    assert(isnan(p.p) == false);
//	    assert(isinf(d) == false);
//	    assert(isinf(p.p) == false);
//	    if(p.sign == 1)
//	      {
//		Probability q(p);
//		q.p = d * p.p;
//		return q;
//	      }
//	    else if(p.sign == 0)
//	      {
//		if(d == 0)
//		  return 1.0;
//		else
//		  return p;
//	      }
//	    else
//	      {
//		throw AnError("Probability.pow(double d) with a negative Probability "
//			      "may imply an imaginary number; this is not handled by "
//			      "Probability (...yet)", 1);
//	      }
//	  };
//
//	  Probability 
//	  exp(const Probability& p)         
//	  { 
//	    Probability q(1.0);
//	    q.p = p.val();
//	    assert(isnan(q.p) == false);
//	    assert(isinf(q.p) == false);
//	    return q;
//	  };
//
//	  Probability 
//	  log(const Probability& p)         
//	  { 
//	    if(p.sign <= 0)
//	      {
//		throw AnError("Can't log a negative number or zero\n", 1);
//	      }
//	    Probability q(p.p);
//	    assert(isnan(q.p) == false);
//	    assert(isinf(q.p) == false);
//	    return q;
//	  };
//
//	  //---------------------------------------------------------------------
//	  //
//	  //Factorial and binomial ("u1 choose u2") of sunsigneds  
//	  //using logarithms - returning Probabilities
//	  //
//	  //---------------------------------------------------------------------
//	  Probability
//	  probFact(unsigned u)         
//	  {
//	    Probability q;
//	    while(u > 0)
//	      {	
//		q.p = q.p + std::log((double)u);
//		u--;
//	      }
//	    q.sign = 1;
//	    assert(isnan(q.p) == false);
//	    assert(isinf(q.p) == false);
//	    return q;  
//	  };
//
//	  Probability
//	  probBinom(unsigned u1, unsigned u2)
//	  {
//	    if(u1 >= u2)
//	      {
//		Probability q = (probFact(u1) / (probFact(u2) * probFact(u1 - u2)));
//		assert(isnan(q.p) == false);
//		assert(isinf(q.p) == false);
//		return q;
//	      }
//	    else
//	      {
//		std::cerr<< "******************** \n Incompatibel terms in binomial \n ******************+n";
//		throw AnError("first term in binomial must not be less than second", 1);
//	      }
//	  }
//
//

//
//	//   //private:
//	//   //---------------------------------------------------------------------
//	//   // mpi serialization functions
//	//   //---------------------------------------------------------------------
//	//   template<class Archive> 
//	//   void 
//	//   Probability::serialize(Archive& ar, const unsigned int version) 
//	//   {
////	     ar & p;
////	     ar & sign;
//	//   }
//
//

}
