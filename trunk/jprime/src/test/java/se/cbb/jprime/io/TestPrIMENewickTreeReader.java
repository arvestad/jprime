package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.TreeSet;

import org.junit.* ;

import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;
import se.cbb.jprime.topology.NamesMap;
import static org.junit.Assert.*;

/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestPrIMENewickTreeReader {

	@Test
	public void readSingleTreeFromFile() throws IOException, NewickIOException {
		URL url = this.getClass().getResource("/molli.host.nw");
		PrIMENewickTree tree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		assertEquals(14, tree.getNoOfLeaves());
		assertTrue(tree.hasProperty(MetaProperty.BRANCH_LENGTHS));
		assertEquals("AYWBP", tree.getVertexNames()[0]);
		assertEquals("URPAR", tree.getVertexNames()[21]);
		NamesMap names = tree.getNamesMap();
		assertEquals("AYWBP", names.get(0));
		assertEquals(0, names.getVertex("AYWBP"));
		assertEquals(21, names.getVertex("URPAR"));
		TreeSet<String> namesSorted = names.getNamesSorted(true);
		assertEquals("AYWBP", namesSorted.first());
		assertEquals("URPAR", namesSorted.last());
		assertEquals(14, namesSorted.size());
	}
	
}
