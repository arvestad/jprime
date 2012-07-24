package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.cbb.jprime.math.LogDouble;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.MetropolisHastingsProposal;
import se.cbb.jprime.mcmc.Proposal;
import se.cbb.jprime.mcmc.Proposer;
import se.cbb.jprime.mcmc.ProposerStatistics;
import se.cbb.jprime.mcmc.StateParameter;
import se.cbb.jprime.mcmc.TuningParameter;

/**
 * Proposer which perturbs the topology of a bifurcating rooted tree.
 * In order to increase the probability of accepting the new topology,
 * corresponding lengths and/or times may also be changed according
 * to simple heuristics.
 * <p/>
 * Currently, the tree is perturbed using NNI, SPR (also superset of NNI), and
 * rerooting. By default, these are selected probability [0.5,0,3,0,2], but
 * this may be substituted using <code>setOperationWeights(...)</code>.
 * 
 * @author Lars Arvestad.
 * @author Örjan Åkerborg.
 * @author Joel Sjöstrand.
 */
public class RBTreeBranchSwapper implements Proposer {

	/** Topology. */
	protected RBTree T;
	
	/** Lengths. Null if not used. */
	protected DoubleMap lengths;
	
	/** Times. Null if not used. */
	protected TimesMap times;
	
	/** Statistics. */
	protected ProposerStatistics statistics = null;
	
	/** Pseudo-random number generator. */
	protected PRNG prng;
	
	/** Array of weights for NNI, SPR and rerooting respectively. */
	protected double[] operationWeights;
	
	/** Active flag. */
	protected boolean isActive;
	
