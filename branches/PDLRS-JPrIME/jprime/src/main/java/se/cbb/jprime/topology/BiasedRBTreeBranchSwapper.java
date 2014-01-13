package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import  java.lang.Math;

import se.cbb.jprime.math.LogDouble;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.MetropolisHastingsProposal;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Branch-swapper based on <code>RBTreeBranchSwapper</code> that biases guest trees more towards the host
 * tree. It does so by computing parsimony reconciliation scores.
 * Experimental: Could potentially bias the posterior too, but hopefully it's not that bad.
 * This class should improve mixing on large guest trees by avoiding proposing poor topologies too often.
 * 
 * @author Joel Sjöstrand.
 * @author Nicolas Girault.
 */
public class BiasedRBTreeBranchSwapper extends RBTreeBranchSwapper {

	/** Maximum number of attempts before the suggested tree is selected for the proposed state. */
	public static final int MAX_ATTEMPTS = 20;
	
	/** Maximum parsimony score that is kept tracked of. */
	public static final int MAX_TRACKED_PARSIMONY_SCORE = 100;
	
	/** 2-D Gaussian kernel used for smoothing. */
	public static final double[][] GAUSSIAN_5_KERNEL = new double[][] {
			{1 / 273.0,  4 / 273.0,  7 / 273.0,  4 / 273.0, 1 / 273.0},
			{4 / 273.0, 16 / 273.0, 26 / 273.0, 16 / 273.0, 4 / 273.0},
			{7 / 273.0, 26 / 273.0, 41 / 273.0, 26 / 273.0, 7 / 273.0},
			{4 / 273.0, 16 / 273.0, 26 / 273.0, 16 / 273.0, 4 / 273.0},
			{1 / 273.0,  4 / 273.0,  7 / 273.0,  4 / 273.0, 1 / 273.0}
	};
	
	/** Probability that we'll carry out a biased move. */
	private double biasProb;
	
	/** MPR map. */
	private MPRMap mprMap;
	
	/** Parsimony weight for duplications. This should be in the order of 1. */
	private double dupWeight;
	
	/** Parsimony weight for losses. This should be in the order of 1. */
	private double lossWeight;
	
	/**
	 * Matrix: for element (i,j), holds the two values [accepted transitions, total transitions]
	 * for the transitions from trees with score i to trees with score j.
	 */
	private int[][][] acceptanceMatrix;
	
	/** Current parsimony score. */
	private int oldScore = Integer.MAX_VALUE;
	
	/** Proposed tree's parsimony score. */
	private int newScore = Integer.MAX_VALUE;
	
	/** Pseudo-style odds for increasing safety. */
	private double pseudoOdds = 0.05;
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param times times of T. May be null.
	 * @param prng pseudo-random number generator.
	 * @param mprMap sigma map.
	 * @param dupWeight duplication parsimony weight. This should be in the order of 1.
	 * @param lossWeight loss parsimony weight. This should be in the order of 1.
	 * @param noBiasProb probability of carrying out a biased move. Enables
	 * controlling proportion of biased-unbiased moves.
	 */
	public BiasedRBTreeBranchSwapper(RBTree T, DoubleMap lengths, TimesMap times, PRNG prng,
			MPRMap mprMap, double dupWeight, double lossWeight, double biasProb) {
		super(T, lengths, times, prng);
		if (this.biasProb < 0.0 || this.biasProb > 1.0) {
			throw new IllegalArgumentException("Invalid proportion of biased branch-swap moves.");
		}
		this.biasProb = biasProb;
		this.mprMap = mprMap;
		this.dupWeight = dupWeight;
		this.lossWeight = lossWeight;
		this.acceptanceMatrix = new int[MAX_TRACKED_PARSIMONY_SCORE][][];
		for (int i = 0; i < MAX_TRACKED_PARSIMONY_SCORE; ++i) {
			this.acceptanceMatrix[i] = new int[MAX_TRACKED_PARSIMONY_SCORE][];
		}
	}	
	
