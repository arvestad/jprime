package se.cbb.jprime.prm;

/**
 * Holds an integer.
 * 
 * @author Joel Sjöstrand.
 */
public class IntAttribute implements AttributeEntity {

	/** Attribute value. */
	public int value;
	
	/**
	 * Constructor.
	 * @param value the attribute's value.
	 */
	public IntAttribute(int value) {
		this.value = value;
	}
	
	@Override
	public Object getAsObject() {
		return new Integer(this.value);
	}
	
	@Override
	public String toString() {
		return ("" + this.value);
	}

}
