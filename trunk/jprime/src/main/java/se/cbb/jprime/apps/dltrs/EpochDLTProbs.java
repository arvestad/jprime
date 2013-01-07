package se.cbb.jprime.apps.dltrs;

import java.util.Map;

import se.cbb.jprime.math.ODEExternalSolutionProvider;
import se.cbb.jprime.math.ODEFunction;
import se.cbb.jprime.math.ODESolver;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.InfoProvider;
import se.cbb.jprime.mcmc.ProperDependent;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;

/**
 * Holder of duplication, loss and lateral transfer rates for a
 * discretised host tree with epochs. In addition, the
 * class keeps precomputed probabilities related to the rates for swift
 * access. The precomputed values are first solved numerically for points
 * within each epoch with aid of an ODE solver. Probabilities
 * between points of different epochs are then assembled without need of
 * the solver.
 * <p/>
 * The original of this class was written in an inhumanly pace prior to Ali's
 * dissertation, so bear with me on the incomprehensibility of certain parts.
 * 
 * @author Joel Sjöstrand.
 */
public class EpochDLTProbs implements ProperDependent, ODEFunction, ODEExternalSolutionProvider, InfoProvider {
	
	/** Relative tolerance for each component during ODE solving. */
	public static final double REL_TOL = 1e-6;
	
	/** Absolute tolerance for each component during ODE solving. */
	public static final double ABS_TOL = 1e-6;
	
	/** ODE solver. */
	private ODESolver solver;
	
	/** The discretised tree. */
	private RBTreeEpochDiscretiser discTree;
	
	/** Duplication rate. */
	private DoubleParameter dupRate;
	
	/** Loss rate. */
	private DoubleParameter lossRate;
	
	/** Transfer rate. */
	private DoubleParameter transRate;
	
	/**
	 * Affects model characteristics: true to let transfer probability be normalised
	 * by 1 / # of contemp. species in S; false to perform no normalisation w.r.t. S.
	 */
	private boolean adjustTransferProbabilityOverS;
	
	/**
	 * "Extinction probabilities": prob. of a sole lineage starting at a point
	 * having no descendants at leaves. Stored for every point in the tree.
	 */
	private EpochPtMap m_Qe;
	
	/**
	 * "One-to-one probabilities": prob. of a single lineage starting at
	 * time s in arc e having a sole mortal descendant at time t in arc f.
	 * Stored for pair of points in the tree.
	 */
	private EpochPtPtMap m_Qef;
	
	/** ODE work var.: epoch index. */
	private int wi;
	
	/** ODE work var.: lower current time index in epoch. */
	private int wt;
	
	/** ODE work var.: upper current time index in epoch. */
	private int ws;
	
	/** ODE work var.: last time index in epoch. */
	private int wlast;
	
	/** ODE work var.: number of arcs in epoch. */
	private int wn;
	
	/** ODE work var.: transferRate/(wn-1) or simply transferRate depending on adjustment flag. */
	private double wnorm;
	
	/**
	 * Constructor.
	 * @param EDS the "epochised" discretised host tree.
	 * @param dup the duplication rate.
	 * @param loss the loss rate.
	 * @param trans the lateral transfer rate.
	 * @param adjust true to adjust the probability of transfer by normalising with the number of contemporary host tree arcs.
	 */
	public EpochDLTProbs(RBTreeEpochDiscretiser ed, DoubleParameter dup, DoubleParameter loss, DoubleParameter trans, boolean adjust) {
		this.solver = new ODESolver(this, this, true, REL_TOL, ABS_TOL);
		this.discTree = ed;
		this.dupRate = dup;
		this.lossRate = loss;
		this.transRate = trans;
		this.m_Qe = new EpochPtMap(ed);
		this.m_Qef = new EpochPtPtMap(ed);
		this.wi = 0;
		this.wt = 0;
		this.ws = 0;
		this.wlast = 0;
		this.wn = 0;
		this.wnorm = 0;
		this.adjustTransferProbabilityOverS = adjust;
		
		this.update();
	}
    
	/**
	 * Updates the internally stored probabilities.
	 */
	private void update() {
		// Recompute probabilities.
		calcProbsWithinEpochs();
		calcProbsBetweenEpochs();
	}
	
