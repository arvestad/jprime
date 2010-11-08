package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;

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
public class RTree implements RootedTree {

	/** Used to indicate null references. */
	public static final int NULL = RootedTree.NULL;
	
	/** Name. */
	protected String name;
	
	/** Parents. Note: parents[root] == NULL. */
	protected int[] parents;
	
	/** Children. Note: children[x][k] accesses child number k of vertex x. */
	protected int[][] children;
	
	/** For quick reference, the root index is stored explicitly. */
	protected int root;
	
	/**
	 * Constructor utilised by RTree factory.
	 */
	protected RTree() {
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
		for (int i = 0; i < this.parents.length; ++i)
			vertices.add(i);
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
		return (this.children[x] == null ? 0 : this.children[x].length);
	}

	@Override
	public List<Integer> getDescendants(int x, boolean properOnly) {
		if (this.children[x] == null) {
			if (properOnly) {
				return null;
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
			if (this.children[x] != null) {
				for (int c : this.children[x]) {
					desc.add(c);
				}
			}
		}
		return desc;
	}

	@Override
	public int getNoOfDescendants(int x, boolean properOnly) {
		if (this.children[x] == null)
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
			if (this.children[x] != null) {
				for (int c : this.children[x]) {
					list.add(c);
				}
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
		if (anc.size() == 0)
			return null;
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
			if (this.children[i] == null) {
				leaves.add(i);
			}
		}
		return leaves;
	}

	@Override
	public int getNoOfLeaves() {
		ArrayList<Integer> leaves = new ArrayList<Integer>(this.parents.length);
		for (int i = 0; i < this.parents.length; ++i) {
			if (this.children[i] == null) {
				leaves.add(i);
			}
		}
		return leaves.size();
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
		return (this.children[x] != null);
	}

	@Override
	public int getHeight() {
		return getHeight(this.root);
	}

	@Override
	public int getHeight(int x) {
		if (this.children[x] == null)
			return 0;
		int childHeight = -1;
		for (int c : this.children[x]) {
			childHeight = Math.max(childHeight, getHeight(c));
		}
		return (1 + childHeight);
	}

}
