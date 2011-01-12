package se.cbb.jprime.prm;

/**
 * Represents a relation between two PRM classes (analogous to a link between two relational
 * database tables). The relation is specified by the two fixed attributes A and B
 * representing the link.
 * <p/>
 * The order of A and B matters, since the directionality in a dependency slot chain from a child
 * attribute to a parent is assumed to flow from A to B.
 * <p/>
 * At the moment
 * we allow only one-to-one relations and many-to-one relations between A and B respectively,
 * (implying that A and B represent foreign and primary keys respectively). Moreover,
 * it is assumed that the values of B are unique. Many-to-many relations should be resolved
 * using an intermediary PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public class Relation {

	/** Relation type from A to B. */
	public enum Type {
		ONE_TO_ONE,
		MANY_TO_ONE
	}
	
	/** Fixed attribute of class A. */
	private FixedAttribute a;
	
	/** Fixed attribute of class B. */
	private FixedAttribute b;
	
	/** Relation type. */
	private Type type;
	
	/** Governs if this relation can participate in slot chain. */
	private boolean canBeSlot;
	
	/**
	 * Constructor.
	 * @param the fixed attribute of class A.
	 * @param the fixed attribute of class B.
	 * @param type relation type from A to B.
	 * @param canBeSlot flag governing whether this relation can be part of a slot chain.
	 */
	public Relation(FixedAttribute a, FixedAttribute b, Type type, boolean canBeSlot) {
		this.a = a;
		this.b = b;
		this.type = type;
		this.canBeSlot = canBeSlot;
		
		// Make sure we can do quick access from A to B.
		// Makes assumption B's values are unique.
		if (!this.b.hasIndex()) {
			this.b.createIndex();
		}
	}
	
	/**
	 * Returns a concatenated string thus:
	 * Alpha.Foo-Beta.Bar for class Alpha, attribute Foo and class Beta, attribute Bar,
	 * (corresponding to A and B respectively).
	 * <p/>
	 * This string may also be used for finding identical Relation instances.
	 * @return a unique name representation of the relation.
	 */
	public String getName() {
		return (this.a.getFullName() + "-" + this.b.getFullName());
	}
	
	/**
	 * Returns the relation type.
	 * @return the type.
	 */
	public Type getType() {
		return this.type;
	}
	
	/**
	 * Returns whether this relation can act as a 'slot' in a 'slot chain'
	 * in a parent-child dependency.
	 * @return true if it can be part of a slot chain; otherwise false.
	 */
	public boolean canBeSlot() {
		return this.canBeSlot;
	}
	
	/**
	 * Given a record of PRM class A, follows a relation and returns the index
	 * of the record in B, making the assumption that there is only one such record.
	 * @param aIdx the entity index in A.
	 * @return the corresponding entity index in B.
	 */
	public int getIndex(int aIdx) {
		return this.b.getIndex(this.a.getEntity(aIdx));
	}
	
	/**
	 * Returns the first attribute, i.e., A.
	 * @return fixed attribute A.
	 */
	public FixedAttribute getFirst() {
		return this.a;
	}
	
	/**
	 * Returns the second attribute, i.e., B.
	 * @return fixed attribute B.
	 */
	public FixedAttribute getSecond() {
		return this.b;
	}
}
