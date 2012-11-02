package se.cbb.jprime.apps.phylotools;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.*;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * Simple script for obtaining some aspects of the least-common-ancestor (LCA)
 * induced by a set of guest tree leaves when reconciled with a host tree.
 * That is, it returns info on the root of the species subtree spanning all guest tree leaves.
 * 
 * @author Joel Sjöstrand
 */
public class LCAAnalysis implements JPrIMEApp {

	/**
	 * Starter.
	 * @param args
	 * @throws TopologyException 
	 * @throws IOException .
	 * @throws NewickIOException.
	 */
	@Override
	public void main(String[] args) throws NewickIOException, IOException, TopologyException {
		if (args.length != 2 && args.length != 3) {
			System.err.println("Expecting 2 or 3 arguments.");
			usage();
			System.exit(0);
		}
		File sFile = new File(args[0]);
		File gsFile = new File(args[1]);
		File gFile = (args.length == 3 ? new File(args[2]) : null);
		
		// Read tree and G-S map.
		PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(sFile, false, true);
		RTree s = new RTree(sRaw, "S");
		NamesMap sNames = sRaw.getVertexNamesMap(true, "S.names");
		TimesMap sTimes = sRaw.getTimesMap("S.times");
		GuestHostMap gs = GuestHostMapReader.readGuestHostMap(gsFile);
		Set<String> covNames = gs.getAllHostLeafNames();
		
		// Acquire LCA of host tree leaves found in GS file.
		Iterator<String> it = covNames.iterator();
		int lca = sNames.getVertex(it.next());
		while (it.hasNext()) {
			lca = s.getLCA(sNames.getVertex(it.next()), lca);
		}
		System.out.println("LCA ID: " + lca);
		System.out.println("LCA time: " + sTimes.get(lca));
		System.out.println("LCA number of leaves of sub-tree: " + s.getNoOfDescendantLeaves(lca, false));
		System.out.println("LCA number of vertices of sub-tree: " + s.getNoOfDescendants(lca, false));
		System.out.println("LCA height (max arcs to a leaf): " + s.getHeight(lca));
		System.out.println("LCA reverse height (arcs to root): " + s.getNoOfAncestors(lca, true));
		
		// If guest tree provided, compute guest tree root type. Only bifurcating trees supported right now.
		if (gFile != null) {
			RBTree sb = new RBTree(sRaw, "S");
			PrIMENewickTree gRaw = PrIMENewickTreeReader.readTree(gFile, false, true);
			RBTree g = new RBTree(gRaw, "G");
			NamesMap gNames = gRaw.getVertexNamesMap(true, "G.names");
			MPRMap mpr = new MPRMap(gs, g, gNames, sb, sNames);
			if (mpr.isDuplication(g.getRoot())) {
				System.out.println("MPR guest tree root: Duplication");
			} else {
				System.out.println("MPR guest tree root: Speciation");
			}
		}
	}
	
	/**
	 * Prints usage.
	 */
	public static void usage() {
		System.out.println(
				"Extracts host LCA information for a guest tree inscribed in a host tree (assuming no LGT).\n" +
				"If a guest tree is provided, it will also output whether the root of the guest tree is\n" +
				"a duplication or not.\n" +
				"usage:    LCAAnalysis <host tree> <GS map> [guest tree]");
	}

	@Override
	public String getAppName() {
		return "LCAAnalysis";
	}
}
