package se.cbb.jprime.consensus.day;

import java.util.ArrayList;
import java.util.LinkedList;

import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.RootedTree;

/**
 * Implementation of a <code>PSWTree</code> where leaf labels
 * are based on a template (a <code>ClusterTablePSWTree</code>).
 * The purpose of this class is to be able to make quick comparisons between a
 * tree and a template tree.
 * 
 * @author Joel Sjöstrand.
 */
public class TemplatedPSWTree extends PSWTree {

	/** The template to which this templated tree refers. */
	private ClusterTablePSWTree template;
	
	/**
	 * Constructor. Labels the leaves based on an existing template instance,
	 * then computes PSW. Treatment as rooted/unrooted is deduced from
	 * template.
	 * @param tree the input tree (is not manipulated).
	 * @param names the leaf names, indexed w.r.t. the input tree; must
	 * be compatible with template.
	 * @param template the template DayTree.
	 */
	public TemplatedPSWTree(RootedTree tree, StringMap names, ClusterTablePSWTree template) {
		super();
		this.template = template;
		
		// Duplicate tree intact.
		this.root = duplicateTree(tree, names, tree.getRoot());
		
		// Reroot in case template was rerooted.
		if (template.rerootName != null) {
			this.rerootName = template.rerootName;
			LinkedList<PSWVertex> q = new LinkedList<PSWVertex>();
			q.add(this.root);
			while (true) {
				PSWVertex v = q.pop();
				if (v.isLeaf()) {
					if (v.getName().equals(this.rerootName)) {
						reroot(v);
						break;
					}
				} else {
					for (PSWVertex c : v.getChildren()) {
						q.add(c);
					}
				}
			}
		}
		
		// Label and compute PSW for the tree, etc.
		this.verticesPostordered = new ArrayList<PSWVertex>(tree.getNoOfVertices());
		labelTreeFromTemplate(this.root);
		this.root.computePSW();
	}
	
	/**
	 * Constructor. Labels the leaves based on an existing template instance,
	 * then computes PSW. The input tree is here just duplicated from an existing cluster
	 * tree. If the input tree and the template has been rerooted at different
	 * vertices, an exception is thrown.
	 * @param tree the input tree to duplicate (is not manipulated).
	 * @param template the template DayTree.
	 * @throws Exception when input tree and template have been rerooted at different leaves.
	 */
	public TemplatedPSWTree(ClusterTablePSWTree tree, ClusterTablePSWTree template) {
		super();
		this.template = template;
		if (tree.rerootName == null && template.rerootName != null ||
				tree.rerootName != null && template.rerootName == null ||
				(tree.rerootName != null && !tree.rerootName.equals(template.rerootName))) {
			throw new IllegalArgumentException("Cannot create TemplatedPSWTree when input tree and template tree " +
					"have been rerooted at different leaves.");
		}
		this.rerootName = template.rerootName;
		this.root = new PSWVertex(tree.root);
		this.verticesPostordered = new ArrayList<PSWVertex>(tree.getNoOfVertices());
		labelTreeFromTemplate(this.root);
		this.root.computePSW();
	}
	
	/**
	 * Makes a post-order traversal of the tree, where:
	 * <ul>
	 * <li>leaves are labelled 0,1,... according to the name-number map of the template.</li>
	 * <li>all vertices are stored in a list in the order in which they were visited.</li>
	 * </ul>
	 * @param v the root of the subtree to process.
	 */
	private void labelTreeFromTemplate(PSWVertex v) {
		if (v.isLeaf()) {
			v.setNumber(this.template.getNumber(v.getName()));
		} else {
			for (PSWVertex c : v.getChildren()) {
				labelTreeFromTemplate(c);
			}
		}
		this.verticesPostordered.add(v);
	}
	
	/**
	 * Returns the template on which this instance is based.
	 * @return
	 */
	public ClusterTablePSWTree getTemplate() {
		return this.template;
	}
	
}
