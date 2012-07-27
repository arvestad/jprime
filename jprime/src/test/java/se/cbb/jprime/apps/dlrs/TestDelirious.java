package se.cbb.jprime.apps.dlrs;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.cbb.jprime.apps.dlrs.Delirious;

/**
 * Unit tests.
 * 
 * @author Joel Sjöstrand.
 */
public class TestDelirious {
	
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
		Delirious.main(new String[]{});
		assertTrue(out.toString().startsWith("========="));
		out.reset();
		Delirious.main(new String[]{ "-h" });
		assertTrue(out.toString().startsWith("========="));
		out.reset();
	}
}
