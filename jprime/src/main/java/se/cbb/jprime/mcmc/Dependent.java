package se.cbb.jprime.mcmc;

import java.util.Set;

/**
 * Interface for vertices in an acyclig digraph (DAG) of dependencies.
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
 * <li>D1,...,Dk changes and notifies this object: <code>this.addParentChangeInfo()</code> [invoked by each Di].</li>
 * <li>D1,...,Dk has finished updating: <code>this.update()</code>.</li>
 * <li>
 *   <ul>
 *     <li>Changes have been accepted: <code>this.clearCache()</code>.</li>
 *     <li>Changes have been rejected: <code>this.restoreCache()</code>.</li>
 *   </ul>
 * </li>
 * </ol>
 * In all calls in a cycle like above, a flag is included specifying if the
 * pending state will be sampled or not, which may be used for special
 * processing if necessary.
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
	 *        Invariant over any cache()-addParentChangeInfo()-update()-clearCache()/restoreCache() cycle.
	 */
	public void cache(boolean willSample);
	
	/**
	 * Updates this object, typically after changes of the objects it relies on.
	 * Importantly, any child dependents should be notified of this object's own change by invoking
	 * <code>child.addParentChangeInfo()</code>.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-addParentChangeInfo()-update()-clearCache()/restoreCache() cycle.
	 */
	public void update(boolean willSample);
	
	/**
	 * Clears the stored state, e.g. when a proposed state has been accepted.
	 * Also clears any stored info about previous parent changes.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-addParentChangeInfo()-update()-clearCache()/restoreCache() cycle.
	 */
	public void clearCache(boolean willSample);
	
	/**
	 * Restores the cached state, e.g. when a proposed state has been rejected.
	 * Also clears any stored info about previous parent changes.
	 * Note that if the <code>willSample</code> flag is true, it may
	 * be necessary to do additional pre-processing on the restored state (since it
	 * probably originates from an earlier non-sample iteration).
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-addParentChangeInfo()-update()-clearCache()/restoreCache() cycle.
	 */
	public void restoreCache(boolean willSample);
	
	/**
	 * Callback which parent dependents use to notify this object of
	 * changes. Any such stored info is typically cleared on a
	 * call to clearCache() or restoreCache().
	 * @param info information detailing the parent's change.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any cache()-addParentChangeInfo()-update()-clearCache()/restoreCache() cycle.
	 */
	public void addParentChangeInfo(ChangeInfo info, boolean willSample);
	
}
