package se.cbb.jprime.mcmc;

import java.util.List;

/**
 * Interface for generative models.
 * 
 * @author Joel Sjöstrand.
 */
public interface GenerativeModel extends Model {
	
	/**
	 * Returns the state parameters of the model.
	 * @return the parameters.
	 */
	public List<StateParameter> getModelParameters();
}
