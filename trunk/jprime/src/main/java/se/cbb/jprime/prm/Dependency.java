package se.cbb.jprime.prm;

import java.util.List;

/**
 * Represents a parent-child PRM dependency relationship between
 * two probabilistic PRM attributes, possibly linked via a 'slot chain'
 * (an ordered list of relations).
 * <p/>
 * These scenarios can occur:
 * for parent attribute P and child attribute C:
 * <ol>
 * <li>P and C belong to the same PRM class with an empty slot chain, i.e.,
 *     the same entity.</li>
 * <li>P and C belong to the same PRM class but are linked via a slot chain,
 *     i.e., they may be in different entities.</li>
 * <li>P and C belong to different PRM classes linked via a slot chain.</li>
 * </ol>.
 * 
 * @author Joel Sjöstrand.
 */
public class Dependency {

	/** Child. */
	private ProbabilisticAttribute child;
	
	/** Slot chain from child to parent, possibly empty. */
	private Relation[] slotChain;
	
	/** Parent. */
	private ProbabilisticAttribute parent;
	
	/**
	 * Creates a dependency. Does not perform verification of slot chain validity,
	 * dependency constraints, etc.
	 * @param child child attribute.
	 * @param slotChain slot chain from child to attribute. May be empty, but not null.
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
	public Object getParentEntity(int idx) {
		for (Relation rel : this.slotChain) {
			idx = rel.getIndex(idx);
		}
		return this.parent.getEntityAsObject(idx);
	}
	
	/**
	 * Returns the length of the slot chain of this
	 * dependency, i.e., 0 if the attributes belong to the same
	 * PRM class without relations in between.
	 * @return the length.
	 */
	public int getLength() {
		return this.slotChain.length;
	}

	/**
	 * Returns the dependency's name thus:
	 * A1.Alpha.A2&lt;-B1.Beta.B2&lt;-G1.Gamma.G2 for a child-parent
	 * dependency over classes Alpha, Beta and Gamma,
	 * with the child attribute A1 in Alpha and parent attribute
	 * G2 in Gamma. With an empty slot chain, this reduces to
	 * A1.Alpha.A2.
	 * <p/>
	 * This string may also be used to identify
	 * identical dependencies.
	 * @return a unique string representation.
	 */
	public String getName() {
		StringBuilder sb = new StringBuilder(20 + 20 * this.slotChain.length);
		sb.append(this.child.getName()).append('.');
		for (Relation r : this.slotChain) {
			sb.append(r.getFirst().getFullName()).append("<-")
				.append(r.getSecond().getName()).append('.');
		}
		sb.append(this.parent.getPRMClass().getName()).append('.')
			.append(this.parent.getName());
		return sb.toString();
	}

	/**
	 * Returns the child attribute.
	 * @return the child.
	 */
	public ProbabilisticAttribute getChild() {
		return this.child;
	}
	
	/**
	 * Returns the parent attribute.
	 * @return the parent.
	 */
	public ProbabilisticAttribute getParent() {
		return this.parent;
	}
	
}
