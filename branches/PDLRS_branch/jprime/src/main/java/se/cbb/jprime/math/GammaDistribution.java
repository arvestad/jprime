package se.cbb.jprime.math;

import java.util.Map;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;

/**
 * Represents a 1-D gamma distribution Gamma(k,theta).
 * It is possible to let the distribution rely on two floating point state parameters and
 * act as a <code>Dependent</code>.
 * <p/>
 * Beware of the alternative parametrisation that is sometimes used. We use
 * <i>shape</i> parameter k and <i>scale</i> parameter theta. Sometimes, one
 * encounters Gamma(alpha,beta), with shape parameter alpha=k and <i>rate</i> parameter
 * beta=1/theta.
 *
 * @author Joel Sjöstrand.
 */
public class GammaDistribution implements Continuous1DPDDependent {
	
	/** Mean state parameter. Null if not used. */
	protected DoubleParameter mean;
	
	/** CV state parameter. Null if not used. */
	protected DoubleParameter cv;
	
	/** Shape parameter. Should reflect DoubleParameters' values. */
	protected double k;
	
	/** Scale parameter. Should reflect DoubleParameters' values. */
	protected double theta;
	
	/** For speed, holds -ln(G(k)*theta^k), where G() is the gamma function. */
	protected double c;
	
	/** Shape cache. */
	private double kCache = Double.NaN;

	/** Scale cache. */
	private double thetaCache = Double.NaN;

	/** C cache. */
	private double cCache = Double.NaN;
	
	/**
	 * Constructor for when the distribution does not rely on state parameters.
	 * Note the parametrisation where <i>scale</i> parameter theta=1/beta, for <i>rate</i> parameter beta.
	 * @param k distribution shape parameter.
	 * @param theta distribution scale parameter.
	 */
	public GammaDistribution(double k, double theta) {
		if (k <= 0.0 || theta <= 0.0) {
			throw new IllegalArgumentException("Cannot have non-positive shape or scale for gamma distribution.");
		}
		this.mean = null;
		this.cv = null;
		this.k = k;
		this.theta = theta;
		this.update();
	}
	
	/**
	 * Constructor for when the distribution relies on state parameters.
	 * The distribution will add itself as a child dependent of the parameters.
	 * For MCMC mixing purposes, mean and CV has been selected as parameterisation.
	 * @param mean the mean.
	 * @param cv the CV.
	 */
	public GammaDistribution(DoubleParameter mean, DoubleParameter cv) {
		this.mean = mean;
		this.cv = cv;
		this.update();
	}
	
