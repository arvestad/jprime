package se.cbb.jprime.topology;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Specialisation of a StringMap, where each non-null element is required to be unique, implying that
 * vertex/arc indices may be retrieved from names (and vice versa).
 * 
 * @author Joel Sjöstrand.
 */
public class NamesMap extends StringMap {
	
	/** The vertices/arcs indexed by name. */
	protected HashMap<String, Integer> vertices;
	
	/**
	 * Constructor.
	 * @param graph the graph to which the map refers.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 */
	public NamesMap(String name, String[] vals) {
		super(name, vals.length);
		vertices = new HashMap<String, Integer>(2 * vals.length);
		for (int i = 0; i < vals.length; ++i) {
			if (vals[i] != null) {
				if (this.vertices.put(vals[i], new Integer(i)) != null) {
					throw new IllegalArgumentException("Cannot insert duplicate name in NamesMap.");
				}
			}
		}
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
	public Object getAsObject(int x) {
		return this.values[x];
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = value.toString();
	}

	/**
	 * Returns the element of a vertex/arc.
	 * @param x the vertex/head of arc.
	 * @return the name.
	 */
	public String get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex/arc.
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
	 * Returns the vertex/arc of a certain name.
	 * @param name the name.
	 * @return the vertex containing the name.
	 */
	public int getVertex(String val) {
		return this.vertices.get(val).intValue();
	}
	
	/**
	 * Returns all non-null names.
	 * @return the names.
	 */
	public Set<String> getNames() {
		return this.vertices.keySet();
	}
	
	/**
	 * Returns all non-null names in a sorted representation.
	 * @return the names.
	 */
	public TreeSet<String> getNamesSorted() {
		return new TreeSet<String>(this.vertices.keySet());
	}
	
}