	/**
	 * Constructor for pseudogenized model.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param times times of T. May be null.
	 * @param prng pseudo-random number generator.
	 * @param mprMap sigma map.
	 * @param dupWeight duplication parsimony weight. This should be in the order of 1.
	 * @param lossWeight loss parsimony weight. This should be in the order of 1.
	 * @param noBiasProb probability of carrying out a biased move. Enables
	 * controlling proportion of biased-unbiased moves.
	 */
	public BiasedRBTreeBranchSwapper(RBTree T, DoubleMap lengths, TimesMap times, PRNG prng,
			MPRMap mprMap, double dupWeight, double lossWeight, double biasProb, DoubleMap pgSwitchs, IntMap edgeModel,  LinkedHashMap<String, Integer> pgMap, NamesMap gNames) {
		super(T, lengths, times, prng, pgSwitchs, edgeModel, pgMap, gNames);
		if (this.biasProb < 0.0 || this.biasProb > 1.0) {
			throw new IllegalArgumentException("Invalid proportion of biased branch-swap moves.");
		}
		this.biasProb = biasProb;
		this.mprMap = mprMap;
		this.dupWeight = dupWeight;
		this.lossWeight = lossWeight;

		this.acceptanceMatrix = new int[MAX_TRACKED_PARSIMONY_SCORE][][];
		for (int i = 0; i < MAX_TRACKED_PARSIMONY_SCORE; ++i) {
			this.acceptanceMatrix[i] = new int[MAX_TRACKED_PARSIMONY_SCORE][];
		}
	}
	
	
	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
		// The higher the score is, the less parsimonious the tree is.
		double dupScore = this.mprMap.getTotalNoOfDuplications() * this.dupWeight;
		double lossScore = this.mprMap.getTotalNoOfLosses() * this.lossWeight;
		this.oldScore = (int) Math.round(dupScore + lossScore);
		
		double oldToNew;
		int attempts = 0;
		if (this.prng.nextDouble() > this.biasProb) {
			// UNBIASED MOVE.
			this.perturbTree();
			oldToNew = getEmpiricalOdds(this.oldScore, this.newScore);
		} else {
			// BIASED MOVE.
			
			// Loop until a sufficiently good tree is OK as our proposal.
			while (true) {
				if (attempts == MAX_ATTEMPTS) {
					// Just go with any tree.
					this.perturbTree();
					oldToNew = getEmpiricalOdds(this.oldScore, this.newScore);
					break;
				}
				
				// Perturb.
				this.perturbTree();
				oldToNew = this.getEmpiricalOdds(oldScore, newScore);
				attempts++;
				
				// Evaluate.
				if (oldToNew > this.prng.nextDouble()) {
					// We have our new state!
					break;
				} else {
					// Not settled on the new state yet; keep on trying, but with original values as starting point...
					this.T.restoreCache();
					if (this.times != null) {
						this.times.restoreCache();
					}
					if (this.lengths != null) {
						this.lengths.restoreCache();
					}
				}
			}
		}
				
