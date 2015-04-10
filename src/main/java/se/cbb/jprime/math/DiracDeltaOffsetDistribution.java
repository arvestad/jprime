package se.cbb.jprime.math;

/**
 * Dirac delta function defined over the entire real line but with a single, infinitely high peak at a single value (0 in the formal definition).
 * The distribution may be translated from 0 to a specified offset.
 * 
 * @author Joel Sjöstrand.
 */
public class DiracDeltaOffsetDistribution implements Continuous1DPD {

	/**
	 * Constructor.
	 * @param offset the value of the peak. Set to 0 for the formal definition of a Dirac delta distribution.
	 */
	public DiracDeltaOffsetDistribution(double offset) {
		this.offset = offset;
	}
	
	/** Offset of function (peak occurs at offset on real line). */
	private double offset;
	
	@Override
	public String getName() {
		return "DiracDeltaOffsetDistribution";
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
		if (Math.abs(x - this.offset) < 1e-8) {
			return Double.POSITIVE_INFINITY;
		}
		return 0;
	}

	@Override
	public double getCDF(double x) {
		if (x >= this.offset) {
			return 1.0;
		}
		return 0.0;
	}

	@Override
	public double getQuantile(double p) {
		throw new UnsupportedOperationException("Cannot compute quantile for Dirac delta distrbution.");
	}

	@Override
	public double getProbability(double a, double b) {
		return (b >= this.offset && a <= this.offset ? 1.0 : 0.0);
	}

	@Override
	public double getMean() {
		return this.offset;
	}

	@Override
	public void setMean(double mean) {
		this.offset = mean;
	}

	@Override
	public double getMedian() {
		return this.offset;
	}

	@Override
	public double getMode() {
		return this.offset;
	}

	@Override
	public double getStandardDeviation() {
		return 0.0;
	}

	@Override
	public void setStandardDeviation(double stdev) {
		throw new UnsupportedOperationException("Cannot set standard deviation of Dirac delta distribution.");
	}

	@Override
	public double getVariance() {
		return 0.0;
	}

	@Override
	public void setVariance(double var) {
		throw new UnsupportedOperationException("Cannot set variance of Dirac delta distribution.");
	}

	@Override
	public double getCV() {
		return 0.0;
	}

	@Override
	public RealInterval getDomainInterval() {
		return new RealInterval();
	}

	@Override
	public double sampleValue(PRNG prng) {
		return this.offset;
	}

}
