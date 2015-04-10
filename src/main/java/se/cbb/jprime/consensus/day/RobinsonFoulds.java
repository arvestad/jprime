package se.cbb.jprime.consensus.day;

import java.io.IOException;
import java.util.List;

import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.RootedTree;
import se.cbb.jprime.topology.TopologyException;

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
	
	/**
	 * Returns the RF distances between a list of k trees with equal terminal nodes.
	 * The output is a symmetric matrix. For convenience,
	 * the distances are returned on floating-point format.
	 * @param trees the trees.
	 * @param treatAsUnrooted true to treat as unrooted; false as rooted.
	 * @return the distances.
	 * @throws TopologyException.
	 * @throws IOException.
	 */
	public static double[][] computeDistanceMatrix(List<NewickTree> trees, boolean treatAsUnrooted) throws IOException, TopologyException {
		// Not optimised in the slightest way right now...
		int n = trees.size();
		double[][] dists = new double[n][];
		for (int i = 0; i < n; ++i) {
			dists[i] = new double[n];
			dists[i][i] = 0;
		}
		for (int i = 0; i < n - 1; ++i) {
			NewickTree t1 = trees.get(i);
			RTree r1 = new RTree(t1, "T1");
			NamesMap n1 = t1.getVertexNamesMap(true, "N1");
			for (int j = i + 1; j < trees.size(); ++j) {
				NewickTree t2 = trees.get(j);
				//System.out.println(t1.toString() + "     " + t2.toString());
				RTree r2 = new RTree(t2, "T2");
				NamesMap n2 = t2.getVertexNamesMap(true, "N2");
				int dist = RobinsonFoulds.computeDistance(r1, n1, r2, n2, treatAsUnrooted);
				dists[i][j] = dist;
				dists[j][i] = dist;
			}
		}
		return dists;
	}
	
	/**
	 * Returns the RF distance between pairs of trees. For convenience,
	 * the distances are returned on floating-point format.
	 * @param trees1 tree column 1.
	 * @param trees2 tree column 2.
	 * @param treatAsUnrooted true to treat as unrooted; false as rooted.
	 * @return the distances.
	 * @throws TopologyException.
	 * @throws IOException.
	 */
	public static double[] computePairedDistances(List<NewickTree> trees1, List<NewickTree> trees2, boolean treatAsUnrooted) throws TopologyException, IOException {
		if (trees1.size() != trees2.size()) {
			throw new IllegalArgumentException("Input tree lists do not have equal length.");
		}
		double[] dists = new double[trees1.size()];
		for (int i = 0; i < trees1.size(); ++i) {
			RTree r1 = new RTree(trees1.get(i), "T1");
			NamesMap n1 = trees1.get(i).getVertexNamesMap(true, "N1");
			RTree r2 = new RTree(trees2.get(i), "T2");
			NamesMap n2 = trees2.get(i).getVertexNamesMap(true, "N2");
			int dist = RobinsonFoulds.computeDistance(r1, n1, r2, n2, treatAsUnrooted);
			dists[i] = dist;
		}
		return dists;
	}
}
