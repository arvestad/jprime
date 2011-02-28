package se.cbb.jprime.mcmc;

import org.junit.*;

import static org.junit.Assert.*;

public class TestConstantThinner {

	@Test
	public void testDoSample() {
		Iteration iter = new Iteration(2000);
		ConstantThinner thinner = new ConstantThinner(iter, 50);
		assertTrue(thinner.doSample());
		int count = 1;
		for (int i = 0; i < 211; ++i) {
			iter.increment();
			if (thinner.doSample()) {
				count++;
			}
		}
		assertEquals(5, count);
		while (iter.increment()) {
			if (thinner.doSample()) {
				count++;
			}
		}
		assertEquals(2000, iter.getIteration());
		assertEquals(40 + 1, count);
		assertEquals(count, thinner.getTotalNoOfSamples());
	}
	
}
