package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import se.cbb.jprime.prm.ProbAttribute.DataType;

/**
 * Container to collect all PRM dependencies for a single child attribute.
 * Two instances of this class containing equal (or rather identical) internal dependencies
 * are considered equal. The number of internal dependencies may be 0, i.e. parent-less
 * instances are allowed.
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
	 * Adds a dependency. The dependency's child must be correct.
	 * A duplicate will only overwrite its identical value.
	 * @param dep the dependency.
	 * @return true if added, false if already containing an equivalent dependency.
	 */
	public boolean put(Dependency dep) {
		if (this.child != dep.getChild()) {
			throw new IllegalArgumentException("Cannot add dependency to collection" +
					" due to incorrect child attribute.");
		}
		return this.dependencies.add(dep);
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
	 * may occur multiple times).
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
	 * Returns true if the number of parents is greater than 0.
	 * @return true if there are parents.
	 */
	public boolean hasParents() {
		return (this.dependencies.size() > 0);
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
		return (this.child.getDataType() == DataType.DISCRETE);
	}

	/**
	 * Returns true if either the child or one of its parents are
	 * latent. This can be used e.g. for optimisations when one knows
	 * that no attribute entities are bound to change.
	 * @return true if one or more of the attributes are latent.
	 */
	public boolean isLatent() {
		for (Dependency dep : this.dependencies) {
			if (dep.isLatent()) {
				return true;
			}
		}
		return this.child.isLatent();
	}
	
	/**
	 * Returns the names of all contained attributes, separated by line breaks.
	 * @return the names of all contained dependencies.
	 */
	public String getName() {
		StringBuilder sb = new StringBuilder(this.dependencies.size() * 30);
		sb.append(this.child.getFullName()).append("-dependencies:\n");
		for (Dependency dep : this.dependencies) {
			sb.append('\t').append(dep.getName()).append('\n');
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
	
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		return this.getName().equals(((Dependencies) obj).getName());
	}

	/**
	 * Returns true if this collection contains an equivalent dependency.
	 * @param dep the dependency.
	 * @return true if it contains an equivalent dependency.
	 */
	public boolean contains(Dependency dep) {
		return this.dependencies.contains(dep);
	}
	
}
