package se.cbb.jprime.topology;

/**
 * Holds a double for each vertex/arc of a tree or graph.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleMap implements AcyclicDigraphMap {
	
	/** The name of this map, if any. */
	protected String name;
	
	/** The map values. */
	protected double[] values;
	
	/**
	 * Constructor. Initialises all map values to 0.0.
	 * @param name the map's name.
	 * @param size the size of the map.
	 */
	public DoubleMap(String name, int size) {
		this.name = name;
		this.values = new double[size];
	}
	
	/**
	 * Constructor.
	 * @param graph the graph to which the map refers.
	 * @param name the map's name.
	 * @param size the size of the map.
	 * @param defaultVal default value for all elements.
	 */
	public DoubleMap(String name, int size, int defaultVal) {
		this.name = name;
		this.values = new double[size];
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
	public DoubleMap(String name, double[] vals) {
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
		return new Double(this.values[x]);
	}

	@Override
	public void setAsObject(int x, Object value) {
		this.values[x] = ((Double) value).doubleValue();
	}

	/**
	 * Returns the element of a vertex/arc.
	 * @param x the vertex/head of arc.
	 * @return the value.
	 */
	public double get(int x) {
		return this.values[x];
	}
	
	/**
	 * Sets the element of a vertex/arc.
	 * @param x the vertex/head of arc.
	 * @param val the value.
	 */
	public void set(int x, double val) {
		this.values[x] = val;
	}
}
