//package se.cbb.jprime.apps.gsrf;
//
//import java.util.Map;
//
//import se.cbb.jprime.io.SampleLogDouble;
//import se.cbb.jprime.math.Continuous1DPDDependent;
//import se.cbb.jprime.math.LogDouble;
//import se.cbb.jprime.mcmc.ChangeInfo;
//import se.cbb.jprime.mcmc.Dependent;
//import se.cbb.jprime.mcmc.Model;
//import se.cbb.jprime.misc.IntPair;
//import se.cbb.jprime.topology.DoubleArrayMap;
//import se.cbb.jprime.topology.DoubleMap;
//import se.cbb.jprime.topology.IntMap;
//import se.cbb.jprime.topology.MPRMap;
//import se.cbb.jprime.topology.RBTreeArcDiscretiser;
//import se.cbb.jprime.topology.RootedBifurcatingTreeParameter;
//
///**
// * Implements the GSRf model.
// * Effectively, this class computes the likelihood
// * Pr[G,l | S,t,lambda,mu,alpha,m,v], where G is the guest tree
// * topology, l the branch lengths of G, S the host tree, t the
// * divergence times of S, lambda the duplication (birth) rate,
// * mu the loss (death) rate, alpha a substitution rate parameter,
// * m the substitution rate mean, and v the substitution rate variance.
// * 
// * @author Joel Sjöstrand.
// */
//public class GSRfModel implements Model {
//
//	/** The guest tree G. */
//	protected RootedBifurcatingTreeParameter g;
//	
//	/** The host tree S. */
//	protected RootedBifurcatingTreeParameter s;
//	
//	/** G-S reconciliation info. */
//	protected MPRMap gsMap;
//	
//	/** The branch lengths l. */
//	protected DoubleMap lengths;
//	
//	/** The divergence times t for the discretised tree S'. */
//	protected RBTreeArcDiscretiser times;
//	
//	/** P11 and similar info. */
//	protected DupLossProbs dupLossProbs;
//	
//	/** Substitution rate distribution. */
//	protected Continuous1DPDDependent substPD;
//	
//	/**
//	 * Probability of rooted subtree G_u for each valid placement of u in S'.
//	 */
//	protected DoubleArrayMap ats;
//	
//	/**
//	 * Probability of planted subtree G^u for each valid placement of tip of u's
//	 * parent arc in S'.
//	 */
//	protected DoubleArrayMap belows;
//	
//	/**  */
//	protected IntMap loLims;
//	
//	/**  */
//	protected IntMap upLims;
//	
//	public GSRfModel(RootedBifurcatingTreeParameter g, RootedBifurcatingTreeParameter s, MPRMap gsMap,
//			DoubleMap lengths, RBTreeArcDiscretiser times, DupLossProbs dupLossProbs, Continuous1DPDDependent substPD) {
//		this.g = g;
//		this.s = s;
//		this.gsMap = gsMap;
//		this.lengths = lengths;
//		this.times = times;
//		this.dupLossProbs = dupLossProbs;
//		this.substPD = substPD;
//	}
//	
//	@Override
//	public Dependent[] getParentDependents() {
//		return new Dependent[] {this.g, this.s, this.gsMap, this.lengths, this.times, this.dupLossProbs, this.substPD };
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
//	@Override
//	public Class<?> getSampleType() {
//		return SampleLogDouble.class;
//	}
//
//	@Override
//	public String getSampleHeader() {
//		return "GSRf-likelihood";
//	}
//
//	@Override
//	public String getSampleValue() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public LogDouble getLikelihood() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//
////
////	EdgeDiscGSR::EdgeDiscGSR(
////			Tree* G,
////			EdgeDiscTree* DS,
////			StrStrMap* GSMap,
////			Density2P* edgeRateDF,
////			EdgeDiscBDProbs* BDProbs,
////			UnsignedVector* fixedGNodes) :
////			EdgeWeightModel(),
////			PerturbationObserver(),
////			m_G(G),
////			m_DS(DS),
////			m_edgeRateDF(edgeRateDF),
////			m_BDProbs(BDProbs),
////			m_sigma(*G, DS->getTree(), *GSMap),
////			m_lengths(NULL),
////			m_fixedGNodes(fixedGNodes),
////			m_loLims(*G),
////			m_upLims(*G),
////			m_ats(*G, ProbabilityEdgeDiscPtMap(DS, 0.0)),
////			m_belows(*G, ProbabilityEdgeDiscPtMap(DS, 0.0))
//////			m_rateDelta(0.0)
////	{
////
////		// Set G lengths, creating new if they don't exist.
////		if (m_G->hasLengths())
////		{
////			m_lengths = &(m_G->getLengths());
////		}
////		else
////		{
////			// G itself responsible for destruction of lengths.
////			m_lengths = new RealVector(m_G->getNumberOfNodes(), m_edgeRateDF->getMean());
////			m_G->setLengths(*m_lengths, true);
////		}
////
////		// Set rate delta to a quarter of smallest timestep (for instance).
////		//m_rateDelta = DS.getMinTimestep() / 4;
////
////		// Compute values to have something to start with.
////		updateHelpStructures();
////		updateProbsFull();
////
////		// Register as listener on parameter holders we depend on.
////		m_DS->getTree().addPertObserver(this);
////		m_G->addPertObserver(this);
////		m_BDProbs->addPertObserver(this);
////		m_edgeRateDF->addPertObserver(this);
////	}
////
////
////	EdgeDiscGSR::~EdgeDiscGSR()
////	{
////	}
////
////
////	Tree&
////	EdgeDiscGSR::getTree() const
////	{
////		return (*m_G);
////	}
////
////
////	unsigned
////	EdgeDiscGSR::nWeights() const
////	{
////		// Top time edge in G is never perturbed, but both of root's children.
////		return (m_G->getNumberOfNodes() - 1);
////	}
////
////
////	RealVector&
////	EdgeDiscGSR::getWeightVector() const
////	{
////		return *m_lengths;
////	}
////
////
////	Real
////	EdgeDiscGSR::getWeight(const Node& node) const
////	{
////		return (*m_lengths)[node];
////	}
////
////
////	void
////	EdgeDiscGSR::setWeight(const Real& weight, const Node& u)
////	{
////		(*m_lengths)[u] = weight;
////	}
////
////
////	void
////	EdgeDiscGSR::getRange(Real& low, Real& high)
////	{
////		m_edgeRateDF->getRange(low, high);
////	}
////
////
////	string
////	EdgeDiscGSR::print() const
////	{
////		ostringstream oss;
////		oss << "The rate probabilities are modeled using a \n" << m_edgeRateDF->print();
////		return oss.str();
////	}
////
////
////	void
////	EdgeDiscGSR::perturbationUpdate(const PerturbationObservable* sender, const PerturbationEvent* event)
////	{
////		// We have these scenarios:
////		//
////		// 1) Perturbation event, where sender==
////		//  a)    DS, lacking info on time change     =>  Recreate discretization, full update.
////		//  b)    DS, detailed info on time change    =>  See a). TODO: Optimize similar to e)!
////		//  c)    G, lacking info                     =>  Full update.
////		//  d)    G, detailed info on topology change =>  Full update (partial update is tricky).
////		//  e)    G, detailed info on length change   =>  Partial update.
////		//  f)    BDProbs (birth/death rate change)   =>  Full update.
////		//  g)    rateDF (edge rate change)           =>  Full update.
////		// 2) Restoration event, where sender==
////		//  a)    DS                                  =>  Restore dependent and internal caches.
////		//  b)    Any other                           =>  Restore internal cache.
////		//
////		// We always do a full update on help structures. HOWEVER: In case of a restoration,
////		// we do it afterwards to make sure we restore exactly cached parts. This latter update
////		// is not really necessary, but might cause confusion when debugging otherwise.
////
////		//========== RESTORATION ==========
////		if (event != NULL && event->getType() == PerturbationEvent::RESTORATION)
////		{
////			if (sender == m_DS)
////			{
////				// Case 2a).
////				m_BDProbs->restoreCache();  // Dependent.
////				restoreCachedProbs();       // Internal.
////				updateHelpStructures();
////			}
////			else
////			{
////				// Case 2b).
////				restoreCachedProbs();
////				updateHelpStructures();
////			}
////			return;
////		}
////
////		//========== PERTURBATION ==========
////
////		static long iter = 0;
////
////		// If 1b), 1d) or 1e), details will not be null.
////		// Occasionally, we replace a partial update with a full one
////	    // to avoid accumulated numeric error drift.
////		const TreePerturbationEvent* details = dynamic_cast<const TreePerturbationEvent*>(event);
////		bool doFull = (details == NULL || iter % 20 == 0);
////
////		updateHelpStructures();
////		if (sender == m_DS)
////		{
////			// Case 1a), 1b) and 1c). Case 1b) could be broken out to be
////			// optimized similar to 1e), i.e. only rediscretize and update
////			// the affected part of host and guest tree.
////			cacheProbs(NULL);      // Internal.
////			m_BDProbs->cache();    // Dependent.
////			BeepVector<ProbabilityEdgeDiscPtMap>::iterator it;
////			for (it = m_ats.begin(); it != m_ats.end(); ++it)
////				(*it).rediscretize(0.0);
////			for (it = m_belows.begin(); it != m_belows.end(); ++it)
////				(*it).rediscretize(0.0);
////			m_BDProbs->update(true);
////			updateProbsFull();
////		}
////		else if (sender == m_G)
////		{
////			if (doFull || details->getTreePerturbationType() != TreePerturbationEvent::EDGE_WEIGHT)
////			{
////				// Case 1c) and 1d).
////				cacheProbs(NULL);
////				updateProbsFull();
////			}
////			else
////			{
////				// Case 1b).
////				// Since we only allow partial updates at edge weight events (and the two root
////				// child edges are considered separate entities) we only consider the first root path.
////				// p2 may contain a node, but its weight should (and must) be untouched.
////				const Node* p1;
////				const Node* p2;
////				details->getRootPaths(p1, p2);
////				cacheProbs(p1);
////				updateProbsPartial(p1);
////			}
////		}
////		else
////		{
////			// Case 1f) and 1g).
////			cacheProbs(NULL);
////			updateProbsFull();
////		}
////		++iter;
////	}
////
////
////	void
////	EdgeDiscGSR::update()
////	{
////	}
////
////
////	void
////	EdgeDiscGSR::updateHelpStructures()
////	{
////		// Note: Order of invocation matters.
////		m_sigma.update(*m_G, m_DS->getTree());
////		const Node* uRoot = m_G->getRootNode();
////		updateLoLims(uRoot);
////		updateUpLims(uRoot);
////	}
////
////
//	
//	protected void updateLoLims(int u) {
//		int sigma = this.gsMap.getSigma(u);
//
//		if (this.g.isLeaf(u)) {
//			this.loLims.set(u, sigma + (0 << 16));
//		} else {
//			int lc = this.g.getLeftChild(u);
//			int rc = this.g.getRightChild(u);
//
//			// Update children first.
//			updateLoLims(lc);
//			updateLoLims(rc);
//
//			int lcLo = this.loLims.get(lc);
//			int rcLo = this.loLims.get(rc);
//
//			// Set the lowest point at the left child to begin with.
//			IntPair lo = new IntPair((lcLo << 16) >>> 16, (lcLo >>> 16) + 1);
//
//			// Start at the left child.
//			int curr = lo.first;
//
//			// Start at the lowest placement of the left child and move
//			// on the path from u towards the root.
//			while (curr != RootedBifurcatingTreeParameter.NULL) {
//				// If we are at sigma(u) and we haven't marked it as
//				// the lowest point of u, do so.
//				if (curr == sigma && lo.first != sigma) {
//					lo = new IntPair(sigma, 0);
//				}
//
//				// If we are at the same lowest edge as the right child.
//				if (curr == ((rcLo << 16) >>> 16)) {
//					if (lo.first == curr) {
//						// u also has this edge as its lowest point.
//						lo = new IntPair(lo.first, Math.max(lo.second, (rcLo >>> 16) + 1));
//					}
//					else {
//						// The right child is higher up in the tree
//						// than the left child.
//						lo = new IntPair((rcLo << 16) >>> 16, (rcLo >>> 16) + 1);
//					}
//				}
//
//				curr = this.g.getParent(curr);
//			}
//
//			// If we have moved outside edge's points, choose next pure disc. pt.
//			if (lo.second > this.times.getNoOfSlices(lo.first)) {
//				lo = new IntPair(this.g.getParent(lo.first), 1);
//				if (lo.first == RootedBifurcatingTreeParameter.NULL) {
//					throw new RuntimeException("Insufficient no. of discretization points.\n" +
//	        				      "Try using denser discretization for 1) top edge, 2) remaining vertices.");
//				}
//			}
//			this.loLims.set(u, lo.first + (lo.second << 16));
//		}
//	}
//
//	
//	protected void updateUpLims(int u) {
//		int sigma = this.gsMap.getSigma(u);
//
//		if (this.g.isLeaf(u)) {
//			this.upLims.set(u, sigma + (0 << 16));
//		} else if (this.g.isRoot(u)) {         
//			// We disallow placement on very tip.
//			m_upLims[u] = m_DS->getTopmostPt();
//			--(m_upLims[u].second);
//		} else {
//			// Normal case: set u's limit just beneath parent's limit.
//			Point pLim = m_upLims[u->getParent()];
//			if (pLim.second >= 2) {
//				// There is a disc point available below the parent.
//				m_upLims[u] = Point(pLim.first, pLim.second - 1);
//			} else if (pLim.second == 1 && pLim.first == sigma) {
//				// We place u at speciation since upper and lower limits here coincide with sigma.
//				m_upLims[u] = Point(pLim.first, 0);
//			} else {
//				// We can't place u below its sigma.
//				if (sigma == pLim.first) {
//					throw new RuntimeException("Insufficient no. of discretization points.\n" +
//	        				       "Try using denser discretization for 1) top edge, 2) remaining vertices.");
//				}
//
//				// No disc point available on this edge; find the edge below.
//				const Node* n = sigma;
//				while (n->getParent() != pLim.first) {
//					n = n->getParent();
//				}
//				m_upLims[u] = Point(n, m_DS->getNoOfPts(n) - 1);
//			}
//		}
//
//		// Catch insufficient discretizations.
//		if ((m_loLims[u].first == m_upLims[u].first && m_loLims[u].second > m_upLims[u].second)
//				|| (m_loLims[u].first == m_upLims[u].first->getParent())) {
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
////
////
////	Probability
////	EdgeDiscGSR::calculateDataProbability()
////	{
////		return m_belows[m_G->getRootNode()].getTopmost();
////		// POTENTIAL VARIANT: We divide density with p11 for entire host
////		// tree top edge so as to reduce its effect on BD rate scaling!
////		//return (m_belows[m_G->getRootNode()].getTopmost() / m_BDProbs->getOneToOneProb(m_DS->getRootNode()));
////	}
////
////
////	void
////	EdgeDiscGSR::updateProbsFull()
////	{
////		// Full recursive update.
////		updateAtProbs(m_G->getRootNode(), true);
////	}
////
////
////	void
////	EdgeDiscGSR::updateProbsPartial(const Node* rootPath)
////	{
////		// We currently only support partial updates for edge weight
////		// changes. Therefore, we only consider partial caching along a
////		// single root path.
////
////		// Do a non-recursive update along the changed root path.
////		while (rootPath != NULL)
////		{
////			updateAtProbs(rootPath, false);
////			rootPath = rootPath->getParent();
////		}
////	}
////
////
////	void
////	EdgeDiscGSR::updateAtProbs(const Node* u, bool doRecurse)
////	{
////		if (u->isLeaf())
////		{
////			m_ats[u](m_loLims[u]) = Probability(1.0);
////		}
////		else
////		{
////			const Node* lc = u->getLeftChild();
////			const Node* rc = u->getRightChild();
////
////			// Must do children first, if specified.
////			if (doRecurse)
////			{
////				updateAtProbs(lc, true);
////				updateAtProbs(rc, true);
////			}
////
////			// Retrieve placement bounds. End is here also a valid placement.
////			EdgeDiscTreeIterator x = m_DS->begin(m_loLims[u]);
////			EdgeDiscTreeIterator xend = m_DS->begin(m_upLims[u]);
////
////			// For each valid placement x.
////			while (true)
////			{
////				EdgeDiscretizer::Point xPt = x.getPt();
////				m_ats[u](x) = m_belows[lc](x) * m_belows[rc](x) * duplicationFactor(xPt);
////				if (x == xend) { break; }
////				x.pp();
////			}
////		}
////
////		// Update planted tree probs. afterwards.
////		updateBelowProbs(u);
////	}
////
////
////	void
////	EdgeDiscGSR::updateBelowProbs(const Node* u)
////	{
////		// x refers to point of tip of planted tree G^u.
////		// y refers to point where u is placed (strictly below x).
////
////		Real l = (*m_lengths)[u];
////
////		// Get limits for x (both are valid placements).
////		EdgeDiscTreeIterator x, xend;
////		if (u->isRoot())
////		{
////			x = xend = m_DS->end();
////		}
////		else
////		{
////			x = m_DS->begin(m_loLims[u->getParent()]);
////			xend = m_DS->begin(m_upLims[u->getParent()]);
////		} 
////
////		// Get limits for y (both valid placements).
////		EdgeDiscTreeIterator y;
////		EdgeDiscTreeIterator yend = m_DS->begin(m_upLims[u]);
////
////		// For each x.
////		while (true)
////		{
////			// For each y strictly below x.
////			m_belows[u](x) = Probability(0.0);
////			for (y = m_DS->begin(m_loLims[u]); y < x; y.pp())
////			{
////				//Probability rateDens = 1.0;
////				Probability rateDens = u->isRoot() ?
////						1.0 : calcRateDensity(l, (*m_DS)(x) - (*m_DS)(y));
////				m_belows[u](x) += rateDens * m_BDProbs->getOneToOneProb(x, y) * m_ats[u](y);
////
////				if (y == yend) { break; }
////			}
////			if (x == xend) { break; }
////			x.pp();
////		}
////	}
////
////
////	void
////	EdgeDiscGSR::cacheProbs(const Node* rootPath)
////	{
////		clearAllCachedProbs();
////
////		// This mirrors full and partial updates.
////		// See partialUpdate() for more info.
////		if (rootPath == NULL)
////		{
////			// Recursively store all values.
////			cacheNodeProbs(m_G->getRootNode(), true);
////		}
////		else
////		{
////			// Store only values along path to root.
////			while (rootPath != NULL)
////			{
////				cacheNodeProbs(rootPath, false);
////				rootPath = rootPath->getParent();
////			}
////		}
////	}
////
////
////	void
////	EdgeDiscGSR::cacheNodeProbs(const Node* u, bool doRecurse)
////	{
////		m_belows[u].cachePath(m_sigma[u]);
////		if (!u->isLeaf())
////		{
////			// Note: Leaf's "ats" are never changed.
////			m_ats[u].cachePath(m_sigma[u]);
////			if (doRecurse)
////			{
////				cacheNodeProbs(u->getLeftChild(), true);
////				cacheNodeProbs(u->getRightChild(), true);
////			}
////		}
////	}
////
////
////	void
////	EdgeDiscGSR::restoreCachedProbs()
////	{
////		for (Tree::const_iterator it = m_G->begin(); it != m_G->end(); ++it)
////		{
////			m_ats[*it].restoreCachePath(m_sigma[*it]);
////			m_belows[*it].restoreCachePath(m_sigma[*it]);
////		}
////	}
////
////
////	void
////	EdgeDiscGSR::clearAllCachedProbs()
////	{
////		for (Tree::const_iterator it = m_G->begin(); it != m_G->end(); ++it)
////		{
////			m_ats[*it].invalidateCache();
////			m_belows[*it].invalidateCache();
////		}
////	}
////
////
////	string
////	EdgeDiscGSR::getDebugInfo(bool inclAts, bool inclBelows)
////	{
////		ostringstream oss;
////		Tree::const_iterator it;
////
////		oss << "# GENERAL INFO:" << endl;
////		oss << "# Node no:\tSigma:\tLoLim:\tUpLim:\t" << endl;
////		for (it = m_G->begin(); it != m_G->end(); ++it)
////		{
////			const Node* u = (*it);
////			oss
////			<< "# " << u->getNumber() << '\t' << '\t'
////			<< m_sigma[u]->getNumber() << '\t'
////			<< '(' << m_loLims[u].first->getNumber() << ',' << m_loLims[u].second << ")\t"
////			<< '(' << m_upLims[u].first->getNumber() << ',' << m_upLims[u].second << ")\t"
////			<< endl;
////		}
////		if (inclAts)
////		{
////			oss << "# AT-PROBABILITIES:" << endl;
////			for (it = m_G->begin(); it != m_G->end(); ++it)
////			{
////				oss << "# Node " << (*it)->getNumber() << ':' << endl
////						<< m_ats[*it].printPath(m_sigma[*it]);
////			}
////		}
////		if (inclBelows)
////		{
////			oss << "# BELOW-PROBABILITIES:" << endl;
////			for (it = m_G->begin(); it != m_G->end(); ++it)
////			{
////				oss << "# Node " << (*it)->getNumber() << ':' << endl
////						<< m_belows[*it].printPath(m_sigma[*it]);
////			}
////		}
////		return oss.str();
////	}
////
////
////	string
////	EdgeDiscGSR::getRootProbDebugInfo()
////	{
////		ostringstream oss;
////		const Node* u = m_G->getRootNode();
////		EdgeDiscTreeIterator top = m_DS->begin(m_DS->getTopmostPt());
////		EdgeDiscTreeIterator x = m_DS->begin(m_loLims[u]);
////		while (x != top)
////		{
////			oss << (m_BDProbs->getOneToOneProb(top, x) * m_ats[u](x)) << " ";
////			x = x.pp();
////		}
////		return oss.str();
////	}
////
//
//
//}
