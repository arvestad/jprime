package se.cbb.jprime.apps.genphylodata;

/**
 * Thrown when max attempts have been exceeded.
 * 
 * @author Joel Sjöstrand.
 */
public class MaxAttemptsException extends Exception {

	/** Eclipse-generated serial version UID. */
	private static final long serialVersionUID = 5762265740698458537L;

	/**
	 * Constructor.
	 * @param str error message.
	 */
	public MaxAttemptsException(String str) {
		super(str);
	}
}
