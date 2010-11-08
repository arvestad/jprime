package se.cbb.jprime.topology;

/**
 * Exception raised when errors concerning topology occur (tree copying,
 * erroneous arcs, topology-map mismatch, etc.).
 * 
 * @author Joel Sjöstrand.
 */
public class TopologyException extends Exception {

	/** Auto-generated serial version UID. */
	private static final long serialVersionUID = -3662562805705772146L;

	/**
	 * Constructor.
	 * @param msg error message. 
	 */
	public TopologyException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public TopologyException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor.
	 * @param msg error message.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public TopologyException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
