package se.cbb.jprime.mcmc;

/**
 * Interface for data types that can be serialised for sampling.
 * Implementing classes may e.g. provide convenience methods
 * for serialisation (MCMC chain) and de-serialisation (chain parser).
 * 
 * @author Joel Sjöstrand.
 */
public interface SampleType {

	/**
	 * Returns a string ID for this data type,
	 * e.g. 'Double' or 'LogDouble'.
	 * @return the type.
	 */
	public String getType();
	
	/**
	 * Appends a string ID for this data type,
	 * e.g. 'Double' or 'LogDouble'.
	 * @param sb the buffer to append to.
	 */
	public void appendType(StringBuilder sb);
}
