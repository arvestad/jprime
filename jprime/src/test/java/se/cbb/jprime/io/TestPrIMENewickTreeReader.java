package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.* ;

import se.cbb.jprime.io.PrIMENewickTree.MetaProperty;
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
		assertEquals("MYPUL", tree.getVertexNames()[10]);
	}
	
}
