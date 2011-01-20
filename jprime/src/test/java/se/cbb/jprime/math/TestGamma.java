package se.cbb.jprime.math;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for Gamma.
 * 
 * @author Joel Sjöstrand.
 */
public class TestGamma {

	@Test
	public void testLnGamma() {
		double a = 3.0;
		double lngA = Gamma.lnGamma(a);
		double b = 12.3456789;
		double lngB = Gamma.lnGamma(b);
		assertTrue(Math.abs(lngA - 0.6931472) < 1e-5);
		assertTrue(Math.abs(lngB - 18.35183) < 1e-5);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testLnGammaSafeOne() {
	    Gamma.lnGammaSafe(0.0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testLnGammaSafeTwo() {
	    Gamma.lnGammaSafe(-1);
	}
}
