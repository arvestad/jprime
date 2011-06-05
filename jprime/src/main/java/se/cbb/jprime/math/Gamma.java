package se.cbb.jprime.math;

/**
 * Not a gamma distribution per se; instead contains
 * convenience methods concerning gamma functions.
 * Returning logarithms is often necessary to avoid overflow.
 * 
 * @author Joel Sjöstrand.
 */
public class Gamma {
	
	/** 14 Lanczos coefficients for estimating ln(Gamma(xx)).  */
	public static double[] LANCZOS_COEF = {
		 57.1562356658629235,
		-59.5979603554754912,
		 14.1360979747417471,
		 -0.491913816097520199,
		   .339946499848118887e-4,
		   .465236289270485756e-4,
		  -.983744753048795646e-4,
		   .158088703224912494e-3,
		  -.210264441724104883e-3,
		   .217439618115212643e-3,
		  -.164318106536763890e-3,
		   .844182239838527433e-4,
		  -.261908384015814087e-4,
		   .368991826595316234e-5
		};
	
	public static final double RATIONAL_671_128 = 5.24218750000000000;
	
	public static final double SER = 0.999999999999997092;
	
	public static final double SQRT_2_PI = Math.sqrt(2 * Math.PI);
	
	/**
	 * Computes the natural logarithm of the complete gamma function,
	 * i.e., ln(Gamma(xx)) for xx > 0, where
	 * Gamma(x) = integral(t^(x-1) e^(-t), t=0,...,inf).
	 * Uses Lanczos' approximation formula, see Numerical Recipes 6.1.
	 * No bounds checking; see also <code>lnGammaSafe(xx)</code>.
	 * @param xx input parameter > 0.
	 * @return ln(Gamma(xx)) where Gamma(.) is the complete gamma function.
	 */
	public static double lnGamma(double xx) {
		double x = xx;
		double y = xx;
		double tmp = x + RATIONAL_671_128;
		tmp = (x + 0.5) * Math.log(tmp) - tmp;
		double ser = SER;
		for (int j = 0; j < 14; ++j) {
			ser += LANCZOS_COEF[j] / ++y;
		}
		return (tmp + Math.log(SQRT_2_PI * ser / x));
	}
	
