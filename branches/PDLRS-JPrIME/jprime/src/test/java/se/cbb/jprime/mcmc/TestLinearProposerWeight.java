package se.cbb.jprime.mcmc;

import org.junit.*;

import static org.junit.Assert.*;

public class TestLinearProposerWeight {

	@Test
	public void testIncreasingWeight() {
		Iteration iter = new Iteration(2000);
		LinearProposerWeight weight = new LinearProposerWeight(iter, 12.3, 45.6);
		assertTrue(Math.abs(12.3 - weight.getValue()) < 1e-6);
		while (iter.increment()) {};
		assertEquals(2000, iter.getIteration());
		assertTrue(Math.abs(45.6 - weight.getValue()) < 1e-6);
	}
	
	@Test
	public void testDecreasingWeight() {
		Iteration iter = new Iteration(2000);
		LinearProposerWeight weight = new LinearProposerWeight(iter, 98.7, 65.4);
		assertTrue(Math.abs(98.7 - weight.getValue()) < 1e-6);
		while (iter.increment()) {};
		assertEquals(2000, iter.getIteration());
		assertTrue(Math.abs(65.4 - weight.getValue()) < 1e-6);
	}
	
}
