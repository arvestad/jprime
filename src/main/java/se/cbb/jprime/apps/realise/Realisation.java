package se.cbb.jprime.apps.realise;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.topology.BooleanMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RootedBifurcatingTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.TimesMap;

/**
 * Represents a dated guest tree according to the DLRS model. As such, it also
 * induces a reconciliation with the host tree.
 * <p/>
 * A realisation is presented on PrIME-Newick format, with times
 * as branch lengths, and additional vertex PrIME tags for leaf/speciation/loss
 * status and the internal discretization point:<br/>
 * <code>(A:1.0[&&PrIME VERTEXTYPE=Leaf DiscPt=0,0)],
 * (B:0.2[&&PrIME VERTEXTYPE=Leaf DISCPT=(1,0)],
 * C:0.2[&&PrIME VERTEXTYPE=Leaf DISCPT=(1,0)]):0.8[&&PrIME VERTEXTYPE=Duplication DISCPT=(1,5)]):1.0[&&PrIME VERTEXTYPE=Speciation DISCPT=(2,0)];
 * </code>.
 * 
 * @author Joel Sjöstrand.
 */
public class Realisation {

	/** Guest tree. */
	private RootedBifurcatingTree G;
	
	/** Leaf names of G. */
	private NamesMap names;
	
	/** The times of G. */
	private TimesMap times;
	
	/** For each vertex v of G, states whether it corresponds to a duplication or not. */
	private BooleanMap isDuplication;
	
	/** Placement info of the vertex in the discretised host tree. */
	private StringMap placements;
	
	/**
	 * Constructor.
	 * @param G tree topology.
	 * @param names names of G.
	 * @param times times of G.
	 * @param isDup for each vertex v of G: true if v corresponds to a duplication; false if v corresponds to a speciation or leaf.
	 * @param placements for each vertex v of G: discretisation placement info.
	 */
	public Realisation(RootedBifurcatingTree G, NamesMap names, TimesMap times, BooleanMap isDup, StringMap placements) {
		this.G = G;
		this.names = names;
		this.times = times;
		this.isDuplication = isDup;
		this.placements = placements;
	}

	/**
	 * Returns the tree on Newick format.
	 * @return tree on Newick format.
	 */
	@Override
	public String toString() {
		try {
			int sz = this.G.getNoOfVertices();
			
			// Create meta tags.
			// Holds vertex status (leaf/speciation/duplication),
			// e.g., "[&&PrIME VertexType=Duplication]".
			StringMap meta = new StringMap("Meta", sz);
			for (int v = 0; v < sz; ++v) {
				StringBuilder sb = new StringBuilder(256);
				if (this.G.isLeaf(v)) {
					sb.append("[&&PRIME VERTEXTYPE=Leaf");
				} else if (this.isDuplication.get(v)) {
					sb.append("[&&PRIME VERTEXTYPE=Duplication");
				} else {
					sb.append("[&&PRIME VERTEXTYPE=Speciation");
				}
				sb.append(" DISCPT=").append(placements.get(v)).append("]");
				meta.set(v, sb.toString());
			}
			
			// Convert realisation into Newick string.
			return NewickTreeWriter.write(this.G, this.names, this.times, meta, true);
		} catch (NewickIOException e) {
			throw new RuntimeException("Could not transform realisation into Newick string.", e);
		}
	}
	
	
}
