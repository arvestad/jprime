package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a 'PRM class', i.e., more or less the equivalent of a relational
 * database table. Each class defines a schema by specifying these components:
 * <ol>
 * <li>a list of fixed string attributes, typically corresponding to primary and foreign keys.</li>
 * <li>a list of probabilistic attributes (strings, integers, ...) typically corresponding
 *     to various characteristics.</li>
 * </ol>
 * All attributes must have unique names (within their type).
 * <p/>
 * The table records are maintained column-wise in the attributes themselves, but we
 * require that all values corresponding to a certain table record are aligned by having
 * the same index in each attribute.
 * <p/>
 * In addition, a PRM class provides access to all 'relations' emanating from the class,
 * see <code>Relation</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class PRMClass {
	
	/** PRM class name. */
	private final String name;
	
	/** Registered relations emanating from this class, hashed by name. */
	private final HashMap<String, Relation> relations;
	
	/** Fixed attributes hashed by name. */
	private final HashMap<String, FixedAttribute> fixedAttributes;
	
	/** Probabilistic attributes, hashed by name. */
	private final HashMap<String, ProbabilisticAttribute> probAttributes;
	
	/**
	 * Constructor.
	 * @param name name of PRM class.
	 */
	public PRMClass(String name) {
		this.name = name;
		this.relations = new HashMap<String, Relation>(4);
		this.fixedAttributes = new HashMap<String, FixedAttribute>(4);
		this.probAttributes = new HashMap<String, ProbabilisticAttribute>(8);
	}
	
	/**
	 * Returns the name of the PRM class.
	 * @return the PRM class name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Adds a relation emanating from this class.
	 * No verification is made for its validity.
	 * @param rel the relation.
	 */
	public void addRelation(Relation rel) {
		this.relations.put(rel.getName(), rel);
	}
	
	/**
	 * Returns a relation emanating from this class.
	 * @param name the name of the relation.
	 * @return the relation.
	 */
	public Relation getRelation(String name) {
		return this.relations.get(name);
	}
	
	/**
	 * Returns all relations emanating from this PRM class.
	 * @return the relations.
	 */
	public Collection<Relation> getRelations() {
		return this.relations.values();
	}
	
	/**
	 * Adds a fixed attribute. No verification is made for its validity.
	 * @param attribute the attribute.
	 */
	public void addFixedAttribute(FixedAttribute attribute) {
		this.fixedAttributes.put(attribute.getName(), attribute);
	}
	
	/**
	 * Returns a fixed attribute.
	 * @param name the attribute name.
	 * @return the attribute.
	 */
	public FixedAttribute getFixedAttribute(String name) {
		return this.fixedAttributes.get(name);
	}
	
	/**
	 * Returns all fixed attributes.
	 * @return all attributes.
	 */
	public Collection<FixedAttribute> getFixedAttributes() {
		return this.fixedAttributes.values();
	}
	
	/**
	 * Returns the number of fixed attributes.
	 * @return the number of attributes.
	 */
	public int getNoOfFixedAttributes() {
		return this.fixedAttributes.size();
	}
	
	/**
	 * Adds a probabilistic attribute. No verification is made for its validity.
	 * @param attribute the attribute.
	 */
	public void addProbAttribute(ProbabilisticAttribute attribute) {
		this.probAttributes.put(attribute.getName(), attribute);
	}
	
	/**
	 * Returns a probabilistic attribute.
	 * @param name the attribute name.
	 * @return the attribute.
	 */
	public ProbabilisticAttribute getProbAttribute(String name) {
		return this.probAttributes.get(name);
	}
	
	/**
	 * Returns all probabilistic attributes.
	 * @return all attributes.
	 */
	public Collection<ProbabilisticAttribute> getProbAttributes() {
		return this.probAttributes.values();
	}
	
	/**
	 * Returns the number of probabilistic attributes.
	 * @return the number of attributes.
	 */
	public int getNoOfProbAttributes() {
		return this.probAttributes.size();
	}
	
	/**
	 * Returns the number of entities. It is assumed that
	 * all intrinsic attributes has this number.
	 * @return
	 */
	public int getNoOfEntities() {
		if (this.fixedAttributes.size() > 0) {
			return this.fixedAttributes.values().iterator().next().getNoOfEntities();
		}
		if (this.probAttributes.size() > 0) {
			return this.probAttributes.values().iterator().next().getNoOfEntities();
		}
		return 0;
	}
}
