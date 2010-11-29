package se.cbb.jprime.topology;

/**
 * Interface for holders of per-vertex values for an acyclic digraph G, e.g. vertex names
 * or branch lengths. A value is accessed using the integer ID of the vertex.
 * <p/>
 * Of course, one may use it for arc values too, in which case it is natural
 * that the value of an arc (x,y) is stored/accessed using the head y. In particular,
 * this is useful when one is dealing with trees where there is a requirement
 * of a "pseudo-arc" predating the root (for which one may thus store a value using
 * the root without having to introduce an extra vertex in the tree).
 * 
 * @author Joel Sjöstrand.
 */
public interface AcyclicDigraphMap {

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
}
