package se.cbb.jprime.prm;

/**
 * Holds a double.
 * 
 * @author Joel Sjöstrand.
 */
public class DoubleAttribute implements PRMAttribute {

	/** Attribute value. */
	public double value;
	
	/**
	 * Constructor.
	 * @param value the attribute's value.
	 */
	public DoubleAttribute(double value) {
		this.value = value;
	}
	
	@Override
	public Object getAsObject() {
		return new Double(this.value);
	}
	
	@Override
	public String toString() {
		return ("" + this.value);
	}

}
