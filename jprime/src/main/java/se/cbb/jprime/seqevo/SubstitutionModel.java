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
import se.cbb.jprime.topology.TreeAlgorithms;

/**
 * Implements the standard (probabilistic) Markov model for
 * the substitution process of sequence evolution for any type 
 * of aligned sequence data, see e.g., Felsenstein 1981.
 * <p/>
 * Sequence positions with identical state patterns over leaves are identified and counted.
 * Rate variation across sites over discrete classes, e.g., Yang 1993,
 * can be modelled.
 * <p/>
 * This model does not yet support partitions of data into user-defined "independent" loci
 * (domains, codon positions, etc.). NOTE: This class is derived from the C++ class
 * <code>CacheSubstitutionModel</code> and not <code>FastCacheSubstitutionModel</code>
 * since the latter was stated unsuitable for tree topology changes in the C++ CMake
 * default settings. /Joel
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class SubstitutionModel implements Model {

	/** Model name. */
	private String name;
	
	/** Sequence data (MSA). */
	private MSAData D;
	
	/** Rate variation across sites. */
	private GammaSiteRateHandler siteRates;
	
	/** Transition rate matrix Q and, with it, P. */
	private SubstitutionMatrixHandler Q;
	
	/** Tree. */
	private RBTree T;
	
	/** Leaf names (=sequence names) of T. */
	private NamesMap names;

    /** Branch lengths, i.e., Markov model "time". */
    private DoubleMap branchLengths;
    
    /** Decides if root arc should be included in computations. */
    private boolean useRootArc;
    
    /**
     * For each vertex n of V(T), holds the likelihoods for the planted subtree
     * T^n. Each such element is an array with rows corresponding to unique patterns and
     * columns corresponding to site rate categories. Each element in such an array holds a
     * vector with likelihoods corresponding to the states of the sequence type alphabet. */
    private GenericMap<PatternLikelihoods> likelihoods;
    
    /** Model likelihood. */
    private LogDouble modelLikelihood;
    
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
     * @param useRootArc if true, utilises the root arc ("stem") branch length when computing model
     *        likelihood; if false, discards the root arc.
     */
    public SubstitutionModel(String name, MSAData D, GammaSiteRateHandler siteRates, SubstitutionMatrixHandler Q,
    		RBTree T, NamesMap names, DoubleMap branchLengths, boolean useRootArc) {
    	this.name = name;
    	this.D = D;
    	this.siteRates = siteRates;
    	this.Q = Q;
    	this.T = T;
    	this.names = names;
    	this.branchLengths = branchLengths;
    	this.useRootArc = useRootArc;
    	int noOfVertices = T.getNoOfVertices();
    	int noOfPatterns = D.getPatterns().size();
    	int noOfSiteRates = siteRates.getNoOfCategories();
    	int alphabetSize = Q.getAlphabetSize();
    	this.likelihoods = new GenericMap<PatternLikelihoods>(names + "Likelihoods", noOfVertices);
    	this.modelLikelihood = new LogDouble(0.0);
    	this.tmp = new DenseMatrix64F(alphabetSize, 1);
    	for (int n = 0; n < noOfVertices; ++n) {
    		this.likelihoods.set(n, new PatternLikelihoods(noOfPatterns, noOfSiteRates, alphabetSize));
    	}
    	this.updateLikelihood(this.T.getRoot(), true);
		this.computeModelLikelihood();
    }

    @Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
    	// Find out which parents have changed.
    	ChangeInfo tInfo = changeInfos.get(this.T);
    	ChangeInfo blInfo = changeInfos.get(this.branchLengths);
		ChangeInfo siteRateInfo = changeInfos.get(this.siteRates);
		if (tInfo != null || siteRateInfo != null || (blInfo != null && blInfo.getAffectedElements() == null)) {
			// Full update if tree or site rates have changed, or if undisclosed
			// branch lengths changes.
			this.fullUpdate();
			changeInfos.put(this, new ChangeInfo(this, "SubstitutionModel - full update"));
		} else if (blInfo != null && blInfo.getAffectedElements() != null) {
			// Partial update if disclosed branch length changes.
			// Get reverse-topological-ordered affected vertices.
			int[] allAffected = TreeAlgorithms.getSpanningRootSubtree(this.T, blInfo.getAffectedElements());
			this.partialUpdate(allAffected);
			changeInfos.put(this, new ChangeInfo(this, "SubstitutionModel - partial update", allAffected));
		}
	}
    
    /**
     * Performs a full update.
     */
    private void fullUpdate() {
		this.cacheModelLikelihood = new LogDouble(this.modelLikelihood);
		try {
			this.likelihoods.cache(null);
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		this.updateLikelihood(this.T.getRoot(), true);
		this.computeModelLikelihood();
    }
    
    /**
     * Performs a partial update.
     * @param affectedVertices vertices to update, in reverse topological order (leaves to root).
     */
    private void partialUpdate(int[] affectedVertices) {
    	this.cacheModelLikelihood = new LogDouble(this.modelLikelihood);
		try {
			this.likelihoods.cache(affectedVertices);
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		for (int n : affectedVertices) {
			this.updateLikelihood(n, false);
		}
		this.computeModelLikelihood();
    }

	
	/**
	 * Computes the overall model likelihood by consulting the root likelihood
	 * and the stationary state frequencies. The likelihood data structures must be up-to-date.
	 */
	private void computeModelLikelihood() {
		
		// Get root likelihood and patterns.
		LinkedHashMap<String, int[]> patterns = this.D.getPatterns();
		int n = this.T.getRoot();
		PatternLikelihoods pl = this.likelihoods.get(n);
		
		// Reset model likelihood.
		this.modelLikelihood = new LogDouble(1.0);
		
		// For each unique pattern i.
		int i = 0;
		for (Entry<String, int[]> pattern : patterns.entrySet()) {
			
			// For each site rate category j.
			LogDouble patternL = new LogDouble(0.0);
			for (int j = 0; j < pl.getNoOfSiteRateCategories(); ++j) {
				DenseMatrix64F curr = pl.get(i, j);
				
				// Multiply with stationary frequencies (that's our assumption for evolution start).
				this.Q.multiplyWithPi(curr, this.tmp);
				patternL.add(CommonOps.elementSum(this.tmp));
			}
			
			// Pr[site rate category] = 1 / # of categories.
			patternL.div((double) this.siteRates.getNoOfCategories());
			
			// # of actual columns of pattern.
			int noOfOccs = pattern.getValue()[1];
			
			// Multiply with overall likelihood.
			this.modelLikelihood.mult(patternL.pow(noOfOccs));
			i++;
		}
	}

	/**
	 * DP method which updates the likelihood column vectors for a subtree.
	 * @param n vertex root of subtree.
	 * @param doRecurse true to process entire subtree rooted at n; false to only do n.
	 */
	private void updateLikelihood(int n, boolean doRecurse) {
		if (this.T.isLeaf(n)) {
			this.updateLeafLikelihood(n);
		} else {
			
			// Process kids first.
			if (doRecurse) {
				this.updateLikelihood(this.T.getLeftChild(n), true);
				this.updateLikelihood(this.T.getRightChild(n), true);
			}
			
			// Get data and likelihood storage.
			LinkedHashMap<String, int[]> patterns = this.D.getPatterns();
			PatternLikelihoods pl = this.likelihoods.get(n);
			
			// Get child likelihoods.
			PatternLikelihoods pl_l = this.likelihoods.get(this.T.getLeftChild(n));
			PatternLikelihoods pl_r = this.likelihoods.get(this.T.getRightChild(n));
			
			// Just a special case: we discard evolution over the stem arc if desired (when doUseP = false).
			boolean doUseP = (this.useRootArc || !this.T.isRoot(n));
			
			// Compute Pr[Dk | T, l, r(j)] for each site rate category j.
			for (int j = 0; j < this.siteRates.getNoOfCategories(); j++) {
				
				if (doUseP) {
					// Set up site rate-specific P matrix.
					double w = this.branchLengths.get(n) * this.siteRates.getRate(j);
					this.Q.updateTransitionMatrix(w);
				}
				
				// Lastly, loop over each unique pattern in patterns.
				for (int i = 0; i < patterns.size(); i++) {
					DenseMatrix64F left = pl_l.get(i, j);
					DenseMatrix64F right = pl_r.get(i, j);
					
					// Element-wise multiplication, tmp = left .* right.
					CommonOps.elementMult(left, right, this.tmp);
					DenseMatrix64F curr = pl.get(i, j);
					if (doUseP) {
						this.Q.multiplyWithP(this.tmp, curr);
					} else {
						curr.set(this.tmp);
					}
				}
			}
		}
	}

	/**
	 * DP method which updates the likelihoods column vector for a leaf vertex.
	 * @param n leaf vertex.
	 */
	private void updateLeafLikelihood(int n) {
		
		// Set up data and likelihood storage.
		LinkedHashMap<String, int[]> patterns = this.D.getPatterns();
		PatternLikelihoods pl = this.likelihoods.get(n);
	
		// Get sequence index for this vertex.
		int seqIdx = this.D.getSequenceIndex(this.names.get(n));
		
		// Loop over rate categories.
		for (int j = 0; j < this.siteRates.getNoOfCategories(); j++) {
			
			// Set up site rate-specific P matrix.
			double w = this.branchLengths.get(n) * this.siteRates.getRate(j);
			this.Q.updateTransitionMatrix(w);
	
			// Loop over each unique pattern in patterns.
			int i = 0;
			for (Entry<String, int[]> pattern : patterns.entrySet()) {
				
				// Get position of pattern's first occurrence in partition.
				int pos = pattern.getValue()[0];
				
				// Compute likelihood.
				DenseMatrix64F curr = pl.get(i, j);
				int state = this.D.getIntState(seqIdx, pos);
				this.Q.getLeafLikelihood(state, curr);
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
		this.likelihoods.clearCache();
		this.cacheModelLikelihood = null;
	}


	@Override
	public void restoreCache(boolean willSample) {
		this.likelihoods.restoreCache();
		this.modelLikelihood = this.cacheModelLikelihood;
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
		return SampleLogDouble.toString(this.modelLikelihood);
	}

	@Override
	public LogDouble getLikelihood() {
		return this.modelLikelihood;
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(4096);
		sb.append(prefix).append("SUBSTITUTION MODEL\n");
		sb.append(prefix).append("Including root arc in computations: ").append(this.useRootArc).append('\n');
		sb.append(prefix).append("Multiple sequence alignment data:\n");
		sb.append(this.D.getPreInfo(prefix + '\t'));
		sb.append(prefix).append("Discrete site rates:\n");
		sb.append(this.siteRates.getPreInfo(prefix + '\t'));
		sb.append(prefix).append("Substitution matrix:\n");
		sb.append(this.Q.getPreInfo(prefix + '\t'));
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(4096);
		sb.append(prefix).append("SUBSTITUTION MODEL\n");
		sb.append(prefix).append("Multiple sequence alignment data:\n");
		sb.append(this.D.getPostInfo(prefix + '\t'));
		sb.append(prefix).append("Discrete site rates:\n");
		sb.append(this.siteRates.getPostInfo(prefix + '\t'));
		sb.append(prefix).append("Substitution matrix:\n");
		sb.append(this.Q.getPostInfo(prefix + '\t'));
		return sb.toString();
	}
    
}
