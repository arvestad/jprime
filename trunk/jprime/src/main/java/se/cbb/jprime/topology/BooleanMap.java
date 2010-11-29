package se.cbb.jprime.topology;

/**
 * Holds a boolean for each vertex/arc of a tree or graph.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanMap implements AcyclicDigraphMap {
	
	/** The name of this map, if any. */
	private String name;
	
	/** The map values. */
	private boolean[] values;
	
	/**
	 * Constructor. Initialises all map values to null.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public BooleanMap(String name, int size) {
		this.name = name;
		this.values = new boolean[size];
	}
	
	/**
	 * Constructor.
	 * @param graph the graph to which the map refers.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public BooleanMap(String name, int size, boolean defaultVal) {
		this.name = name;
		this.values = new boolean[size];
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
	public BooleanMap(String name, boolean[] vals) {
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
		return new Boolean(this.values[x]);
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = ((Boolean) value).booleanValue();
	}

	/**
	 * Returns the element of a vertex/arc.
	 * @param x the vertex/head of arc.
	 * @return the value.
	 */
	public boolean get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex/arc.
	 * @param x the vertex/head of arc.
	 * @param val the value.
	 */
	public void set(int x, boolean val) {
		this.values[x] = val;
	}
}
