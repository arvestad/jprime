package se.cbb.jprime.consensus.day;

import java.util.ArrayList;

import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Abstract class for trees and data structures pertaining to William H. E. Day's
 * article "Optimal Algorithms for Comparing Trees with Labeled Leaves",
 * Journal of Classification, 1985.
 * <p/>
 * More specifically, instances provide access to the "post-order sequences with weights", PSW:
 * <li>Leaves of the tree are labelled with integers from 0 to |L(T)|-1.</li>
 * <li>For each interior vertex, the leaf label span of its descendant subtree is stored as a
 * (min,max) pair. The weight (number of proper descendants), and number of leaves are also stored.</li>
 * </ul>
 * These properties may be used to create a "cluster table"; a data structure used
 * for efficient computation of strict consensus trees, the Robinson-Foulds distance metric, etc.
 * <p/>
 * Input trees may be treated as rooted or unrooted.
 * On a side note, in the latter case, since the internal topology is in fact rooted, the
 * input tree is rerooted at a leaf. This implies that the topology is reduced by one or
 * two vertices (the leaf and possibly the original root).
 * <p/>
 * Two implementations are provided:
 * <ol>
 * <li><code>ClusterTablePSWTree</code>: Labels the leaves as traversed in a DFS, then computes the PSW and
 * creates a cluster table.</li>
 * <li><code>TemplatedPSWTree</code>: Labels the leaves based on a ClusterTableTree template, then computes
 * the PSW. Used for comparisons with the template.</li>
 * </ol>
 * Behaviour for a trees with a single leaf or single interior vertex is undefined.
 * 
 * @author Joel Sjöstrand.
 */
public abstract class PSWTree {

	/** Root for the topology. */
	protected PSWVertex root;
	
	/**
	 * Holds the name of the leaf used for rerooting (when treating input tree as
	 * unrooted), otherwise null.
	 */
	protected String rerootName = null;
	
	/** All vertices in the order they are visited in a post-order traversal. */
	protected ArrayList<PSWVertex> verticesPostordered;
	
	/**
	 * Builds the tree recursively by copying a rooted tree.
	 * Only the topology and names are set, not attributes (weights, number labels, etc.).
	 * @param tree the tree to be copied.
	 * @param names the names of (indices referring to the input tree).
	 * @param x the subtree being processed (index referring to the input tree).
	 * @return the root of the created subtree.
	 */
	protected PSWVertex duplicateTree(RootedTree tree, StringMap names, int x) {
		if (tree.isLeaf(x)) {
			return new PSWVertex(names.get(x), x);
		}
		PSWVertex v = new PSWVertex(x);
		for (int c : tree.getChildren(x)) {
			PSWVertex w = duplicateTree(tree, names, c);
			v.addChild(w);
			w.setParent(v);
		}
		return v;
	}
	
	/**
	 * Reroots the tree at another vertex v. If v was a leaf, it will be collapsed
	 * with its single child in the rerooted tree. Similarly, if the original root
	 * has out-degree 2, it will be collapsed with its single child in the rerooted tree.
	 * @param newRoot the vertex which will be made the new root.
	 */
	protected void reroot(PSWVertex newRoot) {
		// Rearrange all arcs along the path from the new root until the
		// original root, save for the last one.
		PSWVertex v = newRoot;
		PSWVertex vp = v.getParent();
		while (!vp.isRoot()) {
			PSWVertex vpp = vp.getParent();
			vp.removeChild(v);
			vp.setParent(v);
			v.addChild(vp);
			v = vp;
			vp = vpp;
		}
		// Rearrange the arcs of the original root (i.e. vp). If the latter
		// has out-degree 2, it can be collapsed.
		if (vp.getNoOfChildren() == 2) {
			// Collapse vp.
			PSWVertex vs = (vp.getChildren().getFirst() == v ?
					vp.getChildren().get(1) : vp.getChildren().getFirst());
			vs.setParent(v);
			v.addChild(vs);
		} else {
			// Don't collapse vp.
			vp.removeChild(v);
			vp.setParent(v);
			v.addChild(vp);
		}
		
		// If the new root has out-degree 1, collapse it.
		if (newRoot.getNoOfChildren() == 1) {
			this.root = newRoot.getChildren().getFirst();
		} else {
			this.root = newRoot;
		}
		this.root.setParent(null);
	}
	
	/**
	 * Returns the total number of vertices.
	 * @return the number of vertices.
	 */
	public int getNoOfVertices() {
		return this.verticesPostordered.size();
	}
	
	/**
	 * Returns the number of leaves.
	 * @return the number of leaves.
	 */
	public int getNoOfLeaves() {
		return this.root.getNoOfLeaves();
	}
	
	/**
	 * Returns the number of interior vertices.
	 * @return the number of interior vertices.
	 */
	public int getNoOfInteriorVertices() {
		return (this.verticesPostordered.size() - this.root.getNoOfLeaves());
	}
	
	/**
	 * Returns true if this instance is based on an input tree which was to be treated as
	 * unrooted. The leaf which was used for internal rerooting is no longer part of the topology.
	 * @return true if rerooted (i.e. unrooted input tree); otherwise false.
	 */
	public boolean isRerooted() {
		return (this.rerootName != null);
	}
	
	/**
	 * If this instance is based on an input tree which was to be treated as unrooted, this
	 * method returns the name of the original leaf that was used for internal rerooting.
	 * That leaf is no longer part of the topology.
	 * @return the name of the leaf used for rerooting; null if not rerooted.
	 */
	public String getRerootName() {
		return this.rerootName;
	}
	
	/**
	 * Returns all vertices of this tree as visited in a post-order traversal.
	 * @return the vertices.
	 */
	public ArrayList<PSWVertex> getVerticesPostordered() {
		return this.verticesPostordered;
	}
	
	/**
	 * Returns the root of the this instance.
	 * @return the root.
	 */
	public PSWVertex getRoot() {
		return this.root;
	}
}
