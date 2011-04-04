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
	 * Writes the sample header.
	 * @param sampleables the objects to sample from (processed in list order).
	 * @throws IOException.
	 */
	public void writeSampleHeader(List<Sampleable> sampleables) throws IOException;
	
	/**
	 * Writes a sample of the current state.
	 * @param sampleables the objects to sample from (processed in list order).
	 * @throws IOException.
	 */
	public void writeSample(List<Sampleable> sampleables) throws IOException;
}
