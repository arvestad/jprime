package se.cbb.jprime.apps.dltrs;

import java.util.Map;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.InfoProvider;
import se.cbb.jprime.mcmc.ProperDependent;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.LeafLeafMap;
import se.cbb.jprime.topology.RBTree;

/**
 * Keeps track of the various things related to allowed reconciliations
 * (and thus realisations) between the current G and S'.
 * 
 * @author Joel Sjöstrand.
 */
public class ReconciliationHelper implements ProperDependent, InfoProvider {

	/** The guest tree G. */
	protected RBTree g;
	
	/** The host tree S. */
	protected RBTree s;
	
	/** The divergence times t for the discretised tree S'. */
	protected EpochDiscretiser times;
	
	/** Leaf-to-leaf map. */
	protected LeafLeafMap llMap;
	
	/** Host tree arc index in lowermost epoch for guest tree leaves. */
	protected IntMap leafIndices;
	
	/**
	 * For each vertex u of G, the lowermost placement x_i in S where u can be placed.
	 * HACK: Right now we store the tuple x_i as a single int, with x in the rightmost
	 * bits, and i shifted 16 bits to the left.
	 */
	protected IntMap loLims;
	
	/**
	 * For each vertex u of G, the uppermost placement x_i in S where u can be placed.
	 * HACK: Right now we store the tuple x_i as a single int, with x in the rightmost
	 * bits, and i shifted 16 bits to the left.
	 */
	protected IntMap upLims;
	
	/**
	 * Constructor.
	 * @param g guest tree.
	 * @param s host tree.
	 * @param times host tree discretisation.
	 * @param gsMap G-S MPR map.
	 */
	public ReconciliationHelper(RBTree g, RBTree s, EpochDiscretiser times, LeafLeafMap llMap) {
		this.g = g;
		this.s = s;
		this.times = times;
		this.llMap = llMap;
		this.leafIndices = new IntMap("DLTRS.leafindices", g.getNoOfVertices());
		this.loLims = new IntMap("DLTRS.lolims", g.getNoOfVertices());
		this.upLims = new IntMap("DLTRS.uplims", g.getNoOfVertices());
		update();
	}
	
	/**
	 * Performs a full update.
	 */
	private void update() {
		// Retrieve arc index in leaf epoch for each leaf in guest tree.
		int[] hostLeaves = times.getEpoch(0).getArcs();
		for (int u : this.g.getLeaves()) {
			int sigma = llMap.getLeafSigma(u);
			for (int i = 0; true; ++i) {
				if (hostLeaves[i] == sigma) {
					this.leafIndices.set(u, i);
					break;
				}
			}
		}
		this.updateLoLim(this.g.getRoot());
		this.updateUpLim(this.g.getRoot());
	}
	
	/**
	 * Recursively updates lowermost allowed placements.
	 * @param u root of guest subtree. 
	 */
	private void updateLoLim(int u) {
		if (g.isLeaf(u)) {
			loLims.set(u, 0 + (0 << 16));
		} else {
			// Update children first.
			updateLoLim(g.getLeftChild(u));
			updateLoLim(g.getRightChild(u));
			
			// Set limit of u to above childrens' limits.
			int lcLo = loLims.get(g.getLeftChild(u));
			int rcLo = loLims.get(g.getRightChild(u));
			int mx = (((lcLo << 16) >>> 16) > ((rcLo << 16) >>> 16)) ? lcLo : rcLo;
			int[] pt = times.getEpochPtAboveStrict(((mx << 16) >>> 16), mx >>> 16);
			loLims.set(u, pt[0] + (pt[1] << 16));
		}
	}
	
	/**
	 * Recursively updates uppermost allowed placements.
	 * @param u root of guest subtree. 
	 */
	private void updateUpLim(int u) {
		if (g.isLeaf(u)) {
			upLims.set(u, 0 + (0 << 16));
		} else if (g.isRoot(u)) {
			// Beneath very tip of host tree. Placement on the actual
			// tip is disallowed.
			int[] top = times.getEpochPtAtTop();
			upLims.set(u, top[0] + ((top[1] - 1) << 16));
		} else {
			// Normal case: set u's limit just beneath parent's limit.
			int rawPt = upLims.get(g.getParent(u));
			int[] t = times.getEpochPtBelowStrict((rawPt << 16) >>> 16, rawPt >>> 16);
			upLims.set(u, t[0] + (t[1] << 16));
		}
		
		// Update children afterwards.
		if (!g.isLeaf(u)) {
			updateUpLim(g.getLeftChild(u));
			updateUpLim(g.getRightChild(u));
		}
	}

	/**
	 * Returns the lowermost viable placement in S' for a guest tree vertex
	 * u.
	 * @param u the guest tree vertex.
	 * @return the point in S' as {x,index}.
	 */
	public int[] getLoLim(int u) {
		int x = (this.loLims.get(u) << 16) >>> 16;
		int i = this.loLims.get(u) >>> 16;
		return new int[] {x, i};
	}
	
