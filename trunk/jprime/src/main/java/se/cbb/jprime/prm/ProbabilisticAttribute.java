package se.cbb.jprime.prm;

import java.util.Random;

/**
 * Represents a fixed attribute of a PRM class, analogous to
 * a column in a relational database table. These hold variant data,
 * typically corresponding various characteristics.
 * <p/>
 * The attribute is tied to a PRM class, and also holds all attribute
 * entities (column values) in an indexed list which must be aligned
 * with all other attributes of the PRM class.
 * 
 * @author Joel Sjöstrand.
 */
public interface ProbabilisticAttribute {

	/** General type of data contained within. */
	public enum DataType {
		DISCRETE,
		CONTINUOUS
	}
	
	/**
	 * Type governing whether the attribute can be
	 * parent or child in a dependency structure.
	 */
	public enum DependencyConstraints {
		/** Can be both parent and child. */     NONE,
		/** Can only be parent. */               PARENT_ONLY,
		/** Can only be child. */                CHILD_ONLY,
		/** Can neither be parent nor child. */  NEITHER_PARENT_NOR_CHILD
	}
	
	/**
	 * Returns the PRM class this attribute belongs to.
	 * @return the PRM class.
	 */
	public PRMClass getPRMClass();
	
	/**
	 * Returns the name of the attribute.
	 * @return the attribute's name.
	 */
	public String getName();
	
	/**
	 * Returns the full name of the attribute thus:
	 * ClassName.AttributeName.
	 * @return the full name.
	 */
	public String getFullName();
	
	/**
	 * Returns the general data type of this attribute.
	 * @return the data type.
	 */
	public DataType getDataType();
	
	/**
	 * Returns the dependency constraints of this attribute.
	 * @return the dependency constraints.
	 */
	public DependencyConstraints getDependencyConstraints();
	
	/**
	 * Returns an attribute value at a specific index.
	 * @param idx the index.
	 * @return the value.
	 */
	public Object getEntityAsObject(int idx);
	
	/**
	 * Sets an attribute value at a specified index.
	 * @param idx the index.
	 * @param entity the value to be set.
	 */
	public void setEntityAsObject(int idx, Object entity);
	
	/**
	 * Adds an attribute value.
	 * @param entity the value.
	 */
	public void addEntityAsObject(Object entity);
	
	/**
	 * Returns the number of entities.
	 * @return the number of entities.
	 */
	public int getNoOfEntities();
	
	/**
	 * Returns a (presumably uniformly drawn) random attribute entity in accordance
	 * with the attribute definition.
	 * @param random an RNG (possibly PRNG).
	 * @return a random attribute.
	 */
	public Object getRandomEntityAsObject(Random random);
}
