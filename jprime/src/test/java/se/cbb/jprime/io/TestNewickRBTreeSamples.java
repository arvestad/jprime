package se.cbb.jprime.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.junit.Test;


import se.cbb.jprime.topology.TopologyException;

/**
 * JUnit test case.
 * 
 * @author Vincent Llorens.
 */
public class TestNewickRBTreeSamples {
	
	private URL shortRunURL = this.getClass().getResource("/mcmc_output/short_run.mcmc");
	private File shortRun = new File(this.shortRunURL.getFile());
	private URL longRunURL = this.getClass().getResource("/mcmc_output/long_run_w_lengths.mcmc");
	private File longRun = new File(this.longRunURL.getFile());
	
	@Test
	public void testShortRun() throws FileNotFoundException, NewickIOException, TopologyException {
		// Initialization
		NewickRBTreeSamples tree = NewickRBTreeSamples.readTreesWithoutLengths(this.shortRun, true, 1, 0, 0.0);
		// Testing
		assertEquals(tree.getNoOfTrees(), 3);
		assertEquals(tree.getTotalTreeCount(), 10);
		assertEquals(tree.getTreeCount(0), 7);
		assertEquals(tree.getTreeCount(1), 2);
		assertEquals(tree.getTreeCount(2), 1);
		for (int i = 0; i < tree.getNoOfTrees(); i++) {
			assertNull(tree.getTreeBranchLengths(i));
		}
	}
	
	@Test
	public void testLongRun() throws FileNotFoundException, NewickIOException, TopologyException {
		// Initialization
		NewickRBTreeSamples tree = NewickRBTreeSamples.readTreesWithLengths(this.longRun, true, 1, 0, 0.0);
		// Testing
		assertEquals(tree.getNoOfTrees(), 81);
		assertEquals(tree.getTotalTreeCount(), 1001);
		assertEquals(tree.getTreeCount(0), 234);
		assertEquals(tree.getTreeCount(1), 98);
		assertEquals(tree.getTreeCount(2), 82);
		for (int i = 0; i < tree.getNoOfTrees(); i++) {
			assertNotNull(tree.getTreeBranchLengths(i));
		}
	}
	
	@Test
	public void testCvgShortRun() throws FileNotFoundException, NewickIOException, TopologyException {
		// Initialization
		NewickRBTreeSamples fullTreeSample = NewickRBTreeSamples.readTreesWithoutLengths(this.shortRun, true, 1, 0, 0.0);
		NewickRBTreeSamples bisFullTreeSample = NewickRBTreeSamples.readTreesWithoutLengths(this.shortRun, true, 1, 0, 0.1);
		NewickRBTreeSamples smallerTreeSample = NewickRBTreeSamples.readTreesWithoutLengths(this.shortRun, true, 1, 0, 0.2);
		NewickRBTreeSamples smallestTreeSample = NewickRBTreeSamples.readTreesWithoutLengths(this.shortRun, true, 1, 0, 0.7);
		assertEquals(fullTreeSample.getNoOfTrees(), 3);
		assertEquals(bisFullTreeSample.getNoOfTrees(), 3);
		assertEquals(smallerTreeSample.getNoOfTrees(), 2);
		assertEquals(smallestTreeSample.getNoOfTrees(), 1);
	}
}
