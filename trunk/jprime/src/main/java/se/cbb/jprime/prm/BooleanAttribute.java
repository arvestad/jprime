package se.cbb.jprime.prm;

/**
 * Holds a boolean.
 * 
 * @author Joel Sjöstrand.
 */
public class BooleanAttribute implements PRMAttribute {

	/** Attribute value. */
	public boolean value;
	
	/**
	 * Constructor.
	 * @param value the attribute's value.
	 */
	public BooleanAttribute(boolean value) {
		this.value = value;
	}
	
	@Override
	public Object getAsObject() {
		return new Boolean(this.value);
	}
	
	@Override
	public String toString() {
		return (this.value ? "1" : "0");
	}

}
