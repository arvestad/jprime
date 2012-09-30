package se.cbb.jprime.apps.dltrs;

import java.util.Map;

import se.cbb.jprime.apps.dltrs.ReconciliationHelper;
import se.cbb.jprime.io.SampleLogDouble;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.Model;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GenericMap;
import se.cbb.jprime.topology.RootedBifurcatingTreeParameter;
import se.cbb.jprime.topology.TreeAlgorithms;

/**
 * In accordance with the DTLRS model, computes the probability of
 * a guest tree topology and branch lengths given remaining parameters
 * and a "dated" host tree.
 * <p/>
 * DTLRS in short:<br/>
 * 1) A guest tree topology G evolves inside a host tree by means of
 * duplication, loss and transfer rates (say delta, mu, tau).
 * 2) Substitution rates for edges in the guest tree are drawn IID
 * from a gamma distribution with mean m and variance v. Rates r are
 * computed as r=l/t where l are the guest tree branch lengths and t are
 * the timespans in the host tree. This gives a relaxed molecular clock.
 * 3) Sequence evolution is guided by a substitution model of choice.
 * <p/>
 * The host tree topology S (including times t) is assumed to be known.
 * The probability Pr[G,l | delta,mu,tau,m,v] is approximated by discretising
 * S and considering all possible "dated" reconciliations of G and l with
 * respect to the discretisation using dynamic programming.
 * <p/>
 * Two approximations are made:<br/>
 * A) Of two adjacent duplication/transfer events in G (i.e. two connected
 * vertices), only one is allowed to occur in a discretisation interval.<br/>
 * B) The probability of the duplication/transfer event in the
 * interval is approximated by the event density at the midpoint multiplied
 * with the interval timestep.
 * <p/>
 * The original of this class was written in an inhumanly pace prior to Ali's
 * dissertation, so bear with me on the incomprehensibility of certain parts.
 * 
 * @author Joel Sjöstrand.
 */
public class DLTRSModel implements Model {

	/** The guest tree G. */
	protected RootedBifurcatingTreeParameter g;
	
	/** The host tree S. */
	protected RootedBifurcatingTreeParameter s;
	
	/** Reconciliations helper. */
	protected ReconciliationHelper reconcHelper;
	
	/** The branch lengths l. */
	protected DoubleMap lengths;
	
	/** P11 and similar info. */
	protected EpochDLTProbs dltProbs;
	
	/** Substitution rate distribution. */
	protected Continuous1DPDDependent substPD;
	
	/** Probability of rooted subtree G_u for each valid placement of u in S'. */
	protected GenericMap<EpochPtMap> ats;
	
	/** Probability of planted subtree G^u for each valid placement of tip of u's parent arc in S'. */
	protected GenericMap<EpochPtMap> belows;
	
