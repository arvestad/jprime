package se.cbb.jprime.mcmc;

/**
 * Exception for when there is a controlled shut-down of an MCMC chain.
 * 
 * @author Joel Sjöstrand.
 */
public class RunAbortedException extends RuntimeException {
	
	/** Eclipse-generated UID. */
	private static final long serialVersionUID = 2078787628319758222L;

	/**
	 * Constructor.
	 * @param msg message.
	 */
	public RunAbortedException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 * @param msg message.
	 * @param cause cause.
	 */
	public RunAbortedException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
