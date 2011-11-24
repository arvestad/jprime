package se.cbb.jprime.apps.gsrf;

import java.util.Map;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.ProperDependent;
import se.cbb.jprime.topology.DoubleArrayMatrixMap;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;

/**
 * Point-wise duplication and loss probabilities for a
 * discretised host tree.
 * 
 * @author Joel Sjöstrand.
 */
public class DupLossProbs implements ProperDependent {

	/** Host tree. */
	protected RBTree s;
	
	/** Discretised times. */
	protected RBTreeArcDiscretiser times; 
	
	/** Duplication (birth) rate. */
	protected DoubleParameter lambda;
	
	/** Loss (death) rate. */
	protected DoubleParameter mu;
	
	/** P11 between points. */
	protected DoubleArrayMatrixMap p11;
	
	/** Death probabilites for planted subtrees. */
	protected DoubleMap extinction;
	
	/**
	 * Constructor.
	 * @param s
	 * @param times
	 * @param lambda
	 * @param mu
	 */
	public DupLossProbs(RBTree s, RBTreeArcDiscretiser times, DoubleParameter lambda, DoubleParameter mu) {
		this.s = s;
		this.times = times;
		this.lambda = lambda;
		this.mu = mu;
		this.p11 = new DoubleArrayMatrixMap(s.getNoOfVertices());
		this.extinction = new DoubleMap("extinction", s.getNoOfVertices());
	}

	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] { s, times, lambda, mu };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		ChangeInfo tci = changeInfos.get(this.times);
		int maxAffectedVertices = 8;
		if (changeInfos.get(this.s) == null && changeInfos.get(this.lambda) == null &&
			changeInfos.get(this.mu) == null && tci != null && tci.getAffectedElements() != null &&
			tci.getAffectedElements().length < maxAffectedVertices) {
			// TODO: Here one could find the parts of the tree spanned by
			// tci.getAffectedElements() and do partial update.
			this.p11.cache(null);
			this.extinction.cache(null);
			fullUpdate();
		} else {
			this.p11.cache(null);
			this.extinction.cache(null);
			fullUpdate();
		}
	}

	/**
	 * Performs a full update.
	 */
	private void fullUpdate() {
		this.computeP11AndExtinctionForArc(this.s.getRoot(), true);
		this.computeP11ForRootPath(this.s.getRoot(), true);
	}

	/**
	 * Partial update. Not implemented.
	 * @param affectedElements.
	 */
	private void partialUpdate(int[] affectedElements) {
		// TODO: Implement.
	}
	
	/**
	 * Helper. Computes P(t) and u_t for a given time. P(t) corresponds
	 * to 1-P_0(t) in Kendall's notation.
	 * @param t the time interval.
	 * @return [P(t),u_t].
	 */
	private double[] computePtAndUt(double t) {
		if (Math.abs(this.lambda.getValue() - this.mu.getValue()) < 1e-9) {
			double denom = 1.0 + (this.mu.getValue() * t);
			return new double[] { 1.0 / denom, (this.mu.getValue() * t) / denom };
		}
		else if (this.mu.getValue() < 1e-9) {
			//TODO: This was not allowed earlier. Why?
			return new double[] { 1.0, 1.0 - Math.exp(-this.lambda.getValue() * t)};
		}
		else {
			double dbDiff = this.mu.getValue() - this.lambda.getValue();
			double E = Math.exp(dbDiff * t);
			double denom = this.lambda.getValue() - (this.mu.getValue() * E);
			return new double[] {-dbDiff / denom, (this.lambda.getValue() * (1.0 - E)) / denom};
		}
	}

	@Override
	public void clearCache(boolean willSample) {
		this.p11.clearCache();
		this.extinction.clearCache();
	}

	@Override
	public void restoreCache(boolean willSample) {
		this.p11.restoreCache();
		this.extinction.restoreCache();
	}

	/**
	 * Returns the extinction probability of a planted tree S^x.
	 * @param x the arc of the planted subtree (equalling the arc's head vertex).
	 * @return the probability of extinction for a single lineage starting the tip of S^x.
	 */
	public double getExtinctionProbability(int x) {
		return this.extinction.get(x);
	}
	
	/**
	 * Retrieves p11 between two points on the discretized host tree.
	 * Indexing as follows. Index 0 is the speciation of an arc,
	 * index 1,...,k are the k pure discretization points, and index k+1
	 * is the speciation of the parent arc (or tip of the stem arc).
	 * @param x the ancestral arc (equalling the arc's head vertex).
	 * @param y the descendant arc (equalling the arc's head vertex).
	 * @param i the point on the ancestral arc.
	 * @param j the point on the descendant arc.
	 * @return p11 between point x_i and point y_j.
	 */
	public double getP11Probability(int x, int y, int i, int j) {
		return this.p11.get(x, y, (this.times.getNoOfPts(y) + 2) * i + j);
	}

	/**
	 * Computes and stores p11 and extinction probabilities for points within an arc.
	 * @param x
	 * @param doRecurse
	 */
	private void computeP11AndExtinctionForArc(int x, boolean doRecurse) {
		// Children must be updated first.
		if (doRecurse && !this.s.isLeaf(x))
		{
			computeP11AndExtinctionForArc(this.s.getLeftChild(x), true);
			computeP11AndExtinctionForArc(this.s.getRightChild(x), true);
		}

		// Compute Pt and ut for separately for inner segment and end segments
		// because of different time spans (dt and dt/2 respectively).
		double dt = this.times.getIntervalTime(x);
		double[] Ptut = computePtAndUt(dt);
		double[] PtutEnd = computePtAndUt(dt / 2.0);

		///////// POINT-TO-NODE PROBS. ////////

		// Probability of death below for a lineage at the current point.
		double D = this.s.isLeaf(x) ? 0.0 :
			this.extinction.get(this.s.getLeftChild(x)) * this.extinction.get(this.s.getRightChild(x));

		// No. of points is the no. of pure discretization points + the two endpoints.
		int sz = this.times.getNoOfPts(x) + 2;
		
		// Treat first segment separately since shorter time.
		double[] arcp11 = new double[sz * sz];
		arcp11[0] = 1.0;
		double p11 = (PtutEnd[0] * (1.0 - PtutEnd[1]) / ((1.0 - PtutEnd[1] * D) * (1.0 - PtutEnd[1] * D)));
		D = 1.0 - PtutEnd[0] * (1.0 - D) / (1.0 - PtutEnd[1] * D);
		arcp11[1 * sz + 0] = p11;

		// Remaining inner points.
		for (int i = 1 ; i < sz - 1; ++i) {
			p11 = (p11 * Ptut[0] * (1.0 - Ptut[1]) / ((1.0 - Ptut[1] * D) * (1.0 - Ptut[1] * D)));
			arcp11[i * sz + 0] = p11;
			D = 1.0 - Ptut[0] * (1.0 - D) / (1.0 - Ptut[1] * D);
		}

		// Again, special treatment of last segment since shorter time.
		p11 = (p11 * PtutEnd[0] * (1.0 - PtutEnd[1]) / ((1.0 - PtutEnd[1] * D) * (1.0 - PtutEnd[1] * D)));
		D = 1.0 - PtutEnd[0] * (1.0 - D) / (1.0 - PtutEnd[1] * D);
		arcp11[(sz - 1) * sz + 0] = p11;

		// Extinction probability in the planted tree S^node.
		this.extinction.set(x, D);

		///////// POINT-TO-POINT PROBS. ////////

		// Use Markovian property to compute probability for inner point pairs.
		// i is upper point, j is lower point.
		for (int j = 0; j < sz; ++j) {
			for (int i = j; i < sz; ++i) {
				if (j == i) {
					arcp11[sz * i + j] = 1.0;
				} else {
					arcp11[sz * i + j] = arcp11[sz * i + 0] / arcp11[sz * 0 + j];
				}
			}
		}
		
		// Finally, store the array.
		this.p11.set(x, x, arcp11);
	}

	/**
	 * For an arc y, computes and stores p11 for each proper ancestral arc x.
	 * @param y the arc (or arc with head vertex y, if you wish).
	 * @param doRecurse true to process all of the tree rooted at y.
	 */
	private void computeP11ForRootPath(int y, boolean doRecurse) {
		
		// Edge y refers to the lower most arc, i.e. the descendant edge.
		
		// Perform computations from leaves to root.
		if (!this.s.isLeaf(y) && doRecurse) {
			this.computeP11ForRootPath(this.s.getLeftChild(y), true);
			this.computeP11ForRootPath(this.s.getRightChild(y), true);
		}
		
		if (!this.s.isRoot(y)) {
			
			// No. of points on y, including speciation endpoints.
			int ySz = this.times.getNoOfPts(y) + 2;

			// Arc x refers to the ancestral arc. We are calculating p11 from
			// points on x to points on y.
			int x = this.s.getParent(y);

			// One-to-one for intermediate arcs between x and y, including losses.
			double p11ForIntermediateArcs = 1.0;

			// Loss for planted subtree ending in y but of other clade than x.
			double loss = this.extinction.get(this.s.getSibling(y));

			// p11 for points within descendant edge y.
			double[] yp11 = this.p11.get(y, y);
			
			// For each ancestral arc x, compute probabilities between points on x and points on y.
			while (true) {
				
				// No. of points on x, including speciation endpoints.
				int xSz = this.times.getNoOfPts(x) + 2;
				
				// p11 for points within ancestral arc y.
				double[] xp11 = this.p11.get(x, x);
				
				// What we're computing: p11 between x and y.
				double[] xyp11 = new double[xSz * ySz];

				// Compute p11 from the first point on x (the speciation)
				// to each point j on y.
				for (int j = 0; j < ySz; ++j) {
					xyp11[ySz * 0 + j] = p11ForIntermediateArcs * yp11[ySz * (ySz - 1) + j];
				}

				// The next iterations will be to points above the
				// speciation, so add the current loss factor to p11.
				p11ForIntermediateArcs *= loss;

				// For each point i on x:
				//    for each point j on y:
				//        calculate p11(a, b)
				for (int i = 1; i < xSz; ++i) {
					double p11ToSpec = xp11[xSz * i + 0];
					for (int j = 0; j < ySz; ++j) {
						xyp11[ySz * i + j] = p11ToSpec * p11ForIntermediateArcs * yp11[ySz * (ySz - 1) + j];
					}
				}

				// Store the values.
				this.p11.set(x, y, xyp11);
				
				// If we have completed the root we are done
				if (this.s.isRoot(x)) {
					break;
				}

				// Update p11 for intermediate arcs for the next round.
				p11ForIntermediateArcs *= xp11[xSz * (xSz - 1) + 0];
				loss = this.extinction.get(this.s.getSibling(x));
				x = this.s.getParent(x);
			}
		}
	}

}
