package se.cbb.jprime.mcmc;

import java.util.Set;

/**
 * Interface for objects perturbing the values of one or more
 * <code>Parameter</code> objects, e.g. MCMC parameters.
 * Each <code>Perturber</code> is also associated with a <code>PerturberWeight</code> which
 * dictates how often it will in fact be invoked to perform a perturbation (the weight
 * may e.g. change over time).
 * 
 * @author Joel Sjöstrand.
 */
public interface Perturber {

	/**
	 * Returns the parameters perturbed by this object.
	 * @return the parameters.
	 */
	public Set<Parameter> getParameters();
	
	/**
	 * Returns the number of parameters.
	 * @return the number of parameters.
	 */
	public int getNoOfParameters();
	
	/**
	 * Returns the number of sub-parameters contained within the
	 * parameters perturbed by this object.
	 * @return the total number of sub-parameters.
	 */
	public int getTotalNoOfSubParameters();
	
	/**
	 * Returns the weight object associated with this object.
	 * See also <code>getWeight()</code>.
	 * @return the weight object.
	 */
	public PerturberWeight getPerturberWeight();
	
	/**
	 * Shorthand for retrieving the current weight of the
	 * <code>PerturberWeight</code> associated with this object.
	 * @return the current weight.
	 */
	public double getWeight();
}
