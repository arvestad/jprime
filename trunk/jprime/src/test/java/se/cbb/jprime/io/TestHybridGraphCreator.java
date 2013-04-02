package se.cbb.jprime.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.topology.DAG;
import se.cbb.jprime.topology.DiscretisedArc;
import se.cbb.jprime.topology.EnumMap;
import se.cbb.jprime.topology.HybridVertexType;
import se.cbb.jprime.topology.NamesMap;

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
		
		Triple<DAG<DiscretisedArc>, NamesMap, EnumMap<HybridVertexType>> wrapper = HybridGraphCreator.createHybridGraph(f, 3, 10, 0.1, 7);
		DAG<DiscretisedArc> dag = wrapper.first;
		NamesMap names = wrapper.second;
		EnumMap<HybridVertexType> types = wrapper.third;
		
		assertEquals(31, dag.getNoOfVertices());
		assertEquals(1, dag.getSources().size());
		assertEquals(11, dag.getSinks().size());
		assertEquals("SK", names.get(10));
		assertEquals(HybridVertexType.STEM_TIP, types.get(30));
		assertEquals(HybridVertexType.LEAF, types.get(4));
		assertEquals(HybridVertexType.SPECIATION, types.get(17));
		assertEquals(HybridVertexType.HYBRID_DONOR, types.get(19));
		assertEquals(HybridVertexType.HYBRID_DONOR, types.get(22));
		assertEquals(HybridVertexType.ALLOPOLYPLOIDIC_HYBRID, types.get(25));
		assertEquals(HybridVertexType.AUTOPOLYPLOIDIC_HYBRID, types.get(23));
	}
}
