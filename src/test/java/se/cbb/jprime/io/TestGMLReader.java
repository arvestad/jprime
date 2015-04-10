package se.cbb.jprime.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestGMLReader {

	@Test
	public void testGML() throws GMLIOException, IOException {
		URL url = this.getClass().getResource("/phylogenetics/graph.gml");
		List<GMLKeyValuePair> gml = GMLReader.readGML(new File(url.getFile()));
		GMLGraph g = GMLFactory.getGraphs(gml).get(0);
		assertEquals(2, g.getNoOfNodes());
		assertEquals(1, g.getNoOfEdges());
		assertEquals("The principles of space travel", g.getLabel());
		assertEquals(1, g.getNodes().get(0).getId().intValue());
		assertEquals("Mars", g.getNodes().get(1).getLabel());
		assertEquals(2, g.getEdges().get(0).getTarget().intValue());
		assertEquals(1, g.getVersion().intValue());
		assertEquals("demo", g.getCreator());
	}
	
}
