package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import se.cbb.jprime.prm.ProbAttribute.DependencyConstraints;

/**
 * Collects various methods for creating random PRM structures.
 * 
 * @author Joel Sjöstrand.
 */
public class RandomStructureGenerator {

	/** Thrown when dependencies cannot be generated. */
	static class DependencyGenerationException extends Exception {

		private static final long serialVersionUID = 1L;
		
		public DependencyGenerationException(String errMsg) {
			super(errMsg);
		}
	}
	
	/**
	 * 
	 * @param rng
	 * @param skeleton
	 * @param n
	 * @param maxTries
	 * @param maxSlots
	 * @return
	 */
	public static Structure createRandomStructure(Random rng, Skeleton skeleton, int n, int maxTries, int maxSlots) {
		Structure struct = new Structure(skeleton);
		int tries = 0;
		do {
			try {
				// Select a random PRMClass.
				PRMClass c = skeleton.getRandomPRMClass(rng);
				
				// Select a child for the current dependency.
				List<ProbAttribute> atts = getValidChildAttributes(c.getProbAttributes());
				if (atts.size() == 0) {
					throw new DependencyGenerationException("No valid child attributes in " +
							"PRM class " + c.getName() + ".");
				}
				ProbAttribute child = atts.get(rng.nextInt(atts.size()));
				
				// Find a parent, possibly with a slot chain.
				ArrayList<Relation> slotChain = new ArrayList<Relation>(4);
				ProbAttribute parent = getRandomParentAttribute(rng, c, slotChain, maxSlots);
				if (parent == child && slotChain.size() == 0) {
					throw new DependencyGenerationException("Cannot add dependency between attribute " +
							parent.getFullName() + " and itself without any slots in between.");
				}
				
				// Add dependency. If it's already present, it should only overwrite the old one.
				Dependency dep = new Dependency(child, slotChain, parent);
				struct.addDependency(dep);
				
			} catch (DependencyGenerationException ex) {
				System.out.println(ex.getMessage());
			}
			++tries;
		} while (struct.getNoOfDependencies() < n && tries < maxTries);
		return struct;
	}
	
	/**
	 * Randomly chooses a parent probabilistic attribute for a dependency.
	 * Selection starts at a given PRM class, and the attribute is chosen either
	 * directly from the class or recursively by following a relation to another class.
	 * @param rng random number generator.
	 * @param c starting class.
	 * @param slotChain current slot chain.
	 * @param maxSlots max allowed length of slot chain.
	 * @return the chosen parent attribute.
	 * @throws DependencyGenerationException if failed to find a parent.
	 */
	private static ProbAttribute getRandomParentAttribute(Random rng, PRMClass c, List<Relation> slotChain,
			int maxSlots) throws DependencyGenerationException {
		List<ProbAttribute> atts = getValidParentAttributes(c.getProbAttributes());
		List<Relation> rels = getValidSlotRelations(c.getRelations());
		int noOfAtts = atts.size();
		int noOfRels = rels.size();
		int tot = noOfAtts + noOfRels;
		if (tot == 0) {
			throw new DependencyGenerationException("No valid parent attributes or relations" +
					" in PRM class " + c.getName() + ".");
		}
		
		// Uniformly select either an attribute or relation.
		int idx = rng.nextInt(tot);
		if (idx < noOfAtts) {
			// Attribute was chosen.
			return atts.get(idx);
		}
		
		// Relation was chosen.
		if (slotChain.size() >= maxSlots) {
			throw new DependencyGenerationException("Exceeded max slot chain length.");
		}
		Relation r = rels.get(idx - noOfAtts);
		slotChain.add(r);
		return getRandomParentAttribute(rng, r.getSecond().getPRMClass(), slotChain, maxSlots);
	}
	
	/**
	 * Filters a collection of probabilistic attributes for those that may
	 * function as a child in a dependency.
	 * @param atts the attributes.
	 * @return a list of valid child attributes.
	 */
	private static List<ProbAttribute> getValidChildAttributes(Collection<ProbAttribute> atts) {
		ArrayList<ProbAttribute> al = new ArrayList<ProbAttribute>();
		for (ProbAttribute a : atts) {
			ProbAttribute.DependencyConstraints cons = a.getDependencyConstraints();
			if (cons == DependencyConstraints.NONE || cons == DependencyConstraints.CHILD_ONLY) {
				al.add(a);
			}
		}
		return al;
	}
	
	/**
	 * Filters a collection of probabilistic attributes for those that may
	 * function as a parent in a dependency.
	 * @param atts the attributes.
	 * @return a list of valid parent attributes.
	 */
	private static List<ProbAttribute> getValidParentAttributes(Collection<ProbAttribute> atts) {
		ArrayList<ProbAttribute> al = new ArrayList<ProbAttribute>();
		for (ProbAttribute a : atts) {
			ProbAttribute.DependencyConstraints cons = a.getDependencyConstraints();
			if (cons == DependencyConstraints.NONE || cons == DependencyConstraints.PARENT_ONLY) {
				al.add(a);
			}
		}
		return al;
	}
	
	/**
	 * Filters a collection of relations for those that may
	 * function as slots in a slot chain of a dependency.
	 * @param rels the relations.
	 * @return a list of valid slot relations.
	 */
	private static List<Relation> getValidSlotRelations(Collection<Relation> rels) {
		ArrayList<Relation> al = new ArrayList<Relation>();
		for (Relation r : rels) {
			if (r.canBeSlot()) {
				al.add(r);
			}
		}
		return al;
	}
}
