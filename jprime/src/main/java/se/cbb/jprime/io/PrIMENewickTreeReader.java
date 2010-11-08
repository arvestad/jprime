package se.cbb.jprime.io;

import java.util.ArrayList;

/**
 * Similar to <code>NewickTreeReader</code>, but provides methods
 * for reading trees where PrIME meta info is parsed.
 * 
 * @author Joel Sjöstrand.
 */
public class PrIMENewickTreeReader {

	/**
	 * Reads consecutive Newick trees (augmented with PrIME meta info) from input.
	 * Empty input is allowed and renders an empty list.
	 * @param str the input.
	 * @param doSort true to sort the tree according to vertex names.
	 * @param strict set to true to raise an exception if some standard tests fail;
	 * alternatively one may conduct specific validation using class PrIMENewickTreeVerifier.
	 * @return the trees in a list.
	 * @throws NewickIOException if sanity checks fail.
	 */
	public static ArrayList<PrIMENewickTree> readTrees(String str, boolean doSort, boolean strict) throws NewickIOException {
		ArrayList<NewickTree> rawTrees = NewickTreeReader.readTrees(str, true);
		ArrayList<PrIMENewickTree> trees = new ArrayList<PrIMENewickTree>(rawTrees.size());
		for (NewickTree t : rawTrees) {
			trees.add(new PrIMENewickTree(t, strict));
		}
		return trees;
	}
	
	/**
	 * Reads exactly one Newick tree (augmented with PrIME meta info) from input.
	 * Additional trees following the first tree will be ignored and not cause parse errors.
	 * If input is just comprised of the tree, a semi-colon at the end is not compulsory.
	 * @param str the input.
	 * @param doSort true to sort the tree according to vertex names.
	 * @param strict set to true to raise an exception if some standard tests fail;
	 * alternatively one may conduct specific validation using class PrIMENewickTreeVerifier.
	 * @return the tree.
	 * @throws NewickIOException if sanity checks fail.
	 */
	public static PrIMENewickTree readTree(String str, boolean doSort, boolean strict) throws NewickIOException {
		return new PrIMENewickTree(NewickTreeReader.readTree(str, doSort), strict);
	}
	
}