	/**
	 * Returns the duplication rate.
	 * @return the duplication rate.
	 */
	public double getDuplicationRate() {
		return dupRate.getValue();
	}
	
	/**
	 * Returns the loss rate.
	 * @return the loss rate.
	 */
	public double getLossRate() {
		return lossRate.getValue();
	}
	
	/**
	 * Returns the lateral transfer rate.
	 * @return the lateral transfer rate.
	 */
	public double getTransferRate() {
		return transRate.getValue();
	}
	
	/**
	 * Returns the sum of all three rates.
	 * @return the sum of all rates.
	 */
	public double getRateSum() {
		return (dupRate.getValue() + lossRate.getValue() + transRate.getValue());
	}
	
	/**
	 * Returns a reference to the precomputed one-to-one
	 * ("function p11") probabilities for all pairs of
	 * discretisation points of the tree. p11(s,t) can be
	 * interpreted as the probability of a single lineage
	 * starting at discretisation point s having one sole
	 * mortal descendant at point t below, while the remaining
	 * descendants are ghosts destined to go extinct.
	 * A point is referenced using a 3-tuple;
	 * (epochIndex,timeInEpochIndex,arcInEpochIndex).  
	 * The returned matrix is essentially triangular.
	 * @return the p11 probabilities for pairs of points.
	 */
	public EpochPtPtMap getOneToOneProbs() {
		return m_Qef;
	}
	
	/**
	 * Returns a reference to the precomputed extinction
	 * probabilities for all discretisation points of
	 * the tree. For a point p, it states the probability of
	 * a single lineage starting at p having no descendants
	 * among the leaves.
	 * A point is referenced using a 3-tuple;
	 * (epochIndex,timeInEpochIndex,arcInEpochIndex).
	 * @return the extinction probabilities for all points.
	 */
	public EpochPtMap getExtinctionProbs() {
		return m_Qe;
	}
	
	/**
	 * Performs computation of derivative of extinction and one-to-one
	 * probabilities during ODE solving.
	 * @param t current solver time value.
	 * @param Q current solver extinction and one-to-one probabilities.
	 * @param dQdt the derivatives to be computed.
	 */
	@Override
	public void evaluate(double x, double[] Q, double[] dQdt) {
		// Defines the organisation of solver's concatenated vectors.
		// First wn elements are reserved for extinction probs, Qe.
		// Next wn*wn elements are one-to-one probs, Qef.
		
		// Compute sum of Qe.
		double sumqe = 0.0;
		for (int i = 0; i < wn; ++i) {
			sumqe += Q[i];
		}
		
		// For each f (sic!), compute sum of Qef.
		double[] sumqxf = new double[wn];
		for (int e = 0; e < wn; ++e) {
			for (int f = 0, ef = e * wn + wn; f < wn; ++f, ++ef) {
				sumqxf[f] += Q[ef];
			}
		}
		
		// Compute derivatives.
		double d = this.dupRate.getValue();
		double l = this.lossRate.getValue();
		double t = this.transRate.getValue();
		double rateSum = d + l + t;
		for (int e = 0; e < wn; ++e) {
			double qe = Q[e];
			double sumqg = sumqe - qe;
			
			// dQedt = delta*Qe(t)^2 + tau/(n-1)*Qe*sum_{f in E\e}Qf(t) + mu - phi*Qe(t).
			dQdt[e] = d * qe * qe + wnorm * qe * sumqg + l - rateSum * qe;
			
			// dQefdt = 2*delta*Qe(t)*Qef(t,t0) + tau/(n-1)*(Qe(t)*sum_{g in E\e}Qgf(t,t0) +
			// Qef(t,t0)*sum_{g in E\e}Qg(t)) - phi*Qef(t,t0).
			for (int f = 0, ef = e * wn + wn; f < wn; ++f, ++ef) {
				double qef = Q[ef];
				dQdt[ef] = 2 * d * qe * qef + wnorm * (qe * (sumqxf[f] - qef) + qef * sumqg) - rateSum * qef;
			}
		}
	}
	
