package se.cbb.jprime.mcmc;

import java.util.Set;

/**
 * Interface for vertices in an acyclig digraph (DAG) of model dependencies.
 * Most commonly, such a graph will consist of state parameters as
 * sources, sub-models corresponding to conditional probabilities as sinks,
 * possibly with cached data structures in between.
 * Note: The DAG will induce the Bayesian hierarchy of the sub-models, but, as
 * mentioned, allows for additional intermediary data structures.
 * <p/>
 * The purpose of this interface is mainly to be able to do optimised updates
 * when state parameters have been perturbed or restored.
 * <p/>
 * Let D1,...,Dk be the objects on which this object relies (or a subset thereof).
 * Typically, methods will be invoked in this order:
 * <ol>
 * <li>D1,...,Dk are about to change: <code>this.cache()</code>.</li>
 * <li>D1,...,Dk has finished updating: <code>this.update()</code>. When doing this,
 *     the child should be able to query its parents for updates through
 *     <code>di.getChangeInfo()</code>.</li>
 * <li>
 *   <ul>
 *     <li>Changes have been accepted: <code>this.clearCache()</code>.</li>
 *     <li>Changes have been rejected: <code>this.restoreCache()</code>.</li>
 *   </ul>
 * </li>
 * </ol>
 * In all calls in a cycle like above, a flag is included specifying if the
 * pending state will be sampled or not, which may be used for special
 * processing if necessary. See also interface <code>StateParameter</code>.
 * 
 * @author Joel Sjöstrand.
 */
public interface Dependent {
	
	/**
	 * Returns true if this instance is a sink in the DAG, i.e.
	 * has no dependents.
	 * @return true if sink; false if others rely on it.
	 */
	public boolean isDependentSink();
	
	/**
	 * Adds a dependent that relies on this object, i.e. a child
	 * of this vertex in the corresponding DAG.
	 * @param dep the child dependent.
	 */
	public void addChildDependent(Dependent dep);
	
	/**
	 * Returns all dependents which rely on this object, i.e. the
	 * children of this vertex in the corresponding DAG.
	 * May return null if sink.
	 * @return all dependents.
	 */
	public Set<Dependent> getChildDependents();
	
	/**
	 * Stores the current state e.g. prior to a change.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void cache(boolean willSample);
	
	/**
	 * Updates this object, typically after changes of the objects it relies on.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void update(boolean willSample);
	
	/**
	 * Clears the stored state and change info, e.g. when a proposed state has been accepted.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void clearCache(boolean willSample);
	
	/**
	 * Restores the cached state, e.g. when a proposed state has been rejected, and also
	 * clears the change info.
	 * Note that if the <code>willSample</code> flag is true, it may
	 * be necessary to do additional pre-processing on the restored state (since it
	 * probably originates from an earlier non-sample iteration).
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-update()-clearCache()/restoreCache() cycle.
	 */
	public void restoreCache(boolean willSample);
	
	/**
	 * Method which child dependents may use to retrieves info on the change of this object.
	 * Returning null is considered indication of an unchanged state.
	 * @return info information detailing this object's change; null if unchanged.
	 */
	public ChangeInfo getChangeInfo();
	
}
