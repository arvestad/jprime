package se.cbb.jprime.seqevo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import se.cbb.jprime.io.SampleLogDouble;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.Model;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GenericMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;

/**
 * Implements the standard (probabilistic) Markov model for
 * the substitution process of sequence evolution for any type 
 * of aligned sequence data, see e.g., Felsenstein 1981.
 * <p/>
 * Sequence positions with identical patterns of states over leaves are identified and counted.
 * Rate variation across sites over discrete classes, e.g., Yang 1993,
 * can be modelled.
 * <p/>
 * This model does not yet support partitions of data into user-defined "independent" loci
 * (domains, codon positions, etc.). NOTE: This class is derived from the C++ class
 * <code>CacheSubstitutionModel</code> and not <code>FastCacheSubstitutionModel</code>
 * since the latter was stated unsuitable for tree topology changes in the C++ CMake
 * default settings.
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class SubstitutionModel implements Model {

	/** Model name. */
	private String name;
	
	/** Sequence data (MSA). */
	private SequenceData D;
	
	/** Rate variation across sites. */
	private SiteRateHandler siteRates;
	
	/** Transition rate matrix Q and, with it, P. */
	private MatrixTransitionHandler Q;
	
	/** Tree. */
	private RBTree T;
	
	/** Leaf names (=sequence names) of T. */
	private NamesMap names;

    /** Branch lengths, i.e., Markov model "time". */
    private DoubleMap branchLengths;
    
    /** Likelihoods for each gene tree vertex. */
    private GenericMap<PatternLikelihoods> likes;
    
    /** Model likelihood. */
    private LogDouble like;
    
    /** Cached likelihood. */
    private LogDouble cacheModelLikelihood = null;

    /** Temporary matrix used during computations. */
    private DenseMatrix64F tmp;
    
    /**
     * Constructor.
     * @param name model name.
     * @param D sequence data (MSA).
     * @param siteRates site rate categories.
     * @param Q data transition matrix Q (and P).
     * @param T tree.
     * @param names leaf names of T.
     * @param branchLengths branch lengths of T.
     */
    public SubstitutionModel(String name, SequenceData D, SiteRateHandler siteRates, MatrixTransitionHandler Q, RBTree T, NamesMap names, DoubleMap branchLengths) {
    	this.name = name;
    	this.D = D;
    	this.siteRates = siteRates;
    	this.Q = Q;
    	this.T = T;
    	this.names = names;
    	this.branchLengths = branchLengths;
    	int noOfVertices = T.getNoOfVertices();
    	int noOfPatterns = D.getPatterns().size();
    	int noOfSiteRates = siteRates.nCat();
    	int alphabetSize = Q.getAlphabetSize();
    	this.likes = new GenericMap<PatternLikelihoods>("SubstitutionModelLikelihoods", noOfVertices);
    	this.like = new LogDouble(0.0);
    	this.tmp = new DenseMatrix64F(alphabetSize, 1);
    	for (int n = 0; n < T.getNoOfVertices(); ++n) {
    		this.likes.set(n, new PatternLikelihoods(noOfPatterns, noOfSiteRates, alphabetSize));
    	}
    }

    @Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
    	ChangeInfo tInfo = changeInfos.get(this.T);
    	ChangeInfo blInfo = changeInfos.get(this.branchLengths);
		ChangeInfo siteRateInfo = changeInfos.get(this.siteRates);
		if (tInfo != null || siteRateInfo != null || (blInfo != null && blInfo.getAffectedElements() == null)) {
			// Full update if tree or site rates have changed, or if undisclosed
			// branch lengths changes.
			
		} else if (blInfo != null && blInfo.getAffectedElements() != null) {
			// Partial update if disclosed branch length changes.
			
			// For the root vertex r, perturbations in some classes may affect both children
			// of r. Therefore we add the sibling.
		}
	}
    

    /**
     * Likelihood calculation - interface to MCMCModels
     */
    private LogDouble calculateDataProbability() {
    	int left = this.T.getLeftChild(this.T.getRoot());
    	int right = this.T.getRightChild(this.T.getRoot());

    	// Reset return value.
    	this.like = new LogDouble(1.0);
    	
		// First recurse down to initiate likelihoods.
		this.recursiveLikelihood(left);
		this.recursiveLikelihood(right);

		this.like.mult(this.rootLikelihood());

		//this.T.perturbedNode(0);
    	return this.like;
    }

    
    /**
     * Partial likelihood calculation - for use with perturbedNode.
     */
	private LogDouble calculateDataProbability(int n) {
		assert (n != RBTree.NULL);
		
		// Reset overall likelihood value.
		this.like = new LogDouble(1.0);
	
		// For the root vertex r, perturbations in some classes may affect both children
		// of r. Therefore we start by descending one step.
		if (!this.T.isLeaf(n)) {
			this.updateLikelihood(this.T.getLeftChild(n));
			this.updateLikelihood(this.T.getRightChild(n));
		}
		
		// Then we just ascend up to root.
		while (!this.T.isRoot(n)) {
			this.updateLikelihood(n);
			n = this.T.getParent(n);
			assert (n != RBTree.NULL);
		}
		this.like.mult(this.rootLikelihood());
		
		assert (this.like.greaterThan(0.0));
		return this.like;
	}

	
	/**
	 * Helper functions for likelihood calculation.
	 * Recurse down to leaves and then updates Likelihoods for each node on
	 * the way up.
	 * \f$ \{ Pr[D_{i,\cdot}|T_n, w, r, p], r\in siteRates, p \in partitions[partition]\}\f$
	 */
	private LogDouble rootLikelihood() {
		int n = this.T.getRoot();
		if (this.T.isLeaf(n)) {
			return new LogDouble(1.0);
		} else {
			// Initiate return value.
			LogDouble returnL = new LogDouble(1.0);
	
			// Set up data and likelihood storage.
			LinkedHashMap<String, int[]> patterns = this.D.getPatterns();
			PatternLikelihoods pl = this.likes.get(n);
			
			// Get nested likelihoods.
			PatternLikelihoods pl_l = this.likes.get(this.T.getLeftChild(n));
			PatternLikelihoods pl_r = this.likes.get(this.T.getRightChild(n));
	
			// Lastly, loop over each unique pattern in patterns.
			int i = 0;
			for (Entry<String, int[]> pattern : patterns.entrySet()) {
				LogDouble patternL = new LogDouble(0.0);
	
				// Compute Pr[Dk | T, ew, r(j)] for each site rate category j.
				for (int j = 0; j < this.siteRates.nCat(); j++) {
					DenseMatrix64F left = pl_l.get(i, j);
					DenseMatrix64F right = pl_r.get(i, j);
					
					// Element-wise multiplication, tmp = left .* right.
					CommonOps.elementMult(left, right, this.tmp);
					
					// Pi * tmp, store result in current.
					DenseMatrix64F curr = pl.get(i, j);
					this.Q.multiplyWithPi(this.tmp, curr);
					patternL.add(CommonOps.elementSum(curr));
				}
				
				// # of occurrences of pattern.
				int noOfOccs = pattern.getValue()[1];
				
				// Pr[rate cat] = 1 / nCat.
				returnL.mult(patternL.div((double)this.siteRates.nCat()).pow(noOfOccs));
			}
			return returnL;
		}
	}

	/**
	 * 
	 * @param n
	 */
	private void recursiveLikelihood(int n) {
		if (!this.T.isLeaf(n)) {
			this.recursiveLikelihood(this.T.getLeftChild(n));
			this.recursiveLikelihood(this.T.getRightChild(n));
		}
		this.updateLikelihood(n);
	}

	/**
	 * 
	 * @param n
	 */
	private void updateLikelihood(int n) {
		if (this.T.isLeaf(n)) {
			this.leafLikelihood(n);
		} else {
			// Get data and likelihood storage.
			LinkedHashMap<String, int[]> pv = this.D.getPatterns();
			PatternLikelihoods pl = this.likes.get(n);
			
			// Get nested Likelihoods
			PatternLikelihoods pl_l = this.likes.get(this.T.getLeftChild(n));
			PatternLikelihoods pl_r = this.likes.get(this.T.getRightChild(n));
			
			// Compute Pr[Dk | T, ew, r(j)] for each site rate category j.
			for (int j = 0; j < this.siteRates.nCat(); j++) {
				
				// Set up site rate-specific P matrix.
				assert this.branchLengths.get(n) > 0;
				double w = this.branchLengths.get(n) * this.siteRates.getRate(j);
				this.Q.updateTransitionMatrix(w);
				
				// Lastly, loop over each unique pattern in patterns.
				for (int i = 0; i < pv.size(); i++) {
					DenseMatrix64F left = pl_l.get(i, j);
					DenseMatrix64F right = pl_r.get(i, j);
					
					// Element-wise multiplication, tmp = left .* right.
					CommonOps.elementMult(left, right, this.tmp);
					DenseMatrix64F curr = pl.get(i, j);
					this.Q.multiplyWithP(this.tmp, curr);
				}
			}
		}
	}

	/**
	 * Updates the likelihoods for a leaf vertex.
	 * @param n leaf vertex.
	 */
	private void leafLikelihood(int n) {
		
		// Set up data and likelihood storage.
		LinkedHashMap<String, int[]> patterns = this.D.getPatterns();
		PatternLikelihoods pl = this.likes.get(n);
	
		// Get sequence index for this vertex.
		int seqIdx = this.D.getIndex(this.names.get(n));
		
		// Loop over rate categories.
		for (int j = 0; j < this.siteRates.nCat(); j++) {
			
			// Initiate transition matrix P.
			double w = this.branchLengths.get(n) * this.siteRates.getRate(j);
			this.Q.updateTransitionMatrix(w);
	
			// Loop over each unique pattern in patterns.
			int i = 0;
			for (Entry<String, int[]> pattern : patterns.entrySet()) {
				
				// Get position of pattern's first occurrence in partition.
				int pos = pattern.getValue()[0];
				
				// Compute likelihood.
				DenseMatrix64F curr = pl.get(i, j);
				this.Q.getLeafLikelihood(this.D.get(seqIdx, pos), curr);
				i++;
			}
		}
	}


	@Override
	public Dependent[] getParentDependents() {
		// We assume this.namesMap won't change.
		return new Dependent[] { this.T, this.branchLengths, this.siteRates };
	}


	@Override
	public void clearCache(boolean willSample) {
		this.likes.clearCache();
		this.cacheModelLikelihood = null;
	}


	@Override
	public void restoreCache(boolean willSample) {
		this.likes.restoreCache();
		this.like = this.cacheModelLikelihood;
		this.cacheModelLikelihood = null;
	}

	@Override
	public Class<?> getSampleType() {
		return SampleLogDouble.class;
	}

	@Override
	public String getSampleHeader() {
		return this.name;
	}

	@Override
	public String getSampleValue() {
		return SampleLogDouble.toString(this.like);
	}

	@Override
	public LogDouble getLikelihood() {
		return this.like;
	}
    
}
