package se.cbb.jprime.prm;

/**
 * Exception for PRM dependencies.
 * 
 * @author Joel Sjöstrand.
 */
public class DependencyException extends Exception {
	
	/** Eclipse-generated ID. */
	private static final long serialVersionUID = -5344692966843351773L;

	/**
	 * Constructor.
	 * @param msg error message.
	 */
	public DependencyException(String msg) {
		super(msg);
	}
	
}
