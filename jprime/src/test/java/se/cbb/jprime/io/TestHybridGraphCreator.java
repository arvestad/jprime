package se.cbb.jprime.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.topology.HybridGraph;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestHybridGraphCreator {

	@Test
	public void readGraphFromFile() throws GMLIOException, IOException {
		URL url = this.getClass().getResource("/phylogenetics/hybridnetwork.gml");
		File f = new File(url.getFile());
		HybridGraph dag = HybridGraphReader.readHybridGraph(f, 3, 10, 0.1, 7);
		
		assertEquals(31, dag.getNoOfVertices());
		assertEquals(1, dag.getSources().size());
		assertEquals(11, dag.getSinks().size());
		assertEquals("SK", dag.getVertexName(10));
		assertEquals(HybridGraph.VertexType.STEM_TIP, dag.getVertexType(30));
		assertEquals(HybridGraph.VertexType.LEAF, dag.getVertexType(4));
		assertEquals(HybridGraph.VertexType.SPECIATION, dag.getVertexType(17));
		assertEquals(HybridGraph.VertexType.HYBRID_DONOR, dag.getVertexType(19));
		assertEquals(HybridGraph.VertexType.HYBRID_DONOR, dag.getVertexType(22));
		assertEquals(HybridGraph.VertexType.ALLOPOLYPLOIDIC_HYBRID, dag.getVertexType(25));
		assertEquals(HybridGraph.VertexType.AUTOPOLYPLOIDIC_HYBRID, dag.getVertexType(23));
		
		
		url = this.getClass().getResource("/phylogenetics/hybrid_graph_w_extinctions.gml");
		f = new File(url.getFile());
		dag = HybridGraphReader.readHybridGraph(f, 3, 10, 0.1, 7);
		
		assertEquals(HybridGraph.VertexType.STEM_TIP, dag.getVertexType(20));
		assertEquals(HybridGraph.VertexType.LEAF, dag.getVertexType(5));
		assertEquals(HybridGraph.VertexType.SPECIATION, dag.getVertexType(19));
		assertEquals(HybridGraph.VertexType.HYBRID_DONOR, dag.getVertexType(12));
		assertEquals(HybridGraph.VertexType.EXTINCT_HYBRID_DONOR, dag.getVertexType(10));
		assertEquals(HybridGraph.VertexType.EXTINCT_HYBRID_DONOR, dag.getVertexType(6));
		assertEquals(HybridGraph.VertexType.ALLOPOLYPLOIDIC_HYBRID, dag.getVertexType(7));
		assertEquals(HybridGraph.VertexType.AUTOPOLYPLOIDIC_HYBRID, dag.getVertexType(9));
	}
}
