package se.cbb.jprime.io;

import java.util.ArrayList;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedTree;
import se.cbb.jprime.topology.StringMap;

/**
 * Class with convenience methods for transforming various tree data structures
 * into plain Newick strings. Note: It is perfectly possible to serialise
 * Newick trees with PrIME tags as well; just feed the meta info into the
 * appropriate methods.
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
		NewickVertex nv = createNewickTree(T, T.getRoot(), names, branchLengths, null);
		NewickTree nt = new NewickTree(nv, null, false, doSort);
		return nt.toString();
	}
	
	/**
	 * Converts a rooted tree into a Newick string.
	 * @param T the tree.
	 * @param names the vertex/leaf names.
	 * @param branchLengths the branch lengths.
	 * @param metas the meta info (e.g., PrIME tags).
	 * @param doSort true to sort according to vertex names; false to leave unsorted. Sorting requires unique
	 *        vertex names (no bootstrap values or similarly as names!).
	 * @return a Newick tree.
	 * @throws NewickIOException.
	 */
	public static String write(RootedTree T, NamesMap names, DoubleMap branchLengths, StringMap metas, boolean doSort) throws NewickIOException {
		NewickVertex nv = createNewickTree(T, T.getRoot(), names, branchLengths, metas);
		NewickTree nt = new NewickTree(nv, null, false, doSort);
		return nt.toString();
	}
	
	/**
	 * Converts a rooted tree into a Newick string.
	 * @param T the tree.
	 * @param names the vertex/leaf names.
	 * @param branchLengths the branch lengths.
	 * @param metas the meta info (e.g., PrIME tags).
	 * @param treeMeta the meta info for the tree itself (e.g., a PrIME tag).
	 * @param doSort true to sort according to vertex names; false to leave unsorted. Sorting requires unique
	 *        vertex names (no bootstrap values or similarly as names!).
	 * @return a Newick tree.
	 * @throws NewickIOException.
	 */
	public static String write(RootedTree T, NamesMap names, DoubleMap branchLengths, StringMap metas, String treeMeta, boolean doSort) throws NewickIOException {
		NewickVertex nv = createNewickTree(T, T.getRoot(), names, branchLengths, metas);
		NewickTree nt = new NewickTree(nv, treeMeta, false, doSort);
		return nt.toString();
	}
	
	/**
	 * Creates a NewickVertex tree for the subtree of T rooted at x.
	 * @param T the rooted tree.
	 * @param x the subtree of T rooted at x.
	 * @param names the vertex/leaf names.
	 * @param bls the branch lengths. May be null.
	 * @param metas the meta info (e.g., PrIME tags). May be null.
	 * @return the NewickVertex corresponding to x.
	 */
	protected static NewickVertex createNewickTree(RootedTree T, int x, NamesMap names, DoubleMap bls, StringMap metas) {
		String name = names.get(x);
		Double bl = (bls != null ? (!Double.isNaN(bls.get(x)) ? bls.get(x) : null) : null);
		String meta = (metas != null ? metas.get(x) : null);
		NewickVertex nv = new NewickVertex(x, name, bl, meta);
		if (!T.isLeaf(x)) {
			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>(T.getNoOfChildren(x));
			for (int c : T.getChildren(x)) {
				children.add(createNewickTree(T, c, names, bls, metas));
			}
			nv.setChildren(children);
		}
		return nv;
	}
		
}
