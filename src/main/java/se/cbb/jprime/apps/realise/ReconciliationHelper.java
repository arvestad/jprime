package se.cbb.jprime.apps.realise;

import java.util.Map;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.InfoProvider;
import se.cbb.jprime.mcmc.ProperDependent;
import se.cbb.jprime.misc.IntPair;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RootedBifurcatingTreeParameter;

/**
 * Keeps track of the various things related to allowed reconciliations
 * (and thus realisations) between the current G and S'.
 * This class allows for optimisations
 * so that placements which would yield an unrealistic number
 * of implied losses can be discarded.
 * 
 * @author Joel Sjöstrand.
 */
public class ReconciliationHelper implements ProperDependent, InfoProvider {

	/** The guest tree G. */
	protected RBTree g;
	
	/** The host tree S. */
	protected RBTree s;
	
	/** The divergence times t for the discretised tree S'. */
	protected RBTreeArcDiscretiser times;
	
	/** G-S reconciliation info. */
	protected MPRMap gsMap;
	
	/** Maximum allowed implied losses. Placements above this limit are discarded. */
	protected int maxImpliedLosses;
	
	/**
	 * For each vertex u of G, the lowermost placement x_i in S where u can be placed.
	 * HACK: Right now we store the tuple x_i as a single int, with x in the rightmost
	 * bits, and i shifted 16 bits to the left.
	 */
	protected IntMap loLims;
	
	/**
	 * Constructor.
	 * @param g guest tree.
	 * @param s host tree.
	 * @param times host tree discretisation.
	 * @param gsMap G-S MPR map.
	 * @param maxImpliedLosses max number of allowed losses on the way for a guest tree vertex.
	 */
	public ReconciliationHelper(RBTree g, RBTree s, RBTreeArcDiscretiser times, MPRMap gsMap, int maxImpliedLosses) {
		this.g = g;
		this.s = s;
		this.times = times;
		this.gsMap = gsMap;
		this.maxImpliedLosses = maxImpliedLosses;
		this.loLims = new IntMap("DLRS.lolims", g.getNoOfVertices());
		this.updateLoLims(this.g.getRoot());
	}
	
