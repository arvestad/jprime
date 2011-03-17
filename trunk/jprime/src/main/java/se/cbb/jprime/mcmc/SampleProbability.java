package se.cbb.jprime.mcmc;

import se.cbb.jprime.math.Probability;

/**
 * Sample type for probabilities. They are serialised in log-form.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleProbability implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "Probability";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
	
	/**
	 * Converts a Probability to a string, using its log-form.
	 * @param p the probability.
	 * @return the string.
	 */
	public static String toString(Probability p) {
		return p.toString();
	}

	/**
	 * Parses a string into a Probability.
	 * It is assumed that the string holds a log-form value.
	 * @param s the string.
	 * @return the probability.
	 */
	public static Probability toProbability(String s) {
		return Probability.parseProbability(s);
	}
}
