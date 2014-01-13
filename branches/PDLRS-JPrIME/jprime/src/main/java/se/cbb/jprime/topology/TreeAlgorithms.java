package se.cbb.jprime.topology;

import java.util.HashSet;
import java.util.List;

/**
 * Algorithms pertaining to trees not already in the tree implementations themselves.
 * 
 * @author Joel Sjöstrand.
 */
public class TreeAlgorithms {

	/**
	 * Retrieves the set of vertices of the subtree of T spanned by the input
	 * vertices and the root of T. The output vertices are returned in reverse topological order
	 * (leaves to root).
	 * @param T tree.
	 * @param inputVertices subset of vertices of T (may or may not include the root).
	 * @return vertices sorted in reverse topological order.
	 */
	public static int[] getSpanningRootSubtree(RootedTree T, int[] inputVertices) {
		// First, find all affected vertices of T.
		HashSet<Integer> allEffected = new HashSet<Integer>(64);
		allEffected.add(T.getRoot());
		for (int u : inputVertices) {
			while (u != RTree.NULL) {
				if (!allEffected.add(u)) { break; }
				u = T.getParent(u);
			}
		}
		
		// Now sort them.
		int[] sorted = new int[allEffected.size()];
		List<Integer> vertices = T.getTopologicalOrdering();
		for (int i = vertices.size() - 1, j = 0; i >= 0; --i) {
			int u = vertices.get(i);
			if (allEffected.contains(u)) {
				sorted[j++] = u;
			}
		}
		return sorted;
	}
	
}