	@Override
	public String getName() {
		return "Gamma distribution";
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
	public Dependent[] getParentDependents() {
		if (this.mean != null) {
			return new Dependent[] { mean, cv };
		}
		return null;
	}

	/**
	 * Updates the distribution following, e.g., a change of the underlying DoubleParameters.
	 */
	private void update() {
		if (this.mean != null) {
			double cv2 = Math.pow(this.cv.getValue(), 2);
			this.k = 1.0 / cv2;
			this.theta = this.mean.getValue() * cv2;
		}
		this.c = -this.k * Math.log(this.theta) - Gamma.lnGamma(this.k);
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		if (changeInfos.get(this.mean) != null || changeInfos.get(this.cv) != null) {
			String s = this.toString();
			this.kCache = this.k;
			this.thetaCache = this.theta;
			this.cCache = this.c;
			this.update();
			changeInfos.put(this, new ChangeInfo(this, s + " was perturbed into " + this.toString()));
		} else {
			changeInfos.put(this, null);
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.kCache = Double.NaN;
		this.thetaCache = Double.NaN;
		this.cCache = Double.NaN;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.k = this.kCache;
		this.theta = this.thetaCache;
		this.c = this.cCache;
		this.kCache = Double.NaN;
		this.thetaCache = Double.NaN;
		this.cCache = Double.NaN;
	}
	
	@Override
	public double getPDF(double x) {
		return Math.exp((this.k - 1.0) * Math.log(x) - x / this.theta + this.c);
	}
	
	/**
	 * For speed reasons, provides access to the probability density function f(x) for
	 * a specified value x, returned as a <code>Probability</code>.
	 * @param x the value where to evaluate density.
	 * @return the probability density, f(x).
	 */
	public LogDouble getPDFAsProbability(double x) {
		return new LogDouble((this.k - 1.0) * Math.log(x) - x / this.theta + this.c, 1);
	}

	@Override
	public double getCDF(double x) {
		return Gamma.gammaCDF(x, this.k, this.theta);
	}

	@Override
	public double getQuantile(double p) {
		return Gamma.gammaQuantile(p, this.k, this.theta);
	}

	@Override
	public double getProbability(double a, double b) {
		return (this.getCDF(b) - this.getCDF(a));
	}

	@Override
	public double getMean() {
		return (this.k * this.theta);
	}

	/**
	 * Sets the mean by changing the shape parameter but not scale parameter.
	 * @param mean the new mean.
	 */
	@Override
	public void setMean(double mean) {
		if (mean <= 0.0) { throw new IllegalArgumentException("Cannot set non-positive mean on gamma distribution."); }
		this.k = mean / this.theta;
		this.c = -this.k * Math.log(this.theta) - Gamma.lnGamma(this.k);
		if (this.mean != null) {
			synchMeanAndCV();
		}
	}

	@Override
	public double getMedian() {
		return this.getQuantile(0.5);
	}

	@Override
	public double getStandardDeviation() {
		return Math.sqrt(this.k) * this.theta;
	}

	/**
	 * Sets the standard deviation by changing the shape parameter but not scale parameter.
	 * @param stdev the new standard deviation.
	 */
	@Override
	public void setStandardDeviation(double stdev) {
		if (stdev <= 0.0) { throw new IllegalArgumentException("Cannot set non-positive variance on gamma distribution."); }
		this.k = Math.pow(stdev / this.theta, 2);
		this.c = -this.k * Math.log(this.theta) - Gamma.lnGamma(this.k);
		if (this.mean != null) {
			synchMeanAndCV();
		}
	}

	@Override
	public double getVariance() {
		return (this.k * this.theta * this.theta);
	}

	/**
	 * Sets the variance by changing the shape parameter but not scale parameter.
	 * @param var the new variance.
	 */
	@Override
	public void setVariance(double var) {
		if (var <= 0.0) { throw new IllegalArgumentException("Cannot set non-positive variance on gamma distribution."); }
		this.k = var / (this.theta * this.theta);
		this.c = -this.k * Math.log(this.theta) - Gamma.lnGamma(this.k);
		if (this.mean != null) {
			synchMeanAndCV();
		}
	}

	/**
	 * Adjusts DoubleParameters' values in accordance with the current internal values
	 * following a change of the shape parameter.
	 */
	private void synchMeanAndCV() {
		this.mean.setValue(this.k * this.theta);
		this.cv.setValue(1.0 / Math.sqrt(this.k));
	}
	
	@Override
	public double getCV() {
		return (1.0 / Math.sqrt(k));
	}

	@Override
	public RealInterval getDomainInterval() {
		return new RealInterval(0, Double.POSITIVE_INFINITY, false, true);
	}

	@Override
	public double sampleValue(PRNG prng) {
		return Gamma.gammaQuantile(prng.nextDouble(), this.k, this.theta);	
	}

	@Override
	public String toString() {
		return "Gamma(" + this.k + ", " + this.theta + ')';
	}

	@Override
	public double getMode() {
		if (this.k >= 1.0) {
			return ((this.k - 1.0) * this.theta);
		}
		throw new UnsupportedOperationException("Cannot compute gamma distribution mode when shape parameter k < 1.");
	}
	
	/**
	 * Returns the shape parameter.
	 * @return the shape parameter k, a.k.a alpha.
	 */
	public double getShape() {
		return this.k;
	}
	
	/**
	 * Returns the scale parameter theta.
	 * @return the scale parameter theta, a.k.a beta^-1.
	 */
	public double getScale() {
		return this.theta;
	}

}
