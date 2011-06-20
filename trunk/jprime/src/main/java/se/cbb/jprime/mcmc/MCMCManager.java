package se.cbb.jprime.mcmc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.io.Sampleable;
import se.cbb.jprime.io.Sampler;
import se.cbb.jprime.math.LogDouble;

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
 * <li>an acyclic digraph (DAG) of dependencies D1,...,Dm of the state parameters. These typically constitute
 *     of the state parameters S1,...,Sk as sources, the models M1,...,Mn as sinks, and possibly cached
 *     data structures in between.</li>
 * <li>a list of "sampleable" objects to sample from, C1,...,Cv. These are usually comprised
 *     of the state parameters S1,...,Sk (or some other dependency Di).</li>
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
	
	/** Iteration of MCMC chain. */
	private Iteration iteration;
	
	/** Governs how often samples are taken. */
	private Thinner thinner;
	
	/** Governs which proposer(s) should be selected at each iteration. */
	private ProposerSelector proposerSelector;
	
	/** Governs whether the proposed state be accepted or rejected. */
	private ProposalAcceptor proposalAcceptor;
	
	/** Governs how output is produced. */
	private Sampler sampler;
	
	/** The state parameters. */
	private ArrayList<StateParameter> stateParameters;
	
	/**
	 * The state parameters, "pure" dependents, and models in topological
	 * ordering, from sources to sinks.
	 */
	private ArrayList<Dependent> dependents;
	
	/** The models of the overall model. */
	private ArrayList<Model> models;
	
	/** The proposers which perturb the state parameters. */
	private ArrayList<Proposer> proposers;
	
	/** The fields included in each sampling-tuple. */
	private ArrayList<Sampleable> sampleables;
	
	/**
	 * Constructor.
	 * @param iteration iteration object of the chain.
	 * @param thinner dictates how often samples are drawn.
	 * @param proposerSelector controls which proposers are selected each iteration.
	 * @param proposalAcceptor controls when a proposed state should be accepted or not.
	 * @param sampler handles output of drawn samples.
	 */
	public MCMCManager(Iteration iteration, Thinner thinner, ProposerSelector proposerSelector,
			ProposalAcceptor proposalAcceptor, Sampler sampler) {
		this.iteration = iteration;
		this.thinner = thinner;
		this.proposerSelector = proposerSelector;
		this.proposalAcceptor = proposalAcceptor;
		this.sampler = sampler;
		this.stateParameters = new ArrayList<StateParameter>();
		this.dependents = new ArrayList<Dependent>();
		this.models = new ArrayList<Model>();
		this.proposers = new ArrayList<Proposer>();
		this.sampleables = new ArrayList<Sampleable>();
	}
	
	/**
	 * Adds a state parameter, automatically adding it as a "dependent".
	 * @param stateParameter the parameter
	 * @param addAsSampleable true to automatically include the parameter
	 *        among the "sampleable" fields.
	 */
	public void addStateParameter(StateParameter stateParameter, boolean addAsSampleable) {
		this.stateParameters.add(stateParameter);
		this.dependents.add(stateParameter);
		if (addAsSampleable) {
			this.sampleables.add(stateParameter);
		}
	}
	
	/**
	 * Adds a "dependent" other than a state parameter or model (these are
	 * automatically included).
	 * @param dependent the dependent.
	 */
	public void addDependent(Dependent dependent) {
		this.dependents.add(dependent);
	}
	
	/**
	 * Adds a model, automatically including it as a "dependent".
	 * @param model the model.
	 * @param addAsSampleable true to automatically include the model
	 *        among the "sampleable" fields.
	 */
	public void addModel(Model model, boolean addAsSampleable) {
		this.models.add(model);
		this.dependents.add(model);
		if (addAsSampleable) {
			this.sampleables.add(model);
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
	 * obtaining MCMC samples.
	 * @param sampleable the sampleable field.
	 */
	public void addSampleable(Sampleable sampleable) {
		this.sampleables.add(sampleable);
	}

	/**
	 * Updates the topological ordering of all dependents, i.e., 
	 * state parameters, models and "pure" dependents. This method must
	 * be invoked when all of the above has been added.
	 */
	private void updateDependencyStructure() {
		// TODO: Implement!
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
		
		// First time, do a clean update.
		LogDouble oldLikelihood = new LogDouble(1.0);
		boolean willSample = this.thinner.doSample();
		for (Dependent d : this.dependents) {
			d.update(willSample);
		}
		for (Model m : this.models) {
			oldLikelihood.mult(m.getLikelihood());
		}
		if (willSample) {
			this.sampler.writeSample(this.sampleables);
		}
		
		// Iterate.
		while (this.iteration.increment()) {
			
			// Query whether this is a sample iteration or not.
			willSample = this.thinner.doSample();
			
			// Get proposer(s) and then obtain a topologically ordered set of affected dependents.
			Set<Proposer> props = this.proposerSelector.getDisjointProposers(proposers);
			List<Dependent> affectedDeps = this.getAffectedDependents(props);
			
			// Cache affected dependents.
			for (Dependent d : affectedDeps) {
				d.cache(willSample);
			}
			
			// Perturb affected dependents.
			ArrayList<Proposal> proposals = new ArrayList<Proposal>(props.size());
			for (Proposer p : props) {
				proposals.add(p.propose());
			}
			
			// Update affected dependents.
			for (Dependent d : affectedDeps) {
				d.update(willSample);
			}
			
			// Get likelihood of proposed state.
			LogDouble newLikelihood = new LogDouble(1.0);
			for (Model m : this.models) {
				newLikelihood.mult(m.getLikelihood());
			}
			
			// Finally, decide whether to accept or reject.
			boolean doAccept = this.proposalAcceptor.acceptNewState(newLikelihood, oldLikelihood, proposals);
			if (doAccept) {
				for (Dependent d : affectedDeps) {
					d.clearCache(willSample);
				}
				oldLikelihood = newLikelihood;
			} else {
				for (Dependent d: affectedDeps) {
					d.restoreCache(willSample);
				}
			}
			
			// Sample, if desired.
			if (willSample) {
				this.sampler.writeSample(this.sampleables);
			}
		}
	}
	
	/**
	 * From the set of all dependents, returns the subset which may be affected
	 * by the upcoming state change. The set is returned topologically ordered, and
	 * includes the source parameters.
	 * @param props the proposers to be used for the state change.
	 * @return the dependents which may be affected, in topological order.
	 */
	private List<Dependent> getAffectedDependents(Set<Proposer> props) {
		TreeSet<Dependent> deps = new TreeSet<Dependent>();
		LinkedList<Dependent> q = new LinkedList<Dependent>();
		for (Proposer p : props) {
			q.addAll(p.getParameters());
		}
		while (!q.isEmpty()) {
			Dependent dep = q.pop();
			if (deps.add(dep)) {
				q.addAll(dep.getChildDependents());
			}
		}
		ArrayList<Dependent> affectedDeps = new ArrayList<Dependent>(16);
		for (Dependent d : this.dependents) {
			if (deps.contains(d)) {
				affectedDeps.add(d);
			}
		}
		return affectedDeps;
	}
}
