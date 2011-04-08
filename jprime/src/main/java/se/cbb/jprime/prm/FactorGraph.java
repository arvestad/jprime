package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a bipartite factor graph derived from a Bayesian network induced
 * by the entities of a <code>Structure</code>.
 * May consist of several disjoint components.
 *  
 * @author Joel Sjöstrand.
 */
public class FactorGraph {

	/** Common interface for both kinds of vertices. */
	interface FGVertex {
		
	}
	
	/** For probabilistic attribute entities. */
	class VariableVertex implements FGVertex {
		
	}
	
	/** For factor vertices. */
	class FactorVertex {
		
	}
	
	/** Vertices. */
	private HashMap<String, FGVertex> vertices;
	
	/**
	 * Creates a factor graph from a structure and its underlying completed skeleton.
	 * @param struct the structure.
	 */
	public FactorGraph(Structure struct) {
		// Retrieve all latent attributes.
		ArrayList<ProbAttribute> latAtts = new ArrayList<ProbAttribute>(8);
		int sz = 0;
		for (PRMClass c : struct.getSkeleton().getPRMClasses()) {
			for (ProbAttribute a : c.getProbAttributes()) {
				if (a.isLatent()) {
					latAtts.add(a);
					sz += 2 * a.getNoOfEntities();
					Dependencies deps = struct.getDependencies(a);
					for (Dependency d : deps.getAll()) {
						ProbAttribute pa = d.getParent();
						if (!pa.isLatent()) {
							sz += 2 * pa.getNoOfEntities();
						}
					}
				}
			}
		}
		
		this.vertices = new HashMap<String, FactorGraph.FGVertex>(sz);
		
		/* Algorithm:
		 * 
		 * V := empty set.
		 * E := empty set.
		 * G := G(V,E).
		 * for each class c do:
		 *   for each probabilistic attribute a do:
		 *     if a is latent do:
		 *       P := parent probabilistic attributes of a.
		 *       for each entity a_e of a do:
		 *         if V does not contain a_e do:
		 *           V := V union a_e.
		 *         for each p in P do:
		 *           a_p = parent of a_e in p.
		 *           if V does not contain a_p do:
		 *             V := V union a_p.
		 *           E := E union (a_p,a_e).
		 *           
		 * 
		 */
	}
	
}
