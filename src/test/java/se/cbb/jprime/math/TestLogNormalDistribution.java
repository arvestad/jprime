package se.cbb.jprime.math;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for LogNormalDistribution.
 * 
 * @author Joel Sjöstrand.
 */
public class TestLogNormalDistribution {

	private LogNormalDistribution logn = new LogNormalDistribution(0.0, 1.0);
	private LogNormalDistribution lognpos = new LogNormalDistribution(123.45, 145.6);
	
	@Test
	public void testPDF() {
		assertEquals(0.01561430, logn.getPDF(5.67), 1e-8);
		assertEquals(2.643084e-25, lognpos.getPDF(150), 1e-8);
	}
	
	@Test
	public void testCDF() {
		assertEquals(0.0, logn.getCDF(0.0), 1e-6);
		assertEquals(0.5, logn.getCDF(1.0), 1e-6);
		assertEquals(0.7558914, logn.getCDF(2.0), 1e-6);
		
		assertEquals(4.82471e-23, lognpos.getCDF(150), 1e-6);
		assertEquals(9.332653e-18, lognpos.getCDF(1150000000), 1e-9);
	}
	
	@Test
	public void testGetQuantile() {
		assertEquals(1.0, logn.getQuantile(0.5), 1e-10);
		assertEquals(0.09765173, logn.getQuantile(0.01), 1e-4);
		assertEquals(10.24047, logn.getQuantile(0.99), 1e-3);
	
		assertEquals(4.108221e+53, lognpos.getQuantile(0.5), 1e48);
		assertEquals(2.646334e+41, lognpos.getQuantile(0.01), 1e38);
		assertEquals(6.377683e+65, lognpos.getQuantile(0.99), 1e63);
	}
	
	@Test
	public void testMisc() {
		LogNormalDistribution d = new LogNormalDistribution(2.2, 1.1);
		assertEquals(15.64436, d.getMean(), 1e-2);
		assertEquals(490.5789, d.getVariance(), 1e0);
		assertEquals(22.14902, d.getStandardDeviation(), 1e-2);
		assertEquals(22.14902/15.64436, d.getCV(), 1e-4);
		assertEquals(Math.exp(2.2), d.getMedian(), 1e-6);
		assertEquals(Math.exp(2.2-1.1), d.getMode(), 1e-6);
	}
}
