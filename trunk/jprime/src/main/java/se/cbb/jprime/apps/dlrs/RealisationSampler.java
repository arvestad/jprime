package se.cbb.jprime.apps.dlrs;

import se.cbb.jprime.topology.DoubleArrayMap;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;

/**
 * Enables sampling of <i>realisations</i>, i.e., dated embeddings
 * of G in S according to the probability distribution of embeddings under the DLRS model.
 * <p/>
 * 
 * @author Muhammad Owais Mahmudi.
 * @author Joel Sjöstrand.
 */
public class RealisationSampler {


	/**
	 * Samples a realisation given the input guest tree, "at-probabilities", p11-probabilities, etc.
	 * @param G guest tree G.
	 * @param names leaf names of G.
	 * @param times times of discretised host tree S'.
	 * @param loLims lowest possible placement of u of V(G) in discretised S'.
	 * @param dupLossProbs p11, etc.
	 * @param ats rooted subtree G_u probability for u of V(G).
	 */
	public static Realisation sample(RBTree G, NamesMap names, RBTreeArcDiscretiser times,
			IntMap loLims, DupLossProbs dupLossProbs, DoubleArrayMap ats) {
		// TODO: Implement!
		return null;
	}
	
	/**
	 * Returns a proper representation of a lower limit.
	 * @param loLim the lower limit, holding arc and discretisation point in one int.
	 * @return [arc in S, discretisation point].
	 */
	private static int[] getProperLolims(int loLim) {
		int[] prop = new int[2];
		prop[0] = ((loLim << 16) >>> 16);   // Arc (=head vertex of arc).
		prop[1] = (loLim >>> 16);           // Discretisation point.
		return prop;
	}

}
