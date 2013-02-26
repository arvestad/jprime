package se.cbb.jprime.apps.genphylodata;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * Creates and encapsulates a guest tree in pruned and unpruned form.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTree {

	PrIMENewickTree unprunedTree;
	PrIMENewickTree prunedTree;
	RBTreeEpochDiscretiser hostTree;
	PRNG prng;
	EventCreator mightyGodPlaysDice;
	int attempts = 0;
	NamesMap hostNames;
	
	public GuestTree(PrIMENewickTree host, Double stem, String seed, double lambda, double mu, double tau, double rho,
			int min, int max, int minper, int maxper, List<Integer> leafSizes, int maxAttempts, String vertexPrefix,
			boolean excludeMeta) throws NewickIOException, TopologyException, MaxAttemptsException {
		
		// Host tree.
		RBTree S = new RBTree(host, "HostTree");
		TimesMap hostTimes = host.getTimesMap("HostTimes");
		this.hostNames = host.getVertexNamesMap(true, "HostNames");
		if (stem != null) {
			hostTimes.getArcTimes()[S.getRoot()] = stem;
		}
		// Hack: Set 0 stem to eps.
		if (hostTimes.getArcTime(S.getRoot()) <= 0.0) {
			hostTimes.getArcTimes()[S.getRoot()] = 1.0e-64;
		}
		hostTree = new RBTreeEpochDiscretiser(S, hostNames, hostTimes);
		
		// PRNG.
		this.prng = (seed == null ? new PRNG() : new PRNG(new BigInteger(seed)));
		
		// Helper.
		mightyGodPlaysDice = new EventCreator(hostTree, lambda, mu, tau, rho, prng);
		
		// Produce tree.
		GuestVertex unprunedRoot;
		int exact = -1;
		if (leafSizes != null) {
			exact = leafSizes.get(this.prng.nextInt(leafSizes.size()));
		}
		do {
			if (attempts > maxAttempts) {
				throw new MaxAttemptsException("" + attempts + " reached.");
			}
			unprunedRoot = createRawUnprunedTree();
			int no = PruningHelper.labelUnprunableVertices(unprunedRoot, 0, vertexPrefix);
			PruningHelper.labelPrunableVertices(unprunedRoot, no, vertexPrefix);
			attempts++;
		} while (!isOK(unprunedRoot, min, max, minper, maxper, exact));
		
		// Restore 0 length stem.
		if (unprunedRoot.getBranchLength() <= 1.0e-32) {
			unprunedRoot.setBranchLength(0.0);
		}
		
		// Meta.
		if (!excludeMeta) {
			GuestVertex.setMeta(unprunedRoot);
		}
		
		// Finally, some trees.
		GuestVertex prunedRoot = PruningHelper.prune(unprunedRoot);
		String treeMeta = (excludeMeta ? null : "[&&PRIME NAME=UnprunedTree]");
		this.unprunedTree = new PrIMENewickTree(new NewickTree(unprunedRoot, treeMeta, false, false), false);
		treeMeta = (excludeMeta ? null : "[&&PRIME NAME=PrunedTree]");
		this.prunedTree = (prunedRoot == null ?
				null : new PrIMENewickTree(new NewickTree(prunedRoot, treeMeta, false, false), false));
		
	}
	
	/**
	 * Creates an unpruned tree.
	 * @return the root.
	 */
	private GuestVertex createRawUnprunedTree() {
		LinkedList<GuestVertex> alive = new LinkedList<GuestVertex>();
				
		GuestVertex root = mightyGodPlaysDice.createGuestVertex(hostTree.getRoot(), hostTree.getTipToLeafTime());
		alive.add(root);
		while (!alive.isEmpty()) {
			GuestVertex lin = alive.pop();
			if (lin.event == Event.LOSS || lin.event == Event.LEAF || lin.event == Event.UNSAMPLED_LEAF) {
				continue;	
			}
			
			GuestVertex lc = null;
			GuestVertex rc = null;
			if (lin.event == Event.SPECIATION) {
				lc = mightyGodPlaysDice.createGuestVertex(hostTree.getLeftChild(lin.sigma), lin.abstime);
				rc = mightyGodPlaysDice.createGuestVertex(hostTree.getRightChild(lin.sigma), lin.abstime);
			} else if (lin.event == Event.DUPLICATION) {
				lc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
				rc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
			} else if (lin.event == Event.TRANSFER) {
				if (this.prng.nextDouble() < 0.5) {
					lc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
					rc = mightyGodPlaysDice.createGuestVertex(lin.epoch.sampleArc(this.prng, lin.sigma), lin.abstime);
				} else {
					lc = mightyGodPlaysDice.createGuestVertex(lin.epoch.sampleArc(this.prng, lin.sigma), lin.abstime);
					rc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
				}
			}
			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>(2);
			children.add(lc);
			children.add(rc);
			lin.setChildren(children);
			lc.setParent(lin);
			rc.setParent(lin);
			alive.add(lc);
			alive.add(rc);
		}
		return root;
	}
	
	
	/**
	 * Validates.
	 * @param root guest tree root.
	 * @param min min sampled leaves.
	 * @param max max sampled leaves.
	 * @param minPer min sampled leaves per host leaf.
	 * @param maxPer max sampled leaves per host leaf.
	 * @param exact -1 if not applicable, otherwise exact number of leaves required.
	 * @return true if OK; otherwise false.
	 */
	private boolean isOK(GuestVertex root, int min, int max, int minPer, int maxPer, int exact) {
		int sampledLeaves = 0;
		LinkedList<NewickVertex> vertices = new LinkedList<NewickVertex>();
		HashMap<Integer, Integer> sigmaCnt = new HashMap<Integer, Integer>(512); 
		if (root != null) {
			vertices.add(root);
		}
		while (!vertices.isEmpty()) {
			GuestVertex v = (GuestVertex) vertices.pop();
			if (v.event == Event.LEAF) {
				sampledLeaves++;
				Integer cnt = sigmaCnt.get(v.sigma);
				if (cnt != null) {
					sigmaCnt.put(v.sigma, cnt + 1);
				} else {
					sigmaCnt.put(v.sigma, 1);
				}
			} else if (!v.isLeaf()) {
				vertices.addAll(v.getChildren());
			}
		}
		if (exact != -1 && sampledLeaves != exact) {
			return false;
		}
		if (sampledLeaves < min || sampledLeaves > max) {
			return false;
		}
		for (int l : hostTree.getLeaves()) {
			Integer cnt = sigmaCnt.get(l) ;
			if (cnt == null) { cnt = 0; }
			if (cnt < minPer || cnt > maxPer) {
				return false;
			}
		}
		return true;
	}
	
}
