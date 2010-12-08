package se.cbb.jprime.apps.lcaanalysis;

import java.io.File;
import java.io.IOException;
import se.cbb.jprime.io.*;
import se.cbb.jprime.topology.GSMap;

/**
 * TBD.
 * 
 * @author Joel Sjöstrand
 */
public class LCAAnalysis {

	/**
	 * Starter.
	 * @param args
	 * @throws IOException .
	 * @throws NewickIOException.
	 */
	public static void main(String[] args) throws NewickIOException, IOException {
		if (args.length != 2) {
			System.err.println("Expecting 2 arguments.");
			usage();
		}
		File sFile = new File(args[0]);
		File gsFile = new File(args[1]);
		
		// Read tree and G-S map.
		PrIMENewickTree s = PrIMENewickTreeReader.readTree(sFile, true, true);
		GSMap gs = GSMapReader.readGSMap(gsFile);
		
	}
	
	/**
	 * Prints usage.
	 */
	public static void usage() {
		System.out.println(
				"Extracts LCA information for a guest tree inscribed in a host tree.\n" +
				"usage:    LCAAnalysis <host tree> <GS map>");
	}
}
