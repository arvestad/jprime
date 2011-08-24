package se.cbb.jprime.apps.phylotools;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.* ;

import se.cbb.jprime.io.GuestHostMapReader;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestInparalogueFilterer {
	
	@Test
	public void testInparalogueFilterer() throws NewickIOException, IOException, TopologyException {
		// Contrived case.
		PrIMENewickTree SRaw = PrIMENewickTreeReader.readTree("((A,B),C);", false, false);
		PrIMENewickTree GRaw = PrIMENewickTreeReader.readTree("((a1:0.5,a2:0.6):0.8,(a3:0.2,a4:0.3):0.9):0.3;", false, false);
		GuestHostMap GS = GuestHostMapReader.readGuestHostMap("a1\tA\na2\tA\na3\tA\na4\tA");
		String s = InparalogueFilterer.filterInparalogues(GRaw, SRaw, GS);
		assertEquals("a1:1.6;", s);
		// More complex case.
		SRaw = PrIMENewickTreeReader.readTree("(A,(B,C));", false, false);
		GRaw = PrIMENewickTreeReader.readTree("(((a1:0.5,a2:0.5):0.5,b1:0.5):0.5,((a3:0.5,a4:0.5):0.5,((c1:0.5,c2:0.5):0.5,c3:0.5):0.5):0.5):0.5;", false, false);
		GS = GuestHostMapReader.readGuestHostMap("a1\tA\na2\tA\na3\tA\na4\tA\nb1\tB\nc1\tC\nc2\tC\nc3\tC");
		s = InparalogueFilterer.filterInparalogues(GRaw, SRaw, GS);
		assertEquals("((a1:1.0,b1:0.5):0.5,(a3:1.0,c1:1.5):0.5):0.5;", s);
	}
	
}