	/** Last operation type. */
	protected String lastOperationType;
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, PRNG prng) {
		this(T, null, null, prng);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param times times of T. May be null.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, TimesMap times, PRNG prng) {
		this(T, null, times, prng);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, DoubleMap lengths, PRNG prng) {
		this(T, lengths, null, prng);
	}
	
	/**
	 * Constructor.
	 * @param T tree topology to perturb.
	 * @param lengths lengths of T. May be null.
	 * @param times times of T. May be null.
	 * @param prng pseudo-random number generator.
	 */
	public RBTreeBranchSwapper(RBTree T, DoubleMap lengths, TimesMap times, PRNG prng) {
		this.T = T;
		this.lengths = lengths;
		this.times = times;
		this.prng = prng;
		this.operationWeights = new double[] {0.5, 0.3, 0.2};
		this.isActive = true;
		this.lastOperationType = null;
	}
	
	@Override
	public Set<StateParameter> getParameters() {
		HashSet<StateParameter> ps = new HashSet<StateParameter>();
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
	public ProposerStatistics getStatistics() {
		return this.statistics;
	}

	@Override
	public List<TuningParameter> getTuningParameters() {
		return null;
	}
	
	/**
	 * Asserts that all vertices are present and unique.
	 * @return true if all presents
	 */
	private boolean verticesAreUnique() {
		String nw = this.T.toString().replace(")", ",").replace("(", "").replace(";", "");
		String[] vertices = nw.split(",");
		if (vertices.length != this.T.getNoOfVertices()) {
			return false;
		}
		HashSet<String> visited = new HashSet<String>(vertices.length);
		for (String x : vertices) {
			if (!visited.add(x)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Proposal cacheAndPerturb(Map<Dependent, ChangeInfo> changeInfos) {
		// First determine move to make.
		double w = this.prng.nextDouble() * (this.operationWeights[0] + this.operationWeights[1] + this.operationWeights[2]);
		
		// Cache everything.
		this.T.cache();
		if (this.lengths != null) {
			this.lengths.cache(null);
		}
		if (this.times != null) {
			this.times.cache(null);
		}
		
		// Perturb!
		//System.out.println("\n" + this.T.getSampleValue());
		if (w < this.operationWeights[0]) {
			this.doNNI();
			this.lastOperationType = "NNI";
		} else if (w < this.operationWeights[0] + this.operationWeights[1]) {
			this.doSPR();
			this.lastOperationType = "SPR";
		} else {
			this.doReroot();
			this.lastOperationType = "Reroot";
		}
		//System.out.println("\n" + this.T.getSampleValue());
		assert this.verticesAreUnique();
		
		// Note changes. Just say that all sub-parameters have changed.
		ArrayList<StateParameter> affected = new ArrayList<StateParameter>(3);
		changeInfos.put(this.T, new ChangeInfo(this.T));
		int no = this.T.getNoOfSubParameters();
		affected.add(this.T);
		if (this.lengths != null) {
			changeInfos.put(this.lengths, new ChangeInfo(this.lengths));
			affected.add(this.lengths);
			no += this.lengths.getNoOfSubParameters();
		}
		if (this.times != null) {
			changeInfos.put(this.times, new ChangeInfo(this.times));
			affected.add(this.times);
			no += this.getNoOfSubParameters();
		}
		
		// Right now, we consider forward-backward probabilities as equal.
		return new MetropolisHastingsProposal(this, new LogDouble(1.0), new LogDouble(1.0), affected, no);
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
	 * Sets the operation weights for how often certain branch-swapping
	 * operations will be undertaken. The probability of an operation
	 * is its weight divided by the sum of all weights.
	 * @param nni NNI tuning parameter.
	 * @param spr SPR tuning parameter.
	 * @param rerooting rerooting tuning parameter.
	 */
	public void setOperationWeights(double nni, double spr, double rerooting) {
		if (nni < 0.0 || spr < 0.0 || rerooting < 0.0) {
			throw new IllegalArgumentException("Must set non-negative operation weight in branch-swapper.");
		}
		this.operationWeights[0] = nni;
		this.operationWeights[1] = spr;
		this.operationWeights[2] = rerooting;
	}
	

	/**
	 * Disconnects the subtrees rooted at input vertices and reconnects them at each other's parents.
	 * Prerequisites: Neither argument is root. Arguments are distinct.
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
		this.T.setParentAndChildren(vp, vs, w);
		this.T.setParentAndChildren(wp, ws, v);
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
	@SuppressWarnings("unused")
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
		RBTreeBranchSwapper.rotate(this.T, p, v, this.lengths, this.times);
	}

	@Override
	public String getPreInfo(String prefix) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(prefix).append("BRANCH-SWAPPER PROPOSER\n");
		sb.append(prefix).append("Perturbed tree parameter: ").append(this.T.getName()).append('\n');
		sb.append(prefix).append("Perturbed times parameter: ").append(this.times == null ? "None" : this.times.getName()).append('\n');
		sb.append(prefix).append("Perturbed lengths parameter: ").append(this.lengths == null ? "None" : this.lengths.getName()).append('\n');
		sb.append(prefix).append("Is active: ").append(this.isActive).append("\n");
		sb.append(prefix).append("Operation weights (NNI, SPR, rerooting): ").append(Arrays.toString(this.operationWeights)).append('\n');
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPreInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	@Override
	public String getPostInfo(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("BRANCH-SWAPPER PROPOSER\n");
		sb.append(prefix).append("Perturbed tree parameter: ").append(this.T.getName()).append('\n');
		sb.append(prefix).append("Perturbed times parameter: ").append(this.times == null ? "None" : this.times.getName()).append('\n');
		sb.append(prefix).append("Perturbed lengths parameter: ").append(this.lengths == null ? "None" : this.lengths.getName()).append('\n');
		if (this.statistics != null) {
			sb.append(prefix).append("Statistics:\n").append(this.statistics.getPostInfo(prefix + '\t'));
		}
		return sb.toString();
	}

	/**
	 * Changes the rooting of the tree, for example, <code>(a,(b,c)) => ((a,b),c)</code>.
	 * Currently, the root node is not just moved, instead we 
	 * scramble the nodes round the root, this might not be optimal, 
	 * see rotate-methods.
	 */
	private void doReroot() {
		int treeSize = this.T.getNoOfVertices();
		if (treeSize <= 3) {
			throw new UnsupportedOperationException("Cannot perform re-rooting on tree topology with 2 leaves or less.");
		}

		// Loop until valid node is found.
		int v = this.prng.nextInt(treeSize);
		while (this.T.isRoot(v) || this.T.isRoot(this.T.getParent(v))) {
			v = this.prng.nextInt(treeSize);
		}

		// We will now let v's parent's parent act as a new root.
		// Execute rotations until that is the case.
		int parent = this.T.getParent(v);
		RBTreeBranchSwapper.rotate(this.T, parent, v, this.lengths, this.times);
	}

	/**
	 * Nearest neighbour interchange (NNI) on a random node of the tree, and a random neighbour.
	 * For example, <code>((a,b),(c,d)) => ((a,d),(b,c))</code> somewhere in the tree.
	 * Precondition: if the number of leaves is 4, input tree must not be symmetric.
	 */
	private void doNNI() {
		// Disallow symmetric 4-leaf trees.
		if (this.T.getNoOfLeaves() == 4 && this.T.isLeaf(this.T.getLeftChild(this.T.getRoot())) && this.T.isLeaf(this.T.getRightChild(this.T.getRoot()))) {
			throw new UnsupportedOperationException("Cannot perform NNI on symmetric tree topology with 4 leaves.");
		}
		
		// Pick a vertex and choose its parent's sibling to swap with.
		// Make sure the parent is not the root!
		int treeSize = T.getNoOfVertices();
		int v;
		do {
			// Loop until valid node is found
			v = this.prng.nextInt(treeSize);
		} while (this.T.isRoot(v) || this.T.isRoot(this.T.getParent(v)) || this.T.isRoot(this.T.getParent(this.T.getParent(v))));

		// Determine relations.
		int w = this.T.getSibling(this.T.getParent(v));
		int vs = this.T.getSibling(v);
		int vp = this.T.getParent(v);
		int wp = this.T.getParent(w);
		int wpp = this.T.getParent(wp);

		// Time heuristics stuff.
		double intervalMax = Double.NaN;
		double kvp = Double.NaN;
		double kwp = Double.NaN;
		// if (lengths != null) {
		// 	double vp_rate = 1.0;
		// 	double wp_rate = 1.0;
		// }
		
		if (this.times != null) {
			// Check sanity.
			assert(this.times.getVertexTime(v) < this.times.getVertexTime(vp));
			assert(this.times.getVertexTime(vs) < this.times.getVertexTime(vp));
			assert(this.times.getVertexTime(w) < this.times.getVertexTime(wp));
			assert(this.times.getVertexTime(wp) < this.times.getVertexTime(wpp));
			
			// IMPORTANT: Joel: I don't see why the below should be applied only
			// when BOTH times and lengths are present, so now only times matter.
			
			intervalMax = this.times.getVertexTime(wpp);
			double intervalMinBefore = Math.max(this.times.getVertexTime(v), this.times.getVertexTime(vs));
			assert(intervalMinBefore > 0);
			kvp = this.times.getArcTime(vp) / (intervalMax - intervalMinBefore);
			kwp = this.times.getArcTime(wp) / (intervalMax - intervalMinBefore);
			
			// if (lengths != null) {
			// vp_rate = this.lengths.get(vp) / this.times.getArcTime(vp);
			// wp_rate = this.lengths.get(wp) / this.times.getArcTime(wp);
			// }
		}
		
		// Perform the actual NNI swap.
		this.swap(v, w);

		// Carry out some time-length heuristics in accordance with the swap.
		if (this.times != null) {
			
			// IMPORTANT: Joel: Corresponds to my comment before the swap.
			// Now only times considered.
			
			double intervalMinAfter = Math.max(Math.max(this.times.getVertexTime(w), this.times.getVertexTime(vs)), this.times.getVertexTime(v));
			assert(intervalMinAfter > 0);

			double vp_time = kvp * (intervalMax - intervalMinAfter);
			double wp_time = kwp * (intervalMax - intervalMinAfter);

			double[] vts = this.times.getVertexTimes();
			double[] ats = this.times.getArcTimes();
			vts[wp] = intervalMax - wp_time;
			vts[vp] = intervalMax - wp_time - vp_time;
			ats[wp] = vts[wpp] - vts[wp];
			ats[vp] = vts[wp] - vts[vp];
			ats[v] = vts[wp] - vts[v];
			ats[w] = vts[vp] - vts[w];
			ats[vs] = vts[vp] - vts[vs];
			
			// if (lengths != null) {
			//  this.lengths.set(vp, this.times.getArcTime(vp) * vp_rate);
			// 	this.lengths.set(wp, this.times.getArcTime(wp) * wp_rate);	
			// }    
		
			// Check sanity.
			assert(this.times.getVertexTime(v) < this.times.getVertexTime(vp));
			assert(this.times.getVertexTime(vs) < this.times.getVertexTime(vp));
			assert(this.times.getVertexTime(w) < this.times.getVertexTime(wp));
			assert(this.times.getVertexTime(wp) < this.times.getVertexTime(wpp));	// This assert has triggered for me! /arve 2009-09-25.
		}
	}

	
	/**
	 * Performs subtree pruning and regrafting (SPR). That is:
	 * <pre>
	 *              .up                      .                    .
	 *             / \                      / \                   . 
	 *           a/   \                    /   \                  . 
	 *           /     \                  /     \ u_c_new_p       .
	 *          /       \                /     / \                .
	 *         /         \              /     /   \a'             .
	 *        /u          \ u_s        /     /     \              .
	 *       / \          /\          /     /     u/\             .
	 *   b-a/   \        /  \b'      /     /      /  \b'-a'       .
	 *     /u_oc \ u_c  /    \      /     /      /    \ u_c_new   .
	 *    /1\   /2\    /3\  /4\    /1\   /3\    /2\  /4\          .
	 *   /___\ /___\  /___\/___\  /___\ /___\  /___\/___\         .
	 * </pre>
	 */
	private void doSPR() {

		int treeSize = this.T.getNoOfVertices();
		
		// Determine which vertex (subtree) to move.
		int u_c = this.prng.nextInt(treeSize);	// Node to hang off.
		
		// Loop until valid vertex is found.
		while (this.T.isRoot(u_c) || this.T.isRoot(this.T.getParent(u_c))) {
			u_c = this.prng.nextInt(treeSize);
		}

		int u = this.T.getParent(u_c);
		int u_s = this.T.getSibling(u);
		int u_oc = this.T.getSibling(u_c);
		int u_p = this.T.getParent(u);

		if (this.times != null) {
			// Check sanity.
			assert(this.times.getVertexTime(u_oc) < this.times.getVertexTime(u));
			assert(this.times.getVertexTime(u_c) < this.times.getVertexTime(u));
			assert(this.times.getVertexTime(u) < this.times.getVertexTime(u_p));
			assert(this.times.getVertexTime(u_s) < this.times.getVertexTime(u_p));
		}

		// Hang on u to the arc above this vertex.
		int u_c_new = this.prng.nextInt(treeSize);

		// Loop until valid.
		// u must not be hung to a vertex in its own subtree!
		while (this.T.isRoot(u_c_new) || this.isInSubtree(u_c_new, u)) {
			u_c_new = this.prng.nextInt(treeSize);
		}
		
		// Time heuristics stuff.
		double u_nodeTimeBefore = Double.NaN;
		double b_prime = Double.NaN;
		double a_prime = Double.NaN;
		if (this.times != null) {
			u_nodeTimeBefore = this.times.getVertexTime(u);
			double b = this.times.getArcTime(u_oc) + this.times.getArcTime(u);
			double a = this.times.getArcTime(u);
			double k = b / a;		
	
			b_prime = this.times.getArcTime(u_c_new);
			a_prime = b_prime / k;
		}
		
		// Do the SPR move.
		this.T.setParentAndChildren(u_p, u_oc, u_s);
		int u_c_new_p = this.T.getParent(u_c_new);    // Order seems to matter when u_s=u_c_new:
		int u_c_new_s = this.T.getSibling(u_c_new);   // must make above move first! /Joel
		this.T.setParentAndChildren(u, u_c, u_c_new);
		this.T.setParentAndChildren(u_c_new_p, u_c_new_s, u);

		// Time heuristics.
		if (this.times != null) {	
			double u_nodeTimeAfter = this.times.getVertexTime(u_c_new) + b_prime - a_prime;
			//this.times.setVertexTime(u, u_nodeTimeAfter);
			double[] vts = this.times.getVertexTimes();
			double[] ats = this.times.getArcTimes();
			vts[u] = u_nodeTimeAfter;
			ats[u] = vts[u_c_new_p] - vts[u];
			ats[u_c] = u_nodeTimeAfter - vts[u_c];
			ats[u_c_new] = u_nodeTimeAfter - vts[u_c_new];

			double k_height = u_nodeTimeAfter / u_nodeTimeBefore;
			RBTreeBranchSwapper.recursiveEdgeTimeScaling(this.T, times, u_c, k_height);
			
			assert(this.times.getVertexTime(u_oc) < this.times.getVertexTime(u_p));
			assert(this.times.getVertexTime(u_s) < this.times.getVertexTime(u_p));
			assert(this.times.getVertexTime(u_c) < this.times.getVertexTime(u));
			assert(this.times.getVertexTime(u_c_new) < this.times.getVertexTime(u));
			assert(this.times.getVertexTime(u_c_new_s) < this.times.getVertexTime(u_c_new_p));
			assert(this.times.getVertexTime(u) < this.times.getVertexTime(u_c_new_p));
		}
		
		// Length heuristics.
		if (lengths != null) {
			double a = this.lengths.get(u);
			double b = this.lengths.get(u_oc);
			double c = this.lengths.get(u_c_new);
			double x = a * c / (a + b);
			this.lengths.set(u, x);
			this.lengths.set(u_oc, a + b);
			this.lengths.set(u_c_new, c - x);
		}	

	}
	
	
	/** 
	 * Reroots a tree so that the leaves in the outgroup forms a 
	 * monophyletic group.
	 * @param T the topology to reroot.
	 * @param names the names of the leaves.
	 * @param outgroup the subset of names of the outgroup.
	 */
	public static void rootAtOutgroup(RBTree T, NamesMap names, List<String> outgroup) {
		assert(outgroup.size() > 0);

		int lca = names.getVertex(outgroup.get(0));
		for (int i = 1; i < outgroup.size(); ++i) {
			int l =  names.getVertex(outgroup.get(i));
			lca = T.getLCA(lca, l);
		}

		if (T.isRoot(lca) || T.isRoot(T.getParent(lca))) {
			return;
		} else {
			int parent = T.getParent(lca);
			RBTreeBranchSwapper.rotate(T, parent, lca, null, null);
			// TODO: Check that outgroup is monophyletic, else warn /bens.
			return;
		}
	}


	/**
	 * Recursively moves root to <v,v_child> and updates arc lengths.
	 * Notice that the same node will be root node.
	 * <p/>
	 * Here is the OLD basic move:
	 * <pre>
	 *           .vp            .vp                               .
	 *          / \c        a+b/ \x                               .
	 *        b/   \          /vc \                               . 
	 *        /   /3\        /1\   \                              .
	 *       /v  /___\      /___\   \v                            .
	 *      / \         to         / \                            . 
	 *    a/   \                  /   \c-x                        .
	 *    /vc   \                /     \    v          (argument 1)
	 *   /1\   /2\              /2\   /3\   vc=v_child (argument 2)
	 *  /___\ /___\            /___\ /___\  vp=v_parent
	 * </pre>
	 * <p/>
	 * Here is the NEW basic move:
	 * <pre>
	 *          .vp            .vp                               .
	 *         / \c        a-x/ \x                               .
	 *       b/   \          /vc \                               . 
	 *       /   /3\        /1\   \                              .
	 *      /v  /___\      /___\   \v                            .
	 *     / \         to         / \                            . 
	 *   a/   \                  /   \b+c                        .
	 *   /vc   \                /     \    v          (argument 1)
	 *  /1\   /2\              /2\   /3\   vc=v_child (argument 2)
	 * /___\ /___\            /___\ /___\  vp=v_parent
	 * </pre>
	 * <p/>
	 * Prerequisites:
	 * t_vp > t_v && t_vp > t_3<br/>
	 * t_vp > t_v && t_vp > t_1.
	 * 
	 * @param T the tree.
	 * @param v see illustration above.
	 * @param v_child see illustration above.
	 * @param lengths the lengths of the tree (if any).
	 * @param times the times of the tree (if any).
	 */
	private static void rotate(RBTree T, int v, int v_child, DoubleMap lengths, TimesMap times) {
		assert(v != RBTree.NULL);
		assert(v_child != RBTree.NULL);
	
		if (times != null) {
			// Check sanity
			assert(times.getVertexTime(v) < times.getVertexTime(T.getParent(v)));
			assert(times.getVertexTime(v_child) < times.getVertexTime(v));
			assert(times.getVertexTime(T.getSibling(v_child)) < times.getVertexTime(v));
		}
		
		int v_parent = T.getParent(v);
		if (v_parent == RBTree.NULL) {
			throw new IllegalArgumentException("In rotating branch-swapping, vertex v must not be the root.");
		}

		if (!T.isRoot(v_parent)) {
			// Rotate nodes above our current position, 
			// then rotate here. (Could probably write this more neatly!)
			RBTreeBranchSwapper.rotate(T, v_parent, v, lengths, times);
			v_parent = T.getParent(v);
		}

		int v_otherChild = T.getSibling(v_child);
		int v_sibling    = T.getSibling(v);

		// Pre-move times heuristics.
		double root_time = Double.NaN;
		double lowerLimit = Double.NaN;
		double k = Double.NaN;
		if (times != null) {
			root_time = times.getVertexTime(T.getParent(v));
			
			// Lower limit of the interval to be split
			lowerLimit = Math.max(times.getVertexTime(T.getLeftChild(v)),
					times.getVertexTime(T.getRightChild(v)));
			
			// Relative position of v in the interval.
			k = times.getArcTime(v) / (root_time - lowerLimit);
		}
		
		// Pre-move length heuristics.
		double a = Double.NaN;
		double b = Double.NaN;
		double c = Double.NaN;
		if (lengths != null) {
			// a,b,c from figure above
			a = lengths.get(v_child);
			b = lengths.get(v);
			c = lengths.get(v_sibling);
		}

		// Move v.
		T.setParentAndChildren(v, v_otherChild, v_sibling);
		T.setParentAndChildren(v_parent, v_child, v);

		// Post-move times heuristics.
		if (times != null) {
			// Fix branch times for v, v:s new child (v:s_former sibling) and 
			// for v:s moved child. No length change here.
			// The relative position of v should be kept in its new place.
			lowerLimit = Math.max(times.getVertexTime(T.getLeftChild(v)),
					times.getVertexTime(T.getRightChild(v)));
			double v_time = k * (root_time - lowerLimit);
			assert(v_time > 0);
			double v_nodeTime = root_time - v_time;

			// Update the times.
			double[] vts = times.getVertexTimes();
			double[] ats = times.getArcTimes();
			vts[v] = v_nodeTime;
			ats[v] = vts[v_parent] - vts[v];
			ats[v_otherChild] = vts[v] - vts[v_otherChild];
			ats[v_sibling] = vts[v] - vts[v_sibling];
			
			// Check sanity.
			assert(times.getVertexTime(v) < times.getVertexTime(T.getParent(v)));
			assert(times.getVertexTime(v_child) < times.getVertexTime(T.getParent(v_child)));
			assert(times.getVertexTime(T.getSibling(v_child)) < times.getVertexTime(T.getParent(v_child)));
		}

		// Length heuristics.
		if (lengths != null) {
			// Old stuff:
			// Fix branch lengths, a,b,c from figure above.
			// double split_point = R.genrand_real3();
			// v_child->setLength((1-split_point) * a);
			// v->setLength(split_point * a);
			// v_sibling->setLength(b+c);

			// Fix branch lengths, Jens's deterministic version.
			double split_point = b / (b + c);
			lengths.set(v_child, (1 - split_point) * a);
			lengths.set(v, split_point * a);
			lengths.set(v_sibling, b + c);
			// TODO: What happens when lengths fall outside the allowed range in the MCMC?
		}
	}

	/**
	 * Recursively scales a subtree.
	 * @param T the tree.
	 * @param times the times of T.
	 * @param v the subtree root.
	 * @param scaleFactor the scale factor which all times of T_u are rescaled with.
	 */
	private static void recursiveEdgeTimeScaling(RBTree T, TimesMap times, int v, double scaleFactor) {
		assert(times != null);

		double[] vts = times.getVertexTimes();
		double[] ats = times.getArcTimes();
		double v_nodeTime = vts[v] * scaleFactor;
		vts[v] = Math.max(0.0, v_nodeTime);
		ats[v] = vts[T.getParent(v)] - vts[v];

		// v->setTime(v_time);

		if (!T.isLeaf(v)) {
			int lc = T.getLeftChild(v);
			int rc = T.getRightChild(v);
			ats[lc] = vts[v] - vts[lc];
			ats[rc] = vts[v] - vts[rc];
			//	v->setNodeTime(v->getParent()->getVertexTime()-v_time);
			recursiveEdgeTimeScaling(T, times, lc, scaleFactor);
			recursiveEdgeTimeScaling(T, times, rc, scaleFactor);
		}
	}

	@Override
	public void clearCache() {
		if (this.statistics != null) {
			this.statistics.increment(true, this.lastOperationType);
		}
		this.T.clearCache();
		if (this.times != null) {
			this.times.clearCache();
		}
		if (this.lengths != null) {
			this.lengths.clearCache();
		}
	}

	@Override
	public void restoreCache() {
		if (this.statistics != null) {
			this.statistics.increment(false, this.lastOperationType);
		}
		this.T.restoreCache();
		if (this.times != null) {
			this.times.restoreCache();
		}
		if (this.lengths != null) {
			this.lengths.restoreCache();
		}
	}

	@Override
	public void setStatistics(ProposerStatistics stats) {
		this.statistics = stats;
	}
	
	@Override
	public String toString() {
		return "RBTreeBranchSwapper perturbing [" + this.T.getName() + 
			(this.times != null ? ", " + this.times.getName() : "") +
			(this.lengths != null ? ", " + this.lengths.getName() : "") +
			"]";
	}


}
