package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;

/**
 * Provides means for generating a uniform tree.
 * 
 * @author Joel Sjöstrand.
 */
public class UniformRBTreeGenerator {
	
	/**
	 * Creates a uniform bifurcating tree by
	 * simply joining two random vertices into a cherry until
	 * all have been joined.
	 * @param treeName the tree parameter's name.
	 * @param leafNames leaf names of the tree to be created.
	 * @param prng pseudo-random number generator.
	 * @return tree and its names map.
	 */
	public static Pair<RBTree,NamesMap> createUniformTree(String treeName,
			List<String> leafNames, PRNG prng) {
		int k = leafNames.size();
		int n = k * 2 - 1;
		// Number leaves 0,...,k-1.
		String[] vals = new String[n];
		NamesMap names = new NamesMap(treeName + "Names", leafNames.toArray(vals));
		
		// Create tree.
		RBTree T = new RBTree(treeName, k);
		ArrayList<Integer> looseEnds = new ArrayList<Integer>(k);
		for (int i = 0; i < k; ++i) {
			looseEnds.add(i);
		}
		int z = k;   // Parent vertex in cherry.
		while (z < n) {
			// Randomly pick two loose ends and join to a cherry.
			int x = looseEnds.remove(prng.nextInt(looseEnds.size()));
			int y = looseEnds.remove(prng.nextInt(looseEnds.size()));
			T.setParentAndChildren(z, x, y);
			looseEnds.add(z);
			z++;
		}
		T.setRoot(n - 1);
		return new Pair<RBTree, NamesMap>(T, names);
	}

}
