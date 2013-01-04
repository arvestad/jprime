package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.SampleNewickTree;
import se.cbb.jprime.misc.IntQueue;

/**
 * Implementation of a rooted multifurcating tree topology where parents,
 * and children are stored in arrays indexed
 * by vertex number (the children of a specified vertex are, in turn, held as an array
 * in no particular order).
 * Null references are indicated as in its interface.
 * <p/>
 * Data such as leaf names, branch lengths, etc. are stored elsewhere, in maps.
 * Completely empty trees are not allowed, nor are trees with vertices that may
 * be collapsed.
 * 
 * @author Joel Sjöstrand.
 */
public class RTree implements RootedTreeParameter {

	/** Used to indicate null references. */
	public static final int NULL = RootedTree.NULL;
	
	/** Name. */
	protected String name;
	
	/** Parents. Note: parents[root] == NULL. */
	protected int[] parents;
	
	/** Children. Note: children[x][k] accesses child number k of vertex x. Children have empty lists, not null. */
	protected int[][] children;
	
	/** For quick reference, the root index is stored explicitly. */
	protected int root;
	
	/** Cache. */
	protected RTree cache = null;
	
	/**
	 * Constructor. Creates a rooted tree from a Newick tree.
	 * The input tree is required to be "uncollapsable", not empty, and have
	 * vertices numbered from 0 to |V(T)|-1.
	 * @param tree the Newick tree to base the topology on.
	 * @param name the name of the tree parameter.
	 */
	public RTree(NewickTree tree, String name) throws TopologyException {
		this.name = name;
		int k = tree.getNoOfVertices();
		this.parents = new int[k];
		this.children = new int[k][];
		NewickVertex root = tree.getRoot();
		if (root == null)
			throw new TopologyException("Cannot create RTree from empty NewickTree.");
		if (tree.isCollapsable())
			throw new TopologyException("Cannot create RTree from collapsable NewickTree.");
		List<NewickVertex> vertices = tree.getVerticesAsList();
		for (NewickVertex v : vertices) {
			int i = v.getNumber();
			
			// Parent relationships.
			if (v.isRoot()) {
				this.parents[i] = NULL;
				this.root = i;
			} else {
				this.parents[i] = v.getParent().getNumber();
			}
			
			// Child relationships.
			if (v.isLeaf()) {
				this.children[i] = new int[0];
			} else {
				ArrayList<NewickVertex> ch = v.getChildren();
				this.children[i] = new int[ch.size()];
				for (int j = 0; j < this.children[i].length; ++j) {
					this.children[i][j] = ch.get(j).getNumber();
				}
			}
		}
	}
	
