package se.cbb.jprime.math;

/**
 * Interface for external solver callback function which will be invoked after each (accepted) solver step
 * of <code>ODESolver</code>.
 * 
 * @author Joel Sjöstrand.
 */
public interface ODEExternalSolutionProvider {

	/** Return values from external solution provider solout(). */
	public enum SolutionProviderResult {
		/** External provider returned interrupt request. */			INTERRUPT_SOLVER,  
		/** External provider was not called. */						NOT_INVOKED,  
		/** External provider returned without changing solution. */	SOLUTION_NOT_CHANGED,  
		/** External provider returned with altered solution. */		SOLUTION_CHANGED;
	}
	
	/**
	 * External callback function which will be invoked after each (accepted) solver step.
	 * This function may change the solution, in which case it should make use of the
	 * corresponding return code. Additionally, it may call the method contd5() if it
	 * seeks an interpolated solution for a value in the range [xold,x] (albeit only if
	 * the hasDense flag has been set to true).
	 * @param no the current iteration number (accepted steps).
	 * @param xold the previous x-value of the solver.
	 * @param x the current x-value of the solver.
	 * @param y the current solution at x.
	 * @param icomp list of component indices for which there are continuous output.
	 * @return a suitable return code.
	 */
	public SolutionProviderResult solout(int no, double xold, double x, double[] y);
	
}
