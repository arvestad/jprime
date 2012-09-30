package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.SampleNewickTree;
import se.cbb.jprime.misc.IntQueue;

/**
 * Implementation of a rooted binary tree topology (the name does not refer to the data structure <i>red-black tree</i>!)
 * Internally, parents, left and right children are stored in arrays indexed
 * by vertex number. Null references are indicated as in its interface.
 * <p/>
 * Data such as leaf names, branch lengths, etc. are stored elsewhere.
 * Completely empty trees are not allowed.
 * 
 * @author Joel Sjöstrand.
 */
public class RBTree implements RootedTreeParameter, RootedBifurcatingTreeParameter {
	
	/** Used to indicate null references. */
	public static final int NULL = RootedBifurcatingTree.NULL;
	
	/** Name. */
	protected String name;
	
	/** Parents. Note: parents[root] == NULL. */
	protected int[] parents;
	
	/** Left children. Note: leftChildren[leaf] == NULL. */
	protected int[] leftChildren;
	
	/** Right children. Note: rightChildren[leaf] == NULL. */
	protected int[] rightChildren;
	
	/** For quick reference, the root index is stored explicitly. */
	protected int root;
	
	/** Cache. */
	protected RBTree cache = null;
	
	/**
	 * Constructor. Creates a rooted tree from a Newick tree.
	 * The input tree is required to be bifurcating, not empty, and have
	 * vertices numbered from 0 to |V(T)|-1.
	 * @param tree the Newick tree to base the topology on.
	 * @param name the name of the tree parameter.
	 */
	public RBTree(NewickTree tree, String name) throws TopologyException {
		this.name = name;
		int k = tree.getNoOfVertices();
		this.parents = new int[k];
		this.leftChildren = new int[k];
		this.rightChildren = new int[k];
		NewickVertex root = tree.getRoot();
		if (root == null)
			throw new TopologyException("Cannot create RBTree from empty NewickTree.");
		if (!tree.isBifurcating())
			throw new TopologyException("Cannot create RBTree from non-bifurcating NewickTree.");
		List<NewickVertex> vertices = tree.getVerticesAsList();
		for (NewickVertex v : vertices) {
			int i = v.getNumber();
			if (v.isRoot()) {
				this.parents[i] = NULL;
				this.root = i;
			} else {
				this.parents[i] = v.getParent().getNumber();
			}
			if (v.isLeaf()) {
				this.leftChildren[i] = NULL;
				this.rightChildren[i] = NULL;
			} else {
				this.leftChildren[i] = v.getChildren().get(0).getNumber();
				this.rightChildren[i] = v.getChildren().get(1).getNumber();
			}
		}
	}
	
	/**
	 * Low-level constructor. Initialises all vertex references to NULL.
	 * @param name the name of the tree parameter.
	 * @param noOfLeaves the number of leaves.
	 */
	public RBTree(String name, int noOfLeaves) {
		this.name = name;
		int k = 2 * noOfLeaves - 1;
		this.parents = new int[k];
		this.leftChildren = new int[k];
		this.rightChildren = new int[k];
		for (int i = 0; i < k; ++i) {
			this.parents[i] = NULL;
			this.leftChildren[i] = NULL;
			this.rightChildren[i] = NULL;
		}
		this.root = NULL;
	}
	
