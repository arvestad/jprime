package se.cbb.jprime.io;

import se.cbb.jprime.math.LogDouble;

/**
 * Sample type for LogDoubles. They are serialised in log-form.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleLogDouble implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "LogDouble";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a LogDouble to a string, using its log-form.
	 * @param p the LogDouble.
	 * @return the string.
	 */
	public static String toString(LogDouble p) {
		return p.toString();
	}

	/**
	 * Parses a string into a LogDouble.
	 * It is assumed that the string holds a log-form value.
	 * @param s the string.
	 * @return the LogDouble.
	 */
	public static LogDouble toLogDouble(String s) {
		return LogDouble.parseLogDouble(s);
	}
}
