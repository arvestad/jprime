package se.cbb.jprime.prm;

import java.util.HashMap;

/**
 * Holds a PRM skeleton, providing access to PRM classes and the
 * relations between them. Class and relation names must be unique.
 * (Also holds a state, since entities are included within classes.)
 * 
 * @author Joel Sjöstrand.
 */
public class Skeleton {

	/** PRM classes. */
	private HashMap<String, PRMClass> classes;
	
	/** PRM relations. */
	private HashMap<String, Relation> relations;
	
	/**
	 * Constructor.
	 */
	public Skeleton() {
		this.classes = new HashMap<String, PRMClass>();
		this.relations = new HashMap<String, Relation>();
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
}
