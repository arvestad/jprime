package se.cbb.jprime.topology;

import java.util.List;

import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.misc.Pair;

/**
 * Factory class used for creating RBTree instances.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTreeFactory {

	/** Used for denoting null references. */
	public static final int NULL = RBTree.NULL;
	
	/**
	 * Creates an instance from a Newick tree, disregarding all meta info.
	 * The input tree is required to be bifurcating, not empty, and have
	 * vertices numbered from 0 to |V(T)|-1.
	 * @param tree the Newick tree to base the topology on.
	 * @param name the name of the tree.
	 */
	public static RBTree createTree(NewickTree tree, String name) throws TopologyException {
		RBTree t = new RBTree();
		t.name = name;
		int k = tree.getNoOfVertices();
		t.parents = new int[k];
		t.leftChildren = new int[k];
		t.rightChildren = new int[k];
		NewickVertex root = tree.getRoot();
		if (root == null)
			throw new TopologyException("Cannot create RBTree from empty NewickTree.");
		if (!tree.isBifurcating())
			throw new TopologyException("Cannot create RBTree from non-bifurcating NewickTree.");
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
				t.leftChildren[i] = NULL;
				t.rightChildren[i] = NULL;
			} else {
				t.leftChildren[i] = v.getChildren().get(0).getNumber();
				t.rightChildren[i] = v.getChildren().get(1).getNumber();
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
//	public static Pair<RBTree, StringMap> createTreeAndNames(NewickTree tree, String name) throws NullPointerException, TopologyException {
//		RBTree t = createTree(tree, name);
//		List<NewickVertex> vertices = tree.getVerticesAsList();
//		StringMap names = new StringMap("names", vertices.size());
//		for (NewickVertex v : vertices) {
//			String n = v.getName();
//			names.set(v.getNumber(), n);
//			if (n == null && v.isLeaf()) {
//				throw new NullPointerException("Missing leaf name in vertex " + v.getNumber() + '.');
//			}
//		}
//		return new Pair<RBTree, StringMap>(t, names);
//	}
//	
//	/**
//	 * Creates an instance from a Newick tree along with leaf/vertex names, where
//	 * PrIME meta info has been read.
//	 * @param tree the PrIME Newick tree.
//	 * @return the tree and names, where the latter is null if missing names.
//	 * @throws TopologyException if topology incompatible.
//	 */
//	public static Pair<RBTree, StringMap> createTreeAndNames(PrIMENewickTree tree) throws NullPointerException, TopologyException {
//		RBTree t = createTree(tree.getNewickTree(), tree.getTreeName());
//		return new Pair<RBTree, StringMap>(t, new StringMap("names", tree.getVertexNames()));
//	}
}
