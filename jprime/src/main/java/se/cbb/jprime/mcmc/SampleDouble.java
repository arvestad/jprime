package se.cbb.jprime.mcmc;

/**
 * Sample type for doubles.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleDouble implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "Double";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a double to a string.
	 * @param d the double.
	 * @return the string.
	 */
	public static String toString(double d) {
		return Double.toString(d);
	}

	/**
	 * Parses a string into a double.
	 * @param s the string.
	 * @return the double.
	 */
	public static double toDouble(String s) {
		return Double.parseDouble(s);
	}
}
