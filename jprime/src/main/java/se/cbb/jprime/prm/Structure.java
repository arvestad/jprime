package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds a PRM structure, that is, a set of parent-child dependencies.
 * Two instances of this class are considered equal if they contain
 * equivalent dependencies.
 * 
 * @author Joel Sjöstrand.
 */
public class Structure implements Comparable<Structure> {

	/** The skeleton to which this structure refers. */
	private final Skeleton skeleton;
	
	/**
	 * Dependencies of this structure, indexed by child attribute.
	 * Attributes which lack dependencies also has a Dependencies object, albeit empty.
	 */
	private HashMap<ProbAttribute, Dependencies> dependencies;
	
	/** Number of individual dependencies. */
	private int noOfDependencies;
	
	/** For quicker equivalence comparisons: name as concatenated dependency names. */
	private String name;
	
	/**
	 * For quick access: all attributes which lack parents. No handling of
	 * self-referencing PRM classes yet.
	 */
	private TreeSet<ProbAttribute> sources;
	
	/**
	 * For quick access: all attributes which lack children. No handling of
	 * self-referencing PRM classes yet.
	 */
	private TreeSet<ProbAttribute> sinks;
	
	/**
	 * Constructor.
	 * @param skeleton the skeleton this structure refers to.
	 */
	public Structure(Skeleton skeleton) {
		this.skeleton = skeleton;
		int initCap = skeleton.getNoOfPRMClasses() * 8;
		this.dependencies = new HashMap<ProbAttribute, Dependencies>(initCap);
		this.noOfDependencies = 0;
		this.sources = new TreeSet<ProbAttribute>();
		this.sinks = new TreeSet<ProbAttribute>();
		
		// Create containers for all children.
		// Initially, every attribute is a "source" and "sink".
		for (PRMClass c : skeleton.getPRMClasses()) {
			for (ProbAttribute a : c.getProbAttributes()) {
				this.dependencies.put(a, new Dependencies(a));
				this.sources.add(a);
				this.sinks.add(a);
			}
		}
		this.updateName();
	}
	
	/**
	 * Copy-constructor. The generated object will refer to the same
	 * skeleton, but have copied instances of Dependencies (which, in turn,
	 * will refer to the same immutable Dependency instances).
	 * @param struct the object to copy.
	 */
	public Structure(Structure struct) {
		this.skeleton = struct.skeleton;
		this.dependencies = new HashMap<ProbAttribute, Dependencies>(struct.dependencies.size());
		for (Dependencies deps : struct.dependencies.values()) {
			this.dependencies.put(deps.getChild(), new Dependencies(deps));
		}
		this.noOfDependencies = struct.noOfDependencies;
		this.name = struct.name;
		this.sources = new TreeSet<ProbAttribute>(struct.sources);
		this.sinks = new TreeSet<ProbAttribute>(struct.sinks);
	}
	
	/**
	 * Adds a dependency to this structure. Duplicates will
	 * only overwrite its previous value.
	 * @param dep a (non-empty) dependency.
	 */
	public void putDependency(Dependency dep) {
		ProbAttribute child = dep.getChild();
		if (this.dependencies.get(child).put(dep)) {
			++this.noOfDependencies;
			this.sources.remove(child);
			this.sinks.remove(dep.getParent());
			this.updateName();
		}
	}
	
	/**
	 * Updates the name of this structure, which is more or less a
	 * concatenation of internal dependency names.
	 */
	private void updateName() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("PRM structure on skeleton ").append(this.skeleton.getName());
		sb.append(" with dependencies (Child)...(Parent):\n");
		for (Dependencies deps : this.dependencies.values()) {
			for (Dependency dep : deps.getAll()) {
				sb.append('\t').append(dep).append('\n');
			}
		}
		this.name = sb.toString();
	}

	/**
	 * Returns the number of dependencies, i.e. parent-child arcs in
	 * the "template graph".
	 * @return the number of dependencies.
	 */
	public int getNoOfDependencies() {
		return this.noOfDependencies;
	}
	
	/**
	 * Returns the number of dependencies for a certain child.
	 * @param child the child.
	 * @return the number of dependencies.
	 */
	public int getNoOfDependencies(ProbAttribute child) {
		return this.dependencies.get(child).getSize();
	}
	
	/**
	 * Returns all dependencies for a certain child.
	 * Even if it has none, an empty collection is returned.
	 * @param child the child attribute.
	 * @return the parent-child dependencies for the child.
	 */
	public Dependencies getDependencies(ProbAttribute child) {
		return this.dependencies.get(child);
	}
	
	/**
	 * Returns all dependencies, collected by common child.
	 * There will be a collection also for attributes which
	 * are not children in any dependencies.
	 * @return for every probabilistic attribute, the dependencies
	 *         in which the attribute is a child (possibly 0).
	 */
	public Collection<Dependencies> getDependencies() {
		return this.dependencies.values();
	}
	
	/**
	 * Returns all attributes which are not children in any dependency.
	 * Handling of self-referencing attributes is not implemented currently. 
	 * @return all attributes which are not children.
	 */
	public Set<ProbAttribute> getSources() {
		return this.sources;
	}
	
	/**
	 * Verifies if an equivalent dependency is contained within the structure.
	 * @param dep the dependency.
	 * @return true if this structure contains an equivalent dependency.
	 */
	public boolean hasDependency(Dependency dep) {
		return this.dependencies.get(dep.getChild()).contains(dep);
	}

	/**
	 * Returns true if the structure already contains the dependency,
	 * or if it contains a dependency with the same parent and child.
	 * @param dep the dependency.
	 * @return true if an equal or parallel dependency already contained within; otherwise false.
	 */
	public boolean hasParallelDependency(Dependency dep) {
		Dependencies deps = this.dependencies.get(dep.getChild());
		for (Dependency d : deps.getAll()) {
			if (d.getParent().equals(dep.getParent())) { return true; }
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int compareTo(Structure o) {
		return this.name.compareTo(o.name);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		return this.name.equals(((Structure) obj).name);
	}
	
	
}
