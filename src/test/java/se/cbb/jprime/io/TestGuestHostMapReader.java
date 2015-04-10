package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.* ;

import se.cbb.jprime.topology.GuestHostMap;
import static org.junit.Assert.*;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestGuestHostMapReader {

	@Test
	public void readGSMap() throws IOException, NewickIOException {
		URL url = this.getClass().getResource("/phylogenetics/molli.fam.gs");
		GuestHostMap gs = GuestHostMapReader.readGuestHostMap(new File(url.getFile()));
		assertEquals(15, gs.getGuestLeafNames("ONYEL").size());
		assertEquals(30, gs.getAllGuestLeafNames().size());
		assertTrue(gs.getHostLeafName("MYCCT_1_PE300").equals("MYCCT"));
		assertTrue(gs.getHostLeafName("MYCCT_1_PE545").equals("MYCCT"));
	}
	
}
