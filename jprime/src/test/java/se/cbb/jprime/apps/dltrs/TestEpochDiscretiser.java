package se.cbb.jprime.apps.dltrs;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;
import static org.junit.Assert.*;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestEpochDiscretiser {
	
	@Test
	public void test() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/molli.host.nw");
		PrIMENewickTree rawTree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RBTree tree = new RBTree(rawTree, "Molli.Tree");
		TimesMap times = rawTree.getTimesMap("Molli.Times");
		EpochDiscretiser disc = new EpochDiscretiser(tree, times, 3, 7, 0.05, 10);
		//System.out.println(disc);
		assertEquals(14, disc.getNoOfEpochs());
	}
	
}
