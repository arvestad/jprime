package se.cbb.jprime.io;

/**
 * General purpose exception class for Newick IO errors.
 * 
 * @author Joel Sjöstrand.
 */
public class NewickIOException extends Exception {
	
	/** Auto-generated serial version UID. */
	private static final long serialVersionUID = 8360169124254804736L;

	/**
	 * Constructor.
	 * @param msg error message. 
	 */
	public NewickIOException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public NewickIOException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor.
	 * @param msg error message.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public NewickIOException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
