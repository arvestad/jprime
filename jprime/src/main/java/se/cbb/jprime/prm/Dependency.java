package se.cbb.jprime.prm;

import java.util.List;

/**
 * Represents a parent-child PRM dependency relationship between
 * two probabilistic PRM attributes, possibly linked via a 'slot chain'
 * (an ordered list of relations). Instances are supposed to be immutable.
 * <p/>
 * A dependency represents a parent-child arc in the "template graph".
 * These scenarios can occur for a parent attribute P and a child attribute C:
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
public final class Dependency {

	/** Child. */
	private final ProbAttribute child;
	
	/** Slot chain from child to parent, possibly empty. */
	private final Relation[] slotChain;
	
	/** Parent. */
	private final ProbAttribute parent;
	
	/** String representation, stored for quick access. */
	private final String name;
	
	/**
	 * Creates a dependency. Does not perform verification of slot chain validity,
	 * dependency constraints, etc.
	 * @param child child attribute.
	 * @param slotChain slot chain from child to attribute. May be empty, but not null.
	 * @param parent parent attribute.
	 */
	public Dependency(ProbAttribute child, List<Relation> slotChain,
			ProbAttribute parent) {
		this.child = child;
		this.slotChain = new Relation[slotChain.size()];
		slotChain.toArray(this.slotChain);
		this.parent = parent;
		
		// Create name.
		StringBuilder sb = new StringBuilder(30 + 30 * this.slotChain.length);
		sb.append(this.child.getName()).append('.');
		for (Relation r : this.slotChain) {
			sb.append(r.getFirst().getFullName()).append("-")
				.append(r.getSecond().getName()).append('.');
		}
		sb.append(this.parent.getPRMClass().getName()).append('.')
			.append(this.parent.getName());
		this.name = sb.toString();
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
		return this.name;
	}

	/**
	 * Returns the child attribute.
	 * @return the child.
	 */
	public ProbAttribute getChild() {
		return this.child;
	}
	
	/**
	 * Returns the parent attribute.
	 * @return the parent.
	 */
	public ProbAttribute getParent() {
		return this.parent;
	}
	
}
