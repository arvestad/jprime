package se.cbb.jprime.apps.analysisextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * For parsing FASTA-file information.
 * 
 * @author Joel Sjöstrand.
 */
public class SequenceExtractor {

	/**
	 * Retrieves the number of sequences and the length of the multialignment.
	 * @param filename the FASTA filename.
	 * @return [0] - Integer with the number of sequences,
	 *         [1] - Integer with the multialignment length.
	 * @throws FileNotFoundException.
	 */
	public static List<Integer> parse(String filename) throws FileNotFoundException {
		Scanner in = new Scanner(new File(filename));
		String ln;
		int cnt = 0;
		int length = 0;
		while (in.hasNextLine()) {
			ln = in.nextLine().trim();
			if (ln.startsWith(">")) {
				++cnt;
			} else {
				length += ln.length();
			}
		}
		in.close();
		ArrayList<Integer> al = new ArrayList<Integer>();
		al.add(cnt);
		al.add(length / cnt);
		return al;
	}
	
}
