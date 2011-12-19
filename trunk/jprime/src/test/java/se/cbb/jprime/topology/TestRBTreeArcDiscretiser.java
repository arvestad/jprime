package se.cbb.jprime.topology;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import static org.junit.Assert.*;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestRBTreeArcDiscretiser {
	
	@Test
	public void test() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/molli.host.nw");
		PrIMENewickTree rawTree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RBTree tree = new RBTree(rawTree, "Molli.Tree");
		TimesMap times = rawTree.getTimesMap("Molli.Times");
		RBTreeArcDiscretiser disc = new RBTreeArcDiscretiser(tree, times, 3, 7, 0.05, 10);
		System.out.println(disc);
		
	}
	
}
