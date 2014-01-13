package se.cbb.jprime.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds an unparsed rooted tree realisation as a PrIME Newick tree. All the properties of the realisation can be accessed through the
 * PrIME Newick tree properties. Some useful methods are provided, however, such as string representations for e.g. hashing or comparisons.
 * 
 * @author Joel Sjöstrand.
 */
public class UnparsedRealisation {

	/**
	 * For comparisons with other realisations, provides different types of string representations where various levels
	 * of info have been stripped away.
	 * 
	 * @author Joel Sjöstrand.
	 */
	public enum Representation {
		/** Includes only the topology. */
		TOPOLOGY,
		/** Includes point info, where speciations such as (3,0) are untouched but non-speciations such as (3,2) turns into (3,X). */
		RECONCILIATION,
		/** Includes point info. */
		REALISATION,
		/** Includes point info along with auxiliary info such as branch lengths, etc. */
		REALISATION_PLUS
	}
	
	/** For finding PrIME tags + optionally branch lengths. */
	private static final Pattern PRIME_TAG = Pattern.compile("(:[0-9\\+\\-\\.eE]+)?(\\[&&PRIME [^\\]]*\\])");
	
	/** For point tags. */
	private static final Pattern DISC_PT_TAG = Pattern.compile("(DISCPT=\"?\\([0-9\\+\\-\\.eE]+,[0-9\\+\\-\\.eE]+\\))\"?");
	
	/** Sorted string representation of the sample. */
	public final String treeAsNewickString;
	
	/** Tree. */
	public final PrIMENewickTree tree;
	
	/** Realisation ID. */
	public final int realID;
	
	/** Subsample ID. */
	public final int subSampleID;
	
	/**
	 * Constructor.
	 * @param real realisation.
	 * @param realId realisation's mapping to MCMC sample.
	 * @param subSampleID realisation's subsample within the MCMC sample.
	 * @throws NewickIOException.
	 */
	public UnparsedRealisation(String real, int realID, int subSampleID) throws NewickIOException {
		this.tree = PrIMENewickTreeReader.readTree(real, true, true);
		this.treeAsNewickString = tree.toString();  // Guaranteed to be sorted unlike original string.
		this.realID = realID;
		this.subSampleID = subSampleID;
	}
	
	/**
	 * For string comparisons, hashing, etc., returns a sorted Newick tree representation where a user-defined
	 * level of info has been stripped away.
	 * @param rt the desired info-level.
	 * @return the string representation.
	 */
	public String getStringRepresentation(Representation rt) {
		if (rt == Representation.REALISATION_PLUS) {
			return this.treeAsNewickString;
		}
		StringBuffer nw = new StringBuffer(this.treeAsNewickString.length());
		Matcher m = PRIME_TAG.matcher(this.treeAsNewickString);
		Matcher m2;
		switch (rt) {
		case REALISATION:
			while (m.find()) {
				m2 = DISC_PT_TAG.matcher(m.group(m.groupCount()));
				if (m2.find()) {
					m.appendReplacement(nw, "[&&PRIME " + m2.group(1) + ']');
				}
			}
			break;
		case RECONCILIATION:
			while (m.find()) {
				m2 = DISC_PT_TAG.matcher(m.group(m.groupCount()));
				if (m2.find()) {
					m.appendReplacement(nw, "[&&PRIME " +  m2.group(1).replaceAll(",[1-9][0-9]*", ",X") + ']');
				}
			}
			break;
		case TOPOLOGY:
			while (m.find()) {
				m2 = DISC_PT_TAG.matcher(m.group(m.groupCount()));
				if (m2.find()) {
					m.appendReplacement(nw, "");
				}
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown String representation type for realisation.");
		}
		m.appendTail(nw);
		return nw.toString();
	}
	
}