	/**
	 * Copy-constructor.
	 * @param tree the tree to copy.
	 */
	public RBTree(RBTree tree) {
		this.name = tree.name;
		this.parents = new int[tree.parents.length];
		System.arraycopy(tree.parents, 0, this.parents, 0, tree.parents.length);
		this.leftChildren = new int[tree.leftChildren.length];
		System.arraycopy(tree.leftChildren, 0, this.leftChildren, 0, tree.leftChildren.length);
		this.rightChildren = new int[tree.rightChildren.length];
		System.arraycopy(tree.rightChildren, 0, this.rightChildren, 0, tree.rightChildren.length);
		this.root = tree.root;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int getRoot() {
		return this.root;
	}

	@Override
	public List<Integer> getChildren(int x) {
		if (this.leftChildren[x] == NULL) {
			return new ArrayList<Integer>(0);
		}
		ArrayList<Integer> ch = new ArrayList<Integer>(2);
		ch.add(this.leftChildren[x]);
		ch.add(this.rightChildren[x]);
		return ch;
	}

	@Override
	public List<Integer> getDescendants(int x, boolean properOnly) {
		if (this.leftChildren[x] == NULL) {
			if (properOnly) {
				return new ArrayList<Integer>(0);
			}
			ArrayList<Integer> desc = new ArrayList<Integer>(1);
			desc.add(x);
			return desc;
		}
		ArrayList<Integer> desc = new ArrayList<Integer>();
		if (properOnly) {
			desc.add(this.leftChildren[x]);
			desc.add(this.rightChildren[x]);
		}
		else {
			desc.add(x);
		}
		for (int i = 0; i < desc.size(); ++i) {
			x = desc.get(i);
			if (this.leftChildren[x] != NULL) {
				desc.add(this.leftChildren[x]);
				desc.add(this.rightChildren[x]);
			}
		}
		return desc;
	}
	
	@Override
	public int getNoOfDescendants(int x, boolean properOnly) {
		if (this.leftChildren[x] == NULL)
			return (properOnly ? 0 : 1);
		ArrayList<Integer> list = new ArrayList<Integer>();
		if (properOnly) {
			list.add(this.leftChildren[x]);
			list.add(this.rightChildren[x]);
		}
		else {
			list.add(x);
		}
		for (int i = 0; i < list.size(); ++i) {
			x = list.get(i);
			if (this.leftChildren[x] != NULL) {
				list.add(this.leftChildren[x]);
				list.add(this.rightChildren[x]);
			}
		}
		return list.size();
	}
	
	@Override
	public int getParent(int x) {
		return this.parents[x];
	}

	@Override
	public List<Integer> getAncestors(int x, boolean properOnly) {
		ArrayList<Integer> anc = new ArrayList<Integer>();
		if (properOnly) { x = this.parents[x]; }
		while (x != NULL) {
			anc.add(x);
			x = this.parents[x];
		}
		return anc;
	}

	@Override
	public int getNoOfAncestors(int x, boolean properOnly) {
		if (properOnly) {
			x = this.parents[x];
		}
		int count = 0;
		while (x != NULL) {
			++count;
			x = this.parents[x];
		}
		return count;
	}
	
	@Override
	public List<Integer> getLeaves() {
		int n = this.parents.length;
		ArrayList<Integer> leaves = new ArrayList<Integer>((n + 1) / 2);
		for (int i = 0; i < n; ++i) {
			if (this.leftChildren[i] == NULL) {
				leaves.add(i);
			}
		}
		return leaves;
	}

	@Override
	public int getLCA(int x, int y) {
		// Very naïve implementation...
		ArrayList<Integer> visited = new ArrayList<Integer>();
		while (true) {
			if (x != NULL) {
				if (visited.contains(x)) { return x; }
				visited.add(x);
				x = this.parents[x];
			}
			if (y != NULL) {
				if (visited.contains(y)) { return y; }
				visited.add(y);
				y = this.parents[y];
			}
		}
	}

	@Override
	public boolean isRoot(int x) {
		return (this.parents[x] == NULL);
	}

	@Override
	public boolean isLeaf(int x) {
		return (this.leftChildren[x] == NULL);
	}

	@Override
	public int getNoOfVertices() {
		return this.parents.length;
	}

	@Override
	public int getNoOfComponents() {
		return 1;
	}

	@Override
	public boolean isStronglyConnected() {
		return true;
	}

	@Override
	public List< List<Integer> > getComponents() {
		ArrayList<Integer> vertices = new ArrayList<Integer>(this.parents.length);
		for (int i = 0; i < this.parents.length; ++i) {
			vertices.add(i);
		}
		ArrayList< List<Integer> > comp = new ArrayList< List<Integer> >(1);
		comp.add(vertices);
		return comp;
	}

	@Override
	public List<Integer> getSources() {
		ArrayList<Integer> sources = new ArrayList<Integer>(1);
		sources.add(this.root);
		return sources;
	}

	@Override
	public List<Integer> getSinks() {
		return this.getLeaves();
	}

	@Override
	public boolean hasArc(int x, int y) {
		return (this.parents[y] == x);
	}

	@Override
	public boolean hasPath(int x, int y) {
		do {
			y = this.parents[y];
			if (x == y) { return true; }
		} while (y != NULL);
		return false;
	}

	@Override
	public List<Integer> getDirectSuccessors(int x) {
		return this.getChildren(x);
	}

	@Override
	public List<Integer> getSuccessors(int x) {
		return this.getDescendants(x, true);
	}

	@Override
	public int getLeftChild(int x) {
		return this.leftChildren[x];
	}

	@Override
	public int getRightChild(int x) {
		return this.rightChildren[x];
	}

	@Override
	public int getNoOfChildren(int x) {
		return (this.leftChildren[x] == NULL ? 0 : 2);
	}
	
	@Override
	public int getNoOfLeaves() {
		return ((this.parents.length + 1) / 2);
	}

	@Override
	public int getNoOfSources() {
		return 1;
	}

	@Override
	public int getNoOfSinks() {
		return this.getNoOfLeaves();
	}

	@Override
	public int getNoOfDirectSuccessors(int x) {
		return this.getNoOfChildren(x);
	}

	@Override
	public int getNoOfSuccessors(int x) {
		return this.getNoOfDescendants(x, true);
	}

	@Override
	public int getHeight() {
		return getHeight(this.root);
	}
	
	@Override
	public int getHeight(int x) {
		if (this.leftChildren[x] == NULL)
			return 0;
		return (1 + Math.max(getHeight(this.leftChildren[x]), getHeight(this.rightChildren[x])));
	}

	@Override
	public List<Integer> getDescendantLeaves(int x, boolean properOnly) {
		List<Integer> desc = this.getDescendants(x, properOnly);
		ArrayList<Integer> descLeaves = new ArrayList<Integer>((desc.size() + 2) / 2);
		for (Integer i : desc) {
			if (this.isLeaf(i)) {
				descLeaves.add(i);
			}
		}
		return descLeaves;
	}

	@Override
	public int getNoOfDescendantLeaves(int x, boolean properOnly) {
		if (this.isLeaf(x)) {
			return (properOnly ? 0 : 1);
		}
		List<Integer> desc = this.getDescendants(x, properOnly);
		if (properOnly) {
			return ((desc.size() + 2) / 2);
		} else {
			return ((desc.size() + 1) / 2);
		}
	}

	@Override
	public List<Integer> getSuccessorSinks(int x) {
		return this.getDescendantLeaves(x, true);
	}

	@Override
	public int getNoOfSuccessorSinks(int x) {
		return this.getNoOfDescendantLeaves(x, true);
	}

	@Override
	public boolean isSource(int x) {
		return this.isRoot(x);
	}

	@Override
	public boolean isSink(int x) {
		return this.isLeaf(x);
	}

	@Override
	public int getNoOfSubParameters() {
		return 1;
	}
	
	/**
	 * Caches the whole current tree. May e.g. be used by a <code>Proposer</code>.
	 */
	public void cache() {
		this.cache = new RBTree(this);
	}

	/**
	 * Clears the cached tree. May e.g. be used by a <code>Proposer</code>.
	 */
	public void clearCache() {
		this.cache = null;
	}

	/**
	 * Replaces the current tree with the cached tree, and clears the latter.
	 * May e.g. be used by a <code>Proposer</code>.
	 */
	public void restoreCache() {
		this.parents = this.cache.parents;
		this.leftChildren = this.cache.leftChildren;
		this.rightChildren = this.cache.rightChildren;
		this.root = this.cache.root;
		this.cache = null;
	}

	@Override
	public Class<?> getSampleType() {
		return SampleNewickTree.class;
	}

	@Override
	public String getSampleHeader() {
		// Only prints internal vertex labels.
		// Use RBTreeSampleWrapper for proper output.
		return (this.name + "InternalLabels");
	}

	@Override
	public String getSampleValue() {
		// Only prints internal vertex labels.
		// Use RBTreeSampleWrapper for proper output.
		return this.toString();
	}

	
	@Override
	public String toString() {
		// Only prints internal vertex labels.
		// Use RBTreeSampleWrapper for proper output.
		StringBuilder sb = new StringBuilder(512);
		this.writeInternalNewickSubtree(sb, this.root);
		sb.append(';');
		return sb.toString();
	}
	
	/**
	 * Recursively writes a Newick tree with the internal integer vertex labels.
	 * @param sb string buffer to append to.
	 * @param x current vertex.
	 */
	private void writeInternalNewickSubtree(StringBuilder sb, int x) {
		if (!this.isLeaf(x)) {
			sb.append('(');
			this.writeInternalNewickSubtree(sb, this.leftChildren[x]);
			sb.append(',');
			this.writeInternalNewickSubtree(sb, this.rightChildren[x]);
			sb.append(')');
		}
		sb.append('v').append(x);
	}

	@Override
	public int getSibling(int x) {
		int p = this.parents[x];
		if (p == RBTree.NULL) {
			return RBTree.NULL;  // x is the root.
		}
		return (x == this.leftChildren[p] ? this.rightChildren[p] : this.leftChildren[p]);
	}
	
	/**
	 * Swap two vertices in the tree.
	 * @param i vertex number.
	 * @param j vertex number.
	 */
	public void swap(int i, int j) {
		if (this.root == i) {
			this.root = j;
		} else if (this.root == j) {
			this.root = i;
		}
		this.swapNumbers(i, j, this.parents);
		this.swapNumbers(i, j, this.leftChildren);
		this.swapNumbers(i, j, this.rightChildren);
		int[] iTmp = {this.parents[i], this.leftChildren[i], this.rightChildren[i]};
		this.parents[i] = this.parents[j];
		this.leftChildren[i] = this.leftChildren[j];
		this.rightChildren[i] = this.rightChildren[j];
		this.parents[j] = iTmp[0];
		this.leftChildren[j] = iTmp[1];
		this.rightChildren[j] = iTmp[2];
	}
	
	/**
	 * Swap the number i and j in a list.
	 * @param i number.
	 * @param j number.
	 * @param list list to swap.
	 */
	private void swapNumbers(int i, int j, int[] list) {
		for (int n = 0; n < list.length; n++) {
			if (list[n] == i) {
				list[n] = j;
			} else if(list[n] == j) {
				list[n] = i;
			}
		}
	}
	
	/**
	 * Low-level setter for internal parent and children arrays.
	 * @param p parent vertex.
	 * @param lc left child.
	 * @param rc right child.
	 */
	void setParentAndChildren(int p, int lc, int rc) {
		this.leftChildren[p] = lc;
		this.rightChildren[p] = rc;
		this.parents[lc] = p;
		this.parents[rc] = p;
	}
	
	/**
	 * Low-level setter for root.
	 * @param x new root vertex.
	 */
	void setRoot(int x) {
		this.root = x;
	}
	
	/**
	 * Low-level setter for copying the topology of another tree. At the moment only handles equally big trees.
	 * @param tree the tree from which the new topology is mimicked.
	 */
	void setTopology(RBTree tree) {
		System.arraycopy(tree.parents, 0, this.parents, 0, tree.parents.length);
		System.arraycopy(tree.leftChildren, 0, this.leftChildren, 0, tree.leftChildren.length);
		System.arraycopy(tree.rightChildren, 0, this.rightChildren, 0, tree.rightChildren.length);
		this.root = tree.root;
	}
	
	@Override
	public List<Integer> getTopologicalOrdering() {
		return this.getTopologicalOrdering(this.root);
	}
	
	@Override
	public List<Integer> getTopologicalOrdering(int source) {
		ArrayList<Integer> l = new ArrayList<Integer>(this.parents.length);
		IntQueue q = new IntQueue();
		q.put(source);
		while (!q.isEmpty()) {
			int x = q.get();
			l.add(x);
			if (!this.isLeaf(x)) {
				q.put(this.leftChildren[x]);
				q.put(this.rightChildren[x]);
			}
		}
		return l;
	}
}
