package se.cbb.jprime.topology;

import se.cbb.jprime.math.PRNG;

/**
 * Given a guest-to-host leaf map and a host tree, provides
 * various methods for generating a reconciled guest tree.
 * See also <code>UniformRBTreeGenerator</code>.
 * <p/>
 * In the future, this could include methods based on the MPR
 * reconciliation, and even NJ.
 * 
 * @author Joel Sjöstrand.
 */
public class ReconciledRBTreeGenerator {

	/**
	 * Simplistic generator of a reconciled guest tree, which just
	 * joins all guest tree leaves as in-paralogs, then connects
	 * those edges congruent with host tree.
	 * @param GSMap guest-to-host tree leaf map.
	 * @param S host tree topology.
	 * @param SNames names of S.
	 * @return guest tree topology and its names map.
	 */
	public static RBTree createSimplisticGuestTree(GuestHostMap GSMap,
			RootedBifurcatingTreeParameter S, NamesMap SNames) {
		// TODO: Implement.
		return null;
	}
	
	/**
	 * Creates a guest tree using <code>createSimplisticGuestTree(...)</code>,
	 * then perturbs it through a user-defined number of branch swapping
	 * events.
	 * @param GSMap guest-to-host tree leaf map.
	 * @param S host tree topology.
	 * @param SNames names of S.
	 * @param prng pseudo-random number generator.
	 * @param n the numer of branch swapping events to perform.
	 * @return guest tree topology and its names map.
	 */
	public static RBTree createRandomSimplisticGuestTree(GuestHostMap GSMap,
			RootedBifurcatingTreeParameter S, NamesMap SNames, PRNG prng, int n) {
		// TODO: Implement.
		return null;
	}
}
