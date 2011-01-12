package se.cbb.jprime.prm;

import java.util.Collection;
import java.util.HashMap;

/**
 * Holds a PRM skeleton, providing access to PRM classes and the
 * relations between them. Class and relation names must be unique
 * (within their categories).
 * Ultimately also holds a state, since entities are kept within PRM classes.
 * 
 * @author Joel Sjöstrand.
 */
public class Skeleton {

	/** PRM classes hashed by name. */
	private HashMap<String, PRMClass> classes;
	
	/** PRM relations hashed by name. */
	private HashMap<String, Relation> relations;
	
	/**
	 * Constructor.
	 */
	public Skeleton() {
		this.classes = new HashMap<String, PRMClass>(8);
		this.relations = new HashMap<String, Relation>(8);
	}
	
	/**
	 * Adds a PRM class.
	 * @param c the class.
	 */
	public void addPRMClass(PRMClass c) {
		this.classes.put(c.getName(), c);
	}
	
	/**
	 * Returns a PRM class.
	 * @param name the name of the class.
	 * @return the class.
	 */
	public PRMClass getPRMClass(String name) {
		return this.classes.get(name);
	}
	
	/**
	 * Returns all PRM classes of this skeleton.
	 * @return all PRM classes.
	 */
	public Collection<PRMClass> getPRMClasses() {
		return this.classes.values();
	}
	
	/**
	 * Returns the number of PRM classes of this skeleton.
	 * @return the number of PRM classes.
	 */
	public int getNoOfPRMClasses() {
		return this.classes.size();
	}
	
	/**
	 * Adds a PRM relation.
	 * @param r the relation.
	 */
	public void addRelation(Relation r) {
		this.relations.put(r.getName(), r);
	}
	
	/**
	 * Returns a PRM relation.
	 * @param name the name of the relation.
	 * @return the relation.
	 */
	public Relation getRelation(String name) {
		return this.relations.get(name);
	}
	
	/**
	 * Returns all relations of this skeleton.
	 * @return all relations.
	 */
	public Collection<Relation> getRelations() {
		return this.relations.values();
	}
	
	/**
	 * Returns the number of relations of this skeleton.
	 * @return the number of relations.
	 */
	public int getNoOfRelations() {
		return this.relations.size();
	}
}
