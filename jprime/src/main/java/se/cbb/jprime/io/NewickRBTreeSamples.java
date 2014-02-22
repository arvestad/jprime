package se.cbb.jprime.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TopologyException;

/**
 * Reads a column of Newick trees from a tab-delimited file (typically an output file from
 * MCMC), either matching a tree with or without branch lengths.
 * Typically used in conjunction with <code>RBTreeBranchSwapperSampler</code>.
 * Trees can be obtained sorted according to topology frequency.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickRBTreeSamples {
	
	/** Inner class for counting identical topologies and keeping track of "lengthses". */
	class TreeInstances implements Comparable<TreeInstances> {
		RBTree tree;
		String nwTopology;
		int count;
		ArrayList<DoubleMap> lengthses;
		
		public TreeInstances(RBTree tree, String nwTopology, DoubleMap lengths) {
			this.tree = tree;
			this.nwTopology = nwTopology;
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
	private LinkedHashMap<String, TreeInstances> trees;
	
	/** Tree instances sorted by descending frequency. */
	private ArrayList<TreeInstances> treesByFreq;
	
	/** Tree instances, hashed by purified Newick string. */
	private ArrayList<String> MAPTreeSampleIDs;
	
	/** The total no. of instances. */
	private int totalCount;
	
	/** Lengths flag. */
	private boolean hasLengths;
	
	/** Template NamesMap */
	private NamesMap templateNamesMap;
	
	/**
	 * Private constructor.
	 * @param f file.
	 * @param withLengths true if trees with lengths.
	 * @param absColIdx absolute column index.
	 * @param firstLn row index of first sample (e.g 1 to discard header).
	 * @param minCvg minimum coverage for a topology to be included among the samples, e.g. 0.01.
	 * @throws NewickIOException.
	 * @throws TopologyException.
	 * @throws FileNotFoundException.
	 */
	private NewickRBTreeSamples(File f, boolean withLengths, int absColIdx, int firstLn, double minCvg) throws FileNotFoundException, NewickIOException, TopologyException {
		this.hasLengths = withLengths;
		
		Scanner sc = new Scanner(f);
		int i = 0;
		while (i < firstLn) {
			sc.nextLine();
			i++;
		}
		
		this.trees = new LinkedHashMap<String, NewickRBTreeSamples.TreeInstances>(4096);
		
		// Read tree instances.
		i = 0;
		while (sc.hasNextLine()) {
			String[] parts = sc.nextLine().split("\t");
			i++;
			// Must sort to ensure equal numbering of identical trees.
			NewickTree t = NewickTreeReader.readTree(parts[absColIdx], true);
			DoubleMap lengths = null;
			if (withLengths) {
				lengths = t.getBranchLengthsMap("Lengths");
			}
			// We want to hash on "pure" Newick tree.
			t.clearBranchLengths();
			t.clearMeta();
			RBTree rbt = new RBTree(t, "Dummy");
			NamesMap rbtNamesMap = t.getVertexNamesMap(true, "Dummy");
			String nw = t.toString();
			TreeInstances ts = this.trees.get(nw);
			if (ts == null) {
				ts = new TreeInstances(rbt, nw, lengths);
				this.trees.put(nw, ts);
			} else {
				ts.add(lengths);
			}
			if (i == 1) {
				// Initialize the template
				this.templateNamesMap = t.getVertexNamesMap(true, "Template");
			} else {
				// Change the numbering of the current tree according to the template
				this.renumberTree(rbt, rbtNamesMap);
			}
		}
		sc.close();
		this.totalCount = i;
		
		// Sort trees according to topology frequency.
		this.treesByFreq = new ArrayList<NewickRBTreeSamples.TreeInstances>(this.trees.values());
		Collections.sort(this.treesByFreq, Collections.reverseOrder());
		
		sc.reset();
		i=0;
		sc = new Scanner(f);
		while (i < firstLn) {
			sc.nextLine();
			
			
			i++;
		}
		i=0;
		int count =0; // Count the count of MAP gene trees
		MAPTreeSampleIDs = new ArrayList<String>();;
		while (sc.hasNextLine()) {
			//System.out.println("MAP Trees are :" + this.getTreeCount(0) + "\n" + "gene tree looks like :" + this.getTreeNewickString(0));
			String[] parts = sc.nextLine().split("\t");
			i++;
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
			
			if(this.getTreeNewickString(0) == (ts.nwTopology))
			{
				MAPTreeSampleIDs.add(parts[0]);
				count=count+1;
			}
			
		}
		sc.close();
		
		// Remove samples not meeting with the coverage requirements.
		while (this.treesByFreq.get(this.treesByFreq.size() - 1).count / (double) this.totalCount < minCvg) {
			TreeInstances t = this.treesByFreq.get(this.treesByFreq.size() - 1);
			this.treesByFreq.remove(this.treesByFreq.size() - 1);
			this.trees.remove(t.tree.toString());
			this.totalCount -= t.count;
		}
	}
	
	/**
	 * Returns the trees from a column, where trees are expected to lack lengths.
	 * @param f the file.
	 * @param hasHeader true if header; false if none.
	 * @param relColNo the relative column number containing trees without lengths, e.g., 1
	 * if the first encountered column with Newick trees without lengths is the desired one.
	 * @param burnInProp proportion of samples to discard as burn-in, e.g. 0.25 for 25%.
	 * @param minCvg minimum coverage for a topology to be included among the samples, e.g. 0.01.
	 * @return the trees of the column.
	 * @throws FileNotFoundException.
	 * @throws NewickIOException.
	 * @throws TopologyException.
	 */
	public static NewickRBTreeSamples readTreesWithoutLengths(File f, boolean hasHeader, int relColNo, double burnInProp, double minCvg) throws FileNotFoundException, NewickIOException, TopologyException {
		int[] colStart = findAbsColAndStartLn(f, hasHeader, relColNo, burnInProp, false);
		return new NewickRBTreeSamples(f, false, colStart[0], colStart[1], minCvg);
	}
	
	/**
	 * Returns the trees from a column, where trees are expected to have lengths.
	 * @param f the file.
	 * @param hasHeader true if header; false if none.
	 * @param relColNo the relative column number containing trees with lengths, e.g., 1
	 * if the first encountered column with Newick trees with lengths is the desired one.
	 * @param burnInProp proportion of samples to discard as burn-in, e.g. 0.25 for 25%.
	 * @param minCvg minimum coverage for a topology to be included among the samples, e.g. 0.01.
	 * @return the trees of the column.
	 * @throws FileNotFoundException.
	 * @throws NewickIOException.
	 * @throws TopologyException.
	 */
	public static NewickRBTreeSamples readTreesWithLengths(File f, boolean hasHeader, int relColNo, double burnInProp, double minCvg) throws FileNotFoundException, NewickIOException, TopologyException {
		int[] colStart = findAbsColAndStartLn(f, hasHeader, relColNo, burnInProp, true);
		return new NewickRBTreeSamples(f, true, colStart[0], colStart[1], minCvg);
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
		String[] parts = ln.split("\t");       // TODO: This could be made into a default-only in the future.
		int matchCols = 0;
		for (int i = 0; i < parts.length; ++i) {
			try {
				String part = parts[i];
				NewickTree t = NewickTreeReader.readTree(part, false);
				if ((t.getBranchLengths() != null) == withLengths && !part.matches("-?\\d+(.\\d+)?")) {
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
	 * @return the number of trees. See also <code>getTotalTreeCount()</code>.
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
	
	/**
	 * Returns the total number of tree instances, i.e., accounting for
	 * the multiplicity of each unique topology. See also <code>getNoOfTrees()</code>.
	 * @return the total number of samples.
	 */
	public int getTotalTreeCount() {
		return this.totalCount;
	}

	/**
	 * Returns the Newick string topology representation of a tree.
	 * @param i the index.
	 * @return the Newick string topology representation of a tree.
	 */
	public String getTreeNewickString(int i) {
		return this.treesByFreq.get(i).nwTopology;
	}
	
	/**
	 * Returns the template names map.
	 * @return the template names map.
	 */
	public NamesMap getTemplateNamesMap() {
		return this.templateNamesMap;
	}
	
	/**
	 * Returns true if lengths are included with the tree samples.
	 * @return true if lengths included; false if not included.
	 */
	public boolean hasLengths() {
		return this.hasLengths;
	}
	
	/**
	 * Returns MAP tree sample IDs.
	 * @return List of sample IDs containing MAP tree.
	 */
	public ArrayList<String> getMAPTreeSampleIDs() {
		return this.MAPTreeSampleIDs;
	}
	
	/**
	 * Renumber a RBTree so that its vertex leaf numbers are consistent with
	 * the template.
	 * @param t tree to renumber.
	 * @param nm tree t names map.
	 */
	private void renumberTree(RBTree t, NamesMap nm) {
		for (String leafName : this.templateNamesMap.getNames(false)) {
			int templateNum = this.templateNamesMap.getVertex(leafName);
			int currentNum = nm.getVertex(leafName);
			if (templateNum != currentNum) {
				t.swap(currentNum, templateNum);
				// Update NamesMap according to the swapping.
				if (nm.get(templateNum) != null) {
					nm.swapVertices(leafName, nm.get(templateNum));
				} else {
					nm.changeVertex(leafName, templateNum);
				}
			}
		}
	}
}