	/**
	 * Solver callback. Stores point probabilities for current epoch during solving.
	 * @param no solver iteration number (irrelevant).
	 * @param told previous solver t.
	 * @param t current solver t.
	 * @param Q current solver values at t.
	 * @return code always indicating that solution has not been altered.
	 */
	public SolutionProviderResult solout(int no, double told, double t, double[] Q) {	
		// Store probabilities for discretised times the solver has passed.
		// Extinction probs. need only be stored once for an epoch, and are
		// reused in later iterations.
		// Since solver may in rare cases return negative values v=0-eps,
		// we always store max(v,0), without altering solver's current solution.
		
		while (ws <= wlast && discTree.getEpoch(wi).getTime(ws) < t + 1e-8) {
			double[] it = Q;
			if (Math.abs(t - discTree.getEpoch(wi).getTime(ws)) > 1e-8) {
				// If not on a discretisation time, interpolate.
				it = new double[Q.length];
				solver.contd5(it, discTree.getEpoch(wi).getTime(ws));
			}
			
			// Store values for time-tuple (s,t).
			if (wt == 0) {
				m_Qe.setWithMin(wi, ws, it, 0, 0.0);
			}
			m_Qef.setWithMin(wi, wt, wi, ws, it, wn, 0.0);
			
			++ws;
		}
		return SolutionProviderResult.SOLUTION_NOT_CHANGED;
	}
	
	/**
	 * Helper. For all epochs, computes and stores
	 * point-to-point probabilities for points within
	 * the current epoch. Calculations require solving a
	 * an ODE system, and are carried out numerically
	 * using a Runge-Kutta solver.
	 */
	private void calcProbsWithinEpochs() {
		// We start iterating at leaf epoch.
		wi = 0;
		wlast = discTree.getEpoch(0).getNoOfTimes() - 1;
		wn = discTree.getEpoch(0).getNoOfArcs();
		wnorm = (this.adjustTransferProbabilityOverS ? transRate.getValue() / (wn - 1) : transRate.getValue());
		
		// The vector of components used in ODE solving, Q, is concatenated this way
		// (all with respect to the single current epoch):
		// First wn elements are Qe(t), corresponding to prob. of extinction for
		// a single lineage at time t in arc e.
		// Next wn*wn elements are Qef(s,t), denoting prob. of single surviving mortal
		// at time t in arc f when a single lineage starts at time s in arc e.
		double[] Q = new double[wn + wn * wn];
		
		// Initial values at t=0 for leaf epoch:
		// Qe=0, while Qee=1 and Qef=0 where e!=f.
		setInitVals(Q);
		
		// For each epoch i strictly below top time epoch.
		while (wn > 1) {
			// For each lower discretised time wt.
			for (wt = 0; wt <= wlast; ++wt) {
				// Initial values. Already set for wt==0.
				if (wt > 0) {
					System.arraycopy(m_Qe.get(wi, wt), 0, Q, 0, wn);   // First wn element is extinction values at t.
					setInitVals(Q);                                    // Remaining wn*wn is unit matrix.
				}
				
				// For each upper discretised time ws >= wt.
				ws = wt;
				double t = discTree.getEpoch(wi).getTime(ws);
				
				if (ws == wlast) {
					// Explicitly store probs. for ws==wt==wlast.
					this.solout(-1, t, t, Q);
				} else {
					// Solve ODE system from wt to up to wlast.
					// Probs. for ws = 0,...,wlast are implicitly stored
					// in solout() callbacks.
					double h = 0;
					solver.dopri5(t, discTree.getEpoch(wi).getUpperTime(), Q, h);
				}
			}
			
			// Update Q for next epoch by merging extinction values of the two arcs
			// that joined moving upwards.
			++wi;
			int split = discTree.getSplitIndex(wi);
			Q[split] = Q[split] * Q[split + 1];   // Works due to arc indexing.
			// Resize for next generation.
			--wn;
			double[] tmp = Q;
			Q = new double[wn + wn * wn];
			for (int i = 0; i < wn + 1; ++i) {
				if (i != split + 1) { Q[i < split + 1 ? i : i - 1] = tmp[i]; }
			}
			wlast = discTree.getEpoch(wi).getNoOfTimes() - 1;
			wnorm = (this.adjustTransferProbabilityOverS ? transRate.getValue() / (wn - 1) : transRate.getValue());
			setInitVals(Q);
		}
		
		// Compute probabilities for top time arc "Kendall-way"
		// since no transfers may take place here.
		assert(Q.length == 1 + 1 * 1);
		double D = Q[0];
		double o2o = 1.0;
		double[] PtutFull = calcPtAndUt(discTree.getEpoch(wi).getTimestep());
		double[] PtutHalf = calcPtAndUt(discTree.getEpoch(wi).getTimestep() / 2.0);
		for (wt = 0; wt <= wlast; ++wt) {
			if (wt > 0) {
				D = m_Qe.get(wi, wt, 0);
				o2o = 1.0;
			}
			for (ws = wt; ws <= wlast; ++ws) {
				// Store values more less same way as in solout().
				if (wt == 0) {
					m_Qe.set(wi, ws, 0, D);
				}
				m_Qef.set(wi, wt, 0, wi, ws, 0, o2o);
				
				// Update for next upper endpoint. Only half timestep at epoch boundaries.
				boolean halfTimestep = (ws == 0 && wt == 0) || (ws + 1 == wlast);
				double Pt = halfTimestep ? PtutHalf[0] : PtutFull[0];
				double ut = halfTimestep ? PtutHalf[1] : PtutFull[1];
				o2o = o2o * Pt * (1.0 - ut) / ((1.0 - ut * D) * (1.0 - ut * D));
				D = 1.0 - Pt * (1.0 - D) / (1.0 - ut * D);
			}
		}
	}
	
