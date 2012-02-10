package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.Set;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;

/**
 * Given a guest-to-host leaf map and a host tree, provides
 * means for generating a reconciled guest tree.
 * See also <code>UniformRBTreeGenerator</code>.
 * <p/>
 * In the future, this could include e.g. a method for the most parsimonious reconciliation.
 * 
 * @author Joel Sjöstrand.
 */
public class ReconciledRBTreeGenerator {
	
	/**
	 * Simplistic generator of a reconciled guest tree, which just joins all guest tree
	 * leaves as in-paralogs, then connects those subtrees congruently with host tree.
	 * @param GSMap guest-to-host tree leaf map.
	 * @param S host tree topology.
	 * @param SNames leaf/vertex names of S.
	 * @param paramName the parameter name of the guest topology G to be created. 
	 * @param prng pseudo-random number generator.
	 * @return guest tree topology and its names map.
	 */
	public static Pair<RBTree,NamesMap> createRandomSimplisticGuestTree(GuestHostMap GSMap,
			RootedBifurcatingTreeParameter S, NamesMap SNames, String paramName, PRNG prng) {
		int lsz = GSMap.getAllGuestLeafNames().size();
		int vsz = lsz * 2 - 1;
		String[] vals = new String[vsz];
		RBTree G = new RBTree(paramName, lsz);
		int x = getSimplisticVertex(prng, GSMap, S, SNames, S.getRoot(), G, vals, 0);
		G.setRoot(x);	
		NamesMap GNames = new NamesMap(paramName + "Names", vals);
		return new Pair<RBTree, NamesMap>(G, GNames);
	}
	
	
	private static int getSimplisticVertex(PRNG prng, GuestHostMap GSMap,
			RootedBifurcatingTree S, NamesMap SNames, int X,
			RBTree G, String[] GNames, int nxt) {
		if (S.isLeaf(X)) {
			// Label and number all guest leaves in this host leaf.
			Set<String> names = GSMap.getGuestLeafNames(SNames.get(X));
			if (names == null || names.isEmpty()) {
				return RBTree.NULL;
			}
			ArrayList<Integer> looseEnds = new ArrayList<Integer>(names.size());
			for (String name : names) {
				GNames[nxt] = name;
				looseEnds.add(nxt);
				nxt++;
			}
			// Join'em.
			while (looseEnds.size() > 1) {
				// Randomly pick two loose ends and join to a cherry.
				int x = looseEnds.remove(prng.nextInt(looseEnds.size()));
				int y = looseEnds.remove(prng.nextInt(looseEnds.size()));
				G.setParentAndChildren(nxt, x, y);
				looseEnds.add(nxt);
				nxt++;
			}
			return (nxt - 1);  // Root of created subtree.
		} else {
			// Interior host vertex case.
			int x = getSimplisticVertex(prng, GSMap, S, SNames, S.getLeftChild(X), G, GNames, nxt);
			if (x != RBTree.NULL) { nxt = x + 1; }
			int y = getSimplisticVertex(prng, GSMap, S, SNames, S.getRightChild(X), G, GNames, nxt);
			if (y != RBTree.NULL) { nxt = y + 1; }
			if (x == RBTree.NULL && y == RBTree.NULL) {
				return RBTree.NULL;
			}
			if (x == RBTree.NULL) {
				return y;
			}
			if (y == RBTree.NULL) {
				return x;
			}
			G.setParentAndChildren(nxt, x, y);  // We set x left and y right for no particular reason.
			return nxt;
		}
	}
}
