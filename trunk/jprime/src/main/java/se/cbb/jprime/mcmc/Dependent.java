package se.cbb.jprime.mcmc;

/**
 * Interface for vertices in an acyclig digraph (DAG) of model dependencies.
 * Most commonly, such a graph will consist of state parameters as
 * sources, sub-models corresponding to conditional probabilities as sinks,
 * possibly with cached data structures in between.
 * Note: The DAG will induce any Bayesian hierarchy of the sub-models, but, as
 * mentioned, allows for additional intermediary data structures.
 * <p/>
 * See sub-interfaces <code>StateParameter</code> and <code>ProperDependent</code>
 * for details on the expected behaviour. The purpose of these interfaces is mainly to
 * be able to do optimised updates when state changes have been proposed or rejected.
 * 
 * @author Joel Sjöstrand.
 */
public abstract interface Dependent {
	
	/**
	 * Returns all dependents on which this object relies, i.e. the
	 * parents of this vertex in the corresponding DAG.
	 * @return all parent dependents (if any).
	 */
	public Dependent[] getParentDependents();
	
}
