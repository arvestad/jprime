package se.cbb.jprime.apps.gsrf;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;
import static org.junit.Assert.*;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestDupLossProbs {
	
	@Test
	public void test() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/simple.05.nw");
		PrIMENewickTree rawTree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RBTree s = new RBTree(rawTree, "S");
		TimesMap pureTimes = rawTree.getTimesMap("Times");
		RBTreeArcDiscretiser times = new RBTreeArcDiscretiser(s, pureTimes, 3, 5, 0.05, 5);
		DupLossProbs dupLoss = new DupLossProbs(s, times, new DoubleParameter("Lambda", 0.5), new DoubleParameter("Mu", 0.4));
		System.out.println(dupLoss.toString());
	}
	
}
