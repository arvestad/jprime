package se.cbb.jprime.math;


/**
 * Interface for 1-D continuous probability distributions, i.e. acting on real numbers. 
 * Examples include the gamma and exponential distributions.
 * <p/>
 * "Continuous" is here used in the sense distinguishing it from discrete distributions.
 * It is thus perfectly allowed to have an implementation which is piecewise continuous, e.g.
 * resembling a histogram. We do, however, require that all implementations are defined
 * for a single domain interval [a,b], (a,b], [a,b) or (a,b), where the bounds a&lt;b may take on
 * real numbers as well as negative and positive infinity.
 * 
 * @author Joel Sjöstrand.
 */
public interface Continuous1DPD extends ProbabilityDistribution {
	
	/**
	 * Returns the probability density function f(x) for
	 * a specified value x.
	 * @param x the value where to evaluate density.
	 * @return the probability density, f(x).
	 */
	public double getPDF(double x);

	/**
	 * Returns cumulative density function F(x)=P(X<=x) for a
	 * specified value x.
	 * @param x the value where to evaluate the cumulative density.
	 * @return the cumulative density F(x).
	 */
	public double getCDF(double x);
	
	/**
	 * Returns the quantile function, i.e., the inverse of the CDF, F^{-1}(p), 0<=p<=1.
	 * @param p the probability.
	 * @return x so that F(x)=P(X<=x)=p.
	 */
	public double getQuantile(double p);
	
	/**
	 * For specified values a<=b, returns the probability F(b)-F(a)=P(a<=X<=b).
	 * @param a the smaller boundary.
	 * @param b the larger boundary.
	 * @return P(a<=X<=b).
	 */
	public double getProbability(double a, double b);
	
	/**
	 * Returns the mean, i.e. the expected value mu=E(X).
	 * @return the mean.
	 */
	public double getMean();
	
	/**
	 * Sets the mean.
	 * @param mean the new mean.
	 */
	public void setMean(double mean);
		
	/**
	 * Returns the median, i.e. the value m so that P(X<=m)=P(X>m)=1/2.
	 * Equivalent to <code>getQuantile(0.5)</code>.
	 * @return the median.
	 */
	public double getMedian();
	
	/**
	 * Returns the mode. If there are multiple modes, may e.g. return
	 * one of them.
	 * @return the mode.
	 */
	public double getMode();
	
	/**
	 * Returns the standard deviation sigma=sqrt(E((X-E(X))^2)).
	 * @return the standard deviation.
	 */
	public double getStandardDeviation();
	
	/**
	 * Sets the standard deviation.
	 * @param stdev the new standard deviation.
	 */
	public void setStandardDeviation(double stdev);
	
	/**
	 * Returns the variance sigma^2=E((X-E(X))^2).
	 * @return the variance.
	 */
	public double getVariance();
	
	/**
	 * Sets the variance.
	 * @param var the new variance.
	 */
	public void setVariance(double var);
	
	/**
	 * Returns the coefficient of variation, i.e. c_v=sigma/|mu|, where sigma
	 * is the standard deviation and mu the mean.
	 * @return the standard deviation.
	 */
	public double getCV();
	
	/**
	 * Returns the domain interval, i.e. what is sometimes referred to as the <i>support</i>.
	 * @return the domain interval.
	 */
	public RealInterval getDomainInterval();
	
	/**
	 * Samples a value from the distribution.
	 * @param prng the pseudo-random number generator.
	 * @return the sample.
	 */
	public double sampleValue(PRNG prng);
}
