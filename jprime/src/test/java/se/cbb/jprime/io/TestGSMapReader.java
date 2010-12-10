package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.* ;

import se.cbb.jprime.topology.GSMap;
import static org.junit.Assert.*;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestGSMapReader {

	@Test
	public void readGSMap() throws IOException, NewickIOException {
		URL url = this.getClass().getResource("/molli.fam.gs");
		GSMap gs = GSMapReader.readGSMap(new File(url.getFile()));
		assertEquals(15, gs.getGuestLeafNames("ONYEL").size());
		assertEquals(30, gs.getAllGuestLeafNames().size());
		assertTrue(gs.getHostLeafName("MYCCT_1_PE300").equals("MYCCT"));
		assertTrue(gs.getHostLeafName("MYCCT_1_PE545").equals("MYCCT"));
	}
	
}
