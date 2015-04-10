package se.cbb.jprime.math;

import java.util.Map;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;

/**
 * Represents a 1-D continuous exponential distribution Exp(lambda).
 * It is possible to let the distribution rely on a floating point state parameter and
 * act as a <code>Dependent</code>.
 *
 * @author Joel Sjöstrand.
 */
public class ExponentialDistribution implements Continuous1DPDDependent {

	/** ln(2). */
	protected static final double LN2 = Math.log(2);
	
	/** Rate = 1/mean. */
	protected double lambda;
	
	/** State parameter, representing the mean = 1/lambda. Null if not used. */
	protected DoubleParameter p1;
	
	/**
	 * Constructor for when the distribution does not rely on a state parameter.
	 * Beware of the alternative parametrisation using the mean instead of rate.
	 * @param lambda the rate, i.e, 1/mean.
	 */
	public ExponentialDistribution(double lambda) {
		if (lambda <= 0) {
			throw new IllegalArgumentException("Invalid rate for exponential distribution.");
		}
		this.p1 = null;
		this.lambda = lambda;
	}
	
	/**
	 * Constructor for when the distribution relies on a state parameter.
	 * The distribution will add itself as a child dependent of the parameter.
	 * @param mean the mean of the distribution , i.e. 1/lambda.
	 */
	public ExponentialDistribution(DoubleParameter mean) {
		if (mean.getValue() <= 0) {
			throw new IllegalArgumentException("Invalid mean for exponential distribution.");
		}
		this.p1 = mean;
		this.lambda = 1.0 / mean.getValue();
	}
	
	@Override
	public String getName() {
		return "Exponential distribution";
	}

	@Override
	public int getNoOfParameters() {
		return 1;
	}

	@Override
	public int getNoOfDimensions() {
		return 1;
	}

	@Override
	public double getPDF(double x) {
		return (this.lambda * Math.exp(-this.lambda * x));
	}

	@Override
	public double getCDF(double x) {
		return (1.0 - Math.exp(-this.lambda * x));
	}

	@Override
	public double getQuantile(double p) {
		return (-Math.log(1.0 - p) / this.lambda);
	}

	@Override
	public double getProbability(double a, double b) {
		return (this.getCDF(b) - this.getCDF(a));
	}

	@Override
	public double getMean() {
		return (1.0 / this.lambda);
	}

	@Override
	public void setMean(double mean) {
		this.lambda = 1.0 / mean;
		if (this.p1 != null) {
			p1.setValue(mean);
		}
	}

	@Override
	public double getMedian() {
		return ((1.0 / this.lambda) * LN2);
	}

	@Override
	public double getStandardDeviation() {
		return (1.0 / this.lambda);
	}

	@Override
	public double getMode() {
		return 0.0;
	}  
	
	@Override
	public void setStandardDeviation(double stdev) {
		this.lambda = 1.0 / stdev;
		if (this.p1 != null) {
			this.p1.setValue(stdev);
		}
	}

	@Override
	public double getVariance() {
		return (1.0 / Math.pow(this.lambda, 2));
	}

	@Override
	public void setVariance(double var) {
		double stdev = Math.sqrt(var);
		this.lambda = 1.0 / stdev;
		if (this.p1 != null) {
			this.p1.setValue(stdev);
		}
	}
	
	@Override
	public double getCV() {
		return 1.0;
	}

	@Override
	public RealInterval getDomainInterval() {
		return new RealInterval(0, Double.POSITIVE_INFINITY, false, true);
	}

	/**
	 * Returns a random number drawn from the distribution.
	 * May contain inconsistencies around boundary values since it relies on <code>RealInterval.getRandom()</code>.
	 * @param prng the pseudo-random number generator.
	 * @return a random value.
	 */
	@Override
	public double sampleValue(PRNG prng) {
		double x = prng.nextDouble();
		// No bounds checking for within representable range...
		return (this.getQuantile(x));
	}

	@Override
	public String toString() {
		return ("Exp(" + this.lambda + ')');
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		if (changeInfos.get(this.p1) != null) {
			String old = this.toString();
			this.lambda = 1.0 / this.p1.getValue();
			changeInfos.put(this, new ChangeInfo(this, old + " was perturbed into " + this.toString()));
		} else {
			changeInfos.put(this, null);
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		// Caching not worthwhile.
	}

	@Override
	public void restoreCache(boolean willSample) {
		// Caching not worthwhile.
		this.lambda = 1.0 / this.p1.getValue();
	}

	@Override
	public Dependent[] getParentDependents() {
		if (this.p1 != null) {
			return new Dependent[] { p1 };
		}
		return null;
	}

	/**
	 * Returns the rate parameter.
	 * @return the rate lambda.
	 */
	public double getRate() {
		return this.lambda;
	}
	
}
