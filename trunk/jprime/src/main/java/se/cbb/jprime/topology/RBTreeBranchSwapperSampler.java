package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.cbb.jprime.io.NewickRBTreeSamples;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.ProposerStatistics;
import se.cbb.jprime.mcmc.StateParameter;
import se.cbb.jprime.mcmc.TuningParameter;

/**
 * Represents a branch-swapper which is constrained to a certain
 * number of trees (typically obtained as a output from a previous
 * MCMC run.
 * <p/>
 * Topologies with or without branch lengths are currently supported.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTreeBranchSwapperSampler implements Proposer {
	
	/** Trees to sample from. */
	private ArrayList<RBTree> trees;
	
	/** Topology. */
	protected RBTree T;
	
	/** Lengths. Null if not used. */
	protected DoubleMap lengths;
	
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
	 * @param minCvg minimum coverage for a topology to be included among the samples, e.g. 0.01.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, PRNG prng, NewickRBTreeSamples treeSamples, double minCvg) {
		this(T, null, prng, treeSamples, minCvg);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param prng pseudo-random number generator.
	 * @param treeSamples tree samples.
	 * @param minCvg minimum coverage for a topology to be included among the samples, e.g. 0.01.
	 */
	public RBTreeBranchSwapperSampler(RBTree T, DoubleMap lengths, PRNG prng, NewickRBTreeSamples treeSamples, double minCvg) {
		this.T = T;
		this.lengths = lengths;
		this.prng = prng;
		this.isActive = true;
		
		// TODO: Implement.
	}

	@Override
	public String getPreInfo(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPostInfo(String prefix) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearCache() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreCache() {
		// TODO Auto-generated method stub
		
	}
	
}
