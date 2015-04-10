package se.cbb.jprime.math;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestUniformDistribution {

	private UniformDistribution ori = new UniformDistribution(-1.0, 1.0, true, true);
	private UniformDistribution neg = new UniformDistribution(-4, -3, false, false);
	private UniformDistribution pos = new UniformDistribution(0, 5, false, true);
	
	@Test
	public void testMisc() {
		assertEquals(0.5, ori.getPDF(-0.5), 1e-7);
		assertEquals(0.5, ori.getPDF(0), 1e-7);
		assertEquals(0, ori.getPDF(1), 1e-7);
		assertEquals(0, neg.getPDF(-0.5), 1e-8);
		assertEquals(0, neg.getPDF(0), 1e-8);
		assertEquals(0, neg.getPDF(1), 1e-8);
		assertEquals(0, pos.getPDF(-0.5), 1e-8);
		assertEquals(0.2, pos.getPDF(0), 1e-8);
		assertEquals(0.2, pos.getPDF(1), 1e-8);
		
		assertEquals(0, ori.getCDF(-3), 1e-7);
		assertEquals(1, ori.getCDF(1), 1e-7);
		assertEquals(1, neg.getCDF(-3), 1e-7);
		assertEquals(1, neg.getCDF(1), 1e-7);
		assertEquals(0, pos.getCDF(-3), 1e-7);
		assertEquals(0.2, pos.getCDF(1), 1e-7);
		
		assertEquals(-3.5, neg.getMedian(), 1e-10);
		assertEquals(-3.5, neg.getMode(), 1e-10);
		assertEquals(Math.sqrt(0.08333333), neg.getStandardDeviation(), 1e-6);
		assertEquals(0.08333333, neg.getVariance(), 1e-6);
		assertEquals(0.08247861, neg.getCV(), 1e-6);
	}
	
}
