package se.cbb.jprime.io;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Sample type for boolean arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleBooleanArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "BooleanArray";
	
	/** Regular expression for the string of this type. */
	public static final Pattern STRING_REGEX = Pattern.compile("^\\[(|false|true|((true|false)(,(true|false))*))\\]$");
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a boolean array to a string thus "[false, true false, false]".
	 * @param ba the boolean array.
	 * @return the string.
	 */
	public static String toString(boolean[] ba) {
		return Arrays.toString(ba);
	}

	/**
	 * Parses a string into a boolean array.
	 * @param s the string.
	 * @return the boolean array.
	 */
	public static boolean[] toBooleanArray(String s) {
		if (s.equals("null")) {
			return null;
		}
		s = s.replaceAll(" ", "");
		if (STRING_REGEX.matcher(s).matches()) {
			if (s.equals("[]")) {
				return new boolean[]{};
			}
			String[] sVals = s.substring(1, s.length() - 1).split(",");
			boolean[] vals = new boolean[sVals.length];
			for (int i = 0; i < sVals.length; ++i) {
				vals[i] = Boolean.parseBoolean(sVals[i]);
			}
			return vals;
		}
		throw new IllegalArgumentException("Cannot convert string into sample boolean array.");
	}
}
