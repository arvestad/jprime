package se.cbb.jprime.apps.dltrs;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
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
public class TestEpochPtPtMap {
	
	@Test
	public void test() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/simple.03.nw");
		PrIMENewickTree rawTree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RBTree tree = new RBTree(rawTree, "Tree");
		NamesMap names = rawTree.getVertexNamesMap(true, "Names");
		TimesMap times = rawTree.getTimesMap("Times");
		RBTreeEpochDiscretiser disc = new RBTreeEpochDiscretiser(tree, names, times, 2, 2, 0.05, 5);
		EpochPtPtMap pts = new EpochPtPtMap(disc);
		assertEquals(0.0, pts.get(0, 0, 0, 0, 0, 2), 1e-6);
		pts.reset(1.0);
		assertEquals(1.0, pts.get(2, 0, 0, 0, 6, 0), 1e-6);
		//System.out.println(pts);
	}
	
}
