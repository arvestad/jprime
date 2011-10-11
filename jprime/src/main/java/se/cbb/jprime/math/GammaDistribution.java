package se.cbb.jprime.math;

import java.util.Set;
import java.util.TreeSet;

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

	/**
	 * When the distribution depends on state parameters, defines
	 * what the latter represent.
	 */
	public enum ParameterSetup {
		/** Shape and scale. */                    K_AND_THETA,
		/** Mean and standard deviation. */        MEAN_AND_STDEV,
		/** Mean and coefficient of variation. */  MEAN_AND_CV
	}
	
	/** First state parameter. Null if not used. */
	protected DoubleParameter p1;
	
	/** Second state parameter. Null if not used. */
	protected DoubleParameter p2;
	
	/** State parameter representation. Null if not used. */
	protected ParameterSetup setup;
	
	/** Shape parameter. Should reflect p1 and p2's values. */
	protected double k;
	
	/** Scale parameter. Should reflect p1 and p2's values. */
	protected double theta;
	
	/** For speed, holds -ln(G(k)*theta^k), where G() is the gamma function. */
	protected double c;
	
	/** Child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Shape cache. */
	private double kCache = Double.NaN;

	/** Scale cache. */
	private double thetaCache = Double.NaN;

	/** C cache. */
	private double cCache = Double.NaN;
	
	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
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
		this.p1 = null;
		this.p2 = null;
		this.setup = null;
		this.dependents = null;
		this.k = k;
		this.theta = theta;
		this.update(false);
	}
	
	/**
	 * Constructor for when the distribution relies on state parameters.
	 * The distribution will add itself as a child dependent of the parameters.
	 * @param p1 the first parameter.
	 * @param p2 the second parameter.
	 * @param setup what p1 and p2 represents.
	 */
	public GammaDistribution(DoubleParameter p1, DoubleParameter p2, ParameterSetup setup) {
		this.p1 = p1;
		this.p2 = p2;
		this.setup = setup;
		this.dependents = new TreeSet<Dependent>();
		p1.addChildDependent(this);
		p2.addChildDependent(this);
		this.update(false);
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
	public boolean isDependentSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public Set<Dependent> getChildDependents() {
		return this.dependents;
	}

	@Override
	public void cache(boolean willSample) {
		this.kCache = this.k;
		this.thetaCache = this.theta;
		this.cCache = this.c;
	}

	@Override
	public void update(boolean willSample) {
		// We always do an update.
		if (this.p1 != null) {
			String old = this.toString();
			switch (this.setup) {
			case K_AND_THETA:
				this.k = this.p1.getValue();
				this.theta = this.p2.getValue();
				break;
			case MEAN_AND_STDEV:
				this.k = Math.pow(this.p1.getValue() / this.p2.getValue(), 2);
				this.theta = Math.pow(this.p2.getValue(), 2) / this.p1.getValue();
				break;
			case MEAN_AND_CV:
				this.k = 1.0 / Math.pow(this.p1.getValue(), 2);
				this.theta = this.p1.getValue() * Math.pow(this.p2.getValue(), 2);
				break;
			default:
				throw new IllegalArgumentException("Unknown parameter setup for gamma distribution.");
			}
			this.c = -this.k * Math.log(this.theta) - Gamma.lnGamma(this.k);
			this.changeInfo = new ChangeInfo(this, "Proposed: " + this.toString() + ", Old: " + old);
		} else {
			this.c = -this.k * Math.log(this.theta) - Gamma.lnGamma(this.k);
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.kCache = Double.NaN;
		this.thetaCache = Double.NaN;
		this.cCache = Double.NaN;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.k = this.kCache;
		this.theta = this.thetaCache;
		this.c = this.cCache;
		this.k = Double.NaN;
		this.thetaCache = Double.NaN;
		this.cCache = Double.NaN;
		this.changeInfo = null;
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
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
		if (this.p1 != null) {
			synchP1AndP2();
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
		if (this.p1 != null) {
			synchP1AndP2();
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
		if (this.p1 != null) {
			synchP1AndP2();
		}
	}

	/**
	 * Adjusts p1 and p2 in accordance with the current internal values
	 * following a change of the shape parameter.
	 */
	private void synchP1AndP2() {
		switch (this.setup) {
		case K_AND_THETA:
			this.p1.setValue(this.k);
			break;
		case MEAN_AND_STDEV:
			this.p1.setValue(this.k * this.theta);
			this.p2.setValue(Math.sqrt(this.k) * this.theta);
			break;
		case MEAN_AND_CV:
			this.p1.setValue(this.k * this.theta);
			this.p2.setValue(1.0 / Math.sqrt(this.k));
			break;
		default:
			throw new IllegalArgumentException("Unknown parameter setup for normal distribution.");
		}
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

}
