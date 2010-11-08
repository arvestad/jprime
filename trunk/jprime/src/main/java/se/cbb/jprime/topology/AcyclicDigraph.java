package se.cbb.jprime.topology;

import java.util.List;

/**
 * Interface for implementations of acyclic digraphs (commonly referred
 * to as 'DAGs'). The interface allows multiple disjoint components, although
 * implementations/sub-interfaces may of course specialise to contain only one.
 * See the super-interface for more details.
 *  
 * @author Joel Sjöstrand.
 */
public interface AcyclicDigraph extends Graph {
	
	/**
	 * Returns all vertices with in-degree 0. The list is not guaranteed
	 * to be sorted or in any particular order. A null-graph returns null.
	 * @return all source vertices.
	 */
	public List<Integer> getSources();
	
	/**
	 * Returns the number of vertices with in-degree 0.
	 * @return the number of sources.
	 */
	public int getNoOfSources();
	
	/**
	 * Returns all vertices with out-degree 0. The list is not guaranteed
	 * to be sorted or in any particular order. A null-graph returns null.
	 * @return all sink vertices.
	 */
	public List<Integer> getSinks();
	
	/**
	 * Returns the number of vertices with out-degree 0, i.e. "leaves.
	 * @return the number of sinks.
	 */
	public int getNoOfSinks();
	
	/**
	 * Returns true if there is an arc (x,y). By definition,
	 * hasArc(x,x) will always return false due to graph acyclicity.
	 * @param x the tentative tail vertex.
	 * @param y the tentative head vertex.
	 * @return true if there is an arc (x,y).
	 */
	public boolean hasArc(int x, int y);
	
	/**
	 * Returns true if there is a path from x to y, i.e.
	 * y is a proper successor of x. By definition,
	 * hasPath(x,x) will always return false due to graph acyclicity.
	 * @param x the tentative predecessor.
	 * @param y the tentative proper successor.
	 * @return true if there is a path from x to y.
	 */
	public boolean hasPath(int x, int y);
	
	/**
	 * Returns all immediate "children" of a vertex x.
	 * If no children exist, null is returned.
	 * The list is not guaranteed to be sorted or in any
	 * particular order.
	 * @param x the tail.
	 * @return all vertices y so that there is an arc (x,y).
	 */
	public List<Integer> getDirectSuccessors(int x);

	/**
	 * Returns the number of immediate "children" of a vertex x.
	 * @param x the tail.
	 * @return the number of vertices y so that there is an arc (x,y).
	 */
	public int getNoOfDirectSuccessors(int x);
	
	/**
	 * Returns all "descendants" of a vertex x, excluding x itself.
	 * If no descendants exist, null is returned.
	 * The list is not guaranteed to be sorted or in any
	 * particular order.
	 * @param x the tail.
	 * @return all vertices y so that there is a path from x to y.
	 */
	public List<Integer> getSuccessors(int x);
	
	/**
	 * Returns the number of "descendants" of a vertex x, excluding x itself.
	 * @param x the tail.
	 * @return the number of vertices y so that there is a path from x to y.
	 */
	public int getNoOfSuccessors(int x);
}
