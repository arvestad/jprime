package se.cbb.jprime.mcmc;

import java.io.IOException;
import java.util.List;

/**
 * Interface for objects sampling from a list of "sampleables".
 * How this is performed (to file, standard out, etc.) is up
 * to implementing classes.
 * 
 * @author Joel Sjöstrand.
 */
public interface Sampler {

	/**
	 * Sets the sampleables.
	 * @param sampleables the "sampleable" objects.
	 */
	public void setSampleables(List<Sampleable> sampleables);
	
	/**
	 * Returns the "sampleable" objects.
	 * @return the "sampleables".
	 */
	public List<Sampleable> getSampleables();
	
	/**
	 * Writes the sample header.
	 * @throws IOException.
	 */
	public void writeSampleHeader() throws IOException;
	
	/**
	 * Writes a sample of the current state.
	 * @throws IOException.
	 */
	public void writeSample() throws IOException;
}
