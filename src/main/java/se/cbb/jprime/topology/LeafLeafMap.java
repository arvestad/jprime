package se.cbb.jprime.topology;

/**
 * Very simple map linking each leaf in a guest topology G to its leaf
 * in a host topology S. Assumes that this mapping will never change,
 * and is therefore not a <code>Dependent</code>. See also <code>MPRMap</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class LeafLeafMap {

	/** Child-leaf-to-host-leaf map. */
	private int[] leafSigma;
	
	/**
	 * Constructor.
	 * @param GSMap guest-to-host leaf map.
	 * @param G guest tree topology.
	 * @param GNames guest tree leaf names.
	 * @param S host tree topology.
	 * @param SNames host tree leaf names.
	 */
	public LeafLeafMap(GuestHostMap GSMap, RootedTree G, NamesMap GNames,
			RootedTree S, NamesMap SNames) {
		this.leafSigma = new int[G.getNoOfVertices()];
		for (int i = 0; i < this.leafSigma.length; ++i) {
			// Just for safety.
			this.leafSigma[i] = RootedTree.NULL;
		}
		for (int l : G.getLeaves()) {
			String sigmaname = GSMap.getHostLeafName(GNames.get(l));
			this.leafSigma[l] = SNames.getVertex(sigmaname);
		}
	}
	
	/**
	 * Returns the leaf in the host tree which contains a leaf in the guest tree.
	 * No bounds checking.
	 * @param l the guest tree leaf.
	 * @return the host tree leaf containing l.
	 */
	public int getLeafSigma(int l) {
		return this.leafSigma[l];
	}
 }
