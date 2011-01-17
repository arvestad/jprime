package se.cbb.jprime.prm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds a PRM structure, that is, a set of parent-child dependencies.
 * <p/>
 * Note: Invoking <code>equal()</code> on identical structures based of different instances should
 * hopefully yield true.
 * 
 * @author Joel Sjöstrand.
 */
public class Structure {

	/** The skeleton to which this structure refers. */
	private final Skeleton skeleton;
	
	/**
	 * Dependencies of this structure, indexed by child attribute.
	 * There will be a Dependencies object for every attribute, albeit
	 * empty if it does not have parents.
	 */
	private final HashMap<ProbAttribute, Dependencies> dependencies;
	
	/** For quick equivalence comparisons: dependency names. */
	private final HashSet<String> dependencyNames;
	
	/**
	 * For quick access: all attributes which lack parents. No handling of
	 * self-referencing PRM classes yet.
	 */
	private final HashSet<ProbAttribute> sources;
	
	/**
	 * Constructor.
	 * @param skeleton the skeleton this structure refers to.
	 */
	public Structure(Skeleton skeleton) {
		this.skeleton = skeleton;
		int initCap = skeleton.getNoOfPRMClasses() * 8;
		this.dependencies = new HashMap<ProbAttribute, Dependencies>(initCap);
		this.dependencyNames = new HashSet<String>(initCap * 4);
		this.sources = new HashSet<ProbAttribute>(initCap);
		
		// Create an initially empty Dependencies for each attribute.
		// Also add all as potential "sources".
		for (PRMClass c : skeleton.getPRMClasses()) {
			for (ProbAttribute a : c.getProbAttributes()) {
				this.dependencies.put(a, new Dependencies(a));
				this.sources.add(a);
			}
		}
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
		this.dependencyNames = new HashSet<String>(struct.dependencyNames);
		this.sources = new HashSet<ProbAttribute>(struct.sources);
	}
	
	/**
	 * Adds a dependency to this structure. Duplicates will
	 * only overwrite its previous value.
	 * @param dep the dependency.
	 */
	public void putDependency(Dependency dep) {
		Dependencies deps = this.dependencies.get(dep.getChild());
		deps.put(dep);
		this.dependencyNames.add(dep.getName());
		this.sources.remove(dep.getChild());
	}

	/**
	 * Returns the number of dependencies, i.e. parent-child arcs in
	 * the "template graph".
	 * @return the number of dependencies.
	 */
	public int getNoOfDependencies() {
		return this.dependencyNames.size();
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
	 * Returns all dependencies ending with a certain child.
	 * @param child the child attribute.
	 * @return the parent-child dependencies for the child.
	 */
	public Dependencies getDependencies(ProbAttribute child) {
		return this.dependencies.get(child);
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
	 * Verifies if a dependency is contained within the structure.
	 * The input parameter need not refer to the same instance;
	 * any equivalent dependency found will yield true.
	 * @param dep the dependency.
	 * @return true if this structure contains an equivalent dependency.
	 */
	public boolean hasDependency(Dependency dep) {
		return this.dependencyNames.contains(dep.getName());
	}
	
	@Override
	public int hashCode() {
		return (this.skeleton.hashCode() * 31 + this.dependencyNames.hashCode());
	}

	/**
	 * Compares two structures. Will return true for different instances
	 * referring to the same skeleton and having identical dependency names.
	 * @param the structure to compare with.
	 * @return true if equivalent.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		
		Structure s = (Structure) obj;
		return (this.skeleton == s.skeleton &&
			this.dependencyNames.equals(s.dependencyNames));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("PRM structure on skeleton ").append(this.skeleton.getName());
		sb.append(" with dependencies (Child)...(Parent):\n");
		for (String s : this.dependencyNames) {
			sb.append('\t').append(s).append('\n');
		}
		return sb.toString();
	}
	
	
}
