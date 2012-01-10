package se.cbb.jprime.io;

import java.util.Arrays;

/**
 * Sample type for string arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleStringArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "StringArray";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts an string array to a string thus "[abra, ca, "dabra", etc]".
	 * @param sa the string array.
	 * @return the string.
	 */
	public static String toString(String[] sa) {
		return Arrays.toString(sa);
	}

	/**
	 * Parses a string into a string array.
	 * @param s the string.
	 * @return the string array.
	 */
	public static String[] toStringArray(String s) {
		throw new UnsupportedOperationException("SampleStringArray.toStringArray(String s) not implemented yet.");
	}
}
