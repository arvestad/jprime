package se.cbb.jprime.mcmc;

/**
 * Base class for storing information on changes of state
 * parameters and other data structures. This usually enables optimised
 * updates of objects that depend on the perturbed object.
 * 
 * @author Joel Sjöstrand.
 */
public class ChangeInfo {

	/** The object which the information refers to. */
	protected Dependent changed;
	
	/** String which can be used for discriminating between changes in simple cases. */
	protected String info = null;
	
	/**
	 * Constructor.
	 * @param changed the object which changed.
	 */
	public ChangeInfo(Dependent changed) {
		this.changed = changed;
	}
	
	/**
	 * Constructor.
	 * @param changed the object which changed.
	 * @param info details or ID for the change.
	 */
	public ChangeInfo(Dependent changed, String info) {
		this.changed = changed;
	}
	
	/**
	 * Returns the object which the information refers to.
	 * @return the changed object.
	 */
	public Dependent getChanged() {
		return this.changed;
	}
}
