package se.cbb.jprime.apps.analysisextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Parses contents according to Ali's meta info files.
 * Contents typically like so:
 * <pre>
 * My info: A
 * My second info: B, C, E
 * ...
 * </pre>
 * 
 * @author Joel Sjöstrand.
 */
public class AlixFileExtractor {

	public static TreeMap<String, String> parse(String filename, String delim) throws FileNotFoundException {
		TreeMap<String, String> map = new TreeMap<String, String>();
		Scanner in = new Scanner(new File(filename));
		String ln;
		while (in.hasNextLine()) {
			ln = in.nextLine();
			int idx = ln.split(delim)[0].length();
			String key = ln.substring(0, idx).trim();
			String val = ln.substring(idx + delim.length()).trim();
			map.put(key, val);
		}
		in.close();
		return map;
	}
}
