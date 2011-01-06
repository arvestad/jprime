package se.cbb.jprime.prm;

/**
 * Holds a string.
 * 
 * @author Joel Sjöstrand.
 */
public class StringAttribute implements AttributeEntity {

	/** Attribute value. */
	public String value;
	
	/**
	 * Constructor.
	 * @param value the attribute's value.
	 */
	public StringAttribute(String value) {
		this.value = value;
	}
	
	@Override
	public Object getAsObject() {
		return ((Object) this);
	}

	@Override
	public String toString() {
		return this.value;
	}
	
}
