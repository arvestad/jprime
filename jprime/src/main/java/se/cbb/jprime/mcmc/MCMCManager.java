package se.cbb.jprime.mcmc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import se.cbb.jprime.io.Sampleable;
import se.cbb.jprime.io.Sampler;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;

/**
 * MCMC framework class for handling a plain non-hierarchical MCMC chain (MC^2).
 * Maintains the following elements:
 * <ul>
 * <li>a set of state parameters S1,..., Sk.</li>
 * <li>a set of proposers P1,...,Pl, which perturb the state parameters. A single
 *     proposer Pi may perturb more than one of the parameters, and a single parameter Sj may be perturbed
 *     by more than one proposer (but never at the same time).</li>
 * <li>a list of models M1,...,Mn which usually correspond to the chain of conditional probabilities
 *     of the overall model.</li>
 * <li>an acyclic digraph (DAG) of dependencies D1,...,Dm of the state parameters. These constitute
 *     of the state parameters S1,...,Sk as sources, the models M1,...,Mn as sinks, and possibly 
 *     "proper dependents" (e.g. cached data structures) in between.</li>
 * <li>a list of "sampleable" objects to sample from, C1,...,Cv. These are usually comprised
 *     of the state parameters S1,...,Sk (and perhaps more).</li>
 * </ul>
 * Apart from this, the class has:
 * <ul>
 * <li>an iteration object I.</li>
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
 * <li>Sb1,...,Sbr are perturbed by Pa1,...,Paq, any info detailing the perturbations are stored within,
 *     and the proposal densities from/to the new state are noted.</li>
 * <li>Dc1,...,Dcs are asked to update in topological order.</li>
 * <li>The likelihood of the proposed state is collected from M1,...,Mn.</li>
 * <li>A is used to decide whether to accept or reject the new state:</li>
 * <li>
 *   <ul>
 *     <li>Accepted: Dc1,...,Dcs are asked to clear their cache and clear any perturbation info.
 *         The current likelihood is updated.</li>
 *     <li>Rejected: Dc1,...,Dcs are asked to restore their cache and clear any perturbation info.</li>
 *   </ul>
 * </li>
 * <li>Go to 1 or finish.</li>
 * </ol>
 * 
 * @author Joel Sjöstrand.
 */
public class MCMCManager {
	
	/**
	 * Holder of dependent and topological ordering index in the dependency DAG,
	 * from sources to sinks (although the sources==state parameters are actually excluded).
	 */
	class OrderedDependent implements Comparable<OrderedDependent> {
		ProperDependent dependent;
		Integer order;
		
		public OrderedDependent(ProperDependent dependent, int order) {
			this.dependent = dependent;
			this.order = order;
		}

		@Override
		public int compareTo(OrderedDependent o) {
			return this.order.compareTo(o.order);
		}
	}
	
	/** Iteration of MCMC chain. */
	protected Iteration iteration;
	
	/** Governs how often samples are taken. */
	protected Thinner thinner;
	
	/** Governs which proposer(s) should be selected at each iteration. */
	protected ProposerSelector proposerSelector;
	
	/** Governs whether the proposed state be accepted or rejected. */
	protected ProposalAcceptor proposalAcceptor;
	
	/** Governs how output is produced. */
	protected Sampler sampler;
	
	/** Pseudo-random number generator. */
	protected PRNG prng;
	
	/**
	 * Map in which all dependents (state parameters, models, remaining "proper dependents") are
	 * keys, and their respective children are values.
	 */
	protected HashMap<Dependent, ArrayList<OrderedDependent>> dependents;
	
	/** The models of the overall model. */
	protected ArrayList<Model> models;
	
	/** The proposers which perturb the state parameters. */
	protected ArrayList<Proposer> proposers;
	
	/** The fields included in each sampling-tuple. */
	protected ArrayList<Sampleable> sampleables;
	
	/**
	 * Constructor.
	 * @param iteration iteration object of the chain.
	 * @param thinner dictates how often samples are drawn.
	 * @param proposerSelector controls which proposers are selected each iteration.
	 * @param proposalAcceptor controls when a proposed state should be accepted or not.
	 * @param sampler handles output of drawn samples.
	 * @param prng pseudo-random number generator. May be null in certain situations.
	 */
	public MCMCManager(Iteration iteration, Thinner thinner, ProposerSelector proposerSelector,
			ProposalAcceptor proposalAcceptor, Sampler sampler, PRNG prng) {
		this.iteration = iteration;
		this.thinner = thinner;
		this.proposerSelector = proposerSelector;
		this.proposalAcceptor = proposalAcceptor;
		this.sampler = sampler;
		this.prng = prng;
		this.dependents = new HashMap<Dependent, ArrayList<OrderedDependent>>(32);
		this.models = new ArrayList<Model>(8);
		this.proposers = new ArrayList<Proposer>(16);
		this.sampleables = new ArrayList<Sampleable>(16);
	}
	
	/**
	 * Adds a model, and recursively adds all <code>Dependent</code> objects on which it relies, down
	 * to the state parameters.
	 * @param model the model.
	 */
	public void addModel(Model model) {
		this.models.add(model);
		this.addDependent(model);
	}
	
	/**
	 * Recursive helper. Adds a dependent and all of its ancestors.
	 * @param dependent the dependent.
	 */
	private void addDependent(Dependent dependent) {
		if (!this.dependents.containsKey(dependent)) {
			this.dependents.put(dependent, new ArrayList<OrderedDependent>(8));    // Null for the moment.
		}
		Dependent parents[] = dependent.getParentDependents();
		if (parents != null) {
			for (Dependent par : parents) {
				this.addDependent(par);
			}
		}
	}
	
