package se.cbb.jprime.apps.dltrs;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;
import static org.junit.Assert.*;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestEpochDLTProbs {
	
	@Test
	public void test() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/simple.03.nw");
		PrIMENewickTree rawTree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RBTree tree = new RBTree(rawTree, "Tree");
		NamesMap names = rawTree.getVertexNamesMap(true, "Names");
		TimesMap times = rawTree.getTimesMap("Times");
		RBTreeEpochDiscretiser disc = new RBTreeEpochDiscretiser(tree, names, times, 2, 2, 0.05, 5);
		DoubleParameter dup = new DoubleParameter("Dup", 0.01);
		DoubleParameter loss = new DoubleParameter("Loss", 0.05);
		DoubleParameter trans = new DoubleParameter("Trans", 0.07);
		EpochDLTProbs probs = new EpochDLTProbs(disc, dup, loss, trans, true);
		//System.out.print(probs.toString());
		assertEquals(0.0, probs.getExtinctionProbs().get(0, 0, 0), 1e-6);
		assertTrue(probs.getExtinctionProbs().get(1, 1, 0) > 0.0);
		assertEquals(1.0, probs.getOneToOneProbs().get(1, 0, 0, 1, 0, 0), 1e-6);
		assertEquals(0.0, probs.getOneToOneProbs().get(1, 0, 0, 1, 0, 1), 1e-6);
		assertTrue(probs.getOneToOneProbs().get(0, 1, 2, 2, 3, 0) > 0.0);
	}
	
}