	/**
	 * Helper. For all epochs, computes and stores
	 * point-to-point probabilities for points of different
	 * epochs. Is based on within-epoch values, meaning
	 * that calcProbsWithinEpochs() must be invoked first.
	 */
	private void calcProbsBetweenEpochs() {
		// For every upper epoch i.
		for (int i = 1; i < discTree.getNoOfEpochs(); ++i) {
			// For every lower epoch j strictly beneath i.
			for (int j = 0; j < i; ++j) {
				calcProbsBetweenEpochs(i, j);
			}
		}
	}

	
	/**
	 * Helper. Computes probabilities for all points
	 * between epoch i and epoch j, i>j. Probs. for
	 * any epoch k where i>k>j must be up-to-date, as well
	 * as in-epoch probs. for i and j.
	 * @param i index of upper epoch.
	 * @param j index of lower epoch. 
	 */
	private void calcProbsBetweenEpochs(int i, int j) {
		Epoch epi = discTree.getEpoch(i);
		Epoch epj = discTree.getEpoch(j);
		int lastti = epi.getNoOfTimes() - 1;
		int lastei = epi.getNoOfArcs() - 1;
		int lasttj = epj.getNoOfTimes() - 1;
		int lastej = epj.getNoOfArcs() - 1;
		
		// Let z refer to epoch just below i.
		int z = i - 1;
		Epoch epz = discTree.getEpoch(z);
		int lastzt = epz.getNoOfTimes() - 1;
		
		// Edge number g in epoch i split into arcs number
		// g and g+1 in epoch z.
		int g = discTree.getSplitIndex(i);
		double Dgp = m_Qe.getForLastTime(z, g);
		double Dgb = m_Qe.getForLastTime(z, g + 1);
		
		// For every upper point time s.
		for (int s = 0; s <= lastti; ++s) {
			// For every upper point arc e.
			for (int e = 0; e <= lastei; ++e) {
				// For every lower point time t.
				for (int t = 0; t <= lasttj; ++t) {
					// For every lower point arc f.
					for (int f = 0; f <= lastej; ++f) {
						// COMPUTE PROBABILITY Qef(j,t,f,i,s,e).
												
						// First treat case with arc g.
						double qef = m_Qef.get(i, 0, g, i, s, e) * (m_Qef.get(j, t, f, z, lastzt, g) * Dgb + m_Qef.get(j, t, f, z, lastzt, g + 1) * Dgp);
											
						// For every arc h in epoch i besides g. hb refers to same arc, but below.
						for (int h = 0, hb = 0; h <= lastei; ++h, ++hb) {
							if (h == g) {
								++hb;
								continue;
							}
							qef += m_Qef.get(i, 0, h, i, s, e) * m_Qef.get(j, t, f, z, lastzt, hb);
						}
						m_Qef.set(j, t, f, i, s, e, qef);
					}
				}
			}
		}
	}
	
