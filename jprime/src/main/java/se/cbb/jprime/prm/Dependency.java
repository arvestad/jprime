package se.cbb.jprime.prm;

import java.util.List;

import se.cbb.jprime.prm.Relation.Type;

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
 * </ol>
 * Note that this implies that we may have more than one arc for the same P and C,
 * albeit with different slot chains.
 * <p/>
 * For many-to-one and one-to-one relations, we allow caching (creating an "index")
 * of entity indices from C to P, enabling quicker lookups.
 * <p/>
 * Two instances of this class are considered equal if they refer to the same
 * child-parents and have identical slot chains.
 * 
 * @author Joel Sjöstrand.
 */
public final class Dependency implements Comparable<Dependency> {

	/** Child. */
	private final ProbAttribute child;
	
	/** Slot chain from child to parent, possibly empty. */
	private final Relation[] slotChain;
	
	/** Parent. */
	private final ProbAttribute parent;
	
	/** String representation, stored for quick access. */
	private final String name;
	
	/**
	 * Cache relating an entity of the child C to its parent P.
	 * Only applicable for many-to-one and one-to-one relations,
	 * and when caching has been enabled.
	 */
	private int[] index = null;
	
	/**
	 * Creates a dependency. Does not perform verification of slot chain validity,
	 * dependency constraints, etc.
	 * @param child child attribute.
	 * @param slotChain slot chain from child to attribute. Null is interpreted as empty.
	 * @param parent parent attribute.
	 */
	public Dependency(ProbAttribute child, List<Relation> slotChain,
			ProbAttribute parent) {
		this.child = child;
		if (slotChain == null) {
			this.slotChain = new Relation[0];
		} else {
			this.slotChain = new Relation[slotChain.size()];
			slotChain.toArray(this.slotChain);
		}
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
	 * Follows an entity from child to parent, assuming that the
	 * encountered relations will never yield more than one record in return.
	 * @return the presumably single parent entity.
	 */
	public int getSingleParentEntity(int idx) {
		if (this.hasIndex()) {
			return this.index[idx];
		}
		for (Relation rel : this.slotChain) {
			idx = rel.getIndex(idx);
		}
		return idx;
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
	 * Returns true if this dependency makes use of a slot chain,
	 * i.e. there are relations between parent and child attributes.
	 * @return true if there is a slot chain.
	 */
	public boolean hasSlotChain() {
		return (this.slotChain.length > 0);
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
	
	/**
	 * Creates a lookup table relating entities of the child C to its
	 * parent P for quicker access. If unable to do so (e.g. by a one-to-many
	 * relation in the slot chain), false is returned.
	 * See e.g. <code>getSingleParentEntityIndexed()</code>.
	 * @return true if index was created, false if index could not be created.
	 */
	public boolean createIndex() {
		for (Relation rel : this.slotChain) {
			if (rel.getType() != Type.MANY_TO_ONE && rel.getType() != Type.ONE_TO_ONE) {
				return false;
			}
		}
		this.index = new int[this.child.getNoOfEntities()];
		for (int i = 0; i < this.index.length; ++i) {
			int idx = i;
			for (Relation rel : this.slotChain) {
				idx = rel.getIndex(idx);
			}
			this.index[i] = idx;
		}
		return true;
	}
	
	/**
	 * Returns true if caching has been enabled.
	 * @return
	 */
	public boolean hasIndex() {
		return (this.index != null);
	}

	/**
	 * Returns true if the child attribute or the parent attribute
	 * is latent.
	 * @return true if any of the attributes latent.
	 */
	public boolean isLatent() {
		return (this.child.isLatent() || this.parent.isLatent());
	}
	
	@Override
	public int compareTo(Dependency o) {
		return this.name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return this.name;
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
		return this.name.equals(((Dependency) obj).getName());
	}
	
}
