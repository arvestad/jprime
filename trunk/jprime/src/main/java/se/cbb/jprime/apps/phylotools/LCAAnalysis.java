package se.cbb.jprime.apps.phylotools;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import se.cbb.jprime.io.*;
import se.cbb.jprime.topology.GSMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.RTreeFactory;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * TBD.
 * 
 * @author Joel Sjöstrand
 */
public class LCAAnalysis {

	/**
	 * Starter.
	 * @param args
	 * @throws TopologyException 
	 * @throws IOException .
	 * @throws NewickIOException.
	 */
	public static void main(String[] args) throws NewickIOException, IOException, TopologyException {
		if (args.length != 2) {
			System.err.println("Expecting 2 arguments.");
			usage();
		}
		File sFile = new File(args[0]);
		File gsFile = new File(args[1]);
		
		// Read tree and G-S map.
		PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(sFile, false, true);
		RTree s = RTreeFactory.createTree(sRaw, "HostTree");
		NamesMap names = sRaw.getVertexNamesMap(true);
		TimesMap times = sRaw.getTimesMap();
		GSMap gs = GSMapReader.readGSMap(gsFile);
		Set<String> covNames = gs.getAllHostLeafNames();
		
		// Acquire LCA of host tree leaves found in GS file.
		Iterator<String> it = covNames.iterator();
		int lca = names.getVertex(it.next());
		while (it.hasNext()) {
			lca = s.getLCA(names.getVertex(it.next()), lca);
		}
		System.out.println("LCA ID: " + lca);
		System.out.println("LCA time: " + times.get(lca));
		System.out.println("LCA height: " + s.getHeight(lca));
		System.out.println("LCA arcs to root: " + s.getNoOfAncestors(lca, true));
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
