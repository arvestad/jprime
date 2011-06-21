package se.cbb.jprime.io;

import java.util.Arrays;

/**
 * Sample type for double arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleDoubleArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "DoubleArray";
	
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
		// TODO: Implement.
		return null;
	}
}
