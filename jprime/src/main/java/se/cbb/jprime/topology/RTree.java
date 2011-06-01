package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.SampleType;

/**
 * Implementation of a rooted multifurcating tree topology where parents,
 * and children are stored in arrays indexed
 * by vertex number (the children of a specified vertex are, in turn, held as an array
 * in no particular order).
 * Null references are indicated as in its interface.
 * <p/>
 * Instances are created using RTreeFactory.
 * <p/>
 * Data such as leaf names, branch lengths, etc. are stored elsewhere.
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
	
	/** Child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
	/** Parent cache. */
	protected int[] parentsCache = null;
	
	/** Children cache. */
	protected int[][] childrenCache = null;
	
	/** Root cache. */
	protected int rootCache = RTree.NULL;
	
	/**
	 * Constructor utilised by RTree factory.
	 */
	protected RTree() {
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
		this.dependents = new TreeSet<Dependent>(tree.dependents);
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

	@Override
	public void setChangeInfo(ChangeInfo info) {
		this.changeInfo = info;
	}

	@Override
	public boolean isDependentSink() {
		return this.dependents.isEmpty();
	}

	@Override
	public void addChildDependent(Dependent dep) {
		this.dependents.add(dep);
	}

	@Override
	public Set<Dependent> getChildDependents() {
		return this.dependents;
	}

	@Override
	public void cache(boolean willSample) {
		this.parentsCache = new int[this.parents.length];
		System.arraycopy(this.parents, 0, this.parentsCache, 0, this.parents.length);
		this.childrenCache = new int[this.children.length][];
		for (int i = 0; i < this.children.length; ++i) {
			int sz = this.children[i].length;
			this.childrenCache[i] = new int[sz];
			System.arraycopy(this.children[i], 0, this.childrenCache[i], 0, sz);
		}
		this.rootCache = this.root;
	}

	@Override
	public void update(boolean willSample) {
	}

	@Override
	public void clearCache(boolean willSample) {
		this.parentsCache = null;
		this.childrenCache = null;
		this.rootCache = RTree.NULL;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.parents = this.parentsCache;
		this.children = this.childrenCache;
		this.root = this.rootCache;
		this.parentsCache = null;
		this.childrenCache = null;
		this.rootCache = RTree.NULL;
		this.changeInfo = null;
	}

	@Override
	public ChangeInfo getChangeInfo() {
		return this.changeInfo;
	}

	@Override
	public SampleType getSampleType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSampleHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSampleValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
