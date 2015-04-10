package se.cbb.jprime.math;

import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.DenseMatrix64F;

/**
 * Class with additional EJML operations not found in
 * the current library at the time of writing.
 * 
 * @author Joel Sjöstrand.
 */
public class AdditionalEJMLOps {

	/**
	 * Gets a more succinct version of the solution of an eigensystem. It is assumed
	 * that no imaginary eigenvalues are present, and that the system has already
	 * been solved by eigdec.decompose(...).
	 * @param dim the dimension of the system, i.e., as dim*dim matrix.
	 * @param eigDec the solved eigensystem.
	 * @param eigvals output vector where to store eigenvalues. Should have size (dim,1).
	 * @param eigvecs output matrix where to store eigenvectors column-wise. Should have size (dim,dim).
	 */
	public static void getEigensystemSolution(int dim, EigenDecomposition<DenseMatrix64F> eigdec, DenseMatrix64F eigvals, DenseMatrix64F eigvecs) {
		for (int i = 0; i < dim; ++i) {
			// Eigenvalues.
			eigvals.set(i, eigdec.getEigenvalue(i).getReal());
			// Corresponding eigenvector column-wise.
			DenseMatrix64F eigvec = eigdec.getEigenVector(i);
			for (int j = 0; j < dim; ++j) {
				eigvecs.set(j, i, eigvec.get(j));
			}
		}
	}
	
	/**
	 * Computes element-wise b_i=exp(a_i*alpha) for each element i.
	 * @param dim number of elements.
	 * @param a matrix with size dim.
	 * @param alpha multiplier.
	 * @param b output matrix with size dim.
	 */
	public static void elementExp(int dim, DenseMatrix64F a, double alpha, DenseMatrix64F b) {
		for (int i = 0; i < dim; ++i) {
			b.set(i, Math.exp(a.get(i) * alpha));
		}
	}
	
	/**
	 * Computes c=a*b where a is a (compacted) diagonal matrix and b is a (typically) dense matrix.
	 * @param dim size of uncompacted a is (dim,dim).
	 * @param a diagonal matrix in 'compacted' form. Should have size (dim,1).
	 * @param b dense matrix. Should have size (dim,k).
	 * @param c output matrix. Should have size (dim,k).
	 */
	public static void multDiagA(int dim, DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) {
		for (int i = 0; i < dim; ++i) {
			double sc = a.get(i);
			for (int j = 0; j < b.numCols; ++j) {
				c.set(i, j, sc * b.get(i, j));
			}
		}
	}
}
