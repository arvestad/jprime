package se.cbb.jprime.math;

/**
 * Represents a scale factor k and offset m, e.g. for rescaling a phylogenetic tree's time span
 * according to y = k*x+m.
 * For example, it might be desirable to transform the original span 0-450 into 1-0, in which case 
 * k = -1/450 and m = 1. 
 * <p/>
 * In addition, the class also provides convenience methods
 * <code>scale()</code> and <code>unscale()</code> for parameters that rely on
 * the scale instance in slightly different fashions. Thus, if computations are carried
 * out in "scaled form", this class can help with "unscaling" them.
 * 
 * @author Joel Sjöstrand.
 */
public class ScaleTransformation {

	/**
	 * Defines common relationships between a scaled parameter y and its unscaled form
	 * x when depending on scale factor k and offset m.
	 */
	public enum TransformationType {
		/** y=x, i.e. x=y.*/                  NONE,
		/** y=k*x+m, i.e., x=(y-m)/k. */      LINEAR,
		/** y=k*x, i.e., x=y/k. */            COEFF,
		/** y=abs(k)*x, i.e., x=y/abs(k). */  ABS_COEFF,
		/** y=k*k*x, i.e. x=y/k^2. */         COEFF_SQUARED
	}
	
	/** Dependency type. */
	private TransformationType type;
	
	/** Scale factor. */
	private double k;
	
	/** Offset. */
	private double m;
	
	/**
	 * Constructor.
	 * @param dep the transformation type.
	 * @param k scale factor.
	 * @param m offset.
	 */
	public ScaleTransformation(TransformationType type, double k, double m) {
		if (k == 0.0) {
			throw new IllegalArgumentException("Cannot create scale factor with coefficient 0.");
		}
		this.type = type;
		this.k = k;
		this.m = m;
	}
	
	/**
	 * Returns the scale factor k.
	 * @return the scale factor.
	 */
	public double getScaleFactor() {
		return this.k;
	}

	/**
	 * Returns the offset.
	 * @return the offset.
	 */
	public double getOffset() {
		return this.m;
	}
	
	/**
	 * Transforms a parameter x into its scaled form y.
	 * @param x the parameter.
	 * @return the scaled value y.
	 */
	public double getScaled(double x) {
		switch (this.type) {
		case NONE: return x;
		case LINEAR: return (this.k * x + m);
		case COEFF: return (this.k * x);
		case ABS_COEFF: return Math.abs(this.k) * x;
		case COEFF_SQUARED: return (this.k * this.k * x);
		default: throw new IllegalArgumentException("Cannot scale parameter: unknown scale transformation type.");
		}
	}
	
	/**
	 * Un-transforms a parameter y into its original form x.
	 * @param y the parameter.
	 * @return the unscaled value x.
	 */
	public double getUnscaled(double y) {
		switch (this.type) {
		case NONE: return y;
		case LINEAR: return ((y - m) / this.k);
		case COEFF: return (y / this.k);
		case ABS_COEFF: return (y / Math.abs(this.k));
		case COEFF_SQUARED: return (y / (this.k * this.k));
		default: throw new IllegalArgumentException("Cannot scale parameter: unknown scale transformation type.");
		}
	}

}
