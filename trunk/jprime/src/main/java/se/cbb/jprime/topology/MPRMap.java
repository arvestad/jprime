package se.cbb.jprime.topology;

import java.util.List;
import java.util.Map;
import java.util.Set;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.ProperDependent;

/**
 * Extends a guest-host map with functionality concerning the most parsimonious reconciliation
 * when embedding the guest tree in the host tree using duplication and loss events.
 * It is assumed that the guest-to-host leaf mapping does not change (nor the names), while the
 * guest or host topologies may. See also <code>LeafMap</code>.
 * <p/>
 * Note: Only bifurcating trees supported at the moment.
 * 
 * @author Joel Sjöstrand.
 */
public class MPRMap implements ProperDependent {

	/** Guest-to-host leaf map. */
	private GuestHostMap GSMap;
	
	/** Guest tree topology. */
	private RootedBifurcatingTreeParameter G;
	
	/** Host tree topology. */
	private RootedBifurcatingTreeParameter S;
	
	/** Child-vertex-to-host-vertex MPR map. */
	private int[] sigma;
	
	/** Cache. */
	private int[] sigmaCache = null;
	
	/**
	 * Constructor.
	 * @param GSMap guest-to-host leaf map.
	 * @param G guest tree topology.
	 * @param GNames guest tree leaf names.
	 * @param S host tree topology.
	 * @param SNames host tree leaf names.
	 */
	public MPRMap(GuestHostMap GSMap, RootedBifurcatingTreeParameter G, NamesMap GNames,
			RootedBifurcatingTreeParameter S, NamesMap SNames) {
		this.GSMap = GSMap;
		this.G = G;
		this.S = S;
		this.sigma = new int[G.getNoOfVertices()];
		
		List<Integer> GLeaves = this.G.getLeaves();
		Set<String> allSLeaves = SNames.getNames(false);
		
		// Fill the sigma map for the leaves only once, prior to the rest.
		for (int l : GLeaves) {
			String GName = GNames.get(l);
			String sigmaname = this.GSMap.getHostLeafName(GName);
			if (sigmaname == null) {
				throw new IllegalArgumentException("Cannot find guest leaf " + GName + " in guest-to-host leaf map.");
			}
			if (!allSLeaves.contains(sigmaname)) {
				throw new IllegalArgumentException("Cannot find host leaf " + sigmaname + " corresponding to guest leaf " + GName + ".");
			}
			this.sigma[l] = SNames.getVertex(sigmaname);
		}
		
		// Additional validation.
		Set<String> GSMapGLeaves = this.GSMap.getAllGuestLeafNames();
		if (GLeaves.size() != GSMapGLeaves.size()) {
			throw new IllegalArgumentException("Mismatch in the number of guest tree leaves and guest-to-host map items: " + GLeaves.size() + " vs. "  + GSMapGLeaves.size() + ".");
		}
		
		// Now fill the rest.
		this.computeSigma(this.G.getRoot());
	}
	
