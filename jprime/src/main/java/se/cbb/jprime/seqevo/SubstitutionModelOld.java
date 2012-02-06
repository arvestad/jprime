//package se.cbb.jprime.seqevo;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map.Entry;
//
//import org.ejml.data.DenseMatrix64F;
//import org.ejml.ops.CommonOps;
//
//import se.cbb.jprime.math.LogDouble;
//import se.cbb.jprime.misc.ArrayAlgorithms;
//import se.cbb.jprime.topology.DoubleMap;
//import se.cbb.jprime.topology.GenericMap;
//import se.cbb.jprime.topology.RBTree;
//
///**
// * Implements the standard (probabilistic) Markov model for
// * the substitution process of sequence evolution for any type 
// * of aligned sequence data, see e.g., Felsenstein 1981.
// * <p/>
// * Data are divided into user-defined partitions (domains, codon
// * positions, etc.). Within each partition, sequence positions with
// * identical patterns of states over leaves are identified and counted.
// * Rate variation across sites over discrete classes, e.g., Yang 1993,
// * can be modelled.
// * 
// * @author Bengt Sennblad.
// * @author Lars Arvestad.
// * @author Joel Sjöstrand.
// */
//public class SubstitutionModelOld {
//
//	/** Sequence data (MSA or MLSA). */
//	private SequenceData D;
//	
//	/** Tree. */
//	private RBTree T;
//	
//	/** Rate variation across sites. */
//	private SiteRateHandler siteRates;
//	
//	/** Transition rate matrix. */
//	private MatrixTransitionHandler Q;
//
//    /** Branch lengths, i.e., Markov model "time". */
//    private DoubleMap edgeWeights;
//    
//    /**  */
//    private LogDouble like;
//	
//    /**
//     * Each ultimate element of likes is a RateVec pointer corresponding
//     * to a pattern in 'partitions' (the sorted data matrix) and a specific
//     * vertex in the tree. The pointers points in a surjective manner into
//     * hiddenLikes, where the actual RateLikes are saved. This means that
//     * if several patterns has the same subpattern for a subtree, the
//     * hiddenLike for this subpPattern only has to be caluculated once.
//     * likes provides an interface to hiddenLikes for the parental vertex.
//     */
//    private GenericMap<PatternLike> likes;
//
//    /**  */
//    DenseMatrix64F tmp;
//    
//    /**  */
//    DenseMatrix64F tmpCurrent;
//	
//
//    //    using __gnu_cxx::hash_map;
//    // Structure to save likelihoods in
//    //----------------------------------------------------------------------
//    //! Stores pointers into a HiddenPatternLike, which stores the 
//    //! subtree likelihoodstree probabilities, for each rate class. 
//    //! Each element pointed to by  RateLike corresponds to a column-specific
//    //! rate hypotheses, r (cf. SiteRateHandler).
//    //! Each elements of a LA_Vector corresponds to a state, i, in the 
//    //! Markov substitution model, and stores the probability of the
//    //! Markov process starting with i at the root of a (sub)tree of 
//    //! interest and given r, produces the state pattern
//    //! corresponding of that position at the leaves of the subtree. 
//    
//	
//    /**
//     * 
//     * @param data
//     * @param T
//     * @param siteRates
//     * @param Q
//     * @param edgeWeights
//     */
//    public SubstitutionModelOld(SequenceData data, RBTree T, SiteRateHandler siteRates, MatrixTransitionHandler Q, DoubleMap edgeWeights) {
//    	this.D = data;
//    	this.T = T;
//    	this.siteRates = siteRates;
//    	this.Q = Q;
//    	this.edgeWeights = edgeWeights;
//    	this.D.updatePatterns();
//    	this.likes = (T_in);  //TODO
//    	this.tmp = new DenseMatrix64F(Q.getAlphabetSize(), 1);
//    	this.tmpCurrent = new DenseMatrix64F(Q.getAlphabetSize(), 1);
//    	this.init();
//    }
//	
//    
//    // Access to Tree T. Note that a const reference is returned. The 
//    // logic is that non-const access to T should be explicit in main()
//    //-------------------------------------------------------------------
////     Tree getTree();
//    
//    
//    // Likelihood calculation - interface to MCMCModels
//    //! Calculates the probability of the data given the tree.
//    //! \f$ Pr[D|T, w, Q, \alpha, ncat]\f$, where \f$ w\f$ is the edgeweights 
//    //! of T and \alpha is the parameter of SiteRateHandler and ncat is the 
//    //! number of rate classes in SiteRateHandler. If D is partitioned, 
//    //! different parameter values may be used for different partitions.
//    // This is calculated recursively.
//    // TODO: We probably need to add a calculateDataProbability(Node n),
//    // for the sake of a common interface.  This is used in
//    // cachedSubstitutionModel, but here would just relay to
//    //calculateDataProbability().
//    //------------------------------------------------------------------
//    
//    
//    // Likelihood calculation - interface to MCMCModels
//    //----------------------------------------------------------------------
//    LogDouble calculateDataProbability() {
//    	// TODO: Check if this really should be an assert. /bens
//    	assert this.T.isLeaf(this.T.getRoot()) == false;
//
//    	int left = this.T.getLeftChild(this.T.getRoot());
//    	int right = this.T.getRightChild(this.T.getRoot());
//
//    	// Reset return value
//    	this.like = new LogDouble(1.0);
//    	
//  
//    	// First recurse down to initiate likes
//		if (this.T.perturbedTree() == true) {
//			ArrayList<Integer> leaves = new ArrayList<Integer>(this.T.getNoOfLeaves());
//			this.initLikelihood(left, leaves);
//			leaves.clear();
//			this.initLikelihood(right, leaves);
//		} else {
//			this.recursiveLikelihood(left);
//			this.recursiveLikelihood(right);
//		}
//		this.like.mult(this.rootLikelihood());
// 
//    	this.T.perturbedTree(false);
//    	return this.like;
//    }
//
//    // Partial Likelihood caluclation - for use with perturbedNode
//    LogDouble calculateDataProbability(int n) {
//    	
//    	// reset return value
//    	this.like = new LogDouble(1.0);
//    	
//		// Perturbations in some classes (e.g., ReconciliationTimeMCMC) may
//		// affect the outgoing edges of n, therefore we start by descending
//		// one step
//		if (!this.T.isLeaf(n)) {
//			this.updateLikelihood(this.T.getLeftChild(n));
//			this.updateLikelihood(this.T.getRightChild(n));
//		}
//		// Then we just ascend up to root.
//		while (!this.T.isRoot(n)) {
//			this.updateLikelihood(n);
//			n = this.T.getParent(n);
//			assert n != RBTree.NULL;    // Check for sanity.
//		}
//		this.like.mult(this.rootLikelihood(i));
//    	
//    	return this.like;
//    }
//
////	  //------------------------------------------------------------------
////	  //
////	  // I/O
////	  // 
////	  //------------------------------------------------------------------
////	  // TODO: Shape up these print functions /bens
////	//   String
////	//   print()
////	//   {
//////	     return print(false);
////	//   }
////
////	  String
////	  print(boolean estimateRates)
////	  {
////	    return "FastCacheSubstitutionModel: " + SubstitutionModel::print(estimateRates);
////	  }
////
////	//   String
////	//   print(boolean estimateRates)
////	//   {
//////	     oStringstream oss;
//////	     oss
//////	       << "Substitution likelihood is performed"
//////	       // Here we should maybe only give the part of data that we are using,
//////	       // i.e., the partitions used?
//////	       << " using sequence data:\n"
//////	       // The funciton below should be replaced by D.print(partition)
//////	       // that should write a "name" for the Sequence data and the 
//////	       // user-definedpartition that is used (e.g., either exactly which 
//////	       // positions are defined or the "name" used for the partition
//////	       << indentString(D.print(), "  ")
//////	       << indentString("partitions, any user-defined partitions of the data\n")
//////	       << "and substitution matrix:\n"
//////	       << indentString(Q.print());
//////	     return oss.str();
////	//   }
////
////
////
//    // Initiates all data structures for storing likelihood.
//    public void init() {
//    	pair pl = make_pair(PatternTranslator(i.size()), SubPatternLike());
//    	
//    	// Fill likes with a copy of pl for each node
//    	this.likes = new GenericMap<PatternLike>(T, pl); 
//
//    	T.perturbedTree(true);
//
//    	//! \todo{ We should maybe only init the PatterTranslator here and 
//    	//!        don't do the probability computation/bens}
//    	calculateDataProbability();
//    }
//
//    
//    /**
//     * Helper functions for likelihood calculation.
//     * Recurses down to leaves and then updates likelihoods for each vertex on 
//     * the way up.
//     * \f$ \{ Pr[D_{i,\cdot}|T_n, w, r, p], r\in siteRates, p \in partitions[partition]\}\f$
//     * @return
//     */
//    private LogDouble rootLikelihood() {
//    	int n = this.T.getRoot();
//    	
//    	if (this.T.isLeaf(n)) {
//    		return new LogDouble(1.0);
//    	} else {
//    		// Initiate return value.
//        	LogDouble returnLike = new LogDouble(1.0);
//    		
//    		// Set up data and likelihood storage.
//        	LinkedHashMap<String, int[]> patterns = this.D.getPatterns();
//
//    		// Get nested likelihoods
//    		PatternLike pl_l = this.likes.get(this.T.getLeftChild(n));
//    		PatternLike pl_r = this.likes.get(this.T.getRightChild(n));
//    		
//    		// Number of site rate categories.
//    		int nCat = this.siteRates.nCat();
//    		
//    		// Lastly, loop over all unique patterns.
//    		int i = 0;
//    		for (Entry<String, int[]>  pattern : patterns.entrySet()) {
//    			LogDouble patternLike = new LogDouble(0.0);
//
//    			// Compute Pr[Dk | T, ew, r(j)] for each site rate category j.
//    			for (int j = 0; j < nCat; j++) {
//    				// Now, this looks weird, but pl_l.first is the 
//    				// PatternTranslator, while pl_L.second holds the 
//    				// RateLike in its second element. 
//    				DenseMatrix64F left = pl_l.second[ pl_l.first[i] ].second[j];
//    				DenseMatrix64F right = pl_r.second[ pl_r.first[i] ].second[j];
//
//    				// Element-wise multiplication tmp = left .* right.
//    				CommonOps.elementMult(left, right, this.tmp);
//    				
//    				// current = Pi * tmp.
//    				this.Q.multiplyWithPi(this.tmp, this.tmpCurrent);
//    				patternLike.add(CommonOps.elementSum(this.tmpCurrent));
//    			} 
//    			
//    			// # of occurrences of pattern.
//    			int exp = pattern.getValue()[1];
//    			
//    			// Multiply end result with this patterns likelihood.
//    			// Note that Pr[rateCat]=1/nCat.
//    			returnLike.mult(patternLike.div((double)nCat).pow(exp));
//    			++i;
//    		}
//    		return returnLike;
//    	}
//    }
//
//    /**
//     * This function must be improved and speeded up!
//     * @param n
//     * @param leaves
//     * @return
//     */
//    private List<Integer> initLikelihood(int n, ArrayList<Integer> leaves) {
//    	if (!this.T.isLeaf(n)) {
//    		// Process kids first.
//    		this.initLikelihood(this.T.getLeftChild(n), leaves);
//    		this.initLikelihood(this.T.getRightChild(n), leaves);
//    	} else {
//    		leaves.add(n);
//    	}
//    	
//    	LinkedHashMap<String, int[]> pv = this.D.getPatterns();
//    	PatternTranslator pt = this.likes.get(n).first;
//    	SubPatternLike sl = this.likes.get(n).second;
//    	sl.clear();  
//
//    	int ncat = this.siteRates.nCat();
//    	
//    	// Create templ.
//    	RateLike templ = new RateLike(ncat, this.Q.getAlphabetSize());
//    	
//    	HashMap<String, Integer> tmp_map;
//    	int sli = 0;    // Index for sl's next element
//    	
//    	int[] seqIndices = this.getSequenceIndices(leaves);
//    	
//    	//  Iterate over tree patterns.
//    	for (int i = 0; i < pv.size(); i++) {
//
//    		// Get pattern (can this be done in a neater way?)
//    		char[] subpat = new char[leaves.size()];
//    		for (int ci = 0; ci < leaves.size(); ++ci) {
//    			subpat[ci] = this.D.get(seqIndices[ci], pv[i].first); // = D[gene, position]
//    		}
//    		String subPattern = oss.str();
//
//    		// Check if this pattern already has been calculated	
//    		HashMap<String, Integer>::const_iterator hi = tmp_map.find(subPattern);
//    		if (hi == tmp_map.end()) {
//    			// If so, first create a corresponding RateLike...
//    			sl.push_back(make_pair(i, templ));
//
//    			// ...then record what pattern it refers to
//    			pt[i] = sli;
//    			tmp_map[subPattern] = sli;
//    			sli++; // Lastly, update sl's index
//    		} else {
//    			pt[i] = hi.second;
//    		}
//    	}
//    	this.updateLikelihood(n);
//    	return leaves;
//    }
//    
//    
//    private void recursiveLikelihood(int n) {
//    	if (!this.T.isLeaf(n)) {
//    		this.recursiveLikelihood(this.T.getLeftChild(n));
//    		this.recursiveLikelihood(this.T.getRightChild(n));
//    	}
//    	this.updateLikelihood(n);
//    }
//
//    
//    private void updateLikelihood(int n) {
//    	if (this.T.isLeaf(n)) {
//    		this.leafLikelihood(n);
//    	} else {
//    		// Get likelihood storage.
//    		SubPatternLike sl = this.likes.get(n).second;
//    		
//    		// Get nested likelihoods.
//    		PatternLike pl_l = this.likes.get(this.T.getLeftChild(n));
//    		PatternLike pl_r = this.likes.get(this.T.getRightChild(n));
//
//    		// Compute Pr[Dk | T, ew, r(j)] for each site rate category j.
//    		for (int j = 0; j < this.siteRates.nCat(); j++) {
//    			
//    			// Set up (j-specific) P matrix.
//    			double w = this.edgeWeights.get(n) * this.siteRates.getRate(j);
//    			this.Q.updateTransitionMatrix(w);
//
//    			// Lastly, loop over all unique patterns in hl.
//    			for (SubPatternLike::iterator si = sl.begin(); si != sl.end(); si++) {
//    				int i = si.first;
//    				// Now, this looks weird, but pl_l.first is the 
//    				// PatternTranslator, while pl_L.second holds the 
//    				// RateLike in its second element. 
//    				DenseMatrix64F left = pl_l.second.get( pl_l.first.get(i) ).second.get(j);
//    				DenseMatrix64F right = pl_r.second.get( pl_r.first.get(i) ).second.get(j);
//
//    				// Element-wise multiplication, tmp = left .* right.
//    				CommonOps.elementMult(left, right, this.tmp);
//    				this.Q.multiplyWithP(tmp, si.second.get(j));
//    			}
//    		}
//    	}
//    }
//
//    private void leafLikelihood(int n) {
//    	// Set up data and likelihood storage.
//    	LinkedHashMap<String, int[]> pv = this.D.getPatterns();
//    	SubPatternLike sl = this.likes.get(n).second;
//
//    	//Loop over rate categories.
//    	for (int j = 0; j < this.siteRates.nCat(); j++) {
//    		
//    		// Update P with a new "time" w.
//    		double w = this.edgeWeights.getWeight(n) * this.siteRates.getRate(j);
//    		this.Q.updateTransitionMatrix(w);
//
//    		// Lastly, loop over all unique patterns in hl.
//    		for (SubPatternLike::iterator si = sl.begin(); si != sl.end(); si++) {
//    			
//    			// Get position of pattern's first occurrence in partition.
//    			int pos = pv[si.first].first; 
//    			if (!this.Q.col_mult(si.second[j], (D)(n.getName(), pos))) {
//    				this.Q.mult(D.leafLike(n.getName(),pos), si.second[j]);
//    			}
//    		}
//    	}
//    }
//    
//}
