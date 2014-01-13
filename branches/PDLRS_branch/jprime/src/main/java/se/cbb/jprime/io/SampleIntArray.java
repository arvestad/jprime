package se.cbb.jprime.io;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Sample type for int arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleIntArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "IntArray";
	
	/** Regular expression for the string of this type. */
	public static final Pattern STRING_REGEX = Pattern.compile("^\\[(|\\-?[0-9]+|(\\-?[0-9]+(,\\-?[0-9]+)*))\\]$");
	
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
		if (s.equals("null")) {
			return null;
		}
		s = s.replaceAll(" ", "");
		if (STRING_REGEX.matcher(s).matches()) {
			if (s.equals("[]")) {
				return new int[]{};
			}
			String[] sVals = s.substring(1, s.length() - 1).split(",");
			int[] vals = new int[sVals.length];
			for (int i = 0; i < sVals.length; ++i) {
				vals[i] = Integer.parseInt(sVals[i]);
			}
			return vals;
		}
		throw new IllegalArgumentException("Cannot convert string into sample int array.");
	}
}
