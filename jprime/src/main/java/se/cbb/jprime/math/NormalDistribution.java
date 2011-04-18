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
	
	/** Standard deviation. Should reflect p2's value. */
	private double stdev;
	
	/** Constant term, given the variance, in the log density function: -0.5 * ln(2 * PI * var). */
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
		this.stdev = Math.sqrt(var);
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
	public double getCDF(double x) {
		
		double xn = (x - this.mean) / this.stdev;
		if (xn < -39) { return 0.0; }
		if (xn > 9)   { return 1.0; }
		
		final double b1 =  0.319381530;
		final double b2 = -0.356563782;
		final double b3 =  1.781477937;
		final double b4 = -1.821255978;
		final double b5 =  1.330274429;
		final double p  =  0.2316419;
		final double c  =  0.39894228;
		
		if (xn >= 0.0) {
			double t = 1.0 / (1.0 + p * xn);
			return (1.0 - c * Math.exp(-xn * xn / 2.0) * t * (t *(t * (t * (t * b5 + b4) + b3) + b2) + b1));
		} else {
			double t = 1.0 / (1.0 - p * xn);
			return (c * Math.exp(-xn * xn / 2.0) * t * (t *(t * (t * (t * b5 + b4) + b3) + b2) + b1));
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
		// Algorithm from Paul M. Voutier, 2010.
		
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
			return (q * (a2 + (a1 * r + a0) / (r * r + b1 * r + b0))) * this.stdev + this.mean;
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
				return (c3 * r + cp2 + (cp1 * r + cp0) / (r * r + d1 * r + d0)) * this.stdev + this.mean;
			}
			double r = Math.sqrt(Math.log(1.0 / Math.pow(1.0 - p, 2)));
			return -(c3 * r + cp2 + (cp1 * r + cp0) / (r * r + d1 * r + d0)) * this.stdev + this.mean;
		}
		
		// Too small p-value.
		return (p < 0.5 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
	}

	@Override
	public double sampleValue(PRNG prng) {
		double x = prng.nextGaussian();
		// No bounds checking for within representable range...
		return (x * this.stdev + this.mean);
	}

}
