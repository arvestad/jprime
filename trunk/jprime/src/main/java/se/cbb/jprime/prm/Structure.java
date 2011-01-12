package se.cbb.jprime.prm;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Holds a PRM structure, that is, a set of parent-child dependencies.
 * <p/>
 * Note: Invoking <code>equal()</code> on identical structures based of different instances should
 * (hopefully) yield true.
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
	private final HashMap<ProbabilisticAttribute, Dependencies> dependencies;
	
	/** For quick equivalence comparisons: dependency names. */
	private final HashSet<String> dependencyNames;
	
	/**
	 * Constructor.
	 * @param skeleton the skeleton this structure refers to.
	 */
	public Structure(Skeleton skeleton) {
		this.skeleton = skeleton;
		int initCap = skeleton.getNoOfPRMClasses() * 8;
		this.dependencies = new HashMap<ProbabilisticAttribute, Dependencies>(initCap);
		this.dependencyNames = new HashSet<String>(initCap * 4);
		
		// Create an initially empty Dependencies for each attribute.
		for (PRMClass c : skeleton.getPRMClasses()) {
			for (ProbabilisticAttribute a : c.getProbAttributes()) {
				this.dependencies.put(a, new Dependencies(a));
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
		this.dependencies = new HashMap<ProbabilisticAttribute, Dependencies>(struct.dependencies.size());
		for (Dependencies deps : struct.dependencies.values()) {
			this.dependencies.put(deps.getChild(), new Dependencies(deps));
		}
		this.dependencyNames = new HashSet<String>(struct.dependencyNames);
	}
	
	/**
	 * Adds a dependency to this structure. Duplicates will
	 * only overwrite its previous value.
	 * @param dep the dependency.
	 */
	public void addDependency(Dependency dep) {
		Dependencies deps = this.dependencies.get(dep.getChild());
		deps.putDependency(dep);
		this.dependencyNames.add(dep.getName());
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
	
	
}
