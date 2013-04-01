package se.cbb.jprime.topology;

import java.util.List;

/**
 * Specialised interface for a multifurcating rooted tree, required to consist of one sole
 * component with at least one vertex. We also require that interior vertices
 * do not have out-degree 1 (and could thus be collapsed).
 * See the super-interface for more details.
 * 
 * @author Joel Sjöstrand.
 */
public interface RootedTree extends AcyclicDigraph {
	
	/**
	 * Returns the root vertex. Guaranteed not to be null, as there
	 * is always at least one vertex.
	 * @return the root.
	 */
	public int getRoot();
	
	/**
	 * Returns the immediate children of a vertex.
	 * If no children exist, an empty list is returned.
	 * There is no guarantee that the list
	 * appears sorted or in any particular order, although implementations
	 * could of course see to that.
	 * Equivalent to getDirectSuccessors();
	 * @param x the vertex.
	 * @return all y such that there is an arc (x,y).
	 */
	public List<Integer> getChildren(int x);
	
	/**
	 * Returns the number of children of a vertex.
	 * Equivalent to getNoOfDirectSuccessors();
	 * @param x the vertex.
	 * @return the number of children.
	 */
	public int getNoOfChildren(int x);
	
	/**
	 * Returns the descendants of a vertex. If lacking descendants,
	 * an empty list is returned.
	 * There is no guarantee that the list
	 * appears sorted or in any particular order, although implementations
	 * could of course see to that.
	 * Equivalent to getSuccessors(), but extended with the possibility to
	 * include the vertex itself.
	 * @param x the vertex.
	 * @param properOnly set to false to include x among descendants.
	 * @return the proper/improper descendants of x.
	 */
	public List<Integer> getDescendants(int x, boolean properOnly);
	
	/**
	 * Returns the number of descendants of a vertex.
	 * Equivalent to getNoOfSuccessors(), but extended with the possibility to
	 * count the vertex itself.
	 * @param x the vertex.
	 * @param properOnly set to false to include x among descendants.
	 * @return the number of proper/improper descendants of x.
	 */
	public int getNoOfDescendants(int x, boolean properOnly);
	
	/**
	 * Returns the descendant leaves of a vertex. If lacking descendants,
	 * an empty list is returned.
	 * There is no guarantee that the list
	 * appears sorted or in any particular order, although implementations
	 * could of course see to that.
	 * Equivalent to getSuccessorSinks(), but extended with the possibility to
	 * include the vertex itself.
	 * @param x the vertex.
	 * @param properOnly set to false to include x among descendants.
	 * @return the proper/improper descendants of x.
	 */
	public List<Integer> getDescendantLeaves(int x, boolean properOnly);
	
	/**
	 * Returns the number of descendants of a vertex.
	 * Equivalent to getNoOfSuccessorSinks(), but extended with the possibility to
	 * count the vertex itself.
	 * @param x the vertex.
	 * @param properOnly set to false to include x among descendants.
	 * @return the number of proper/improper descendants of x.
	 */
	public int getNoOfDescendantLeaves(int x, boolean properOnly);
	
	/**
	 * Returns the immediate parent of x. If x is itself the root,
	 * NULL (as defined in the interface) is returned.
	 * @param x the vertex.
	 * @return z so that there is an arc (z,x).
	 */
	public int getParent(int x);

	/**
	 * Returns the vertices from x to and including the root, so that element
	 * i is the parent of element i-1. If no such path exists,
	 * an empty list is returned.
	 * @param x the vertex.
	 * @param properOnly set to true to start with the parent of x as first element, false
	 * to start with x.
	 * @return the proper/improper ancestors of x sorted so as to end with the root.
	 */
	public List<Integer> getAncestors(int x, boolean properOnly);
	
	/**
	 * Returns the number of vertices from x to and including the root.
	 * @param x the vertex.
	 * @param properOnly set to true to exclude x itself from the count, false to include x.
	 * @return the number of proper/improper ancestors of x.
	 */
	public int getNoOfAncestors(int x, boolean properOnly);
	
	/**
	 * Returns the leaves of the tree. Never null, as there is always at least
	 * one vertex. Equivalent to getSinks().
	 * There is no guarantee that the list appears sorted or in any particular order.
	 * @return the leaves of the tree.
	 */
	public List<Integer> getLeaves();
	
	/**
	 * Returns the number of leaves of the tree. Equivalent to getNoOfSinks().
	 * @return the number of leaves.
	 */
	public int getNoOfLeaves();
	
	/**
	 * Returns the "lowest common ancestor" (a.k.a. "least common ancestor" and
	 * "most recent common ancestor", MRCA) of two vertices x and y,
	 * i.e. the vertex z closest to x and y such that that there is
	 * a path (z,...,x) and a path (z,...,y), or x=y=z.
	 * @param x the first vertex.
	 * @param y the second vertex.
	 * @return the lowest common ancestor of x and y.
	 */
	public int getLCA(int x, int y);
	
	/**
	 * Returns true if a specified vertex is the root.
	 * Equivalent to isSource(...).
	 * @param x the vertex.
	 * @return true if x is the root.
	 */
	public boolean isRoot(int x);
	
	/**
	 * Returns true if a specified vertex is a leaf.
	 * Equivalent to isSink(...).
	 * @param x the vertex.
	 * @return true if x is a leaf.
	 */
	public boolean isLeaf(int x);
	
	/**
	 * Returns the length of the longest path in the tree,
	 * i.e. 0 for a single vertex tree, 1 for a "cherry", etc.
	 * The hypothetical "arc" predating the root is not counted.
	 * See also getNoOfAncestors(...) for the "reverse" height.
	 * @return the number of arcs of the longest path.
	 */
	public int getHeight();
	
	/**
	 * Returns the length of the longest path from a
	 * vertex to its leaves, i.e. 0 if the vertex is
	 * itself a leaf, 1 if it is a "cherry", etc
	 * See also getNoOfAncestors(...) for the "reverse" height.
	 * @param x the vertex.
	 * @return the number of arcs of the longest leaf-path.
	 */
	public int getHeight(int x);
	
}