	/**
	 * Adds a proposer acting on one or more of the state parameters.
	 * @param proposer the proposer.
	 */
	public void addProposer(Proposer proposer) {
		this.proposers.add(proposer);
	}
	
	/**
	 * Adds a "sampleable", i.e. a field which will be included when
	 * outputting MCMC samples. Typically, these consist of state parameters and models.
	 * @param sampleable the sampleable field.
	 */
	public void addSampleable(Sampleable sampleable) {
		this.sampleables.add(sampleable);
	}

	/**
	 * Updates the topological ordering of all dependents, i.e., 
	 * state parameters, models and "proper dependents". This method must
	 * be invoked when all dependents have been added.
	 */
	private void updateDependencyStructure() {
		Set<Dependent> all = this.dependents.keySet();
		
		// First, create ordered dependents.
		HashMap<ProperDependent, OrderedDependent> ordered = new HashMap<ProperDependent, OrderedDependent>(this.dependents.size());
		for (Dependent dep : all) {
			if (dep.getParentDependents() != null) {
				ProperDependent pd = (ProperDependent) dep;
				ordered.put(pd, new OrderedDependent(pd, -1));
			}
		}
		
		// We've got arcs from children->parent. Now compute parent->children arcs.
		for (Dependent child : all) {
			Dependent[] parents = child.getParentDependents();
			if (parents != null) {
				for (Dependent parent : parents) {
					this.dependents.get(parent).add(ordered.get(child));
				}
			}
		}
		
		// Compute topological sorting.
		HashSet<Dependent> visited = new HashSet<Dependent>(all.size());
		ArrayList<Dependent> sorted = new ArrayList<Dependent>(all.size());
		for (Model model : this.models) {
			this.visit(model, visited, sorted);
		}
		
		// Store the topological index.
		for (int i = all.size() - ordered.size(); i < sorted.size(); ++i) {
			ordered.get(sorted.get(i)).order = i;
		}
	}
	
	/**
	 * Recursive helper for computing topological ordering of dependencies.
	 * @param dependent dependent.
	 * @param visited set of visited dependents.
	 * @param sorted topological ordering of dependents, from sources to sinks.
	 */
	private void visit(Dependent dependent, HashSet<Dependent> visited, ArrayList<Dependent> sorted) {
		if (!visited.contains(dependent)) {
			visited.add(dependent);
			Dependent[] parents = dependent.getParentDependents();
			if (parents != null) {
				for (Dependent parent : parents) {
					this.visit(parent, visited, sorted);
				}
			}
        	sorted.add(dependent);
		}
	}

	/**
	 * Starts and executes the MCMC chain.
	 * @throws IOException if unable to produce sampling output.
	 */
	public void run() throws IOException {
		// Update the topological ordering of the dependency DAG.
		this.updateDependencyStructure();
		
		// Write sample header.
		this.sampler.writeSampleHeader(this.sampleables);
		
		// First time, assume all objects are up-to-date and compute initial likelihood.
		LogDouble oldLikelihood = new LogDouble(1.0);
		boolean willSample = this.thinner.doSample();
		for (Model m : this.models) {
			oldLikelihood.mult(m.getLikelihood());
		}
		if (willSample) {
			this.sampler.writeSample(this.sampleables);
		}
		
		// Iterate.
		ArrayList<Proposal> proposals = new ArrayList<Proposal>(8);
		PriorityQueue<OrderedDependent> toBeUpdated = new PriorityQueue<OrderedDependent>(16);
		ArrayList<ProperDependent> affected = new ArrayList<ProperDependent>(16);
		while (this.iteration.increment()) {
			
			// Clear lists.
			proposals.clear();
			toBeUpdated.clear();
			affected.clear();
			
			// Query whether this is a sample iteration or not.
			willSample = this.thinner.doSample();
			
			// Get proposer(s) to use.
			Set<Proposer> shakeItBaby = this.proposerSelector.getDisjointProposers(proposers);
			
			// Perturb state parameters.
			for (Proposer proposer : shakeItBaby) {
				Proposal proposal = proposer.cacheAndPerturbAndSetChangeInfo();
				proposals.add(proposal);
				for (StateParameter param : proposal.getPerturbedParameters()) {
					// Register children for pending updates.
					if (param.getChangeInfo() != null) {
						toBeUpdated.addAll(this.dependents.get(param));
					}
				}
			}
			
			// Update in topological order.
			while (!toBeUpdated.isEmpty()) {
				ProperDependent dep = toBeUpdated.poll().dependent;
				dep.cacheAndUpdateAndSetChangeInfo(willSample);
				affected.add(dep);
				if (dep.getChangeInfo() != null) {
					toBeUpdated.addAll(this.dependents.get(dep));
				}
			}
			
			// Get likelihood of proposed state.
			LogDouble newLikelihood = new LogDouble(1.0);
			for (Model m : this.models) {
				newLikelihood.mult(m.getLikelihood());
			}
			
			// Finally, decide whether to accept or reject.
			boolean doAccept = this.proposalAcceptor.acceptProposedState(newLikelihood, oldLikelihood, proposals, this.prng);
			if (doAccept) {
				for (Proposer proposer : shakeItBaby) {
					proposer.clearCacheAndClearChangeInfo();
				}
				for (ProperDependent dep : affected) {
					dep.clearCacheAndClearChangeInfo(willSample);
				}
				oldLikelihood = newLikelihood;
			} else {
				for (Proposer proposer : shakeItBaby) {
					proposer.restoreCacheAndClearChangeInfo();
				}
				for (ProperDependent dep: affected) {
					dep.restoreCacheAndClearChangeInfo(willSample);
				}
			}
			
			// Sample, if desired.
			if (willSample) {
				this.sampler.writeSample(this.sampleables);
			}
		}
	}
}
