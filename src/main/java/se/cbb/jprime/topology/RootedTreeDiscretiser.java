package se.cbb.jprime.topology;

/**
 * Interface for discretisations on top of rooted trees.
 * 
 * @author Joel Sjöstrand.
 */
public interface RootedTreeDiscretiser {

	/**
	 * Returns an ID string for the discretisation.
	 * @return the type.
	 */
	public String getDiscretisationType();
	
	/**
	 * Returns a string representation of the discretisation and the underlying tree.
	 * @return the discretisation.
	 */
	public String serializeToNewickTree();
	
}
