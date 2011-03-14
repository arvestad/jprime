package se.cbb.jprime.math;

/**
 * Represents a scale factor k, e.g. for rescaling a phylogenetic tree's time span
 * from 450 MYA to 1 (in which case k = 1/450).
 * 
 * @author Joel Sjöstrand.
 */
public class Scale {

	/**
	 * Defines the functional dependency of a non-rescaled parameter p' on its rescaled form p
	 * and the scale factor k.
	 */
	public enum ScaleDependency {
		NONE,                   /** p' = p. */
		LINEAR,                 /** p' = p * k. */
		INVERSE_LINEAR,         /** p' = p * (1 / k). Most common case. */
		SQUARED_LINEAR,         /** p' = p * k^2. */
		INVERSE_SQUARED_LINEAR  /** p' = p * (1 / k)^2. E.g. for variance parameters v=sigma^2. */
	}
	
	/** Original value. */
	private double originalValue;
	
	/** New value. */
	private double newValue;
	
	/** New value divided by original value. */
	private double scaleFactor;
	
	/**
	 * Constructor.
	 * @param originalValue original value.
	 * @param newValue new value.
	 */
	public Scale(double originalValue, double newValue) {
		if (this.originalValue == 0.0) {
			throw new IllegalArgumentException("Cannot rescale values based on original value equal to 0.");
		}
		this.originalValue = originalValue;
		this.newValue = newValue;
		this.scaleFactor = newValue / originalValue;
	}
	
	/**
	 * Returns the original value.
	 * @return the value.
	 */
	public double getOriginalValue() {
		return this.originalValue;
	}

	/**
	 * Returns the new value.
	 * @return the value.
	 */
	public double getNewValue() {
		return this.newValue;
	}

	/**
	 * Returns the new value divided by the original value.
	 * @return the scale factor.
	 */
	public double getScaleFactor() {
		return this.scaleFactor;
	}
	
	/**
	 * Returns the unscaled form p' of a scaled parameter p based
	 * on some common relationship to the scale factor.
	 * @param p the rescaled parameter.
	 * @param dep the known relationship between p and p'.
	 * @return the unscaled parameter p'.
	 */
	public double getUnscaled(double p, ScaleDependency dep) {
		switch (dep) {
		case NONE: return p;
		case LINEAR: return (p * this.scaleFactor);
		case INVERSE_LINEAR: return (p / this.scaleFactor);
		case SQUARED_LINEAR: return (p * this.scaleFactor * this.scaleFactor);
		case INVERSE_SQUARED_LINEAR: return (p / (this.scaleFactor * this.scaleFactor));
		default: throw new IllegalArgumentException("Cannot unscale parameter: unknown scale dependency type.");
		}
	}

}
