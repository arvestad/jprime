package se.cbb.jprime.apps.dltrs;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;

/**
 * Unit tests.
 * 
 * @author Joel Sjöstrand.
 */
public class TestDeleterious {
	
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
	public void testUsage() {
		Deleterious del = new Deleterious();
		del.main(new String[]{});
		assertTrue(out.toString().startsWith("========="));
		out.reset();
		del.main(new String[]{ "-h" });
		assertTrue(out.toString().startsWith("========="));
		out.reset();
	}
	
	@Test
	public void testMolli() throws NewickIOException, IOException {
		URL sURL = this.getClass().getResource("/phylogenetics/molli.host.nw");
		URL dURL = this.getClass().getResource("/phylogenetics/HBG562580.aln-gb");
		URL sigmaURL = this.getClass().getResource("/phylogenetics/HBG562580.gs");
		Deleterious del = new Deleterious();
		del.main(new String[] {"-uncatch", "-dmin", "3", "-dmax", "3", "-dstem", "4", "-i", "10", sURL.getPath(), dURL.getPath(), sigmaURL.getPath()});
		out.reset();
	}
}