	/**
	 * Copy constructor.
	 * @param map the map to copy.
	 */
	public MPRMap(MPRMap map) {
		this.GSMap = map.GSMap;
		this.G = map.G;
		this.S = map.S;
		this.sigma = new int[map.sigma.length];
		System.arraycopy(map.sigma, 0, this.sigma, 0, map.sigma.length);
	}

	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { this.S, this.G };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos,
			boolean willSample) {
		if (changeInfos.get(this.G) != null || changeInfos.get(this.S) != null) {
			// Full cache and update regardless of children's changes.
			this.sigmaCache = new int[this.sigma.length];
			System.arraycopy(this.sigma, 0, this.sigmaCache, 0, this.sigma.length);
			this.computeSigma(this.G.getRoot());
			changeInfos.put(this, new ChangeInfo(this, "Updated MPR map (i.e. sigma map)."));
		} else {
			changeInfos.put(this, null);
		}
	}

	/**
	 * Forces an update, without caching. DO NOT USE in case the instance
	 * is part of a dependency DAG!.
	 */
	public void forceUpdate() {
		this.computeSigma(this.G.getRoot());
	}
	
	@Override
	public void clearCache(boolean willSample) {
		this.sigmaCache = null;
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.sigma = this.sigmaCache;
		this.sigmaCache = null;
	}
	
	/**
	 * Recursive method for determining the sigma mapping of the subtree of G rooted at x.
	 * Fills the sigma array and also returns sigma(x).
	 * @param x the vertex of G.
	 */
	private int computeSigma(int x) {
		if (this.G.isLeaf(x)) {
			// Assumed to be correct already.
			return (this.sigma[x]);
		}
		int lcSigma = this.computeSigma(this.G.getLeftChild(x));
		int rcSigma = this.computeSigma(this.G.getRightChild(x));
		sigma[x] = this.S.getLCA(lcSigma, rcSigma);
		return sigma[x];
	}

	/**
	 * For x in V(G), returns the lowermost vertex/arc v in V(S) where x may be placed
	 * when reconciling G and S using only duplications and losses. More formally,
	 * sigma is a function f: V(G) -> V(S) where
	 * <ol>
	 * <li>sigma(x) is the corresponding leaf in V(S) when x is a leaf.</li>
	 * <li>sigma(x) = LCA_{c in children(x)}(sigma(c)) otherwise.</li>
	 * </ol>
	 * @param x the vertex of G.
	 * @return sigma(x).
	 */
	public int getSigma(int x) {
		return this.sigma[x];
	}
	
	/**
	 * Returns true if a vertex x of G is forced to be a duplication, i.e., if sigma(x)==sigma(y) or sigma(x)==sigma(z),
	 * where y and z are its children.
	 * @param x the vertex of G.
	 * @return true if x must be a duplication in the MPR; false otherwise.
	 */
	public boolean isDuplication(int x) {
		if (this.G.isLeaf(x)) {
			return false;
		}
		return (this.sigma[x] == this.sigma[this.G.getLeftChild(x)] || this.sigma[x] == this.sigma[this.G.getRightChild(x)]);
	}
	
	/**
	 * For an edge e=(x,parent(x)) in the gene tree, returns the number of implied
	 * losses along e in a parsimonious reconciliation of the gene tree. If x is the root, the case
	 * is treated analogously.
	 * @param x the head vertex of the arc.
	 * @return the number of implied losses along the arc.
	 */
	public int getNoOfLossesOfArc(int x) {
		int xsigma = this.sigma[x];
		int xpsigma = (this.G.isRoot(x) ? RBTree.NULL : this.sigma[this.G.getParent(x)]);
		if (xsigma == xpsigma) {
			return 0;
		}
		int cnt = -1;
		while (xsigma != xpsigma) {
			cnt++;
			xsigma = this.S.getParent(xsigma);
		}
		return cnt;
	}
	
	/**
	 * Returns the total number of obligate duplications of G in a parsimonious
	 * reconciliation of G and S.
	 * @return the number of duplications.
	 */
	public int getTotalNoOfDuplications() {
		int cnt = 0;
		for (int x = 0; x < this.G.getNoOfVertices(); ++x) {
			if (isDuplication(x)) {
				cnt++;
			}
		}
		return cnt;
	}
	
	/**
	 * Returns the total number of obligate losses of G in a parsimonious
	 * reconciliation of G and S.
	 * @return the number of losses.
	 */
	public int getTotalNoOfLosses() {
		int cnt = 0;
		for (int x = 0; x < this.G.getNoOfVertices(); ++x) {
			cnt += getNoOfLossesOfArc(x);
		}
		return cnt;
	}
	
	/**
	 * Returns the guest-to-host map.
	 * @return the map.
	 */
	public GuestHostMap getGuestHostMap() {
		return this.GSMap;
	}
}
