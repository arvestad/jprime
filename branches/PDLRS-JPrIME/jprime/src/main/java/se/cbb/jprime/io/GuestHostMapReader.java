package se.cbb.jprime.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import se.cbb.jprime.topology.GuestHostMap;

/**
 * Reads a tab or space delimited file mapping a guest tree leaf to
 * a host tree leaf, e.g.
 * <pre>
 * G1_1   S1
 * G1_2   S1
 * G2_1   S2
 * G3_1   S3
 * G3_2   S3
 * </pre>
 * 
 * @author Joel Sjöstrand.
 */
public class GuestHostMapReader {

	/**
	 * Reads a guest-host leaf map file and returns a corresponding map object.
	 * No syntax verification at the moment.
	 * @param f the file.
	 * @return the map.
	 * @throws FileNotFoundException if no file was found.
	 */
	public static GuestHostMap readGuestHostMap(File f) throws FileNotFoundException {
		Scanner sc = new Scanner(f);
		return readGuestHostMap(sc);
	}
	
	/**
	 * Reads a guest-host leaf map file and returns a corresponding map object.
	 * No syntax verification at the moment.
	 * @param s the string with the map.
	 * @return the map.
	 */
	public static GuestHostMap readGuestHostMap(String s) {
		Scanner sc = new Scanner(s);
		return readGuestHostMap(sc);
	}
	
	/**
	 * Reads a guest-host leaf map file and returns a corresponding map object.
	 * No syntax verification at the moment.
	 * @param sc the Scanner with the map.
	 * @return the map.
	 */
	public static GuestHostMap readGuestHostMap(Scanner sc) {
		GuestHostMap gs = new GuestHostMap();
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			if (ln.equals("")) { break; }
			String[] parts = ln.split("[ \t]+");
			gs.add(parts[0], parts[1]);
		}
		sc.close();
		return gs;
	}
	
}
