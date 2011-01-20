package se.cbb.jprime.math;

/**
 * General purpose exception class for probability-related issues such
 * as (unimplemented) computations lacking closed-form expressions, probabilities
 * having negative sign, etc.
 * 
 * @author Joel Sjöstrand.
 */
public class ProbabilityException extends Exception {

	/** Auto-generated serial version UID. */
	private static final long serialVersionUID = 1293552367073876537L;

	/**
	 * Constructor.
	 * @param msg error message. 
	 */
	public ProbabilityException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public ProbabilityException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor.
	 * @param msg error message.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public ProbabilityException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
