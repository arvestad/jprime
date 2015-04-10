package se.cbb.jprime.math;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test.
 * 
 * @author Joel Sjöstrand.
 */
public class TestChiSquared {

	@Test
	public void testQuantile() {
		assertEquals(0.00393214, ChiSquared.quantile(0.05, 1.0), 1e-10);
		assertEquals(3.841459, ChiSquared.quantile(0.95, 1.0), 1e-6);
		assertEquals(6.531184e-17, ChiSquared.quantile(0.05, 0.16), 1e-10);
		assertEquals(0.9304065, ChiSquared.quantile(0.95, 0.16), 1e-6);
	}
	
}
