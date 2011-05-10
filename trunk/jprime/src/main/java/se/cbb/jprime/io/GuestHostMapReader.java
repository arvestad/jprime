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
	 * @param f the file.
	 * @return the map.
	 * @throws FileNotFoundException if no file was found.
	 */
	public static GuestHostMap readGuestHostMap(File f) throws FileNotFoundException {
		GuestHostMap gs = new GuestHostMap();
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String ln = sc.nextLine().trim();
			String[] parts = ln.split("[ \t]+");
			gs.add(parts[0], parts[1]);
		}
		sc.close();
		return gs;
	}
	
}
