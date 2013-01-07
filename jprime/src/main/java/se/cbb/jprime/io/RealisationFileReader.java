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
 * Reads and provides access to a realisation file. Note thay a list of all realisations
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
	private UnparsedTreeRealisation[] realisations;
	
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
		this.realisations = new UnparsedTreeRealisation[cnt - burnInCnt];
		for (int i = 0; i < this.realisations.length; ++i) {
			String[] parts = in.nextLine().split("\t");
			realisations[i] = new UnparsedTreeRealisation(parts[2], Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
		assert(in.hasNextLine() == false);
		in.close();
	}
	
	/**
	 * Creates a discretised host tree by deserialising the corresponding Newick tree string.
	 * @param discType
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
	public UnparsedTreeRealisation[] getRealisations() {
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
	public UnparsedTreeRealisation get(int i) {
		return this.realisations[i];
	}
}
