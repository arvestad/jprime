package se.cbb.jprime.topology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Extension of a StringMap, where vertex/arc indices may be retrieved from names (and vice versa).
 * <p/>
 * However, note that retrieving an index will only work for unique name identifiers; for instance, a
 * bootstrap value vertex name (say, "99") will in general have many peers with the same name (in which
 * case one of them will be returned).
 * 
 * @author Joel Sjöstrand.
 */
public class NamesMap extends StringMap {
	
	/** The vertices/arcs indexed by name. */
	protected HashMap<String, Integer> vertices;
	
	/**
	 * Constructor.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 */
	public NamesMap(String name, String[] vals) {
		super(name, vals);
		vertices = new HashMap<String, Integer>(2 * vals.length);
		for (int i = 0; i < vals.length; ++i) {
			if (vals[i] != null) {
				this.vertices.put(vals[i], new Integer(i));
			}
		}
	}
	
	/**
	 * Returns the name of this map. Don't confuse this with names
	 * of individual elements (returned by get(x)).
	 * @return the name of the map.
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of this map. Don't confuse this with names
	 * of individual elements.
	 * @param name the name of the map.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Object getAsObject(int x) {
		return this.values[x];
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = value.toString();
	}

	/**
	 * Returns the name of a vertex/arc.
	 * @param x the vertex/head of arc.
	 * @return the name.
	 */
	public String get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the name of a vertex/arc. No check i made for uniqueness.
	 * @param x the vertex/head of arc.
	 * @param val the name.
	 */
	public void set(int x, String val) {
		this.vertices.remove(this.values[x]);
		this.values[x] = val;
		if (this.vertices.put(val, x) != null) {
			throw new IllegalArgumentException("Cannot insert duplicate name in NamesMap.");
		}
	}
	
	/**
	 * Swap two vertices numbers given two vertices names.
	 * @param u vertex name.
	 * @param v vertex name.
	 */
	public void swapVertices(String u, String v) {
		int numU = this.vertices.get(u);
		this.changeVertex(u, this.vertices.get(v));
		this.changeVertex(v, numU);
	}
	
	/**
	 * Changes the vertex number associated with a given name.
	 * @param val name of the vertex.
	 * @param x vertex number.
	 */
	public void changeVertex(String val, int x) {
		this.vertices.put(val, new Integer(x));
		this.values[x] = val;
	}
	
	/**
	 * Returns the vertex/arc of a certain name.
	 * @param name the name.
	 * @return the vertex containing the name.
	 */
	public int getVertex(String val) {
		return this.vertices.get(val).intValue();
	}
	
	/**
	 * Returns all non-null names of vertices/arcs.
	 * @param excludeBootstrapNames excludes all integer names.
	 * @return the names.
	 */
	public Set<String> getNames(boolean excludeBootstrapNames) {
		Set<String> names = this.vertices.keySet();
		for (Iterator<String> iter = names.iterator(); iter.hasNext(); ) {
			String s = iter.next();
			if (excludeBootstrapNames && s.matches("^\\d+$")) {
				iter.remove();
			}
		}
		return names;
	}
	
	/**
	 * Returns all non-null names of vertices/arcs in a sorted representation.
	 * @param excludeBootstrapNames excludes all integer names.
	 * @return the names.
	 */
	public TreeSet<String> getNamesSorted(boolean excludeBootstrapNames) {
		return new TreeSet<String>(getNames(excludeBootstrapNames));
	}
	
}
