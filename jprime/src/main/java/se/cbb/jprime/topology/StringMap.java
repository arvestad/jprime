package se.cbb.jprime.topology;

import se.cbb.jprime.topology.AcyclicDigraph;

/**
 * Holds a string or each vertex/arc of a tree or graph.
 * 
 * @author Joel Sjöstrand.
 */
public class StringMap implements AcyclicDigraphMap {

	/** The graph to which this map refers. */
	private AcyclicDigraph graph;
	
	/** The name of this map, if any. */
	private String name;
	
	/** The map values. */
	private String[] values;
	
	/**
	 * Constructor. Initialises all map values to null.
	 * @param graph the graph to which the map refers.
	 * @param name the map's name.
	 */
	public StringMap(AcyclicDigraph graph, String name) {
		this.graph = graph;
		this.name = name;
		this.values = new String[graph.getNoOfVertices()];
	}
	
	/**
	 * Constructor.
	 * @param graph the graph to which the map refers.
	 * @param name the map's name.
	 * @param defaultVal default value for all elements.
	 */
	public StringMap(AcyclicDigraph graph, String name, String defaultVal) {
		this.graph = graph;
		this.name = name;
		this.values = new String[graph.getNoOfVertices()];
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
	public StringMap(AcyclicDigraph graph, String name, String[] vals) {
		this.graph = graph;
		this.name = name;
		this.values = vals;
	}
	
	@Override
	public AcyclicDigraph getGraph() {
		return this.graph;
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
