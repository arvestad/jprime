package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GML (Graph Meta Language) graph.
 * Example:
 * <pre>
 * graph [
 *    comment "This is a sample graph"
 *    directed 1
 *    id 123
 *    label "Hello, I am a graph"
 *    node [
 *        id 0
 *    ]
 *    node [
 *        id 1
 *    ]
 *    edge [
 *        source 0
 *        target 1
 *    ]
 * ]
 * </pre>
 * The common attributes (ID, nodes, edges, ...) are handled
 * separately, while all remaining non-standard tags are accessed through a list.
 * Duplicate values of common attributes are treated as non-common.
 * 
 * @author Joel Sjöstrand.
 */
public class GMLGraph {

	/** ID. */
	private Integer id = null;
	
	/** Name. */
	private String name = null;
	
	/** Label. */
	private String label = null;
	
	/** Comment. */
	private String comment = null;
	
	/** Nodes. */
	private ArrayList<GMLNode> nodes = new ArrayList<GMLNode>(0);
	
	/** Edges. */
	private ArrayList<GMLEdge> edges = new ArrayList<GMLEdge>(0);

	/** Directionality. */
	private Boolean directed = null;
	
	/** Non-common attributes. */
	private ArrayList<GMLKeyValuePair> attributes = new ArrayList<GMLKeyValuePair>(0);

	/**
	 * Constructor.
	 * @param values all attributes (common and non-common) at the level beneath the graph element.
	 * @throws GMLIOException.
	 */
	public GMLGraph(List<GMLKeyValuePair> values) throws GMLIOException {
		for (GMLKeyValuePair kv : values) {
			if (kv.key.equalsIgnoreCase("node")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.LIST) {
					throw new GMLIOException("Invalid attribute for GML node: expected list [...].");
				}
				@SuppressWarnings("unchecked")
				GMLNode n = new GMLNode((List<GMLKeyValuePair>) kv.value);
				this.nodes.ensureCapacity(32);
				this.nodes.add(n);
			} else if (kv.key.equalsIgnoreCase("edge")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.LIST) {
					throw new GMLIOException("Invalid attribute for GML edge: expected list [...].");
				}
				@SuppressWarnings("unchecked")
				GMLEdge e = new GMLEdge((List<GMLKeyValuePair>) kv.value);
				this.edges.ensureCapacity(64);
				this.edges.add(e);
			} else if (kv.key.equalsIgnoreCase("id")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.INTEGER) {
					throw new GMLIOException("Invalid attribute for GML ID: expected integer.");
				}
				if (this.id == null) {
					this.id = (Integer) kv.value;
				} else {
					this.attributes.add(kv);
				}
			} else if (kv.key.equalsIgnoreCase("name")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML name: expected string.");
				}
				if (this.name == null) {
					this.name = (String) kv.value;
				} else {
					this.attributes.add(kv);
				}
			} else if (kv.key.equalsIgnoreCase("label")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML label: expected string.");
				}
				if (this.label == null) {
					this.label = (String) kv.value;
				} else {
					this.attributes.add(kv);
				}
			} else if (kv.key.equalsIgnoreCase("comment")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML comment: expected string.");
				}
				if (this.comment == null) {
					this.comment = (String) kv.value;
				} else {
					this.attributes.add(kv);
				}
			} else if (kv.key.equalsIgnoreCase("directed")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.INTEGER) {
					throw new GMLIOException("Invalid attribute for GML directed: expected integer.");
				}
				if (this.directed == null) {
					this.directed  = ((Integer) kv.value) == 0 ? false : true;
				} else {
					this.attributes.add(kv);
				}
			} else {
				this.attributes.add(kv);
			}
		}
	}
	
	/**
	 * Returns the ID.
	 * @return the ID.
	 */
	public Integer getID() {
		return id;
	}

	/**
	 * Sets the ID.
	 * @param id the ID to set.
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Returns the name.
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name the name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the label.
	 * @return the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * @param label the label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the comment.
	 * @return the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment.
	 * @param comment the comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Returns graph type.
	 * @return type.
	 */
	public Boolean getDirected() {
		return directed;
	}
	
	/**
	 * Sets graph type.
	 * @param directed graph type.
	 */
	public void setDirected(Boolean directed) {
		this.directed = directed;
	}
	
	/**
	 * Returns the nodes.
	 * @return the nodes.
	 */
	public List<GMLNode> getNodes() {
		return nodes;
	}

	/**
	 * Returns the edges.
	 * @return the edges.
	 */
	public List<GMLEdge> getEdges() {
		return edges;
	}

	/**
	 * Returns the list of non-common tags.
	 * @return the list of tags.
	 */
	public List<GMLKeyValuePair> getAttributes() {
		return attributes;
	}
	
	
}
