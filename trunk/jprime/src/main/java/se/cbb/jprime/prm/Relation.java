package se.cbb.jprime.prm;

/**
 * Represents a relation between two PRM classes A and B (analogous to two relational
 * database tables). The relation is specified by the two fixed attributes representing
 * the link.
 * <p/>
 * The order of A and B matters, since we allow only one-to-one
 * relations and many-to-one relations from A to B respectively.
 * Thus, the values of the attribute in B must be
 * unique. Many-to-many relations should be resolved using an intermediary PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public class Relation {

	/** Relation type from class A to class B. */
	public enum Type {
		ONE_TO_ONE,
		MANY_TO_ONE
	}
	
	/** Allowed probabilistic dependency structures for a relation. */
	public enum DependencyConstraints {
		/** Any direction allowed.  */                                  NONE,
		/** Ancestor-descendant slot chain may go through relation. */  ANCESTOR_DESCENDANT,
		/** Descendant-ancestor slot chain may go through relation. */  DESCENDANT_ANCESTOR,
		/** No slot chain may go through relation. */                   NOT_ALLOWED
	}
	
	/** Fixed attribute of class A. */
	private FixedAttribute a;
	
	/** Fixed attribute of class B. */
	private FixedAttribute b;
	
	/** Relation type. */
	private Type type;
	
	/** Governs which dependency structures are allowed to flow through this relation. */
	private DependencyConstraints dependencyConstraints;
	
	/**
	 * Constructor.
	 * @param the fixed attribute of class A.
	 * @param the fixed attribute of class B.
	 * @param type relation type from A to B.
	 * @param dependencyConstraints governs which slot chains this relation may be part of.
	 */
	public Relation(FixedAttribute a, FixedAttribute b, Type type,
			DependencyConstraints dependencyConstraints) {
		this.a = a;
		this.b = b;
		this.type = type;
		this.dependencyConstraints = dependencyConstraints;
		
		// Make sure we can do quick access from A to B.
		if (!this.b.hasIndex()) {
			this.b.createIndex();
		}
	}
	
	/**
	 * Returns a concatenated string thus:
	 * Alfa.Foo-Beta.Bar for class Alfa, attribute Foo and class Beta, attribute Bar.
	 * @return a name representation of the relation.
	 */
	public String getName() {
		return (this.a.getPRMClass().getName() + "." + a.getName() + "-" +
				this.b.getPRMClass().getName() + "." + b.getName());
	}
	
	/**
	 * Returns the relation type.
	 * @return the type.
	 */
	public Type getType() {
		return this.type;
	}
	
	/**
	 * Returns the type of dependencies that are allowed to through
	 * this relation, i.e., whether it can be used in a slot chain.
	 * @return the dependency constraints.
	 */
	public DependencyConstraints getDependencyConstraints() {
		return this.dependencyConstraints;
	}
	
	/**
	 * Given a record of PRM class A, following the relation, returns the index
	 * of the record in B.
	 * @param aIdx the entity index in A.
	 * @return the corresponding entity index in B.
	 */
	public int getIndex(int aIdx) {
		return this.b.getIndex(this.a.getEntity(aIdx));
	}
}
