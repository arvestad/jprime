package se.cbb.jprime.apps.lcaanalysis;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.junit.* ;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.topology.TopologyException;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestLCAAnalysis {

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(out));
	    System.setErr(new PrintStream(err));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	
	@Test
	public void testLCAAnalysis() throws NewickIOException, IOException, TopologyException {
		URL sURL = this.getClass().getResource("/molli.host.nw");
		URL gsURL = this.getClass().getResource("/molli.fam.gs");
		String[] args = new String[2];
		args[0] = sURL.getFile();
		args[1] = gsURL.getFile();
		LCAAnalysis.main(args);
		assertEquals("26\n", out.toString());
		out.reset();
		gsURL = this.getClass().getResource("/molli.fam2.gs");
		args[1] = gsURL.getFile();
		LCAAnalysis.main(args);
		assertEquals("7\n", out.toString());
		out.reset();
	}
	
}
