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
		URL url = this.getClass().getResource("/phylogenetics/molli.host.nw");
		PrIMENewickTree tree = PrIMENewickTreeReader.readTree(new File(url.getFile()), false, true);
		assertEquals(14, tree.getNoOfLeaves());
		assertTrue(tree.hasProperty(MetaProperty.BRANCH_LENGTHS));
		String[] names = tree.getVertexNames(true);
		assertEquals("AYWBP", names[0]);
		assertEquals("URPAR", names[21]);
		NamesMap namesMap = tree.getVertexNamesMap(true, "T.names");
		assertEquals("AYWBP", namesMap.get(0));
		assertEquals(0, namesMap.getVertex("AYWBP"));
		assertEquals(21, namesMap.getVertex("URPAR"));
		TreeSet<String> namesSorted = namesMap.getNamesSorted(true);
		assertEquals("AYWBP", namesSorted.first());
		assertEquals("URPAR", namesSorted.last());
		assertEquals(14, namesSorted.size());
		namesMap = tree.getVertexNamesMap(false, "T.names");
		assertNotSame(14, namesMap.getNamesSorted(false).size());
		assertEquals(14, namesMap.getNamesSorted(true).size());
	}
	
}
