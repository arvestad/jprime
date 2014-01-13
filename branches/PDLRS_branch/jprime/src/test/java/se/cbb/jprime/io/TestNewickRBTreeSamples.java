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
		NewickRBTreeSamples treeSample = NewickRBTreeSamples.readTreesWithoutLengths(this.shortRun, true, 1, 0, 0.0);
		// Testing
		assertEquals(treeSample.getNoOfTrees(), 3);
		assertEquals(treeSample.getTotalTreeCount(), 10);
		assertEquals(treeSample.getTreeCount(0), 7);
		assertEquals(treeSample.getTreeCount(1), 2);
		assertEquals(treeSample.getTreeCount(2), 1);
		for (int i = 0; i < treeSample.getNoOfTrees(); i++) {
			assertNull(treeSample.getTreeBranchLengths(i));
		}
		String[] nwTopologies = {
				"((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2)),((Cavia_porcellus_1,Mus_musculus_1),(Cavia_porcellus_2,Oryctolagus_cuniculus_1)));",
				"((((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),((Cavia_porcellus_3,Oryctolagus_cuniculus_2),Mus_musculus_2)),Oryctolagus_cuniculus_1),Cavia_porcellus_2),(Cavia_porcellus_1,Mus_musculus_1));",
				"(((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2)),(Cavia_porcellus_2,Oryctolagus_cuniculus_1)),(Cavia_porcellus_1,Mus_musculus_1));"
		};
		for (int i = 0; i < treeSample.getNoOfTrees(); i++) {
			assertEquals(treeSample.getTreeNewickString(i), nwTopologies[i]);
		}
	}
	
	@Test
	public void testLongRun() throws FileNotFoundException, NewickIOException, TopologyException {
		// Initialization
		NewickRBTreeSamples treeSample = NewickRBTreeSamples.readTreesWithLengths(this.longRun, true, 1, 0, 0.0);
		// Testing
		assertEquals(treeSample.getNoOfTrees(), 81);
		assertEquals(treeSample.getTotalTreeCount(), 1001);
		assertEquals(treeSample.getTreeCount(0), 234);
		assertEquals(treeSample.getTreeCount(1), 98);
		assertEquals(treeSample.getTreeCount(2), 82);
		for (int i = 0; i < treeSample.getNoOfTrees(); i++) {
			assertNotNull(treeSample.getTreeBranchLengths(i));
		}
		String[] nwTopologies = {
				"((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),(((Cavia_porcellus_1,Cavia_porcellus_2),Mus_musculus_1),Oryctolagus_cuniculus_1)),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2));",
				"(((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),((((Cavia_porcellus_1,Cavia_porcellus_2),Mus_musculus_1),Oryctolagus_cuniculus_1),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2)));",
				"((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),(Cavia_porcellus_2,Mus_musculus_1)),((Cavia_porcellus_1,Oryctolagus_cuniculus_1),Equus_caballus_1)),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2));",
				"((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),(((Cavia_porcellus_1,Mus_musculus_1),Cavia_porcellus_2),Oryctolagus_cuniculus_1)),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2));",
				"(((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),(((Cavia_porcellus_1,Mus_musculus_1),(Cavia_porcellus_2,Oryctolagus_cuniculus_1)),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2)));"		};
		for (int i = 0; i < 5; i++) {
			assertEquals(treeSample.getTreeNewickString(i), nwTopologies[i]);
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
