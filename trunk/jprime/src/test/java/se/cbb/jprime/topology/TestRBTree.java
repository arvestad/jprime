package se.cbb.jprime.topology;

import static org.junit.Assert.*;

import org.junit.Test;

import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;

/**
 * JUnit test case.
 * 
 * @author Vincent Llorens.
 */
public class TestRBTree {

	@Test
	public void testSwapOne() throws NewickIOException, TopologyException {
		NewickTree t = NewickTreeReader.readTree("((((A,B)C,D)E,F)G,H)I;", false);
		RBTree rbt = new RBTree(t, "Dummy");
		rbt.swap(2, 5);
		int[] expectedParents = {5, 5, 6, 4, 6, 4, 8, 8, -1};
		int[] expectedLeftChildren = {-1, -1, -1, -1, 5, 0, 4, -1, 6};
		int[] expectedRightChildren = {-1, -1, -1, -1, 3, 1, 2, -1, 7};
		for (int n = 0; n < rbt.getNoOfVertices(); n++) {
			assertEquals(rbt.getParent(n), expectedParents[n]);
			assertEquals(rbt.getLeftChild(n), expectedLeftChildren[n]);
			assertEquals(rbt.getRightChild(n), expectedRightChildren[n]);
		}
		assertEquals(true, isConsistent(rbt));
	}

	@Test
	public void testSwapTwo() throws NewickIOException, TopologyException {
		NewickTree t = NewickTreeReader.readTree("((A,B)C,(D,E)F)G;", false);
		RBTree rbt = new RBTree(t, "Dummy");
		rbt.swap(2, 5);
		int[] expectedParents = {5, 5, 6, 2, 2, 6, -1};
		int[] expectedLeftChildren = {-1, -1, 3, -1, -1, 0, 5};
		int[] expectedRightChildren = {-1, -1, 4, -1, -1, 1, 2};
		for (int n = 0; n < rbt.getNoOfVertices(); n++) {
			assertEquals(rbt.getParent(n), expectedParents[n]);
			assertEquals(rbt.getLeftChild(n), expectedLeftChildren[n]);
			assertEquals(rbt.getRightChild(n), expectedRightChildren[n]);
		}
		assertEquals(true, isConsistent(rbt));
	}
	
	@Test
	public void testSwapNeighbours()  throws NewickIOException, TopologyException {
		NewickTree t = NewickTreeReader.readTree("((A,B)C,D)E;", false);
		RBTree rbt = new RBTree(t, "Dummy");
		rbt.swap(0, 2);
		int[] expectedParents = {4, 0, 0, 4, -1};
		int[] expectedLeftChildren = {2, -1, -1, -1, 0};
		int[] expectedRightChildren = {1, -1, -1, -1, 3};
		for (int n = 0; n < rbt.getNoOfVertices(); n++) {
			assertEquals(rbt.getParent(n), expectedParents[n]);
			assertEquals(rbt.getLeftChild(n), expectedLeftChildren[n]);
			assertEquals(rbt.getRightChild(n), expectedRightChildren[n]);
		}
		assertEquals(true, isConsistent(rbt));
	}
	
	@Test
	public void testSwapPermutability() throws NewickIOException, TopologyException {
		NewickTree t = NewickTreeReader.readTree("((((A,B)C,D)E,F)G,H)I;", false);
		RBTree rbt = new RBTree(t, "Dummy");
		RBTree rbtCopy = new RBTree(rbt);
		rbt.swap(2, 5);
		rbtCopy.swap(2, 5);
		for (int n = 0; n < rbt.getNoOfVertices(); n++) {
			assertEquals(rbt.getParent(n), rbtCopy.getParent(n));
			assertEquals(rbt.getLeftChild(n), rbtCopy.getLeftChild(n));
			assertEquals(rbt.getRightChild(n), rbtCopy.getRightChild(n));
		}
		assertEquals(true, isConsistent(rbt));
		assertEquals(true, isConsistent(rbtCopy));
	}
	
	/**
	 * Checks the consistency of a RBTree by checking that a child have the right parent.
	 * @param t the RBTree
	 * @return true if the RBTree is consistent.
	 */
	private boolean isConsistent(RBTree t) {
		boolean res = true;
		if (t.parents.length != t.leftChildren.length || t.parents.length != t.rightChildren.length) {
			res = false;
		} else {
			for (int n = 0; n < t.parents.length; n++) {
				if (t.parents[n] != -1 &&
						!(n == t.leftChildren[t.parents[n]] || n == t.rightChildren[t.parents[n]])) {
					res = false;
					break;
				}
			}
		}
		return res;
	}
}
