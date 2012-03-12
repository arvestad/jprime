package se.cbb.jprime.apps.dlrs;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.topology.BooleanMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.TimesMap;

/**
 * Represents a dated guest tree according to the DLRS model. As such, it also
 * induces a reconciliation with the host tree.
 * <p/>
 * A realisation is presented on PrIME-Newick format, with times
 * as branch lengths, and additional vertex PrIME tags for leaf/speciation/loss
 * status:<br/>
 * <code>(A:1.0[&&PrIME VertexType=Leaf],
 * (B:0.2[&&PrIME VertexType=Leaf],
 * C:0.2[&&PrIME VertexType=Leaf]):0.8[&&PrIME VertexType=Duplication]):1.0[&&PrIME VertexType=Speciation];
 * </code>.
 * 
 * @author Joel Sjöstrand.
 */
public class Realisation {

	/** Guest tree. */
	private RBTree G;
	
	/** Leaf names of G. */
	private NamesMap names;
	
	/** The times of G. */
	private TimesMap times;
	
	/** For each vertex v of G, states whether it corresponds to a duplication or not. */
	private BooleanMap isDuplication;
	
	/**
	 * Constructor.
	 * @param G tree topology.
	 * @param names names of G.
	 * @param times times of G.
	 * @param isDup for each vertex v of G: true if v corresponds to a duplication; false if v corresponds to a speciation or leaf.
	 */
	public Realisation(RBTree G, NamesMap names, TimesMap times, BooleanMap isDup) {
		this.G = G;
		this.names = names;
		this.times = times;
		this.isDuplication = isDup;
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
				if (this.G.isLeaf(v)) {
					meta.set(v, "[&&PrIME VertexType=Leaf]");
				} else if (this.isDuplication.get(v)) {
					meta.set(v, "[&&PrIME VertexType=Duplication]");
				} else {
					meta.set(v, "[&&PrIME VertexType=Speciation]");
				}
			}
			
			// Convert realisation into Newick string.
			return NewickTreeWriter.write(this.G, this.names, this.times, meta, true);
		} catch (NewickIOException e) {
			throw new RuntimeException("Could not transform realisation into Newick string.", e);
		}
	}
	
	
}
