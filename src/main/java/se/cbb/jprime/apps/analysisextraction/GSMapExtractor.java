package se.cbb.jprime.apps.analysisextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * For parsing GS-map-file information.
 * 
 * @author Joel Sjöstrand.
 */
public class GSMapExtractor {

	/**
	 * Parses a G-S map file.
	 * @param filename the G-S map filename.
	 * @return [0] - Integer with the number of species,
	 *         [1] - Integer with the largest number of members for any species.
	 * @throws FileNotFoundException.
	 */
	public static List<Integer> parse(String filename) throws FileNotFoundException {
		Scanner in = new Scanner(new File(filename));
		String ln;
		HashMap<String, Integer> seqCount = new HashMap<String, Integer>(128);
		while (in.hasNextLine()) {
			ln = in.nextLine().trim();
			if (!ln.equals("")) {
				String species = ln.split("[\t ]+")[1];
				Integer cnt = seqCount.get(species);
				if (cnt == null) {
					seqCount.put(species, 1);
				} else {
					seqCount.put(species, cnt+1);
				}
			}
		}
		in.close();
		int max = Integer.MIN_VALUE;
		for (int i : seqCount.values()) {
			if (i > max) { max = i; }
		}
		ArrayList<Integer> al = new ArrayList<Integer>();
		al.add(seqCount.values().size());
		al.add(max);
		return al;
	}
}
