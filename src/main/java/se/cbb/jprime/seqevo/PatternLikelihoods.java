package se.cbb.jprime.seqevo;

import org.ejml.data.DenseMatrix64F;
import org.jfree.util.PublicCloneable;

/**
 * Holds the likelihoods of column patterns across one or more site rate categories
 * for a subtree.
 * Used e.g. by <code>SubstitutionModel</code>.
 * <p/>
 * For each unique pattern i and site rate category j, there is a vector r.
 * Each element in r corresponds to a state s in the 
 * Markov substitution model, representing the probability of the Markov process starting
 * with s at the root of the subtree of interest, and yielding the state pattern
 * corresponding to position i at the leaves of the subtree (under the site rate of category j).
 * 
 * @author Joel Sjöstrand.
 * @author Bengt Sennblad.
 */
public class PatternLikelihoods  implements PublicCloneable {
	
	/** Likelihoods, [i][j] for pattern i and site rate category j. */
	private DenseMatrix64F[][] likelihoods;
	
	/** Alphabet size. */
	private int alphabetSize;
	
	/**
	 * Constructor.
	 * @param noOfPatterns no. of unique patterns.
	 * @param noOfSiteRates no of site rate categories.
	 * @param alphabetSize alphabet size.
	 */
	public PatternLikelihoods(int noOfPatterns, int noOfSiteRates, int alphabetSize) {
		this.likelihoods = new DenseMatrix64F[noOfPatterns][noOfSiteRates];
		this.alphabetSize = alphabetSize;
		for (int i = 0; i < noOfPatterns; ++i) {
			for (int j = 0; j < noOfSiteRates; ++j) {
				this.likelihoods[i][j] = new DenseMatrix64F(alphabetSize, 1);
			}
		}
	}
	
	/**
	 * Copy constructor. Deep-copies the likelihoods.
	 * @param pl the object to copy.
	 */
	public PatternLikelihoods(PatternLikelihoods pl) {
		this.alphabetSize = pl.alphabetSize;
		int m = pl.likelihoods.length;
		int n = pl.likelihoods[0].length;
		this.likelihoods = new DenseMatrix64F[m][n];
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				this.likelihoods[i][j] = new DenseMatrix64F(pl.likelihoods[i][j]);
			}
		}
	}
	
	/**
	 * Returns the likelihoods of pattern i and site rate category j.
	 * @param i pattern index.
	 * @param j site rate index.
	 * @return the likelihoods of pattern i for site rate j.
	 */
	public DenseMatrix64F get(int i, int j) {
		return this.likelihoods[i][j];
	}
	
	/**
	 * Public clone method. Returns a copy of this object with deep-cloned likelihoods.
	 * @return this object.
	 */
	public Object clone() {
		return new PatternLikelihoods(this);
	}
	
	/**
	 * Returns the number of patterns.
	 * @return the number of patterns.
	 */
	public int getNoOfPatterns() {
		return this.likelihoods.length;
	}
	
	/**
	 * Returns the number of site rates.
	 * @return the number of site rates.
	 */
	public int getNoOfSiteRateCategories() {
		return this.likelihoods[0].length;
	}
	
}
