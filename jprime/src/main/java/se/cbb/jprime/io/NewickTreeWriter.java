package se.cbb.jprime.io;

import java.util.ArrayList;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Class with convenience methods for transforming various tree data structures
 * into plain Newick strings.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickTreeWriter {

	/**
	 * Writes a plain Newick tree.
	 * @return the Newick tree string.
	 */
	public static String write(NewickTree nt) {
		return nt.toString();
	}
	
	/**
	 * Converts a rooted tree into a Newick string.
	 * @param T the tree.
	 * @param names the vertex/leaf names.
	 * @param doSort true to sort according to vertex names; false to leave unsorted. Sorting requires unique
	 *        vertex names (no bootstrap values or similarly!).
	 * @return a Newick tree.
	 * @throws NewickIOException.
	 */
	public static String write(RootedTree T, NamesMap names, boolean doSort) throws NewickIOException {
		return write(T, names, null, doSort);
	}
	
	/**
	 * Converts a rooted tree into a Newick string.
	 * @param T the tree.
	 * @param names the vertex/leaf names.
	 * @param branchLengths the branch lengths.
	 * @param doSort true to sort according to vertex names; false to leave unsorted. Sorting requires unique
	 *        vertex names (no bootstrap values or similarly!).
	 * @return a Newick tree.
	 * @throws NewickIOException.
	 */
	public static String write(RootedTree T, NamesMap names, DoubleMap branchLengths, boolean doSort) throws NewickIOException {
		NewickVertex nv = createNewickTree(T, T.getRoot(), names, branchLengths);
		NewickTree nt = new NewickTree(nv, null, false, doSort);
		return nt.toString();
	}
	
	/**
	 * Creates a NewickVertex tree for the subtree of T rooted at x.
	 * @param T the rooted tree.
	 * @param x the subtree of T rooted at x.
	 * @param names the vertex/leaf names.
	 * @param bls the branch lengths. May be null.
	 * @return the NewickVertex corresponding to x.
	 */
	private static NewickVertex createNewickTree(RootedTree T, int x, NamesMap names, DoubleMap bls) {
		String name = names.get(x);
		Double bl = (bls != null ? (!Double.isNaN(bls.get(x)) ? bls.get(x) : null) : null);
		String meta = null;
		NewickVertex nv = new NewickVertex(x, name, bl, meta);
		if (!T.isLeaf(x)) {
			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>(T.getNoOfChildren(x));
			for (int c : T.getChildren(x)) {
				children.add(createNewickTree(T, c, names, bls));
			}
			nv.setChildren(children);
		}
		return nv;
	}
		
}
