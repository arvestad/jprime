package se.cbb.jprime.prm;

import java.util.HashMap;

/**
 * Represents a 'PRM class', i.e., more or less the equivalent of a
 * database table. Each 'entity' of the class (table record) has
 * <ol>
 * <li>a unique String ID (analogous to primary key).</li>
 * <li>an ordered list of attributes (strings, integers, ...) as defined by the class.</li> 
 * <li>an ordered list of references to other class entities (analogous to foreign keys) as
 *     defined by the class.</li>
 * </ol>
 * Apart from defining this table schema, the PRM class provides access to all class entities.
 * If the PRM class has references to other classes, one must first make sure that it points
 * to these PRM classes, after which one can see to that individual entities point to the
 * corresponding referenced entities.
 * 
 * @author Joel Sjöstrand.
 */
public class PRMClass {

	/**
	 * Inner class for representing entities, i.e., typically records for the table.
	 * Indexing of elements refer to the one defined in the parent PRMClass.
	 * At the moment there is no back-reference to the latter.
	 * References are kept de-referenced, that is, an entity directly points to
	 * its foreign entity objects, it does not store the foreign entity ID.
	 * 
	 * @author Joel Sjöstrand.
	 */
	class Entity {
		
		/** Unique ID (equivalent primary key). */
		String ID;
		
		/** Attribute values. */
		PRMAttribute[] attributes;
		
		/** References to other class entities (de-referenced foreign keys). */
		Entity[] references;
		
		/**
		 * Constructor. References are set by parent PRMClass.
		 * @param ID the entity ID.
		 * @param attributes the entity attributes.
		 */
		public Entity(String ID, PRMAttribute[] attributes, int noOfReferences) {
			this.ID = ID;
			this.attributes = attributes;
			this.references = (noOfReferences <= 0 ? null : new Entity[noOfReferences]);
		}
	}
	
	/** PRM class name. */
	private String name;
	
	/** Attribute names, ID excluded. */
	private String[] attributeNames;
	
	/** Maps an attribute names to its index. */
	private HashMap<String, Integer> attributeNameToIndex;
	
	/** References to other PRM classes. Non-null if empty. */
	private PRMClass[] references;
	
	/** Maps a reference to its index. */
	private HashMap<PRMClass, Integer> referenceToIndex;
	
	/** All entities, indexed by ID. */
	private HashMap<String, Entity> entities;
	
	/**
	 * Constructor when no references to foreign classes exist, or are currently unavailable.
	 * References may be set later using setReferences(...).
	 * @param name name of PRM class.
	 * @param attributeNames name of attributes.
	 * @param references referenced classes.
	 */
	public PRMClass(String name, String[] attributeNames) {
		this(name, attributeNames, new PRMClass[0]);
	}
	
	/**
	 * Constructor when there are references to foreign classes, and these already exist.
	 * @param name name of PRM class.
	 * @param attributeNames name of attributes.
	 * @param references referenced classes. Must not be null.
	 */
	public PRMClass(String name, String[] attributeNames, PRMClass[] references) {
		this.name = name;
		this.attributeNames = attributeNames;
		this.attributeNameToIndex = new HashMap<String, Integer>(this.attributeNames.length);
		for (int i = 0; i < this.attributeNames.length; ++i) {
			this.attributeNameToIndex.put(this.attributeNames[i], i);
		}
		this.references = references;
		this.referenceToIndex = new HashMap<PRMClass, Integer>(this.references.length);
		for (int i = 0; i < this.references.length; ++i) {
			this.referenceToIndex.put(this.references[i], i);
		}
		this.entities = new HashMap<String, PRMClass.Entity>();
	}
	
	/**
	 * Returns the name of the PRM class.
	 * @return the PRM class name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the name of an attribute at a specified index.
	 * @param idx the attribute index.
	 * @param the attribute name.
	 */
	public String getAttributeName(int idx) {
		return this.attributeNames[idx];
	}
	
	/**
	 * Returns the index of an attribute.
	 * @param name the attribute name.
	 * @param the attribute's index.
	 */
	public int getAttributeIndex(String name) {
		return this.attributeNameToIndex.get(name);
	}
	
	/**
	 * Returns the number of attributes, ID excluded.
	 * @return the number of attributes.
	 */
	public int getNoOfAttributes() {
		return this.attributeNames.length;
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
	 * Returns the reference index of a specific referenced class.
	 * @param PRMClass the referenced class.
	 * @return the reference's index.
	 */
	public int getReferenceIndex(PRMClass reference) {
		return this.referenceToIndex.get(reference);
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
		this.referenceToIndex = new HashMap<PRMClass, Integer>(this.references.length);
		for (int i = 0; i < this.references.length; ++i) {
			this.referenceToIndex.put(this.references[i], i);
		}
	}
	
	/**
	 * Retrieves an entity, throwing an exception if not found.
	 * @param ID the entities ID.
	 * @return the entity.
	 */
	private Entity getEntity(String ID) {
		Entity e = this.entities.get(ID);
		if (e == null) { throw new IllegalArgumentException("No element with that key exists."); }
		return e;
	}
	
	/**
	 * Adds an entity when references can be set immediately. This requires that
	 * all referenced entities already exist as objects. See also overloaded method.
	 * @param ID the unique ID of this entity. Any old value will be replaced.
	 * @param attributes the attributes.
	 * @param referenceIDs the IDs of all references. Referenced entities must already exist.
	 */
	public void putEntity(String ID, PRMAttribute[] attributes, String[] referenceIDs) {
		Entity e = new Entity(ID, attributes, this.getNoOfReferences());
		this.entities.put(ID, e);
		this.setEntityReferences(e, referenceIDs);
	}
	
	/**
	 * Adds an entity when there are no references, or when references cannot be set
	 * immediately (due to the fact that their
	 * corresponding objects do not already exist). One may at a later stage set references
	 * by invoking setEntityReferences(...). See also overloaded method.
	 * @param ID the unique ID of this entity. Any old value will be replaced.
	 * @param attributes the attributes.
	 */
	public void putEntity(String ID, PRMAttribute[] attributes) {
		Entity e = new Entity(ID, attributes, this.getNoOfReferences());
		this.entities.put(ID, e);
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
	private void setEntityReferences(Entity e, String[] referenceIDs) {
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
	public void setAttribute(String ID, int attributeIndex, PRMAttribute attribute) {
		Entity e = this.entities.get(ID);
		e.attributes[attributeIndex] = attribute;
	}
	
	/**
	 * Sets the attribute of an entity.
	 * @param ID the entity's ID.
	 * @param attributeName the attribute's name.
	 * @param attribute the attribute to be set.
	 */
	public void setAttribute(String ID, String attributeName, PRMAttribute attribute) {
		Entity e = this.entities.get(ID);
		e.attributes[this.attributeNameToIndex.get(attributeName)] = attribute;
	}
}
