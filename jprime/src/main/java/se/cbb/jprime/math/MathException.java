package se.cbb.jprime.math;

/**
 * General purpose exception class for math-related issues such
 * as (unimplemented) computations lacking closed-form expressions, probabilities
 * having negative sign, etc. Not really used that much.
 * 
 * @author Joel Sjöstrand.
 */
public class MathException extends Exception {

	/** Auto-generated serial version UID. */
	private static final long serialVersionUID = 1293552367073876537L;

	/**
	 * Constructor.
	 * @param msg error message. 
	 */
	public MathException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public MathException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor.
	 * @param msg error message.
	 * @param cause the underlying cause, e.g. another exception.
	 */
	public MathException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
