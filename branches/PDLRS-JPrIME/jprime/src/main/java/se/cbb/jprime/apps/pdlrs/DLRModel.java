package se.cbb.jprime.apps.pdlrs;

import java.util.Arrays;
import java.util.Map;
import se.cbb.jprime.io.SampleLogDouble;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.InferenceModel;
import se.cbb.jprime.topology.DoubleArrayMap;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.RootedBifurcatingTreeParameter;
import se.cbb.jprime.topology.TreeAlgorithms;

/**
 * Implements the DLR part of the DLRS (a.k.a. GSR and GSRf) model.
 * Effectively, this class computes the conditional probability
 * Pr[G,l | S,t,lambda,mu,m,v], where G is the guest tree
 * topology, l the branch lengths of G, S the host tree, t the
 * divergence times of S, lambda the duplication (birth) rate,
 * mu the loss (death) rate,
 * m the substitution rate mean, and v the substitution rate variance.
 * <p/>
 * More specifically, this model computes the probability of G and l given
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
public class DLRModel implements InferenceModel {

	/** The guest tree G. */
	protected RootedBifurcatingTreeParameter g;
	
	/** The host tree S. */
	protected RootedBifurcatingTreeParameter s;
	
	/** Reconciliations helper. */
	protected ReconciliationHelper reconcHelper;
	
	/** The branch lengths l. */
	protected DoubleMap lengths;
	
	/** P11 and similar info. */
	protected DupLossProbs dupLossProbs;
	
	/** Substitution rate distribution. */
	protected Continuous1DPDDependent substPD;
	
	/** Pseudogenization switches. (to use its change infos so DLRModel wont get updated on its change)*/
	protected DoubleMap pgSwitches;
	
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
	 * @param reconcHelper the reconciliations helper.
	 * @param lengths the branch lengths of G.
	 * @param dupLossProbs the duplication-loss probabilities over discretised S.
	 * @param substPD the iid rate probability distribution over arcs of G,
	 *  (relaxing the molecular clock).
	 */
	public DLRModel(RootedBifurcatingTreeParameter g, RootedBifurcatingTreeParameter s, ReconciliationHelper reconcHelper,
			DoubleMap lengths, DupLossProbs dupLossProbs, Continuous1DPDDependent substPD) {
		this.g = g;
		this.s = s;
		this.reconcHelper = reconcHelper;
		this.lengths = lengths;
		this.dupLossProbs = dupLossProbs;
		this.substPD = substPD;
		this.ats = new DoubleArrayMap("DLR.ats", g.getNoOfVertices());
		this.belows = new DoubleArrayMap("DLR.belows", g.getNoOfVertices());
				
		// Update.
		this.fullUpdate();
	}
	
	/**
	 * Constructor.
	 * @param g the guest tree G.
	 * @param s the host tree S.
	 * @param reconcHelper the reconciliations helper.
	 * @param lengths the branch lengths of G.
	 * @param dupLossProbs the duplication-loss probabilities over discretised S.
	 * @param substPD the iid rate probability distribution over arcs of G,
	 *  (relaxing the molecular clock).
	 * @param pgSwitches the pseudogenization switches for any branches of gene tree
	 */
	public DLRModel(RootedBifurcatingTreeParameter g, RootedBifurcatingTreeParameter s, ReconciliationHelper reconcHelper,
			DoubleMap lengths, DupLossProbs dupLossProbs, Continuous1DPDDependent substPD, DoubleMap pgSwitches) {
		this.g = g;
		this.s = s;
		this.reconcHelper = reconcHelper;
		this.lengths = lengths;
		this.dupLossProbs = dupLossProbs;
		this.substPD = substPD;
		this.ats = new DoubleArrayMap("DLR.ats", g.getNoOfVertices());
		this.belows = new DoubleArrayMap("DLR.belows", g.getNoOfVertices());
		this.pgSwitches = pgSwitches;
		
		// Update.
		this.fullUpdate();
	}
	
	
	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] {this.g, this.s, this.reconcHelper, this.lengths, this.dupLossProbs, this.substPD };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		ChangeInfo gci = changeInfos.get(this.g);
		ChangeInfo sci = changeInfos.get(this.s);
		ChangeInfo rhci = changeInfos.get(this.reconcHelper);
		ChangeInfo lci = changeInfos.get(this.lengths);
		ChangeInfo dpci = changeInfos.get(this.dupLossProbs);
		ChangeInfo rci = changeInfos.get(this.substPD);
		ChangeInfo pgchi = changeInfos.get(this.pgSwitches);
		try{
		if(!(gci == null && sci == null && rhci == null && dpci == null && rci == null && lci == null)) // removed  && pgchi != null
		{
			// One could think of many optimisations here, especially when there are 
			// time perturbations involved, possibly combined with length perturbations.
			// However, it is easy to make algorithmic mistakes in such situations,
			// so at the only moment solitary length changes result in a partial DP update.
			if (gci == null && sci == null && rhci == null && dpci == null && rci == null) {
				if (lci != null && lci.getAffectedElements() != null ) {
					// Only certain branch lengths have changed. We do a partial update.
					
					int[] affected = TreeAlgorithms.getSpanningRootSubtree(this.g, lci.getAffectedElements());
	//				System.out.println("Affected vertices are:");
	//				for (int iii=0; iii<affected.length; iii++)
	//					System.out.print(affected[iii] + " ");
	//				System.out.println();
					this.ats.cache(affected);
					this.belows.cache(affected);
					this.partialUpdate(affected);
					changeInfos.put(this, new ChangeInfo(this, "Partial DLR update", affected));
				} else if (lci != null) {
					this.ats.cache(null);
					this.belows.cache(null);
					this.fullUpdate();
					changeInfos.put(this, new ChangeInfo(this, "Full DLR update."));
				}
			} else {
				this.ats.cache(null);
				this.belows.cache(null);
				this.fullUpdate();
				changeInfos.put(this, new ChangeInfo(this, "Full DLR update."));
			}
		}
		}catch(Exception e)
		{e.printStackTrace();}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.ats.clearCache();
		this.belows.clearCache();
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.ats.restoreCache();
		this.belows.restoreCache();
	}

	@Override
	public Class<?> getSampleType() {
		return SampleLogDouble.class;
	}

	@Override
	public String getSampleHeader() {
		return "DLRModelDensity";
	}

	@Override
	public String getSampleValue(SamplingMode mode) {
		return this.getDataProbability().toString();
	}

	@Override
	public LogDouble getDataProbability() {
		return new LogDouble(this.belows.get(this.g.getRoot(), 0));
	}
	
	
	/**
	 * Creates (and thus clears) the DP data structures.
	 * @param u the root of the subtree of G.
	 * @param noOfAncestors the number of ancestors of u.
	 */
	protected void clearAtsAndBelows() {
		int[] nos = this.reconcHelper.getNoOfPlacements();
		for (int u = 0; u < this.g.getNoOfVertices(); ++u) {
			this.ats.set(u, new double[nos[u]]);
			if (this.g.isRoot(u)) {
				this.belows.set(u, new double[1]);  // Only tip of host tree.
			} else {
				this.belows.set(u, new double[nos[this.g.getParent(u)]]);
			}	
		}
	}
	
	/**
	 * Performs a full DP update.
	 */
	protected void fullUpdate() {
		int r = this.g.getRoot();
		this.clearAtsAndBelows();
		this.updateAtProbs(r, true);
	}
	
	/**
	 * Performs a partial DP update.
	 * It is assumed that lower limits and number of discretisation points
	 * are up-to-date.
	 * @param sortedAffectedVertices all affected vertices, sorted in reverse topological order.
	 */
	private void partialUpdate(int[] sortedAffectedVertices) {
		for (int u : sortedAffectedVertices) {
			this.updateAtProbs(u, false);
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
			int[] x_i = this.reconcHelper.getLoLim(u);
			int idx = 0;                                // No. of processed viable placements.

			double[] uAts = this.ats.get(u);
			double[] lcBelows = this.belows.get(lc);
			double[] rcBelows = this.belows.get(rc);
			
			// First placement might correspond to a speciation.
			if (x_i[1] == 0) {
				uAts[0] = lcBelows[0] * rcBelows[0];
				++idx;
				++x_i[1];
			}
			
//			try{
			// Remaining placements correspond to duplications for sure.
			for (; idx < uAts.length; ++idx) {
				uAts[idx] = lcBelows[idx] * rcBelows[idx] *
					2 * this.dupLossProbs.getDuplicationRate() * this.reconcHelper.getSliceTime(x_i);
				// Move onto next pure discretisation point above.
				this.reconcHelper.incrementPt(x_i);
			}
//			}catch(Exception e)
//			{System.out.println("Array index out of bound error");}
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
		int[] x_i = (this.g.isRoot(u) ? this.reconcHelper.getTipPt() : this.reconcHelper.getLoLim(this.g.getParent(u)));
		
		// For each x_i.
		for (int xcnt = 0; xcnt < uBelows.length; ++xcnt) {
			// Clear old value.
			uBelows[xcnt] = 0.0;
			// For each y_j strictly below x_i.
			int[] y_j = this.reconcHelper.getLoLim(u);
			double xt = this.reconcHelper.getDiscretisationTime(x_i);
			for (int ycnt = 0; ycnt < uAts.length; ++ycnt) {
				double yt = this.reconcHelper.getDiscretisationTime(y_j);
				// Note: We now allow edge rates over stem arc as well.
				double rateDens = this.substPD.getPDF(length / (xt - yt));
				double p11 = this.dupLossProbs.getP11Probability(x_i[0], x_i[1], y_j[0], y_j[1]);
				uBelows[xcnt] += rateDens * p11 * uAts[ycnt];
				// Move y_j onto next pure discretisation point above.
				this.reconcHelper.incrementPt(y_j);
				if (y_j[0] == x_i[0] && y_j[1] >= x_i[1]) { break; }
			}
			// Move x_i onto next pure discretisation point above.
			this.reconcHelper.incrementPt(x_i);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(65536);
		sb.append("Guest tree vertex:\tLower limit:\tNo of placements:\tType:\tDP ats:\tDP belows:\n");
		for (int u = 0; u < this.g.getNoOfVertices(); ++u) {
			sb.append(u).append('\t');
			sb.append(this.reconcHelper.getLoLimAsString(u)).append('\t');
			sb.append(this.ats.get(u).length).append('\t');
			//sb.append((this.upLims.get(u) << 16) >>> 16).append('_').append(this.upLims.get(u) >>> 16).append('\t');
			sb.append(this.g.isLeaf(u) ? "Leaf" : (this.reconcHelper.isDuplication(u) ? "Duplication" : "Speciation/duplication")).append('\t');
			sb.append(Arrays.toString(this.ats.get(u))).append('\t');
			sb.append(Arrays.toString(this.belows.get(u))).append('\n');
		}
		return sb.toString();
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(65536);
		sb.append(prefix).append("DLR MODEL\n");
		sb.append(prefix).append("Number of vertices of host tree: ").append(this.s.getNoOfVertices()).append('\n');
		sb.append(prefix).append("Number of vertices of guest tree: ").append(this.g.getNoOfVertices()).append('\n');
		sb.append(prefix).append("IID edge rate distribution: ").append(this.substPD.getName()).append('\n');
		sb.append(prefix).append("Reconciliation helper:\n");
		sb.append(this.reconcHelper.getPreInfo(prefix + '\t'));
		//sb.append(this.toString());
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(prefix).append("DLR MODEL\n");
		return sb.toString();
	}

	@Override
	public String getModelName() {
		return "DLR";
	}

}
