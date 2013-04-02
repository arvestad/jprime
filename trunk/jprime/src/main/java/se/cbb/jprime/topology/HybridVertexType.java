package se.cbb.jprime.topology;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Available vertex types for a hybrid network.
 * 
 * @author Joel Sjöstrand.
 */
public enum HybridVertexType {

	STEM_TIP,
	SPECIATION,
	LEAF,
	HYBRID_DONOR,
	ALLOPOLYPLOIDIC_HYBRID,
	AUTOPOLYPLOIDIC_HYBRID;
	
	/**
	 * Deduces and validates allowed vertex types from the set of attributes of a vertex.
	 * @param topo the DAG. Must conform to numbering 0,...,|V(G)|-1.
	 * @param n size of DAG.
	 * @return the vertex types.
	 */
	public static EnumMap<HybridVertexType> getVertexTypes(SimpleDirectedGraph<Integer, DiscretisedArc> topo, int n) {
		EnumMap<HybridVertexType> types = new EnumMap<HybridVertexType>("HybridVertexTypes", n);
		for (int x = 0; x < n; ++x) {
			types.set(x, getVertexTyp(x, topo.incomingEdgesOf(x), topo.outgoingEdgesOf(x)));
		}
		return types;
	}
	
	
	/**
	 * Returns the vertex type.
	 * @param x vertex.
	 * @param in ingoing arcs.
	 * @param out outgoing arcs.
	 * @return type.
	 */
	private static HybridVertexType getVertexTyp(int x, Set<DiscretisedArc> in, Set<DiscretisedArc> out) {
		int indegree = (in == null ? 0 : in.size());
		int outdegree = (out == null ? 0 : out.size());
		
		if (indegree == 0) {
			// STEM_TIP.
			if (outdegree != 1) {
				throw new IllegalArgumentException("Invalid stem tip vertex " + x + ": must have exactly one child (or did you forget the incoming edge?).");
			}
			return STEM_TIP;
		}
		
		if (indegree == 1) {
			DiscretisedArc par = in.iterator().next();
			if (outdegree == 0) {
				// LEAF.
				if (par.getArcTime() <= 1e-10) {
					throw new IllegalArgumentException("Invalid arc time: Must be greater than 0 for arc ending in " + x + ".");
				}
				return LEAF;
			}
			
			if (outdegree == 1) {
				// AUTOPOLYPLOIDIC_HYBRID.
				if (Math.abs(par.getArcTime()) > 1e-10) {
					throw new IllegalArgumentException("Invalid autopolyploidic arc time: Must be 0 for arc ending in " + x + ".");
				}
				return AUTOPOLYPLOIDIC_HYBRID;
			}
			
			if (par.getArcTime() <= 1e-10) {
				throw new IllegalArgumentException("Invalid arc time: Must be greater than 0 for arc ending in " + x + ".");
			}
			Iterator<DiscretisedArc> it = out.iterator();
			DiscretisedArc c1 = it.next();
			DiscretisedArc c2 = it.next();
			double t1 = c1.getArcTime();
			double t2 = c2.getArcTime();
			if (t1 > 1e-10 && t2 > 1e-10) {
				// SPECIATION.
				return SPECIATION;
			}
			if ((t1 > 1e-10 && Math.abs(t2) <= 1e-10) || (t2 > 1e-10 && Math.abs(t1) <= 1e-10)) {
				// HYBRID_DONOR.
				return HYBRID_DONOR;
			}
			throw new IllegalArgumentException("Invalid vertex " + x + ": Wrong degree or times of outgoing arcs.");
		}
		
		if (indegree == 2 && outdegree == 1) {
			Iterator<DiscretisedArc> it = in.iterator();
			DiscretisedArc p1 = it.next();
			DiscretisedArc p2 = it.next();
			double t1 = p1.getArcTime();
			double t2 = p2.getArcTime();
			if (Math.abs(t1) <= 1e-10 && Math.abs(t2) <= 1e-10) {
				// ALLOPOLYPLOIDIC_HYBRID.
				return ALLOPOLYPLOIDIC_HYBRID;
			}
			throw new IllegalArgumentException("Invalid outgoing arc times for vertex " + x + ": Must be greater than 0.");
		}
		throw new IllegalArgumentException("Invalid vertex " + x + ": Wrong degree of in- or outgoing arcs.");
	}
	
}
