package se.cbb.jprime.topology;

import se.cbb.jprime.seqevo.MSAData;

/**
 * Computes the percent identity for a topology with multiple sequence alignment.
 * Percent identity for interior vertices are calculated as a weighted average
 * for the occurrences of states at the children. Only bifurcated trees supported
 * at the moment.
 * 
 * @author Joel Sjöstrand.
 */
public class TreeSequenceIdentity {
	
	/** Tree. */
	private RBTree tree;
	
	/** Leaf names. */
	private NamesMap names;
	
	/** MSA. */
	private MSAData msa;
	
	/**
	 * ID counts thus: (vertex,position,character). Furthermore, after the elements for the character states in the innermost array,
	 * an additional item stores the "weighted ID count", followed by an item for the total number of sequences of the subtree.
	 */
	private int[][][] ids;
	
	/** No. of positions in MSA. */
	private int noOfPos;
	
	/** No. of residue types (4 for nucleotides, etc.). */
	private int alphabetSize;
	
	/**
	 * Constructor.
	 * @param tree tree.
	 * @param names leaf names.
	 * @param msa MSA.
	 */
	public TreeSequenceIdentity(RBTree tree, NamesMap names, MSAData msa) {
		this.tree = tree;
		this.names = names;
		this.msa = msa;
		this.noOfPos = msa.getNoOfPositions();
		this.alphabetSize = msa.getSequenceType().getAlphabetSize();
		this.ids = new int[tree.getNoOfVertices()][][];
		this.computeIdentity(this.tree.getRoot());
	}
	
	/**
	 * Recursively fills the ID map.
	 * @param x the subtree root vertex.
	 */
	private void computeIdentity(int x) {
		int[][] id = new int[noOfPos][];
		
		// Leaf base case.
		if (this.tree.isLeaf(x)) {
			String name = this.names.get(x);
			int idx = this.msa.getSequenceIndex(name);
			
			// For every position.
			for (int i = 0; i < noOfPos; ++i) {
				int[] idi = new int[alphabetSize + 2]; // Two more elements.
				int state = this.msa.getIntState(idx, i);
				if (state < alphabetSize) {
					// Other characters such as - or X are counted but not matched for.
					idi[state] = 1;
				}
				idi[alphabetSize] = 1;      // By definition -- doesn't matter.
				idi[alphabetSize + 1] = 1;  // Total count.
				id[i] = idi;
			}
			this.ids[x] = id;
			return;
		}
		
		// Interior vertex case.
		int lc = this.tree.getLeftChild(x);
		int rc = this.tree.getRightChild(x);
		computeIdentity(lc);
		computeIdentity(rc);
		
		int[][] lcid = this.ids[lc];
		int[][] rcid = this.ids[rc];
		
		// For every position.
		for (int i = 0; i < noOfPos; ++i) {
			
			int[] idi = new int[alphabetSize + 2]; // Two more elements.
			
			// For every residue state.
			for (int j = 0; j < alphabetSize; ++j) {
				idi[j] = lcid[i][j] + rcid[i][j];
				idi[alphabetSize] += lcid[i][j] * rcid[i][j];
			}
			idi[alphabetSize + 1] = lcid[i][alphabetSize + 1] + rcid[i][alphabetSize + 1];
			id[i] = idi;
		}
		this.ids[x] = id;
	}
	
	/**
	 * Returns the percent identity for a vertex and position as a weighted average of all residues at
	 * that position. 1 is returned for leaves.
	 * @param x the vertex.
	 * @param pos the position.
	 * @return the percent ID.
	 */
	public double getPercentIdentity(int x, int pos) {
		if (this.tree.isLeaf(x)) {
			return 1.0;
		}
		// Weighted ID count of vertex divided by all possible character assignemnts of its children.
		return (this.ids[x][pos][alphabetSize] / (double) (this.ids[tree.getLeftChild(x)][pos][alphabetSize+1] * this.ids[tree.getRightChild(x)][pos][alphabetSize+1]));
	}
	
	/**
	 * Returns the percent identity for a vertex as an average over all positions.
	 * @param x the vertex.
	 * @return the percent ID.
	 */
	public double getPercentIdentity(int x) {
		double tot = 0;
		for (int i = 0; i < this.noOfPos; ++i) {
			tot += this.getPercentIdentity(x, i);
		}
		return (tot / this.noOfPos);
	}
	
	/**
	 * Returns a map of branch lengths with values according to percent identity of the parent
	 * vertex. 1 is by definition used for the root.
	 * @return the branch lengths.
	 */
	public DoubleMap getPercentIdentityLengths() {
		DoubleMap bl = new DoubleMap("PercentIdentityLengths", this.tree.getNoOfVertices());
		for (int x = 0; x < this.tree.getNoOfVertices(); ++x) {
			if (tree.isRoot(x)) {
				bl.set(x, 1.0);
			} else {
				double pid = this.getPercentIdentity(this.tree.getParent(x));
				bl.set(x, pid);
			}
		}
		return bl;
	}
	
}
