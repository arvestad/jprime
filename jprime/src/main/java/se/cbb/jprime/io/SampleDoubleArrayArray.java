package se.cbb.jprime.io;

import java.util.Arrays;

import se.cbb.jprime.math.LogDouble;

/**
 * Sample type for arrays of double arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleDoubleArrayArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "DoubleArrayArray";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts an array of double arrays to a string thus "[[4.4, 5.5, 1.0E-13], [-2.0E-34, 7.99E23, -5.0E56]]".
	 * @param daa the double array array.
	 * @return the string.
	 */
	public static String toString(double[][] daa) {
		if (daa == null) { return "null"; }
		StringBuilder sb = new StringBuilder(daa.length * (daa[0] == null ? 32 : daa[0].length * 10));
		sb.append("[");
		for (double[] da : daa) {
			sb.append(Arrays.toString(da)).append(", ");
		}
		sb.setCharAt(sb.length() - 2, ']');         // Replace last comma.
		return sb.substring(0, sb.length() - 1);    // Get rid of last space.
	}
	
	/**
	 * Converts an array of LogDouble arrays to a string thus "[[4.4, 5.5, 1.0E-13], [-2.0E-34, 7.99E23, -5.0E56]]".
	 * @param daa the LogDouble array array.
	 * @return the string.
	 */
	public static String toString(LogDouble[][] daa) {
		if (daa == null) { return "null"; }
		StringBuilder sb = new StringBuilder(daa.length * (daa[0] == null ? 32 : daa[0].length * 10));
		sb.append("[");
		for (LogDouble[] da : daa) {
			sb.append(Arrays.toString(da)).append(", ");
		}
		sb.setCharAt(sb.length() - 2, ']');         // Replace last comma.
		return sb.substring(0, sb.length() - 1);    // Get rid of last space.
	}

	/**
	 * Parses a string into a double array array.
	 * @param s the string.
	 * @return the double array array.
	 */
	public static double[][] toDoubleArrayArray(String s) {
		// TODO: Implement.
		throw new UnsupportedOperationException("SampleDoubleArrayArray.toDoubleArrayArray(String s) not implemented yet.");
	}
}
