package se.cbb.jprime.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.RootedTreeDiscretiser;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * Reads and provides access to a realisation file. Note that a list of all realisations
 * is kept in memory. The file format should be thus:
 * <pre>
 * # Host tree: (...);
 * RealisationID	Subsample	Realisation
 * 0	0	(...);
 * 0	1	(...);
 * 0	2	(...);
 * 100	0	(...);
 * 100	1	(...);
 * 100	2	(...);
 * 200	0	(...);
 * 200	1	(...);
 * 300	2	(...);
 * ...
 * </pre>
 * The first line with the host tree (which may include a discretisation) is optional.
 * Typical usage:
 * <pre>
 * RealisationFileReader rfr = new RealisationFileReader(f, 0.25);
 * UnparsedRealisation[] reals = rfr.getRealisations();
 * ...
 * UnparsedRealisation r = reals[i];
 * String hashTopo = r.getStringRepresentation(Representation.TOPOLOGY);
 * String hashReco = r.getStringRepresentation(Representation.RECONCILIATION);
 * String hashReal = r.getStringRepresentation(Representation.REALISATION);
 * RBTree tree = new RBTree(r.tree, "GuestTree");
 * NamesMap names = r.tree.getVertexNamesMap(true, "GuestNames");
 * IntArrayMap discPts = r.tree.getVertexDiscPtsMap("GuestDiscPts");
 * int l1 = names.getVertex("mm_mdomestica2_ENSMODG00000001601");
 * int l2 = names.getVertex("mm_oanatinus4_ENSOANG00000011572");
 * int lca = tree.getLCA(l1, l2);
 * int[] lcaPt = discPts.get(lca);
 * </pre>
 * 
 * @author Joel Sjöstrand.
 */
public class RealisationFileReader {

	/** Burn-in proportion. */
	private double burnInProportion;
	
	/** Host tree. */
	private PrIMENewickTree hostTree = null;
	
	/** Discretised host tree (if any). */
	private RootedTreeDiscretiser discHostTree = null;
	
	/** Unparsed realisations. */
	private UnparsedRealisation[] realisations;
	
