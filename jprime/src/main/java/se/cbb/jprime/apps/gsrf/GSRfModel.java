package se.cbb.jprime.apps.gsrf;

import java.util.Arrays;
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
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RootedBifurcatingTreeParameter;

/**
 * Implements the GSRf model.
 * Effectively, this class computes the likelihood
 * Pr[G,l | S,t,lambda,mu,alpha,m,v], where G is the guest tree
 * topology, l the branch lengths of G, S the host tree, t the
 * divergence times of S, lambda the duplication (birth) rate,
 * mu the loss (death) rate, alpha a substitution rate parameter,
 * m the substitution rate mean, and v the substitution rate variance.
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
	 * Probability of rooted subtree G_u for each valid placement of u in S'.
	 */
	protected DoubleArrayMap ats;
	
	/**
	 * Probability of planted subtree G^u for each valid placement of tip of u's
	 * parent arc in S'.
	 */
	protected DoubleArrayMap belows;
	
	/**
	 * For each vertex u of G, the lowermost placement x_i in S where u can be placed.
	 * HACK: Right now we store the tuple x_i as a single int, with x in the rightmost
	 * bits, and i shifted 16 bits to the left.
	 */
	protected IntMap loLims;
	
//	/**
//	 * For each vertex u of G, the uppermost placement x_i in S where u can be placed.
//	 * HACK: Right now we store the tuple x_i as a single int, with x in the rightmost
//	 * bits, and i shifted 16 bits to the left.
//	 */
//	protected IntMap upLims;
	
	/**
	 * 
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
		//this.upLims = new IntMap("GSRf.uplims", g.getNoOfVertices());
		this.ats = new DoubleArrayMap("GSRf.ats", g.getNoOfVertices());
		this.belows = new DoubleArrayMap("GSRf.belows", g.getNoOfVertices());
				
		// TODO: Write properly.
		int r = this.g.getRoot();
		this.updateLoLims(r);
		//this.updateUpLims(r);
		this.clearAtsAndBelows(r, 0);
		this.updateAtProbs(r, true);
	}
	
	@Override
	public Dependent[] getParentDependents() {
		return new Dependent[] {this.g, this.s, this.gsMap, this.lengths, this.times, this.dupLossProbs, this.substPD };
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos, boolean willSample) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearCache(boolean willSample) {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreCache(boolean willSample) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogDouble getLikelihood() {
		// TODO Auto-generated method stub
		return null;
	}
	

//
//	EdgeDiscGSR::EdgeDiscGSR(
//			Tree* G,
//			EdgeDiscTree* DS,
//			StrStrMap* GSMap,
//			Density2P* edgeRateDF,
//			EdgeDiscBDProbs* BDProbs,
//			UnsignedVector* fixedGNodes) :
//			EdgeWeightModel(),
//			PerturbationObserver(),
//			m_G(G),
//			m_DS(DS),
//			m_edgeRateDF(edgeRateDF),
//			m_BDProbs(BDProbs),
//			m_sigma(*G, DS->getTree(), *GSMap),
//			m_lengths(NULL),
//			m_fixedGNodes(fixedGNodes),
//			m_loLims(*G),
//			m_upLims(*G),
//			m_ats(*G, ProbabilityEdgeDiscPtMap(DS, 0.0)),
//			m_belows(*G, ProbabilityEdgeDiscPtMap(DS, 0.0))
////			m_rateDelta(0.0)
//	{
//
//		// Set G lengths, creating new if they don't exist.
//		if (m_G->hasLengths())
//		{
//			m_lengths = &(m_G->getLengths());
//		}
//		else
//		{
//			// G itself responsible for destruction of lengths.
//			m_lengths = new RealVector(m_G->getNumberOfNodes(), m_edgeRateDF->getMean());
//			m_G->setLengths(*m_lengths, true);
//		}
//
//		// Set rate delta to a quarter of smallest timestep (for instance).
//		//m_rateDelta = DS.getMinTimestep() / 4;
//
//		// Compute values to have something to start with.
//		updateHelpStructures();
//		updateProbsFull();
//
//		// Register as listener on parameter holders we depend on.
//		m_DS->getTree().addPertObserver(this);
//		m_G->addPertObserver(this);
//		m_BDProbs->addPertObserver(this);
//		m_edgeRateDF->addPertObserver(this);
//	}
//
//
//	EdgeDiscGSR::~EdgeDiscGSR()
//	{
//	}
//
//
//	Tree&
//	EdgeDiscGSR::getTree() const
//	{
//		return (*m_G);
//	}
//
//
//	unsigned
//	EdgeDiscGSR::nWeights() const
//	{
//		// Top time edge in G is never perturbed, but both of root's children.
//		return (m_G->getNumberOfNodes() - 1);
//	}
//
//
//	RealVector&
//	EdgeDiscGSR::getWeightVector() const
//	{
//		return *m_lengths;
//	}
//
//
//	Real
//	EdgeDiscGSR::getWeight(const Node& node) const
//	{
//		return (*m_lengths)[node];
//	}
//
//
//	void
//	EdgeDiscGSR::setWeight(const Real& weight, const Node& u)
//	{
//		(*m_lengths)[u] = weight;
//	}
//
//
//	void
//	EdgeDiscGSR::getRange(Real& low, Real& high)
//	{
//		m_edgeRateDF->getRange(low, high);
//	}
//
//
//	string
//	EdgeDiscGSR::print() const
//	{
//		ostringstream oss;
//		oss << "The rate probabilities are modeled using a \n" << m_edgeRateDF->print();
//		return oss.str();
//	}
//
//
//	void
//	EdgeDiscGSR::perturbationUpdate(const PerturbationObservable* sender, const PerturbationEvent* event)
//	{
//		// We have these scenarios:
//		//
//		// 1) Perturbation event, where sender==
//		//  a)    DS, lacking info on time change     =>  Recreate discretization, full update.
//		//  b)    DS, detailed info on time change    =>  See a). TODO: Optimize similar to e)!
//		//  c)    G, lacking info                     =>  Full update.
//		//  d)    G, detailed info on topology change =>  Full update (partial update is tricky).
//		//  e)    G, detailed info on length change   =>  Partial update.
//		//  f)    BDProbs (birth/death rate change)   =>  Full update.
//		//  g)    rateDF (edge rate change)           =>  Full update.
//		// 2) Restoration event, where sender==
//		//  a)    DS                                  =>  Restore dependent and internal caches.
//		//  b)    Any other                           =>  Restore internal cache.
//		//
//		// We always do a full update on help structures. HOWEVER: In case of a restoration,
//		// we do it afterwards to make sure we restore exactly cached parts. This latter update
//		// is not really necessary, but might cause confusion when debugging otherwise.
//
//		//========== RESTORATION ==========
//		if (event != NULL && event->getType() == PerturbationEvent::RESTORATION)
//		{
//			if (sender == m_DS)
//			{
//				// Case 2a).
//				m_BDProbs->restoreCache();  // Dependent.
//				restoreCachedProbs();       // Internal.
//				updateHelpStructures();
//			}
//			else
//			{
//				// Case 2b).
//				restoreCachedProbs();
//				updateHelpStructures();
//			}
//			return;
//		}
//
//		//========== PERTURBATION ==========
//
//		static long iter = 0;
//
//		// If 1b), 1d) or 1e), details will not be null.
//		// Occasionally, we replace a partial update with a full one
//	    // to avoid accumulated numeric error drift.
//		const TreePerturbationEvent* details = dynamic_cast<const TreePerturbationEvent*>(event);
//		bool doFull = (details == NULL || iter % 20 == 0);
//
//		updateHelpStructures();
//		if (sender == m_DS)
//		{
//			// Case 1a), 1b) and 1c). Case 1b) could be broken out to be
//			// optimized similar to 1e), i.e. only rediscretize and update
//			// the affected part of host and guest tree.
//			cacheProbs(NULL);      // Internal.
//			m_BDProbs->cache();    // Dependent.
//			BeepVector<ProbabilityEdgeDiscPtMap>::iterator it;
//			for (it = m_ats.begin(); it != m_ats.end(); ++it)
//				(*it).rediscretize(0.0);
//			for (it = m_belows.begin(); it != m_belows.end(); ++it)
//				(*it).rediscretize(0.0);
//			m_BDProbs->update(true);
//			updateProbsFull();
//		}
//		else if (sender == m_G)
//		{
//			if (doFull || details->getTreePerturbationType() != TreePerturbationEvent::EDGE_WEIGHT)
//			{
//				// Case 1c) and 1d).
//				cacheProbs(NULL);
//				updateProbsFull();
//			}
//			else
//			{
//				// Case 1b).
//				// Since we only allow partial updates at edge weight events (and the two root
//				// child edges are considered separate entities) we only consider the first root path.
//				// p2 may contain a node, but its weight should (and must) be untouched.
//				const Node* p1;
//				const Node* p2;
//				details->getRootPaths(p1, p2);
//				cacheProbs(p1);
//				updateProbsPartial(p1);
//			}
//		}
//		else
//		{
//			// Case 1f) and 1g).
//			cacheProbs(NULL);
//			updateProbsFull();
//		}
//		++iter;
//	}
//
//
//	void
//	EdgeDiscGSR::update()
//	{
//	}
//
//
//	void
//	EdgeDiscGSR::updateHelpStructures()
//	{
//		// Note: Order of invocation matters.
//		m_sigma.update(*m_G, m_DS->getTree());
//		const Node* uRoot = m_G->getRootNode();
//		updateLoLims(uRoot);
//		updateUpLims(uRoot);
//	}
//
//
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

//	/**
//	 * Recursively computes the uppermost viable placement of each guest tree vertex.
//	 * @param u the subtree of G rooted at u.
//	 */
//	protected void updateUpLims(int u) {
//		
//		// HACK: At the moment we store a point x_i in a single int v by having x in the
//		// rightmost bits, and i shifted 16 bits left.
//		// Insert thus:   v = x + (i << 16);
//		// Extract thus:  x = (v << 16) >>> 16;   i = v >>> 16;
//		
//		int sigma = this.gsMap.getSigma(u);
//
//		if (this.g.isLeaf(u)) {
//			this.upLims.set(u, sigma + (0 << 16));
//		} else if (this.g.isRoot(u)) {         
//			// We disallow placement on very tip of host tree top edge.
//			int r = this.s.getRoot();
//			int p = this.times.getNoOfSlices(r);
//			this.upLims.set(u, sigma + (p << 16));
//		} else {
//			// Normal case: set u's limit just beneath parent's limit.
//			int pVx = (this.upLims.get(this.g.getParent(u)) << 16) >>> 16;
//			int pPt = (this.upLims.get(this.g.getParent(u)) >>> 16);
//			if (pPt >= 2) {
//				// There is a disc point available below the parent.
//				this.upLims.set(u, pVx + ((pPt - 1) << 16));
//			} else if (pPt == 1 && pVx == sigma) {
//				// We place u at speciation since upper and lower limits here coincide with sigma.
//				this.upLims.set(u, pVx + (0 << 16));
//			} else {
//				// We can't place u below its sigma.
//				if (sigma == pVx) {
//					throw new RuntimeException("Insufficient no. of discretization points.\n" +
//	        				       "Try using denser discretization for 1) top edge, 2) remaining vertices.");
//				}
//
//				// No disc point available on this edge; find the edge below.
//				int n = sigma;
//				while (this.s.getParent(n) != pVx) {
//					n = this.s.getParent(n);
//				}
//				this.upLims.set(u, n + (this.times.getNoOfSlices(n) << 16));
//			}
//		}
//
//		// Catch insufficient discretizations.
//		int lVx = (this.loLims.get(u) << 16) >>> 16;
//		int lPt = this.loLims.get(u) >>> 16;
//		int uVx = (this.upLims.get(u) << 16) >>> 16;
//		int uPt = this.upLims.get(u) >>> 16;
//		if ((lVx == uVx && lPt > uPt) || (lVx == this.s.getParent(uVx))) {
//			throw new RuntimeException("Insufficient no. of discretization points.\n" +
//	        		       "Try using denser dicretization for 1) top edge, 2) remaining vertices.");
//		}
//
//		// Update children afterwards.
//		if (!this.g.isLeaf(u)) {
//			updateUpLims(this.g.getLeftChild(u));
//			updateUpLims(this.g.getRightChild(u));
//		}
//	}

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
	
	
	
