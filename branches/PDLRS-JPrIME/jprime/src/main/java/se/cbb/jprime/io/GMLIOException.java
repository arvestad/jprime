package se.cbb.jprime.io;

/**
 * General purpose exception class for GML IO errors.
 * 
 * @author Joel Sjöstrand.
 */
public class GMLIOException extends Exception {

	/** Eclipse-generated UID. */
	private static final long serialVersionUID = 3873787985243690399L;

	/**
	 * Constructor.
	 * @param msg error message. 
	 */
	public GMLIOException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public GMLIOException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor.
	 * @param msg error message.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public GMLIOException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
