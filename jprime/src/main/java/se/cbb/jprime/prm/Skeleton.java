package se.cbb.jprime.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Holds a PRM skeleton, providing access to PRM classes, and thereby indirectly
 * provides access to relations, attributes and the entities thereof.
 * Class names must be unique.
 * 
 * @author Joel Sjöstrand.
 */
public class Skeleton {

	/** PRM classes hashed by name. */
	private final HashMap<String, PRMClass> classesByName;
	
	/** PRM classes in ordered added. */
	private final ArrayList<PRMClass> classesByIndex;
	
	/**
	 * Constructor.
	 */
	public Skeleton() {
		this.classesByName = new HashMap<String, PRMClass>(8);
		this.classesByIndex = new ArrayList<PRMClass>(8);
	}
	
	/**
	 * Adds a PRM class.
	 * @param c the class.
	 */
	public void addPRMClass(PRMClass c) {
		this.classesByName.put(c.getName(), c);
		this.classesByIndex.add(c);
	}
	
	/**
	 * Returns a PRM class.
	 * @param name the name of the class.
	 * @return the class.
	 */
	public PRMClass getPRMClass(String name) {
		return this.classesByName.get(name);
	}
	
	/**
	 * Returns all PRM classes of this skeleton.
	 * @return all PRM classes.
	 */
	public List<PRMClass> getPRMClasses() {
		return this.classesByIndex;
	}
	
	/**
	 * Returns the number of PRM classes of this skeleton.
	 * @return the number of PRM classes.
	 */
	public int getNoOfPRMClasses() {
		return this.classesByIndex.size();
	}
	
	/**
	 * Returns a uniformly selected PRM class.
	 * @param rng random number generator.
	 * @return a uniformly selected PRM class.
	 */
	public PRMClass getRandomPRMClass(Random rng) {
		return this.classesByIndex.get(rng.nextInt(this.classesByIndex.size()));
	}
	
}
