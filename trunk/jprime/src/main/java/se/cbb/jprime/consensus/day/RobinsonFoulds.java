package se.cbb.jprime.consensus.day;

import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Uses Day's algorithm to compute the Robinson-Foulds distance metric
 * between two trees T_1 and T_2, i.e. "the number of partitions of data implied by T_1 but not
 * T_2 + the number of partitions implied by T_2 but not T_1" in Wikipedia's phrasing.
 * 
 * @author Joel Sjöstrand.
 */
public class RobinsonFoulds {

	/**
	 * Computes the symmetric RF distance between two trees.
	 * @param tree1 the first tree.
	 * @param names1 the leaf names of the first tree.
	 * @param tree2 the second tree.
	 * @param names2 the leaf names of the second tree.
	 * @param treatAsUnrooted true to treat the trees as unrooted.
	 * @return the symmetric RF distance.
	 * @throws Exception if naming mismatch, etc.
	 */
	public static int computeDistance(RootedTree tree1, StringMap names1,
			RootedTree tree2, StringMap names2, boolean treatAsUnrooted) {
		
		ClusterTablePSWTree c1 = new ClusterTablePSWTree(tree1, names1, treatAsUnrooted);
		ClusterTablePSWTree c2 = new ClusterTablePSWTree(tree2, names2, treatAsUnrooted);
		TemplatedPSWTree t1, t2;
		if (!treatAsUnrooted) {
			// Very minor optimisation.
			t1 = new TemplatedPSWTree(c1, c2);
			t2 = new TemplatedPSWTree(c2, c1); 
		} else {
			t1 = new TemplatedPSWTree(tree1, names1, c2);
			t2 = new TemplatedPSWTree(tree2, names2, c1);
		}
		return (computeAsymmetricDistance(t1) + computeAsymmetricDistance(t2));
	}
	
	/**
	 * Computes the asymmetric (nota bene) RF distance between two trees.
	 * @param compTree the templated tree to be compared with its template.
	 * @return the number of partitions in the templated tree not in its template.
	 * @throws Exception if tree mismatches, etc.
	 */
	public static int computeAsymmetricDistance(TemplatedPSWTree compTree) {
		ClusterTablePSWTree orig = compTree.getTemplate();
		
		// Traverse interior vertices of templated tree, counting the those which:
		// 1) are obviously incorrect since leaf count is wrong.
		// 2) have correct leaf counts but lack correspondence in cluster table.
		int dist = 0;
		for  (PSWVertex v : compTree.getVerticesPostordered()) {
			if (!v.isLeaf()) {
				if (!v.hasContiguousLeafSpan() || !orig.contains(v.getMin(), v.getMax())) {
					++dist;
				}
			}
		}
		return dist;
	}
}
