package se.cbb.jprime.apps.analysisextraction;

import java.io.File;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Used for parsing info from "true file" produced when generating sequences.
 * 
 * @author Joel Sjöstrand.
 */
public class SequenceTrueFileExtractor {
	
	/**
	 * Retrieves columns from the true file.
	 * @param filename the filename.
	 * @return the column values.
	 * @throws FileNotFoundException.
	 */
	public static TreeMap<String, String> parse(String filename) throws Exception {
		TreeMap<String, String> vals = new TreeMap<String, String>();
		
		Scanner in = new Scanner(new File(filename));
		String ln;
		while (!(ln = in.nextLine()).startsWith("# T N")) {}
		String header = ln.substring(2);
		String values = in.nextLine();
		in.close();
		
		String[] hs = header.split(";");
		String[] vs = values.split(";");
		
		// Special treatment of first columns since space-separated (idiotic!).
		String[] hsf = hs[0].split("[ \t]+");
		String[] vsf = vs[0].split("[ \t]+");
		vals.put(hsf[0].trim(), vsf[0].trim());
		vals.put(hsf[1].trim(), vsf[1].trim());
		StringBuilder hsb = new StringBuilder();
		StringBuilder vsb = new StringBuilder();
		for (int i = 2; i < hsf.length; ++i) { hsb.append(hsf[i]); }
		for (int i = 2; i < vsf.length; ++i) { vsb.append(vsf[i]); }
		vals.put(hsb.toString().trim(), vsb.toString().trim());
		
		// Now clear to proceed with rest.
		for (int i = 1; i < hs.length; ++i) {
			String hstr = hs[i].trim();
			String vstr = vs[i].trim();
			if (!hstr.equals("")) {
				vals.put(hstr, vstr);
			}
		}
		return vals;
	}
}
