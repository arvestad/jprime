package se.cbb.jprime.mcmc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import se.cbb.jprime.math.PRNG;

/**
 * Interface for objects perturbing the values of one or more <code>StateParameter</code> objects,
 * e.g. MCMC parameters. This is closely connected to what is sometimes referred to as <i>operators</i> or
 * <i>kernels</i> in an MCMC context.
 * <p/>
 * Each <code>Proposer</code> is also associated with:
 * <ul>
 * <li>a <code>ProposerStatistics</code> which keeps track of how the often proposed states
 *     have been accepted or rejected (possibly including more detailed info).</li>
 * <li>a set of <code>TuningParameter</code> objects (possibly none), which typically governs the
 *     "reach" of a suggested state change. These parameters may also change over time.</li>
 * </ul>
 * Generally, for optimisation reasons, the <code>Proposer</code> is responsible for caching and
 * restoring the state parameter values rather than the parameters themselves. This is
 * because the <code>Proposer</code> perhaps only alters but a small part of the values.
 * <p/>
 * A <code>Proposer</code> may be turned off/on with method <code>setEnabled(...)</code>.
 * 
 * @author Joel Sjöstrand.
 */
public interface Proposer extends InfoProvider {

	/**
	 * Returns all parameters which can be perturbed by this object.
	 * It is not required that all these are always perturbed
	 * simultaneously, nor is it prohibited.
	 * @return the parameters.
	 */
	//public Set<StateParameter> getParameters();
	public ArrayList<StateParameter> getParameters();
	
	/**
	 * Returns the number of parameters.
	 * @return the number of parameters.
	 */
	public int getNoOfParameters();
	
	/**
	 * Returns the total number of sub-parameters contained within the
	 * parameters perturbed by this object. If a scalar parameter, 1 should
	 * be returned.
	 * @return the total number of sub-parameters.
	 */
	public int getNoOfSubParameters();
	
	/**
	 * Sets the proposal statistics of this object.
	 * @param stats the statistics.
	 */
	public void setStatistics(ProposerStatistics stats);
	
	/**
	 * Returns the proposal statistics of this object.
	 * @return the statistics.
	 */
	public ProposerStatistics getStatistics();
	
	/**
	 * Returns all tuning parameters for this object.
	 * May return null.
	 * @return the tuning parameters.
	 */
	public List<TuningParameter> getTuningParameters();
	
	/**
	 * Returns whether this proposer is active or not.
	 * @return true if enabled; false if disabled.
	 */
	public boolean isEnabled();
	
	/**
	 * Controls whether this proposer is active or not.
	 * @param isActive true to enable; false to disable.
	 */
	public void setEnabled(boolean isActive);
	
	/**
	 * Executes an actual perturbation of state parameter(s).
	 * Behaviour must adhere to the following: Change info is set for
	 * each perturbed parameter, and set to null for each non-perturbed parameter
	 * which this <code>Proposer</code> acts on.
	 * @return an object detailing the proposal.
	 */
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos);
	
	/**
	 * Clears the cached (previous) state of perturbed parameters when a proposed state has been accepted.
	 */
	public void clearCache();
	
	/**
	 * Restores the cached (previous) state of perturbed parameters when a proposed state has been rejected.
	 */
	public void restoreCache();
}
