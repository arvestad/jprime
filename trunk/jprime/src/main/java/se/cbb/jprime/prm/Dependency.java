package se.cbb.jprime.prm;

import java.util.List;

/**
 * Represents a parent-child dependency relationship between
 * two probabilistic PRM attributes. The two attributes may
 * belong to the same class, or belong to different classes connected
 * through a 'slot chain' (a chain of PRM relations).
 * 
 * @author Joel Sjöstrand.
 */
public class Dependency {

	/** Child. */
	private ProbabilisticAttribute child;
	
	/** Slot chain from child to parent, null if attributes within same PRM class. */
	private Relation[] slotChain;
	
	/** Parent. */
	private ProbabilisticAttribute parent;
	
	/**
	 * Creates a dependency. Does not perform verification of intrinsic
	 * dependency constraints on attributes nor relations.
	 * @param child child attribute.
	 * @param slotChain slot chain from child to attribute.
	 * @param parent parent attribute.
	 */
	public Dependency(ProbabilisticAttribute child, List<Relation> slotChain,
			ProbabilisticAttribute parent) {
		this.child = child;
		this.slotChain = slotChain.toArray(this.slotChain);
		this.parent = parent;
	}
	
	/**
	 * Follows the slot chain from child to parent, assuming that the
	 * encountered relations will never yield more than one record in return.
	 * @param idx the child attribute entity.
	 * @return the presumably single parent entity.
	 */
	public Object getAttribute(int idx) {
		for (Relation rel : this.slotChain) {
			idx = rel.getIndex(idx);
		}
		return this.parent.getEntityAsObject(idx);
	}
}
