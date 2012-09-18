package se.cbb.jprime.apps.dlrs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.apps.dlrs.DLRSModel;
import se.cbb.jprime.apps.dlrs.DupLossProbs;
import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.math.GammaDistribution;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestDLRSModel {
	
	@Test
	public void test() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/simple.05.nw");
		PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RBTree s = new RBTree(sRaw, "S");
		TimesMap pureTimes = sRaw.getTimesMap("Times");
		RBTreeArcDiscretiser times = new RBTreeArcDiscretiser(s, pureTimes, 3, 8, 0.05, 5);
		NamesMap sNames = sRaw.getVertexNamesMap(true, "S.names");
		DupLossProbs dupLoss = new DupLossProbs(s, times, new DoubleParameter("Lambda", 0.5), new DoubleParameter("Mu", 0.4));
		url = this.getClass().getResource("/phylogenetics/simple.09.guest.nw");
		PrIMENewickTree gRaw = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, false);
		RBTree g = new RBTree(gRaw, "G");
		NamesMap gNames = gRaw.getVertexNamesMap(false, "G.names");
		url = this.getClass().getResource("/phylogenetics/simple.09.to.simple.05.gs");
		MPRMap gsMap = new MPRMap(GuestHostMapReader.readGuestHostMap(new File(url.getFile())), g, gNames, s, sNames);
		ReconciliationHelper rHelper = new ReconciliationHelper(g, s, times, gsMap, 100);
		DoubleMap lengths = new DoubleMap("Lengths", g.getNoOfVertices(), 0.1);
		GammaDistribution pd = new GammaDistribution(new DoubleParameter("m", 0.1),
				new DoubleParameter("v", 0.05));
		DLRSModel mod = new DLRSModel(g, s, rHelper, lengths, dupLoss, pd);
		//System.out.println(mod);
		//TODO: We could use better tests on more realistic data.
		assertTrue(mod != null);
	}
	
}