	/**
	 * Identical to <code>lnGamma(xx)</code>, but verifies that xx > 0 before
	 * computation.
	 * @param xx input parameter > 0.
	 * @return ln(Gamma(xx)) where Gamma(.) is the complete gamma function.
	 */
	public static double lnGammaSafe(double xx) {
		if (xx <= 0.0) {
			throw new IllegalArgumentException("Cannot compute complete gamma function for x <= 0.");
		}
		return lnGamma(xx);
	}

	
//	//-----------------------------------------------------------------------
//    //
//    // Returns percentage points for the gamma distribution, i.e.
//    // the inverse of the cdf function. Note that notation of beta is such
//    // that mean=alpha/beta. Sometimes theta=1/beta is used instead.
//    //
//    //-----------------------------------------------------------------------
//  Real ppGamma(const Real& p, const Real& alpha, const Real& beta)
//  {
//    assert(alpha > 0.0 && beta > 0.0);// && p > 0.000002);      
//    return ppchi2(p, 2 * alpha)/(2 * beta);
//  }
//  
//
//  //------------------------------------------------------------------
//    //
//    //! Algorithm As 91,
//    //! Reference: Best & Roberts, 1975, Appl. Statist 24(3):385-388
//    //!
//    //! To evaluate the percentage points of the chi-squared
//    //! probability distribution function, i.e. it is the inverse of
//    //! chi-squared's cdf function.
//    //! P must lie in the range 0.000002 to 0.999998, V must be positive,
//    //! G must be supplied and should be equal to ln(gamma(V/2.0))
//    //!
//    //! INPUT;   P and V
//    //!          P; Is the cutoff probability
//    //!	    V; Degree of freedom   V = 2*alpha
//    //!
//    //!  Method; Evaluates the Percentage point of the
//    //!          chi2 square distribution
//    //!          with the probability P and V degree of freedom
//    //!          to obtain the percentage point to the gamma distribution
//    //!
//    //!  Output; The Percentage point of chi2 square distribution with
//    //!         probability P and V degree of freedom.
//    //
//    //------------------------------------------------------------------
//  Real ppchi2(const Real& P, const Real& V)
//  {
//    // TODO: Instead of throwing error - correct p? /bens
//    if (P <= 0 || P >= 1 || V < 0.0 )//(P < 2e-6 || P > 0.999998 || V < 0.0 )
//      {
//	ostringstream oss;
//	oss << "PerPoint_CHI2: Percentage point, P = " 
//	    << P
//	    << ", is not in range 0.000002-0.999998, and degrees of freedom, V = "
//	    << V
//	    << ", is not > 0 and < the numeric limit of double";
//	throw AnError(oss.str());
//      }
//    Real E      = 0.5e-6;
//    Real AA     = log(2.0);
////     Real ppchi2 = -1.0;
//    Real XX     = 0.5 *V;
//    Real C      = XX - 1.0;
//    Real G      = lgamma(XX);//log(Gamma(XX));  
//  
//    Real ch;
//    if(V < (-1.24 *log(P)))   // approximation for small chi-squares
//      {
//	ch = std::pow(P * XX * std::exp(G + XX * AA), 1.0 / XX);     
//	if (ch < E)
//	  {
//	    return ch;
//	  }
//      }  
//    else 
//      {
//	if(V <= 0.32)   // approximation for v <= 0.32 newton raphson metod
//	  {
//	    ch = 0.4;
//	    Real a = log(1.0 - P);
//	    Real Q;
//	    do
//	      {
//		Q  = ch;
//		Real P1 = 1.0 + ch * (4.67 + ch);
//		Real P2 = ch * (6.73 + ch * (6.66 + ch));
//		Real T  = -0.5 + (4.67 + 2.0 * ch) 
//		  / P1 - (6.73 + ch * (13.32 + 3.0 * ch)) / P2;
//		ch  = ch - (1.0 - std::exp(a + G + 0.5 * ch + C * AA) 
//			    * P2/ P1) / T;
//	      }
//	    while (abs(Q / ch - 1) > 0.01); 
//	  }
//	else
//	  {
//	    // Call to Algorithm As 70 - note that p have been tested above
//	    Real X = gauinv(P);
//	    //starting approximation using Wilson and Hilferty estimate
//	    Real P1 = 0.222222 / V;
//	    ch = V * pow((X * sqrt(P1) + 1.0 - P1), 3);
//
//	    if (ch > (2.2 * V + 6.0)) // Approximation for P tending to 1
//	      {
//		ch = -2.0 * (log(1.0 - P) - C * log(0.5 * ch) + G);
//	      }
//	  }
//      }
//    //Call to algorithm AS32 and calculation of seven term Taylor serie
//    Real Q  = ch;
//    do
//      {
//	Q  = ch;
//	Real P1 = 0.5 * ch;
//	Real P2 = P - gamma_in(P1, XX);
//	Real T  = P2 * std::exp(XX * AA + G + P1 - C * log(ch)); 
//	Real B  = T/ch;
//	Real A  = 0.5 * T - B * C;
//	Real S1 = (210+A*(140+A*(105+A*(84+A*(70+60*A)))))/420;
//	Real S2 = (420+A*(735+A*(966+A*(1141+1278*A))))/2520;
//	Real S3 = (210+A*(462+A*(707+932*A)))/2520;
//	Real S4 = (252+A*(672+1182*A)+C*(294+A*(889+1740*A)))/5040;
//	Real S5 = (84+264*A+C*(175+606*A))/2520;
//	Real S6 = (120+C*(346+127*C))/5040;
//	ch = ch+T*
//	  (1+0.5*T*S1-B*C*(S1-B*(S2-B*(S3-B*(S4-B*(S5-B*S6))))));
//      }
//    while (abs(Q/ch -1) > E);  
//    return(ch);
//  }
//
//
//
//  //-------------------------------------------------------------------------
//  //
//  // Computes incomplete gamma ratio for positive values of arguments P 
//  // and alpha. Uses series expansion if alpha > X or X < 1.0, otherwise a 
//  // continued fraction expansion 
//  //
//  // INPUT: X and alpha  
//  //	  X:     is the percentage point for the function
//  //	  alpha: the alpha value of gammafunction  
//  //	  X and alpha must be non-negative real value, alpha must be nonzero          
//  //
//  //  Reference: Bhattacharjee, 1970, Algorithm As 32, J.R.Stat.Soc.C. 19(3)
//  //
//  //  Output: the value of the incomplete gamma function for X and alpha.
//  //
//  //-------------------------------------------------------------------------
//  Real
//  gamma_in(const Real& X, const Real& alpha)
//  {
//    if (X <= 0 || alpha <= 0)
//      {
//	if(X == 0)
//	  {
//	    return 0;
//	  }     
//	else
//	  { 
//	    throw AnError("X and alpha must be non-negative real value");
//	  }
//      }
//  
//    Real acu     = 1.0e-8;
//    Real oflo    = 1.0e30;
//    Real g_in    = 0.0;
//    Real G       = lgamma(alpha);
//    Real factor  = std::exp(alpha * log(X) - X - G);
//
//    Real xbig = 1.0E6;
//    Real alimit = 1000.0;
//
//    // Use normal approximation if alpha > alimit
//    if(alpha > alimit)
//      {
//	Real pn1 = 3 * std::sqrt(alpha) * (std::pow(X / alpha, 1.0/3.0) + 
//				      1.0 / (9.0*alpha) - 1.0);
//	return alnorm(pn1, false);
//      }
//    if(X > xbig)
//      {
//	return 1.0;
//      }
//
//    // Series expansion for x<alpha+1
//    if (X > 1.0 && X >= alpha)
//      {
//	// Continued fraction for x>= alpha+1
//	Real a    = 1.0 - alpha;
//	Real b    = a + X + 1.0;
//	Real term = 0.0;
//	Real pn[6];
//	pn[0]     = 1.0;
//	pn[1]     = X;
//	pn[2]     = X + 1.0;
//	pn[3]     = X * b;
//	g_in      = pn[2] / pn[3];
//	Real dif = 1.0;
//	Real rn = 0.0;
//	do
//	  {
//	    a       += 1.0;
//	    b       += 2.0;
//	    term    += 1.0;
//	    Real an = a * term;
//	    for(unsigned i = 0; i < 2; i++)
//	      {	      
//		pn[i+4] = b * pn[i+2] - an * pn[i];
//	      }
//	    if(pn[5] != 0.0)
//	      {
//		rn = pn[4] / pn[5];
//		dif = abs(g_in - rn);
//		if(dif <= acu)
//		  {
//		    if(dif <= acu* rn)
//		      {
//			return 1.0 - factor * g_in;
//		      }
//		  }
//		g_in =rn;
//	      }
//	    for(unsigned i = 0; i < 4; i++)
//	      {
//		pn[i] = pn[i+2];
//	      }
//	    if(abs(pn[4]) >= oflo)
//	      {
//		for(unsigned i = 0; i < 4; i++)
//		  {
//		    pn[i] = pn[i] / oflo;
//		  }
//	      }
//	  }
//	while(true); 
//      }
//    else
//      {
//	g_in = 1.0;
//	Real term = 1.0;
//	Real rn = alpha;
//	do
//	  {
//	    rn   += 1.0;
//	    term *= X / rn;
//	    g_in += term;
//	  }
//	while(term > acu);
//      
//	return g_in * factor / alpha;
//      }
//  }
//
//
//
//
//  //-------------------------------------------------------
//  //
//  // ACM algorithm 291
//  // Logarithm of the Gamma function
//  // Pike & Hill
//  //
//  // This procedure evaluates the natural logarithm of
//  // gamma(x) for all x > 0, accurate to the 10 decimal 
//  // places. Stirling's formula is used for the central 
//  // polynomial part of the procedure
//  //
//  //-------------------------------------------------------
//  Real
//  loggamma_fn(Real x)
//  {
//    Real f = 0.0;
//    Real z;
//    if(x < 7.0)
//      {
//	f = 1.0;
//	for(z = x; z < 7.0; z += 1.0)
//	  {
//	    x = z;
//	    f *= z;
//	  }
//	x += 1.0;
//	f = - log(f);
//      }
//    z = 1.0 / (x * x);
//    return f + (x -0.5) * log(x) - x + 0.918938533204673 +
//      (((-0.000595238095238 * z + 0.000793650793651) * z - 
//	0.002777777777778) * z + 0.083333333333333) / x;
//  }
//
//  //------------------------------------------------------------------
//  //
//  // Gauinv finds percentage points of the normal distribution,
//  // i.e. it is the inverse of the cdf function.
//  //
//  // Algorithm As70
//  // Odeh and Evans, 1974 Applied Statistics 23(1):96-97
//  //
//  // Input: Real P value of lower tail area p
//  // Gauinv finds percentage points of the normal distribution
//  // Kollad!
//  //-----------------------------------------------------------
//  Real
//  gauinv(const Real& P)
//  {
//    Real alimit = 1.0e-20;
//
//    Real p0 = -0.322232431088;
//    Real p1 = -1.0;
//    Real p2 = -0.342242088547;
//    Real p3 = -0.204231210245e-1;
//    Real p4 = -0.453642210148e-4;
//
//    Real q0 = 0.993484626060e-1;
//    Real q1 = 0.588581570495;
//    Real q2 = 0.531103462366;
//    Real q3 = 0.103537752850;
//    Real q4 = 0.38560700634e-2;
//
//    Real ps = P;
//    if(ps > 0.5)
//      {
//	ps = 1.0 -ps;
//      }
//    if(ps < alimit) 
//      {
//	throw AnError("gauinv: P is not in the interval [10e-20, 1-10e-20]");
//      }
//    if(ps == 0.5)
//      {
//	return 0.0;
//      }
//    Real yi =sqrt(log(1.0 / (ps *ps)));
//    Real gauinv = yi + 
//      ((((yi * p4 + p3) * yi + p2) * yi + p1) * yi + p0) / 
//      ((((yi * q4 + q3) * yi + q2) * yi + q1) * yi + q0);
//    if(P < 0.5)
//      {
//	return -gauinv;
//      }
//    else
//      {
//	return gauinv;
//      }
//  }
//
//
//  // Algorithm AS 66 - The normal integral
//  //
//  // by I.D. HIll, 1973, Applied Statistics 22(3):424-427
//  // Calculates the upper or lower tail area of the standard normal 
//  // distribution curve corresponding to any given argument.
//  //
//  // Parameters: Real x - the argument value of the normal distr function
//  //             bool upper - true = calculate area from x to infinity
//  //                          false = calclat area from 0 to x
//  //
//  // Data constants: LTONE should be set to the value at which the lower
//  //                 tail area becomes 1.0 to the accuracy of the machine.
//  //                 LTONE=(n+9)/3 gives the rquired value accurately 
//  //                 enough for a machine that produces n nedical digits 
//  //                 in its real number.
//  //                 UTZERO should be set to the value at which the upper
//  //                 tail area becomes 0.0 to the accuracy of the machine.
//  //                 This may be taken as the value such that
//  //                 exp(-0.5*UTZERO�)/(UTZERO*scrt(2*pi)) is just greater
//  //                 than the smallest allowable real number.
//  //--------------------------------------------------------------------
//  Real
//  alnorm(Real x, bool upper)
//  {
//    // This is waht is recommended, but...
//        Real LTONE = (Real_limits::digits10 +9)/3;
//        Real UTZERO = 37.4949; // Suits Apple PowerBook G4 w MacOSX
//    
//    // ...this seems to work:-o
////     Real LTONE = Real_limits::max();
////     Real UTZERO = Real_limits::max();
//
//    Real alnorm = 0;
//
//    if(x < 0)
//      {
//	upper = !upper;
//	x = -x;
//      }
//    if(x <= LTONE || (upper && x <= UTZERO))
//      {
//	Real y = 0.5 * x * x;
//	if(x <= 1.28)
//	  {
//	    alnorm = 0.5 - x * (0.398942280444 - 0.399903438504 * y /
//				(y + 5.75885480458 - 29.8213557808 /
//				 (y + 2.62433121679 + 48.6959930692 /
//				  (y + 5.92885724438))));
//	  }
//	else
//	  {
//	    alnorm = 0.398942280385 * std::exp(-y) /
//	      (x - 3.8052E-8 + 1.00000615302 /
//	       (x + 3.98064794E-4 +1.98615381364 /
//		(x - 0.151679116635 + 5.29330324926 /
//		 (x + 4.8385912808 -15.1508972451 /
//		  (x + 0.742380924027 + 30.789933034 /
//		   (x + 3.99019417011))))));
//	  }
//      }
//    if(upper == false)
//      {
//	alnorm = 1.0 - alnorm;
//      }
//    return alnorm;
//  }
}
