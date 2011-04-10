package se.cbb.jprime.prm;

import java.util.ArrayList;
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
	 * Given a Structure, retrieves a list of all dependencies which are not in the
	 * cache and, if desired, may be in the cache but concern latent attributes.
	 * @param struct the structure.
	 * @param includeLatent true to also return all latent dependencies; false to ignore latent
	 *        dependencies already in cache.
	 * @return the dependencies not in the cache.
	 */
	public List<Dependencies> update(Structure struct, boolean includeLatent) {
		ArrayList<Dependencies> l = new ArrayList<Dependencies>();
		Collection<Dependencies> depsSets = struct.getDependencies();
		for (Dependencies deps : depsSets) {
			boolean encountered = this.pool.containsKey(deps);
			if (!encountered) {
				l.add(deps);
			} else if (includeLatent && deps.isLatent()) {
				l.add(deps);
			}
		}
		return l;
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
}
