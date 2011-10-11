package se.cbb.jprime.math;

import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;

/**
 * Represents a 1-D continuous uniform distribution U(a,b).
 * It is possible to let the distribution rely on two floating point state parameters and
 * act as a <code>Dependent</code>.
 *
 * @author Joel Sjöstrand.
 */
public class UniformDistribution implements Continuous1DPDDependent {

	/** First state parameter. Null if not used. */
	protected DoubleParameter p1;
	
	/** Second state parameter. Null if not used. */
	protected DoubleParameter p2;
	
	/** Bounds. Should reflect p1's and p2's values. */
	protected RealInterval ab;
	
	/** A's boundary type. */
	protected boolean isLeftOpen;
	
	/** B's boundary type. */
	protected boolean isRightOpen;
	
	/** Child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Cache. */
	private RealInterval abCache = null;

	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
	/**
	 * Constructor for when the distribution does not rely on state parameters.
	 * @param a lower bound.
	 * @param b upper bound.
	 * @param isLeftOpen true for (a,... and false for [a,...
	 * @param isRightOpen true for ...,b) and false for ...,b].
	 */
	public UniformDistribution(double a, double b, boolean isLeftOpen, boolean isRightOpen) {
		this.p1 = null;
		this.p2 = null;
		this.ab = new RealInterval(a, b, this.isLeftOpen, this.isRightOpen);
		this.dependents = null;
		this.isLeftOpen = isLeftOpen;
		this.isRightOpen = isRightOpen;
	}
	
	/**
	 * Constructor for when the distribution relies on state parameters.
	 * The distribution will add itself as a child dependent of the parameters.
	 * @param p1 the first parameter a.
	 * @param p2 the second parameter b.
	 * @param isLeftOpen true for (a,... and false for [a,...
	 * @param isRightOpen true for ...,b) and false for ...,b].
	 */
	public UniformDistribution(DoubleParameter p1, DoubleParameter p2, boolean isLeftOpen, boolean isRightOpen) {
		if (p1.getValue() >= p2.getValue()) {
			throw new IllegalArgumentException("Invalid range for uniform distribution.");
		}
		this.p1 = p1;
		this.p2 = p2;
		this.dependents = new TreeSet<Dependent>();
		p1.addChildDependent(this);
		p2.addChildDependent(this);
		this.update(false);
	}
	
	@Override
	public String getName() {
		return "Uniform distribution";
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
		this.abCache = this.ab;
	}

	@Override
	public void update(boolean willSample) {
		if (this.p1 != null) {
			// We always do an update.
			String old = this.toString();
			if (p1.getValue() >= p2.getValue()) {
				throw new IllegalArgumentException("Invalid range for uniform distribution.");
			}
			this.ab = new RealInterval(this.p1.getValue(), this.p2.getValue(), this.isLeftOpen, this.isRightOpen);
			this.changeInfo = new ChangeInfo(this, "Proposed: " + this.toString() + ", Old: " + old);
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.abCache = null;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.ab = this.abCache;
		this.abCache = null;
		this.changeInfo = null;
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}

	@Override
	public double getPDF(double x) {
		return (this.ab.isWithin(x) ? 1.0 / this.ab.getWidth() : 0.0);
	}

	@Override
	public double getCDF(double x) {
		if (x <= this.ab.getLowerBound()) {
			return 0.0;
		}
		if (x >= this.ab.getUpperBound()) {
			return 1.0;
		}
		return ((x - this.ab.getLowerBound()) / this.ab.getWidth());
	}

	@Override
	public double getQuantile(double p) {
		if (p <= 0.0) {
			return this.ab.getLowerBound();
		}
		if (p >= 1.0) {
			return this.ab.getUpperBound();
		}
		return (this.ab.getLowerBound() + p * this.ab.getWidth());
	}

	@Override
	public double getProbability(double a, double b) {
		return (this.getCDF(b) - this.getCDF(a));
	}

	@Override
	public double getMean() {
		return ((this.ab.getLowerBound() + this.ab.getUpperBound()) / 2.0);
	}

	/**
	 * Sets the mean by moving a and b but keeping the current width b-a.
	 * @param mean the new mean.
	 */
	@Override
	public void setMean(double mean) {
		double wh = this.ab.getWidth() / 2.0;
		this.ab = new RealInterval(mean - wh, mean + wh, this.isLeftOpen, this.isRightOpen);
		if (this.p1 != null) {
			p1.setValue(mean - wh);
			p2.setValue(mean + wh);
		}
	}

	@Override
	public double getMedian() {
		return ((this.ab.getLowerBound() + this.ab.getUpperBound()) / 2.0);
	}

	@Override
	public double getStandardDeviation() {
		final double sqrt12 = Math.sqrt(12);
		return (this.ab.getWidth() / sqrt12);
	}

	/**
	 * Returns the mode. Owing to its non-uniqueness, returns the distribution mean.
	 * @return the mean as the mode.
	 */
	@Override
	public double getMode() {
		return ((this.ab.getLowerBound() + this.ab.getUpperBound()) / 2.0);
	}  
	
	/**
	 * Sets the standard deviation by changing the width b-a but not the mean.
	 * @param stdev the new standard deviation.
	 */
	@Override
	public void setStandardDeviation(double stdev) {
		double wh = stdev * Math.sqrt(3);
		double m = this.getMean();
		this.ab = new RealInterval(m - wh, m + wh, this.isLeftOpen, this.isRightOpen);
		if (this.p1 != null) {
			this.p1.setValue(m - wh);
			this.p2.setValue(m + wh);
		}
	}

	@Override
	public double getVariance() {
		return (Math.pow(this.ab.getWidth(), 2) / 12.0);
	}

	/**
	 * Sets the standard deviation by changing the width b-a but not the mean.
	 * @param var the new variance.
	 */
	@Override
	public void setVariance(double var) {
		double wh = Math.sqrt(3 * var);
		double m = this.getMean();
		this.ab = new RealInterval(m - wh, m + wh, this.isLeftOpen, this.isRightOpen);
		if (this.p1 != null) {
			this.p1.setValue(m - wh);
			this.p2.setValue(m + wh);
		}
	}
	
	@Override
	public double getCV() {
		double a = this.ab.getLowerBound();
		double b = this.ab.getUpperBound();
		return ((b - a) / (Math.sqrt(3) * (b + a)));
	}

	@Override
	public RealInterval getDomainInterval() {
		return this.ab;
	}

	/**
	 * Returns a random number drawn from the distribution.
	 * May contain inconsistencies around boundary values since it relies on <code>RealInterval.getRandom()</code>.
	 * @param prng the pseudo-random number generator.
	 * @return a random value.
	 */
	@Override
	public double sampleValue(PRNG prng) {
		return this.ab.getRandom(prng);
	}

	@Override
	public String toString() {
		return ("Uniform" + this.ab.toString());
	}


}
