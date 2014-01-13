package se.cbb.jprime.io;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Sample type for double arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleDoubleArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "DoubleArray";
	
	/** Regular expression for the string of this type. */
	public static final Pattern STRING_REGEX = Pattern.compile("^\\[(|[0-9\\-eE\\.]+|([0-9\\-eE\\.]+(,[0-9\\-eE\\.]+)*))\\]$");
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a double array to a string thus "[4.4, 5.5, 1.0E-13, -2.0E-34, 7.99E23, -5.0E56]".
	 * @param da the double array.
	 * @return the string.
	 */
	public static String toString(double[] da) {
		return Arrays.toString(da);
	}

	/**
	 * Parses a string into a double array.
	 * @param s the string.
	 * @return the double array.
	 */
	public static double[] toDoubleArray(String s) {
		if (s.equals("null")) {
			return null;
		}
		s = s.replaceAll(" ", "");
		if (STRING_REGEX.matcher(s).matches()) {
			if (s.equals("[]")) {
				return new double[]{};
			}
			String[] sVals = s.substring(1, s.length() - 1).split(",");
			double[] vals = new double[sVals.length];
			for (int i = 0; i < sVals.length; ++i) {
				vals[i] = Double.parseDouble(sVals[i]);
			}
			return vals;
		}
		throw new IllegalArgumentException("Cannot convert string into sample double array.");
	}
}
