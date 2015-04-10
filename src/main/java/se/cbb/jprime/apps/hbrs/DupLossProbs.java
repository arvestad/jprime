//package se.cbb.jprime.apps.hbrs;
//
//import java.util.Map;
//
//import se.cbb.jprime.mcmc.ChangeInfo;
//import se.cbb.jprime.mcmc.Dependent;
//import se.cbb.jprime.mcmc.DoubleParameter;
//import se.cbb.jprime.mcmc.ProperDependent;
//import se.cbb.jprime.topology.HybridGraph;
//
///**
// * Calculates and stores p11 for the hybrid host DAG.
// * Right now assumes fixed DAG, and fixed post-hybridisation "zone", but listens for dup/loss rate changes.
// * 
// * @author Joel Sjöstrand.
// */
//public class DupLossProbs implements ProperDependent {
//
//	/** Host DAG. */
//	protected HybridGraph dag;
//	
//	/** Duplication (birth) rate. */
//	protected DoubleParameter lambda;
//	
//	/** Loss (death) rate. */
//	protected DoubleParameter mu;
//	
//	/** Duplication change factor. */
//	protected DoubleParameter lambdaPostHyb;
//	
//	/** Loss change factor. */
//	protected DoubleParameter muPostHyb;
//
//	/** Post-hybridisation time zone. Internally, this is rounded off to a the nearest discretisation interval boundary */
//	protected double postHybTime;
//	
//	/**
//	 * Constructor.
//	 * @param dag the hybrid graph.
//	 * @param lambda duplication rate parameter.
//	 * @param mu loss rate parameter.
//	 * @param lambdaPostHyb post-hybridisation change factor.
//	 * @param muPostHyb post-hybridisation change factor.
//	 * @param postHybTime post-hybridisation time.
//	 */
//	public DupLossProbs(HybridGraph dag, DoubleParameter lambda, DoubleParameter mu, DoubleParameter lambdaPostHyb, DoubleParameter muPostHyb, double postHybTime) {
//		this.dag = dag;
//		this.lambda = lambda;
//		this.mu = mu;
//		this.lambdaPostHyb = lambdaPostHyb;
//		this.muPostHyb = muPostHyb;
//		this.postHybTime = postHybTime;
//		this.update();
//	}
//	
//	/**
//	 * 
//	 */
//	private void update() {
//		
//	}
//
//	@Override
//	public Dependent[] getParentDependents() {
//		return new Dependent[] { lambda, mu };
//	}
//
//	@Override
//	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos,
//			boolean willSample) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void clearCache(boolean willSample) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void restoreCache(boolean willSample) {
//		// TODO Auto-generated method stub
//		
//	}
// 
//	/**
//	 * Returns the extinction probability of a planted tree S^x.
//	 * @param x the arc of the planted subtree (equalling the arc's head vertex).
//	 * @return the probability of extinction for a single lineage starting the tip of S^x, (i.e., at
//	 * the tail of the arc (p(x),x).
//	 */
//	public double getExtinctionProbability(int x) {
//		return this.extinction.get(x);
//	}
//	
//	/**
//	 * Computes and stores p11 and extinction probabilities for points within an arc (p(x),x).
//	 * If x is an alloploidic hybridisation, there are in fact two parents p1 and p2, but this
//	 * does not cause any problem since no guest vertices can be placed onto these arcs.
//	 * All temporally lower vertices are assumed to have been processed.
//	 * @param x head vertex of arc.
//	 */
//	private void computeP11AndExtinctionForArc(int x) {
//		
//		// Probability of death below for a lineage at the current point.
//		double D;
//		int[] pars = dag.getParents(x);
//		switch (dag.getVertexType(x)) {
//		case STEM_TIP:
//			return;
//		case LEAF:
//			D = 0.0;
//			break;
//		case SPECIATION:
//		case HYBRID_DONOR:
//			D = getExtinctionProbability(pars[0]) * getExtinctionProbability(pars[1]);
//			break;
//		case EXTINCT_HYBRID_DONOR:
//			D = getExtinctionProbability(pars[0]);
//			break;
//		case ALLOPOLYPLOIDIC_HYBRID:
//			D = getExtinctionProbability(pars[0]);
//			break;
//		case AUTOPOLYPLOIDIC_HYBRID:
//			D = getExtinctionProbability(pars[0]) * getExtinctionProbability(pars[0]);
//			break;
//		default:
//			throw new UnsupportedOperationException("Invalid vertex type in computeP11AndExtinctionForArc().");
//		
//		
//			
//		
//		// Compute Pt and ut for separately for inner segment and end segments
//		// because of different time spans (dt and dt/2 respectively).
//		double dt = this.times.getSliceTime(x);
//		double[] Ptut = computePtAndUt(dt);
//		double[] PtutEnd = computePtAndUt(dt / 2.0);
//
//		///////// POINT-TO-NODE PROBS. ////////
//
//
//		// No. of points is the no. of pure discretisation points + the two endpoints.
//		int sz = this.times.getDiscretisationTimes(x).length;
//		
//		// Treat first segment separately since shorter time.
//		double[] arcp11 = new double[sz * sz];
//		arcp11[0 * sz + 0] = 1.0;
//		double p11 = (PtutEnd[0] * (1.0 - PtutEnd[1]) / ((1.0 - PtutEnd[1] * D) * (1.0 - PtutEnd[1] * D)));
//		D = 1.0 - PtutEnd[0] * (1.0 - D) / (1.0 - PtutEnd[1] * D);
//		arcp11[1 * sz + 0] = p11;
//
//		// Remaining inner points.
//		for (int i = 1 ; i < sz - 1; ++i) {
//			p11 = (p11 * Ptut[0] * (1.0 - Ptut[1]) / ((1.0 - Ptut[1] * D) * (1.0 - Ptut[1] * D)));
//			arcp11[i * sz + 0] = p11;
//			D = 1.0 - Ptut[0] * (1.0 - D) / (1.0 - Ptut[1] * D);
//		}
//
//		// Again, special treatment of last segment since shorter time.
//		p11 = (p11 * PtutEnd[0] * (1.0 - PtutEnd[1]) / ((1.0 - PtutEnd[1] * D) * (1.0 - PtutEnd[1] * D)));
//		D = 1.0 - PtutEnd[0] * (1.0 - D) / (1.0 - PtutEnd[1] * D);
//		arcp11[(sz - 1) * sz + 0] = p11;
//
//		// Extinction probability in the planted tree S^node.
//		this.extinction.set(x, D);
//
//		///////// POINT-TO-POINT PROBS. ////////
//
//		// Use Markovian property to compute probability for inner point pairs.
//		// i is upper point, j is lower point.
//		for (int j = 0; j < sz; ++j) {
//			for (int i = j; i < sz; ++i) {
//				if (j == i) {
//					arcp11[sz * i + j] = 1.0;
//				} else {
//					arcp11[sz * i + j] = arcp11[sz * i + 0] / arcp11[sz * j + 0];
//				}
//			}
//		}
//		
//		// Finally, store the array.
//		this.p11.set(x, x, arcp11);
//	}
//	
//	
//}
