package se.cbb.jprime.mcmc;

/**
 * Interface for proper dependents, i.e. objects which ultimately rely on
 * state parameters.
 * <p/>
 * Let D1,...,Dk be the objects on which this object relies and which have changed.
 * Typically, methods will be invoked in this order:
 * <ol>
 * <li>D1,...,Dk have changed: <code>this.cacheAndUpdateAndSetChangeInfo()</code>. Internally, this typically
 *     means:
 *     <ol>
 *         <li>Querying {D1,...,Dk}<code>.getChangeInfo()</code> for info.</li>
 *         <li>Caching relevant parts.</li>
 *         <li>Updating relevant parts.</li>
 *         <li>Setting this object's own change info.</li>
 *     </ol>
 * <li>After all state changes:
 *   <ul>
 *     <li>State has been accepted: <code>this.clearCacheAndClearChangeInfo()</code>.</li>
 *     <li>State has been rejected: <code>this.restoreCacheAndClearChangeInfo()</code>.</li>
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
	 * Caches this object, then updates, then sets its change info.
	 * It may be assumed that prior to this call, all parent dependents have
	 * been changed and updated, and their change info can be acquired through <code>parent.getChangeInfo()</code>.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any
	 *        <code>cacheAndUpdateAndSetChangeInfo()-clearCacheAndClearChangeInfo()/restoreCacheAndClearChangeInfo()</code> cycle.
	 */
	public void cacheAndUpdateAndSetChangeInfo(boolean willSample);
	
	/**
	 * Clears the cached (old) state and change info when a proposed state has been accepted.
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any
	 *        <code>cacheAndUpdateAndSetChangeInfo()-clearCacheAndClearChangeInfo()/restoreCacheAndClearChangeInfo()</code> cycle.
	 */
	public void clearCacheAndClearChangeInfo(boolean willSample);
	
	/**
	 * Restores the cached state when a proposed state has been rejected, and also
	 * clears the change info.
	 * Note that if the <code>willSample</code> flag is true, it may
	 * be necessary to do additional pre-processing on the restored state (since it
	 * probably originates from an earlier non-sample iteration).
	 * @param willSample true if the pending state will be sampled; false if not sampled.
	 *        Invariant over any
	 *        <code>cacheAndUpdateAndSetChangeInfo()-clearCacheAndClearChangeInfo()/restoreCacheAndClearChangeInfo()</code> cycle.
	 */
	public void restoreCacheAndClearChangeInfo(boolean willSample);
	
}
