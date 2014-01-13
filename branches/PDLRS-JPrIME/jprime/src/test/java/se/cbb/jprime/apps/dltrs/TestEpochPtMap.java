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
public class TestEpochPtMap {
	
	@Test
	public void test() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/molli.host.nw");
		PrIMENewickTree rawTree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RBTree tree = new RBTree(rawTree, "Molli.Tree");
		NamesMap names = rawTree.getVertexNamesMap(true, "Names");
		TimesMap times = rawTree.getTimesMap("Molli.Times");
		RBTreeEpochDiscretiser disc = new RBTreeEpochDiscretiser(tree, names, times, 3, 7, 0.05, 10);
		EpochPtMap pts = new EpochPtMap(disc);
		assertEquals(0.0, pts.getTopmost(), 1e-6);
		pts.reset(1.0);
		assertEquals(1.0, pts.getTopmost(), 1e-6);
		//System.out.println(pts);
	}
	
}
