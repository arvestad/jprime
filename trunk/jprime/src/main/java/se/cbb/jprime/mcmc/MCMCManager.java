package se.cbb.jprime.mcmc;

/**
 * MCMC framework class for handling an MCMC chain.
 * Maintains the following elements:
 * <ul>
 * <li>a set of state parameters S1,..., Sk.</li>
 * <li>a set of proposers P1,...,Pl, which perturb the state parameters. A single
 *     proposer Pi may perturb more than one of the parameters, and a single parameter Sj may be perturbed
 *     by more than one proposer.</li>
 * <li>an acyclic digraph (DAG) of dependencies D1,...,Dm of the state parameters. These constitute
 *     e.g. cached data structures which ultimately depend on the parameters.</li>
 * <li>a list of sub-models M1,...,Mn which usually correspond to the chain of conditional probabilities
 *     of the overall model. Typically, these are a subset of the dependencies D1,...,Dm.</li>
 * </ul>
 * Apart from this, this class has:
 * <ul>
 * <li>an iterator object I.</li>
 * <li>a thinner object T which dictates how often samples are drawn for output.</li>
 * <li>a proposer selector L which governs which parameter or parameters to perturb each iteration.</li>
 * </ul>
 * 
 * @author Joel Sjöstrand.
 */
public class MCMCManager {
	
	// TODO: Implement.

}
