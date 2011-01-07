package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a 'PRM class', i.e., more or less the equivalent of a relational
 * database table. Each class defines a schema by specifying these components:
 * <ol>
 * <li>an ordered list of fixed string attributes, typically corresponding to primary and foreign keys.
 *     <b>Note:</b> We require that element 0 represents a unique string ID (analogous to a primary key).</li>
 * <li>an ordered list of probabilistic attributes (strings, integers, ...) typically corresponding
 *     to various characteristics.</li>
 * </ol>
 * Relations to other PRM classes are handled elsewhere (see <code>Relation</code>).
 * <p/>
 * Moreover, the class will provide access to all 'entities' of the class (i.e. table records).
 * 
 * @author Joel Sjöstrand.
 */
public class PRMClass {
	
	/** PRM class name. */
	private String name;
	
	/** Unique names of fixed attributes. Element 0 must correspond to a unique ID. */
	private String[] fixedAttributeNames;
	
	/** Unique names of probabilistic attributes. */
	private String[] probAttributeNames;
	
	/** All class entities, indexed by element 0 of fixed attributes. */
	private HashMap<String, ClassEntity> entities;
	
	/**
	 * Constructor.
	 * @param name name of PRM class.
	 * @param fixedAttributeNames names of fixed attributes.
	 * @param probAttributeNames names of probabilistic attributes.
	 */
	public PRMClass(String name, String[] fixedAttributeNames, String[] probAttributeNames) {
		this.name = name;
		this.fixedAttributeNames = fixedAttributeNames;
		this.probAttributeNames = probAttributeNames;
		this.entities = new HashMap<String, ClassEntity>();
	}
	
	/**
	 * Returns the name of the PRM class.
	 * @return the PRM class name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the name of a fixed attribute at a specified index.
	 * @param idx the attribute index.
	 * @param the attribute name.
	 */
	public String getFixedAttributeName(int idx) {
		return this.fixedAttributeNames[idx];
	}
	
	/**
	 * Returns the number of fixed attributes.
	 * @return the number of attributes.
	 */
	public int getNoOfFixedAttributes() {
		return this.fixedAttributeNames.length;
	}
	
	/**
	 * Returns the name of the fixed attribute containing the ID
	 * (element 0).
	 * @return the ID name.
	 */
	public String getIDName() {
		return this.fixedAttributeNames[0];
	}
	
	/**
	 * Returns the name of a probabilistic attribute at a specified index.
	 * @param idx the attribute index.
	 * @param the attribute name.
	 */
	public String getProbAttributeName(int idx) {
		return this.probAttributeNames[idx];
	}
	
	/**
	 * Returns the number of probabilistic attributes.
	 * @return the number of attributes.
	 */
	public int getNoOfProbAttributes() {
		return this.probAttributeNames.length;
	}
	
	/**
	 * Retrieves an entity without verifying its presence.
	 * @param ID the entity's ID.
	 * @return the entity.
	 */
	public ClassEntity getEntity(String ID) {
		return this.entities.get(ID);
	}
	
	/**
	 * Retrieves an entity, throwing an exception if not found.
	 * @param ID the entity's ID.
	 * @return the entity.
	 */
	public ClassEntity getEntitySafe(String ID) {
		ClassEntity e = this.entities.get(ID);
		if (e == null) { throw new IllegalArgumentException("No element with that key exists."); }
		return e;
	}
	
	/**
	 * Adds an entity. Parameters must comply with PRM class schema.
	 * @param fixedAttributes the fixed attribute values.
	 * @param probAttributes the probabilistic attribute values.
	 */
	public void putEntity(String[] fixedAttributes, AttributeEntity[] probAttributes) {
		ClassEntity e = new ClassEntity(fixedAttributes, probAttributes);
		this.entities.put(e.getID(), e);
	}
	
	/**
	 * Adds an entity. Its parameters must comply with PRM class schema.
	 * @param e the entity.
	 */
	public void putEntity(ClassEntity e) {
		this.entities.put(e.getID(), e);
	}
	
	/**
	 * Returns the number of entities.
	 * @return the number of entities.
	 */
	public int getNoOfEntities() {
		return this.entities.size();
	}
	
	/**
	 * Sets the probabilistic attribute value of a class entity.
	 * @param ID the entity's ID.
	 * @param attrIdx the attribute's index.
	 * @param attr the attribute to be set.
	 */
	public void setEntityProbAttribute(String ID, int attrIdx, AttributeEntity attr) {
		ClassEntity e = this.entities.get(ID);
		e.setProbAttribute(attrIdx, attr);
	}
	
	/**
	 * Returns all entities.
	 * @return the entities.
	 */
	public Collection<ClassEntity> getEntities() {
		return this.entities.values();
	}
}
