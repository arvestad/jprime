package se.cbb.jprime.prm;

/**
 * Represents PRM class entities, i.e., typically corresponding to records of a
 * relational database table.
 * There is a direct correspondence between its values and the scheme defined by its PRM class.
 * At the moment there is no back-reference to the latter.
 * <p/>
 * We always require that element 0 of the fixed attributes is a unique ID for
 * the entity.
 * 
 * @author Joel Sjöstrand.
 */
public class ClassEntity {
	
	/** Fixed attribute values. First element is used as ID and must be present. */
	private String[] fixedAttributes;
	
	/** Probabilistic attribute values. */
	private AttributeEntity[] probAttributes;
	
	/**
	 * Constructor. Parameters must comply with corresponding PRM class.
	 * @param ID the entity ID.
	 * @param attributes the entity attributes.
	 */
	public ClassEntity(String[] fixedAttributes, AttributeEntity[] probAttributes) {
		this.fixedAttributes = fixedAttributes;
		this.probAttributes = probAttributes;
	}
	
	/**
	 * Returns the fixed attribute value at a specified index.
	 * @param idx the index.
	 * @return the fixed attribute value.
	 */
	public String getFixedAttribute(int idx) {
		return this.fixedAttributes[idx];
	}
	
	/**
	 * Returns this entity's ID (which is element 0 of the fixed attributes).
	 * @return the ID.
	 */
	public String getID() {
		return this.fixedAttributes[0];
	}
	
	/**
	 * Returns the probabilistic attribute value at a specified index.
	 * @param idx the index.
	 * @return the fixed attribute value.
	 */
	public AttributeEntity getProbAttribute(int idx) {
		return this.probAttributes[idx];
	}
	
	/**
	 * Sets the probabilistic attribute value at a specified index.
	 * @param idx the index.
	 * @param attr the attribute value to be set.
	 */
	public void setProbAttribute(int idx, AttributeEntity attr) {
		this.probAttributes[idx] = attr;
	}
}
