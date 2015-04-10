package se.cbb.jprime.io;

import java.util.List;

/**
 * Holder of GML key-value pairs. The value may take on a number of different object types.
 * Currently, this is handled in a rather crude fashion. Pattern implementation applicable, for anyone interested!
 *  
 * @author Joel Sjöstrand.
 */
public class GMLKeyValuePair {

	/** Allowed value types as per GML definition. */
	public static enum ValueType {
		/** <code>Integer</code> */                       INTEGER,
		/** <code>Double</code> */                        REAL,
		/** <code>String</code> */                        STRING,
		/** <code>{@code List<GMLKeyValuePair>}</code> */ LIST  
	}
	
	/** Type identifier. */
	protected ValueType valueType;
	
	/** Key. */
	protected String key;
	
	/** Value. */
	protected Object value;
	
	/**
	 * Constructor.
	 * @param key key.
	 * @param vt value type.
	 * @param val value.
	 */
	public GMLKeyValuePair(String key, ValueType vt, Object val) {
		this.valueType = vt;
		this.key = key;
		this.value = val;
		switch (this.valueType) {
		case INTEGER:
			Integer i = (Integer) val;
			this.value = i;
			break;
		case REAL:
			Double d = (Double) val;
			this.value = d;
			break;
		case STRING:
			String s = (String) val;
			this.value = s;
			break;
		case LIST:
			@SuppressWarnings("unchecked")
			List<GMLKeyValuePair> l = (List<GMLKeyValuePair>) val;
			this.value = l;
			break;
		default:
			throw new IllegalArgumentException("Invalid GML value type.");
		}
	}
	

	/**
	 * Returns the key.
	 * @return the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns the value type.
	 * @return the value type.
	 */

	public ValueType getValueType() {
		return this.valueType;
	}

	/**
	 * Returns the value.
	 * @return the value.
	 */
	public Object getValue() {
		return this.value;
	}
}
