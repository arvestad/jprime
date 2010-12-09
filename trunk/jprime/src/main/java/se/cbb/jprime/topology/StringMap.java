package se.cbb.jprime.topology;

/**
 * Holds a string for each vertex/arc of a tree or graph.
 * 
 * @author Joel Sjöstrand.
 */
public class StringMap implements AcyclicDigraphMap {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected String[] values;
	
	/**
	 * Constructor. Initialises all map values to null.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public StringMap(String name, int size) {
		this.name = name;
		this.values = new String[size];
	}
	
	/**
	 * Constructor.
	 * @param graph the graph to which the map refers.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public StringMap(String name, int size, String defaultVal) {
		this.name = name;
		this.values = new String[size];
		for (int i = 0; i < this.values.length; ++i) {
			values[i] = defaultVal;
		}
	}
	
	/**
	 * Constructor.
	 * @param graph the graph to which the map refers.
	 * @param name the map's name.
	 * @param vals the initial values of this map, indexed by vertex number.
	 */
	public StringMap(String name, String[] vals) {
		this.name = name;
		this.values = vals;
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
	 * @return the value.
	 */
	public String get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex/arc.
	 * @param x the vertex/head of arc.
	 * @param val the value.
	 */
	public void set(int x, String val) {
		this.values[x] = val;
	}
}
