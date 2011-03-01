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
 * 
 * @author Joel Sjöstrand.
 */
public interface Dependent {

	/**
	 * Returns true if this instance is a sink in the DAG, i.e.
	 * has no dependents.
	 * @return true if source; false if it depends on others.
	 */
	public boolean isSink();
	
	/**
	 * Adds a dependent that relies on this object, i.e. a children
	 * of this vertex in the corresponding DAG.
	 * @param dep the dependent.
	 */
	public void addDependent(Dependent dep);
	
	/**
	 * Returns all dependents which rely on this object, i.e. the
	 * children of this vertex in the corresponding DAG.
	 * @return all dependents.
	 */
	public List<Dependent> getDependents();
	
	/**
	 * Stores the current state e.g. prior to a perturbation.
	 */
	public void cache();
	
	/**
	 * Restores the cached state, e.g. when a proposed state
	 * has been rejected.
	 */
	public void restore();
	
	/**
	 * If available, provides information on the current changes of
	 * this object. This may be utilised by children to
	 * perform optimised updates.
	 * @return info disclosing what has changed on this object.
	 */
	public PerturbationInfo getPerturbationInfo();
	
	/**
	 * Sets perturbation info detailing what has changed in this object.
	 * This may e.g. be utilised by a <code>Proposer</code> when it perturbs
	 * a state parameter.
	 * @param info the info detailing the change.
	 */
	public void setPerturbationInfo(PerturbationInfo info);
}
