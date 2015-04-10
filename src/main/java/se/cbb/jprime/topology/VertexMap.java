package se.cbb.jprime.topology;

/**
 * Interface for holders of per-vertex values for a graph G, e.g. vertex names.
 * A value is accessed using the integer ID of the vertex.
 * <p/>
 * Owing to the often complex relationship between a map M and its graph G, M is not
 * automatically made a dependent of G. However, the user may of course explicitly
 * specify this.
 * <p/>
 * Naturally, for trees, one may use a map for arc values too. In particular,
 * using the head y for access of
 * an arc (x,y) is useful when one is dealing with trees where there is a requirement
 * of a "pseudo-arc" predating the root (for which one may thus store a value using
 * the root without having to introduce an extra vertex in the tree).
 * 
 * @author Joel Sjöstrand.
 */
public interface VertexMap {

	/**
	 * Returns this map's name, if any.
	 * @return the name.
	 */
	public String getName();
	
	/**
	 * Sets this map's name.
	 * @param name the name.
	 */
	public void setName(String name);
	
	/**
	 * Returns the value for a vertex/arc.
	 * For maps with primitive types, the object type be the
	 * corresponding wrapper.
	 * @param x the vertex.
	 * @return the value of the vertex/arc.
	 */
	public Object getAsObject(int x);
	
	/**
	 * Sets the value for a vertex/arc.
	 * For maps with primitive types, the object should be the
	 * corresponding wrapper.
	 * @param x the vertex.
	 * @param the value of the vertex/arc.
	 */
	public void setAsObject(int x, Object value);
	
	/**
	 * Returns the number of elements of the map, i.e., typically the number of vertices
	 * of the corresponding graph.
	 * @return the number of elements.
	 */
	public int getSize();
}
