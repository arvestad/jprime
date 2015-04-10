package se.cbb.jprime.math;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for NormalDistribution.
 * 
 * @author Joel Sjöstrand.
 */
public class TestNormalDistribution {

	private NormalDistribution n = new NormalDistribution(0.0, 1.0);
	private NormalDistribution pos = new NormalDistribution(123.45, 145.6);
	private NormalDistribution neg = new NormalDistribution(-23.45, 11.34);
	
	@Test
	public void testPDF() {
		assertEquals(0.3989423, n.getPDF(0.0), 1e-7);
		assertEquals(4.167399e-08, n.getPDF(5.67), 1e-8);
		assertEquals(4.167399e-08, n.getPDF(-5.67), 1e-8);
		
		assertEquals(0.03306202, pos.getPDF(123.45), 1e-8);
		assertEquals(6.174496e-25, pos.getPDF(0.0), 1e-26);
		assertEquals(0.002937925, pos.getPDF(150.0), 1e-9);
		
		assertEquals(0.1184687, neg.getPDF(-23.45), 1e-7);
		assertEquals(3.763361e-15, neg.getPDF(-50.0), 1e-20);
		assertEquals(3.496535e-12, neg.getPDF(0.0), 1e-15);
	}
	
	@Test
	public void testCDF() {
		assertEquals(0.5, n.getCDF(0.0), 1e-6);
		assertEquals(0.8413447, n.getCDF(1.0), 1e-6);
		assertEquals(0.1586553, n.getCDF(-1.0), 1e-6);
		
		assertEquals(0.5, pos.getCDF(123.45), 1e-6);
		assertEquals(7.26699e-08, pos.getCDF(60), 1e-9);
		assertEquals(0.9987734, pos.getCDF(160), 1e-6);
		
		assertEquals(0.5, neg.getCDF(-23.45), 1e-7);
		assertEquals(1.582701e-15, neg.getCDF(-50.0), 1e-16);
		assertEquals(0.9999675, neg.getCDF(-10.0), 1e-6);
	}
	
	@Test
	public void testGetQuantile() {
		assertEquals(0.0, n.getQuantile(0.5), 1e-10);
		assertEquals(-1.880794, n.getQuantile(0.03), 1e-3);
		assertEquals(1.880794, n.getQuantile(0.97), 1e-3);
		assertEquals(-2.326348, n.getQuantile(0.01), 1e-4);
		assertEquals(2.326348, n.getQuantile(0.99), 1e-4);
	
		assertEquals(123.45, pos.getQuantile(0.5), 1e-10);
		assertEquals(100.7554, pos.getQuantile(0.03), 1e-2);
		assertEquals(146.1446, pos.getQuantile(0.97), 1e-2);
		assertEquals(95.37916, pos.getQuantile(0.01), 1e-3);
		assertEquals(151.5208, pos.getQuantile(0.99), 1e-3);
		
		assertEquals(-23.45, neg.getQuantile(0.5), 1e-10);
		assertEquals(-29.78356, neg.getQuantile(0.03), 1e-3);
		assertEquals(-17.11644, neg.getQuantile(0.97), 1e-3);
		assertEquals(-31.28396, neg.getQuantile(0.01), 1e-3);
		assertEquals(-15.61604, neg.getQuantile(0.99), 1e-3);
	}
	
	@Test
	public void testMisc() {
		assertEquals(Math.sqrt(11.34)/23.45, neg.getCV(), 1e-6);
	}
}
