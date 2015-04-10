package se.cbb.jprime.io;

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
	 * @param mode sampling mode.
	 * @throws IOException.
	 */
	public void writeSample(List<Sampleable> sampleables, Sampleable.SamplingMode mode) throws IOException;

	/**
	 * Writes a specified string. May be used e.g. for outputting debugging info.
	 * @param str the string.
	 * @throws IOException.
	 */
	public void writeString(String str) throws IOException;
	
	/**
	 * Returns the sample header.
	 * @param sampleables the objects to sample from (processed in list order).
	 * @return the sample.
	 */
	public String getSampleHeader(List<Sampleable> sampleables);
	
	/**
	 * Returns a sample of the current state.
	 * @param sampleables the objects to sample from (processed in list order).
	 * @param mode sampling mode.
	 * @return the sample.
	 */
	public String getSample(List<Sampleable> sampleables, Sampleable.SamplingMode mode);
}
