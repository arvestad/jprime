package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a relation between two PRM classes A and B (analogous to two relational
 * database tables). The relation is specified by
 * <ol>
 * <li>the class instances A and B.</li>
 * <li>the indices of the fixed attribute in A and B that links the two (typically 
 *     corresponding to a foreign key in A and the primary key of B. <b>At the moment,
 *     B's index cannot be specified; it is and assumed to be its ID field.</b></li>
 * </ol>
 * Furthermore, the relation can be used to cache all 'relation entities', that is, all 
 * references between class entities of the two classes.
 * <p/>
 * Note: the order of A and B matter, since we allow only one-to-one
 * relations and many-to-one relations (and also w.r.t. to the B index assumption above).
 * Many-to-many relations must be resolved using
 * an intermediary PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public class Relation {

	/** Relation type from class A to class B. */
	public enum Type {
		ONE_TO_ONE,
		MANY_TO_ONE
	}
	
	/** First class. */
	private PRMClass a;
	
	/** Index of fixed attribute in A. */
	private int aIndex;
	
	/** Second class. */
	private PRMClass b;
	
	/** Index of fixed attribute in B. Assumed to be the ID field! */
	//private int bIndex;
	
	/** Relation type. */
	private Type type;
	
	/** Relation entities, where instances of A acts as keys. */
	private HashMap<ClassEntity, ClassEntity> entities;
	
	/**
	 * Constructor.
	 * @param a PRM class A.
	 * @param aIndex index of fixed attribute in A containing link.
	 * @param b PRM class B.
	 * @param type relation type from A to B.
	 */
	public Relation(PRMClass a, int aIndex, PRMClass b, Type type) {
		this.a = a;
		this.b = b;
		this.aIndex = aIndex;
		//this.bIndex = bIndex;
		this.type = type;
		this.entities = new HashMap<ClassEntity, ClassEntity>(this.a.getNoOfEntities());
	}
	
	/**
	 * Stores all relation entities for quick access.
	 */
	public void cacheEntities() {
		Collection<ClassEntity> aEntities = this.a.getEntities();
		for (ClassEntity ae : aEntities) {
			// We assume that B's ID field is used!
			ClassEntity be = this.b.getEntitySafe(ae.getFixedAttribute(this.aIndex));
			this.entities.put(ae, be);
		}
	}
	
	/**
	 * Clears all relation entities.
	 */
	public void clearEntities() {
		this.entities.clear();
	}
	
	/**
	 * Returns a concatenated string thus:
	 * <A's name><A's index>-<B's name><B's index>.
	 * @return a name representation of the relation.
	 */
	public String getName() {
		return (this.a.getName() + this.aIndex + "-" + this.b.getName() + "0");
	}
	
	/**
	 * Returns the relation type.
	 * @return the type.
	 */
	public Type getType() {
		return this.type;
	}
}
