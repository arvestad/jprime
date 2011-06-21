package se.cbb.jprime.io;

import java.util.Arrays;

/**
 * Sample type for int arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleIntArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "IntArray";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts an int array to a string thus "[-2, 1, 4, -8]".
	 * @param ia the int array.
	 * @return the string.
	 */
	public static String toString(int[] ia) {
		return Arrays.toString(ia);
	}

	/**
	 * Parses a string into an int array.
	 * @param s the string.
	 * @return the int array.
	 */
	public static int[] toIntArray(String s) {
		// TODO: Implement.
		return null;
	}
}
