package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.MetropolisHastingsProposal;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.ProposerStatistics;
import se.cbb.jprime.mcmc.StateParameter;
import se.cbb.jprime.mcmc.TuningParameter;

/**
 * Represents a branch-swapper which is constrained to a certain
 * number of trees (typically obtained as an output from a previous
 * MCMC run).
 * <p/>
 * Topologies with or without branch lengths are currently supported.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTreeBranchSwapperSampler implements Proposer {
	
	/** Trees to sample from. */
	private NewickRBTreeSamples treeSamples;
	
	/** Topology. */
	protected RBTree T;
	
	/** Lengths. Null if not used. */
	protected DoubleMap lengths;
	
	/** The number of available instances of the current topology. */
	protected int count;
	
	/** Cached number of counts. */
	protected int countCache = -1;
	
	/** If true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence. */
	protected boolean equalTopoChance;
	
	/** List with topology indices among one samples. */
	protected int[] sampleIndices;
	
	/** Statistics. */
	protected ProposerStatistics statistics = null;
	
	/** Pseudo-random number generator. */
	protected PRNG prng;
	
	/** Active flag. */
	protected boolean isActive;
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param prng pseudo-random number generator.
	 * @param treeSamples tree samples.
	 * @param equalTopoChance if true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, PRNG prng, NewickRBTreeSamples treeSamples, boolean equalTopoChance) {
		this(T, null, prng, treeSamples, equalTopoChance);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param prng pseudo-random number generator.
	 * @param treeSamples tree samples.
	 * @param equalTopoChance if true, samples uniformly among unique topologies; if false, samples weighted according to topology prevalence.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, DoubleMap lengths, PRNG prng, NewickRBTreeSamples treeSamples, boolean equalTopoChance) {
		this.T = T;
		this.count = 1;     // Dummy, as low as possible.
		this.lengths = lengths;
		this.treeSamples = treeSamples;
		this.equalTopoChance = equalTopoChance;
		this.prng = prng;
		this.isActive = true;
		
		// Fill a list with indices to choose from.
		if (equalTopoChance) {
			this.sampleIndices = new int[treeSamples.getNoOfTrees()];
			for (int i = 0; i < this.sampleIndices.length; ++i) {
				this.sampleIndices[i] = i;
			}
		} else {
			this.sampleIndices = new int[treeSamples.getTotalTreeCount()];
			int i = 0;
			for (int j = 0; j < treeSamples.getNoOfTrees(); ++j) {
				for (int k = 0; k < this.treeSamples.getTreeCount(j); ++k) {
					this.sampleIndices[i++] = j;
				}
			}
		}
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(prefix).append("BRANCH-SWAPPER-SAMPLER PROPOSER\n");
		sb.append(prefix).append("Perturbed tree parameter: ").append(this.T.getName()).append('\n');
		sb.append(prefix).append("Perturbed lengths parameter: ").append(this.lengths == null ? "None" : this.lengths.getName()).append('\n');
		sb.append(prefix).append("Is active: ").append(this.isActive).append("\n");
		sb.append(prefix).append("No. of unique trees: ").append(this.treeSamples.getNoOfTrees()).append('\n');
		sb.append(prefix).append("Total no. of tree instances: ").append(this.treeSamples.getTotalTreeCount()).append('\n');
		sb.append(prefix).append("Coverage of most prevalent tree: ").append(this.treeSamples.getTreeCount(0) / (double) this.treeSamples.getTotalTreeCount()).append('\n');
		sb.append(prefix).append("Coverage of least prevalent tree: ").append(this.treeSamples.getTreeCount(this.treeSamples.getNoOfTrees() - 1) / (double) this.treeSamples.getTotalTreeCount()).append('\n');
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPreInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("BRANCH-SWAPPER-SAMPLER PROPOSER\n");
		sb.append(prefix).append("Perturbed tree parameter: ").append(this.T.getName()).append('\n');
		sb.append(prefix).append("Perturbed lengths parameter: ").append(this.lengths == null ? "None" : this.lengths.getName()).append('\n');
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPostInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public Set<StateParameter> getParameters() {
		HashSet<StateParameter> ps = new HashSet<StateParameter>();
		ps.add(this.T);
		if (this.lengths != null) { ps.add(this.lengths); }
		return ps;
	}

	@Override
	public int getNoOfParameters() {
		int cnt = 1;
		if (this.lengths != null) { cnt++; }
		return cnt;
	}

	@Override
	public int getNoOfSubParameters() {
		int cnt = this.T.getNoOfSubParameters();
		if (this.lengths != null) { cnt += this.lengths.getNoOfSubParameters(); }
		return cnt;
	}

	@Override
	public void setStatistics(ProposerStatistics stats) {
		this.statistics = stats;
	}

	@Override
	public ProposerStatistics getStatistics() {
		return this.statistics;
	}

	@Override
	public List<TuningParameter> getTuningParameters() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return this.isActive;
	}

	@Override
	public void setEnabled(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
		// Cache everything.
		this.T.cache();
		this.countCache = this.count;
		if (this.lengths != null) {
			this.lengths.cache(null);
		}
		
		// Sample a tree.
		int idx = this.sampleIndices[this.prng.nextInt(this.sampleIndices.length)];
		RBTree sampledTree = this.treeSamples.getTree(idx);
		this.T.setTopology(sampledTree);
		this.count = this.treeSamples.getTreeCount(idx);
		if (this.lengths != null) {
			// Sample a lengths.
			List<DoubleMap> lengthses = this.treeSamples.getTreeBranchLengths(idx);
			DoubleMap sampledLengths = lengthses.get(this.prng.nextInt(lengthses.size()));
			for (int x = 0; x < sampledLengths.getSize(); ++x) {
				this.lengths.set(x, sampledLengths.get(x));
			}
		}
		
		// Note changes.
		ArrayList<StateParameter> affected = new ArrayList<StateParameter>(2);
		changeInfos.put(this.T, new ChangeInfo(this.T));
		int no = this.T.getNoOfSubParameters();
		affected.add(this.T);
		if (this.lengths != null) {
			changeInfos.put(this.lengths, new ChangeInfo(this.lengths));
			affected.add(this.lengths);
			no += this.lengths.getNoOfSubParameters();
		}
		
		// Bias forward-backwards density according to topology prevalence, disregarding branch lengths.
		LogDouble forward, backward;
		if (this.equalTopoChance) {
			forward = new LogDouble(1.0 / this.treeSamples.getNoOfTrees());
			backward = new LogDouble(1.0 / this.treeSamples.getNoOfTrees());
		} else {
			forward = new LogDouble(this.count / (double) this.treeSamples.getTotalTreeCount());
			backward = new LogDouble(this.countCache / (double) this.treeSamples.getTotalTreeCount());
		}
		return new MetropolisHastingsProposal(this, forward, backward, affected, no);
	}

	@Override
	public void clearCache() {
		if (this.statistics != null) {
			this.statistics.increment(true);
		}
		this.T.clearCache();
		if (this.lengths != null) {
			this.lengths.clearCache();
		}
		this.countCache = -1;
	}

	@Override
	public void restoreCache() {
		if (this.statistics != null) {
			this.statistics.increment(false);
		}
		this.T.restoreCache();
		if (this.lengths != null) {
			this.lengths.restoreCache();
		}
		this.count = this.countCache;
		this.countCache = -1;
	}
	
}
