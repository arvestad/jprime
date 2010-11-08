package se.cbb.jprime.consensus.day;

import java.util.ArrayList;
import java.util.TreeMap;

import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.misc.IntPair;
import se.cbb.jprime.misc.IntTriple;
import se.cbb.jprime.topology.RootedTree;

/**
 * Implementation of a <code>PSWTree</code> where leaves are labelled from 0 to
 * |L(T)|-1 in the order visited in a DFS traversal. This enables the computation
 * of a "cluster table"; a data structure allowing cluster (clade) existence lookups to be
 * made in O(1).
 * <p/>
 * The cluster table is indexed from 0 to |L(T)|-1. Each element is a triple
 * (min,max,weight) which either corresponds to a cluster of an interior vertex, or otherwise
 * is empty (see below). In the former case:
 * <ul>
 * <li>[min,max] defines the span of the leaf labels of the cluster.</li>
 * <li>The weight defines the number of vertices of the subtree, root excluded.</li>
 * <li>The contiguous leaf numbering ensures that the number of leaves is max-min+1.</li>
 * <li>The table is assigned so that the triple will either be found at index min or index max.
 * This enables lookups to be made in O(1).</li>
 * </ul>
 * <p/>
 * Various sources seem to show small nuances in how the cluster table is
 * defined. This implementation adheres to the one in Day's original article, but
 * <ul>
 * <li>leaves are labelled from 0,1,... rather than 1,2,...</li>
 * <li>the root's (min,max,weight) is stored at both ends of the table (i.e. at indices 0
 * and |L(T)|-1).</li>
 * <li>(-1,-1,-1) are used for "empty" table values rather than (0,0,0).</li>
 * </ul>
 * 
 * @author Joel Sjöstrand.
 */
public class ClusterTablePSWTree extends PSWTree {
		
	/**
	 * Links leaf names to the integer label. Has nothing to do
	 * with the vertex numbers of the input tree. If unrooted, this.rerootName
	 * is naturally excluded from this list.
	 */
	private TreeMap<String, Integer> nameNumberMap;
	
	/** Cluster table. */
	private IntTriple[] clusterTable;
	
	/**
	 * Constructor. Labels the leaves DFS-wise, then computes PSW.
	 * @param tree the input tree (is not manipulated).
	 * @param names the leaf names, indexed w.r.t. the input tree.
	 * @param treatAsUnrooted true to treat the input tree as if unrooted,
	 * (the first encountered leaf will then be used as a root in internal
	 * representation).
	 */
	public ClusterTablePSWTree(RootedTree tree, StringMap names, boolean treatAsUnrooted) {
		super();
		
		// Duplicate tree intact.
		this.root = duplicateTree(tree, names, tree.getRoot());
		
		// If to treat as unrooted, reroot at the first leaf
		// (thus effectively eliminating that leaf from tree).
		if (treatAsUnrooted) {
			PSWVertex newRoot = this.root;
			while (!newRoot.isLeaf()) {
				newRoot = newRoot.getChildren().getFirst();
			}
			this.rerootName = newRoot.getName();
			reroot(newRoot);
		}
			
		// Label and compute PSW for the tree, etc.
		this.nameNumberMap = new TreeMap<String, Integer>();
		this.verticesPostordered = new ArrayList<PSWVertex>(tree.getNoOfVertices());
		labelTree(this.root, 0);
		this.root.computePSW();
		createClusterTable();
	}	
	
	/**
	 * Makes a post-order traversal of the tree, where:
	 * <ul>
	 * <li>leaves are labelled 0,1,... in the order they were visited.</li>
	 * <li>each leaf name is mapped to its number.</li>
	 * <li>all vertices are stored in a list in the order in which they were visited.</li>
	 * </ul>
	 * @param v the root of the subtree to process.
	 * @param nextNumber the next available number.
	 * @return the next available number.
	 */
	private int labelTree(PSWVertex v, int nextNumber) {
		if (v.isLeaf()) {
			this.nameNumberMap.put(v.getName(), nextNumber);
			v.setNumber(nextNumber++);
		} else {
			for (PSWVertex c : v.getChildren()) {
				nextNumber = labelTree(c, nextNumber);
			}
		}
		this.verticesPostordered.add(v);
		return nextNumber;
	}
	
