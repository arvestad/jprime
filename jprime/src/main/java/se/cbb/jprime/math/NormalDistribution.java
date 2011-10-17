package se.cbb.jprime.math;

import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.ChangeInfo;

/**
 * Represents a 1-D normal distribution, "Gaussian", N(m,v).
 * <p/>
 * It is possible to let the distribution rely on two floating point state parameters and
 * act as a <code>Dependent</code>.
 * 
 * @author Martin Linder.
 * @author Bengt Sennblad.
 * @author Joel Sjöstrand.
 */
public class NormalDistribution implements Continuous1DPDDependent {
	
	/**
	 * When the distribution depends on state parameters, defines
	 * what the latter represent.
	 */
	public enum ParameterSetup {
		/** Mean and variance. */                  MEAN_AND_VAR,
		/** Mean and standard deviation. */        MEAN_AND_STDEV,
		/** Mean and coefficient of variation. */  MEAN_AND_CV
	}
	
	/** First state parameter. Null if not used. */
	protected DoubleParameter p1;
	
	/** Second state parameter. Null if not used. */
	protected DoubleParameter p2;
	
	/** State parameter representation. Null if not used. */
	protected ParameterSetup setup;
	
	/** Mean value. Should reflect p1's value. */
	protected double mean;
	
	/** Variance. Should reflect p2's value. */
	protected double var;
	
	/** Standard deviation. Should reflect p2's value. */
	protected double stdev;
	
	/** Constant term, given the variance, in the log density function: -0.5 * ln(2 * PI * var). */
	protected double logDensFact;
	
	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor for when the distribution does not rely on state parameters.
	 * @param mean distribution mean.
	 * @param var distribution variance.
	 */
	public NormalDistribution(double mean, double var) {
		if (var <= 0.0) {
			throw new IllegalArgumentException("Cannot have non-positive variance for normal distribution.");
		}
		this.p1 = null;
		this.p2 = null;
		this.setup = null;
		this.mean = mean;
		this.var = var;
		this.stdev = Math.sqrt(var);
		this.update();
	}
	
	/**
	 * Constructor for when the distribution relies on state parameters.
	 * The distribution will add itself as a child dependent of the parameters.
	 * @param p1 the first parameter.
	 * @param p2 the second parameter.
	 * @param setup what p1 and p2 represents.
	 */
	public NormalDistribution(DoubleParameter p1, DoubleParameter p2, ParameterSetup setup) {
		this.p1 = p1;
		this.p2 = p2;
		this.setup = setup;
		this.update();
	}
	
	@Override
	public String getName() {
		return "Normal distribution";
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
		return Math.exp(-0.5 * Math.pow(x - this.mean, 2) / this.var + this.logDensFact);
	}

	@Override
	public double getCDF(double x) {
		x = (x - this.mean) / this.stdev;
		return Normal.cdf(x);
	}

	@Override
	public double getProbability(double a, double b) {
		return (this.getCDF(b) - this.getCDF(a));
	}

	@Override
	public double getMean() {
		return this.mean;
	}

	@Override
	public double getMedian() {
		return this.mean;
	}

	@Override
	public double getStandardDeviation() {
		return this.stdev;
	}

	@Override
	public double getVariance() {
		return this.var;
	}

	@Override
	public double getCV() {
		return (this.stdev / Math.abs(this.mean));
	}

	@Override
	public RealInterval getDomainInterval() {
		return new RealInterval();
	}

	@Override
	public void cacheAndUpdateAndSetChangeInfo(boolean willSample) {
		// Caching not worthwhile.
		update();
	}

	@Override
	public void clearCacheAndClearChangeInfo(boolean willSample) {
		this.changeInfo = null;
	}

	@Override
	public void restoreCacheAndClearChangeInfo(boolean willSample) {
		// Just do clean update.
		this.update();
		this.changeInfo = null;
	}

	@Override
	public Dependent[] getParentDependents() {
		if (this.p1 != null) {
			return new Dependent[] { p1, p2 };
		}
		return null;
	}

	/**
	 * Updates the distribution.
	 */
	public void update() {
		// We always do an update.
		if (this.p1 != null) {
			String old = this.toString();
			switch (this.setup) {
			case MEAN_AND_VAR:
				this.mean = this.p1.getValue();
				this.var = this.p2.getValue();
				this.stdev = Math.sqrt(this.var);
				break;
			case MEAN_AND_STDEV:
				this.mean = this.p1.getValue();
				this.stdev = this.p2.getValue();
				this.var = Math.pow(this.stdev, 2);
				break;
			case MEAN_AND_CV:
				this.mean = this.p1.getValue();
				this.stdev = this.p2.getValue() * this.mean;
				this.var = Math.pow(this.stdev, 2);
				break;
			default:
				throw new IllegalArgumentException("Unknown parameter setup for normal distribution.");
			}
			this.logDensFact = -0.5 * Math.log(2 * Math.PI * this.var);
			this.changeInfo = new ChangeInfo(this, "Proposed: " + this.toString() + ", Old: " + old);
		} else {
			this.logDensFact = -0.5 * Math.log(2 * Math.PI * this.var);
		}
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}
	
	@Override
	public String toString() {
		return "N(" + this.mean + ", " + this.var + ')';
	}

	@Override
	public void setMean(double mean) {
		this.mean = mean;
		if (this.p1 != null) {
			switch (this.setup) {
			case MEAN_AND_VAR:
			case MEAN_AND_STDEV:
			case MEAN_AND_CV:
				this.p1.setValue(mean);
				break;
			default:
				throw new IllegalArgumentException("Unknown parameter setup for normal distribution.");
			}
		}
	}
	
	@Override
	public void setStandardDeviation(double stdev) {
		if (stdev <= 0.0) {
			throw new IllegalArgumentException("Cannot have non-positive standard deviation for normal distribution.");
		}
		this.stdev = stdev;
		this.var = Math.pow(stdev, 2);
		if (this.p2 != null) {
			switch (this.setup) {
			case MEAN_AND_VAR:
				this.p2.setValue(this.var);
				break;
			case MEAN_AND_STDEV:
				this.p2.setValue(stdev);
			case MEAN_AND_CV:
				this.p2.setValue(stdev / Math.abs(this.mean));
				break;
			default:
				throw new IllegalArgumentException("Unknown parameter setup for normal distribution.");
			}
		}
		this.logDensFact = -0.5 * Math.log(2 * Math.PI * this.var);
	}

	@Override
	public void setVariance(double var) {
		if (var <= 0.0) {
			throw new IllegalArgumentException("Cannot have non-positive variance for normal distribution.");
		}
		this.var = var;
		this.stdev = Math.sqrt(var);
		if (this.p2 != null) {
			switch (this.setup) {
			case MEAN_AND_VAR:
				this.p2.setValue(var);
				break;
			case MEAN_AND_STDEV:
				this.p2.setValue(this.stdev);
			case MEAN_AND_CV:
				this.p2.setValue(this.stdev / Math.abs(this.mean));
				break;
			default:
				throw new IllegalArgumentException("Unknown parameter setup for normal distribution.");
			}
		}
		this.logDensFact = -0.5 * Math.log(2 * Math.PI * this.var);
	}

	@Override
	public double getQuantile(double p) {
		return (Normal.quantile(p) * this.stdev + this.mean);
	}

	@Override
	public double sampleValue(PRNG prng) {
		double x = prng.nextGaussian();
		// No bounds checking for within representable range...
		return (x * this.stdev + this.mean);
	}

	@Override
	public double getMode() {
		return this.mean;
	}

}
