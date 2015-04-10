package se.cbb.jprime.io;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Provides various methods for reading files with delimited contents.
 * Lines are always trimmed of whitespace, and empty lines ignored. 
 * 
 * @author Joel Sjöstrand.
 */
public class DelimitedFileReader {

	/** Parameter delimiter pattern. */
	private String delim;
	
	/** Ignore prefix (e.g. # or //). */
	private String ignorePrefix;
	
	/**
	 * Constructor.
	 * @param delim delimiting symbol or string. May be a regular expression.
	 * @param ignorePrefix ignore lines starting with this string. Cannot be
	 *        a regular expression. May be null.
	 */
	public DelimitedFileReader(String delim, String ignorePrefix) {
		this.delim = delim;
		this.ignorePrefix = ignorePrefix;
		if (ignorePrefix.equals("")) {
			throw new IllegalArgumentException("Cannot use empty string as ignore prefix.");
		}
	}
	
	/**
	 * Reads parsed contents of a file according to (here with space as delimiter):
	 * <pre>
	 * key1 val1
	 * key2 val2
	 * ...
	 * keyn valn
	 * </pre>
	 * and returns the results in a map. Only the first delimiter encountered
	 * is considered, resulting in the remainder of the line for value.
	 * @param f the input file.
	 * @return the keys and values in a map.
	 * @throws FileNotFoundException if file not found.
	 */
	public Map<String, String> readSimpleMap(File f) throws FileNotFoundException {
		HashMap<String, String> map = new HashMap<String, String>();
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("") || (this.ignorePrefix != null && ln.startsWith(ignorePrefix))) {
				continue;
			}
			String[] parts = ln.split(this.delim, 2);
			map.put(parts[0], parts[1]);
			
		}
		sc.close();
		return map;
	}
	
	/**
	 * Returns all values of a file consisting of a single line, e.g.
	 * <pre>
	 * # My comments
	 * # are bla bla.
	 * val1, val2, val3, ..., valn
	 * </pre>
	 * Only the first encountered non-ignored line is parsed.
	 * @param f the file.
	 * @return the values in an array.
	 * @throws FileNotFoundException if file not found.
	 */
	public String[] readSingleLine(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		String ln = null;
		while (ln == null) {
			ln = sc.nextLine().trim();
			if (ln.equals("") || (this.ignorePrefix != null && ln.startsWith(ignorePrefix))) {
				ln = null;
				continue;
			}
		}
		sc.close();
		return ln.split(this.delim);
	}
}
