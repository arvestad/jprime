package se.cbb.jprime.math;

import java.util.Map;

import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.ChangeInfo;

/**
 * Represents a 1-D log-normal distribution, log N(mu,sigma^2).
 * <p/>
 * It is possible to let the distribution rely on two floating point state parameters and
 * act as a <code>Dependent</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class LogNormalDistribution implements Continuous1DPDDependent {
	
	/** Mean parameter of the log-normal (no the normal!). Null if not used. */
	protected DoubleParameter meanParam;
	
	/** CV parameter of the log-normal (not the normal!). Null if not used. */
	protected DoubleParameter cvParam;
	
	/** mu value. Should reflect parameters' values. */
	protected double mu;
	
	/** sigma value. Should reflect parameters' values. */
	protected double sigma;
	
	/** Constant term, given the variance, in the log density function: -0.5 * ln(2 * PI * sigma^2). */
	protected double logDensFact;
	
	/**
	 * Constructor for log N(mu,sigma^2) when the distribution does not rely on state parameters.
	 * @param mu distribution mu (i.e., mean of underlying normal distribution).
	 * @param sigma2 distribution sigma2 (i.e., variance of underlying normal distribution).
	 */
	public LogNormalDistribution(double mu, double sigma2) {
		if (sigma2 <= 0.0) {
			throw new IllegalArgumentException("Cannot have non-positive sigma^2 for log-normal distribution.");
		}
		this.meanParam = null;
		this.cvParam = null;
		this.mu = mu;
		this.sigma = Math.sqrt(sigma2);
		this.update();
	}
	
	/**
	 * Constructor for when the distribution relies on state parameters.
	 * The distribution will add itself as a child dependent of the parameters.
	 * For MCMC mixing purposes, mean and CV has been selected as parameterisation.
	 * @param mean the mean parameter. NOTE: This is the actual mean of the distribution, i.e. exp(mu+sigma^2/2).
	 * @param cv the CV parameter. NOTE: this is the actual CV of the distribution.
	 */
	public LogNormalDistribution(DoubleParameter mean, DoubleParameter cv) {
		this.meanParam = mean;
		this.cvParam = cv;
		this.update();
	}
	
	@Override
	public String getName() {
		return "Log-normal distribution";
	}

	@Override
	public int getNoOfParameters() {
		return 2;
	}

	@Override
	public int getNoOfDimensions() {
		return 1;
	}

	@Override
	public double getPDF(double x) {
		double lnx = Math.log(x);
		return Math.exp(-0.5 * Math.pow((lnx - this.mu) / this.sigma, 2) + this.logDensFact - lnx);
	}

	@Override
	public double getCDF(double x) {
		x = (Math.log(x) - this.mu) / this.sigma;
		return Normal.cdf(x);
	}

	@Override
	public double getProbability(double a, double b) {
		return (this.getCDF(b) - this.getCDF(a));
	}

	@Override
	public double getMean() {
		return Math.exp(this.mu + Math.pow(this.sigma, 2) / 2);
	}

	@Override
	public double getMedian() {
		return Math.exp(this.mu);
	}

	@Override
	public double getStandardDeviation() {
		return Math.sqrt(this.getVariance());
	}

	@Override
	public double getVariance() {
		double sigma2 = Math.pow(this.sigma, 2);
		return (Math.exp(sigma2) - 1) * Math.exp(2 * this.mu + sigma2);
	}

	@Override
	public double getCV() {
		return Math.sqrt(Math.exp(this.sigma * this.sigma) - 1);
	}

	@Override
	public RealInterval getDomainInterval() {
		return new RealInterval(0, Double.POSITIVE_INFINITY, true, true);
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		if (changeInfos.get(this.meanParam) != null || changeInfos.get(this.cvParam) != null) {
			String s = this.toString();
			this.update();
			changeInfos.put(this, new ChangeInfo(this, s + " was perturbed into " + this.toString()));
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
		this.update();
	}
	
	@Override
	public Dependent[] getParentDependents() {
		if (this.meanParam != null) {
			return new Dependent[] { meanParam, cvParam };
		}
		return null;
	}

	/**
	 * Updates the distribution.
	 */
	public void update() {
		if (this.meanParam != null) {
			double m = this.meanParam.getValue();
			double cv = this.cvParam.getValue();
			this.sigma = Math.sqrt(Math.log(cv * cv + 1));
			this.mu = Math.log(m) - this.sigma * this.sigma / 2;
		}
		this.logDensFact = -0.5 * Math.log(2 * Math.PI * this.sigma * this.sigma);
	}
	
	@Override
	public String toString() {
		return "log N(" + this.mu + ", " + (this.sigma * this.sigma) + ')';
	}

	@Override
	public void setMean(double mean) {
		if (this.meanParam != null) {
			this.meanParam.setValue(mean);
		}
		// We only update mu.
		this.mu = Math.log(mean) - this.sigma * this.sigma / 2;
	}
	
	@Override
	public void setStandardDeviation(double stdev) {
		// TODO: Preferably change only sigma, and not mu.
		throw new UnsupportedOperationException("Changing standard deviation on log-normal distribution currently not supported.");
	}

	@Override
	public void setVariance(double var) {
		// TODO: Preferably change only sigma, and not mu.
		throw new UnsupportedOperationException("Changing standard deviation on log-normal distribution currently not supported.");
	}

	@Override
	public double getQuantile(double p) {
		return Math.exp(Normal.quantile(p) * this.sigma + this.mu);
	}

	@Override
	public double sampleValue(PRNG prng) {
		double x = prng.nextGaussian();
		// No bounds checking for within representable range...
		return (this.getQuantile(x));
	}

	@Override
	public double getMode() {
		return Math.exp(this.mu - this.sigma * this.sigma);
	}
	
	/**
	 * Returns the mean mu of the underlying normal distribution.
	 * @return mu.
	 */
	public double getUnderlyingMean() {
		return this.mu;
	}
	
	/**
	 * Returns the variance sigma^2 of the underlying normal distribution.
	 * @return sigma2.
	 */
	public double getUnderlyingVariance() {
		return (this.sigma * this.sigma);
	}

}
