package se.cbb.jprime.topology;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ConstantTuningParameter;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.ProposerStatistics;
import se.cbb.jprime.mcmc.ProposerWeight;
import se.cbb.jprime.mcmc.StateParameter;
import se.cbb.jprime.mcmc.TuningParameter;

/**
 * Proposer which perturbs the topology of a bifurcating rooted tree.
 * In order to increase the probability of accepting the new topology,
 * corresponding lengths and/or times may also be changed according
 * to simple heuristics.
 * <p/>
 * Currently, the tree is perturbed using NNI, SPR (also superset of NNI), and
 * rerooting. By default, one of these is selected with equal probability
 * according to individual tuning parameters, but
 * these may be substituted using <code>setTuningParameters(...)</code>.
 * 
 * @author Lars Arvestad.
 * @author Örjan Åkerborg.
 * @author Joel Sjöstrand.
 */
public class RBTreeBranchSwapper implements Proposer {

	/** Topology. */
	private RBTree T;
	
	/** Lengths. Null if not used. */
	private DoubleMap lengths;
	
	/** Times. Null if not used. */
	private TimesMap times;
	
	/** Weight. */
	private ProposerWeight weight;
	
	/** Statistics. */
	private ProposerStatistics statistics;
	
	/** Pseudo-random number generator. */
	private PRNG prng;
	
	/** Array of tuning weights for SPR, NNI and rerooting respectively. */
	private TuningParameter[] tuningParams;
	
