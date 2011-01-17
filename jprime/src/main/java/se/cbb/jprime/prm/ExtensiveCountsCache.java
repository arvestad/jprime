package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Given a PRM structure of only discrete probabilistic attributes, computes counts
 * for all dependencies. Maintains an extensive pool of all encountered dependencies,
 * so that non-latent ones may be retrieved rather than recounted.
 * <p/>
 * Only for use when storage requirements are feasible.
 * 
 * @author Joel Sjöstrand.
 */
public class ExtensiveCountsCache {

	/** If true, performs indexing on all encountered dependencies. */
	private boolean doIndexing;
	
	/** Holds all encountered dependencies, etc. */
	private HashSet<Dependency> dependencyPool;
	
	/** Holds counts for all encountered dependency sets. */
	private HashMap<Dependencies, Counts> countsPool;
	
	/**
	 * Constructor.
	 */
	public ExtensiveCountsCache(boolean doIndexing) {
		this.doIndexing = doIndexing;
		this.dependencyPool = new HashSet<Dependency>(128);
		this.countsPool = new HashMap<Dependencies, Counts>(128);
	}
	
	/**
	 * Produces counts for a discrete valued structure. Counts are either retrieved
	 * as previously encountered, or created (and added to the cache).
	 * @param struct the structure.
	 * @return the counts for the structure.
	 */
	public HashMap<Dependencies, Counts> getCounts(Structure struct) {
		Collection<Dependencies> depsSets = struct.getDependencies();
		HashMap<Dependencies, Counts> counts = new HashMap<Dependencies, Counts>(depsSets.size());
		for (Dependencies deps : depsSets) {
			if (!deps.isDiscrete()) {
				throw new IllegalArgumentException("Cannot cache dependency which is not discrete.");
			}
			boolean encountered = this.countsPool.containsKey(deps);
			
			if (!encountered) {
				// Cache all individual dependencies.
				for (Dependency dep : deps.getAll()) {
					if (!this.dependencyPool.contains(dep)) {
						if (this.doIndexing && !dep.hasIndex()) {
							dep.createIndex();
						}
						this.dependencyPool.add(dep);
					}
				}
			}
			
			Counts c;
			if (!encountered || deps.isLatent()) {
				// Count anew / again.
				c = new Counts(deps);
				this.countsPool.put(deps, c);
			} else {
				// Use existing counts.
				c = this.countsPool.get(deps);
			}
			counts.put(deps, c);
		}
		return counts;
	}
	
	
}
