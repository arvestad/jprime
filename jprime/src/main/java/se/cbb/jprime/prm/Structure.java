package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Holds a PRM structure, that is, a set of parent-child dependencies.
 * 
 * @author Joel Sjöstrand.
 */
public class Structure {

	/** All probabilistic attributes of the skeleton. */
	private ArrayList<ProbabilisticAttribute> probAttributes;
	
	/** Full names of all probabilistic attributes of the skeleton. */
	private HashSet<String> probAttributeNames;
	
	/** Dependencies of this structure, indexed by child attribute. */
	private HashMap<ProbabilisticAttribute, Dependencies> dependencies;
	
	/** Names of all dependencies of this structure. */
	private HashSet<String> dependencyNames;
	
	/**
	 * Constructor.
	 * @param skeleton the skeleton this structure refers to.
	 */
	public Structure(Skeleton skeleton) {
		int initCap = skeleton.getNoOfPRMClasses() * 8;
		this.probAttributes = new ArrayList<ProbabilisticAttribute>(initCap);
		this.probAttributeNames = new HashSet<String>(initCap);
		this.dependencies = new HashMap<ProbabilisticAttribute, Dependencies>(initCap);
		this.dependencyNames = new HashSet<String>(initCap * 4);
		
		// Read all probabilistic attributes.
		for (PRMClass c : skeleton.getPRMClasses()) {
			for (ProbabilisticAttribute a : c.getProbAttributes()) {
				this.probAttributes.add(a);
				this.probAttributeNames.add(a.getFullName());
			}
		}
	}
	
	/**
	 * Adds a dependency to this structure. Duplicates will
	 * only overwrite its previous value.
	 * @param dep the dependency.
	 */
	public void addDependency(Dependency dep) {
		ProbabilisticAttribute ch = dep.getChild();
		Dependencies deps = this.dependencies.get(ch);
		if (deps == null) {
			deps = new Dependencies(ch);
			this.dependencies.put(ch, deps);
		}
		deps.put(dep);
		this.dependencyNames.add(dep.getName());
	}
}
