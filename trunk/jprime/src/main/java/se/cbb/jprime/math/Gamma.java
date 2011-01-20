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
	 * Uses Lanczos' approximation formula.
	 * See Numerical Recipes 6.1. No bounds checking, see <code>lnGammaSafe(xx)</code>.
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

}
