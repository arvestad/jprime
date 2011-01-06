package se.cbb.jprime.prm;

import java.util.HashMap;

/**
 * Represents a 'PRM class', i.e., more or less the equivalent of a relational
 * database table. Each class defines a schema by specifying these components:
 * <ol>
 * <li>an ordered list of fixed string attributes, where element 0 represents a unique String ID
 *     (analogous to a primary key).</li>
 * <li>an ordered list of probabilistic attributes (strings, integers, ...).</li> 
 * <li>an ordered list of references to other classes (analogous to foreign keys). At the moment
 *     we require one-to-one relations where it is assumed that the ID specified in the reference refers
 *     to element 0 in the fixed attributes of the foreign class.</li>
 * </ol>
 * Moreover, the class will provide access to all 'entities' of the class (i.e. table records).
 * <p/>
 * One may choose to setup references for the class itself (and the entities) either
 * immediately or in two steps; the latter when not all objects are available yet.
 * 
 * @author Joel Sjöstrand.
 */
public class PRMClass {

	/**
	 * Inner class for representing entities, i.e., typically records for the table.
	 * There is a direct correspondence between its values and the scheme of its class.
	 * At the moment there is no back-reference to the latter.
	 * <p/>
	 * Since one-to-one reference relations are assumed, references point directly to their foreign
	 * entity objects, (i.e., foreign keys are "dereferenced").
	 * 
	 * @author Joel Sjöstrand.
	 */
	class ClassEntity {
		
		/** Fixed attribute values. First element is used as ID and must be present. */
		String[] fixedAttributes;
		
		/** Probabilistic attribute values. */
		AttributeEntity[] probAttributes;
		
		/** References to other class entities (dereferenced foreign keys). */
		ClassEntity[] references;
		
		/**
		 * Constructor. "Dereferencing" is done by PRMClass.
		 * @param ID the entity ID.
		 * @param attributes the entity attributes.
		 */
		public ClassEntity(String[] fixedAttributes, AttributeEntity[] probAttributes, int noOfReferences) {
			this.fixedAttributes = fixedAttributes;
			this.probAttributes = probAttributes;
			this.references = (noOfReferences <= 0 ? null : new ClassEntity[noOfReferences]);
		}
	}
	
	/** PRM class name. */
	private String name;
	
	/** Unique names of fixed attributes. Element 0 should contain ID. */
	private String[] fixedAttributeNames;
	
	/** Unique names of probabilistic attributes. */
	private String[] probAttributeNames;
	
	/**
	 * References to other PRM classes. Non-null if empty. Reference relations are assumed to
	 * be one-to-one and refer to element 0 of fixed attributes of foreign class.
	 */
	private PRMClass[] references;
	
	/** All class entities, indexed by element 0 of fixed attributes. */
	private HashMap<String, ClassEntity> entities;
	
	/**
	 * Constructor when no references to foreign classes exist, or are currently unavailable.
	 * References may be set later using setReferences(...).
	 * @param name name of PRM class.
	 * @param fixedAttributeNames names of fixed attributes.
	 * @param probAttributeNames names of probabilistic attributes.
	 */
	public PRMClass(String name, String[] fixedAttributeNames, String[] probAttributeNames) {
		this(name, fixedAttributeNames, probAttributeNames, new PRMClass[0]);
	}
	