	/**
	 * Constructor.
	 * @param f file.
	 * @param burnInProportion burn-in proportion.
	 * @throws FileNotFoundException.
	 * @throws NewickIOException.
	 * @throws TopologyException.
	 */
	public RealisationFileReader(File f, double burnInProportion) throws FileNotFoundException, NewickIOException, TopologyException {
		if (burnInProportion < 0.0 || burnInProportion > 1.0) {
			throw new IllegalArgumentException("Invalid burn-in range.");
		}
		this.burnInProportion = burnInProportion;
		int cnt = 0;
		Scanner in = new Scanner(f);
		// Read header or host tree.
		String s = in.nextLine();
		if (s.startsWith("# Host tree:")) {
			// Parse host tree.
			this.hostTree = PrIMENewickTreeReader.readTree(s.substring(12), false, true);
			
			// ===============================================================
			// TEMPORARY HACK FOR OWAIS: REPLACES ERRONOUS SPECIES TREE STRING
			// ===============================================================
			this.hostTree = PrIMENewickTreeReader.readTree("(((vv_acarolinensis3:0.38[&&PRIME ID=9 NT=0.0 DISCTIMES=(0.0, 0.012666666666666666, 0.038, 0.06333333333333332, 0.08866666666666667, 0.11399999999999999, 0.13933333333333334, 0.16466666666666666, 0.19, 0.21533333333333332, 0.24066666666666667, 0.266, 0.29133333333333333, 0.31666666666666665, 0.34199999999999997, 0.36733333333333335, 0.38)],(mm_ggallus2:0.15[&&PRIME ID=10 NT=0.0 DISCTIMES=(0.0, 0.009375, 0.028124999999999997, 0.046875, 0.065625, 0.08437499999999999, 0.103125, 0.121875, 0.140625, 0.15)],mm_tguttata4:0.15[&&PRIME ID=11 NT=0.0 DISCTIMES=(0.0, 0.009375, 0.028124999999999997, 0.046875, 0.065625, 0.08437499999999999, 0.103125, 0.121875, 0.140625, 0.15)]):0.23[&&PRIME ID=12 NT=0.15 DISCTIMES=(0.15, 0.15958333333333333, 0.17875, 0.19791666666666666, 0.21708333333333335, 0.23625000000000002, 0.2554166666666667, 0.27458333333333335, 0.29375, 0.3129166666666667, 0.33208333333333334, 0.35125, 0.37041666666666667, 0.38)]):0.09999999999999998[&&PRIME ID=13 NT=0.38 DISCTIMES=(0.38, 0.39, 0.41, 0.43, 0.45, 0.47, 0.48)],(((mm_cfamiliaris2:0.15[&&PRIME ID=2 NT=0.0 DISCTIMES=(0.0, 0.009375, 0.028124999999999997, 0.046875, 0.065625, 0.08437499999999999, 0.103125, 0.121875, 0.140625, 0.15)],(mm_hsapiens5:0.13[&&PRIME ID=4 NT=0.0 DISCTIMES=(0.0, 0.009285714285714286, 0.027857142857142858, 0.04642857142857143, 0.065, 0.08357142857142857, 0.10214285714285715, 0.12071428571428572, 0.13)],mm_mmusculus3:0.13[&&PRIME ID=3 NT=0.0 DISCTIMES=(0.0, 0.009285714285714286, 0.027857142857142858, 0.04642857142857143, 0.065, 0.08357142857142857, 0.10214285714285715, 0.12071428571428572, 0.13)]):0.01999999999999999[&&PRIME ID=5 NT=0.13 DISCTIMES=(0.13, 0.1325, 0.1375, 0.1425, 0.1475, 0.15)]):0.16[&&PRIME ID=6 NT=0.15 DISCTIMES=(0.15, 0.16, 0.18, 0.2, 0.22, 0.24, 0.26, 0.28, 0.3, 0.31)],mm_mdomestica2:0.31[&&PRIME ID=1 NT=0.0 DISCTIMES=(0.0, 0.010333333333333333, 0.031, 0.051666666666666666, 0.07233333333333333, 0.093, 0.11366666666666667, 0.13433333333333333, 0.155, 0.17566666666666667, 0.19633333333333333, 0.217, 0.23766666666666666, 0.2583333333333333, 0.279, 0.29966666666666664, 0.31)]):0.06[&&PRIME ID=7 NT=0.31 DISCTIMES=(0.31, 0.3175, 0.3325, 0.3475, 0.3625, 0.37)],mm_oanatinus4:0.37[&&PRIME ID=0 NT=0.0 DISCTIMES=(0.0, 0.012333333333333333, 0.037, 0.06166666666666667, 0.08633333333333333, 0.111, 0.13566666666666666, 0.16033333333333333, 0.185, 0.20966666666666667, 0.23433333333333334, 0.259, 0.2836666666666667, 0.30833333333333335, 0.333, 0.3576666666666667, 0.37)]):0.10999999999999999[&&PRIME ID=8 NT=0.37 DISCTIMES=(0.37, 0.37916666666666665, 0.39749999999999996, 0.41583333333333333, 0.43416666666666665, 0.4525, 0.4708333333333333, 0.48)]):0.52[&&PRIME ID=14 NT=0.48 DISCTIMES=(0.48, 0.4973333333333333, 0.532, 0.5666666666666667, 0.6013333333333333, 0.636, 0.6706666666666666, 0.7053333333333334, 0.74, 0.7746666666666666, 0.8093333333333332, 0.844, 0.8786666666666667, 0.9133333333333333, 0.948, 0.9826666666666666, 1.0)],vv_tnigroviridis3:1.0[&&PRIME ID=15 NT=0.0 DISCTIMES=(0.0, 0.03333333333333333, 0.1, 0.16666666666666666, 0.23333333333333334, 0.3, 0.36666666666666664, 0.43333333333333335, 0.5, 0.5666666666666667, 0.6333333333333333, 0.7, 0.7666666666666666, 0.8333333333333334, 0.9, 0.9666666666666667, 1.0)]):0.5[&&PRIME ID=16 NT=1.0 DISCTIMES=(1.0, 1.025, 1.075, 1.125, 1.175, 1.225, 1.275, 1.325, 1.375, 1.425, 1.475, 1.5)][&&PRIME NAME=HostTree DISCTYPE=RBTreeArcDiscretiser NMIN=4 NMAX=15 DELTAT=0.02 NROOT=10];", false, true);
			
			String discType = hostTree.getTreeDiscType();
			if (discType != null) {
				// Create discretised host tree.
				this.createDiscHostTree(discType);
			}
			// Read away header.
			s = in.nextLine();
		}
		
		// Count occurrences.
		while (in.hasNextLine()) {
			cnt++;
			in.nextLine();
		}
		int burnInCnt = (int) Math.round(cnt * burnInProportion);
		
		// Read away the burn-in samples.
		in.close();
		in = new Scanner(f);
		s = in.nextLine();
		if (s.startsWith("# Host tree:")) {
			in.nextLine(); // Read header.
		}
		for (int i = 0; i < burnInCnt; ++i) {
			in.nextLine();
		}
		
		// Read the realisations.
		this.realisations = new UnparsedRealisation[cnt - burnInCnt];
		for (int i = 0; i < this.realisations.length; ++i) {
			String[] parts = in.nextLine().split("\t");
			realisations[i] = new UnparsedRealisation(parts[2], Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
		assert(in.hasNextLine() == false);
		in.close();
	}
	
	/**
	 * Creates a discretised host tree by deserialising the corresponding Newick tree string.
	 * @param discType the kind of discretisation.
	 * @throws TopologyException
	 * @throws NewickIOException
	 */
	private void createDiscHostTree(String discType) throws TopologyException, NewickIOException {
		if (discType.equals(RBTreeArcDiscretiser.DISC_TYPE)) {
			RBTree t = new RBTree(hostTree, hostTree.getTreeName());
			NamesMap names = hostTree.getVertexNamesMap(true, "Names");
			TimesMap times = hostTree.getTimesMap("Times");
			int nmin = hostTree.getTreeNMin();
			int nmax = hostTree.getTreeNMax();
			int nroot = hostTree.getTreeNRoot();
			double deltat = hostTree.getTreeDeltaT();
			this.discHostTree = new RBTreeArcDiscretiser(t, names, times, nmin, nmax, deltat, nroot);
		} else if (discType.equals(RBTreeEpochDiscretiser.DISC_TYPE)) {
			RBTree t = new RBTree(hostTree, hostTree.getTreeName());
			NamesMap names = hostTree.getVertexNamesMap(true, "Names");
			TimesMap times = hostTree.getTimesMap("Times");
			int nmin = hostTree.getTreeNMin();
			int nmax = hostTree.getTreeNMax();
			int nroot = hostTree.getTreeNRoot();
			double deltat = hostTree.getTreeDeltaT();
			this.discHostTree = new RBTreeEpochDiscretiser(t, names, times, nmin, nmax, deltat, nroot);
		} else {
			throw new IllegalArgumentException("Unknown discretisation type of host tree; cannot deserialize.");
		}
	}
	
	/**
	 * Returns the host tree, if any.
	 * @return the tree.
	 */
	public PrIMENewickTree getHostTree() {
		return this.hostTree;
	}
	
	/**
	 * Returns the discretised host tree, if any.
	 * @return the tree.
	 */
	public RootedTreeDiscretiser getDiscretisedHostTree() {
		return this.discHostTree;
	}
	
	/**
	 * Returns the burn-in proportion.
	 * @return the proportion.
	 */
	public double getBurnInProportion() {
		return this.burnInProportion;
	}
	
	/**
	 * Returns a list of all realisations after the burn-in.
	 * @return the realisations.
	 */
	public UnparsedRealisation[] getRealisations() {
		return this.realisations;
	}
	
	/**
	 * Returns the number of retained realisations.
	 * @return the number of realisations.
	 */
	public int getNoOfRealisations() {
		return this.realisations.length;
	}
	
	/**
	 * Returns a realisation.
	 * @param i the number, with 0 corresponding to first post-burn-in realisation.
	 * @return the realisation.
	 */
	public UnparsedRealisation get(int i) {
		return this.realisations[i];
	}
}