	/**
	 * Constructor.
	 * @param g the guest tree G.
	 * @param s the host tree S.
	 * @param reconcHelper the reconciliations helper.
	 * @param lengths the branch lengths of G.
	 * @param dltProbs the duplication-loss-transfer probabilities over discretised S.
	 * @param substPD the iid rate probability distribution over arcs of G,
	 *  (relaxing the molecular clock).
	 */
	public DLTRSModel(RootedBifurcatingTreeParameter g, RootedBifurcatingTreeParameter s, ReconciliationHelper reconcHelper,
			DoubleMap lengths, EpochDLTProbs dltProbs, Continuous1DPDDependent substPD) {
		this.g = g;
		this.s = s;
		this.reconcHelper = reconcHelper;
		this.lengths = lengths;
		this.dltProbs = dltProbs;
		this.substPD = substPD;
		this.ats = new GenericMap<EpochPtMap>("DLTRS.ats", g.getNoOfVertices());
		this.belows = new GenericMap<EpochPtMap>("DLTRS.belows", g.getNoOfVertices());
				
		// Update.
		this.fullUpdate();
	} 
	
	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] {this.g, this.s, this.reconcHelper, this.lengths, this.dltProbs, this.substPD };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		ChangeInfo gci = changeInfos.get(this.g);
		ChangeInfo sci = changeInfos.get(this.s);
		ChangeInfo rhci = changeInfos.get(this.reconcHelper);
		ChangeInfo lci = changeInfos.get(this.lengths);
		ChangeInfo dpci = changeInfos.get(this.dltProbs);
		ChangeInfo rci = changeInfos.get(this.substPD);

		// One could think of many optimisations here, especially when there are 
		// time perturbations involved, possibly combined with length perturbations.
		// However, it is easy to make algorithmic mistakes in such situations,
		// so at the only moment solitary length changes result in a partial DP update.
		try {
			if (gci == null && sci == null && rhci == null && dpci == null && rci == null) {
				if (lci != null && lci.getAffectedElements() != null) {
					// Only certain branch lengths have changed. We do a partial update.
					
					int[] affected = TreeAlgorithms.getSpanningRootSubtree(this.g, lci.getAffectedElements());
					this.ats.cache(affected);
					this.belows.cache(affected);
					this.partialUpdate(affected);
					changeInfos.put(this, new ChangeInfo(this, "Partial DLTRS update", affected));
				} else if (lci != null) {
					this.ats.cache(null);
					this.belows.cache(null);
					this.fullUpdate();
					changeInfos.put(this, new ChangeInfo(this, "Full DLTRS update."));
				}
			} else {
				this.ats.cache(null);
				this.belows.cache(null);
				this.fullUpdate();
				changeInfos.put(this, new ChangeInfo(this, "Full DLTRS update."));
			}
		} catch (CloneNotSupportedException ex) {
		}
	}
	
	/**
	 * Helper. Updates all at-probabilities for the specified vertex u of G,
	 * i.e. the probabilities of the rooted subtree G_u for all valid
	 * placements of u. Requires that help structures are up-to-date,
	 * as well as u's children's probabilities in case of a
	 * non-recursive call. Note: leaf values are not stored since they
	 * can only be placed at sigma(u) by definition.
	 * @param u the vertex of G.
	 * @param doRecurse true to process children recursively first.
	 */
	private void updateAtProbs(int u, boolean doRecurse) {
		if (g.isLeaf(u)) {
			return;
		}
		
		if (doRecurse) {
			// Must do children first, if specified.
			updateAtProbs(g.getLeftChild(u), true);
			updateAtProbs(g.getRightChild(u), true);
		}
		
		// Retrieve placement bounds for u.
		// Note: Time index of upLim in epoch in question is <last.
		// Note: Time index of loLim in epoch in question is >0.
		int[] upLim = reconcHelper.getUpLim(u);
		int[] s = reconcHelper.getLoLim(u);
		
		// For each valid placement time s <= upLim.
		while (!(upLim[0] < s[0] || (!(s[0] < upLim[0]) && upLim[1] < s[1]))) {
			if (s[1] == 0) {
				atSpec(u, s);        // Speciation at s.
			} else {
				atDupOrTrans(u, s);  // Duplication or transfer at s.
			}
			s = reconcHelper.getEpochPtAbove(s);
		};
	}

	/**
	 * Helper. Invoked by updateAtProbs().
	 * Updates all at-probabilities for vertex u of G being
	 * placed at time s, where s is a speciation time, i.e. the
	 * first time of an epoch.
	 * @param u the non-leaf vertex of G.
	 * @param s the speciation time.
	 */
	private void atSpec(int u, int[] s) {
		int lc = g.getLeftChild(u);
		int rc = g.getRightChild(u);
		
		double[] ats = this.ats.get(u).get(s[0], s[1]);
		int[] sb = reconcHelper.getEpochPtBelow(s);
		int split = reconcHelper.getSplitIndex(s[0]);
		
		// Get speciation probability by multiplying lineage values
		// from children. At the moment, we set values for all remaining
		// contemporaries to 0.
		for (int i = 0; i < ats.length; ++i) {
			ats[i] = 0.0;
		}
		ats[split] = belows.get(lc).get(sb[0], sb[1], split) * belows.get(rc).get(sb[0], sb[1], split+1)
			+ belows.get(lc).get(sb[0], sb[1], split+1) * belows.get(rc).get(sb[0], sb[1], split);
	}
	
	/**
	 * Helper. Invoked by updateAtProbs().
	 * Updates all at-probabilities for vertex u of G being
	 * placed at time s, where s is not a speciation time, i.e.
	 * not the first time of an epoch. Recursively updates the lineage-
	 * probabilities for u's children first.
	 * @param u the non-leaf vertex of G.
	 * @param s the duplication/transfer time.
	 */
	private void atDupOrTrans(int u, int[] s) {
		// Note: We perform two approximations:
		// 1) Only one adjacent duplication/transfer event is allowed
		//    per subinterval.
		// 2) The probability of an event in a subinterval is estimated
		//    by multiplying the density for the event at the midpoint
		//    with the interval timestep.
		double dt = reconcHelper.getTimestep(s[0]);	
		
		int lc = g.getLeftChild(u);
		int rc = g.getRightChild(u);
		
		double[] ats = this.ats.get(u).get(s[0], s[1]);
		double dupFact = 2 * dltProbs.getDuplicationRate();
		double trFact = dltProbs.getTransferRate() / (ats.length - 1);
		
		// Compute probs for all planted subtrees G^lc and G^rc with
		// lineages starting at time s.
		updateBelowProbs(lc, s);
		updateBelowProbs(rc, s);
		
		// Compute probs for all rooted subtrees G_u at s.
		double[] lclins = belows.get(lc).get(s[0], s[1]);
		double[] rclins = belows.get(rc).get(s[0], s[1]);
		if (ats.length > 1) {
			double lcsum = 0.0;
			for (double val : lclins) {
				lcsum += val;
			}
			double rcsum = 0.0;
			for (double val : rclins) {
				rcsum += val;
			}
			for (int e = 0; e < ats.length; ++e) {
				ats[e] = dt * (dupFact * lclins[e] * rclins[e] +
					trFact * (lclins[e] * (rcsum - rclins[e]) + rclins[e] * (lcsum - lclins[e])));
			}
		} else {
			// Case with top time edge. No transfer possible.
			ats[0] = dt * dupFact * lclins[0] * rclins[0];
		}
	}
	
	/**
	 * Helper. Updates all probabilities for the planted subtree G^u
	 * when the lineage starts at time s. At-probabilites for all rooted
	 * subtrees G_u strictly below s must be up-to-date.
	 * @param u the vertex of G.
	 * @param s the time when the lineage leading to u starts.
	 */
	private void updateBelowProbs(int u, int[] s) {
		double sTime = reconcHelper.getTime(s);
		double l = lengths.get(u);
		double[] lins = belows.get(u).get(s[0], s[1]);
		int sz = lins.length;
		
		if (g.isLeaf(u)) {
			int sigma = this.reconcHelper.getHostLeafIndex(u);
			double rateDens = substPD.getPDF(l / sTime);  // Assumes leaf time 0.
			
			// For each edge e where lineage can start at time s.
			for (int e = 0; e < sz; ++e) {
				lins[e] = this.dltProbs.getOneToOneProbs().get(0, 0, sigma, s[0], s[1], e) * rateDens;
			}
		} else {
			// Reset values.
			for (int i = 0; i < sz; ++i) {
				lins[i] = 0.0;
			}
			
			// We always ignore last time index for at-probs of current epoch,
			// since such values are correctly stored at index 0 of next epoch.
			int[] t = this.reconcHelper.getLoLim(u);
			if (reconcHelper.isLastEpochTime(t)) {
				t = new int[] {t[0]+1, 0};
			}
			
			// For each valid time t where u can be placed (strictly beneath s).
			while (t[0] < s[0] || (!(s[0] < t[0]) && t[1] < s[1])) {
				double rateDens = substPD.getPDF(l / (sTime - reconcHelper.getTime(t)));
						
				// For each edge e where lineage can start at time s.
				double[] ats = this.ats.get(u).get(t[0], t[1]);
				for (int e = 0; e < sz; ++e) {
					// For each edge f where u can be placed at time t.
					for (int f = 0; f < ats.length; ++f) {
						lins[e] += dltProbs.getOneToOneProbs().get(t[0], t[1], f, s[0], s[1], e) * rateDens * ats[f];
					}
				}
				
				t = reconcHelper.getEpochTimeAboveNotLast(t);
			}
		}
	}

	/**
	 * Makes a full update.
	 */
	private void fullUpdate() {
		clearAtsAndBelows();
		updateAtProbs(g.getRoot(), true);
		updateBelowProbsForTop();
	}
	
	/**
	 * Helper. Works similarly to updateLinProbs() but for the root lineage
	 * of G starting at the very top of ES.
	 */
	private void updateBelowProbsForTop() {
		int[] sTop = reconcHelper.getEpochPtAtTop();
		updateBelowProbs(g.getRoot(), sTop);
	}
	
	/**
	 * Performs a partial DP update.
	 * It is assumed that limits and number of discretisation points
	 * are up-to-date.
	 * @param sortedAffectedVertices all affected vertices, sorted in reverse topological order.
	 */
	private void partialUpdate(int[] sortedAffectedVertices) {
		for (int u : sortedAffectedVertices) {
			this.updateAtProbs(u, false);
		}
		updateBelowProbsForTop();
	}
	
	/**
	 * Creates (and thus clears) the DP data structures.
	 * @param u the root of the subtree of G.
	 * @param noOfAncestors the number of ancestors of u.
	 */
	protected void clearAtsAndBelows() {
		EpochDiscretiser disc = reconcHelper.getDiscretisation();
		for (int u = 0; u < this.g.getNoOfVertices(); ++u) {
			this.ats.set(u, new EpochPtMap(disc));
			this.belows.set(u, new EpochPtMap(disc));
		}
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
		return "DLTRSModelProbability";
	}

	@Override
	public String getSampleValue() {
		return this.getDataProbability().toString();
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(65536);
		sb.append(prefix).append("DLTRS MODEL\n");
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
		sb.append(prefix).append("DLTRS MODEL\n");
		return sb.toString();
	}

	@Override
	public LogDouble getDataProbability() {
		// Return value for planted tree G^u with lineage
		// starting at tip of host tree.
		int uRoot = g.getRoot();
		double p = this.belows.get(uRoot).getTopmost();
		return new LogDouble(p);
	}

}