	/** Active flag. */
	private boolean isActive;
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param weight weight of this proposer.
	 * @param stats statistics of this proposer.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, ProposerWeight weight,
			ProposerStatistics stats, PRNG prng) {
		this(T, null, null, weight, stats, prng);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param times times of T. May be null.
	 * @param weight weight of this proposer.
	 * @param stats statistics of this proposer.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, TimesMap times, ProposerWeight weight,
			ProposerStatistics stats, PRNG prng) {
		this(T, null, times, weight, stats, prng);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param weight weight of this proposer.
	 * @param stats statistics of this proposer.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, DoubleMap lengths, ProposerWeight weight,
			ProposerStatistics stats, PRNG prng) {
		this(T, lengths, null, weight, stats, prng);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param times times of T. May be null.
	 * @param weight weight of this proposer.
	 * @param stats statistics of this proposer.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, DoubleMap lengths, TimesMap times,
			ProposerWeight weight, ProposerStatistics stats, PRNG prng) {
		this.T = T;
		this.lengths = lengths;
		this.times = times;
		this.weight = weight;
		this.statistics = stats;
		this.prng = prng;
		this.tuningParams = new TuningParameter[3];
		this.tuningParams[0] = new ConstantTuningParameter(1.0/3);
		this.tuningParams[1] = new ConstantTuningParameter(1.0/3);
		this.tuningParams[2] = new ConstantTuningParameter(1.0/3);
	}
	
	@Override
	public Set<StateParameter> getParameters() {
		TreeSet<StateParameter> ps = new TreeSet<StateParameter>();
		ps.add(this.T);
		if (this.lengths != null) { ps.add(this.lengths); }
		if (this.times != null) { ps.add(this.times); }
		return ps;
	}

	@Override
	public int getNoOfParameters() {
		int cnt = 1;
		if (this.lengths != null) { cnt++; }
		if (this.times != null)   { cnt++; }
		return cnt;
	}

	@Override
	public int getNoOfSubParameters() {
		int cnt = this.T.getNoOfSubParameters();
		if (this.lengths != null) { cnt += this.lengths.getNoOfSubParameters(); }
		if (this.times != null)   { cnt += this.times.getNoOfSubParameters(); }
		return cnt;
	}

	@Override
	public ProposerWeight getProposerWeight() {
		return this.weight;
	}

	@Override
	public double getWeight() {
		return this.weight.getValue();
	}

	@Override
	public ProposerStatistics getStatistics() {
		return this.statistics;
	}

	@Override
	public List<TuningParameter> getTuningParameters() {
		return Arrays.asList(this.tuningParams);
	}
	
	@Override
	public Proposal cacheAndPerturbAndSetChangeInfo() {
		// First determine move to make.
		double w = this.prng.nextDouble() * (this.tuningParams[0].getValue() + this.tuningParams[1].getValue() +
				this.tuningParams[2].getValue());
		if (w < this.tuningParams[0].getValue()) {
			//this.doSPR();
		} else if (w < this.tuningParams[0].getValue() + this.tuningParams[1].getValue()) {
			// this.doNNI();
		} else {
			// this.doRerooting();
		}
		// TODO Implement!
		return null;
	}

	@Override
	public boolean isEnabled() {
		return this.isActive;
	}

	@Override
	public void setEnabled(boolean isActive) {
		this.isActive = isActive;
	}
	
	/**
	 * Sets the tuning parameters for how often certain branch-swapping
	 * operations will be undertaken. The probability of an operation
	 * is its tuning parameter divided by the sum of all tuning parameters.
	 * @param spr SPR tuning parameter.
	 * @param nni NNI tuning parameter.
	 * @param rerooting rerooting tuning parameter.
	 */
	public void setMoveWeight(TuningParameter spr, TuningParameter nni, TuningParameter rerooting) {
		if (spr.getMinValue() < 0.0 || nni.getMinValue() < 0.0 || rerooting.getMinValue() < 0.0) {
			throw new IllegalArgumentException("Must set non-negative tuning parameter in branch-swapper.");
		}
		this.tuningParams[0] = spr;
		this.tuningParams[1] = nni;
		this.tuningParams[2] = rerooting;
	}
	

	/**
	 * Disconnects the subtrees rooted at input vertices and reconnects them at each other's parents.
	 * Assumptions: Neither argument is root. Arguments are distinct.
	 * <pre>
	 *             .                           .
	 *            / \                         / \
	 *           /   \                       /   \
	 *          /     \                     /     \ 
	 *         /       \                   /       \          Vertices:
	 *        /         \                 /         \         ---------
	 *       /vp         \ wp            /vp         \ wp     v
	 *      / \         / \      to     / \         / \       w
	 *     /   \       /   \           /   \       /   \      vp = v's parent  
	 *    /v  vs\     /w  ws\         /w  vs\     /v  ws\     wp = w's parent  
 	 *   / \   / \   / \   / \       / \   / \   / \   / \    vs = v's sibling  
	 *  /___\ /___\ /___\ /___\     /___\ /___\ /___\ /___\   ws = w's sibling  
	 * </pre>
	 * @param v first vertex.
	 * @param w second vertex.
	 */
	public void swap(int v, int w) {
		assert(v != RBTree.NULL);
		assert(w != RBTree.NULL);
		assert(v != w);
		assert(!this.T.isRoot(v));
		assert(!this.T.isRoot(w));

		int vp = T.getParent(v);
		int wp = T.getParent(w);
		int vs = T.getSibling(v);
		int ws = T.getSibling(w);
		
		// Swap children. It is OK if we accidentally swap children from left to 
		// right while we are at it!
		this.T.setChildren(vp, vs, w);
		this.T.setChildren(wp, ws, v);
	}

	/**
	 * Checks whether a vertex is in a specified subtree.
	 * @param v vertex to look for.
	 * @param r root of subtree.
	 * @return true if v is in the subtree rooted at r; otherwise false.
	 */
	private boolean isInSubtree(int v, int r) {
		while (v != RBTree.NULL) {
			if (v == r) { return true; }
			v = this.T.getParent(v);
		}
		return false;
	}
	
	/**
	 * Places the root of the tree on the arc with sink v.
	 * This is done by identifying the path from v up to the 
	 * root and iteratively rotating the tree at the top.
	 * @param v the sink.
	 */
	private void setRootOn(int v) {
		if (T.isRoot(v)) {
			return;
		}
		int p = T.getParent(v);
		if (T.isRoot(p)) {
			// This means the root is already "on top of" v, so we are done.
			return;
		}
		
		// Due to how rotate works, we want p to be a child of the root. 
		// We solve this recursively.
		this.setRootOn(p);
		assert(this.T.isRoot(this.T.getParent(p)));
		
		// This should be our final rotation
		this.rotate(p, v);
	}

	private void rotate(int p, int v) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getPreInfo(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPostInfo(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearCacheAndClearChangeInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreCacheAndClearChangeInfo() {
		// TODO Auto-generated method stub
		
	}
	
//
//
//	//----------------------------------------------------------------------
//	//
//	// Interface
//	//
//	//----------------------------------------------------------------------
//
//	// Change the rooting of the tree
//	// For example, (a, (b, c)) => ((a,b),c)
//	// Currently, the root node is not just moved, instead we 
//	// scramble the nodes round the root, this might not be optimal, 
//	// see rotate-functions
//	//----------------------------------------------------------------------
//	TreePerturbationEvent*
//	BranchSwapping::doReRoot(Tree &T, bool withLengths, bool withTimes, bool returnInfo)
//	{
//		if ((T.hasTimes() == false) && (withTimes == true))
//		{
//			PROGRAMMING_ERROR("doReRoot() - Times are not modeled !");
//		}
//		else if ((T.hasLengths() == false) && (withLengths == true))
//		{
//			PROGRAMMING_ERROR("doReRoot() - Lengths are not modeled !");
//		}
//
//	#ifdef DEBUG_BRANCHSWAPPING
//		cout << "doReRoot with ";
//		if (withTimes)
//			cout << "times and ";
//		if (withLengths)
//			cout << "lengths\n";
//	#endif
//
//		unsigned treeSize = T.getNumberOfNodes();
//		unsigned node_no = R.genrand_modulo(treeSize - 1);
//		Node *v = T.getNode(node_no);
//
//		// Loop until valid node is found
//
//	#ifdef DEBUG_BRANCHSWAPPING
//		cout << "Node " << v->getNumber () << " is chosen\n";
//	#endif
//		while (v->isRoot() || v->getParent()->isRoot())
//		{
//	#ifdef DEBUG_BRANCHSWAPPING
//			cout << "No reRoot because v->isRoot()= " << v->isRoot () << " and v->getParent()->isRoot()= " << v->getParent()->isRoot() << "\n";
//	#endif
//			node_no = R.genrand_modulo(treeSize-1);
//			v = T.getNode(node_no);	
//
//	#ifdef DEBUG_BRANCHSWAPPING
//			cout << "Node " << v->getNumber () << " is chosen\n";
//	#endif
//		}
//
//		// If specified, store info (before perturbing!).
//		TreePerturbationEvent* info = returnInfo ?
//				TreePerturbationEvent::createReRootInfo(v) : NULL;
//				
//		// We will now let v's parent's parent act as a new root.
//		// Execute rotations until that is the case.
//		Node *parent = v->getParent();
//		if(withTimes) // This can be reduced to rotate(parent, v, withLengths, withTimes)
//		{
//	#ifdef DEBUG_BRANCHSWAPPING
//			cout << "do rotate with times\n";
//	#endif	
//			rotate(parent, v, withLengths, true);
//		}
//		else if(withLengths)
//		{
//	#ifdef DEBUG_BRANCHSWAPPING
//			cout << "do rotate with lengths\n";
//	#endif	
//			rotate(parent, v, true, false);
//		}
//		else
//		{
//	#ifdef DEBUG_BRANCHSWAPPING
//			cout << "do rotate without lengths\n";
//	#endif	
//			rotate(parent, v, false, false);
//		}
//		
//		return info;
//	}
//
//	// Nearest neighbour interchange on two random nodes of the tree.
//	// For example, ((a,b),(c,d)) => ((a,d),(b,c)) somewhere in the tree.
//	// Precondition: if number of leaves is 4, input tree must not be
//	// symmetric.
//	//----------------------------------------------------------------------
//	TreePerturbationEvent*
//	BranchSwapping::doNNI(Tree &T, bool withLengths, bool withTimes, bool returnInfo)
//	{
//		// Disallow symmetric 4-leaf trees.
//		assert(T.getNumberOfLeaves() != 4 || (T.getRootNode()->getLeftChild()->isLeaf()
//				|| T.getRootNode()->getRightChild()->isLeaf()));
//		
//		if ((T.hasTimes() == false) && (withTimes == true))
//		{
//			cerr << "BranchSwapping::doNNI() - Times are not modeled !\n";
//			exit(1);
//		}
//		else if ((T.hasLengths() == false) && (withLengths == true))
//		{
//			cerr << "BranchSwapping::doNNI() - Lengths are not modeled !\n";
//			exit(1);
//		}
//
//	#ifdef DEBUG_BRANCHSWAPPING
//		cout << "doNNI with ";
//		if (withTimes)
//			cout << "times and ";
//		if (withLengths)
//			cout << "lengths\n";
//	#endif
//
//		// Pick a node and choose its parent's sibling to swap with.
//		// Make sure the parent is not the root!
//		unsigned treeSize = T.getNumberOfNodes();
//		Node *v;
//		do // Loop until valid node is found
//		{
//			v = T.getNode(R.genrand_modulo(treeSize));
//
//	#ifdef DEBUG_BRANCHSWAPPING
//			cout << "Node " << v->getNumber () << " is chosen\n";
//	#endif
//		}
//		while (v->isRoot() || 
//				v->getParent()->isRoot() || 
//				v->getParent()->getParent()->isRoot());
//
//		// If specified, store perturbation info (before perturbing!).
//		TreePerturbationEvent* info = returnInfo ?
//				TreePerturbationEvent::createNNIInfo(v) : NULL;
//		
//		// Determine relations
//		Node *w = v->getParent()->getSibling();
//		Node *vs = v->getSibling();
//		Node *vp = v->getParent();
//		Node *wp = w->getParent();
//		Node *wpp = wp->getParent();
//
//		if (withTimes)
//		{
//			// Check sanity
//			assert(T.getTime(*v) < T.getTime(*vp));
//			assert(T.getTime(*vs) < T.getTime(*vp));
//			assert(T.getTime(*w) < T.getTime(*wp));
//			assert(T.getTime(*wp) < T.getTime(*wpp));
//		}
//
//		//     Real vp_rate = 1.0;
//		//     Real wp_rate = 1.0;
//
//		Real intervalMax = wpp->getNodeTime();
//		Real kvp = 1.0;
//		Real kwp = 1.0;
//
//		if ((withTimes) && (withLengths))
//		{	
//			// 	vp_rate = vp->getLength()/vp->getTime();
//			// 	wp_rate = wp->getLength()/wp->getTime();
//
//			Real intervalMinBefore = max(v->getNodeTime(),vs->getNodeTime());
//			assert(intervalMinBefore > 0);
//			kvp = vp->getTime()/(intervalMax - intervalMinBefore);
//			kwp = wp->getTime()/(intervalMax - intervalMinBefore);
//		}
//		
//		// Perform the swap
//		swap(v, w);
//
//		if ((withTimes) && (withLengths))
//		{
//			Real intervalMinAfter = max(max(w->getNodeTime(),
//					vs->getNodeTime()),v->getNodeTime());
//			assert(intervalMinAfter > 0);
//
//			Real vp_time = kvp*(intervalMax - intervalMinAfter);
//			Real wp_time = kwp*(intervalMax - intervalMinAfter);
//
//			wp->setNodeTime(intervalMax - wp_time);
//			vp->setNodeTime(intervalMax - wp_time - vp_time);
//
//			// 	if(T.hasLengths())
//			// 	  {
//			// 	    vp->setLength(vp->getTime()*vp_rate);
//			// 	    wp->setLength(wp->getTime()*wp_rate);	
//			// 	  }    
//		}
//		
//		if (withTimes)
//		{
//			// Check sanity
//			assert(T.getTime(*v) < T.getTime(*vp));
//			assert(T.getTime(*vs) < T.getTime(*vp));
//			assert(T.getTime(*w) < T.getTime(*wp));
//			assert(T.getTime(*wp) != T.getTime(*wpp));
//			assert(T.getTime(*wp) < T.getTime(*wpp)); // This assert has triggered for me! /arve 090925
//		}
//		
//		return info;
//	}
//
//	// SPR with times (and lengths)                              .
////	                                                          . 
////	            .up                      .                    .
////	           / \                      / \                   . 
////	         a/   \                    /   \                  . 
////	         /     \                  /     \ u_c_new_p        .
////	        /       \                /     / \                .
////	       /         \              /     /   \a'             .
////	      /u          \ u_s        /     /     \              .
////	     / \          /\          /     /     u/\             .
//	// b-a/   \        /  \b'      /     /      /  \b'-a'       .
//	//   /u_oc \ u_c  /    \      /     /      /    \ u_c_new    .
//	//  /1\   /2\    /3\  /4\    /1\   /3\    /2\  /4\          .
//	// /___\ /___\  /___\/___\  /___\ /___\  /___\/___\         .
//	//-----------------------------------------------------------
//	TreePerturbationEvent*
//	BranchSwapping::doSPR(Tree &T, bool withLengths, bool withTimes, bool returnInfo)
//	{
//		if ((T.hasTimes() == false) && (withTimes == true))
//		{
//			cerr << "BranchSwapping::doSPR() - Times are not modeled !\n";
//			exit(1);
//		}
//		else if ((T.hasLengths() == false) && (withLengths == true))
//		{
//			cerr << "BranchSwapping::doSPR() - Lengths are not modeled !\n";
//			exit(1);
//		}
//
//	#ifdef DEBUG_BRANCHSWAPPING
//		cout << "doSPR with ";
//		if (withTimes)
//			cout << "times and ";
//		if (withLengths)
//			cout << "lengths\n";
//	#endif
//
//		unsigned treeSize = T.getNumberOfNodes();
//		
//		// Determine which Node (subtree) to move
//		unsigned node_no_u_c = R.genrand_modulo(treeSize);
//		Node *u_c = T.getNode(node_no_u_c); // Node to hang off
//		
//		// Loop until valid node is found
//		while (u_c->isRoot() || u_c->getParent()->isRoot())
//		{
//			node_no_u_c = R.genrand_modulo(treeSize);
//			u_c = T.getNode(node_no_u_c);	
//		}
//		
//		Node *u = u_c->getParent();
//		Node *u_s = u->getSibling();
//		Node *u_oc = u_c->getSibling();
//		Node *u_p = u->getParent();
//
//		if (withTimes)
//		{
//			// check sanity
//			assert(T.getTime(*u_oc) < T.getTime(*u));
//			assert(T.getTime(*u_c) < T.getTime(*u));
//			assert(T.getTime(*u) < T.getTime(*u_p));
//			assert(T.getTime(*u_s) < T.getTime(*u_p));
//		}
//
//		unsigned u_c_new_number = R.genrand_modulo(treeSize);
//		Node *u_c_new = T.getNode(u_c_new_number); // Hang on u to the edge above this node
//
//		while (u_c_new->isRoot() || u_c_new->getNumber()==u->getNumber() || isInSubtree(u_c_new,u) == true) // u must not be hung to a node in its own subtree !
//		{
//			u_c_new_number = R.genrand_modulo(treeSize);
//			u_c_new = T.getNode(u_c_new_number);	
//		}
//
//	#ifdef DEBUG_BRANCHSWAPPING
//		cout << "The edge leading to node " << u_c->getNumber () << " will be hung on the edge above: " << u_c_new->getNumber () << "\n";
//	#endif
//
//		// If specified, store perturbation info (before perturbing!).
//		TreePerturbationEvent* info = returnInfo ?
//				TreePerturbationEvent::createSPRInfo(u_c, u_c_new) : NULL;
//		
//		Real u_nodeTimeBefore = u->getNodeTime();
//
//		Real b = u_oc->getTime()+u->getTime();
//		Real a = u->getTime();
//		Real k = b/a;		
//
//		Real b_prime = u_c_new->getTime();
//		Real a_prime = b_prime/k;
//
//		u_p->setChildren(u_oc,u_s);
//
//		Node *u_c_new_p = u_c_new->getParent();
//		Node *u_c_new_s = u_c_new->getSibling();
//		u->setChildren(u_c,u_c_new);
//		u_c_new_p->setChildren(u_c_new_s,u);
//
//		if (withTimes)
//		{	
//			Real u_nodeTimeAfter = u_c_new->getNodeTime() + b_prime - a_prime;
//			//u->setNodeTime(u_nodeTimeAfter);
//			u->getTree()->setTimeNoAssert(*u,u_nodeTimeAfter);
//
//			Real k_height = u_nodeTimeAfter/u_nodeTimeBefore;
//			recursiveEdgeTimeScaling(u_c,k_height);
//			
//			assert(T.getTime(*u_oc) < T.getTime(*u_p));
//			assert(T.getTime(*u_s) < T.getTime(*u_p));
//			assert(T.getTime(*u_c) < T.getTime(*u));
//			assert(T.getTime(*u_c_new) < T.getTime(*u));
//			assert(T.getTime(*u_c_new_s) < T.getTime(*u_c_new_p));
//			assert(T.getTime(*u) < T.getTime(*u_c_new_p));
//		}
//		
//		// joelgs: This part proved to generate problems when only lengths were modeled,
//		// why I added '&& withTimes'. I can't say anything about the behaviour when both
//		// lengths and times are included.
//		if (withLengths && withTimes)
//		{
//			Real a = u->getLength();
//			Real b = u_oc->getLength();
//			Real c = u_c_new->getLength();
//			Real x = a*c / (a+b);
//			u->setLength(x);
//			u_oc->setLength(a+b);
//			u_c_new->setLength(c-x);
//		}
//		
//		return info;
//	}
//
//	// Place root such that the leaves in outgroup froms a 
//	// monophyletic group
//	//----------------------------------------------------------------------
//	void
//	BranchSwapping::rootAtOutgroup(Tree& T, vector<string> outgroup)
//	{
//		assert(outgroup.size() > 0); //Check precondition
//
//		Node* LCA = T.findLeaf(outgroup[0]);
//		for(unsigned i = 1; i < outgroup.size(); i++)
//		{
//			Node *l =  T.findLeaf(outgroup[i]);
//			LCA = T.mostRecentCommonAncestor(LCA, l);
//		}
//
//		if(LCA->isRoot() || LCA->getParent()->isRoot())
//		{
//			return;
//		}
//		else
//		{
//			Node* parent = LCA->getParent();
//			rotate(parent, LCA, false, false);
//			//! \todo{Check that outgroup is monophyletic, else warn /bens}
//
//			return;
//		}
//	}
//

//
//
//	// Recursively move root to <v,v_child> and update edge lengths.
//	// Notice that the same node will be root node.
//	//
//	// Here is the OLD basic move.
//	//
////	            .vp            .vp                               .
////	           / \c        a+b/ \x                               .
////	         b/   \          /vc \                               . 
////	         /   /3\        /1\   \                              .
////	        /v  /___\      /___\   \v                            .
////	       / \         to         / \                            . 
////	     a/   \                  /   \c-x                        .
////	     /vc   \                /     \    v          (argument 1)
////	    /1\   /2\              /2\   /3\   vc=v_child (argument 2)
//	//   /___\ /___\            /___\ /___\  vp=v_parent  
//	//----------------------------------------------------------------------
//	// Recursively move root to <v,v_child> and update edge lengths.
//	// Notice that the same node will be root node.
//	//
//	// Here is the NEW basic move.
//	//
////	            .vp            .vp                               .
////	           / \c        a-x/ \x                               .
////	         b/   \          /vc \                               . 
////	         /   /3\        /1\   \                              .
////	        /v  /___\      /___\   \v                            .
////	       / \         to         / \                            . 
////	     a/   \                  /   \b+c                        .
////	     /vc   \                /     \    v          (argument 1)
////	    /1\   /2\              /2\   /3\   vc=v_child (argument 2)
//	//   /___\ /___\            /___\ /___\  vp=v_parent  
//	//
//	// PRE: t_vp > t_v && t_vp > t_3
//	// PRE: t_vp > t_v && t_vp > t_1
//	//----------------------------------------------------------------------
//	void
//	BranchSwapping::rotate(Node* v, Node *v_child, 
//			bool withLengths, bool withTimes)
//	{
//		assert(v != NULL);
//		assert(v_child != NULL);
//
//	#ifndef NDEBUG
//		Tree* T = v->getTree();
//	#endif
//		
//		if(withTimes)
//		{
//			// Check sanity
//			assert(T->getTime(*v) < T->getTime(*v->getParent()));
//			assert(T->getTime(*v_child) < T->getTime(*v));
//			assert(T->getTime(*v_child->getSibling()) < T->getTime(*v));
//		}
//
//		Node *v_parent = v->getParent();
//		if(v_parent == 0)
//		{
//			cerr << v->getTree()<< endl;
//			cerr << v->getNumber() << "'s parent is NULL" << endl;
//		}
//
//		if (v_parent->isRoot() == false)
//		{
//			//
//			// Rotate nodes above our current position, 
//			// then rotate here. (Could probably write this more neatly!)
//			//
//			rotate(v_parent, v, withLengths, withTimes);
//			v_parent = v->getParent();
//		}
//
//		Node *v_otherChild = v_child->getSibling();
//		Node *v_sibling    = v->getSibling();
//
//		//a,b,c from figure above
//		Real a = v_child->getLength();
//		Real b = v->getLength();
//		Real c = v_sibling->getLength();
//
//		Real root_time = v->getParent()->getNodeTime();
//		// Lower limit of the interval to be split
//		Real lowerLimit = max(v->getLeftChild()->getNodeTime(),
//				v->getRightChild()->getNodeTime());
//		// Relative position of v in the interval
//		Real k = v->getTime() / (root_time-lowerLimit);
//
//		// Move v
//		v->setChildren(v_otherChild, v_sibling); 
//		v_parent->setChildren(v_child, v);
//
//		if (withTimes)
//		{
//			// Fix branchTime for v, v:s new child (former v_sibling) and 
//			// for v:s moved child. No length change here.
//			// The relative position of v should be kept in its new place
//			lowerLimit = max(v->getLeftChild()->getNodeTime(),
//					v->getRightChild()->getNodeTime());
//			Real v_time = k*(root_time - lowerLimit);
//			assert(v_time > 0);
//			Real v_nodeTime = root_time - v_time;
//
//			v->setNodeTime(v_nodeTime);
//			
//			// Check sanity.
//			assert(T->getTime(*v) < T->getTime(*v->getParent()));
//			assert(T->getTime(*v_child) < T->getTime(*v_child->getParent()));
//			assert(T->getTime(*v_child->getSibling()) < T->getTime(*v_child->getParent()));
//		}
//
//		if (withLengths)
//		{
//			// Fix branchLengths, a,b,c from figure above
//			// 	Real split_point = R.genrand_real3();
//			//  	v_child->setLength((1-split_point) * a);
//			//  	v->setLength(split_point * a);
//			// 	v_sibling->setLength(b+c);
//
//			// Fix branchLengths, Jens deterministic version
//			Real split_point = b/(b+c);
//			v_child->setLength((1-split_point) * a);
//			v->setLength(split_point * a);
//			v_sibling->setLength(b+c);
//	                // TODO: What happens when lengths falls outside the allowed range in the MCMC?
//		}
//	}
//
//	void
//	BranchSwapping::recursiveEdgeTimeScaling(Node* v, Real scaleFactor)
//	{
//		assert(v->getTree()->hasTimes()); // Assert that we model times in this tree
//
//		//Real v_time = v->getTime()*scaleFactor;
//		//Real vp_nodeTime = v->getParent()->getNodeTime();
//		//v->setNodeTime(max(0.0,vp_nodeTime-v_time));
//
//		Real v_nodeTime = v->getNodeTime()*scaleFactor; 
//		//    cout << "v: " << v->getNumber() << " and v_nodeTime: " << v_nodeTime << "\n";  
//		//v->setNodeTime(max(0.0,v_nodeTime));
//		v->getTree()->setTimeNoAssert(*v,max(0.0,v_nodeTime));
//
//		//     v->setTime(v_time);
//
//		if (!v->isLeaf())
//		{
//			//	v->setNodeTime(v->getParent()->getNodeTime()-v_time);
//			recursiveEdgeTimeScaling(v->getLeftChild(),scaleFactor);
//			recursiveEdgeTimeScaling(v->getRightChild(),scaleFactor);
//		}
//	}
//

}
