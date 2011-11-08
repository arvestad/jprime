package se.cbb.jprime.mcmc;

import java.util.Map;

/**
 * Interface for proper dependents, i.e. objects which rely on other proper dependents or
 * state parameters. Unlike a <code>StateParameter</code>, a <code>ProperDependent</code>
 * is itself responsible for caching and restoring its contents.
 * <p/>
 * Let D1,...,Dk be the objects on which this object relies and which may have changed.
 * Typically, methods will be invoked in this order:
 * <ol>
 * <li>D1,...,Dk may have changed: <code>this.cacheAndUpdate()</code>.</li>
 * <li>After entire DAG structure has been updated:
 *   <ul>
 *     <li>State has been accepted: <code>this.clearCache()</code>.</li>
 *     <li>State has been rejected: <code>this.restoreCache()</code>.</li>
 *   </ul>
 * </li>
 * </ol>
 * In all calls in a cycle like above, a flag is included specifying if the
 * pending state will be sampled or not, which may be used for special
 * processing if necessary.
 * 
 * @author Joel Sjöstrand.
 */
public interface ProperDependent extends Dependent {
	
	/**
	 * Returns all dependents on which this object relies, i.e. the
	 * parents of this vertex in the corresponding DAG.
	 * @return all dependents.
	 */
	public Dependent[] getParentDependents();
	
	/**
	 * Caches and updates this object.<br/>
	 * <b>Input:</b> All changes made so far, i.e.,
	 * changes to dependent objects which precede this in the dependency DAG.<br/>
	 * <b>Output:</b> Changes made to this object are added to the input.
	 * <p/>
	 * Behaviour must adhere to the following:
	 * <ol>
	 * <li>This objects queries input for change info of its parents.</li>
	 * <li>If no parent has changed:
	 *   <ol>
	 *   <li>This object sets its own change info to null.</li>
	 *   <li>Exit.</li>
	 *   </ol>
	 * </li>
	 * <li>Else:
	 *   <ol>
	 *   <li>This object caches.</li>
	 *   <li>This object sets its own change info.</li>
	 *   <li>Exit.</li>
	 *   </ol> 
	 * </li> 
	 * </ol>
	 * @param changeInfos all changes made so far, including those of this objects parents, if any
	 *        (null is interpreted as no change).
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any
	 *        <code>cacheAndUpdate()-clearCache()/restoreCache()</code> cycle.
	 */
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample);
	
	/**
	 * Clears the cached (old) state when a proposed state has been accepted.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any
	 *        <code>cacheAndUpdate()-clearCache()/restoreCache()</code> cycle.
	 */
	public void clearCache(boolean willSample);
	
	/**
	 * Restores the cached state when a proposed state has been rejected.
	 * Note that if the <code>willSample</code> flag is true, it may
	 * be necessary to do additional pre-processing on the restored state (since it
	 * probably originates from an earlier non-sample iteration).
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any
	 *        <code>cacheAndUpdate()-clearCache()/restoreCache()</code> cycle.
	 */
	public void restoreCache(boolean willSample);
	
}
