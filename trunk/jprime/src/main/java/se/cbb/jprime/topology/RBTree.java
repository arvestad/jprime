package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.SampleType;

/**
 * Implementation of a rooted binary tree topology where parents,
 * left and right children are stored in arrays indexed
 * by vertex number. Null references are indicated as in its interface.
 * <p/>
 * Instances are created using RBTreeFactory.
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
	
	/** Child dependents. */
	protected TreeSet<Dependent> dependents;
	
	/** Change info. */
	protected ChangeInfo changeInfo = null;
	
	/** Parent cache. */
	protected int[] parentsCache = null;
	
	/** Left children cache. */
	protected int[] leftChildrenCache = null;
	
	/** Right children cache. */
	protected int[] rightChildrenCache = null;
	
	/** Root cache. */
	protected int rootCache = RTree.NULL;
	
	/**
	 * Constructor utilised by RBTree factory.
	 */
	protected RBTree() {
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
		return this.getLeftChild(x);
	}

	@Override
	public int getRightChild(int x) {
		return this.getRightChild(x);
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
		this.leftChildrenCache = new int[this.leftChildren.length];
		System.arraycopy(this.leftChildren, 0, this.leftChildrenCache, 0, this.leftChildren.length);
		this.rightChildrenCache = new int[this.rightChildren.length];
		System.arraycopy(this.rightChildren, 0, this.rightChildrenCache, 0, this.rightChildren.length);
		this.rootCache = this.root;
	}

	@Override
	public void update(boolean willSample) {
	}

	@Override
	public void clearCache(boolean willSample) {
		this.parentsCache = null;
		this.leftChildrenCache = null;
		this.rightChildrenCache = null;
		this.rootCache = RTree.NULL;
		this.changeInfo = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.parents = this.parentsCache;
		this.leftChildren = this.leftChildrenCache;
		this.rightChildren = this.rightChildrenCache;
		this.root = this.rootCache;
		this.parentsCache = null;
		this.leftChildrenCache = null;
		this.rightChildrenCache = null;
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
