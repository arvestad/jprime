package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.StringMap;

/**
 * Holds a "pure" Newick tree. Essentially, this only consists of a tree
 * rooted at a NewickNode, but with tree-specific meta info added, as well as
 * some convenience methods. See NewickNode for more details.
 * <p/>
 * One may invoke renumber() to renumber a tree post-order (Newick-style),
 * starting with 0 at the first leaf. Be advised that this property may
 * not always hold, e.g. if the tree numbers have been overridden by processed meta
 * tags.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickTree {

	/** The root. */
	private NewickVertex root;
	
	/** Meta info between brackets. Null if empty. */
	private String meta = null;
	
	/**
	 * Creates a Newick tree from a topology rooted at a NewickVertex.
	 * If chosen to be sorted, this occurs prior to numbering.
	 * @param root the root of the topology, must not be null.
	 * @param meta the meta info (provided between brackets).
	 * @param doRenumber true to relabel the NewickNodes post-order (Newick-style).
	 * @param doSort true to sort the tree using method sort().
	 * @throws NewickIOException.
	 */
	public NewickTree(NewickVertex root, String meta, boolean doRenumber, boolean doSort) throws NewickIOException {
		this.root = root;
		this.meta = meta;
		if (root == null)
			throw new NewickIOException("Cannot create empty NewickTree.");
		if (doSort) {
			sort();
		}
		if (doRenumber) {
			renumber();
		}
	}

	/**
	 * Returns the root vertex.
	 * @return the root.
	 */
	public NewickVertex getRoot() {
		return this.root;
	}
	
	/**
	 * Returns the ID if the root vertex.
	 * @return the ID.
	 */
	public int getRootNumber() {
		return this.root.getNumber();
	}
	
	/**
	 * Sets the root.
	 * @param root the root.
	 * @throws NewickIOException.
	 */
	public void setRoot(NewickVertex root) throws NewickIOException {
		this.root = root;
		if (root == null)
			throw new NewickIOException("Cannot set root in NewickTree to null.");
	}
	
	/**
	 * Returns the meta info (provided between brackets) for the tree.
	 * @return the meta info, possibly null.
	 */
	public String getMeta() {
		return this.meta;
	}
	
	/**
	 * Sets the meta info for the tree. Null is recommended
	 * over empty string if lacking such info.
	 * @param meta the meta info.
	 */
	public void setMeta(String meta) {
		this.meta = meta;
	}
	
	/**
	 * Relabels the node numbers of the tree Newick-style,
	 * i.e. post-order, starting with 0 at the first leaf.
	 */
	public void renumber() {
		this.root.renumber(0);
	}
	
	/**
	 * Returns the number of vertices of the tree.
	 * @return the number of nodes.
	 */
	public int getNoOfVertices() {
		return this.root.getNoOfDescendants(false);
	}
	
	/**
	 * Returns true if the tree is strictly bifurcating, i.e.
	 * every vertex has 2 or 0 children.
	 * @return true if the tree is bifurcating.
	 */
	public boolean isBifurcating() {
		return this.root.isBifurcating(true);
	}
	
	/**
	 * Returns true if the tree has a vertex with a single child.
	 * @return
	 */
	public boolean isCollapsable() {
		return this.root.isCollapsable(true);
	}
	
	/**
	 * Returns all vertices as an array. Moreover, the array is sorted post-order style,
	 * i.e. it preserves the partial order of the tree in that for a vertex at index k, its ancestors
	 * have index <k, while its descendants have index >k. This implies that
	 * the first element is the root, whereas the last element is a leaf.
	 * @return all vertices.
	 */
	public List<NewickVertex> getVerticesAsList() {
		ArrayList<NewickVertex> q = new ArrayList<NewickVertex>();
		q.add(getRoot());
		for (int i = 0; i < q.size(); ++i) {
			NewickVertex v = q.get(i);
			if (v.hasChildren()) {
				for (NewickVertex c : v.getChildren()) {
					q.add(c);
				}
			}
		}
		return q;
	}
	
	/**
	 * Returns all leaves as a list.
	 * @return all vertices.
	 */
	public List<NewickVertex> getLeavesAsList() {
		List<NewickVertex> vs = getVerticesAsList();
		ArrayList<NewickVertex> ls = new ArrayList<NewickVertex>((vs.size()+1) / 2);
		for (NewickVertex v : vs) {
			if (v.isLeaf()) { ls.add(v); }
		}
		return ls;
	}
	
	/**
	 * Sorts the tree according to vertex names. See sort() of class NewickVertex
	 * for more details on how this is achieved. Assumes that vertex names
	 * are unique.
	 * @throws NewickIOException if name collision while sorting.
	 */
	public void sort() throws NewickIOException {
		this.root.sort();
	}
	
	/**
	 * Returns a map of the names indexed by vertex numbers.
	 * @return the map.
	 */
	public StringMap getVertexNamesMap() {
		List<NewickVertex> vertices = this.getVerticesAsList();
		StringMap names = new StringMap("VertexNames", vertices.size());
		for (NewickVertex v : vertices) {
			String n = v.getName();
			names.set(v.getNumber(), n);
			if (n == null && v.isLeaf()) {
				throw new NullPointerException("Missing leaf name in vertex " + v.getNumber() + '.');
			}
		}
		return names;
	}
	
	/**
	 * Returns a map of the branch lengths indexed by vertex numbers.
	 * @return the map.
	 */
	public DoubleMap getBranchLengthsMap() {
		List<NewickVertex> vertices = this.getVerticesAsList();
		DoubleMap bls = new DoubleMap("BranchLengths", vertices.size());
		for (NewickVertex v : vertices) {
			Double bl = v.getBranchLength();
			bls.set(v.getNumber(), bl);
		}
		return bls;
	}
}