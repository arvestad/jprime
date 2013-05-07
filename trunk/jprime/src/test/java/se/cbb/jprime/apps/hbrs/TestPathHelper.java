package se.cbb.jprime.apps.hbrs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.Test;

import se.cbb.jprime.apps.hbrs.PathHelper;
import se.cbb.jprime.io.GMLIOException;
import se.cbb.jprime.io.HybridGraphReader;
import se.cbb.jprime.topology.HybridGraph;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestPathHelper {

	@Test
	public void testPaths() throws GMLIOException, IOException {
		
		URL url = this.getClass().getResource("/phylogenetics/hybrid_graph_w_extinctions.gml");
		File f = new File(url.getFile());
		HybridGraph dag = HybridGraphReader.readHybridGraph(f, 3, 10, 0.1, 7);
		PathHelper paths = new PathHelper(dag);
		System.out.println(paths);
		assertTrue(paths != null);
	}
}
