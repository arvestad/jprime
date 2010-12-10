package se.cbb.jprime.io;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.topology.DoubleMap;
import static org.junit.Assert.*;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestPrIMENewickTree {
	
	private String in = "(A:12[&&PRIME ID=0 NT=21],(B:23[&&PRIME ID=1 NT=21],(C:34[&&PRIME ID=2 NT=21],D:45[&&PRIME ID=3 NT=21 BL=450]," +
			"E[&&PRIME ID=4 NT=21])F[&&PRIME ID=5 NT=21])G[&&PRIME ID=6 NT=21])H[&&PRIME ID=7 NT=22]" +
			"[&&PRIME NAME=TestTree TT=1.0e2];";
	
	@Test
	public void readUnstrict() throws NewickIOException {
		PrIMENewickTree t = PrIMENewickTreeReader.readTree(in, true, false);
		assertTrue(t.getTreeName().equals("TestTree"));
		assertTrue(t.hasProperty(MetaProperty.BRANCH_LENGTHS));
		assertFalse(t.hasProperty(MetaProperty.ARC_TIMES));
		assertTrue(t.hasProperty(MetaProperty.VERTEX_TIMES));
		DoubleMap bls = t.getBranchLengthsMap();
		assertTrue(bls.get(0) == 12);
		assertTrue(t.getVertexTimes()[7] == 22);
		
		// Verify overridden number and branch length.
		assertTrue(bls.get(3) == 450);
		assertTrue(t.getTreeTopTime() > 0);
	}
	
	@Test(expected=NewickIOException.class)
	public void readStrict() throws NewickIOException {
		// Input tree lacks some branch lengths.
		PrIMENewickTreeReader.readTree(in, true, true);
	}
}
