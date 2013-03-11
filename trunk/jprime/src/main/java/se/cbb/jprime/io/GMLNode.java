package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GML node.
 * Example:
 * <pre>
 *  node [
 *     id 1
 *     label "node 1"
 *     thisIsASampleAttribute 42
 *  ]
 * </pre>
 * The common attributes (ID, name, comment, ...) are handled
 * separately, while all remaining non-standard tags are accessed through a list.
 * Duplicate values of common attributes are treated as non-common.
 * 
 * @author Joel Sjöstrand.
 */
public class GMLNode {

	/** ID. */
	private Integer id = null;
	
	/** Name. */
	private String name = null;
	
	/** Label. */
	private String label = null;
	
	/** Comment. */
	private String comment = null;
	
	/** Edge anchor. */
	private String edgeAnchor = null;
	
	/** Non-common attributes. */
	private ArrayList<GMLKeyValuePair> attributes = new ArrayList<GMLKeyValuePair>(0);
	
	/**
	 * Constructor.
	 * @param values all attributes (common and non-common) at the level beneath the node element.
	 * @throws GMLIOException.
	 */
	public GMLNode(List<GMLKeyValuePair> values) throws GMLIOException {
		for (GMLKeyValuePair kv : values) {
			if (kv.key.equalsIgnoreCase("id")) {
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
			} else if (kv.key.equalsIgnoreCase("edgeAnchor")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.STRING) {
					throw new GMLIOException("Invalid attribute for GML edge anchor: expected string.");
				}
				if (this.edgeAnchor == null) {
					this.edgeAnchor = (String) kv.value;
				} else {
					this.attributes.add(kv);
				}
			} else {
				this.attributes.add(kv);
			}
		}
	}

	
	
}
