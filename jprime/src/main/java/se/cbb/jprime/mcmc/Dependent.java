package se.cbb.jprime.mcmc;

import java.util.List;

/**
 * Interface for vertices in an acyclig digraph (DAG) of dependencies.
 * Most commonly, such a graph will consist of state parameters as
 * sources, sub-models corresponding to conditional probabilities as sinks,
 * possibly with cached data structures in between.
 * <p/>
 * The purpose of this interface is mainly to be able to do optimised updates
 * when state parameters have been perturbed or restored.
 * <p/>
 * Let D1,...,Dk be the objects on which this object relies.
 * Typically, methods will be invoked in this order:
 * <ol>
 * <li>D1,...,Dk (or a subset thereof) is about to change: <code>cache()</code>.</li>
 * <li>D1,...,Dk has changed: <code>update()</code>.</li>
 * <li>
 *   <ul>
 *     <li>Changes to D1,...,Dk has been accepted: <code>clearCache()</code>.</li>
 *     <li>Changes to D1,...,Dk has been rejected: <code>restoreCache()</code>.</li>
 *   </ul>
 * </li>
 * </ol>
 * Additionally, in all calls in a cycle like above, a flag is included specifying if the
 * pending state will be sampled or not, which may be used for special
 * processing if necessary.
 * 
 * @author Joel Sjöstrand.
 */
public interface Dependent {

	/**
	 * Returns true if this instance is a source in the DAG, i.e.
	 * is not dependent.
	 * @return true if source; false if it depends on others.
	 */
	public boolean isSource();
	
	/**
	 * Returns true if this instance is a sink in the DAG, i.e.
	 * has no dependents.
	 * @return true if sink; false if others rely on it.
	 */
	public boolean isSink();
	
	/**
	 * Adds a dependent that relies on this object, i.e. a child
	 * of this vertex in the corresponding DAG.
	 * @param dep the dependent.
	 */
	public void addChildDependent(Dependent dep);
	
	/**
	 * Returns all dependents which rely on this object, i.e. the
	 * children of this vertex in the corresponding DAG.
	 * May yield null if sink.
	 * @return all dependents.
	 */
	public List<Dependent> getChildDependents();
	
	/**
	 * Returns all dependents which this object relies on, i.e. the
	 * parents of this vertex in the corresponding DAG. May yield null
	 * if source.
	 * @return all dependents.
	 */
	public List<Dependent> getParentDependents();
	
	/**
	 * Stores the current state e.g. prior to a perturbation.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void cache(boolean willSample);
	
	/**
	 * Updates this object, e.g. after a perturbation of an object it relies on.
	 * This method should only be invoked when an update might be required.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void update(boolean willSample);
	
	/**
	 * Clears the stored state, e.g. when a proposed state has been accepted.
	 * Also clears the stored perturbation info, if any.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void clearCache(boolean willSample);
	
	/**
	 * Restores the cached state, e.g. when a proposed state
	 * has been rejected.
	 * Also clears the stored perturbation info, if any.
	 * Note that if the <code>willSample</code> flag is true, it may
	 * be necessary to do additional pre-processing on the restored state (since it
	 * probably originates from an earlier non-sample iteration).
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void restoreCache(boolean willSample);
	
	/**
	 * If available, provides information on the current changes of
	 * this object. Its contents may be utilised by children to
	 * perform optimised updates. The info object is typically cleared on a call
	 * to clearCache() or restoreCache().
	 * @return info disclosing what has changed on this object.
	 */
	public PerturbationInfo getPerturbationInfo();
	
	/**
	 * Sets perturbation info detailing what has changed in this object.
	 * This may be set by a <code>Proposer</code> when it perturbs
	 * a state parameter. The info object is typically cleared on a call
	 * to clearCache() or restoreCache().
	 * @param info the info detailing the change.
	 */
	public void setPerturbationInfo(PerturbationInfo info);
}