	/**
	 * Copy-constructor.
	 * @param tree the tree to copy.
	 */
	public RTree(RTree tree) {
		this.name = tree.name;
		this.parents = new int[tree.parents.length];
		System.arraycopy(tree.parents, 0, this.parents, 0, tree.parents.length);
		this.children = new int[tree.children.length][];
		for (int i = 0; i < tree.children.length; ++i) {
			int sz = tree.children[i].length;
			this.children[i] = new int[sz];
			System.arraycopy(tree.children[i], 0, this.children[i], 0, sz);
		}
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
	public int getNoOfVertices() {
		return (this.parents.length);
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
	public int getNoOfSources() {
		return 1;
	}

	@Override
	public List<Integer> getSinks() {
		return this.getLeaves();
	}

	@Override
	public int getNoOfSinks() {
		return this.getNoOfLeaves();
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
	public int getNoOfDirectSuccessors(int x) {
		return this.getNoOfChildren(x);
	}

	@Override
	public List<Integer> getSuccessors(int x) {
		return this.getDescendants(x, true);
	}

	@Override
	public int getNoOfSuccessors(int x) {
		return this.getNoOfDescendants(x, true);
	}

	@Override
	public int getRoot() {
		return this.root;
	}

	@Override
	public List<Integer> getChildren(int x) {
		ArrayList<Integer> ch = new ArrayList<Integer>(this.children[x].length);
		for (int v : this.children[x]) {
			ch.add(v);
		}
		return ch;
	}

	@Override
	public int getNoOfChildren(int x) {
		return this.children[x].length;
	}

	@Override
	public List<Integer> getDescendants(int x, boolean properOnly) {
		if (this.children[x].length == 0) {
			if (properOnly) {
				return new ArrayList<Integer>(0);
			}
			ArrayList<Integer> desc = new ArrayList<Integer>(1);
			desc.add(x);
			return desc;
		}
		ArrayList<Integer> desc = new ArrayList<Integer>();
		if (properOnly) {
			for (int c : this.children[x]) {
				desc.add(c);
			}
		}
		else {
			desc.add(x);
		}
		// Use BFS to add remaining descendants.
		for (int i = 0; i < desc.size(); ++i) {
			x = desc.get(i);
			for (int c : this.children[x]) {
				desc.add(c);
			}
		}
		return desc;
	}

	@Override
	public int getNoOfDescendants(int x, boolean properOnly) {
		if (this.children[x].length == 0)
			return (properOnly ? 0 : 1);
		ArrayList<Integer> list = new ArrayList<Integer>();
		if (properOnly) {
			for (int c : this.children[x]) {
				list.add(c);
			}
		}
		else {
			list.add(x);
		}
		// Use BFS to add remaining descendants.
		for (int i = 0; i < list.size(); ++i) {
			x = list.get(i);
			for (int c : this.children[x]) {
				list.add(c);
			}
		}
		return list.size();
	}

	@Override
	public int getParent(int x) {
		return (this.parents[x]);
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
		ArrayList<Integer> leaves = new ArrayList<Integer>(this.parents.length);
		for (int i = 0; i < this.parents.length; ++i) {
			if (this.children[i].length == 0) {
				leaves.add(i);
			}
		}
		return leaves;
	}

	@Override
	public int getNoOfLeaves() {
		int cnt = 0;
		for (int i = 0; i < this.parents.length; ++i) {
			if (this.children[i].length == 0) {
				++cnt;
			}
		}
		return cnt;
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
		return (this.children[x].length == 0);
	}

	@Override
	public int getHeight() {
		return getHeight(this.root);
	}

	@Override
	public int getHeight(int x) {
		int childHeight = -1;
		for (int c : this.children[x]) {
			childHeight = Math.max(childHeight, getHeight(c));
		}
		return (1 + childHeight);
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
	public List<Integer> getDescendantLeaves(int x, boolean properOnly) {
		List<Integer> desc = this.getDescendants(x, properOnly);
		ArrayList<Integer> descLeaves = new ArrayList<Integer>(desc.size());
		for (Integer i : desc) {
			if (this.isLeaf(i)) {
				descLeaves.add(i);
			}
		}
		return descLeaves;
	}

	@Override
	public int getNoOfDescendantLeaves(int x, boolean properOnly) {
		List<Integer> desc = this.getDescendants(x, properOnly);
		ArrayList<Integer> descLeaves = new ArrayList<Integer>();
		for (Integer i : desc) {
			if (this.isLeaf(i)) {
				descLeaves.add(i);
			}
		}
		return descLeaves.size();
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
		this.cache = new RTree(this);
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
		this.children = this.cache.children;
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
	public String getSampleValue(SamplingMode mode) {
		// Only prints internal vertex labels.
		// Use RBTreeSampleWrapper for proper output.
		return this.toString();
	}
	
	@Override
	public String toString() {
		// Only prints internal vertex labels.
		// Use RBTreeSampleWrapper for proper output.
		StringBuilder sb = new StringBuilder(1024);
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
			this.writeInternalNewickSubtree(sb, this.children[x][0]);
			for (int i = 1; i < this.children[x].length; ++i) {
				sb.append(',');
				this.writeInternalNewickSubtree(sb, this.children[x][i]);
			}
			sb.append(')');
		}
		sb.append('v').append(x);
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
			for (int c : this.children[x]) {
				q.put(c);
			}
		}
		return l;
	}

}
