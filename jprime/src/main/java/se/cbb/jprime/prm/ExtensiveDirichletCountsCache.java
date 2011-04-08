package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Given a PRM structure of only discrete probabilistic attributes, retrieves Dirichlet counts
 * for all dependencies. Maintains an extensive pool of all encountered dependencies,
 * so that non-latent ones may be retrieved rather than recounted.
 * <p/>
 * Only for use when storage requirements are feasible.
 * 
 * @author Joel Sjöstrand.
 */
public class ExtensiveDirichletCountsCache {

	/** If true, performs indexing on all encountered dependencies. */
	private boolean doIndexing;
	
	/** Dirichlet parameter used for all parameters. */
	private double dirichletParam;
	
	/** Holds all encountered dependencies, etc. */
	private HashSet<Dependency> dependencyPool;
	
	/** Holds counts for all encountered dependency sets. */
	private HashMap<Dependencies, DirichletCounts> countsPool;
	
	/**
	 * Constructor.
	 * @param doIndexing create index for all cached dependencies.
	 * @param dirichletParam Dirichlet "pseudo-count" parameter used on all
	 *        dependencies.
	 */
	public ExtensiveDirichletCountsCache(boolean doIndexing, double dirichletParam) {
		this.doIndexing = doIndexing;
		this.dirichletParam = dirichletParam;
		this.dependencyPool = new HashSet<Dependency>(128);
		this.countsPool = new HashMap<Dependencies, DirichletCounts>(128);
	}
	
	/**
	 * Produces counts for a discrete valued structure. Counts are either retrieved
	 * as previously encountered, or created (and added to the cache).
	 * @param struct the structure.
	 * @return the counts for the structure.
	 */
	public HashMap<Dependencies, DirichletCounts> getCounts(Structure struct) {
		Collection<Dependencies> depsSets = struct.getDependencies();
		HashMap<Dependencies, DirichletCounts> counts = new HashMap<Dependencies, DirichletCounts>(depsSets.size());
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
			
			DirichletCounts c;
			if (!encountered || deps.isLatent()) {
				// Count anew / again.
				c = new DirichletCounts(deps, this.dirichletParam);
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
