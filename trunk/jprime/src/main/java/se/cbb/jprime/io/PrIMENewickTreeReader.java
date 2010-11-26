package se.cbb.jprime.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	public static List<PrIMENewickTree> readTrees(String str, boolean doSort, boolean strict) throws NewickIOException {
		List<NewickTree> rawTrees = NewickTreeReader.readTrees(str, true);
		ArrayList<PrIMENewickTree> trees = new ArrayList<PrIMENewickTree>(rawTrees.size());
		for (NewickTree t : rawTrees) {
			trees.add(new PrIMENewickTree(t, strict));
		}
		return trees;
	}
	
	/**
	 * Reads consecutive Newick trees (augmented with PrIME meta info) from an input file.
	 * Empty input is allowed and renders an empty list. Not optimised for large files.
	 * @param f the input file.
	 * @param doSort true to sort the tree according to vertex names.
	 * @param strict set to true to raise an exception if some standard tests fail;
	 * alternatively one may conduct specific validation using class PrIMENewickTreeVerifier.
	 * @return the trees in a list.
	 * @throws NewickIOException if sanity checks fail.
	 * @throws IOException.
	 */
	public static List<PrIMENewickTree> readTrees(File f, boolean doSort, boolean strict) throws NewickIOException, IOException {
		byte[] buffer = new byte[(int) f.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(buffer);
		} finally {
			if (fis != null) try { fis.close(); } catch (IOException ex) {}
		}
	    String str = new String(buffer);
	    return readTrees(str, doSort, strict);
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
	
	/**
	 * Reads exactly one Newick tree (augmented with PrIME meta info) from an input file.
	 * Additional trees following the first tree will be ignored and not cause parse errors.
	 * If input is just comprised of the tree, a semi-colon at the end is not compulsory.
	 * Not optimised for large files.
	 * @param f the input file.
	 * @param doSort true to sort the tree according to vertex names.
	 * @param strict set to true to raise an exception if some standard tests fail;
	 * alternatively one may conduct specific validation using class PrIMENewickTreeVerifier.
	 * @return the tree.
	 * @throws NewickIOException if sanity checks fail.
	 * @throws IOException.
	 */
	public static PrIMENewickTree readTree(File f, boolean doSort, boolean strict) throws NewickIOException, IOException {
		byte[] buffer = new byte[(int) f.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(buffer);
		} finally {
			if (fis != null) try { fis.close(); } catch (IOException ex) {}
		}
	    String str = new String(buffer);
	    return readTree(str, doSort, strict);
	}
	
}
