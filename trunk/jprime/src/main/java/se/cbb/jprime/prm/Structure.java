package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds a PRM structure, that is, a set of parent-child dependencies.
 * 
 * @author Joel Sjöstrand.
 */
public class Structure implements Comparable<Structure> {

	/** The skeleton to which this structure refers. */
	private final Skeleton skeleton;
	
	/**
	 * Dependencies of this structure, indexed by child attribute.
	 * There will be a Dependencies object for every attribute, albeit
	 * empty if it does not have parents.
	 */
	private final HashMap<ProbAttribute, Dependencies> dependencies;
	
	/** Number of explicit dependencies. */
	private int noOfDependencies;
	
	/** For quicker equivalence comparisons: name as concatenated dependency names. */
	private String name;
	
	/**
	 * For quick access: all attributes which lack parents. No handling of
	 * self-referencing PRM classes yet.
	 */
	private final TreeSet<ProbAttribute> sources;
	
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
		
		// Create an initially empty Dependencies for each attribute.
		// Also add all as potential "sources".
		for (PRMClass c : skeleton.getPRMClasses()) {
			for (ProbAttribute a : c.getProbAttributes()) {
				this.dependencies.put(a, new Dependencies(a));
				this.sources.add(a);
			}
		}
		this.updateName();
	}
	
	/**
	 * Copy-constructor. The generated object will refer to the same
	 * skeleton, but have identical copied instances of Dependencies (which, in turn,
	 * will refer to the same immutable Dependency objects).
	 * @param struct the object to copy.
	 */
	public Structure(Structure struct) {
		this.skeleton = struct.skeleton;
		this.dependencies = new HashMap<ProbAttribute, Dependencies>(struct.dependencies.size());
		for (Dependencies deps : struct.dependencies.values()) {
			this.dependencies.put(deps.getChild(), new Dependencies(deps));
		}
		this.name = struct.name;
		this.sources = new TreeSet<ProbAttribute>(struct.sources);
	}
	
	/**
	 * Adds a dependency to this structure. Duplicates will
	 * only overwrite its previous value.
	 * @param dep a (non-empty) dependency.
	 */
	public void putDependency(Dependency dep) {
		Dependencies deps = this.dependencies.get(dep.getChild());
		deps.put(dep);
		++this.noOfDependencies;
		this.sources.remove(dep.getChild());
		this.updateName();
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
	 * @param child the child attribute.
	 * @return the parent-child dependencies for the child.
	 */
	public Dependencies getDependencies(ProbAttribute child) {
		return this.dependencies.get(child);
	}
	
	/**
	 * Returns all dependencies, collected by common child.
	 * @return all dependencies of this structure.
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

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int compareTo(Structure o) {
		return this.name.compareTo(o.name);
	}
	
	
}