//
//
//	Probability
//	EdgeDiscGSR::calculateDataProbability()
//	{
//		return m_belows[m_G->getRootNode()].getTopmost();
//		// POTENTIAL VARIANT: We divide density with p11 for entire host
//		// tree top edge so as to reduce its effect on BD rate scaling!
//		//return (m_belows[m_G->getRootNode()].getTopmost() / m_BDProbs->getOneToOneProb(m_DS->getRootNode()));
//	}
//
//
//	void
//	EdgeDiscGSR::updateProbsFull()
//	{
//		// Full recursive update.
//		updateAtProbs(m_G->getRootNode(), true);
//	}
//
//
//	void
//	EdgeDiscGSR::updateProbsPartial(const Node* rootPath)
//	{
//		// We currently only support partial updates for edge weight
//		// changes. Therefore, we only consider partial caching along a
//		// single root path.
//
//		// Do a non-recursive update along the changed root path.
//		while (rootPath != NULL)
//		{
//			updateAtProbs(rootPath, false);
//			rootPath = rootPath->getParent();
//		}
//	}
//
//
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

	
//	void
//	EdgeDiscGSR::cacheProbs(const Node* rootPath)
//	{
//		clearAllCachedProbs();
//
//		// This mirrors full and partial updates.
//		// See partialUpdate() for more info.
//		if (rootPath == NULL)
//		{
//			// Recursively store all values.
//			cacheNodeProbs(m_G->getRootNode(), true);
//		}
//		else
//		{
//			// Store only values along path to root.
//			while (rootPath != NULL)
//			{
//				cacheNodeProbs(rootPath, false);
//				rootPath = rootPath->getParent();
//			}
//		}
//	}
//
//
//	void
//	EdgeDiscGSR::cacheNodeProbs(const Node* u, bool doRecurse)
//	{
//		m_belows[u].cachePath(m_sigma[u]);
//		if (!u->isLeaf())
//		{
//			// Note: Leaf's "ats" are never changed.
//			m_ats[u].cachePath(m_sigma[u]);
//			if (doRecurse)
//			{
//				cacheNodeProbs(u->getLeftChild(), true);
//				cacheNodeProbs(u->getRightChild(), true);
//			}
//		}
//	}
//
//
//	void
//	EdgeDiscGSR::restoreCachedProbs()
//	{
//		for (Tree::const_iterator it = m_G->begin(); it != m_G->end(); ++it)
//		{
//			m_ats[*it].restoreCachePath(m_sigma[*it]);
//			m_belows[*it].restoreCachePath(m_sigma[*it]);
//		}
//	}
//
//
//	void
//	EdgeDiscGSR::clearAllCachedProbs()
//	{
//		for (Tree::const_iterator it = m_G->begin(); it != m_G->end(); ++it)
//		{
//			m_ats[*it].invalidateCache();
//			m_belows[*it].invalidateCache();
//		}
//	}
//
//
//	string
//	EdgeDiscGSR::getDebugInfo(bool inclAts, bool inclBelows)
//	{
//		ostringstream oss;
//		Tree::const_iterator it;
//
//		oss << "# GENERAL INFO:" << endl;
//		oss << "# Node no:\tSigma:\tLoLim:\tUpLim:\t" << endl;
//		for (it = m_G->begin(); it != m_G->end(); ++it)
//		{
//			const Node* u = (*it);
//			oss
//			<< "# " << u->getNumber() << '\t' << '\t'
//			<< m_sigma[u]->getNumber() << '\t'
//			<< '(' << m_loLims[u].first->getNumber() << ',' << m_loLims[u].second << ")\t"
//			<< '(' << m_upLims[u].first->getNumber() << ',' << m_upLims[u].second << ")\t"
//			<< endl;
//		}
//		if (inclAts)
//		{
//			oss << "# AT-PROBABILITIES:" << endl;
//			for (it = m_G->begin(); it != m_G->end(); ++it)
//			{
//				oss << "# Node " << (*it)->getNumber() << ':' << endl
//						<< m_ats[*it].printPath(m_sigma[*it]);
//			}
//		}
//		if (inclBelows)
//		{
//			oss << "# BELOW-PROBABILITIES:" << endl;
//			for (it = m_G->begin(); it != m_G->end(); ++it)
//			{
//				oss << "# Node " << (*it)->getNumber() << ':' << endl
//						<< m_belows[*it].printPath(m_sigma[*it]);
//			}
//		}
//		return oss.str();
//	}
//
//
//	string
//	EdgeDiscGSR::getRootProbDebugInfo()
//	{
//		ostringstream oss;
//		const Node* u = m_G->getRootNode();
//		EdgeDiscTreeIterator top = m_DS->begin(m_DS->getTopmostPt());
//		EdgeDiscTreeIterator x = m_DS->begin(m_loLims[u]);
//		while (x != top)
//		{
//			oss << (m_BDProbs->getOneToOneProb(top, x) * m_ats[u](x)) << " ";
//			x = x.pp();
//		}
//		return oss.str();
//	}
//


}