		// Note changes. Just say that all sub-parameters have changed.
		ArrayList<StateParameter> affected = new ArrayList<StateParameter>(3);
		changeInfos.put(this.T, new ChangeInfo(this.T));
		int no = this.T.getNoOfSubParameters();
		affected.add(this.T);
		if (this.lengths != null) {
			changeInfos.put(this.lengths, new ChangeInfo(this.lengths));
			affected.add(this.lengths);
			no += this.lengths.getNoOfSubParameters();
		}
		if (this.times != null) {
			changeInfos.put(this.times, new ChangeInfo(this.times));
			affected.add(this.times);
			no += this.getNoOfSubParameters();
		}
		double newToOld = this.getEmpiricalOdds(this.newScore, this.oldScore);
		//System.out.println("Attempts: " + attempts + "\tOld to new: " + this.oldScore + " => " + this.newScore + ": " + Arrays.toString(this.acceptanceMatrix[this.oldScore][this.newScore]) + ", forward: " + oldToNew
		//		+ "\tNew to old: " + this.newScore + " => " + this.oldScore + ": " + Arrays.toString(this.acceptanceMatrix[this.newScore][this.oldScore]) + ", backward: " + newToOld);
		return new MetropolisHastingsProposal(this, new LogDouble(oldToNew), new LogDouble(newToOld), affected, no);
	}
	
	/**
	 * Perturbs the current tree.
	 */
	private void perturbTree() {
		// Cache everything.
		this.T.cache();
		if (this.lengths != null) {
			this.lengths.cache(null);
		}
		if (this.times != null) {
			this.times.cache(null);
		}
		
		// Perturb!
		// First determine move to make.
		double w = this.prng.nextDouble() * (this.operationWeights[0] + this.operationWeights[1] + this.operationWeights[2]);
		RBTree geneTree = new RBTree(this.T);
		int i =0;
		do
		{	
			i++;
			this.T = geneTree;

			if (w < this.operationWeights[0]) {
				this.doNNI();
				this.lastOperationType = "NNI";
			} else if (w < this.operationWeights[0] + this.operationWeights[1]) {
				this.doSPR();
				this.lastOperationType = "SPR";
			} else {
				this.doReroot();
				this.lastOperationType = "Reroot";
			}
		}while((!isALegalConfiguration(this.T.getRoot(), this.T)) && i < MAX_LIMIT);
			
		if (i >= MAX_LIMIT)
		{
			// restore and cache again instead of doing the following:
			this.T.restoreCache();
			this.T.cache();
		}else  //else legalize the edgemodel and edge switches! 
		{
			// Converts all switches below switches to plain pseudogenized edge (does not allow a gene edge below a switch)
			makePseudogenizationConsistant(this.T.getRoot(), this.T);
		}
		
		// Compute score.
		MPRMap mpr = new MPRMap(this.mprMap);
		mpr.forceUpdate();
		double dupScore = mpr.getTotalNoOfDuplications() * this.dupWeight;
		double lossScore = mpr.getTotalNoOfLosses() * this.lossWeight;
		this.newScore = (int) Math.round(dupScore + lossScore);
	}
	
	
	
	/**
	 * Returns empirical odds for the transition from parsimony score v to parsimony score w with respect to recorded data so far.
	 * @param v parsimony from-score.
	 * @param w parsimony to-score.
	 * @return a "suggestion score".
	 */
	private double getEmpiricalOdds(int v, int w) {		
		if (v > MAX_TRACKED_PARSIMONY_SCORE) {
			return 1;
		}
		if (w > MAX_TRACKED_PARSIMONY_SCORE) {
			return 0;
		}
		
		// Get smoothed empirical transition odds.
		double p = 0.0;
		double norm = 0;
		for (int i = v - 2; i <= v + 2; i++) {
			if (i < 0 || i >= MAX_TRACKED_PARSIMONY_SCORE) {
				continue;
			}
			for (int j = w - 2 ; j <= w + 2; j++) {
				if (j < 0 || j >= MAX_TRACKED_PARSIMONY_SCORE) {
					continue;
				}
				int[] accTot = this.acceptanceMatrix[i][j];
				if (accTot == null) {
					// Unseen combo. We disregard it.
				} else {
					double weight = GAUSSIAN_5_KERNEL[i - v + 2][j - w + 2];
					norm += weight;
					p += (weight * accTot[0]) / accTot[1];
				}
			}
		}
		
		if (norm == 0.0) {
			// Completely unknown case.
			return 0.5;
		}
	
		// Smoothed empirical odds of moving from old to new score.
		p /= norm;
		return Math.min(p + this.pseudoOdds, 1.0);
	}
	
	@Override
	public void clearCache() {
		super.clearCache();
		int[] cnt = this.acceptanceMatrix[this.oldScore][this.newScore];
		if (cnt == null) {
			this.acceptanceMatrix[this.oldScore][this.newScore] = new int[] { 1, 1 };
		} else {
			cnt[0]++;
			cnt[1]++;
		}
	}

	@Override
	public void restoreCache() {
		super.restoreCache();
		int[] cnt = this.acceptanceMatrix[this.oldScore][this.newScore];
		if (cnt == null) {
			this.acceptanceMatrix[this.oldScore][this.newScore] = new int[] { 0, 1 };
		} else {
			cnt[1]++;
		}
	}
	
}
