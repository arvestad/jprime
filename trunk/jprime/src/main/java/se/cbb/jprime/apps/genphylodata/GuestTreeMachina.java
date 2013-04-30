package se.cbb.jprime.apps.genphylodata;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.topology.TopologyException;

/**
 * Generator that simulates a guest tree in unpruned and pruned form, verifies
 * requirements, labels vertices, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeMachina {

	private PRNG prng;
	private int min;
	private int max;
	private int minper;
	private int maxper;
	private List<Integer> leafSizes;
	private int maxAttempts;
	private String vertexPrefix;
	private boolean excludeMeta;
	private int attempts;
	
	/**
	 * Constructor.
	 * @param seed PRNG seed.
	 * @param min min no of leaves.
	 * @param max max no of leaves. 
	 * @param minper min leaves per host leaf.
	 * @param maxper max leaves per host leaf.
	 * @param leafSizes leaf sizes to sample from, unless null.
	 * @param maxAttempts max tries to meet requirements.
	 * @param vertexPrefix vertex name prefix.
	 * @param excludeMeta true to exclude meta info.
	 */
	protected GuestTreeMachina(String seed, int min, int max, int minper, int maxper, List<Integer> leafSizes, int maxAttempts, String vertexPrefix, boolean excludeMeta) {
		this.prng = (seed == null ? new PRNG() : new PRNG(new BigInteger(seed)));
		this.min = min;
		this.max = max;
		this.minper = minper;
		this.maxper = maxper;
		this.leafSizes = leafSizes;
		this.maxAttempts = maxAttempts;
		this.vertexPrefix = vertexPrefix;
		this.excludeMeta = excludeMeta;
		this.attempts = 0;
	}
		
	/**
	 * Samples a host tree.
	 * @param mightyGodPlaysDice unpruned tree creator.
	 * @return the tree, in pruned and unpruned form, respectively.
	 * @throws NewickIOException.
	 * @throws TopologyException.
	 * @throws MaxAttemptsException.
	 */
	public Pair<PrIMENewickTree,PrIMENewickTree> sampleGuestTree(UnprunedGuestTreeCreator mightyGodPlaysDice) throws NewickIOException, TopologyException, MaxAttemptsException {
		
		this.attempts = 0;
		List<Integer> hostLeaves = mightyGodPlaysDice.getHostLeaves();
		GuestVertex unprunedRoot;
		int exact = -1;
		if (leafSizes != null) {
			exact = leafSizes.get(this.prng.nextInt(leafSizes.size()));
		}
		
		GuestVertex prunedRoot = null;
		
		do {
			do {
				if (attempts > maxAttempts) {
					throw new MaxAttemptsException("" + attempts + " reached.");
				}
				
				// Generate unpruned trees until requirements are met.
				if (attempts > maxAttempts) {
					throw new MaxAttemptsException("" + attempts + " reached.");
				}
				unprunedRoot = mightyGodPlaysDice.createUnprunedTree(this.prng);
				int no = PruningHelper.labelUnprunableVertices(unprunedRoot, 0, vertexPrefix);
				PruningHelper.labelPrunableVertices(unprunedRoot, no, vertexPrefix);
				attempts++;
			} while (!unprunedIsOK(unprunedRoot, exact, hostLeaves));
			
			// Set meta info.
			if (!this.excludeMeta) {
				GuestVertex.setMeta(unprunedRoot);
			}
			
			// Finally, an unpruned candidate tree.
			prunedRoot = PruningHelper.prune(unprunedRoot);
			attempts++;
		} while (!prunedIsOK(prunedRoot));
		
		String treeMeta = (excludeMeta ? null : "[&&PRIME NAME=UnprunedTree]");
		PrIMENewickTree unprunedTree = new PrIMENewickTree(new NewickTree(unprunedRoot, treeMeta, false, false), false);
		treeMeta = (excludeMeta ? null : "[&&PRIME NAME=PrunedTree]");
		PrIMENewickTree prunedTree = (prunedRoot == null ? null : new PrIMENewickTree(new NewickTree(prunedRoot, treeMeta, false, false), false));
		return new Pair<PrIMENewickTree, PrIMENewickTree>(prunedTree, unprunedTree);
	}

	/**
	 * Validates requirements of unpruned tree.
	 * @param root guest tree root.
	 * @param exact -1 if not applicable, otherwise exact number of leaves required.
	 * @param hostLeaves host leaves.
	 * @return true if OK; otherwise false.
	 */
	protected boolean unprunedIsOK(GuestVertex root, int exact, List<Integer> hostLeaves) {
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
		for (int l : hostLeaves) {
			Integer cnt = sigmaCnt.get(l) ;
			if (cnt == null) { cnt = 0; }
			if (cnt < minper || cnt > maxper) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Validates requirements of pruned tree.
	 * @param root guest tree root.
	 * @return true if OK; otherwise false.
	 */
	private boolean prunedIsOK(GuestVertex prunedRoot) {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * Returns the no. of attempts of the last simulation.
	 * @return the number of tries.
	 */
	public int getAttempts() {
		return this.attempts;
	}
	
}
