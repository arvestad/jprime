package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.IterationListener;
import se.cbb.jprime.mcmc.Proposal;

/**
 * TODO: Document!
 * Note: Only supports branch lengths at the time being.
 * 
 * 
 * @author Joel Sjöstrand.
 */
public class BiasedRBTreeBranchSwapper extends RBTreeBranchSwapper implements IterationListener {

	public static int MAX_NO_OF_LENGTHS_PER_TREE = 20;
	
	/**
	 * Inner container for encountered trees. Stores up to
	 * MAX_NO_OF_LENGTHS_PER_TREE of the last instances with lengths.
	 */
	class EncounteredTree {
		
		String treeWithoutLengths;
		int count;
		LinkedList<String> treesWithLengths;
		
		EncounteredTree(String treeWithoutLengths, String treeWithLengths) {
			this.treeWithoutLengths = treeWithoutLengths;
			this.count = 1;
			if (treeWithLengths != null) {
				this.treesWithLengths = new LinkedList<String>();
				this.treesWithLengths.add(treeWithLengths);
			}
		}
		
		void addSample(String treeWithLengths) {
			this.count++;
			if (treeWithLengths != null) {
				this.treesWithLengths.add(treeWithLengths);
				if (this.treesWithLengths.size() > MAX_NO_OF_LENGTHS_PER_TREE) {
					this.treesWithLengths.pollFirst();
				}
			}
		}
	}
	
	/** Portion of iterations when biased proposals kick in. */
	protected double breakpoint;
	
	/** Governs the coverage of trees included in biased-phase. */
	protected double cutoff;
	
	protected HashMap<String, EncounteredTree> encounteredTrees;
	
	protected ArrayList<EncounteredTree> biasedTrees;
		
	public BiasedRBTreeBranchSwapper(RBTree T, DoubleMap lengths, PRNG prng, double breakpoint, double cutoff) {
		super(T, lengths, prng);
		if (breakpoint < 0.0 || breakpoint > 1.0) {
			throw new IllegalArgumentException("Invalid iteration breakpoint for biased branch-swapper; value must be in [0.0,1.0]");
		}
		if (cutoff < 0.0 || cutoff > 1.0) {
			throw new IllegalArgumentException("Invalid probability cutoff for biased branch-swapper; value must be in [0.0,1.0]");
		}
		this.breakpoint = breakpoint;
		this.cutoff = cutoff;
	}

	@Override
	public void incrementPerformed(int iterCurr, int iterTotal) {
		if (iterCurr / (double) iterTotal >= this.breakpoint) {
			// TODO: Implement! (Sort, fill array, ...)
			this.biasedTrees = new ArrayList<EncounteredTree>();
		}
	}
	
	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
		if (this.biasedTrees == null) {
			// Still not in bias-phase.
			return super.cacheAndPerturb(changeInfos);
		} else {
			// In bias-phase: suggest a biased tree.
			// TODO: Implement!
			throw new UnsupportedOperationException("Not implmented yet!");
		}
	}
	
	@Override
	public void clearCache() {
		super.clearCache();
	}

	@Override
	public void restoreCache() {
		super.restoreCache();
	}

}
