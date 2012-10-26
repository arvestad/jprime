package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.Map;

import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.MetropolisHastingsProposal;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * 
 * @author 
 */
public class BiasedRBTreeBranchSwapper extends RBTreeBranchSwapper {

	/** MPR map. */
	private MPRMap mprMap;
	
	/** Parsimony weight for duplications. */
	private double dupWeight;
	
	/** Parsimony weight for losses. */
	private double lossWeight;
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param times times of T. May be null.
	 * @param prng pseudo-random number generator.
	 */
	public BiasedRBTreeBranchSwapper(RBTree T, DoubleMap lengths, TimesMap times, PRNG prng,
			MPRMap mprMap, double dupWeight, double lossWeight) {
		super(T, lengths, times, prng);
		this.mprMap = mprMap;
		this.dupWeight = dupWeight;
		this.lossWeight = lossWeight;
	}
	
	
	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
		// First determine move to make.
		double w = this.prng.nextDouble() * (this.operationWeights[0] + this.operationWeights[1] + this.operationWeights[2]);
		
		double dupScore = this.mprMap.getTotalNoOfDuplications() * this.dupWeight;
		double lossScore = this.mprMap.getTotalNoOfLosses() * this.lossWeight;
		// The higher the score is, the less parsimonious the tree is
		double oldScore = dupScore + lossScore;
		
		//To see how selective 'treeIsOK' method is
		//int cpt = 0;
		
		// Loop to be selective on the tree we propose
		// according to the score
		while(true) {
			//cpt++;
			// Cache everything.
			this.T.cache();
			if (this.lengths != null) {
				this.lengths.cache(null);
			}
			if (this.times != null) {
				this.times.cache(null);
			}
			
			
			// Perturb!
			//System.out.println("\n" + this.T.getSampleValue());
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
			
			// NOT OK - RESTORE, PERTURB AGAIN.
			// OK, END-OF-LOOP.
			if (treeIsOK(oldScore)){
				//System.out.println(1.0/cpt);
				break;
			}else{
				this.restoreCache();
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
		
		// Right now, we consider forward-backward probabilities as equal.
		return new MetropolisHastingsProposal(this, new LogDouble(1.0), new LogDouble(1.0), affected, no);
	}
	
	private boolean treeIsOK(double oldScore) {
		MPRMap mpr = new MPRMap(this.mprMap);
		mpr.forceUpdate();
		double dupScore = mpr.getTotalNoOfDuplications() * this.dupWeight;
		double lossScore = mpr.getTotalNoOfLosses() * this.lossWeight;
		double score = dupScore + lossScore;
		
		//Probability to choose the new tree
		double pbb = 0;
		
		//Let's see how it behaves with this:
		if(score < oldScore){
			pbb = 1;
		}else{
			pbb = oldScore / score;
		}
		
		//The tree is OK according to the pbb
		if(this.prng.nextDouble() < pbb){
			return true;	
		}else{
			return false;
		}
	}
	
}
