package se.cbb.jprime.io;

import java.util.Arrays;

/**
 * Sample type for boolean arrays.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleBooleanArray implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "BooleanArray";
	
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
		// TODO: Implement.
		return null;
	}
}
