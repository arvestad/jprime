package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Stores data for a set of <code>Dependencies</code>. In particular, it enables
 * caching of data for invariant dependencies, so that given a new <code>Structure</code>
 * one needs only update values for non-encountered dependencies and those which
 * feature latent variables.
 * <p/>
 * Only for use when storage requirements (i.e. number of encountered dependencies) are feasible.
 * 
 * @author Joel Sjöstrand.
 */
public class DependenciesCache<T> {
	
	/** Values. */
	private HashMap<Dependencies, T> pool;
	
	/**
	 * Constructor.
	 */
	public DependenciesCache() {
		this.pool = new HashMap<Dependencies, T>(128);
	}
	
	/**
	 * Given a structure, retrieves a list of all dependencies which are not in the
	 * cache, and that are in the cache but contains latent attributes.
	 * @param struct the structure.
	 * @param notInCache list to be filled with dependencies of structure not already in cache.
	 * @param latentInCache list to be filled with dependencies of structure already in cache but with latent attributes.
	 */
	public void getNonCached(Structure struct, List<Dependencies> notInCache, List<Dependencies> latentInCache) {
		notInCache.clear();
		latentInCache.clear();
		Collection<Dependencies> depsSets = struct.getDependencies();
		for (Dependencies deps : depsSets) {
			if (!this.pool.containsKey(deps)) {
				notInCache.add(deps);
			} else if (deps.isLatent()) {
				latentInCache.add(deps);
			}
		}
	}
	
	/**
	 * Puts a value into the cache.
	 * @param deps the dependencies.
	 * @param value the value.
	 * @return the previous value; null if none.
	 */
	public T put(Dependencies deps, T value) {
		return this.pool.put(deps, value);
	}
	
	/**
	 * Returns a value from the cache.
	 * @param deps the dependencies.
	 * @return the value.
	 */
	public T get(Dependencies deps) {
		return this.pool.get(deps);
	}
	
	/**
	 * Returns true if this cache has a value for a specific key.
	 * @param deps the key.
	 * @return true if the cache has a value for the key; false otherwise.
	 */
	public boolean containsKey(Dependencies deps) {
		return this.pool.containsKey(deps);
	}
}
