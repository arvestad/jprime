package se.cbb.jprime.io;

/**
 * Interface for classes from which one can obtain a sample for output, e.g.
 * an MCMC chain.
 * 
 * @author Joel Sjöstrand.
 */
public interface Sampleable {

	/**
	 * Returns the sample type.
	 * Must not return null.
	 * @return the sample type.
	 */
	public Class<?> getSampleType();
	
	/**
	 * Returns this object's name or some similar ID.
	 * Must not return null. Preferred format: 'MyFantasticVariable'.
	 * @return the identifier.
	 */
	public String getSampleHeader();
	
	/**
	 * Returns this object's current value as a string.
	 * Must not return null.
	 * @return the value.
	 */
	public String getSampleValue();
	
}
