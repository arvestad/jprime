package se.cbb.jprime.apps.analysisextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used for parsing info from host tree file.
 * 
 * @author Joel Sjöstrand.
 */
public class HostTreeExtractor {

	private static final Pattern BL_REGEX = Pattern.compile(":[0-9e\\.\\+\\-]+");
	private static final Pattern ET_SUB_REGEX = Pattern.compile("ET=[0-9e\\.\\+\\-]+");
	private static final Pattern ET_SUPER_REGEX = Pattern.compile("&&PRIME[^\\]]*" + ET_SUB_REGEX.pattern());
	private static final Pattern NT_SUB_REGEX = Pattern.compile("NT=[0-9e\\.\\+\\-]+");
	private static final Pattern NT_SUPER_REGEX = Pattern.compile("&&PRIME[^\\]]*" + NT_SUB_REGEX.pattern());
	//private static final Pattern TT_SUB_REGEX = Pattern.compile("TT=[0-9e\\.\\+\\-]+");
	//private static final Pattern TT_SUPER_REGEX = Pattern.compile("&&PRIME[^\\]]*" + TT_SUB_REGEX.pattern());
	
	/**
	 * Retrieves the host tree and related info from a host tree file.
	 * @param filename the filename.
	 * @return [0] - String with the host tree in Newick format,
	 *         [1] - Integer with the tree height,
	 *         [2] - Double with the total time span, or null if no times exist.
	 *         [3] - Double with the total time span, top time excluded, or null if no times exist.
	 * @throws FileNotFoundException.
	 */
	public static List<Object> parse(String filename) throws FileNotFoundException {
		ArrayList<Object> al = new ArrayList<Object>();
		
		// Retrieve Newick tree as one long string.
		Scanner in = new Scanner(new File(filename));
		StringBuilder sb = new StringBuilder();
		while (in.hasNextLine()) {
			sb.append(in.nextLine().trim());
		}
		in.close();
		String tree = sb.toString();
		al.add(tree);
		
		// Retrieve tree height. Single vertex has height 1.
		int maxHeight = 0;
		int height = 1;
		boolean inComment = false;
		for (char c : tree.toCharArray()) {
			if (inComment) {
				if (c == ']') {
					inComment = false;
				}
			} else {
				if (c == '[') {
					inComment = true;
				} else if (c == '(') {
					++height;
					maxHeight = (height > maxHeight ? height : maxHeight);
				} else if (c == ')') {
					--height;
				}
			}
		}
		al.add(new Integer(maxHeight));

		// Obtain times depending on tree tags.
		double[] times = null;
		if (BL_REGEX.matcher(tree).find()) {
			times = getTotalTimeFromBL(tree);
		} else if (ET_SUPER_REGEX.matcher(tree).find()) {
			times = getTotalTimeFromET(tree);
		} else if (NT_SUPER_REGEX.matcher(tree).find()) {
			times = getTotalTimeFromNT(tree);
		}
		if (times != null) {
			al.add(new Double(times[0]));
			al.add(new Double(times[1]));
		}
		
		return al;
	}
	
	/**
	 * Retrieves total time span from branch lengths, incl. and excl. top time.
	 * Assumes that top time is stored as a branch length for the root, and ignores
	 * TT tag (if such exists).
	 * @param tree the tree as a Newick string.
	 * @return the total time span, top time included and excluded respectively.
	 */
	private static double[] getTotalTimeFromBL(String tree) {
		Matcher m = BL_REGEX.matcher(tree);
		double tot = 0;
		double et = 0;
		while (m.find()) {
			String s = m.group();
			et = Double.parseDouble(s.substring(1));
			tot += et;
		}
		double[] times = new double[2];
		times[0] = tot;
		times[1] = tot - et;
		return times;
	}
	
	/**
	 * Retrieves total time span from edge times, incl. and excl. top time.
	 * Assumes that top time is stored as an edge time for the root, and ignores
	 * TT tag (if such exists).
	 * @param tree the tree as a Newick string.
	 * @return the total time span, top time included and excluded respectively.
	 */
	private static double[] getTotalTimeFromET(String tree) {
		Matcher m = ET_SUPER_REGEX.matcher(tree);
		double tot = 0;
		double et = 0;
		while (m.find()) {
			String s = ET_SUB_REGEX.matcher(m.group()).group();
			et = Double.parseDouble(s.substring(3));
			tot += et;
		}
		double[] times = new double[2];
		times[0] = tot;
		times[1] = tot - et;
		return times;
	}
	
	/**
	 * NOT IMPLEMENTED YET!
	 * Retrieves total time span from node times, incl. and excl. top time.
	 * Assumes that top time (if any) is stored using TT tag.
	 * @param tree the tree as a Newick string.
	 * @return the total time span, top time included and excluded respectively.
	 * @throws Exception always thrown, since functionality not implemented yet.
	 */
	private static double[] getTotalTimeFromNT(String tree) throws IllegalArgumentException {
		// TODO: Implement.
		throw new IllegalArgumentException("Not supported yet!");
	}
}
