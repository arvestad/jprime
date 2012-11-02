package se.cbb.jprime.apps.phylotools;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.topology.BooleanMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RTree;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * Simple app which takes as input:
 * <ol>
 * <li>a host tree S with times in Newick format</li>
 * <li>a G-S map</li>
 * </ol>
 * and returns the time span of the subtree of the host tree induced by the guest leaves.
 * <p/>
 * The word <i>parsimonious</i> may be a bit misleading, although one can imagine returning
 * the time of the root of the guest tree when this necessarily occurs at a speciation.
 * 
 * @author Joel Sjöstrand
 */
public class ParsimoniousTimeEstimator implements JPrIMEApp {

	/**
	 * Starter.
	 * @param args host tree path and guest-host map path.
	 * @throws TopologyException. 
	 * @throws IOException.
	 * @throws NewickIOException.
	 */
	public void main(String[] args) throws NewickIOException, IOException, TopologyException {
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
		NamesMap names = sRaw.getVertexNamesMap(true, "S.names");
		TimesMap times = sRaw.getTimesMap("S.times");
		GuestHostMap gs = GuestHostMapReader.readGuestHostMap(gsFile);
		Set<String> covNames = gs.getAllHostLeafNames();
		
		// Count the time of the subtree induced by the guest leaves.
		double totTime = 0.0;
		BooleanMap visited = new BooleanMap(null, s.getNoOfVertices(), false);
		for (String leaf : covNames) {
			int x = names.getVertex(leaf);
			while (x != RTree.NULL && !visited.get(x)) {
				totTime += times.getArcTime(x);
				visited.set(x, true);
				x = s.getParent(x);
			}
		}
		System.out.println(totTime);
	}
	
	/**
	 * Prints usage.
	 */
	public void usage() {
		System.out.println(
				"Computes the time spanned by the subtree of a host tree induced by a set of guest tree leaves.\n" +
				"usage:    ParsimouniousTimeEstimator <host tree> <guest-host leaf map>");
	}

	@Override
	public String getAppName() {
		return "ParsimoniousTimeEstimator";
	}
}