	/**
	 * Returns the lowermost viable placement in S' for a guest tree vertex
	 * u on a string format.
	 * @param u the guest tree vertex.
	 * @return the point in S' as 'x_index'.
	 */
	public String getLoLimAsString(int u) {
		int x = (this.loLims.get(u) << 16) >>> 16;
		int i = this.loLims.get(u) >>> 16;
		return ("" + x + "_" + i);
	}
	
	/**
	 * Returns the uppermost viable placement in S' for a guest tree vertex
	 * u.
	 * @param u the guest tree vertex.
	 * @return the point in S' as {x,index}.
	 */
	public int[] getUpLim(int u) {
		int x = (this.upLims.get(u) << 16) >>> 16;
		int i = this.upLims.get(u) >>> 16;
		return new int[] {x, i};
	}
	
	/**
	 * Returns the uppermost viable placement in S' for a guest tree vertex
	 * u on a string format.
	 * @param u the guest tree vertex.
	 * @return the point in S' as 'x_index'.
	 */
	public String getUpLimAsString(int u) {
		int x = (this.upLims.get(u) << 16) >>> 16;
		int i = this.upLims.get(u) >>> 16;
		return ("" + x + "_" + i);
	}
	
	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(65536);
		sb.append(prefix).append("RECONCILIATION HELPER\n");
		sb.append(prefix).append("Discretisation:\n");
		sb.append(this.times.getPreInfo(prefix + '\t'));
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		return "RECONCILIATION HELPER\n";
	}

	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] {this.g, this.s, this.times };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		this.loLims.cache(null);
		this.upLims.cache(null);
		this.update();
		changeInfos.put(this, new ChangeInfo(this, "Reconciliation helper update."));
	}

	@Override
	public void clearCache(boolean willSample) {
		this.loLims.clearCache();
		this.upLims.clearCache();
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.loLims.restoreCache();
		this.upLims.restoreCache();
	}

	/**
	 * Returns the discretised time identifier above another.
	 * @param pt the epoch identifier and index.
	 * @return the time identifier as [epoch number, index in epoch].
	 */
	public int[] getEpochPtAbove(int[] pt) {
		return times.getEpochPtAbove(pt[0], pt[1]);
	}
	
	/**
	 * Returns the discretised time identifier beneath another.
	 * @param pt the epoch identifier and index.
	 * @return the time identifier as [epoch number, index in epoch].
	 */
	public int[] getEpochPtBelow(int[] pt) {
		return times.getEpochPtBelow(pt[0], pt[1]);
	}
	
	/**
	 * With respect to an epoch's arc vector, returns
	 * the index of the arc splitting at the epoch's
	 * lower border, i.e., the index of the arc that has out-degree
	 * 2.
	 * @param epochNo epoch identifier.
	 * @return index of splitting arc (not a vertex index, mind you).
	 */
	public int getSplitIndex(int epochNo) {
		return times.getSplitIndex(epochNo);
	}

	/**
	 * Returns the timestep of a certain epoch.
	 * @param epochNo the epoch identifier.
	 * @return the timestep.
	 */
	public double getTimestep(int epochNo) {
		return times.getEpochTimestep(epochNo);
	}

	/**
	 * Returns a discretised time.
	 * @param s the epoch identifier and index.
	 * @return the discretised time.
	 */
	public double getTime(int[] s) {
		return this.times.getTime(s[0], s[1]);
	}
	
	/**
	 * Returns the index in the host tree leaf epoch containing a specified guest tree leaf.
	 * @param u the guest tree leaf.
	 * @return the host tree leaf index.
	 */
	public int getHostLeafIndex(int u) {
		return this.leafIndices.get(u);
	}

	/**
	 * Returns true if the discretised time identifier is the
	 * last of its epoch.
	 * @param t the epoch identifier and index.
	 * @return true if last time of epoch.
	 */
	public boolean isLastEpochTime(int[] t) {
		return times.isLastEpochPt(t[0], t[1]);
	}

	/**
	 * Returns the discretised time identifier above another,
	 * with the condition that if the new value would be the last
	 * of the epoch, a move yet another notch up is made.
	 * @param t the epoch identifier and index.
	 * @return the time identifier as [epoch number, index in epoch], albeit not last of epoch.
	 */
	public int[] getEpochTimeAboveNotLast(int[] t) {
		return times.getEpochPtAboveNotLast(t[0], t[1]);
	}

	/**
	 * Returns the discretised time identifier of
	 * the very tip of the top time arc.
	 * @return the top time "tip" as [epoch number, index in epoch].
	 */
	public int[] getEpochPtAtTop() {
		return times.getEpochPtAtTop();
	}
	
	/**
	 * Returns the underlying discretisation.
	 * @return the discretisation.
	 */
	public EpochDiscretiser getDiscretisation() {
		return this.times;
	}
	
}
