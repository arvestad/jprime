package se.cbb.jprime.mcmc;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import se.cbb.jprime.io.SampleLogDouble;
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
 *     by more than one proposer (but never at the same time). These are accessed via L (see below).</li>
 * <li>a list of models M1,...,Mn which usually correspond to the chain of conditional probabilities
 *     of the overall model.</li>
 * <li>an acyclic digraph (DAG) of dependencies D1,...,Dm of the state parameters. These constitute
 *     of the state parameters S1,...,Sk as sources, the models M1,...,Mn as sinks, and possibly 
 *     "proper dependents" (e.g. cached data structures) in between.</li>
 * <li>a list of "sampleable" objects to sample from, C1,...,Cv. These are usually comprised
 *     of the state parameters S1,...,Sk  and the models M1,...,Mn.</li>
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
 * <li>Pa1,...,Paq are asked to cache the parts of Sb1,...,Sbr they will affect, and carry out the perturbations.
 *     Proposal densities from/to the new state are noted</li>
 * <li>The dependencies Dc1,...,Dcs relying on Sb1,...,Sbr are asked to cache and update in topological order.
 *     This is recursively repeated until all dependencies up to and including the models are up-to-date.</li>
 * <li>The likelihood of the proposed state is collected from M1,...,Mn.</li>
 * <li>A is used to decide whether to accept or reject the new state:</li>
 * <li>
 *   <ul>
 *     <li>Accepted: Pa1,...,Paq and induced dependencies are asked to clear their cache.
 *         The current likelihood is updated.</li>
 *     <li>Rejected: Pa1,...,Paq and induced dependencies are asked to restore their cache.</li>
 *   </ul>
 * </li>
 * <li>Go to 1 or finish.</li>
 * </ol>
 * 
 * @author Joel Sjöstrand.
 */
public class MCMCManager implements Sampleable, InfoProvider {
	
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
	
	/** State parameters. */
	protected ArrayList<StateParameter> parameters;
	
	/** Proper dependents (in topological order after sorting). */
	protected ArrayList<ProperDependent> properDependents;
	
	/** The models of the overall model. */
	protected ArrayList<Model> models;
	
	/** The fields included in each sampling-tuple. */
	protected ArrayList<Sampleable> sampleables;
	
	/** Current overall likelihood. */
	protected LogDouble likelihood;
	
	/** Best seen overall likelihood so far. */
	protected LogDouble bestLikelihood;
	
	/** Best seen state so far. */
	protected String bestState;
	
	/** Time at iteration start in ns. */
	protected long startTime;
	
	/** Time at iteration end in ns. */
	protected long endTime;
	
	/** Debug flag. */
	protected boolean doDebug = false;
	
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
		this.parameters = new ArrayList<StateParameter>(16);
		this.properDependents = new ArrayList<ProperDependent>(16);
		this.models = new ArrayList<Model>(16);
		this.sampleables = new ArrayList<Sampleable>(16);
		this.likelihood = null;
		this.bestLikelihood = null;
		this.bestState = null;
		this.startTime = -1;
		this.endTime = -1;
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
		if (dependent instanceof StateParameter) {
			// State parameter, i.e. DAG source.
			if (!this.parameters.contains(dependent)) {
				StateParameter sp = (StateParameter) dependent;
				this.parameters.add(sp);
			}
		} else {
			// Proper dependent.
			ProperDependent pd = (ProperDependent) dependent;
			if (!this.properDependents.contains(pd)) {
				this.properDependents.add(pd);
				for (Dependent par : pd.getParentDependents()) {
					this.addDependent(par);
				}
			}
		}
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
				
		// Compute topological sorting using Tarjan's BFS technique.
		HashSet<Dependent> visited = new HashSet<Dependent>(this.parameters.size() + this.properDependents.size());
		ArrayList<Dependent> sorted = new ArrayList<Dependent>(visited.size());
		for (Model model : this.models) {
			this.visit(model, visited, sorted);
		}
		
