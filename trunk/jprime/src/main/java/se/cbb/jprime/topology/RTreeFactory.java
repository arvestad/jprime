package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.misc.Pair;

/**
 * Factory class used for creating RTree instances.
 * 
 * @author Joel Sjöstrand.
 */
public class RTreeFactory {

	/** Used for denoting null references. */
	public static final int NULL = RBTree.NULL;
	
	/**
	 * Creates an instance from a Newick tree, disregarding all meta info.
	 * The input tree is required to be "uncollapsable"	, not empty, and have
	 * vertices numbered from 0 to |V(T)|-1.
	 * @param tree the Newick tree to base the topology on.
	 * @param name the name of the tree.
	 */
	public static RTree createTree(NewickTree tree, String name) throws TopologyException {
		RTree t = new RTree();
		t.name = name;
		int k = tree.getNoOfVertices();
		t.parents = new int[k];
		t.children = new int[k][];
		NewickVertex root = tree.getRoot();
		if (root == null)
			throw new TopologyException("Cannot create RTree from empty NewickTree.");
		if (tree.isCollapsable())
			throw new TopologyException("Cannot create RTree from collapsable NewickTree.");
		List<NewickVertex> vertices = tree.getVerticesAsList();
		for (NewickVertex v : vertices) {
			int i = v.getNumber();
			if (v.isRoot()) {
				t.parents[i] = NULL;
				t.root = i;
			} else {
				t.parents[i] = v.getParent().getNumber();
			}
			if (v.isLeaf()) {
				t.children[i] = null;
			} else {
				ArrayList<NewickVertex> ch = v.getChildren();
				t.children[i] = new int[ch.size()];
				for (int j = 0; j < t.children[i].length; ++j) {
					t.children[i][j] = ch.get(j).getNumber();
				}
			}
		}
		return t;
	}
	
//	/**
//	 * Creates an instance from a Newick tree along with leaf/vertex names, disregarding all meta info.
//	 * The input tree is required to be bifurcating, not empty, and have
//	 * vertices numbered from 0 to |V(T)|-1.
//	 * @param tree the tree to base the topology on.
//	 * @param name the name of the tree.
//	 * @return the tree and names, where the latter is null if missing names.
//	 * @throws NullPointerException if a leaf name is null.
//	 * @throws TopologyException if the topology is incompatible.
//	 */
//	public static Pair<RTree, StringMap> createTreeAndNames(NewickTree tree, String name) throws NullPointerException, TopologyException {
//		RTree t = createTree(tree, name);
//		List<NewickVertex> vertices = tree.getVerticesAsList();
//		StringMap names = new StringMap("names", vertices.size());
//		for (NewickVertex v : vertices) {
//			String n = v.getName();
//			names.set(v.getNumber(), n);
//			if (n == null && v.isLeaf()) {
//				throw new NullPointerException("Missing leaf name in vertex " + v.getNumber() + '.');
//			}
//		}
//		return new Pair<RTree, StringMap>(t, names);
//	}
//	
//	/**
//	 * Creates an instance from a Newick tree along with leaf/vertex names, where
//	 * PrIME meta info has been read.
//	 * @param tree the PrIME Newick tree.
//	 * @return the tree and names, where the latter is null if missing names.
//	 * @throws TopologyException if topology incompatible.
//	 */
//	public static Pair<RTree, StringMap> createTreeAndNames(PrIMENewickTree tree) throws NullPointerException, TopologyException {
//		RTree t = createTree(tree.getNewickTree(), tree.getTreeName());
//		return new Pair<RTree, StringMap>(t, new StringMap("names", tree.getVertexNames()));
//	}
}
