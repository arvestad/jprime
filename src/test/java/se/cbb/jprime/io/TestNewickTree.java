package se.cbb.jprime.io;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * JUnit Test Case.
 * 
 * @author Vincent Llorens.
 */
public class TestNewickTree {

	@Test
	public void copyConstructor() throws NewickIOException {
		NewickTree tree = NewickTreeReader.readTree("(a1,(a2,(a3,b3)b2)b1,(a2,b2,c2)c1)root;", false);
		NewickTree treeCopy = new NewickTree(tree);
		List<NewickVertex> treeVertices = tree.getVerticesAsList();
		List<NewickVertex> treeCopyVertices = treeCopy.getVerticesAsList();
		for (int i = 0; i < treeVertices.size(); i++) {
			for (int j = 0; j < treeCopyVertices.size(); j++) {
				assertNotSame(treeVertices.get(i), treeCopyVertices.get(j));
			}
		}
	}
	
	@Test
	public void getEdgeBranchLength() throws NewickIOException {
		NewickTree treeWithLengths = NewickTreeReader.readTree("((a:0.1,b:0.2)e:0.3,c:0.4,d:0.5)r;", false);
		NewickVertex e = treeWithLengths.getVertex(2);
		NewickVertex b = treeWithLengths.getVertex(1);
		NewickVertex c = treeWithLengths.getVertex(3);
		NewickVertex r = treeWithLengths.getVertex(5);
		assertEquals(treeWithLengths.getEdgeBranchLength(e, b), 0.2, 0);
		assertEquals(treeWithLengths.getEdgeBranchLength(c, r), treeWithLengths.getEdgeBranchLength(r, c), 0);
	}
	
	@Test(expected=NewickIOException.class)
	public void getEdgeBranchLengthNonAdjacent() throws NewickIOException {
		NewickTree treeWithLengths = NewickTreeReader.readTree("((a:0.1,b:0.2)e:0.3,c:0.4,d:0.5)r;", false);
		NewickVertex a = treeWithLengths.getVertex(0);
		NewickVertex r = treeWithLengths.getVertex(5);
		treeWithLengths.getEdgeBranchLength(a, r);
	}

	@Test
	public void getMapEdgeBranchLength() throws NewickIOException {
		NewickTree treeWithLengths = NewickTreeReader.readTree("((a:0.1,b:0.2)c:0.4,d)r;", false);
		NewickVertex a = treeWithLengths.getVertex(0);
		NewickVertex b = treeWithLengths.getVertex(1);
		NewickVertex c = treeWithLengths.getVertex(2);
		NewickVertex d = treeWithLengths.getVertex(3);
		NewickVertex r = treeWithLengths.getVertex(4);
		assertEquals(treeWithLengths.getEdgeBranchLength(a, c), 0.1, 0);
		assertEquals(treeWithLengths.getEdgeBranchLength(b, c), 0.2, 0);
		assertEquals(treeWithLengths.getEdgeBranchLength(c, r), 0.4, 0);
		assertNull(treeWithLengths.getEdgeBranchLength(d, r));
	}
	
	@Test
	public void setEdgeBranchLength() throws NewickIOException {
		NewickTree treeWithLengths = NewickTreeReader.readTree("((a:0.1,b:0.2)c:0.4,d)r;", false);
		NewickVertex d = treeWithLengths.getVertex(3);
		d.setBranchLength(0.3);
		assertEquals(treeWithLengths.getVertex(3).getBranchLength(), 0.3, 0);
	}
}
