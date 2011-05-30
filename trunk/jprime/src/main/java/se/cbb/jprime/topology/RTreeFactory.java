package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.mcmc.Dependent;

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
		t.dependents = new ArrayList<Dependent>();
		NewickVertex root = tree.getRoot();
		if (root == null)
			throw new TopologyException("Cannot create RTree from empty NewickTree.");
		if (tree.isCollapsable())
			throw new TopologyException("Cannot create RTree from collapsable NewickTree.");
		List<NewickVertex> vertices = tree.getVerticesAsList();
		for (NewickVertex v : vertices) {
			int i = v.getNumber();
			
			// Parent relationships.
			if (v.isRoot()) {
				t.parents[i] = NULL;
				t.root = i;
			} else {
				t.parents[i] = v.getParent().getNumber();
			}
			
			// Child relationships.
			if (v.isLeaf()) {
				t.children[i] = new int[0];
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
}
