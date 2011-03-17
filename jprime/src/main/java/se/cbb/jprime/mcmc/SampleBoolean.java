package se.cbb.jprime.mcmc;

/**
 * Sample type for booleans.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleBoolean implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "Boolean";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a boolean to a string.
	 * @param b the boolean.
	 * @return the string.
	 */
	public static String toString(boolean b) {
		return Boolean.toString(b);
	}

	/**
	 * Parses a string into a boolean.
	 * @param s the string.
	 * @return the boolean.
	 */
	public static boolean toBoolean(String s) {
		return Boolean.parseBoolean(s);
	}
}
