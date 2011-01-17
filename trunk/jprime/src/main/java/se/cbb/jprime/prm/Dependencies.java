package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.prm.ProbAttribute.DataType;

/**
 * Container to collect all PRM dependencies for a single child attribute.
 * 
 * @author Joel Sjöstrand.
 */
public class Dependencies implements Comparable<Dependencies> {

	/** Probabilistic child attribute in all dependencies. */
	private final ProbAttribute child;
	
	/** Dependencies. */
	private final TreeSet<Dependency> dependencies;
	
	/**
	 * Constructor.
	 * @param child the child common to all dependencies in this collection.
	 */
	public Dependencies(ProbAttribute child) {
		this.child = child;
		this.dependencies = new TreeSet<Dependency>();
	}
	
	/**
	 * Copy-constructor. The created instance will refer to the same
	 * child and Dependency objects, although the latter should be
	 * immutable.
	 * @param deps the object to copy.
	 */
	public Dependencies(Dependencies deps) {
		this.child = deps.child;
		this.dependencies = new TreeSet<Dependency>(deps.dependencies);
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
		this.dependencies.add(dep);
	}
	
	/**
	 * Returns all dependencies.
	 * @return the dependencies in no particular order.
	 */
	public Set<Dependency> getAll() {
		return this.dependencies;
	}
	
	/**
	 * Returns the number of dependencies.
	 * @return the number of dependencies.
	 */
	public int getSize() {
		return this.dependencies.size();
	}
	
	/**
	 * Returns the child of the dependencies.
	 * @return the child of all dependencies.
	 */
	public ProbAttribute getChild() {
		return this.child;
	}
	
	/**
	 * Returns the parents of the contained dependencies (meaning a parent
	 * may occurr multiple times).
	 * Hopefully the order will be the same irrespective of in which
	 * order the dependencies were added.
	 * @return the parents.
	 */
	public List<ProbAttribute> getParents() {
		ArrayList<ProbAttribute> parents = new ArrayList<ProbAttribute>(this.dependencies.size());
		for (Dependency dep : this.dependencies) {
			parents.add(dep.getParent());
		}
		return parents;
	}
	
	/**
	 * Returns true if all contained dependencies are discrete.
	 * @return true if all parents of the child are discrete.
	 */
	public boolean isDiscrete() {
		for (Dependency dep : this.dependencies) {
			if (dep.getParent().getDataType() != DataType.DISCRETE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the names of all contained attributes, separated by line breaks.
	 * @return the names of all contained dependencies.
	 */
	public String getName() {
		StringBuilder sb = new StringBuilder(this.dependencies.size() * 30);
		for (Dependency dep : this.dependencies) {
			sb.append(dep.getName()).append('\n');
		}
		return sb.toString();
	}
	
	@Override
	public int compareTo(Dependencies o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public String toString() {
		return this.getName();
	}
	
}
