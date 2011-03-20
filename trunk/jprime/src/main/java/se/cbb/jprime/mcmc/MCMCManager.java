package se.cbb.jprime.mcmc;

/**
 * MCMC framework class for handling an MCMC chain.
 * Maintains the following elements:
 * <ul>
 * <li>a set of state parameters S1,..., Sk.</li>
 * <li>a set of proposers P1,...,Pl, which perturb the state parameters. A single
 *     proposer Pi may perturb more than one of the parameters, and a single parameter Sj may be perturbed
 *     by more than one proposer (but never at the same time).</li>
 * <li>a list of sub-models M1,...,Mn which usually correspond to the chain of conditional probabilities
 *     of the overall model.</li>
 * <li>an acyclic digraph (DAG) of dependencies D1,...,Dm of the state parameters. These typically constitute
 *     of the state parameters S1,...,Sk as sources, the sub-models M1,...,Mn as sinks, and possibly cached
 *     data structures in between.</li>
 * <li>a list of "sampleable" objects to sample from, C1,...,Cv. These are usually comprised
 * of the state parameters S1,...,Sk.
 * </ul>
 * Apart from this, the class has:
 * <ul>
 * <li>an iterator object I.</li>
 * <li>a thinner object T which dictates how often samples are drawn for output.</li>
 * <li>a proposer selector L which governs which parameter or parameters to perturb each iteration.</li>
 * <li>a proposal acceptor A which decides whether the proposed state should be accepted or rejected.</li>
 * </ul>
 * The algorithm is as follows (sampling excluded):
 * <ol>
 * <li>I is incremented.</li>
 * <li>Listeners of I are implicitly updated.</li>
 * <li>L is used to select a (possibly singleton) set Pa1,...,Paq so that the state parameters
 *     Sb1,...,Sbr perturbed by these only appear in one Paj.</li>
 * <li>The dependencies Dc1,...,Dcs induced by (and including) Sb1,...,Sbr are asked to cache.</li>
 * <li>Sb1,...,Sbr are perturbed by Pa1,...,Paq, and the proposal densities from/to the new state are noted.</li>
 * <li>Dc1,...,Dcs are asked to update in topological order.</li>
 * <li>The likelihood of the proposed state is collected from M1,...,Mn.</li>
 * <li>A is used to decide whether to accept or reject the new state:</li>
 * <li>
 *   <ul>
 *     <li>Accepted: Dc1,...,Dcs are asked to clear their cache. The current likelihood is updated.</li>
 *     <li>Rejected: Dc1,...,Dcs are asked to restore their cache.</li>
 *   </ul>
 * </li>
 * <li>Go to 1 or finish.</li>
 * </ol>
 * 
 * @author Joel Sjöstrand.
 */
public class MCMCManager {
	
	// TODO: Implement.

}