		// Store the proper dependents, now sorted.
		this.properDependents.clear();
		for (Dependent dep : sorted) {
			if (dep instanceof ProperDependent) {
				this.properDependents.add((ProperDependent) dep);
			}
		}
	}
	
	/**
	 * Recursive helper for computing topological ordering of dependencies according to Tarjan's algorithm (1976).
	 * @param dependent currently visited dependent.
	 * @param visited set of visited dependents.
	 * @param sorted topological ordering of dependents, from sources to sinks.
	 */
	private void visit(Dependent dependent, HashSet<Dependent> visited, ArrayList<Dependent> sorted) {
		if (!visited.contains(dependent)) {
			visited.add(dependent);
			if (dependent instanceof ProperDependent) {
				ProperDependent pd = (ProperDependent) dependent;
				for (Dependent parent : pd.getParentDependents()) {
					this.visit(parent, visited, sorted);
				}
			}
        	sorted.add(dependent);
		}
	}
	
	/**
	 * Turns on/off debugging output information.
	 * @param isOn true to enable debug mode; false to disable.
	 */
	public void setDebugMode(boolean isOn) {
		this.doDebug = isOn;
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
		this.likelihood = new LogDouble(1.0);
		boolean willSample = this.thinner.doSample();
		for (Model m : this.models) {
			this.likelihood.mult(m.getLikelihood());
		}
		if (willSample) {
			this.sampler.writeSample(this.sampleables);
		}
		this.bestLikelihood = this.likelihood;
		this.bestState = this.sampler.getSample(this.sampleables);
		
		// Iterate.
		this.startTime = System.nanoTime();
		HashMap<Dependent, ChangeInfo> changeInfos = new HashMap<Dependent, ChangeInfo>(16);
		ArrayList<Proposal> proposals = new ArrayList<Proposal>(16);
		while (this.iteration.increment()) {
			
			// Clear lists.
			changeInfos.clear();
			proposals.clear();
			
			// Query whether this is a sample iteration or not.
			willSample = this.thinner.doSample();
			
			// Get proposer(s) to use.
			Set<Proposer> shakeItBaby = this.proposerSelector.getDisjointProposers();
			
			// Debug info.
			if (this.doDebug) {
				StringBuilder dbg = new StringBuilder(512);
				dbg.append("# Iteration: ").append(this.iteration.getIteration()).append(", Log-likelihood: ").append(this.likelihood.toString())
					.append(", about to use: ");
				for (Proposer p : shakeItBaby) {
					dbg.append(p.toString()).append(", ");
				}
				this.sampler.writeString(dbg.toString());
			}
			
			// Perturb state parameters.
			for (Proposer proposer : shakeItBaby) {
				Proposal proposal = proposer.cacheAndPerturb(changeInfos);
				proposals.add(proposal);
			}
			
			// Update in topological order, but only if deemed necessary.
			for (ProperDependent dep : this.properDependents) {
				for (Dependent parent : dep.getParentDependents()) {
					if (changeInfos.get(parent) != null) {
						dep.cacheAndUpdate(changeInfos, willSample);
						break;
					}
				}
			}
			
			// Get likelihood of proposed state.
			LogDouble newLikelihood = new LogDouble(1.0);
			for (Model m : this.models) {
				newLikelihood.mult(m.getLikelihood());
			}
			
			// Finally, decide whether to accept or reject.
			boolean doAccept = this.proposalAcceptor.acceptProposedState(newLikelihood, this.likelihood, proposals);
			
			// Debug info.
			if (this.doDebug) {
				this.sampler.writeString(doAccept ? " ...state accepted," : " ...state rejected,");
			}
			
			// Update accordingly.
			if (doAccept) {
				for (Proposer proposer : shakeItBaby) {
					proposer.clearCache();
				}
				for (ProperDependent dep : this.properDependents) {
					if (changeInfos.get(dep) != null) {
						dep.clearCache(willSample);
					}
				}
				this.likelihood = newLikelihood;
				if (this.bestLikelihood.lessThan(newLikelihood)) {
					this.bestLikelihood = newLikelihood;
					this.bestState = this.sampler.getSample(this.sampleables);
				}
			} else {
				for (Proposer proposer : shakeItBaby) {
					proposer.restoreCache();
				}
				for (ProperDependent dep: this.properDependents) {
					if (changeInfos.get(dep) != null) {
						dep.restoreCache(willSample);
					}
				}
			}
			
			// Debug info.
			if (this.doDebug) {
				this.sampler.writeString(doAccept ? " ...cached state deleted.\n" : " ...cached state reinstated.\n");
			}
			
			// Sample, if desired.
			if (willSample) {
				this.sampler.writeSample(this.sampleables);
			}
		}
		
		// Post-run stuff.
		this.endTime = System.nanoTime();
	}

	@Override
	public Class<?> getSampleType() {
		return SampleLogDouble.class;
	}

	@Override
	public String getSampleHeader() {
		return "OverallLikelihood";
	}

	@Override
	public String getSampleValue() {
		return this.likelihood.toString();
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(65536);
		sb.append(prefix).append("MCMC MANAGER\n");
		prefix += '\t';
		sb.append(this.prng.getPreInfo(prefix));
		sb.append(this.iteration.getPreInfo(prefix));
		sb.append(this.thinner.getPreInfo(prefix));
		sb.append(this.proposerSelector.getPreInfo(prefix));
		sb.append(this.proposalAcceptor.getPreInfo(prefix));
		for (Model mod : this.models) {
			sb.append(mod.getPreInfo(prefix));
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(65536);
		sb.append(prefix).append("MCMC MANAGER\n");
		long ns = this.endTime - this.startTime;
		double s = (double) ns / 1000000000.0;
		double h = s / 360.0;
		DecimalFormat df = new DecimalFormat("#.##");
		sb.append(prefix).append("Wall time: ")
			.append(ns).append(" ns = ")
			.append(df.format(s)).append(" s = ")
			.append(df.format(h)).append(" min\n");
		sb.append(prefix).append("Best encountered state:\n")
			.append("\t\t").append(this.sampler.getSampleHeader(this.sampleables)).append('\n')
			.append("\t\t").append(this.bestState).append("\n");
		prefix += '\t';
		sb.append(this.prng.getPostInfo(prefix));
		sb.append(this.iteration.getPostInfo(prefix));
		sb.append(this.thinner.getPostInfo(prefix));
		sb.append(this.proposerSelector.getPostInfo(prefix));
		sb.append(this.proposalAcceptor.getPostInfo(prefix));
		for (Model mod : this.models) {
			sb.append(mod.getPostInfo(prefix));
		}
		return sb.toString();
	}
}
