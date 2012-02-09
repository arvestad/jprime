package se.cbb.jprime.io;

import java.util.HashSet;
import java.util.List;

import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;

/**
 * Collects a number of sanity checks and similar for a <code>PrIMENewickTree</code>,
 * e.g. that all leaves have names, ultrametricity property on times, etc.
 * <p/>
 * Either invoke static method <code>runStandardTestSuite()</code> or create an instance
 * and run individual tests.
 * 
 * @author Joel Sjöstrand.
 */
public class PrIMENewickTreeVerifier {

	/** Max time discrepancy allowed for two vertex times to be considered equal. */
	public static double MAX_TIME_DIFF = 5e-3;
	
	/** The tree instance to perform sanity checks on. */
	private PrIMENewickTree tree;
	
	/** Vertices as array for convenience. */
	private List<NewickVertex> vertices;
	
	/**
	 * Runs a set of standard validations on a PrIMENewickTree and
	 * throws an exception in the event of an error.
	 * @param tree the tree to be verified.
	 * @throws NewickIOException in case of an error.
	 */
	public static void runStandardTestSuite(PrIMENewickTree tree) throws NewickIOException {
		PrIMENewickTreeVerifier tester = new PrIMENewickTreeVerifier(tree);
		tester.validateVertexNumbers();
		tester.validateVertexNames(true);
		tester.validateBranchLengths(true);
		tester.validateVertexWeights();
		tester.validateVertexTimes();
		tester.validateArcTimes(true);
		tester.validateUltrametricity(false, true);
	}
	
	/**
	 * Constructor.
	 * @param tree the tree instance to perform sanity checks on.
	 */
	public PrIMENewickTreeVerifier(PrIMENewickTree tree) {
		this.tree = tree;
		this.vertices = tree.getVerticesAsList();
	}
	
	/**
	 * Verifies that vertex numbers range from 0 to |V(T)|-1.
	 * @throws NewickIOException if incorrect.
	 */
	public void validateVertexNumbers() throws NewickIOException {
		boolean[] numberExists = new boolean[vertices.size()];
		for (NewickVertex v : this.vertices) {
			numberExists[v.getNumber()] = true;
		}
		for (int i = 0; i < numberExists.length; ++i) {
			if (!numberExists[i]) {
				throw new NewickIOException("Newick vertex numbering incorrect: vertex " + i + " is missing.");
			}
		}
	}
	
	/**
	 * Verifies that vertices either all have names or all lack names, and that all
	 * names are unique.
	 * @param leavesOnly true to check only leaves, false to check all vertices.
	 * @throws NewickIOException if incorrect.
	 */
	public void validateVertexNames(boolean leavesOnly) throws NewickIOException {
		if (this.tree.hasProperty(MetaProperty.VERTEX_NAMES)) {
			HashSet<String> names = new HashSet<String>(this.vertices.size());
			for (NewickVertex v : this.vertices) {
				if (!v.isLeaf() && leavesOnly) {
					continue;
				}
				if (!v.hasName()) {
					throw new NewickIOException("Missing name on Newick vertex " + v.getNumber() + ".");
				}
				if (v.hasName() && !names.add(v.getName())) {
					throw new NewickIOException("Duplicate vertex name '" + v.getName() + "' found in " +
							"vertex "+ v.getNumber() + '.');
				}
			}
		}
	}
	
	/**
	 * Verifies that vertices either all have branch lengths or all lack branch lengths.
	 * @param ignoreRoot true to exclude root vertex from test.
	 * @throws NewickIOException if incorrect.
	 */
	public void validateBranchLengths(boolean ignoreRoot) throws NewickIOException {
		validateDoubleArray(this.tree.getBranchLengths(), "branch length", ignoreRoot);
	}
	
	/**
	 * Verifies that vertices either all have vertex weights or all lack vertex weights.
	 * @throws NewickIOException if incorrect.
	 */
	public void validateVertexWeights() throws NewickIOException {
		validateDoubleArray(this.tree.getVertexWeights(), "vertex weight", false);
	}
	
	/**
	 * Verifies that vertices either all have vertex times or all lack vertex times.
	 * @throws NewickIOException if incorrect.
	 */
	public void validateVertexTimes() throws NewickIOException {
		validateDoubleArray(this.tree.getVertexTimes(), "vertex time", false);
	}
	
	/**
	 * Verifies that vertices either all have arc times or all lack arc times.
	 * @param ignoreRoot true to exclude root vertex from test.
	 * @throws NewickIOException if incorrect.
	 */
	public void validateArcTimes(boolean ignoreRoot) throws NewickIOException {
		validateDoubleArray(this.tree.getArcTimes(), "arc time", ignoreRoot);
	}
	
	/**
	 * Helper. Verifies some property array. Either all elements are null,
	 * or all elements have values (save, possibly, for the root).
	 * @param vals the array.
	 * @param propName the name of the array.
	 * @param ignoreRoot true to exclude root from verification.
	 * @throws NewickIOException if incorrect.
	 */
	private void validateDoubleArray(double[] vals, String propName, boolean ignoreRoot) throws NewickIOException {
		if (vals != null) {
			int ignore = (ignoreRoot ? this.tree.getRootNumber() : -1);
			for (int i = 0; i < vals.length; ++i) {
				if (Double.isNaN(vals[i]) && i != ignore) {
					throw new NewickIOException("Missing " + propName + " on Newick vertex " + i + ".");
				}
			}
		}
	}
	
