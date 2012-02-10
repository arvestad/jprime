package se.cbb.jprime.seqevo;

import java.util.Arrays;
import java.util.HashMap;

import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import se.cbb.jprime.math.AdditionalEJMLOps;
import se.cbb.jprime.mcmc.InfoProvider;
import se.cbb.jprime.misc.BoundedRealMap;

/**
 * Handles transition probabilities of a Markov process for molecular sequence evolution.
 * As such, it can be viewed as a representing a momentary transition rate matrix Q, 
 * as well as a transition probability matrix P for some user-set Markov time w.
 * <p/>
 * The momentary transition rate matrix Q can be decomposed into a
 * symmetric <i>exchangeability</i> matrix R and the vector of 
 * stationary frequencies Pi. The transition probability
 * matrix P over a given (Markov) time interval w is given by 
 * P=exp(Qw). Note that w often is measured in the expected 
 * number of events per site occurring over the interval.
 * <p/>
 * Assumes time reversibility, that is, pi_i*mu_ij=pi_j*mu_ji for stationary frequencies pi_x
 * and transition probabilities mu_xy.
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class SubstitutionMatrixHandler implements InfoProvider {
	
	/** The maximum allowed time w on which transition rate matrix Q can act. */
	public static final double MAX_MARKOV_TIME = 1000.0;

	/** Substitution model name. */
	private String modelName;

	/** The sequence type that is handled, e.g., DNA. TODO: Bens: Can we avoid it? */
	private SequenceType sequenceType;

	/** Number of states in model alphabet, dim. */
	private int alphabetSize;

	/**
	 * The symmetric 'intrinsic rate' matrix of the model (a.k.a. 'exchangeability' matrix).
	 * Represented as a row-major triangular matrix with size (dim*(dim-1)/2,1).
	 * Symmetric values are implicitly defined due to time reversibility assumption.
	 */
	private DenseMatrix64F R;

	/** The stationary frequencies of the model formulated as a diagonal matrix. Only diagonal stored as matrix of size (dim,1). */
	private DenseMatrix64F Pi;
	
	/** The transition rate matrix Q, normalised to have 1 expected event over branch length 1. */
	private DenseMatrix64F Q;

	/** Eigenvalues of the transition rate matrix. Only diagonal, stored as matrix of size (dim,1). */
	private DenseMatrix64F E;

	/** Eigenvectors of the transition rate matrix. Size (dim,dim). */
	private DenseMatrix64F V;

	/** Inverse of V. Size (dim,dim). */
	private DenseMatrix64F iV;

	/** Transition probability matrix (updated frequently for different user times w). Size (dim,dim). */
	private DenseMatrix64F P;

	/** Temporary storage matrix. Size (dim,dim). */
	private DenseMatrix64F tmp_matrix;

	/** Temporary storage vector. Size (dim,1). */
	private DenseMatrix64F tmp_diagonal;

	/** A cache for saving instances of P for varying times w to avoid recalculations. */
	private BoundedRealMap<DenseMatrix64F> PCache;
	
	/** Small cache for ambiguity leaf likelihoods. Cleared every time P i updated. */
	private HashMap<Integer, DenseMatrix64F> ambigCache;

	/**
	 * Constructor.
	 * @param modelName name of substitution model.
	 * @param sequenceType sequence type.
	 * @param R_vec 'intrinsic' rate matrix, in row-major format. Time reversibility is assumed, therefore, its length should
	 * be dim*(dim-1)/2, where dim is the alphabet length.
	 * @param Pi_vec stationary frequencies. Should have length dim, where dim is the alphabet length.
	 * @param cacheSize number of P matrices to store in cache, e.g., 1000.
	 */
	public SubstitutionMatrixHandler(String modelName, SequenceType sequenceType, double[] R_vec, double[] Pi_vec, int cacheSize) {
		this.modelName = modelName;
		this.sequenceType = sequenceType;
		this.alphabetSize = sequenceType.getAlphabetSize();
		this.R = new DenseMatrix64F(alphabetSize * (alphabetSize - 1) / 2, 1, true, R_vec);
		this.Pi = new DenseMatrix64F(alphabetSize, 1, true, Pi_vec);
		this.Q = new DenseMatrix64F(alphabetSize, alphabetSize);
		this.E = new DenseMatrix64F(alphabetSize, 1);
		this.V = new DenseMatrix64F(alphabetSize, alphabetSize);
		this.iV = new DenseMatrix64F(alphabetSize, alphabetSize);
		this.P = new DenseMatrix64F(alphabetSize, alphabetSize);
		this.tmp_matrix = new DenseMatrix64F(alphabetSize, alphabetSize);
		this.tmp_diagonal = new DenseMatrix64F(alphabetSize, 1);
		this.PCache = new BoundedRealMap<DenseMatrix64F>(cacheSize, true);
		this.ambigCache = new HashMap<Integer, DenseMatrix64F>(14);  // Not more than at most ~14 different ambiguity characters.
		this.update();
	}


	/**
	 * Tests if model and the submitted data are compatible.
	 * This could be used as an early error-catcher.
	 * @param sd sequence data.
	 * @return true if compatible; false if incompatible.
	 */
	public boolean isCompatible(MSAData sd) {
		return (this.sequenceType == sd.getSequenceType());
	}

	/**
	 * Returns the name of the substitution model.
	 * @return model name.
	 */
	public String getModel() {
		return this.modelName;
	}

	/**
	 * Returns the sequence type that the model handles (DNA, AA, etc.).
	 * @return sequence type.
	 */
	public SequenceType getSequenceType() {
		return this.sequenceType;
	}

	/**
	 * Returns the alphabet size of the Markov process.
	 * @return the size.
	 */
	public int getAlphabetSize() {
		return this.alphabetSize;
	}

	/**
	 * Updates Q and the eigensystem based on R and Pi.
	 */
	private void update() {
		// Creates Q by means of R and Pi.
		// The diagonal values of Q = -the sum of other values of row, by definition.
		// R in this implementation holds upper triangle of symmetric matrix, excluding diagonal.
		this.PCache.clear();
		this.ambigCache.clear();
		this.Q.zero();
		int R_i = 0;
		double val;
		for (int i = 0 ; i < alphabetSize; i++) {
			for (int j = i + 1; j < alphabetSize; j++) {
				val = this.Pi.get(i) * this.R.get(R_i);
				this.Q.set(i, j, val);
				this.Q.set(i, i, this.Q.get(i,i) - val);
				// R is symmetric.
				val = this.Pi.get(j) * this.R.get(R_i++);
				this.Q.set(j, i, val);
				this.Q.set(j, j, this.Q.get(j, j) - val);
			}
		}
		
		// Perform scaling of Q so that a branch length of w=1 yields 1 expected event.
		double beta = 0;
		for (int i = 0; i < this.alphabetSize; ++i) {
			beta -= this.Pi.get(i) * this.Q.get(i, i);
		}
		beta = 1.0 / beta;
		CommonOps.scale(beta, this.Q);
		
		// Solve eigensystem. NOTE: It is assumed solutions with imaginary parts will never be encountered.
		// To avoid checks, we assume non-symmetric Q. Symmetric models (JC69, etc.) are rarely used in practice anyway.
		EigenDecomposition<DenseMatrix64F> eigFact = DecompositionFactory.eigGeneral(this.alphabetSize, true);
		if (!eigFact.decompose(this.Q)) {
			throw new RuntimeException("Unable to decompose eigensystem for substitution model.");
		}
		AdditionalEJMLOps.getEigensystemSolution(this.alphabetSize, eigFact, this.E, this.V);
		
		// Compute inverse of V.
		if (!CommonOps.invert(this.V, this.iV)) {
			throw new RuntimeException("Unable to invert matrix of eigenvectors in substitution model.");
		}
	}


	/**
	 * Sets up P=exp(Qw), the transition probability matrix for the Markov
	 * process over 'time' w (where 'time' is not necessarily temporal).
	 * Precondition: w <= 1000.
	 * @param w the "time" (or branch length) over which Q acts.
	 */
	public void updateTransitionMatrix(double w) {
		// C++ comment which may still apply...  /joelgs
		// If w is too big, the precision of LAPACK seem to get warped!
		// The choice of max value of 1000 is arbitrary and well below the 
		// actual max value. /bens
		// TODO: Could we precondition on a reasonable MarkovTime?
		if (w > MAX_MARKOV_TIME) {
			throw new IllegalArgumentException("In substitution model, cannot compute transition probability matrix P for too large Markov time w=" + w + ".");
		}
		
		// Clear ambiguity cache.
		this.ambigCache.clear();

		// Check in cache if result already exists.
		this.P = this.PCache.get(w);
		if (this.P == null) {
			// Nope, we have to create it.
			AdditionalEJMLOps.elementExp(this.alphabetSize, this.E, w, this.tmp_diagonal);
			AdditionalEJMLOps.multDiagA(this.alphabetSize, this.tmp_diagonal, this.iV, this.tmp_matrix);
			this.P = new DenseMatrix64F(this.alphabetSize, this.alphabetSize);
			CommonOps.mult(this.V, this.tmp_matrix, this.P);
			this.PCache.put(w, this.P);
		}
	}

	/**
	 * Performs matrix multiplication Y=P*X for the current P.
	 * @param X operand matrix (typically vector) of size (dim,ncol).
	 * @param Y resulting matrix Y=P*X. Should have size (dim,ncol).
	 */
	public void multiplyWithP(DenseMatrix64F X, DenseMatrix64F Y) {
		CommonOps.mult(this.P, X, Y);
	}

	/**
	 * Returns the likelihood for a certain leaf state for the current P.
	 * This corresponds to the state's column in P (and analogously for
	 * ambiguity characters.
	 * @param state the state's integer index.
	 * @param result the column values. Should have size (dim,1).
	 */
	public void getLeafLikelihood(int state, DenseMatrix64F result) {
		if (state < this.alphabetSize) {
			for (int i = 0; i < this.alphabetSize; ++i) {
				result.set(i, this.P.get(i, state));
			}
		} else {
			// Ambiguity state.
			DenseMatrix64F res = this.ambigCache.get(state);
			if (res == null) {
				// Not computed before.
				this.multiplyWithP(this.sequenceType.getLeafLikelihood(state), result);
				this.ambigCache.put(state, new DenseMatrix64F(result));
			} else {
				// Computed before.
				result.set(res);
			}
		}
	}

	/**
	 * Element-wise multiplication Y=Pi*X.
	 * @param X operand matrix (typically vector) of size (dim,ncol).
	 * @param Y resulting matrix Y=Pi*X. Should have size (dim,ncol).
	 */
	public void multiplyWithPi(DenseMatrix64F X, DenseMatrix64F Y) {
		AdditionalEJMLOps.multDiagA(this.alphabetSize, this.Pi, X, Y);
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Markov transition rate matrix of model ").append(modelName).append('\n');
		sb.append("Current symmetric intrinsic rate matrix, R (flattened):\n");
		sb.append(this.R.toString()).append('\n');
		sb.append("Current stationary distribution base frequencies, Pi:\n");
		sb.append(this.Pi.toString()).append('\n');
		sb.append("Current eigenvalue matrix of Q, E:\n");
		sb.append(this.E.toString()).append('\n');
		sb.append("Current right-hand side eigenvectors of Q, V:\n");
		sb.append(this.V.toString()).append('\n');
		sb.append("Current inverse of V, iV:\n");
		sb.append(this.iV.toString()).append('\n');
		return sb.toString();
	};


	/**
	 * For debugging and similar purposes, returns R in a String format.
	 * @return the R matrix.
	 */
	public String getRString() {
		StringBuilder sb = new StringBuilder();
		int R_index = 0;
		sb.append("Alphabet_size: ").append(alphabetSize).append('\n');
		for (int i = 0; i < alphabetSize; i++) {
			for (int j = 0; j < alphabetSize; j++) {
				if (j < alphabetSize) {
					sb.append('\t');
				}
				if (j > i) {
					sb.append(this.R.get(R_index++));
				}
			}
			if (i < alphabetSize - 2) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}


	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(4096);
		sb.append(prefix).append("SUBSTITUTION MATRIX HANDLER\n");
		sb.append(prefix).append("Model name: ").append(this.modelName).append('\n');
		sb.append(prefix).append("Alphabet size: ").append(this.alphabetSize).append('\n');
		sb.append(prefix).append("Stationary frequencies Pi: ").append(Arrays.toString(this.Pi.getData())).append('\n');
		sb.append(prefix).append("Exchangeability matrix R (time reversible, symmetric, only part above diagonal in ro-major format): ").append(Arrays.toString(this.R.getData())).append('\n');
		sb.append(prefix).append("Transition matrix P cache size: ").append(this.PCache.getMaxNoOfElements()).append('\n');
		return sb.toString();
	}


	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(prefix).append("SUBSTITUTION MATRIX HANDLER\n");
		return sb.toString();
	}
}
