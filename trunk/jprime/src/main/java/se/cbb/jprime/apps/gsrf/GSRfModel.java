package se.cbb.jprime.apps.gsrf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import se.cbb.jprime.io.SampleLogDouble;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.Model;
import se.cbb.jprime.misc.IntPair;
import se.cbb.jprime.topology.DoubleArrayMap;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RootedBifurcatingTreeParameter;

/**
 * Implements the GSRf model.
 * Effectively, this class computes the likelihood
 * Pr[G,l | S,t,lambda,mu,m,v], where G is the guest tree
 * topology, l the branch lengths of G, S the host tree, t the
 * divergence times of S, lambda the duplication (birth) rate,
 * mu the loss (death) rate,
 * m the substitution rate mean, and v the substitution rate variance.
 * <p/>
 * More specifically, this model computes the likelihood of G and l given
 * S and t, where the G is assumed to have evolved down S according to
 * a birth-death-like process, and the branch lengths of G are iid according
 * to some distribution (e.g. gamma), thus relaxing the molecular clock.
 * <p/>
 * To sum all probability density contributions of ways to temporally embed G within S,
 * S is discretised and a dynamic programming algorithm is applied w.r.t. the
 * discretisation points.
 * 
 * @author Joel Sjöstrand.
 */
public class GSRfModel implements Model {

	/** The guest tree G. */
	protected RootedBifurcatingTreeParameter g;
	
	/** The host tree S. */
	protected RootedBifurcatingTreeParameter s;
	
	/** G-S reconciliation info. */
	protected MPRMap gsMap;
	
	/** The branch lengths l. */
	protected DoubleMap lengths;
	
	/** The divergence times t for the discretised tree S'. */
	protected RBTreeArcDiscretiser times;
	
	/** P11 and similar info. */
	protected DupLossProbs dupLossProbs;
	
	/** Substitution rate distribution. */
	protected Continuous1DPDDependent substPD;
	
	/**
	 * For each vertex u of G, the lowermost placement x_i in S where u can be placed.
	 * HACK: Right now we store the tuple x_i as a single int, with x in the rightmost
	 * bits, and i shifted 16 bits to the left.
	 */
	protected IntMap loLims;
	
	/**
	 * Probability of rooted subtree G_u for each valid placement of u in S'.
	 */
	protected DoubleArrayMap ats;
	
	/**
	 * Probability of planted subtree G^u for each valid placement of tip of u's
	 * parent arc in S'.
	 */
	protected DoubleArrayMap belows;
	
