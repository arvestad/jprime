package se.cbb.jprime.io;

import java.util.List;

import org.junit.* ;
import static org.junit.Assert.*;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;


/**
 * JUnit test class.
 * 
 * @author Joel Sjöstrand.
 */
public class TestNewickTreeReader {
	
	@Test
	public void readNonExistantTree() throws NewickIOException {
		String in = "";
		List<NewickTree> ts = NewickTreeReader.readTrees(in, true);
		assertNotNull(ts);
		assertTrue(ts.size() == 0);
	}
	
	
	@Test
	public void readNoNameTree() throws NewickIOException {
		// Multifurcating.
		String in = "  (,,(\n,\t));   \n\n";
		NewickTree t = NewickTreeReader.readTree(in, true);
		assertNotNull(t);
	}
	
	@Test
	public void readNameAndBLTrees() throws NewickIOException {
		String in = "(A:0.1,B:0.2,(C:0.3,D:0.4):0.5);\n" +
				"((A:0.1,B:0.2,(C:0.3,D:0.4)E:0.5)F:1e123,G:-234,(H:0.456,I:12.34)J:-12e-34)K:0;\n" +
				"((A[&&],B:123[&&]):245[&&],[&&My info tag])E[&&My second info tag!][&&My tree tag!!];";
		List<NewickTree> ts = NewickTreeReader.readTrees(in, true);
		NewickTree t0 = ts.get(0);
		NewickTree t1 = ts.get(1);
		NewickTree t2 = ts.get(2);
		assertNotNull(t0);
		assertNotNull(t1);
		assertSubtree(t0.getRoot(), true, true, false, true);
		assertTrue(t0.getNoOfVertices() == 6);
		assertTrue(t0.getRoot().getNumber() == 5);
		assertSubtree(t1.getRoot(), false, true, false, true);
		assertSubtree(t2.getRoot(), false, false, true, false);
		assertTrue(t2.getRoot().getMeta().equals("[&&My second info tag!]"));
		assertTrue(t2.getMeta().equals("[&&My tree tag!!]"));
	}
	
	
	public void assertSubtree(NewickVertex vertex, boolean onlyLeaves,
			boolean assertHasName, boolean assertHasMeta, boolean assertHasBL) {
		if (!onlyLeaves || vertex.isLeaf()) {
			assertTrue(!assertHasName || vertex.hasName());
			assertTrue(!assertHasMeta || vertex.hasMeta());
			assertTrue(!assertHasBL || vertex.hasBranchLength());
		}
		if (!vertex.isLeaf()) {
			for (NewickVertex child : vertex.getChildren()) {
				assertSubtree(child, onlyLeaves, assertHasName, assertHasMeta, assertHasBL);
			}
		}
	}
}