	/**
	 * Fills the cluster table.
	 */
	private void createClusterTable() {
		this.clusterTable = new IntTriple[this.root.getNoOfLeaves()];
		IntTriple minMaxWeight;
		
		// For each non-root interior vertex with label span [min,max],
		// store the latter in either table[max] or table[min] depending on whether
		// the next vertex in the post-order traversal is a leaf or not.
		for (int i = 0; i < this.verticesPostordered.size() - 1; ++i) {
			PSWVertex v = this.verticesPostordered.get(i);
			if (!v.isLeaf()) {
				minMaxWeight = v.getMinMaxWeight();
				if (this.verticesPostordered.get(i+1).isLeaf()) {
					this.clusterTable[minMaxWeight.second] = minMaxWeight;
				} else {
					this.clusterTable[minMaxWeight.first] = minMaxWeight;
				}
			}
		}
		
		// Store root's (min,max,weight) at both first and last element of table.
		minMaxWeight = this.root.getMinMaxWeight();
		this.clusterTable[0] = minMaxWeight;
		this.clusterTable[this.clusterTable.length-1] = minMaxWeight;
		
		// Fill remaining elements with (-1,-1).
		minMaxWeight = new IntTriple(-1, -1, -1);
		for (int i = 0; i < this.clusterTable.length; ++i) {
			if (this.clusterTable[i] == null) {
				this.clusterTable[i] = minMaxWeight;
			}
		}
	}
	
	/**
	 * Returns the name-number map for leaves.
	 * @return the name-number map.
	 */
	public TreeMap<String, Integer> getNameNumberMap() {
		return this.nameNumberMap;
	}
	
	/**
	 * Returns the number label corresponding to a leaf name.
	 * @param name the leaf name.
	 * @return the leaf number label.
	 */
	public int getNumber(String name) {
		return this.nameNumberMap.get(name);
	}
	
	/**
	 * Returns the size of the cluster table (which is the same as
	 * the number of leaves).
	 * @return the size of the cluster table.
	 */
	public int getClusterTableSize() {
		return this.clusterTable.length;
	}
	
	/**
	 * Returns (min,max,weight) at index i in the cluster table, where i is
	 * in [0, |L(T)|-1].
	 * If not corresponding to a clade, (-1,-1,-1) is returned.
	 * @param i index.
	 * @return (min,max,weight).
	 */
	public IntTriple getMinMaxWeight(int i) {
		return (this.clusterTable[i]);
	}
	
	/**
	 * Returns min from a (min,max,weight) item in the cluster table.
	 * @param i the index.
	 * @return min.
	 */
	public int getMin(int i) {
		return (this.clusterTable[i].first);
	}
	
	/**
	 * Returns max from a (min,max,weight) item in the cluster table.
	 * @param i the index.
	 * @return max.
	 */
	public int getMax(int i) {
		return (this.clusterTable[i].second);
	}
	
	/**
	 * Returns max from a (min,max,weight) item in the cluster table.
	 * @param i the index.
	 * @return max.
	 */
	public int getWeight(int i) {
		return (this.clusterTable[i].third);
	}
	
	/**
	 * Returns true if a triple (min,max,weight) corresponding to a cluster is in the table.
	 * @param min min in triple.
	 * @param max max in triple.
	 * @param weight weight in triple.
	 * @return true if (min,max,weight) is in table; otherwise false.
	 */
	public boolean contains(int min, int max, int weight) {
		IntTriple ctmin = this.clusterTable[min];
		IntTriple ctmax = this.clusterTable[max];
		return ((ctmin.first == min && ctmin.second == max && ctmin.third == weight) ||
				(ctmax.first == min && ctmax.second == max && ctmax.third == weight));
	}
	
	/**
	 * Returns true if a triple (min,max,weight) corresponding to a cluster is in the table.
	 * @param minMaxWeight the triple.
	 * @return true if (min,max,weight) is in table; otherwise false.
	 */
	public boolean contains(IntTriple minMaxWeight) {
		return (this.clusterTable[minMaxWeight.first].equals(minMaxWeight) ||
				this.clusterTable[minMaxWeight.second].equals(minMaxWeight));
	}
	
	/**
	 * Returns true if a there is a triple (min,max,weight) with any weight
	 * in the cluster table for some min and max.
	 * @param min the min.
	 * @param max the max.
	 * @return true if (min,max,anyWeight) is in table; otherwise false.
	 */
	public boolean contains(int min, int max) {
		IntTriple ctmin = this.clusterTable[min];
		IntTriple ctmax = this.clusterTable[max];
		return ((ctmin.first == min && ctmin.second == max) ||
				(ctmax.first == min && ctmax.second == max));
	}
	
	/**
	 * Returns true if a there is a triple (min,max,weight) with any weight
	 * in the cluster table for some min and max.
	 * @param minMax the min and max.
	 * @return true if (min,max,anyWeight) is in table; otherwise false.
	 */
	public boolean contains(IntPair minMax) {
		IntTriple ctmin = this.clusterTable[minMax.first];
		IntTriple ctmax = this.clusterTable[minMax.second];
		return ((ctmin.first == minMax.first && ctmin.second == minMax.second) ||
				(ctmax.first == minMax.first && ctmax.second == minMax.second));
	}
}
