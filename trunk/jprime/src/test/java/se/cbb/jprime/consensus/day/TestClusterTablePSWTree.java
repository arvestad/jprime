package se.cbb.jprime.consensus.day;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.cbb.jprime.consensus.day.ClusterTablePSWTree;
import se.cbb.jprime.consensus.day.PSWVertex;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeReader;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.misc.IntTriple;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TopologyException;

/**
 * Unit test for ClusterTablePSWTree.
 * 
 * @author Joel Sjöstrand.
 */
public class TestClusterTablePSWTree {

	@Test
	public void testRootedTrees() throws NewickIOException, NullPointerException, TopologyException {
		// Tests two bifurcating rooted trees.
		String s1 = "(A,(((B,C),(D,E)),(F,G)));";
		String s2 = "(F,(((B,C),(D,E)),(A,G)));";
		NewickTree n1 = NewickTreeReader.readTree(s1, false);
		NewickTree n2 = NewickTreeReader.readTree(s2, false);
		RBTree t1 = new RBTree(n1, "t1");
		NamesMap names1 = n1.getVertexNamesMap(true);
		RBTree t2 = new RBTree(n2, "t2");
		NamesMap names2 = n2.getVertexNamesMap(true);
		ClusterTablePSWTree d1 = new ClusterTablePSWTree(t1, names1, false);
		ClusterTablePSWTree d2 = new ClusterTablePSWTree(t2, names2, false);
		assertTrue(d1.getNoOfLeaves() == 7);
		assertTrue(d2.getNoOfLeaves() == 7);
		PSWVertex r1 = d1.getRoot();
		PSWVertex r2 = d2.getRoot();
		assertNull(r1.getParent());
		assertEquals(12, r1.getWeight());
		assertEquals(r1.getMinMaxWeight(), new IntTriple(0,6,12));
		assertEquals(r2.getMinMaxWeight(), new IntTriple(0,6,12));
	}
	
	
	@Test
	public void testUnrootedTrees() throws NewickIOException, NullPointerException, TopologyException {
		// Tests two bifurcating unrooted trees.
		String s1 = "(((root,A),((B,C),(D,E))),(F,G));";
		String s2 = "(((root,F),(A,G)),((B,C),(D,E)));";
		NewickTree n1 = NewickTreeReader.readTree(s1, false);
		NewickTree n2 = NewickTreeReader.readTree(s2, false);
		RBTree t1 = new RBTree(n1, "t1");
		NamesMap names1 = n1.getVertexNamesMap(true);
		RBTree t2 = new RBTree(n2, "t2");
		NamesMap names2 = n2.getVertexNamesMap(true);
		ClusterTablePSWTree d1 = new ClusterTablePSWTree(t1, names1, true);
		ClusterTablePSWTree d2 = new ClusterTablePSWTree(t2, names2, true);
//		printTree(d1.getRoot(), 0);
//		System.out.println();
//		System.out.println();
//		printTree(d2.getRoot(), 0);
		assertEquals("root", d1.getRerootName());
		assertEquals("root", d2.getRerootName());
		assertEquals(7, d1.getNoOfLeaves());
		assertEquals(7, d2.getNoOfLeaves());
	}
	
	// For debugging trees.
	public int printTree(PSWVertex v, int no) {
		if (v.isLeaf()) {
			System.out.println("Visiting leaf with post-order number " + no +
					" with name " + v.getName());
		} else {
			for (PSWVertex c : v.getChildren()) {
				no = printTree(c, no);
			}
			System.out.println("Visiting interior vertex with post-order number " + no +
					" having " + v.getNoOfChildren() + " children.");
		}
		return (no + 1);
	}
	
	@Test
	public void testClusterTable() throws Exception {
		// Tests a bifurcating rooted tree.
		String s1 = "(A,(((B,C),(D,E)),(F,G)));";
		NewickTree n1 = NewickTreeReader.readTree(s1, true);
		RBTree t1 = new RBTree(n1, "t1");
		NamesMap names1 = n1.getVertexNamesMap(true);
		ClusterTablePSWTree d1 = new ClusterTablePSWTree(t1, names1, false);
		int sz = d1.getClusterTableSize();
		assertEquals(d1.getNoOfLeaves(), sz);
		assertEquals(d1.getMinMaxWeight(0), new IntTriple(0,6,12));
		assertEquals(d1.getMinMaxWeight(sz-1), new IntTriple(0,6,12));
		assertTrue(d1.contains(1,2,2));
		assertTrue(d1.contains(3,4,2));
		assertTrue(d1.contains(5,6,2));
		assertTrue(d1.contains(1,4,6));
		assertTrue(d1.contains(1,6,10));
		assertFalse(d1.contains(1,5,2));
	}
}
