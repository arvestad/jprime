package se.cbb.jprime.prm;

import java.util.HashMap;

/**
 * Holds a PRM skeleton, i.e. a set of PRM classes (including their entities)
 * and relations between them.
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
	 * @param ID a unique ID of the class.
	 * @param c the class.
	 */
	public void putClass(String ID, PRMClass c) {
		this.classes.put(ID, c);
	}
	
	/**
	 * Returns a PRM class.
	 * @param ID the ID of the class.
	 * @return the class.
	 */
	public PRMClass getClass(String ID) {
		return this.classes.get(ID);
	}
	
	/**
	 * Adds a PRM relation.
	 * @param ID a unique ID of the relation.
	 * @param r the relation.
	 */
	public void putRelation(String ID, Relation r) {
		this.relations.put(ID, r);
	}
	
	/**
	 * Returns a PRM relation.
	 * @param ID the ID of the relation.
	 * @return the relation.
	 */
	public Relation getRelation(String ID) {
		return this.relations.get(ID);
	}
}
