package se.cbb.jprime.math;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.PerturbationInfo;

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
public class NormalDistribution implements Continuous1DPD, Dependent {
	
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
	private DoubleParameter p1;
	
	/** Second state parameter. Null if not used. */
	private DoubleParameter p2;
	
	/** State parameter representation. Null if not used. */
	private ParameterSetup setup;
	
	/** Mean value. Should reflect p1's value. */
	private double mean;
	
	/** Variance. Should reflect p2's value. */
	private double var;
	
	/** Constant term, given the variance, in the log density function: -0.5 * ln(2 * pi * var). */
	private double logDensFact;
	
	/** Child dependents. */
	private ArrayList<Dependent> dependents;
	
	/** Perturbation info which may be used by proposers. */
	private PerturbationInfo perturbationInfo = null;
	
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
		this.dependents = new ArrayList<Dependent>();
		this.mean = mean;
		this.var = var;
		this.update(false);
	}
	
	/**
	 * Constructor for when the distribution relies on state parameters.
	 * @param p1 the first parameter.
	 * @param p2 the second parameter.
	 * @param setup what p1 and p2 represents.
	 */
	public NormalDistribution(DoubleParameter p1, DoubleParameter p2, ParameterSetup setup) {
		this.p1 = p1;
		this.p2 = p2;
		this.setup = setup;
		this.dependents = new ArrayList<Dependent>();
		this.update(false);
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
	public double getCDF(double y) {
		if (y < 1e-100) { return 0.0; }
		if (y > 1e100)  { return 1.0; }

		double x = (y - this.mean) / this.var;

		double b1 = 0.319381530;
		double b2 = -0.356563782;
		double b3 =  1.781477937;
		double b4 = -1.821255978;
		double b5 =  1.330274429;
		double p  =  0.2316419;
		double c  =  0.39894228;
		
		if (x >= 0.0) {
			double t = 1.0 / (1.0 + p * x);
			return (1.0 - c * Math.exp(-x * x / 2.0) * t * (t *(t * (t * (t * b5 + b4) + b3) + b2) + b1));
		} else {
			double t = 1.0 / (1.0 - p * x);
			return (c * Math.exp(-x * x / 2.0) * t * (t *(t * (t * (t * b5 + b4) + b3) + b2) + b1));
		}
	}

	@Override
	public double getProbability(double a, double b) {
		return (this.getCDF(b) - this.getCDF(a));
	}

	@Override
	public double getMean() throws MathException {
		return this.mean;
	}

	@Override
	public double getMedian() throws MathException {
		return this.mean;
	}

	@Override
	public double getStandardDeviation() throws MathException {
		return Math.sqrt(this.var);
	}

	@Override
	public double getVariance() throws MathException {
		return this.var;
	}

	@Override
	public double getCV() throws MathException {
		return (Math.sqrt(this.var) / Math.abs(this.mean));
	}

	@Override
	public RealInterval getDomainInterval() {
		return new RealInterval();
	}

	@Override
	public boolean isSource() {
		return (this.p1 == null);
	}

	@Override
	public boolean isSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public List<Dependent> getChildDependents() {
		return this.dependents;
	}

	@Override
	public List<Dependent> getParentDependents() {
		if (p1 == null) {
			return null;
		}
		ArrayList<Dependent> l = new ArrayList<Dependent>(2);
		l.add(this.p1);
		l.add(this.p2);
		return l;
	}

	@Override
	public void cache(boolean willSample) {
		// Not worthwhile.
	}

	@Override
	public void update(boolean willSample) {
		if (this.p1 != null) {
			switch (this.setup) {
			case MEAN_AND_VAR:
				this.mean = this.p1.getValue();
				this.var = this.p2.getValue();
				break;
			case MEAN_AND_STDEV:
				this.mean = this.p1.getValue();
				this.var = Math.pow(this.p2.getValue(), 2);
				break;
			case MEAN_AND_CV:
				this.mean = this.p1.getValue();
				this.var = Math.pow(this.p2.getValue() * this.mean, 2);
				break;
			default:
				throw new IllegalArgumentException("Unknown parameter setup for normal distribution.");
			}
		}
		this.logDensFact = -0.5 * Math.log(2 * Math.PI * this.var);
	}

	@Override
	public void clearCache(boolean willSample) {
		this.perturbationInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		// Just do clean update.
		this.update(willSample);
		this.perturbationInfo = null;
	}

	@Override
	public PerturbationInfo getPerturbationInfo() {
		return this.perturbationInfo;
	}

	@Override
	public void setPerturbationInfo(PerturbationInfo info) {
		this.perturbationInfo = info;
	}

	@Override
	public String toString() {
		return "N(" + this.mean + ',' + this.var + ')';
	}

	@Override
	public void setMean(double mean) throws MathException {
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
	public void setStandardDeviation(double stdev) throws MathException {
		if (stdev <= 0.0) {
			throw new IllegalArgumentException("Cannot have non-positive standard deviation for normal distribution.");
		}
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
	public void setVariance(double var) throws MathException {
		if (var <= 0.0) {
			throw new IllegalArgumentException("Cannot have non-positive variance for normal distribution.");
		}
		this.var = var;
		if (this.p2 != null) {
			switch (this.setup) {
			case MEAN_AND_VAR:
				this.p2.setValue(var);
				break;
			case MEAN_AND_STDEV:
				this.p2.setValue(Math.sqrt(var));
			case MEAN_AND_CV:
				this.p2.setValue(Math.sqrt(var) / Math.abs(this.mean));
				break;
			default:
				throw new IllegalArgumentException("Unknown parameter setup for normal distribution.");
			}
		}
		this.logDensFact = -0.5 * Math.log(2 * Math.PI * this.var);
	}

}