	/**
	 * Verifies that vertex times are either empty or ultrametric, and similarly
	 * that arc times are either empty or ultrametric. Numeric precision discrepancies are
	 * allowed up to the value of constant MAX_TIME_DIFF.
	 * @param reqZeroAtleaves true to require that all leaf times equal 0.
	 * @param reqCompatibility true to require that vertex times, arc times and tree top time
	 * are compatible.
	 * @throws NewickIOException if incorrect.
	 */
	public void validateUltrametricity(boolean reqZeroAtleaves, boolean reqCompatibility) 
	throws NewickIOException {
		double[] vt = this.tree.getVertexTimes();
		double[] at = this.tree.getArcTimes();
		Double tt = this.tree.getTreeTopTime();
		
		// Verify 0 vertex times at leaves.
		if (reqZeroAtleaves && vt != null) {
			List<NewickVertex> leaves = this.tree.getLeavesAsList();
			for (NewickVertex l : leaves) {
				if (vt[l.getNumber()] != 0.0) {
					throw new NewickIOException("Leaf time at vertex " + l.getNumber() + " is not 0.");
				}
			}
		}
		
		// Verify increasing vertex times closer to root.
		if (vt != null && at == null) {
			for (NewickVertex v : this.vertices) {
				if (!v.isRoot() && vt[v.getNumber()] >= vt[v.getParent().getNumber()]) {
					throw new NewickIOException("Vertex time for vertex " + v.getNumber() + " is not smaller than that of parent vertex.");
				}
			}
		}
		
		// Verify arc times.
		if (at != null) {
			double[] times = relativeToAbsolute(this.vertices, at);
			
			// Verify compatibility.
			if (reqCompatibility) {
				// Vertex times.
				if (vt != null) {
					// Last element is always a leaf.
					double offset = vt[this.vertices.get(this.vertices.size() - 1).getNumber()];
					for (int i = 0; i < vt.length; ++i) {
						if (Math.abs(times[i] + offset - vt[2]) > MAX_TIME_DIFF) {
							throw new NewickIOException("Incompatible arc and vertex times at vertex " + i + '.');
						}
					}
				}
				// Top time.
				double atRoot = at[this.vertices.get(0).getNumber()];
				if (tt != null && !Double.isNaN(atRoot) && Math.abs(tt - atRoot) > MAX_TIME_DIFF) {
					throw new NewickIOException("Incompatible tree top time and root arc time.");
				}
			}
		}
	}
	
	/**
	 * Computes absolute times (vertex times) from a set of relative times (arc times).
	 * @param vertices the vertices, topologically ordered.
	 * @param at the relative times.
	 * @return the corresponding absolute times.
	 * @throws NewickIOException if times are not ultrametric.
	 */
	public static double[] relativeToAbsolute(List<NewickVertex> vertices, double[] at) throws NewickIOException {
		// Create array of "computed" vertex times, with leaf time set to 0.
		// Use NaN as marker of uninitialised elements.
		double[] times = new double[at.length];
		for (int i = 0; i < times.length; ++i) {
			times[i] = Double.NaN;
		}
		// Traverse backwards since vertex list is sorted post-order style.
		// Skip first element, since that is the root.
		for (int i = vertices.size() - 1; i > 0; --i) {
			NewickVertex v = vertices.get(i);
			int vidx = v.getNumber();
			if (v.isLeaf()) {
				times[vidx] = 0.0;
			} 
			if (!Double.isNaN(times[vidx])) {
				// Compute ancestor's vertex time.
				double t = times[vidx] + at[vidx];
				int anc = v.getParent().getNumber();
				if (Double.isNaN(times[anc])) {
					times[anc] = t;
				} else if (Math.abs(times[anc] - t) > MAX_TIME_DIFF) {
					throw new NewickIOException("Incompatible arc times in children of vertex " + anc
							+ ". Time diff is " + Math.abs(times[anc] - t) + ".");
				}
			}
		}
		// Special case for a single-vertex tree.
		if (times.length == 1) {
			times[0] = 0.0;
		}
		return times;
	}
	
	/**
	 * Computes relative times (arc times) from a set of absolute times (vertex times).
	 * @param vertices the vertices, topologically ordered.
	 * @param vt the absolute times.
	 * @return the corresponding relative times.
	 * @throws NewickIOException if times are not increasing from leaves to root.
	 */
	public static double[] absoluteToRelative(List<NewickVertex> vertices, double[] vt) throws NewickIOException {
		double[] times = new double[vt.length];
		times[0] = 0.0;			// Root gets 0 arc time by default.
		for (int i = 1; i < vertices.size(); ++i) {
			NewickVertex v = vertices.get(i);
			int vidx = v.getNumber();
			NewickVertex pv = v.getParent();
			int pvidx = pv.getNumber();
			times[vidx] = vt[pvidx] - vt[vidx];
			if (times[vidx] <= 0.0) {
				throw new NewickIOException("Vertex time for vertex " + v.getNumber() + " is not smaller than that of parent vertex.");
			}
		}
		return times;
	}
}
