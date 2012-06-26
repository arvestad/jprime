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
			assertNotSame(treeVertices.get(i), treeCopyVertices.get(i));
		}
	}

}
