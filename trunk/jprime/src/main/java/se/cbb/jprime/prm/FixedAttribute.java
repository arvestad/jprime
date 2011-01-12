package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a fixed attribute of a PRM class, analogous to
 * a column in a relational database table. These hold invariant data,
 * typically corresponding to primary or foreign keys.
 * <p/>
 * The attribute is tied to a PRM class, and also holds all attribute
 * entities (column values) in an indexed list which must be aligned
 * with all other attributes of the PRM class.
 * <p/>
 * Moreover, if the values are unique, the attribute may be "indexed" so
 * that one may do a quick lookup of a record's integer index from
 * its string value.
 * 
 * @author Joel Sjöstrand.
 */
public class FixedAttribute {

	/** PRM class. */
	private PRMClass prmClass;
	
	/** Attribute name. */
	private String name;
	
	/** Entities. */
	private ArrayList<String> entities;
	
	/** Index from value to integer index. Not always used. */
	private HashMap<String, Integer> index = null;
	
	/**
	 * Constructor.
	 * @param name attribute's name. Should be unique within PRM class.
	 * @param prmClass PRM class this attribute belongs to.
	 * @param initialCapacity initial capacity for attribute entities.
	 */
	public FixedAttribute(String name, PRMClass prmClass, int initialCapacity) {
		this.name = name;
		this.entities = new ArrayList<String>(initialCapacity);
		this.prmClass = prmClass;
		this.prmClass.addFixedAttribute(this);
	}
	
	/**
	 * Returns the PRM class this attribute belongs to.
	 * @return the PRM class.
	 */
	public PRMClass getPRMClass() {
		return this.prmClass;
	}
	
	/**
	 * Returns this attribute's name.
	 * @return the name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the full name of the attribute thus:
	 * ClassName.AttributeName.
	 * @return the full name.
	 */
	public String getFullName() {
		return this.prmClass.getName() + '.' + this.name;
	}
	
	/**
	 * Returns all attribute values.
	 * @return the values.
	 */
	public List<String> getEntities() {
		return this.entities;
	}
	
	/**
	 * Returns an attribute value at a specific index.
	 * @param idx the index.
	 * @return the value.
	 */
	public String getEntity(int idx) {
		return this.entities.get(idx);
	}
	
	/**
	 * Sets an attribute value at a specified index.
	 * @param idx the index.
	 * @param value the value to be set.
	 */
	public void setEntity(int idx, String value) {
		this.entities.set(idx, value);
	}
	
	/**
	 * Adds an attribute value.
	 * @param value the value.
	 */
	public void addEntity(String value) {
		this.entities.add(value);
	}
	
	/**
	 * Returns the number of entities.
	 * @return the number of entities.
	 */
	public int getNoOfEntities() {
		return this.entities.size();
	}
	
	/**
	 * Returns true if there is an index providing quick access
	 * of record integer index from record value.
	 * @return true if an index has been created.
	 */
	public boolean hasIndex() {
		return (this.index != null);
	}
	
	/**
	 * Creates an index providing quick access
	 * of record integer index from record value. Will only work
	 * if record values are unique.
	 */
	public void createIndex() {
		this.index = new HashMap<String, Integer>(this.entities.size());
		for (int i = 0; i < this.entities.size(); ++i) {
			index.put(this.entities.get(i), new Integer(i));
		}
	}
	
	/**
	 * Returns the integer index of a specific value, preconditioned on that
	 * an index has been created (see <code>hasIndex()</code>).
	 * @param value the entity's value.
	 * @return the entity's integer index.
	 */
	public int getIndex(String value) {
		return this.index.get(value).intValue();
	}
}
