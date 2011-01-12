package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;

/**
 * Container to collect all PRM dependencies for a single child attribute.
 * 
 * @author Joel Sjöstrand.
 */
public class Dependencies {

	/** Probabilistic child attribute in all dependencies. */
	private ProbabilisticAttribute child;
	
	/** Dependencies hashed by name. Should ensure no duplicates exist. */
	private HashMap<String, Dependency> dependencies;
	
	/**
	 * Constructor.
	 * @param child the child common to all dependencies in this collection.
	 */
	public Dependencies(ProbabilisticAttribute child) {
		this.child = child;
		this.dependencies = new HashMap<String, Dependency>(8);
	}
	
	/**
	 * Adds a dependency. The dependency's child must correct.
	 * A duplicate will only overwrite its identical value.
	 * @param dep the dependency.
	 */
	public void put(Dependency dep) {
		if (this.child != dep.getChild()) {
			throw new IllegalArgumentException("Cannot add dependency to collection" +
					" due to incorrect child attribute.");
		}
		this.dependencies.put(dep.getName(), dep);
	}
	
	/**
	 * Returns all dependencies.
	 * @return the dependencies in no particular order.
	 */
	public Collection<Dependency> getDependencies() {
		return this.dependencies.values();
	}
	
}
