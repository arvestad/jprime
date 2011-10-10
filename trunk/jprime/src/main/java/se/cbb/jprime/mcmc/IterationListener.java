package se.cbb.jprime.mcmc;

/**
 * Interface for classes observing an Iteration object.
 * 
 * @author Joel Sjöstrand.
 */
public interface IterationListener {

	/**
	 * Callback invoked when an <code>Iteration</code> object has been incremented.
	 * @param iterValue the new iteration value after incrementing.
	 */
	public void incrementPerformed(int iterValue);
}
