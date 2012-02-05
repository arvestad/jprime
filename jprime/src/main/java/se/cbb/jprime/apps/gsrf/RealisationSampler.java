package se.cbb.jprime.apps.gsrf;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.io.SampleNewickTree;
import se.cbb.jprime.io.Sampleable;
import se.cbb.jprime.topology.DoubleArrayMap;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.StringMap;

/**
 * Enables sampling of <i>realisations</i>, i.e., dated embeddings
 * of G in S according to the probability distribution of embeddings.
 * <p/>
 * A realisation is presented on PrIME-Newick format, with times
 * as branch lengths, and additional vertex PrIME tags for leaf/speciation/loss
 * status:<br/>
 * <code>(A:1.0[&&PrIME VertexType=Leaf],
 * (B:0.2[&&PrIME VertexType=Leaf],
 * C:0.2[&&PrIME VertexType=Leaf]):0.8[&&PrIME VertexType=Duplication]):1.0[&&PrIME VertexType=Speciation];
 * </code>.
 * 
 * @author Muhammad Owais Mahmudi.
 * @author Joel Sjöstrand.
 */
public class RealisationSampler implements Sampleable {

	/** Guest tree. */
	private RBTree G;
	
	/** Leaf names of G. */
	private NamesMap names;
	
	/** Times of discretised host tree S'. */
	private RBTreeArcDiscretiser times;
	
	/** Lowest possible placement of u of V(G) in discretised S. */
	private IntMap loLims;
	
	/** p11 and more. */
	private DupLossProbs dupLossProbs;
	
	/** Rooted subtree G_u probability for u of V(G). */
	private DoubleArrayMap ats;

	/**
	 * Constructor.
	 * @param G guest tree G.
	 * @param names leaf names of G.
	 * @param times times of discretised host tree S'.
	 * @param loLims lowest possible placement of u of V(G) in discretised S'.
	 * @param dupLossProbs p11, etc.
	 * @param ats rooted subtree G_u probability for u of V(G).
	 */
	public RealisationSampler(RBTree G, NamesMap names, RBTreeArcDiscretiser times,
			IntMap loLims, DupLossProbs dupLossProbs, DoubleArrayMap ats) {
		this.G = G;
		this.names = names;
		this.times = times;
		this.loLims = loLims;
		this.dupLossProbs = dupLossProbs;
		this.ats = ats;
	}
	
	/**
	 * Returns a proper representation of a lower limit.
	 * @param loLim the lower limit, holding arc and discretisation point in one int.
	 * @return [arc in S, discretisation point].
	 */
	private int[] getProperLolims(int loLim) {
		int[] prop = new int[2];
		prop[0] = ((loLim << 16) >>> 16);   // Arc (=head vertex of arc).
		prop[1] = (loLim >>> 16);           // Discretisation point.
		return prop;
	}
	
	@Override
	public Class<?> getSampleType() {
		return SampleNewickTree.class;
	}

	@Override
	public String getSampleHeader() {
		return (this.G.getName() + "Realisation");
	}

	@Override
	public String getSampleValue() {
		try {
			int sz = this.G.getNoOfVertices();
			
			// Holds realisation times as branch lengths.
			DoubleMap realisationTimes = new DoubleMap("GRealisationTimes", sz);
			
			// Holds vertex status (leaf/speciation/duplication),
			// e.g., "[&&PrIME VertexType=Duplication]".
			StringMap statuses = new StringMap("GMetaTags", sz);
			
			// Get discretisation point of tip of host tree.
			int x = this.times.getRoot();
			int xi = this.times.getNoOfSlices(x) + 1;
			
			// TODO Sample realisation and fill realisationTimes and statuses accordingly!
			// ...
			// ...
			// ...
			// ...
			
			// Convert realisation into Newick string.
			return NewickTreeWriter.write(this.G, this.names, realisationTimes, statuses, false);
		} catch (NewickIOException e) {
			throw new RuntimeException("Could not sample realisation.", e);
		}
	}

}
