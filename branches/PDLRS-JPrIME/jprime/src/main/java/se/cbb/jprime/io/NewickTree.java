package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;

/**
 * Holds a "pure" Newick tree. Essentially, this only consists of a tree
 * rooted at a <code>NewickNode</code>, but with tree-specific meta info added, as well as
 * some convenience methods. See <code>NewickNode</code> for more details, and <code>PrIMENewickTree</code>
 * for an extension.
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
	protected NewickVertex root;
	
	/** Meta info between brackets. Null if empty. */
	protected String meta = null;
	
	/**
	 * Creates a Newick tree from a topology rooted at a NewickVertex.
	 * If chosen to be sorted, this occurs prior to numbering.
	 * @param root the root of the topology, must not be null.
	 * @param meta the meta info (provided between brackets) for the tree itself (vertices have their own meta tags).
	 * @param doRenumber true to relabel the NewickNodes post-order (Newick-style).
	 * @param doSort true to sort the tree using method sort(). Do not use when there are
	 *        non-unique vertex names (e.g. bootstrap value names).
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
	 * Protected constructor for superclasses.
	 * @param tree the tree to copy.
	 */
	protected NewickTree(NewickTree tree, boolean dummy) {
		this.root = tree.root;
		this.meta = tree.meta;
	}
	
	/**
	 * Copy constructor.
	 * Perform a deep copy of a whole NewickTree.
	 * @param tree NewickTree to copy.
	 */
	public NewickTree(NewickTree tree) {
		this.root = new NewickVertex(tree.root);
		this.meta = tree.meta;
		recursCopy(this.root, null);
	}
	
	/**
	 * Recursively copy all the vertices of a subtree.
	 * @param originalVertex vertex that give rise to the subtree to copy.
	 * @param newParent the parent of originalVertex.
	 */
	private void recursCopy(NewickVertex originalVertex, NewickVertex newParent) {
		ArrayList<NewickVertex> originalChildren = null;
		if (!originalVertex.isLeaf()) {
			// We are altering the list of the "originalVertex" children when replacing
			// a child by its copy, therefore we need to remember all the original children.
			originalChildren = new ArrayList<NewickVertex>(originalVertex.getChildren());
		}
		NewickVertex copiedVertex = null;
		if (!originalVertex.isRoot()) {
			copiedVertex = new NewickVertex(originalVertex);
			copiedVertex.setParent(newParent);
			// Copy the list of the newParent children, otherwise changing elements without copying
			// the list will change the elements of the original list.
			ArrayList<NewickVertex> newParentChildren = new ArrayList<NewickVertex>(newParent.getChildren());
			newParentChildren.remove(originalVertex);
			newParentChildren.add(copiedVertex);
			newParent.setChildren(newParentChildren);
		} else {
			// The root has already been copied in the copy constructor, therefore we must not copy it another time.
			copiedVertex = originalVertex;
		}
		if (!originalVertex.isLeaf()) {
			// Do the recursion copy on the children.
			for (NewickVertex child : originalChildren) {
				recursCopy(child, copiedVertex);
			}
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
	 * Returns the meta info (provided between brackets) for the tree itself
	 * (vertices have their own tags).
	 * @return the meta info, possibly null.
	 */
	public String getMeta() {
		return this.meta;
	}
	
	/**
	 * Sets the meta info for the tree itself. Null is recommended over
	 * empty string if lacking such info.
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
	 * Returns the number of leaves of the tree.
	 * @return the number of leaves.
	 */
	public int getNoOfLeaves() {
		return this.root.getNoOfLeaves();
	}
	
	/**
	 * Returns true if meta info is not null (only referring to the tag of the tree
	 * itself, not its vertices).
	 * Empty string returns true, as does
	 * empty string enclosed in brackets.
	 * @return true if vertex has meta info.
	 */
	public boolean hasMeta() {
		return (this.meta != null);
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
	 * Returns the list of all edges of the tree.
	 * Each edges is represented by a couple of vertices.
	 * @return the list of all edges of the tree.
	 */
	public ArrayList<NewickVertex[]> getEdges() {
		ArrayList<NewickVertex[]> edges = new ArrayList<NewickVertex[]>();
		recursAddEdges(this.root, edges);
		return edges;
	}
	
	/**
	 * Add edges of the subtree starting with v to list edges.
	 * @param v start of the subtree.
	 * @param edges list to add edges to.
	 */
	private void recursAddEdges(NewickVertex v, ArrayList<NewickVertex[]> edges) {
		if (!v.isLeaf()) {
			for (NewickVertex c : v.getChildren()) {
				NewickVertex[] e = {v, c}; 
				edges.add(e);
				recursAddEdges(c, edges);
			}
		}
	}
	
	/**
	 * Returns the vertex identified by its number n.
	 * @param n number of the vertex.
	 * @return the vertex identified by its number n.
	 * @throws NewickIOException if no vertex has the number n.
	 */
	public NewickVertex getVertex(int n) throws NewickIOException {
		NewickVertex vres = null;
		List<NewickVertex> vs = getVerticesAsList();
		for (NewickVertex v : vs) {
			if (v.getNumber() == n) { vres = v; }
		}
		if (vres == null) {
			throw new NewickIOException("No vertex found with number "+n+".");
		}
		return vres;
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
	 * Returns the vertex names.
	 * Single uninitialised items are set to null.
	 * See also getVertexNamesMap().
	 * @param leafNamesOnly true to set array's interior vertex names to null.
	 * @return the names.
	 */
	public String[] getVertexNames(boolean leafNamesOnly) {
		List<NewickVertex> vertices = this.getVerticesAsList();
		String[] names = new String[vertices.size()];
		for (NewickVertex v : vertices) {
			if (!leafNamesOnly || v.isLeaf()) {
				String n = v.getName();
				names[v.getNumber()] = n;
				if (n == null && v.isLeaf()) {
					throw new NullPointerException("Missing leaf name in vertex " + v.getNumber() + '.');
				}
			}
		}
		return names;
	}
	
	/**
	 * Returns the branch lengths. If these are lacking altogether, null is returned.
	 * Single uninitialised items are set to NaN
	 * (for which one checks by Double.isNaN(val)).
	 * See also getBranchLengthsMap().
	 * @return the branch lengths.
	 */
	public double[] getBranchLengths() {
		List<NewickVertex> vertices = this.getVerticesAsList();
		double[] bls = new double[vertices.size()];
		for (int i = 0; i < bls.length; ++i) {
			bls[i] = Double.NaN;
		}
		boolean hasBLs = false;
		for (NewickVertex v : vertices) {
			Double bl = v.getBranchLength();
			if (bl != null) {
				bls[v.getNumber()] = bl.doubleValue();
				hasBLs = true;
			}
		}
		return (hasBLs ? bls : null);
	}
	
	/**
	 * Returns a specific branch length. If this is lacking altogether, Double.NaN is returned.
	 * @param x the vertex.
	 * @return the branch length.
	 */
	public double getBranchLength(int x) {
		List<NewickVertex> vertices = this.getVerticesAsList();
		for (NewickVertex v : vertices) {
			if (v.getNumber() == x) {
				Double bl = v.getBranchLength();
				return (bl == null ? Double.NaN : bl);
			}
		}
		return Double.NaN;
	}
	
	/**
	 * Returns a map with a couple NewickVertex as key and its branch length as value.
	 * @return a map with a couple NewickVertex as key and its branch length as value.
	 * @throws NewickIOException
	 */
	public HashMap<NewickVertex[], Double> getMapEdgeBranchLength() throws NewickIOException {
		HashMap<NewickVertex[], Double> map = new HashMap<NewickVertex[], Double>();
		for (NewickVertex[] e : this.getEdges()) {
			map.put(e, this.getEdgeBranchLength(e[0], e[1]));
		}
		return map;
	}
	
	/**
	 * Get the branch length value of the edge (v, w).
	 * @param v vertex.
	 * @param w vertex.
	 * @return branch length value of the edge (v, w).
	 * @throws NewickIOException if trying to get the branch length of two non adjacent vertices.
	 */
	public Double getEdgeBranchLength(NewickVertex v, NewickVertex w) throws NewickIOException {
		NewickVertex child = getEdgeChild(v, w);
		return child.getBranchLength();
	}
	
	/**
	 * Sets the branch length of the edge (v, w).
	 * @param v vertex.
	 * @param w vertex.
	 * @param branchLength branch length.
	 * @throws NewickIOException 
	 */
	public void setEdgeBranchLength(NewickVertex v, NewickVertex w, Double branchLength) throws NewickIOException {
		NewickVertex c = getEdgeChild(v, w);
		c.setBranchLength(branchLength);
	}
	
	/**
	 * Returns the child of the edge formed by the two vertices v and w,
	 * ie. the vertex of the edge that is farther from the root.
	 * @param v vertex of one end of the edge.
	 * @param w vertex of the other end of the edge.
	 * @return the child of the edge formed by the two vertices v and w.
	 * @throws NewickIOException
	 */
	private NewickVertex getEdgeChild(NewickVertex v, NewickVertex w) throws NewickIOException {
		NewickVertex child = null;
		ArrayList<NewickVertex> childrenV = v.getChildren();
		ArrayList<NewickVertex> childrenW = w.getChildren();
		if ((childrenV != null && childrenV.contains(w)) || w.getParent() == v) {
			child = w;
		} else if ((childrenW != null && childrenW.contains(v)) || v.getParent() == w) {
			child = v;
		} else {
			throw new NewickIOException("Error: trying to access an edge of two non adjacent vertices : "+v+", "+w+".");
		}
		return child;
	}
	
	/**
	 * Returns a map of the names indexed by vertex numbers.
	 * @param leafNamesOnly true to set map's interior vertex names to null.
	 * @param name the name of the map.
	 * @return the map.
	 */
	public NamesMap getVertexNamesMap(boolean leafNamesOnly, String name) {
		return new NamesMap(name, this.getVertexNames(leafNamesOnly));
	}
	
	/**
	 * Returns a map of the branch lengths indexed by vertex numbers.
	 * If branch lengths are lacking altogether, null is returned.
	 * @param name the name of the map.
	 * @return the map.
	 */
	public DoubleMap getBranchLengthsMap(String name) {
		double[] bls = this.getBranchLengths();
		return (bls != null ? new DoubleMap(name, bls) : null);
	}
	
	/**
	 * Removes any branch lengths from the tree.
	 */
	public void clearBranchLengths() {
		List<NewickVertex> vertices = this.getVerticesAsList();
		for (NewickVertex v : vertices) {
			v.setBranchLength(null);
		}
	}
	
	/**
	 * Removes any meta info from the vertices and the tree itself.
	 */
	public void clearMeta() {
		List<NewickVertex> vertices = this.getVerticesAsList();
		for (NewickVertex v : vertices) {
			v.setMeta(null);
		}
		this.setMeta(null);
	}
	
	@Override
	public String toString() {
		// Conforms with serialisation of a Newick tree.
		if (this.hasMeta()) {
			StringBuilder sb = new StringBuilder(this.root.toString());
			sb.append(this.meta).append(';');
			return sb.toString();
		}
		return (this.root.toString() + ';');
	}
}