	/**
	 * Helper. Computes P(t) and u_t "Kendall-way".
	 * @param t size of time interval.
	 * @return [P(t), u_t].
	 */
	private double[] calcPtAndUt(double t) {
		double dup = dupRate.getValue();
		double loss = lossRate.getValue();
		if (Math.abs(dup - loss) < 1e-9) {
			double denom = 1.0 + (loss * t);
			return new double[] { 1.0 / denom, (loss * t) / denom };
		} else if (loss < 1e-9) {
			return new double[] { 1.0, 1.0 - Math.exp(-dup * t) };
		} else {
			double dbDiff = loss - dup;
			double E = Math.exp(dbDiff * t);
			double denom = dup - (loss * E);
			return new double[] { -dbDiff / denom, (dup * (1.0 - E)) / denom };
		}
	}
	
	/**
	 * Helper. Sets initial values to the specified
	 * vector. Used during ODE solving.
	 * @param Q sets n*n initial values to Q (Q should already
	 *        contain extinction values in the first n positions).
	 */
	private void setInitVals(double[] Q) {
		// First clear.
		for (int i = 0; i < wn * wn; ++i) {
			Q[i + wn] = 0.0;
		}
		// ID matrix for Qef.
		for (int e = 0; e < wn; ++e) {
			Q[e * wn + e + wn] = 1.0;
		}
	}

	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { this.discTree, this.dupRate, this.lossRate, this.transRate };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		// Cache.
		this.m_Qe.cache();
		this.m_Qef.cache();
		
		// Update.
		if (changeInfos.get(this.discTree) != null) {
			// Discretisation or times have changed. Reinitialise place-holders.
			this.m_Qe = new EpochPtMap(this.discTree);
			this.m_Qef = new EpochPtPtMap(discTree);
		}
		this.update();
		changeInfos.put(this, new ChangeInfo(this, "EpochDLTProbs full update"));
	}

	@Override
	public void clearCache(boolean willSample) {
		this.m_Qe.clearCache();
		this.m_Qef.clearCache();
	}

	@Override
	public void restoreCache(boolean willSample) {
		m_Qe.restoreCache();
		m_Qef.restoreCache();
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder oss = new StringBuilder(4096);
		oss.append(prefix).append("EPOCH DLT PROBS\n");
		oss.append(prefix).append("Initial duplication rate: ").append(dupRate.getValue()).append('\n');
		oss.append(prefix).append("Initial loss rate: ").append(lossRate.getValue()).append('\n');
		oss.append(prefix).append("Initial transfer rate: ").append(transRate.getValue()).append('\n');
		oss.append(prefix).append("Relative ODE tolerance: ").append(REL_TOL).append('\n');
		oss.append(prefix).append("Absolute ODE tolerance: ").append(ABS_TOL).append('\n');
		return oss.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		return "EPOCH DLT PROBS\n";
	}
	
	@Override
	public String toString() {
		StringBuilder oss = new StringBuilder(65536);
		oss.append("# Extinction probs Qe:\n").append(m_Qe.toString()).append('\n');
		oss.append("# One-to-one probs Qef:\n").append(m_Qef.toString()).append('\n');
		return oss.toString();
	}
	
	/**
	 * Returns the total arc time of the entire tree.
	 * @return the total timespan.
	 */
	public double getTotalArcTime() {
		return this.discTree.getTotalArcTime();
	}
	
	/**
	 * Returns the transfer model characteristics flag: true means transfer probability is normalised
	 * by 1 / # of contemp. species in host tree; false means perform no normalisation w.r.t. host tree.
	 * @return the flag.
	 */
	public boolean getTransferProbabilityAdjustment() {
		return this.adjustTransferProbabilityOverS;
	}
	
	/**
	 * Sets the transfer model characteristics flag: true means transfer probability is normalised
	 * by 1 / # of contemp. species in host tree; false means perform no normalisation w.r.t. host tree.
	 * @param doAdjust the flag.
	 */
	public void setTransferProbabilityAdjustment(boolean doAdjust) {
		this.adjustTransferProbabilityOverS = doAdjust;
	}
}
