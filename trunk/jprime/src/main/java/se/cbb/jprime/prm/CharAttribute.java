package se.cbb.jprime.prm;

/**
 * Holds a character.
 * 
 * @author Joel Sjöstrand.
 */
public class CharAttribute implements PRMAttribute {

	/** Attribute value. */
	public char value;
	
	/**
	 * Constructor.
	 * @param value the attribute's value.
	 */
	public CharAttribute(char value) {
		this.value = value;
	}
	
	@Override
	public Object getAsObject() {
		return new Character(this.value);
	}
	
	@Override
	public String toString() {
		return ("" + this.value);
	}

}
