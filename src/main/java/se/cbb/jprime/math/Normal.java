package se.cbb.jprime.math;

/**
 * Not a normal (Gaussian) distribution per se; contains related functions however.
 * 
 * @author Joel Sjöstrand.
 */
public class Normal {

	/**
	 * Cumulative density function F(x)=P(X<=x) for a N(0,1) distribution.
	 * <p/>
	 * This method is also known as <code>alnorm(x, false)</code>.
	 * @param x the value.
	 * @return the cumulative density F(x).
	 */
	public static double cdf(double x) {
		if (x < -39) { return 0.0; }
		if (x > 9)   { return 1.0; }
		
		final double b1 =  0.319381530;
		final double b2 = -0.356563782;
		final double b3 =  1.781477937;
		final double b4 = -1.821255978;
		final double b5 =  1.330274429;
		final double p  =  0.2316419;
		final double c  =  0.39894228;
		
		if (x >= 0.0) {
			double t = 1.0 / (1.0 + p * x);
			return (1.0 - c * Math.exp(-x * x / 2.0) * t * (t *(t * (t * (t * b5 + b4) + b3) + b2) + b1));
		} else {
			double t = 1.0 / (1.0 - p * x);
			return (c * Math.exp(-x * x / 2.0) * t * (t *(t * (t * (t * b5 + b4) + b3) + b2) + b1));
		}
	}
	
	/**
	 * Quantile function for a N(0,1) distribution, i.e. the inverse, F^-1(p), of the 
	 * CDF, F.
	 * <p/>
	 * Algorithm from Paul M. Voutier, 2010.
	 * <p/>
	 * This method is also known as <code>gauinv()</code>.
	 * @param p the probability in range [0,1].
	 * @return x so that F(x)=P(X<=x)=p.
	 */
	public static double quantile(double p) {
		if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException("Cannot compute quantile for probability not in [0,1].");
		}
		
		if (0.025 <= p && p <= 0.975) {
			final double a0 =  0.151015505647689;
			final double a1 = -0.5303572634357367;
			final double a2 =  1.365020122861334;
			final double b0 =  0.132089632343748;
			final double b1 = -0.7607324991323768;
			
			double q = p - 0.5;
			double r = Math.pow(q, 2);
			return (q * (a2 + (a1 * r + a0) / (r * r + b1 * r + b0)));
		}
		
		if (1e-50 < p && p < 1.0 - 1e-16) {
			//final double c0  = 16.896201479841517652;
			//final double c1  = -2.793522347562718412;
			//final double c2  = -8.731478129786263127;
			final double c3  = -1.000182518730158122;
			final double cp0 = 16.682320830719986527;
			final double cp1 =  4.120411523939115059;
			final double cp2 =  0.029814187308200211;
			final double d0  =  7.173787663925508066;
			final double d1  =  8.759693508958633869;
			
			if (p < 0.5) {
				double r = Math.sqrt(Math.log(1.0 / Math.pow(p, 2)));
				return (c3 * r + cp2 + (cp1 * r + cp0) / (r * r + d1 * r + d0));
			}
			double r = Math.sqrt(Math.log(1.0 / Math.pow(1.0 - p, 2)));
			return -(c3 * r + cp2 + (cp1 * r + cp0) / (r * r + d1 * r + d0));
		}
		
		// Too small p-value.
		return (p < 0.5 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
	}
}
