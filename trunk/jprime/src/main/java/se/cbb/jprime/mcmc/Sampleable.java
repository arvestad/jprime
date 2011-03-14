package se.cbb.jprime.mcmc;

/**
 * Interface for classes from which one can obtain a sample for output, e.g.
 * an MCMC chain.
 * 
 * @author Joel Sjöstrand.
 */
public interface Sampleable {

	/**
	 * Returns this object's name or similarly.
	 * @return the identifier.
	 */
	public String getSampleHeader();
	
	/**
	 * Returns this object's current value as a string.
	 * @return the value.
	 */
	public String getSampleValue();
	
	/**
	 * Appends this object's name or similarly.
	 * @param sb the buffer to append to.
	 */
	public void appendSampleHeader(StringBuilder sb);
	
	/**
	 * Appends this object's current value as a string.
	 * @param sb the buffer to append to.
	 */
	public void appendSampleValue(StringBuilder sb);
	
}
