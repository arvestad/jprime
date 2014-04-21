package se.cbb.jprime.io;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Sample type for Newick trees.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleNewickTree implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "NewickTree";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a rooted tree to a sorted Newick string.
	 * @param T the tree.
	 * @return the string.
	 * @throws NewickIOException.
	 */
	public static String toString(RootedTree T, NamesMap names) throws NewickIOException {
		return NewickTreeWriter.write(T, names, true);
	}
	
	/**
	 * Converts a rooted tree to a sorted Newick string.
	 * @param T the tree.
	 * @param branchLengths the branch lengths.
	 * @return the string.
	 * @throws NewickIOException.
	 */
	public static String toString(RootedTree T, NamesMap names, DoubleMap branchLengths) throws NewickIOException {
		return NewickTreeWriter.write(T, names, branchLengths, true);
	}

	/**
	 * Converts a rooted tree to a sorted Newick string.
	 * @param T the tree.
	 * @param branchLengths the branch lengths.
	 * @param pseudoSwitches the pseudogenization switches.
	 * @return the string.
	 * @throws NewickIOException.
	 */
	public static String toString(RootedTree T, NamesMap names, DoubleMap branchLengths, DoubleMap switches) throws NewickIOException {
		return NewickTreeWriter.write(T, names, branchLengths, switches, true);
	}
	
	/**
	 * Parses a string into a Newick tree.
	 * @param s the string.
	 * @return the Newick tree.
	 * @throws NewickIOException.
	 */
	public static NewickTree toNewickTree(String s) throws NewickIOException {
		return NewickTreeReader.readTree(s, false);
	}
}
