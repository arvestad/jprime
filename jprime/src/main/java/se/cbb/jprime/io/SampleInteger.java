package se.cbb.jprime.io;

/**
 * Sample type for ints.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleInteger implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "Integer";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts an int to a string.
	 * @param i the int.
	 * @return the string.
	 */
	public static String toString(int i) {
		return Integer.toString(i);
	}

	/**
	 * Parses a string into an int.
	 * @param s the string.
	 * @return the int.
	 */
	public static int toInt(String s) {
		return Integer.parseInt(s);
	}
}
