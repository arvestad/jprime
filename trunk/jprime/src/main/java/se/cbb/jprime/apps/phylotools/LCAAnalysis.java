package se.cbb.jprime.apps.phylotools;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import se.cbb.jprime.io.*;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * Simple script for obtaining some aspects of the least-common-ancestor (LCA)
 * induced by a set of guest tree leaves when reconciled with a host tree.
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
			System.exit(0);
		}
		File sFile = new File(args[0]);
		File gsFile = new File(args[1]);
		
		// Read tree and G-S map.
		PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(sFile, false, true);
		RTree s = new RTree(sRaw, "HostTree");
		NamesMap names = sRaw.getVertexNamesMap(true);
		TimesMap times = sRaw.getTimesMap();
		GuestHostMap gs = GuestHostMapReader.readGuestHostMap(gsFile);
		Set<String> covNames = gs.getAllHostLeafNames();
		
		// Acquire LCA of host tree leaves found in GS file.
		Iterator<String> it = covNames.iterator();
		int lca = names.getVertex(it.next());
		while (it.hasNext()) {
			lca = s.getLCA(names.getVertex(it.next()), lca);
		}
		System.out.println("LCA ID: " + lca);
		System.out.println("LCA time: " + times.get(lca));
		System.out.println("LCA number of leaves of sub-tree: " + s.getNoOfDescendantLeaves(lca, false));
		System.out.println("LCA number of vertices of sub-tree: " + s.getNoOfDescendants(lca, false));
		System.out.println("LCA height (max arcs to a leaf): " + s.getHeight(lca));
		System.out.println("LCA reverse height (arcs to root): " + s.getNoOfAncestors(lca, true));
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
