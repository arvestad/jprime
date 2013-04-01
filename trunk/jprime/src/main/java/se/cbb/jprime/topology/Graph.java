package se.cbb.jprime.topology;

import java.util.List;
import java.util.Set;

/**
 * Base interface for implementations of graphs, whether directed, partially directed
 * or undirected. The interface allows multiple disjoint components, although
 * implementations/sub-interfaces may of course specialise to contain only one.
 * <p/>
 * All vertices are referenced by unique integer IDs which must range from
 * 0 to |V(G)|-1. The behaviour when providing
 * a method an out-of-bounds ID is undefined, and may very well result in an error.
 * Implementations are encouraged to indicate null references by -1, for
 * which there is a shorthand named 'NULL' in this interface.
 * <p/>
 * Important note: Methods returning or setting reference data types may refer to
 * the direct underlying data structures of the instance. Therefore, it may not be
 * safe to e.g. alter returned values unless explicitly intended in some cases.
 *  
 * @author Joel Sjöstrand.
 */
public interface Graph {

	/** Shorthand which may be used to denote null references for vertices and arcs. */
	public static final int NULL = -1;

	/**
	 * Returns the name of this graph. Null is preferred over an empty string
	 * when lacking name.
	 * @return the name.
	 */
	public String getName();
	
	/**
	 * Sets the name of this graph. Null is preferred over an empty string
	 * when lacking name.
	 * @param name the name.
	 */
	public void setName(String name);
	
	/**
	 * Returns the total number of vertices.
	 * @return the number of vertices.
	 */
	public int getNoOfVertices();
	
	/**
	 * Returns the number of disjoint connected components.
	 * @return the number of connected components.
	 */
	public int getNoOfComponents();
	
	/**
	 * Returns true if the graph is strongly connected,
	 * i.e. has only one component.
	 * @return true if comprised of a single component.
	 */
	public boolean isStronglyConnected();
	
	/**
	 * Returns a list of strong components, i.e. each list element contains
	 * all vertices of that component.
	 * An empty graph returns an empty list, not null.
	 * @return all vertices component-wise.
	 */
	public List< Set<Integer> > getComponents();
	
}
