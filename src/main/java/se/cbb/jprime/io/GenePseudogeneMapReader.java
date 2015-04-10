package se.cbb.jprime.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Scanner;

import se.cbb.jprime.topology.GuestHostMap;

/**
 * Reads a tab or space delimited file mapping a guest tree leaf to
 * a gene (1) or a pseudogene (0)
 * 
 * @author Owais Mahmudi.
 */
public class GenePseudogeneMapReader {

	/**
	 * Reads a Gene-Pseudogene leaf map file and returns a corresponding map object.
	 * No syntax verification at the moment.
	 * @param f the file.
	 * @return the map.
	 * @throws FileNotFoundException if no file was found.
	 */
	public static LinkedHashMap<String, Integer> readGenePseudogeneMap(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		return readGenePseudogeneMap(sc);
	}
	
	/**
	 * Reads a Gene-Pseudogene leaf map file and returns a corresponding map object.
	 * No syntax verification at the moment.
	 * @param s the string with the map.
	 * @return the map.
	 */
	public static LinkedHashMap<String, Integer> readGenePseudogeneMap(String s) {
		Scanner sc = new Scanner(s);
		return readGenePseudogeneMap(sc);
	}
	
	/**
	 * Reads a Gene-Pseudogene leaf map file and returns a corresponding map object.
	 * No syntax verification at the moment.
	 * @param sc the Scanner with the map.
	 * @return the map.
	 */
	public static LinkedHashMap<String, Integer> readGenePseudogeneMap(Scanner sc) {
		LinkedHashMap<String, Integer> genePseudogeneLeafMap = new LinkedHashMap<String, Integer>();
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { break; }
			String[] parts = ln.split("[ \t]+");
			genePseudogeneLeafMap.put(parts[0], Integer.parseInt(parts[1]));
		}
		sc.close();
		return genePseudogeneLeafMap;
	}
	
}
