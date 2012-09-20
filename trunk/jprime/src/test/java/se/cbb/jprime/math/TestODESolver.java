package se.cbb.jprime.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestODESolver {

	/**
	 * Simple 1-component ODE.
	 * Compute for e.g. 0<=x<=2 and y(0)=6.
	 */
	class SinODE extends ODESolver {
		SinODE() {
			super(new ODEFunction() {
				
				@Override
				public void evaluate(double x, double[] y, double[] f) {
					f[0] = Math.sin(x * y[0]);
				}
			}, 1e-4, 1e-4);
		}
	}
	
	/**
	 * 2-component ODE (the Brusselator).
	 * Compute for e.g. 0<=x<=20 and y(0)=[1.5, 3.0].
	 */
	class BrusselatorODE extends ODESolver {
		BrusselatorODE() {
			super(new ODEFunction() {
				
				@Override
				public void evaluate(double x, double[] y, double[] f) {
					f[0] = 1 + y[0] * y[0] * y[1] - 4 * y[0];
					f[1] = 3 * y[0] - y[0] * y[0] * y[1];
				}
			}, 1e-4, 1e-4);
		}	
	}
	
	/**
	 * Classic 2-component autonomous ODE for fox-bunny growth.
	 * Compute for e.g. 0<=x<=9 and y(0)=[150,300]. 
	 */
	class FoxesAndBunniesODE extends ODESolver {
		FoxesAndBunniesODE() {
			super(new ODEFunction() {
				
				@Override
				public void evaluate(double x, double[] y, double[] f) {
					f[0] = -y[0] + 0.01 * y[0] * y[1];     // Fox growth rate.
					f[1] = 2 * y[1] - 0.01 * y[0] * y[1];  // Bunny growth rate.
				}
			}, 1e-4, 1e-4);
		}
	}
	
	@Test
	public void testSinODE() {
		ODESolver ode = new SinODE();
		double x = 0;
		double xend = 2;
		double[] y = new double[] { 6 };
		double h = 0;
		ODESolver.SolverResult res = ode.dopri5(x, xend, y, h);
		assertEquals(ODESolver.SolverResult.SUCCESSFUL, res);
		
		// Print stats.
		int[] stats = ode.getStatistics();
		System.out.println("Function evals: " + stats[0]
			+ ", iterations: " + stats[1]
			+ ", acc. steps: " + stats[2]
			+ ", rej. steps: " + stats[3]
			+ "\n");
	}
	
	@Test
	public void testBrusselatorODE() {
		ODESolver ode = new BrusselatorODE();
		double x = 0;
		double xend = 20;
		double[] y = new double[] { 1.5, 3.0 };
		double h = 0;
		ODESolver.SolverResult res = ode.dopri5(x, xend, y, h);
		assertEquals(ODESolver.SolverResult.SUCCESSFUL, res);
		
		// Print stats.
		int[] stats = ode.getStatistics();
		System.out.println("Function evals: " + stats[0]
			+ ", iterations: " + stats[1]
			+ ", acc. steps: " + stats[2]
			+ ", rej. steps: " + stats[3]
			+ "\n");
	}
	
	@Test
	public void testFoxesAndBunniesODE() {
		ODESolver ode = new FoxesAndBunniesODE();
		double x = 0;
		double xend = 9;
		double[] y = new double[] { 150, 300 };
		double h = 0;
		ODESolver.SolverResult res = ode.dopri5(x, xend, y, h);
		assertEquals(ODESolver.SolverResult.SUCCESSFUL, res);
		
		// Print stats.
		int[] stats = ode.getStatistics();
		System.out.println("Function evals: " + stats[0]
			+ ", iterations: " + stats[1]
			+ ", acc. steps: " + stats[2]
			+ ", rej. steps: " + stats[3]
			+ "\n");
	}
}

