package se.cbb.jprime.io;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestNewickStringAlgorithms {

	@Test
	public void testStrip() {
		String t = "[ kjdsfh ksjhf   kjsfd  /&%/&% 37478] ((A,B[&&PRIME sfd]),(C[jhdf kjf ],D)[&&PRIME sfd])[&&PRIME sfd];  [dsf dsfsdf ]";
		assertEquals("((A,B[&&PRIME sfd]),(C,D)[&&PRIME sfd])[&&PRIME sfd];", NewickStringAlgorithms.strip(t, 100).toString());
	}
	
}
