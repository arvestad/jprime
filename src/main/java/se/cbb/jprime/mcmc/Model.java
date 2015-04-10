package se.cbb.jprime.mcmc;

/**
 * Simple base interface for model interfaces (typically generative and/or those supporting computing data probability).
 * 
 * @author Joel Sjöstrand.
 */
public interface Model {

	/**
	 * Returns the model name.
	 * @return the model name.
	 */
	public String getModelName();
	
}
