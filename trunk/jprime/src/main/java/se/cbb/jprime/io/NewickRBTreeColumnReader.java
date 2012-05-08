package se.cbb.jprime.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TopologyException;

/**
 * Reads a column of Newick trees from a tab-delimited file (typically an output file from
 * MCMC), either matching a tree without branch lengths or with branch lengths.
 * Typically used in conjunction with <code>ConditionedRBTreeBranchSwapper</code>.
 * Trees can be obtained sorted according to topology frequency.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickRBTreeColumnReader {
	
	/** Inner class for counting identical topologies and keeping track of lengths. */
	class TreeInstances implements Comparable<TreeInstances> {
		RBTree tree;
		int count;
		ArrayList<DoubleMap> lengthses;
		
		public TreeInstances(RBTree tree, DoubleMap lengths) {
			this.tree = tree;
			this.count = 1;
			if (lengths != null) {
				this.lengthses = new ArrayList<DoubleMap>();
				this.lengthses.add(lengths);
			}
		}
		
		public void add(DoubleMap lengths) {
			this.count++;
			if (lengths != null) {
				this.lengthses.add(lengths);
			}
		}

		@Override
		public int compareTo(TreeInstances o) {
			return (new Integer(this.count)).compareTo(o.count);
		}
	}
	
	/** Tree instances, hashed by purified Newick string. */
	LinkedHashMap<String, TreeInstances> trees;
	
	/** Tree instances sorted by descending frequency. */
	ArrayList<TreeInstances> treesByFreq;
	
	/**
	 * Private constructor.
	 * @param f
	 * @param withLengths
	 * @param absColIdx
	 * @param firstLn
	 * @throws NewickIOException.
	 * @throws TopologyException.
	 * @throws FileNotFoundException.
	 */
	private NewickRBTreeColumnReader(File f, boolean withLengths, int absColIdx, int firstLn) throws FileNotFoundException, NewickIOException, TopologyException {
		Scanner sc = new Scanner(f);
		int lnNo = 0;
		while (lnNo < firstLn) {
			sc.nextLine();
			lnNo++;
		}
		
		this.trees = new LinkedHashMap<String, NewickRBTreeColumnReader.TreeInstances>(4096);
		
		// Read tree instances.
		while (sc.hasNextLine()) {
			String[] parts = sc.nextLine().split("\t");
			// Must sort to ensure equal numbering of identical trees.
			NewickTree t = NewickTreeReader.readTree(parts[absColIdx], true);
			DoubleMap lengths = null;
			if (withLengths) {
				lengths = t.getBranchLengthsMap("Lengths");
			}
			// We want to hash on "pure" Newick tree.
			t.clearBranchLengths();
			t.clearMeta();
			String nw = t.toString();
			TreeInstances ts = this.trees.get(nw);
			if (ts == null) {
				ts = new TreeInstances(new RBTree(t, "Dummy"), lengths);
				this.trees.put(nw, ts);
			} else {
				ts.add(lengths);
			}
		}
		sc.close();
		
		// Sort trees according to topology frequency.
		this.treesByFreq = new ArrayList<NewickRBTreeColumnReader.TreeInstances>(this.trees.values());
		Collections.sort(this.treesByFreq, Collections.reverseOrder());
	}
	
	public static NewickRBTreeColumnReader readTreesWithoutLengths(File f, boolean hasHeader, int relColNo, double burnInProp) throws FileNotFoundException, NewickIOException, TopologyException {
		int[] colStart = findAbsColAndStartLn(f, hasHeader, relColNo, burnInProp, false);
		return new NewickRBTreeColumnReader(f, false, colStart[0], colStart[1]);
	}
	
	public static NewickRBTreeColumnReader readTreesWithLengths(File f, boolean hasHeader, int relColNo, double burnInProp) throws FileNotFoundException, NewickIOException, TopologyException {
		int[] colStart = findAbsColAndStartLn(f, hasHeader, relColNo, burnInProp, true);
		return new NewickRBTreeColumnReader(f, true, colStart[0], colStart[1]);
	}
	
	/**
	 * Retrieves absolute column index and absolute start line of input file.
	 * @param f file.
	 * @param hasHeader true if header in file; false if none.
	 * @param relColNo sought-after matching column number (1,...).
	 * @param burnInProp burn-in proportion to discard.
	 * @param withLengths true if lengths present; false if no lengths.
	 * @return absolute column index and absolute start line.
	 * @throws FileNotFoundException.
	 */
	private static int[] findAbsColAndStartLn(File f, boolean hasHeader, int relColNo, double burnInProp, boolean withLengths)
	throws FileNotFoundException {
		if (relColNo < 1) {
			throw new IllegalArgumentException("Relative column number must be 1 or greater.");
		}
		if (burnInProp < 0.0 || burnInProp > 1.0) {
			throw new IllegalArgumentException("Burn-in proportion must be in [0.0,1.0].");
		}
		int[] colStart = new int[] {-1, -1};
		Scanner sc = new Scanner(f);
		if (hasHeader) {
			sc.nextLine();  // Not counted (yet).
		}
		String ln = sc.nextLine();
		int lnCnt = 1;
		
		// Find absolute column index.
		String[] parts = ln.split("\t");       // TODO: This could be made into a default only in the future.
		int matchCols = 0;
		for (int i = 0; i < parts.length; ++i) {
			try {
				NewickTree t = NewickTreeReader.readTree(parts[i], false);
				if ((t.getBranchLengths() != null) == withLengths) {
					matchCols++;
				}
			} catch (Exception e) {
			}
			if (matchCols == relColNo) {
				colStart[0] = i;      // i is the column we were looking for.
				break;
			}
		}
		if (matchCols < relColNo) {
			throw new IllegalArgumentException("Could not find sufficient number of column(s) with Newick trees in input file.");
		}
		
		// Find start line.
		while (sc.hasNextLine()) {
			sc.nextLine();
			lnCnt++;
		}
		colStart[1] = (hasHeader ? 1 : 0) + (int) (Math.round(lnCnt * burnInProp));
		sc.close();
		return colStart;
	}
	
	/**
	 * Returns the number of unique tree topologies.
	 * @return the number of trees.
	 */
	public int getNoOfTrees() {
		return this.treesByFreq.size();
	}
	
	/**
	 * Returns a tree ordered by tree frequency, where index 0 is the most common topology.
	 * @param i the index.
	 * @return the tree.
	 */
	public RBTree getTree(int i) {
		return this.treesByFreq.get(i).tree;
	}
	
	/**
	 * Returns the number of instances of a tree, ordered by tree frequency, where index 0 is the most common topology.
	 * @param i the index.
	 * @return the number of occurrences of tree i.
	 */
	public int getTreeCount(int i) {
		return this.treesByFreq.get(i).count;
	}
	
	/**
	 * Returns the branch length instances of a tree, ordered by tree frequency, where index 0 is the most common topology.
	 * May be null.
	 * @param i the index.
	 * @return the branch length occurrences of tree i.
	 */
	public List<DoubleMap> getTreeBranchLengths(int i) {
		return this.treesByFreq.get(i).lengthses;
	}
}
