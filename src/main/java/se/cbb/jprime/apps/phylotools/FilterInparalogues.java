package se.cbb.jprime.apps.phylotools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TopologyException;

/**
 * Simple app which takes a host tree and a guest tree as input,
 * and outputs a guest tree with in-paralogues removed (the first
 * encountered paralogue is kept). Only bifurcating trees supported at the moment.
 * 
 * @author Joel Sjöstrand.
 */
public class FilterInparalogues implements JPrIMEApp {
	
	@Override
	public String getAppName() {
		return "FilterInparalogues";
	}
	
	/**
	 * Starter.
	 * @param args [0]: guest tree, [1]: host tree, [2]: guest-host map.
	 * @throws TopologyException. 
	 * @throws IOException.
	 * @throws NewickIOException.
	 */
	public void main(String[] args) throws NewickIOException, IOException, TopologyException {
		if (args.length != 3) {
			System.err.println("Expecting 3 arguments.");
			usage();
			System.exit(0);
		}
		File GFile = new File(args[0]);
		File SFile = new File(args[1]);
		File GSFile = new File(args[2]);
		
		// Read input.
		PrIMENewickTree GRaw = PrIMENewickTreeReader.readTree(GFile, false, false);
		PrIMENewickTree SRaw = PrIMENewickTreeReader.readTree(SFile, false, false);
		GuestHostMap GS = GuestHostMapReader.readGuestHostMap(GSFile);
		
		// Reduce GRaw.
		System.out.println(filterInparalogues(GRaw, SRaw, GS));
	}
	
	/**
	 * Filters the in-paralogues. NOTE: Makes changes directly on GRaw.
	 * @param GRaw guest tree. Is reduced.
	 * @param SRaw host tree.
	 * @param GS guest-to-host tree.
	 * @return the filtered tree as a Newick string.
	 * @throws TopologyException.
	 */
	public static String filterInparalogues(PrIMENewickTree GRaw, PrIMENewickTree SRaw, GuestHostMap GS) throws TopologyException {
		
		// Turn into non-raw topologies.
		RBTree G = new RBTree(GRaw, "G");
		RBTree S = new RBTree(SRaw, "S");
		NamesMap GNames = GRaw.getVertexNamesMap(true, "G.names");
		NamesMap SNames = SRaw.getVertexNamesMap(true, "S.names");
		MPRMap mpr = new MPRMap(GS, G, GNames, S, SNames);
		
		// ===================================================================
		// NOTE: This method relies on equal numbering between G and raw G!!!!
		// ===================================================================
		
		// Mark all roots of in-paralogue subtrees.
		HashSet<Integer> inparRoots = new HashSet<Integer>(G.getNoOfLeaves());
		findInparalogueRoots(G.getRoot(), inparRoots, G, S, mpr);
		
		// Special (and highly unlikely case): If all guest leaves are contained in a single
		// host leaf, we output a single leaf Newick tree.
		if (inparRoots.contains(G.getRoot())) {
			NewickVertex u = GRaw.getRoot();
			Double l = (u.hasBranchLength() ? u.getBranchLength() : null);
			while (!u.isLeaf()) {
				u = u.getChildren().get(0);
				if (l != null) { l += u.getBranchLength(); }
			}
			u.setBranchLength(l);
			return (u.toString() + ';');
		}
		
		// Collapse all in-paralogue subtrees.
		collapseInparalogues(GRaw.getRoot(), inparRoots);
		return GRaw.toString();
	}
	
	/**
	 * Recursive method to mark all in-paralogue roots of a specified subtree of G.
	 * @param u the subtree root in G.
	 * @param inparRoots the marked roots.
	 * @param G the guest tree.
	 * @param S the host tree.
	 * @param mpr the most parsimonious reconciliation of G in S.
	 */
	private static void findInparalogueRoots(int u, Set<Integer> inparRoots, RBTree G, RBTree S, MPRMap mpr) {
		if (G.isLeaf(u)) {
			// No in-paralogues in this subtree.
			return;
		}
		if (S.isLeaf(mpr.getSigma(u))) {
			// Found an in-paralogue root.
			inparRoots.add(u);
			return;
		}
		// Continue search amongst children.
		findInparalogueRoots(G.getLeftChild(u), inparRoots, G, S, mpr);
		findInparalogueRoots(G.getRightChild(u), inparRoots, G, S, mpr);
	}

	/**
	 * Recursive helper. Collapses subtrees of a raw Newick tree by replacing them with one of their leaves.
	 * @param u root of the current subtree.
	 * @param inparRoots the list of subtrees which should in fact be collapsed.
	 */
	private static void collapseInparalogues(NewickVertex u, HashSet<Integer> inparRoots) {
		if (inparRoots.contains(u.getNumber())) {
			// Collapse subtree. Only let first encountered leaf remain.
			NewickVertex up = u.getParent();
			NewickVertex uc = u;
			Double l = (uc.hasBranchLength() ? u.getBranchLength() : null);
			while (!uc.isLeaf()) {
				uc = uc.getChildren().get(0);
				if (l != null) { l += uc.getBranchLength(); }
			}
			uc.setParent(up);
			uc.setBranchLength(l);
			int idx = up.getChildren().indexOf(u);
			up.getChildren().set(idx, uc);
			return;
		}
		if (!u.isLeaf()) {
			collapseInparalogues(u.getChildren().get(0), inparRoots);
			collapseInparalogues(u.getChildren().get(1), inparRoots);
		}
	}
	
	/**
	 * Prints usage.
	 */
	public static void usage() {
		System.out.println(
				"Takes as input a guest tree and a host tree, then outputs a reduced guest tree where all in-paralogues\n" +
				"in the MPR have been replaced with only one in-paralogue (in no particular order).\n" +
				"Usage:    FilterInparalogues <guest tree> <host tree> <guest-host leaf map>"
				);
	}

}
