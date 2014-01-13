package se.cbb.jprime.topology;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;
import static org.junit.Assert.*;

/**
 * JUnit test case.
 * 
 * @author Joel Sjöstrand.
 */
public class TestRTree {
	
	@Test
	public void testLCA() throws NewickIOException, IOException, TopologyException {
		URL url = this.getClass().getResource("/phylogenetics/molli.host.nw");
		PrIMENewickTree rawTree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		RTree tree = new RTree(rawTree, "molli");
		NamesMap names = rawTree.getVertexNamesMap(true, "tree.names");
		int x = names.getVertex("MYCH2");
		assertEquals(x, tree.getLCA(x, x));
		int y = names.getVertex("URPAR");
		int z = names.getVertex("MYPEN");
		assertEquals(22, tree.getLCA(y, z));
		assertEquals(24, tree.getLCA(x, y));
		assertEquals(26, tree.getLCA(0, 11));
		assertEquals(12, tree.getLCA(10, 12));
		assertEquals(24, tree.getLCA(12, 24));
	}
	
}
