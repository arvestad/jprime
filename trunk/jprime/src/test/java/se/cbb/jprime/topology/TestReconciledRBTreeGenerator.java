package se.cbb.jprime.topology;

import static org.junit.Assert.*;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.misc.Pair;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestReconciledRBTreeGenerator {

	@Test
	public void test() throws NewickIOException, TopologyException {
		String newickS = "(((A,B),C),D);";
		NewickTree rawS = NewickTreeReader.readTree(newickS, false);
		RBTree S = new RBTree(rawS, "S");
		NamesMap SNames = rawS.getVertexNamesMap(true, "S.names");
		GuestHostMap GSMap = new GuestHostMap();
		GSMap.add("a0", "A");
		GSMap.add("a1", "A");
		GSMap.add("b0", "B");
		GSMap.add("d0", "D");
		GSMap.add("d1", "D");
		GSMap.add("d2", "D");
		PRNG prng = new PRNG();
		
		Pair<RBTree,NamesMap> GGNames = ReconciledRBTreeGenerator.createRandomSimplisticGuestTree(GSMap, S, SNames, "G", prng);
		RBTree G = GGNames.first;
		NamesMap GNames = GGNames.second;
		
		assertEquals(6, GNames.getNames(false).size());
		assertEquals(1, GNames.getVertex("a1"));
		assertEquals(6, G.getNoOfLeaves());
		assertEquals(10, G.getRoot());
		assertEquals(4, G.getLeftChild(G.getRoot()));
		assertEquals(9, G.getRightChild(G.getRoot()));
	}
}
