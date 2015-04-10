package se.cbb.jprime.math;

/**
 * Not a gamma distribution per se; instead contains
 * convenience methods concerning gamma functions.
 * Returning logarithms is often necessary to avoid overflow.
 * See also <code>GammaDistribution</code>.
 * 
 * @author Joel Sjöstrand.
 * @author Bengt Sennblad.
 * @author Martin Linder.
 */
public class Gamma {
	
	/** 14 Lanczos coefficients for estimating ln(Gamma(xx)). */
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
	
	/**
	 * Computes the natural logarithm of the complete gamma function,
	 * i.e., ln(Gamma(xx)) for xx > 0, where
	 * Gamma(x) = integral(t^(x-1) e^(-t), t=0,...,inf).
	 * Uses Lanczos' approximation formula, see Numerical Recipes 6.1.
	 * No bounds checking; see also <code>lnGammaSafe(xx)</code>.
	 * <p/>
	 * This method is also known as <code>loggamma_fn(xx)</code>.
	 * @param xx input parameter > 0.
	 * @return ln(Gamma(xx)) where Gamma(.) is the complete gamma function.
	 */
	public static double lnGamma(double xx) {
		final double RATIONAL_671_128 = 5.24218750000000000;
		final double SER = 0.999999999999997092;
		final double SQRT_2_PI = Math.sqrt(2 * Math.PI);
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
	
	/**
	 * Quantile function for the gamma distribution, i.e.
	 * the inverse of the CDF function F. Note that notation of scale parameter theta is such
	 * that mean=k*theta. Sometimes rate parameter beta=1/theta is used instead.
	 * <p/>
	 * This method is also known as <code>ppGamma(p, alpha=k, beta=1/theta)</code>.
	 * @param p probability.
	 * @param k shape parameter.
	 * @param theta scale parameter.
	 * @return x so that F(x)=P(X<=x)=p.
	 */
	public static double gammaQuantile(double p, double k, double theta) {
		if (k <= 0.0 || theta <= 0.0) {
			throw new IllegalArgumentException("Invalid parameters for computing gamma quantile.");
		}
		return (ChiSquared.quantile(p, 2 * k) * theta / 2);
	}
  
	/**
	 * Computes the cumulative density function F(x) for a gamma distribution.
	 * Note that notation of scale parameter theta is such
	 * that mean=k*theta. Sometimes rate parameter beta=1/theta is used instead.
	 * @param x upper bound for region.
	 * @param k shape parameter.
	 * @param theta scale parameter
	 * @return F(x)=P(X<=x).
	 */
	public static double gammaCDF(double x, double k, double theta) {
		if (k <= 0.0 || theta <= 0.0) {
			throw new IllegalArgumentException("Invalid parameters when computing gamma distribution CDF.");
		}
		if (x < 0) {
			return 0.0;
		}
		double invG = incGammaRatio(x / theta, k);
		if (invG < 0.0) {
			return 0.0;
		} else if (invG > 1.0) {
			return 1.0;
		}
		return invG;
	}

	/**
	 * Computes the incomplete gamma ratio function for positive values of arguments x 
	 * and alpha. Uses series expansion if alpha > x or x < 1.0, otherwise a 
	 * continued fraction expansion.
	 * <p/>
	 * Reference: Bhattacharjee, 1970, Algorithm AS 32, J.R.Stat.Soc.C. 19(3).
	 * <p/>
	 * This method is also known as <code>gamma_in(x,alpha=k)</code>.
	 * @param x the value up to which to integrate.
	 * @param k the gamma shape parameter.
	 * @return the incomplete gamma ratio function.
	 */
	public static double incGammaRatio(double x, double k) {
		if (x < 0.0 || k <= 0.0) {
			throw new IllegalArgumentException("Invalid parameters for incomplete gamma ratio.");
		}
		if (x == 0.0) {
			return 0.0;
		}

		final double accuracy = 1.0e-8;
		final double overflow = 1.0e30;
		double g_in = 0.0;
		double g = lnGamma(k);
		double factor = Math.exp(k * Math.log(x) - x - g);
		final double xbig = 1.0E6;
		final double alimit = 1000.0;

		// Use normal approximation if k > alimit.
		if (k > alimit) {
			double pn1 = 3 * Math.sqrt(k) * (Math.pow(x / k, 1.0/3.0) + 1.0 / (9.0 * k) - 1.0);
			return Normal.cdf(pn1);
		}
		if (x > xbig) {
			return 1.0;
		}

		// Series expansion for x < k + 1.
		if (x > 1.0 && x >= k) {
			// Continued fraction for x >= k + 1.
			double a = 1.0 - k;
			double b = a + x + 1.0;
			double term = 0.0;
			double[] pn = new double[6];
			pn[0] = 1.0;
			pn[1] = x;
			pn[2] = x + 1.0;
			pn[3] = x * b;
			g_in = pn[2] / pn[3];
			double dif = 1.0;
			double rn = 0.0;
			do {
				a += 1.0;
				b += 2.0;
				term += 1.0;
				double an = a * term;
				for (int i = 0; i < 2; i++) {	      
					pn[i+4] = b * pn[i+2] - an * pn[i];
				}
				if (pn[5] != 0.0) {
					rn = pn[4] / pn[5];
					dif = Math.abs(g_in - rn);
					if (dif <= accuracy) {
						if (dif <= accuracy * rn) {
							return 1.0 - factor * g_in;
						}
					}
					g_in = rn;
				}
				for (int i = 0; i < 4; i++)  {
					pn[i] = pn[i+2];
				}
				if (Math.abs(pn[4]) >= overflow) {
					for (int i = 0; i < 4; i++) {
						pn[i] = pn[i] / overflow;
					}
				}
			} while (true); 
		} else {
			g_in = 1.0;
			double term = 1.0;
			double rn = k;
			do {
				rn   += 1.0;
				term *= x / rn;
				g_in += term;
			} while (term > accuracy);
			return g_in * factor / k;
		}
	}

	/**
	 * Discretises a continuous gamma distribution into n "categories",
	 * each holding an equal part of the probability mass, 1/n,
	 * then returns the mean value r_i for each category i.
	 * Note: The mean is returned, not the mean density!
     * @param n number of discrete categories that the gamma distribution will be represented by.
     * @param k gamma distribution shape parameter.
     * @param theta gamma distribution scale parameter.
     * @return the mean for each category.
     */
	public static double[] getDiscreteGammaCategories(int n, double k, double theta) {
		// Method: Described in Yang (1994).
	    //         First we calculate the n-1 internal quantile points for the chi2
	    //         distribution using the function ChiSquared.quantile(). Then, utilising
	    //         the fact that Chi2(2*k) equals Gamma(k,2) and that
	    //         X~Gamma(k,theta) <=> cX~Gamma(k,c*theta), we can
	    //         calculate the corresponding quantile point of the gamma distribution.
	    //         We can then obtain the mean value of each category through the
	    //         following formula:
	    //         r_i = k*theta*[I(b/theta,k+1) - I(a/theta,k+1)]*n,
	    //         where a and b are the quantile points that cut category
	    //         i, and I(z,k) is the incomplete gamma ratio which is
	    //         calculated with the function Gamma.incGammaRatio().
		if (n == 1) {
			return new double[] { 1.0 };
		} else {
			double[] means = new double[n];
			double albet = k * theta;
			double v = k * 2;
			double alP = k + 1;
			double b2 = 2.0 / theta;

			double ig1 = 0.0;
			for (int i = 0; i < n - 1; i++) {
				double p = (i + 1) / (double) n;
				double chi2 = ChiSquared.quantile(p, v) / b2; 
				double ig2 = Gamma.incGammaRatio(chi2 / theta, alP);
				means[i] = albet * (ig2 - ig1) * n;
				ig1 = ig2;
			}
			means[n-1] = albet * (1 - ig1) * n;
			return means;
		}
	}
	
}
