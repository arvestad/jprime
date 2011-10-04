package se.cbb.jprime.io;

import java.util.Arrays;

/**
 * Sample type for arrays of int arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleIntArrayArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "IntArrayArray";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts an array of int arrays to a string thus "[[4, 5, 1], [-2, 7, -5]]".
	 * @param iaa the int array array.
	 * @return the string.
	 */
	public static String toString(int[][] iaa) {
		if (iaa == null) { return "null"; }
		StringBuilder sb = new StringBuilder(iaa.length * (iaa[0] == null ? 32 : iaa[0].length * 6));
		sb.append("[");
		for (int[] ia : iaa) {
			sb.append(Arrays.toString(ia)).append(", ");
		}
		sb.setCharAt(sb.length() - 2, ']');         // Replace last comma.
		return sb.substring(0, sb.length() - 1);    // Get rid of last space.
	}

	/**
	 * Parses a string into an int array array.
	 * @param s the string.
	 * @return the int array array.
	 */
	public static int[][] toIntArrayArray(String s) {
		// TODO: Implement.
		return null;
	}
}
