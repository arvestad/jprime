package se.cbb.jprime.topology;

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
	 * @param x the vertex ID.
	 * @param parents the parents' IDs
	 * @param children the children's IDs.
	 * @param vertexTimes the absolute vertex times.
	 * @return the vertex type.
	 */
	public static HybridVertexType getVertexType(int x, int[] parents, int[] children, DoubleMap vertexTimes) {
		int indegree = (parents == null ? 0 : parents.length);
		int outdegree = (children == null ? 0 : children.length);
		
		if (indegree == 0) {
			// STEM_TIP.
			if (outdegree != 1) {
				throw new IllegalArgumentException("Invalid stem tip vertex: must have exactly one child.");
			}
			return STEM_TIP;
		}
		
		if (indegree == 1) {
			if (outdegree == 0) {
				// LEAF.
				if (vertexTimes.get(parents[0]) - vertexTimes.get(x) <= 1e-10) {
					throw new IllegalArgumentException("Invalid arc time: Must be greater than 0 for arc " + parents[0] + "->" + x + ".");
				}
				return LEAF;
			}
			
			if (outdegree == 1) {
				// AUTOPOLYPLOIDIC_HYBRID.
				if (Math.abs(vertexTimes.get(parents[0]) - vertexTimes.get(x)) > 1e-10) {
					throw new IllegalArgumentException("Invalid autopolyploidic arc time: Must be 0 for arc " + parents[0] + "->" + x + ".");
				}
				return AUTOPOLYPLOIDIC_HYBRID;
			}
			
			if (vertexTimes.get(parents[0]) - vertexTimes.get(x) <= 1e-10) {
				throw new IllegalArgumentException("Invalid arc time: Must be greater than 0 for arc " + parents[0] + "->" + x + ".");
			}
			double t1 = vertexTimes.get(x) - vertexTimes.get(children[0]);
			double t2 = vertexTimes.get(x) - vertexTimes.get(children[1]);
			if (t1 > 1e-10 && t2 > 1e-10) {
				return SPECIATION;
			}
			if ((t1 > 1e-10 && Math.abs(t2) <= 1e-10) || (t2 > 1e-10 && Math.abs(t1) <= 1e-10)) {
				return HYBRID_DONOR;
			}
			throw new IllegalArgumentException("Invalid vertex " + x + ": Wrong degree or times of outgoing arcs.");
		}
		
		if (indegree == 2 && outdegree == 1) {
			double t1 = vertexTimes.get(parents[0]) - vertexTimes.get(x);
			double t2 = vertexTimes.get(parents[1]) - vertexTimes.get(x);
			if (Math.abs(t1) <= 1e-10 && Math.abs(t2) <= 1e-10) {
				return ALLOPOLYPLOIDIC_HYBRID;
			}
			throw new IllegalArgumentException("Invalid outgoing arc times for vertex " + x + ": Must be greater than 0.");
		}
		throw new IllegalArgumentException("Invalid vertex " + x + ": Wrong degree of in- or outgoing arcs.");
	}
	
}
