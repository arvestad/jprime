package se.cbb.jprime.apps.analysisextraction;

import java.io.File;
import java.util.Scanner;

/**
 * Used for parsing info from guest tree file.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeExtractor {
	
	/**
	 * Retrieves the guest tree.
	 * @param filename the filename.
	 * @return String with the guest tree.
	 * @throws FileNotFoundException.
	 */
	public static String parse(String filename) throws Exception {
		
		// Retrieve Newick tree as one long string.
		Scanner in = new Scanner(new File(filename));
		StringBuilder sb = new StringBuilder();
		while (in.hasNextLine()) {
			sb.append(in.nextLine().trim());
		}
		in.close();
		return sb.toString();
	}
}
