package se.cbb.jprime.mcmc;

import java.util.Map;

/**
 * Interface for generative models.
 * 
 * @author Joel Sjöstrand.
 */
public interface GenerativeModel extends Model {
	
	/**
	 * Returns the parameters of the model.
	 * @return the parameters as key-value pairs.
	 */
	public Map<String, String> getModelParameters();
}