	/**
	 * Constructor.
	 * @param g the guest tree G.
	 * @param s the host tree S.
	 * @param gsMap the guest-to-host leaf map.
	 * @param lengths the branch lengths of G.
	 * @param times the times (and discretisation) of S.
	 * @param dupLossProbs the duplication-loss probabilities over discretised S.
	 * @param substPD the iid rate probability distribution over arcs of G,
	 *  (relaxing the molecular clock).
	 */
	public GSRfModel(RootedBifurcatingTreeParameter g, RootedBifurcatingTreeParameter s, MPRMap gsMap,
			DoubleMap lengths, RBTreeArcDiscretiser times, DupLossProbs dupLossProbs, Continuous1DPDDependent substPD) {
		this.g = g;
		this.s = s;
		this.gsMap = gsMap;
		this.lengths = lengths;
		this.times = times;
		this.dupLossProbs = dupLossProbs;
		this.substPD = substPD;
		this.loLims = new IntMap("GSRf.lolims", g.getNoOfVertices());
		this.ats = new DoubleArrayMap("GSRf.ats", g.getNoOfVertices());
		this.belows = new DoubleArrayMap("GSRf.belows", g.getNoOfVertices());
				
		// Update.
		this.fullUpdate();
	}
	
	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] {this.g, this.s, this.gsMap, this.lengths, this.times, this.dupLossProbs, this.substPD };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		ChangeInfo gci = changeInfos.get(this.g);
		ChangeInfo sci = changeInfos.get(this.s);
		ChangeInfo gsci = changeInfos.get(this.gsMap);
		ChangeInfo lci = changeInfos.get(this.lengths);
		ChangeInfo tci = changeInfos.get(this.times);
		ChangeInfo dpci = changeInfos.get(this.dupLossProbs);
		ChangeInfo rci = changeInfos.get(this.substPD);

		// One could think of many optimisations here, especially when there are 
		// time perturbations involved, possibly combined with length perturbations.
		// However, it is easy to make algorithmic mistakes in such situations,
		// so at the only moment solitary length changes result in a partial DP update.
		if (gci == null && sci == null && gsci == null && tci == null && dpci == null && rci == null) {
			if (lci != null && lci.getAffectedElements() != null) {
				// Only certain branch lengths have changed. We do a partial update.
				
				// First, find all affected vertices of G.
				HashSet<Integer> vertices = new HashSet<Integer>(64);
				for (int u : lci.getAffectedElements()) {
					while (u != RBTree.NULL) {
						if (!vertices.add(u)) { break; }
						u = this.g.getParent(u);
					}
				}
				this.partialUpdate(vertices);
				changeInfos.put(this, new ChangeInfo(this, vertices, "Partial update"));
			} else if (lci != null) {
				this.fullUpdate();
				changeInfos.put(this, new ChangeInfo(this, "Full update."));
			}
		} else {
			this.fullUpdate();
			changeInfos.put(this, new ChangeInfo(this, "Full GSRf update."));
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.loLims.clearCache();
		this.ats.clearCache();
		this.belows.clearCache();
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.loLims.restoreCache();
		this.ats.restoreCache();
		this.belows.restoreCache();
	}

	@Override
	public Class<?> getSampleType() {
		return SampleLogDouble.class;
	}

	@Override
	public String getSampleHeader() {
		return "GSRf-likelihood";
	}

	@Override
	public String getSampleValue() {
		return this.getLikelihood().toString();
	}

	@Override
	public LogDouble getLikelihood() {
		return new LogDouble(this.belows.get(this.g.getRoot(), 0));
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
				lo = new IntPair(this.g.getParent(lo.first), 1);
				if (lo.first == RootedBifurcatingTreeParameter.NULL) {
					throw new RuntimeException("Insufficient no. of discretization points.\n" +
	        				      "Try using denser discretization for 1) top edge, 2) remaining vertices.");
				}
			}
			this.loLims.set(u, lo.first + (lo.second << 16));
		}
	}
	
	/**
	 * Recursively creates (thus clearing) the DP data structures.
	 * Precondition: upper limits must be up-to-date.
	 * @param u the root of the subtree of G.
	 * @param noOfAncestors the number of ancestors of u.
	 */
	protected void clearAtsAndBelows(int u, int noOfAncestors) {
		if (this.g.isLeaf(u)) {
			this.ats.set(u, new double[1]);
			this.belows.set(u, new double[this.ats.get(this.g.getParent(u)).length]);
		} else {
			int x = (this.loLims.get(u) << 16) >>> 16;
			int i = this.loLims.get(u) >>> 16;
			int cnt = this.times.getNoOfSlicesForRootPath(x) - i + 1 - noOfAncestors;
			this.ats.set(u, new double[cnt]);			
			if (this.g.isRoot(u)) {
				this.belows.set(u, new double[1]);
			} else {
				this.belows.set(u, new double[this.ats.get(this.g.getParent(u)).length]);
			}
			this.clearAtsAndBelows(this.g.getLeftChild(u), noOfAncestors + 1);
			this.clearAtsAndBelows(this.g.getRightChild(u), noOfAncestors + 1);
		}
	}
	
	/**
	 * Performs a full DP update.
	 */
	protected void fullUpdate() {
		int r = this.g.getRoot();
		this.updateLoLims(r);
		this.clearAtsAndBelows(r, 0);
		this.updateAtProbs(r, true);
	}
	
	/**
	 * Performs a partial DP update.
	 * It is assumed that lower limits and number of discretisation points
	 * are up-to-date.
	 * @param affectedElements all affected vertices, not necessarily sorted.
	 */
	private void partialUpdate(HashSet<Integer> affectedElements) {
		List<Integer> vertices = this.g.getTopologicalOrdering();
		for (int i = vertices.size() - 1; i >= 0; --i) {
			int u = vertices.get(i);
			if (affectedElements.contains(u)) {
				this.updateAtProbs(u, false);
			}
		}
	}

	/**
	 * Dynamic programming method for computing the probability of all realisations of
	 * rooted tree G_u when u is placed on point x_i. All viable placements x_i are
	 * tabulated.
	 * @param u the vertex of G.
	 * @param doRecurse true to recursively process all descendants of u.
	 */
	protected void updateAtProbs(int u, boolean doRecurse) {
		if (this.g.isLeaf(u)) {
			this.ats.set(u, 0, 1.0);
		} else {
			int lc = this.g.getLeftChild(u);
			int rc = this.g.getRightChild(u);

			// Must do children first, if specified.
			if (doRecurse) {
				this.updateAtProbs(lc, true);
				this.updateAtProbs(rc, true);
			}

			// Retrieve placement start.
			int x = (this.loLims.get(u) << 16) >>> 16;  // Current arc.
			int xi = this.loLims.get(u) >>> 16;         // Current arc index.
			int idx = 0;                                // No. of processed viable placements.

			double[] uAts = this.ats.get(u);
			double[] lcBelows = this.belows.get(lc);
			double[] rcBelows = this.belows.get(rc);
			
			// First placement might correspond to a speciation.
			if (xi == 0) {
				uAts[0] = lcBelows[0] * rcBelows[0];
				++idx;
				++xi;
			}
			
			// Remaining placements correspond to duplications for sure.
			for (; idx < uAts.length; ++idx) {
				uAts[idx] = lcBelows[idx] * rcBelows[idx] *
					2 * this.dupLossProbs.getDuplicationRate() * this.times.getSliceTime(x);
				// Move onto next pure discretisation point above.
				if (xi == this.times.getNoOfSlices(x)) {
					x = this.s.getParent(x);
					xi = 1;
				} else {
					++xi;
				}
			}
		}

		// Update planted tree probs. afterwards.
		this.updateBelowProbs(u);
	}

	/**
	 * Dynamic programming method for computing the probability of all realisations of
	 * planted tree G^u when ^the tip of u's parent arc is placed on point x_i.
	 * All viable placements x_i are tabulated.
	 * @param u the vertex of G.
	 */
	protected void updateBelowProbs(int u) {
		// x refers to point of tip of planted tree G^u.
		// y refers to point where u is placed (strictly below x).

		double length = this.lengths.get(u);
		double[] uAts = this.ats.get(u);
		double[] uBelows = this.belows.get(u);
		
		// Get limits.
		int x;
		int xi;
		if (this.g.isRoot(u)) {
			// Only very tip of host tree viable.
			x = this.s.getRoot();
			xi = this.times.getNoOfSlices(x) + 1;
		} else {
			x = (this.loLims.get(this.g.getParent(u)) << 16) >>> 16;
			xi = this.loLims.get(this.g.getParent(u)) >>> 16;
		}
		
		// For each x_i.
		for (int xcnt = 0; xcnt < uBelows.length; ++xcnt) {
			// Clear old value.
			uBelows[xcnt] = 0.0;
			// For each y_j strictly below x_i.
			int y = (this.loLims.get(u) << 16) >>> 16;
			int yj = this.loLims.get(u) >>> 16;
			double xt = this.times.getDiscretisationTime(x, xi);
			for (int ycnt = 0; ycnt < uAts.length; ++ycnt) {
				double yt = this.times.getDiscretisationTime(y, yj);
				double rateDens = this.g.isRoot(u) ? 1.0 : this.substPD.getPDF(length / (xt - yt));
				uBelows[xcnt] += rateDens * this.dupLossProbs.getP11Probability(x, xi, y, yj) * uAts[ycnt];
				// Move y_j onto next pure discretisation point above.
				if (yj == this.times.getNoOfSlices(y)) {
					y = this.s.getParent(y);
					yj = 1;
				} else {
					++yj;
				}
				if (y == x && yj >= xi) { break; }
			}
			// Move x_i onto next pure discretisation point above.
			if (xi == this.times.getNoOfSlices(x)) {
				x = this.s.getParent(x);
				xi = 1;
			} else {
				++xi;
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(65536);
		sb.append("Guest tree vertex:\tLower limit:\tNo of placements:\tType:\tDP ats:\tDP belows:\n");
		for (int u = 0; u < this.g.getNoOfVertices(); ++u) {
			sb.append(u).append('\t');
			sb.append((this.loLims.get(u) << 16) >>> 16).append('_').append(this.loLims.get(u) >>> 16).append('\t');
			sb.append(this.ats.get(u).length).append('\t');
			//sb.append((this.upLims.get(u) << 16) >>> 16).append('_').append(this.upLims.get(u) >>> 16).append('\t');
			sb.append(this.g.isLeaf(u) ? "Leaf" : (this.gsMap.isDuplication(u) ? "Duplication" : "Speciation/duplication")).append('\t');
			sb.append(Arrays.toString(this.ats.get(u))).append('\t');
			sb.append(Arrays.toString(this.belows.get(u))).append('\n');
		}
		return sb.toString();
	}

}