	/**
	 * Recursively computes the lowermost viable placement of each guest tree vertex.
	 * @param u the subtree of G rooted at u.
	 */
	protected void updateLoLims(int u) {
		
		// HACK: At the moment we store a point x_i in a single int v by having x in the
		// rightmost bits, and i shifted 16 bits left.
		// Insert thus:   v = x + (i << 16);
		// Extract thus:  x = (v << 16) >>> 16;   i = v >>> 16;
		
		int sigma = this.gsMap.getSigma(u);

		if (this.g.isLeaf(u)) {
			this.loLims.set(u, sigma + (0 << 16));
		} else {
			int lc = this.g.getLeftChild(u);
			int rc = this.g.getRightChild(u);

			// Update children first.
			this.updateLoLims(lc);
			this.updateLoLims(rc);

			int lcLo = this.loLims.get(lc);
			int rcLo = this.loLims.get(rc);

			// Set the lowest point above the left child to begin with.
			IntPair lo = new IntPair((lcLo << 16) >>> 16, (lcLo >>> 16) + 1);

			// Start at the left child.
			int curr = lo.first;

			// Start at the lowest placement of the left child and move
			// on the path from u towards the root.
			while (curr != RootedBifurcatingTreeParameter.NULL) {
				// If we are at sigma(u) and we haven't marked it as
				// the lowest point of u, do so.
				if (curr == sigma && lo.first != sigma) {
					lo = new IntPair(sigma, 0);
				}

				// If we are at the same lowest edge as the right child.
				if (curr == ((rcLo << 16) >>> 16)) {
					if (lo.first == curr) {
						// u also has this edge as its lowest point.
						lo = new IntPair(lo.first, Math.max(lo.second, (rcLo >>> 16) + 1));
						break;
					}
					else {
						// The right child is higher up in the tree
						// than the left child.
						lo = new IntPair((rcLo << 16) >>> 16, (rcLo >>> 16) + 1);
						break;
					}
				}

				curr = this.s.getParent(curr);
			}

			// If we have moved outside edge's points, choose next pure disc. pt.
			if (lo.second > this.times.getNoOfSlices(lo.first)) {
				lo = new IntPair(this.s.getParent(lo.first), 1);
				if (lo.first == RootedBifurcatingTreeParameter.NULL) {
					throw new RuntimeException("Insufficient no. of discretization points.\n" +
	        				      "Try using denser discretization for 1) top edge, 2) remaining vertices.");
				}
			}
			this.loLims.set(u, lo.first + (lo.second << 16));
		}
	}

	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] {this.g, this.s, this.gsMap, this.times };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		this.loLims.cache(null);
		this.updateLoLims(this.g.getRoot());
		changeInfos.put(this, new ChangeInfo(this, "Reconciliation helper update."));
	}

	@Override
	public void clearCache(boolean willSample) {
		this.loLims.clearCache();
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.loLims.restoreCache();
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
	 * Returns the very tip of the discretised host tree.
	 * @return the point in S' as {x,index}.
	 */
	public int[] getTipPt() {
		int x = this.s.getRoot();
		int i = this.getNoOfSlices(x) + 1;
		return new int[] {x, i};
	}
	
	/**
	 * Returns whether a guest tree vertex is an obligate duplication or not.
	 * @param u guest tree vertex.
	 * @return true if an obligate duplication; false if possibly a speciation.
	 */
	public boolean isDuplication(int u) {
		return this.gsMap.isDuplication(u);
	}
	
	/**
	 * Returns the discretisation interval time span of an host tree discretisation point.
	 * @param x the point.
	 * @return the discretisation interval time span.
	 */
	public double getSliceTime(int[] x_i) {
		return this.times.getSliceTime(x_i[0]);
	}
	
	/**
	 * Returns the number of discretisation slices of an host arc.
	 * @param x the head vertex of the host arc.
	 * @return the number of discretisation slices.
	 */
	public int getNoOfSlices(int x) {
		return this.times.getNoOfSlices(x);
	}
	
	/**
	 * Returns the number of discretisation slices of a path from a
	 * vertex to tip of the host tree.
	 * @param x the host tree vertex.
	 * @return the number of discretisation slices.
	 */
	public int getNoOfSlicesForRootPath(int x) {
		return this.times.getNoOfSlicesForRootPath(x);
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(65536);
		sb.append(prefix).append("RECONCILIATION HELPER\n");
		sb.append(prefix).append("Max number of implied losses: ").append(this.maxImpliedLosses).append('\n');
		sb.append(prefix).append("Discretisation:\n");
		sb.append(this.times.getPreInfo(prefix + '\t'));
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(prefix).append("RECONCILIATION HELPER\n");
		return sb.toString();
	}

	/**
	 * Returns the host tree discretisation time of a point.
	 * @param x the point.
	 * @return the discretisation time.
	 */
	public double getDiscretisationTime(int[] x_i) {
		return this.times.getDiscretisationTime(x_i[0], x_i[1]);
	}
	
	/**
	 * Moves a point in the discretised host tree to the closest
	 * discretisation point above. The original point may be a speciation,
	 * but the incremented point will always be a proper discretisation point.
	 * @param x_i the point which is incremented.
	 */
	public void incrementPt(int[] x_i) {
		if (x_i[1] == this.getNoOfSlices(x_i[0])) {
			x_i[0] = this.s.getParent(x_i[0]);
			x_i[1] = 1;
		} else {
			++x_i[1];
		}
	}
	
	/**
	 * Returns the number of viable placements in the discretised host tree
	 * (starting from lower limits and upwards) for every guest tree vertex.
	 * @return the number of points, indexed by guest tree vertex.
	 */
	public int[] getNoOfPlacements() {
		int[] nos = new int[this.g.getNoOfVertices()];
		this.getNoOfPlacements(this.g.getRoot(), RBTree.NULL, 0, nos);
		return nos;
	}
	
	/**
	 * Recursively computes the number of viable placements.
	 * @param u root of the guest subtree to process.
	 * @param px u's parent's uppermost placement arc.
	 * @param pi u's parent's uppermost placement index.
	 * @param nos the number of placements, indexed by guest tree vertex.
	 */
	protected void getNoOfPlacements(int u, int px, int pi, int[] nos) {
		// Leaf base case.
		if (this.g.isLeaf(u)) {
			nos[u] = 1;
			return;
		}
		
		int x = (this.loLims.get(u) << 16) >>> 16; // Current arc.
		int i = this.loLims.get(u) >>> 16;         // Current arc index.
		nos[u] = 0;                                // No. of points.
		
		// Compute no. of implied losses along the way.
		int losses = 0;
		int y = this.gsMap.getSigma(u);
		while (y != x) {
			losses++;
			y = this.s.getParent(y);
		}
		if (losses > this.maxImpliedLosses) {
			throw new RuntimeException("Insufficient no. of discretization points\n" +
		      "with regards to max number of allowed implied losses. Try raising one of them.");
		}
		
		// Count viable placements.
		int xold = x, iold = i;
		while (!(x == px && i >= pi) && losses <= this.maxImpliedLosses) {
			nos[u]++;
			xold = x;
			iold = i;
			// Move to next pure disc. point.
			if (i == this.getNoOfSlices(x)) {
				x = this.s.getParent(x);
				i = 1;
				losses++;
			} else {
				i++;
			}
		}
		
		// Recurse.
		this.getNoOfPlacements(this.g.getLeftChild(u), xold, iold, nos);
		this.getNoOfPlacements(this.g.getRightChild(u), xold, iold, nos);
	}
}