	/**
	 * Constructor when there are references to foreign classes, and the objects corresponding to
	 * these already exist.
	 * @param name name of PRM class.
	 * @param fixedAttributeNames names of fixed attributes.
	 * @param probAttributeNames names of probabilistic attributes.
	 * @param references referenced classes. Must not be null. The same class may appear multiple times.
	 */
	public PRMClass(String name, String[] fixedAttributeNames, String[] probAttributeNames, PRMClass[] references) {
		this.name = name;
		this.fixedAttributeNames = fixedAttributeNames;
		this.probAttributeNames = probAttributeNames;
		this.references = references;
		this.entities = new HashMap<String, PRMClass.ClassEntity>();
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
	 * Returns the reference class at a specific index.
	 * @param idx the index.
	 * @return the reference PRM class.
	 */
	public PRMClass getReference(int idx) {
		return this.references[idx];
	}
	
	/**
	 * Returns the name of a referenced PRM class.
	 * @param idx the index of the reference.
	 * @return the name of the referenced PRM class.
	 */
	public String getReferenceName(int idx) {
		return this.references[idx].name;
	}
	
	/**
	 * Returns the number of references (foreign keys) of this PRM class.
	 * @return the number of references to other PRM classes.
	 */
	public int getNoOfReferences() {
		return this.references.length;
	}
	
	/**
	 * Sets the referenced classes of this class.
	 * @param references the referenced classes. Must not be null.
	 */
	public void setReferences(PRMClass[] references) {
		this.references = references;
	}
	
	/**
	 * Retrieves an entity, throwing an exception if not found.
	 * @param ID the entities ID.
	 * @return the entity.
	 */
	private ClassEntity getEntity(String ID) {
		ClassEntity e = this.entities.get(ID);
		if (e == null) { throw new IllegalArgumentException("No element with that key exists."); }
		return e;
	}
	
	/**
	 * Adds an entity when references can be set immediately. This requires that class references are in place
	 * and that all referenced entities already exist as objects. See also overloaded method.
	 * @param fixedAttributes the fixed attribute values.
	 * @param probAttributes the probabilistic attribute values.
	 * @param referenceIDs the IDs of all references. Referenced entities must already exist.
	 */
	public void putEntity(String[] fixedAttributes, AttributeEntity[] probAttributes, String[] referenceIDs) {
		ClassEntity e = new ClassEntity(fixedAttributes, probAttributes, this.getNoOfReferences());
		this.entities.put(fixedAttributes[0], e);
		this.setEntityReferences(e, referenceIDs);
	}
	
	/**
	 * Adds an entity when there are no references, or when references cannot be set
	 * immediately (due to the fact that their corresponding objects do not yet exist).
	 * One may at a later stage set references by invoking setEntityReferences(...).
	 * See also overloaded method.
	 * @param fixedAttributes the fixed attribute values.
	 * @param probAttributes the probabilistic attribute values.
	 */
	public void putEntity(String[] fixedAttributes, AttributeEntity[] probAttributes) {
		ClassEntity e = new ClassEntity(fixedAttributes, probAttributes, this.getNoOfReferences());
		this.entities.put(fixedAttributes[0], e);
	}
	
	/**
	 * Returns the number of entities.
	 * @return the number of entities.
	 */
	public int getNoOfEntities() {
		return this.entities.size();
	}
	
	/**
	 * Sets an entity's references based on their IDs. All
	 * referenced items must exist and comply with the ordering of referenced PRMClasses.
	 * @param ID the ID of the entity to update.
	 * @param referenceIDs the IDs of the references.
	 */
	public void setEntityReferences(String ID, String[] referenceIDs) {
		setEntityReferences(this.entities.get(ID), referenceIDs);
	}
	
	/**
	 * Sets an entity's references based on their IDs. All
	 * referenced items must exist and comply with the ordering of referenced PRMClasses.
	 * @param e the entity to update.
	 * @param referenceIDs the IDs of the references.
	 */
	private void setEntityReferences(ClassEntity e, String[] referenceIDs) {
		for (int i = 0; i < this.references.length; ++i) {
			PRMClass c = this.references[i];
			e.references[i] = c.getEntity(referenceIDs[i]);
		}
	}
	
	/**
	 * Sets the attribute of an entity.
	 * @param ID the entity's ID.
	 * @param attributeIndex the attribute's index.
	 * @param attribute the attribute to be set.
	 */
	public void setAttribute(String ID, int attributeIndex, AttributeEntity attribute) {
		ClassEntity e = this.entities.get(ID);
		e.probAttributes[attributeIndex] = attribute;
	}
}
