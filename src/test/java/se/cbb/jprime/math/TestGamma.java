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
		//System.out.println(Gamma.lnGamma(a));
		//System.out.println(Gamma.lnGamma(b));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testLnGammaSafeOne() {
	    Gamma.lnGammaSafe(0.0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testLnGammaSafeTwo() {
	    Gamma.lnGammaSafe(-1);
	}
	
	@Test
	public void testGammaQuantile() {
		assertEquals(0.1025866, Gamma.gammaQuantile(0.05, 1, 2), 1e-6);
		assertEquals(5.991465, Gamma.gammaQuantile(0.95, 1, 2), 1e-6);
		assertEquals(3.621315, Gamma.gammaQuantile(0.05, 9.876, 0.6789), 1e-6);
		assertEquals(10.55584, Gamma.gammaQuantile(0.95, 9.876, 0.6789), 1e-6);
	}
	
	@Test
	public void testIncGammaRatio() {
		assertEquals(0.3934693, Gamma.incGammaRatio(0.5, 1.0), 1e-6);
		assertEquals(0.3009707, Gamma.incGammaRatio(1.1, 2.0), 1e-6);
		assertEquals(0.7483561, Gamma.incGammaRatio(1.1, 0.8), 1e-6);
		assertEquals(0.566861, Gamma.incGammaRatio(1005, 1000), 1e-6);
	}
	
	@Test
	public void testGammaCDF() {
		assertEquals(0.04877058, Gamma.gammaCDF(0.1, 1, 2), 1e-6);
		assertEquals(0.3623718, Gamma.gammaCDF(0.9, 1, 2), 1e-6);
		assertEquals(1.966058e-15, Gamma.gammaCDF(0.1, 9.876, 0.6789), 1e-10);
		assertEquals(1.802166e-06, Gamma.gammaCDF(0.9, 9.876, 0.6789), 1e-10);
	}
	
	@Test
	public void testGetDiscreteGammaDensities() {
		double rates[];
		// Multiple categories.
		rates = Gamma.getDiscreteGammaCategories(4, 1.5, 1/1.5);
		assertEquals(0.2252171, rates[0], 1e-3);
		assertEquals(2.135653, rates[3], 1e-3);
		rates = Gamma.getDiscreteGammaCategories(2, 1.0, 2.0);
		assertEquals(0.613932, rates[0], 1e-2);
		assertEquals(3.387487, rates[1], 1e-2);
		rates = Gamma.getDiscreteGammaCategories(4, 3.0, 2.0);
		assertEquals(2.336258, rates[0], 1e-3);
		assertEquals(10.78414, rates[3], 1e-2);
		rates = Gamma.getDiscreteGammaCategories(20, 9.0, 0.5);
		assertEquals(1.986476, rates[0], 1e-4);
		assertEquals(8.132877, rates[19], 1e-4);
		// Single category.
		rates = Gamma.getDiscreteGammaCategories(1, 1.0, 2.0);
		assertEquals(1.0, rates[0], 1e-7);
	}
}
