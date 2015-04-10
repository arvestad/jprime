package se.cbb.jprime.math;

/**
 * Java-style functor for implementing an ODE function.
 * 
 * @author Joel Sjöstrand.
 */
public interface ODEFunction {

	/**
	 * Evaluates the function y' = f(x,y).
	 * @param x the x value.
	 * @param y the y values.
	 * @param f the derivatives y'=f(x,y) the method computes.
	 */
	public void evaluate(double x, double[] y, double[] f);
}
