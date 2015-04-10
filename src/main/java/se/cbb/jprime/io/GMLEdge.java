package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GML edge. Example:
 * <pre>
 * edge [
 *   source 1
 *   target 2
 *   label "Edge from node 1 to node 2"
 * ]
 * </pre>
 * The common attributes (ID, name, comment, ...) are handled
 * separately, while all remaining non-standard tags are accessed through a list.
 * Duplicate values of common attributes are treated as non-common.
 * 
 * @author Joel Sjöstrand.
 */
public class GMLEdge {

	/** ID. */
	private Integer id = null;

	/** Name. */
	private String name = null;
	
	/** Label. */
	private String label = null;
	
	/** Comment. */
	private String comment = null;
	
	/** Source. */
	private Integer source = null;
	
	/** Target. */
	private Integer target = null;
	
	/** Graphics. */
	private GMLGraphics graphics = null;
	
	/** Label graphics. */
	private GMLLabelGraphics labelGraphics = null;
	
	/** Non-common attributes. */
	private ArrayList<GMLKeyValuePair> attributes = new ArrayList<GMLKeyValuePair>(0);
	
	/**
	 * Constructor.
	 * @param values all attributes (common and non-common) at the level beneath the edge element.
	 * @throws GMLIOException.
	 */
	@SuppressWarnings("unchecked")
	public GMLEdge(List<GMLKeyValuePair> values) throws GMLIOException {
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
			} else if (kv.key.equalsIgnoreCase("source")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.INTEGER) {
					throw new GMLIOException("Invalid attribute for GML source: expected integer.");
				}
				if (this.source == null) {
					this.source = (Integer) kv.value;
				} else {
					this.attributes.add(kv);
				}
			} else if (kv.key.equalsIgnoreCase("target")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.INTEGER) {
					throw new GMLIOException("Invalid attribute for GML target: expected integer.");
				}
				if (this.target == null) {
					this.target = (Integer) kv.value;
				} else {
					this.attributes.add(kv);
				}
			} else if (kv.key.equalsIgnoreCase("graphics")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.LIST) {
					throw new GMLIOException("Invalid attribute for GML graphics: expected list [...].");
				}
				if (this.graphics == null) {
					this.graphics = new GMLGraphics((List<GMLKeyValuePair>) kv.value);
				} else {
					this.attributes.add(kv);
				}
			} else if (kv.key.equalsIgnoreCase("LabelGraphics")) {
				if (kv.valueType !=  GMLKeyValuePair.ValueType.LIST) {
					throw new GMLIOException("Invalid attribute for GML LabelGraphics: expected list [...].");
				}
				if (this.labelGraphics == null) {
					this.labelGraphics = new GMLLabelGraphics((List<GMLKeyValuePair>) kv.value);
				} else {
					this.attributes.add(kv);
				}
			} else {
				this.attributes.add(kv);
			}
		}
		
		// Source and target are required.
		if (this.source == null || this.target == null) {
			throw new GMLIOException("Invalid GML edge with ID" + this.id + ": source and target attributes are required.");
		}
	}
	
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the source
	 */
	public Integer getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Integer source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	public Integer getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(Integer target) {
		this.target = target;
	}

	/**
	 * @return the graphics
	 */
	public GMLGraphics getGraphics() {
		return graphics;
	}

	/**
	 * @param graphics the graphics to set
	 */
	public void setGraphics(GMLGraphics graphics) {
		this.graphics = graphics;
	}

	/**
	 * @return the labelGraphics
	 */
	public GMLLabelGraphics getLabelGraphics() {
		return labelGraphics;
	}

	/**
	 * @param labelGraphics the labelGraphics to set
	 */
	public void setLabelGraphics(GMLLabelGraphics labelGraphics) {
		this.labelGraphics = labelGraphics;
	}

	/**
	 * @return the attributes
	 */
	public ArrayList<GMLKeyValuePair> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(ArrayList<GMLKeyValuePair> attributes) {
		this.attributes = attributes;
	}
}
