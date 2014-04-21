package se.cbb.jprime.io;

import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;

/**
 * Wrapper for combining a RBTree with names and possibly lengths into Newick output.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTreeSampleWrapper implements Sampleable {

	/** Tree. */
	private RBTree tree;
	
	/** Tree's names. */
	private NamesMap names;
	
	/** Tree's branch lengths. */
	private DoubleMap lengths;
	
	/** Tree's switches. */
	private DoubleMap switches;
	
	/**
	 * Constructor.
	 * @param tree the tree.
	 * @param names the names of the tree.
	 */
	public RBTreeSampleWrapper(RBTree tree, NamesMap names) {
		this(tree, names, null);
	}
	
	/**
	 * Constructor.
	 * @param tree the tree.
	 * @param names the names of the tree.
	 * @param lengths the branch lengths of the tree.
	 */
	public RBTreeSampleWrapper(RBTree tree, NamesMap names, DoubleMap lengths) {
		this.tree = tree;
		this.names = names;
		this.lengths = lengths;
	}
	
	/**
	 * Constructor.
	 * @param tree the tree.
	 * @param names the names of the tree.
	 * @param lengths the branch lengths of the tree.
	 * @param pseudogenizatoin switches of the tree.
	 */
	public RBTreeSampleWrapper(RBTree tree, NamesMap names, DoubleMap lengths, DoubleMap switches) {
		this.tree = tree;
		this.names = names;
		this.lengths = lengths;
		this.switches = switches;
	}
	
	@Override
	public Class<?> getSampleType() {
		return SampleNewickTree.class;
	}

	@Override
	public String getSampleHeader() {
		return (this.lengths == null ? this.tree.getName() : this.tree.getName() + "And" + this.lengths.getName());
	}

	@Override
	public String getSampleValue(SamplingMode mode) {
		try {
			if (this.lengths == null && this.switches == null) {
				return SampleNewickTree.toString(tree, names);
			}
//			else
//			if(this.switches != null){
//				return SampleNewickTree.toString(tree, names, lengths, switches);
//			}
			return SampleNewickTree.toString(tree, names, lengths);
		} catch (NewickIOException ex) {
			throw new RuntimeException("Could not assemble Newick tree from RBTree.");
		}
	}

}
